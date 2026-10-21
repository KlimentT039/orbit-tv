package com.klt.orbit.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeSettingsRepository
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.GetRelatedVideosUseCase
import com.klt.orbit.domain.usecase.GetVideoUseCase
import com.klt.orbit.domain.usecase.ObserveSettingsUseCase
import com.klt.orbit.domain.usecase.PreparePlaybackUseCase
import com.klt.orbit.domain.usecase.SavePlaybackProgressUseCase
import com.klt.orbit.feature.player.engine.PlaybackFailure
import com.klt.orbit.feature.player.navigation.PlayerDestination
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val videos = FakeVideoRepository()
  private val progress = FakePlaybackProgressRepository()
  private val settings = FakeSettingsRepository()
  private val engine = FakeVideoPlayer()

  private val video = TestData.video(id = "a", keywords = listOf("Apollo"))
  private val nextVideo = TestData.video(id = "b")
  private val assets = TestData.assets("a")

  init {
    videos.videosById["a"] = Outcome.Success(video)
    videos.assetsById["a"] = Outcome.Success(assets)
    videos.videosByQuery["Apollo"] = Outcome.Success(listOf(video, nextVideo))
  }

  /**
   * Runs [block] against a ViewModel held in a [ViewModelStore] and clears it afterwards, as closing
   * the screen would. While a video plays the ViewModel polls the position in a loop; if its scope
   * were not cancelled, `runTest` would keep advancing virtual time through that loop and never end.
   *
   * @param ready whether the engine finishes buffering and starts playing right away.
   */
  private fun playerTest(
    startPositionMs: Long = 0,
    ready: Boolean = true,
    block: suspend TestScope.(PlayerHarness) -> Unit,
  ) = runTest {
    val favorites = FakeFavoritesRepository()
    val store = ViewModelStore()
    val factory =
      viewModelFactory {
        initializer {
          PlayerViewModel(
            SavedStateHandle(
              mapOf(PlayerDestination.VIDEO_ID_ARG to "a", PlayerDestination.START_POSITION_ARG to startPositionMs),
            ),
            { engine },
            PreparePlaybackUseCase(GetVideoUseCase(videos, favorites, progress), videos, progress),
            GetRelatedVideosUseCase(videos),
            ObserveSettingsUseCase(settings),
            SavePlaybackProgressUseCase(progress),
            backgroundScope,
          )
        }
      }
    val viewModel = ViewModelProvider.create(store, factory)[PlayerViewModel::class]
    if (ready) engine.becomeReady()
    try {
      block(PlayerHarness(viewModel, store))
    } finally {
      store.clear()
    }
  }

  private class PlayerHarness(
    val viewModel: PlayerViewModel,
    private val store: ViewModelStore,
  ) {
    val content: PlayerUiState.Content get() = viewModel.uiState.value as PlayerUiState.Content

    fun onAction(action: PlayerAction) = viewModel.onAction(action)

    fun close() = store.clear()
  }

  private fun saveProgress(positionMs: Long) {
    progress.setHistory(listOf(ContinueWatchingItem(video, PlaybackProgress("a", positionMs, 600_000, Instant.EPOCH))))
  }

  private suspend fun savedPositionMs(): Long? = progress.observeProgress("a").first()?.positionMs

  @Test
  fun `starts the best stream at the requested position with the controls showing`() =
    playerTest(startPositionMs = 30_000) { player ->
      assertThat(engine.loads).containsExactly(assets.bestStream.url to 30_000L)
      assertThat(player.content.title).isEqualTo(video.title)
      assertThat(player.content.controlsVisible).isTrue()
    }

  @Test
  fun `resumes from saved progress when no position is requested`() {
    saveProgress(120_000)

    playerTest(startPositionMs = PlayerDestination.RESUME_SAVED_POSITION) {
      assertThat(engine.loads.single().second).isEqualTo(120_000)
    }
  }

  @Test
  fun `shows an error when the video can't be opened, and retry loads it again`() {
    videos.assetsById["a"] = Outcome.Failure(DataError.Network)

    playerTest(ready = false) { player ->
      assertThat(player.viewModel.uiState.value).isEqualTo(PlayerUiState.Error(DataError.Network))

      videos.assetsById["a"] = Outcome.Success(assets)
      player.onAction(PlayerAction.Retry)

      assertThat(player.viewModel.uiState.value).isInstanceOf(PlayerUiState.Content::class.java)
      assertThat(engine.loads).hasSize(1)
    }
  }

  @Test
  fun `saves progress every few seconds while playing`() =
    playerTest {
      engine.positionMs = 42_000

      advanceTimeBy(PlayerViewModel.SAVE_INTERVAL_MS - 1_000)
      assertThat(savedPositionMs()).isNull()

      advanceTimeBy(1_001)
      assertThat(savedPositionMs()).isEqualTo(42_000)
    }

  @Test
  fun `saves progress as soon as playback pauses`() =
    playerTest { player ->
      engine.positionMs = 42_000

      player.onAction(PlayerAction.Pause)

      assertThat(savedPositionMs()).isEqualTo(42_000)
      assertThat(player.content.isPlaying).isFalse()
    }

  @Test
  fun `saves progress when paused while buffering after a seek`() =
    playerTest { player ->
      engine.startBuffering()
      engine.positionMs = 78_000

      player.onAction(PlayerAction.Pause)

      assertThat(savedPositionMs()).isEqualTo(78_000)
    }

  @Test
  fun `seeks in 10 second steps without leaving the video`() =
    playerTest { player ->
      engine.positionMs = 5_000

      player.onAction(PlayerAction.SeekBack)
      assertThat(player.content.positionMs).isEqualTo(0)

      player.onAction(PlayerAction.SeekForward)
      player.onAction(PlayerAction.SeekForward)
      assertThat(player.content.positionMs).isEqualTo(20_000)

      engine.positionMs = FakeVideoPlayer.DURATION_MS - 3_000
      player.onAction(PlayerAction.SeekForward)
      assertThat(player.content.positionMs).isEqualTo(FakeVideoPlayer.DURATION_MS)
    }

  @Test
  fun `controls hide after a few seconds of playback but stay up while paused`() =
    playerTest { player ->
      advanceTimeBy(PlayerViewModel.CONTROLS_TIMEOUT_MS + 1)
      assertThat(player.content.controlsVisible).isFalse()

      player.onAction(PlayerAction.Pause)
      advanceTimeBy(PlayerViewModel.CONTROLS_TIMEOUT_MS * 2)
      assertThat(player.content.controlsVisible).isTrue()
    }

  @Test
  fun `falls back to the next rendition at the same position when a stream can't be decoded`() =
    playerTest { player ->
      engine.positionMs = 30_000

      engine.fail(PlaybackFailure.Unsupported)

      assertThat(engine.loads.last()).isEqualTo(assets.streams[1].url to 30_000L)
      assertThat(player.content.panel).isNull()
    }

  @Test
  fun `shows a retryable error when the last rendition can't be played either`() =
    playerTest { player ->
      engine.fail(PlaybackFailure.Unsupported)
      engine.fail(PlaybackFailure.Unsupported)
      assertThat(player.content.panel).isEqualTo(PlayerPanel.Failed(PlaybackFailure.Unsupported))

      player.onAction(PlayerAction.Retry)

      assertThat(engine.loads.last().first).isEqualTo(assets.bestStream.url)
      assertThat(player.content.panel).isNull()
    }

  @Test
  fun `a dropped connection shows an error and retry prepares the same stream again`() =
    playerTest { player ->
      engine.fail(PlaybackFailure.Network)
      assertThat(player.content.panel).isEqualTo(PlayerPanel.Failed(PlaybackFailure.Network))

      player.onAction(PlayerAction.Retry)

      assertThat(engine.retries).isEqualTo(1)
      assertThat(player.content.panel).isNull()
    }

  @Test
  fun `finishing a video clears its progress and counts down to the next one`() {
    saveProgress(120_000)

    playerTest { player ->
      engine.reachEnd()

      assertThat(savedPositionMs()).isNull()
      assertThat(player.content.panel)
        .isEqualTo(PlayerPanel.UpNext(nextVideo, PlayerViewModel.UP_NEXT_COUNTDOWN_SECONDS))
      player.viewModel.events.test {
        assertThat(awaitItem()).isEqualTo(PlayerEvent.PlayNext("b"))
      }
    }
  }

  @Test
  fun `finishing a video with autoplay off shows the end screen`() =
    playerTest { player ->
      settings.setAutoplayNext(false)

      engine.reachEnd()

      assertThat(player.content.panel).isEqualTo(PlayerPanel.Finished)
    }

  @Test
  fun `finishing a video with nothing related to play shows the end screen`() {
    videos.videosByQuery.clear()

    playerTest { player ->
      engine.reachEnd()

      assertThat(player.content.panel).isEqualTo(PlayerPanel.Finished)
    }
  }

  @Test
  fun `cancelling up next shows the end screen, and replay starts over`() =
    playerTest { player ->
      engine.reachEnd()

      player.onAction(PlayerAction.CancelUpNext)
      assertThat(player.content.panel).isEqualTo(PlayerPanel.Finished)

      player.onAction(PlayerAction.Replay)
      assertThat(player.content.panel).isNull()
      assertThat(engine.positionMs).isEqualTo(0)
      assertThat(engine.state.value.playWhenReady).isTrue()
    }

  @Test
  fun `closing the player saves the position and releases the engine`() =
    playerTest { player ->
      engine.positionMs = 90_000

      player.close()
      runCurrent()

      assertThat(savedPositionMs()).isEqualTo(90_000)
      assertThat(engine.released).isTrue()
    }
}
