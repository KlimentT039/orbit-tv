package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetVideoUseCaseTest {
  private val videos = FakeVideoRepository()
  private val favorites = FakeFavoritesRepository()
  private val progress = FakePlaybackProgressRepository()
  private val useCase = GetVideoUseCase(videos, favorites, progress)

  @Test
  fun `returns the catalog video when available`() =
    runTest {
      val video = TestData.video(id = "a", title = "Remote")
      videos.videosById["a"] = Outcome.Success(video)
      favorites.addFavorite(video.copy(title = "Saved"))

      assertThat(useCase("a")).isEqualTo(Outcome.Success(video))
    }

  @Test
  fun `falls back to a saved favorite when offline`() =
    runTest {
      val saved = TestData.video(id = "a")
      videos.videosById["a"] = Outcome.Failure(DataError.Network)
      favorites.addFavorite(saved)

      assertThat(useCase("a")).isEqualTo(Outcome.Success(saved))
    }

  @Test
  fun `falls back to watch history when offline`() =
    runTest {
      val saved = TestData.video(id = "a")
      videos.videosById["a"] = Outcome.Failure(DataError.Network)
      progress.saveProgress(saved, positionMs = 10_000, durationMs = 100_000)

      assertThat(useCase("a")).isEqualTo(Outcome.Success(saved))
    }

  @Test
  fun `keeps the remote error when nothing is saved`() =
    runTest {
      videos.videosById["a"] = Outcome.Failure(DataError.Network)

      assertThat(useCase("a")).isEqualTo(Outcome.Failure(DataError.Network))
    }
}
