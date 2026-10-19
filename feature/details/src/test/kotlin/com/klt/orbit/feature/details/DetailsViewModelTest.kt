package com.klt.orbit.feature.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.GetRelatedVideosUseCase
import com.klt.orbit.domain.usecase.GetVideoAssetsUseCase
import com.klt.orbit.domain.usecase.GetVideoUseCase
import com.klt.orbit.domain.usecase.ObserveIsFavoriteUseCase
import com.klt.orbit.domain.usecase.ObservePlaybackProgressUseCase
import com.klt.orbit.domain.usecase.ToggleFavoriteUseCase
import com.klt.orbit.feature.details.navigation.DetailsDestination
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DetailsViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val videos = FakeVideoRepository()
  private val favorites = FakeFavoritesRepository()
  private val progress = FakePlaybackProgressRepository()
  private val video = TestData.video(id = "a", keywords = listOf("Mars"))

  private fun viewModel() =
    DetailsViewModel(
      SavedStateHandle(mapOf(DetailsDestination.VIDEO_ID_ARG to "a")),
      GetVideoUseCase(videos, favorites, progress),
      GetVideoAssetsUseCase(videos),
      GetRelatedVideosUseCase(videos),
      ToggleFavoriteUseCase(favorites),
      ObserveIsFavoriteUseCase(favorites),
      ObservePlaybackProgressUseCase(progress),
    )

  private fun programHappyPath() {
    videos.videosById["a"] = Outcome.Success(video)
    videos.assetsById["a"] = Outcome.Success(TestData.assets("a"))
    videos.videosByQuery["Mars"] = Outcome.Success(listOf(video, TestData.video(id = "b")))
  }

  @Test
  fun `shows the video with its sharpest backdrop and related videos`() =
    runTest {
      programHappyPath()

      viewModel().uiState.test {
        val content = awaitItem() as DetailsUiState.Content
        assertThat(content.video).isEqualTo(video)
        assertThat(content.backdropUrl).endsWith("~orig.jpg")
        assertThat(content.playback).isEqualTo(Playback.Available)
        assertThat((content.related as Related.Loaded).videos.map { it.id }).containsExactly("b")
        assertThat(content.resumePositionMs).isNull()
      }
    }

  @Test
  fun `offers resume when there is meaningful progress`() =
    runTest {
      programHappyPath()
      progress.saveProgress(video, positionMs = 120_000, durationMs = 600_000)
      val viewModel = viewModel()

      viewModel.uiState.test {
        assertThat((awaitItem() as DetailsUiState.Content).resumePositionMs).isEqualTo(120_000)
      }
      viewModel.events.test {
        viewModel.onResumeClick()
        assertThat(awaitItem()).isEqualTo(DetailsEvent.Play("a", startPositionMs = 120_000))

        viewModel.onPlayClick()
        assertThat(awaitItem()).isEqualTo(DetailsEvent.Play("a", startPositionMs = 0))
      }
    }

  @Test
  fun `hides play and explains when the video has no playable file`() =
    runTest {
      programHappyPath()
      videos.assetsById["a"] = Outcome.Failure(DataError.NoPlayableMedia)
      progress.saveProgress(video, positionMs = 120_000, durationMs = 600_000)

      viewModel().uiState.test {
        val content = awaitItem() as DetailsUiState.Content
        assertThat(content.playback).isEqualTo(Playback.Unavailable(DataError.NoPlayableMedia))
        assertThat(content.resumePositionMs).isNull()
      }
    }

  @Test
  fun `keeps play available when the file listing fails for a transient reason`() =
    runTest {
      programHappyPath()
      videos.assetsById["a"] = Outcome.Failure(DataError.Network)

      viewModel().uiState.test {
        val content = awaitItem() as DetailsUiState.Content
        assertThat(content.playback.canPlay).isTrue()
        assertThat(content.backdropUrl).isEqualTo(video.thumbnailUrl)
      }
    }

  @Test
  fun `toggles favorite`() =
    runTest {
      programHappyPath()
      val viewModel = viewModel()

      viewModel.uiState.test {
        assertThat((awaitItem() as DetailsUiState.Content).isFavorite).isFalse()

        viewModel.onToggleFavoriteClick()

        assertThat((awaitItem() as DetailsUiState.Content).isFavorite).isTrue()
      }
    }

  @Test
  fun `reports a failed favorite toggle`() =
    runTest {
      programHappyPath()
      favorites.failWith = DataError.Storage
      val viewModel = viewModel()

      viewModel.uiState.test {
        awaitItem()
        viewModel.events.test {
          viewModel.onToggleFavoriteClick()
          assertThat(awaitItem()).isEqualTo(DetailsEvent.ShowError(DataError.Storage))
        }
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `shows an error and recovers on retry`() =
    runTest {
      videos.videosById["a"] = Outcome.Failure(DataError.Network)
      val viewModel = viewModel()

      viewModel.uiState.test {
        assertThat(awaitItem()).isEqualTo(DetailsUiState.Error(DataError.Network))

        programHappyPath()
        viewModel.onRetry()

        assertThat(awaitItem()).isEqualTo(DetailsUiState.Loading)
        assertThat(awaitItem()).isInstanceOf(DetailsUiState.Content::class.java)
        // Files and related videos refine the content afterwards.
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `hides the related row when nothing related is found`() =
    runTest {
      programHappyPath()
      videos.videosByQuery["Mars"] = Outcome.Success(listOf(video))

      viewModel().uiState.test {
        assertThat((awaitItem() as DetailsUiState.Content).related).isEqualTo(Related.None)
      }
    }

  @Test
  fun `related clicks open that video's details`() =
    runTest {
      programHappyPath()
      val viewModel = viewModel()

      viewModel.events.test {
        viewModel.onRelatedClick(TestData.video(id = "b"))
        assertThat(awaitItem()).isEqualTo(DetailsEvent.OpenDetails("b"))
      }
    }
}
