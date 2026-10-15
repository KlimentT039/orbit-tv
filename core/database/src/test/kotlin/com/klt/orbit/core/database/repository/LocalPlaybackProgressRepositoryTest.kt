package com.klt.orbit.core.database.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.database.MutableClock
import com.klt.orbit.core.database.OrbitDatabase
import com.klt.orbit.core.database.inMemoryDatabase
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalPlaybackProgressRepositoryTest {
  private lateinit var database: OrbitDatabase
  private val clock = MutableClock()
  private lateinit var repository: LocalPlaybackProgressRepository

  @Before
  fun setUp() {
    database = inMemoryDatabase()
    repository = LocalPlaybackProgressRepository(database, clock)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `saves and updates progress for a video`() =
    runTest {
      val video = TestData.video(id = "a")
      repository.observeProgress("a").test {
        assertThat(awaitItem()).isNull()

        repository.saveProgress(video, positionMs = 10_000, durationMs = 100_000)
        assertThat(awaitItem()?.positionMs).isEqualTo(10_000)

        clock.advanceSeconds(5)
        repository.saveProgress(video, positionMs = 15_000, durationMs = 100_000)
        val updated = awaitItem()
        assertThat(updated?.positionMs).isEqualTo(15_000)
        assertThat(updated?.updatedAt).isEqualTo(clock.now)
      }
    }

  @Test
  fun `watch history lists the most recently watched first with video metadata`() =
    runTest {
      repository.saveProgress(TestData.video(id = "first", title = "First"), 1_000, 10_000)
      clock.advanceSeconds(60)
      repository.saveProgress(TestData.video(id = "second", title = "Second"), 1_000, 10_000)

      val history = repository.observeWatchHistory().first()

      assertThat(history.map { it.video.title }).containsExactly("Second", "First").inOrder()
    }

  @Test
  fun `clearProgress removes the entry and its unused snapshot`() =
    runTest {
      repository.saveProgress(TestData.video(id = "a"), 1_000, 10_000)

      assertThat(repository.clearProgress("a")).isEqualTo(Outcome.Success(Unit))

      assertThat(repository.observeProgress("a").first()).isNull()
      assertThat(database.videoDao().get("a")).isNull()
    }

  @Test
  fun `clearAll empties history but keeps favorites`() =
    runTest {
      val favorite = TestData.video(id = "fav")
      LocalFavoritesRepository(database, clock).addFavorite(favorite)
      repository.saveProgress(favorite, 1_000, 10_000)
      repository.saveProgress(TestData.video(id = "other"), 1_000, 10_000)

      repository.clearAll()

      assertThat(repository.observeWatchHistory().first()).isEmpty()
      assertThat(database.videoDao().get("fav")).isNotNull()
      assertThat(database.videoDao().get("other")).isNull()
    }
}
