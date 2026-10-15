package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SavePlaybackProgressUseCaseTest {
  private val repository = FakePlaybackProgressRepository()
  private val useCase = SavePlaybackProgressUseCase(repository)
  private val video = TestData.video()

  @Test
  fun `saves progress part way through`() =
    runTest {
      val result = useCase(video, positionMs = 60_000, durationMs = 600_000)

      assertThat(result).isEqualTo(Outcome.Success(SavedProgress.Saved))
      assertThat(repository.observeProgress(video.id).first()?.positionMs).isEqualTo(60_000)
    }

  @Test
  fun `clears progress when the video is finished`() =
    runTest {
      useCase(video, positionMs = 60_000, durationMs = 600_000)

      val result = useCase(video, positionMs = 590_000, durationMs = 600_000)

      assertThat(result).isEqualTo(Outcome.Success(SavedProgress.Finished))
      assertThat(repository.observeProgress(video.id).first()).isNull()
    }

  @Test
  fun `ignores unknown durations and negative positions`() =
    runTest {
      assertThat(useCase(video, positionMs = 10_000, durationMs = 0)).isEqualTo(Outcome.Success(SavedProgress.Ignored))
      assertThat(useCase(video, positionMs = -1, durationMs = 1_000)).isEqualTo(Outcome.Success(SavedProgress.Ignored))
      assertThat(repository.observeWatchHistory().first()).isEmpty()
    }

  @Test
  fun `clamps a position past the end and treats it as finished`() =
    runTest {
      assertThat(useCase(video, positionMs = 700_000, durationMs = 600_000))
        .isEqualTo(Outcome.Success(SavedProgress.Finished))
    }

  @Test
  fun `reports a storage failure`() =
    runTest {
      repository.failWith = DataError.Storage

      assertThat(useCase(video, positionMs = 60_000, durationMs = 600_000))
        .isEqualTo(Outcome.Failure(DataError.Storage))
    }
}
