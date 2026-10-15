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
class LocalFavoritesRepositoryTest {
  private lateinit var database: OrbitDatabase
  private val clock = MutableClock()
  private lateinit var repository: LocalFavoritesRepository

  @Before
  fun setUp() {
    database = inMemoryDatabase()
    repository = LocalFavoritesRepository(database, clock)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `stores the full video snapshot`() =
    runTest {
      val video = TestData.video(id = "a", keywords = listOf("Mars", "Rover"))

      assertThat(repository.addFavorite(video)).isEqualTo(Outcome.Success(Unit))

      assertThat(repository.observeFavorites().first()).containsExactly(video)
    }

  @Test
  fun `lists the most recently added first`() =
    runTest {
      repository.addFavorite(TestData.video(id = "old"))
      clock.advanceSeconds(10)
      repository.addFavorite(TestData.video(id = "new"))

      assertThat(repository.observeFavorites().first().map { it.id }).containsExactly("new", "old").inOrder()
    }

  @Test
  fun `observeIsFavorite follows adds and removes`() =
    runTest {
      val video = TestData.video(id = "a")
      repository.observeIsFavorite("a").test {
        assertThat(awaitItem()).isFalse()

        repository.addFavorite(video)
        assertThat(awaitItem()).isTrue()

        repository.removeFavorite("a")
        assertThat(awaitItem()).isFalse()
      }
    }

  @Test
  fun `removing a favorite prunes its unused video snapshot`() =
    runTest {
      repository.addFavorite(TestData.video(id = "a"))

      repository.removeFavorite("a")

      assertThat(database.videoDao().get("a")).isNull()
    }

  @Test
  fun `removing a favorite keeps a snapshot still used by watch progress`() =
    runTest {
      val video = TestData.video(id = "a")
      repository.addFavorite(video)
      LocalPlaybackProgressRepository(database, clock).saveProgress(video, positionMs = 1_000, durationMs = 10_000)

      repository.removeFavorite("a")

      assertThat(database.videoDao().get("a")).isNotNull()
    }

  @Test
  fun `adding the same video twice keeps one entry with updated metadata`() =
    runTest {
      repository.addFavorite(TestData.video(id = "a", title = "Old"))
      repository.addFavorite(TestData.video(id = "a", title = "New"))

      assertThat(repository.observeFavorites().first().map { it.title }).containsExactly("New")
    }

  @Test
  fun `reports storage errors instead of throwing`() =
    runTest {
      database.close()

      assertThat(repository.addFavorite(TestData.video())).isInstanceOf(Outcome.Failure::class.java)
    }
}
