package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.domain.model.PlaybackItem
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.TestData
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PreparePlaybackUseCaseTest {
  private val videos = FakeVideoRepository()
  private val progress = FakePlaybackProgressRepository()
  private val prepare =
    PreparePlaybackUseCase(GetVideoUseCase(videos, FakeFavoritesRepository(), progress), videos, progress)
  private val video = TestData.video(id = "a")

  private fun programVideo() {
    videos.videosById["a"] = Outcome.Success(video)
    videos.assetsById["a"] = Outcome.Success(TestData.assets("a"))
  }

  private fun saveProgress(positionMs: Long) {
    progress.setHistory(
      listOf(ContinueWatchingItem(video, PlaybackProgress("a", positionMs, durationMs = 600_000, Instant.EPOCH))),
    )
  }

  @Test
  fun `starts at the requested position`() =
    runTest {
      programVideo()
      saveProgress(120_000)

      val result = prepare("a", startPositionMs = 0)

      assertThat(result).isEqualTo(Outcome.Success(PlaybackItem(video, TestData.assets("a"), startPositionMs = 0)))
    }

  @Test
  fun `resumes from saved progress when no position is requested`() =
    runTest {
      programVideo()
      saveProgress(120_000)

      val result = prepare("a", startPositionMs = null) as Outcome.Success

      assertThat(result.value.startPositionMs).isEqualTo(120_000)
    }

  @Test
  fun `starts from the beginning when the saved progress is not worth resuming`() =
    runTest {
      programVideo()
      saveProgress(2_000)

      val result = prepare("a", startPositionMs = null) as Outcome.Success

      assertThat(result.value.startPositionMs).isEqualTo(0)
    }

  @Test
  fun `fails when the streams cannot be listed`() =
    runTest {
      videos.videosById["a"] = Outcome.Success(video)
      videos.assetsById["a"] = Outcome.Failure(DataError.NoPlayableMedia)

      assertThat(prepare("a", startPositionMs = 0)).isEqualTo(Outcome.Failure(DataError.NoPlayableMedia))
    }

  @Test
  fun `fails when the video cannot be found`() =
    runTest {
      videos.assetsById["a"] = Outcome.Success(TestData.assets("a"))

      assertThat(prepare("a", startPositionMs = 0)).isEqualTo(Outcome.Failure(DataError.NotFound))
    }
}
