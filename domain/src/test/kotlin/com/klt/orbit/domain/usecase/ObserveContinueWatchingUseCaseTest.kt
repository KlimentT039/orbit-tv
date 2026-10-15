package com.klt.orbit.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.TestData
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveContinueWatchingUseCaseTest {
  private val repository = FakePlaybackProgressRepository()
  private val useCase = ObserveContinueWatchingUseCase(repository)

  private fun item(
    id: String,
    positionMs: Long,
    durationMs: Long = 600_000,
  ) = ContinueWatchingItem(
    TestData.video(id = id),
    PlaybackProgress(id, positionMs, durationMs, Instant.EPOCH),
  )

  @Test
  fun `keeps only started and unfinished videos in order`() =
    runTest {
      repository.setHistory(
        listOf(
          item("recent", positionMs = 120_000),
          item("barely-started", positionMs = 1_000),
          item("finished", positionMs = 595_000),
          item("older", positionMs = 300_000),
        ),
      )

      useCase().test {
        assertThat(awaitItem().map { it.video.id }).containsExactly("recent", "older").inOrder()
      }
    }

  @Test
  fun `updates when progress changes`() =
    runTest {
      useCase().test {
        assertThat(awaitItem()).isEmpty()

        repository.saveProgress(TestData.video(id = "a"), positionMs = 60_000, durationMs = 600_000)

        assertThat(awaitItem().map { it.video.id }).containsExactly("a")
      }
    }
}
