package com.klt.orbit.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {
  private val repository = FakeFavoritesRepository()
  private val useCase = ToggleFavoriteUseCase(repository)
  private val video = TestData.video()

  @Test
  fun `adds a video that is not a favorite yet`() =
    runTest {
      repository.observeIsFavorite(video.id).test {
        assertThat(awaitItem()).isFalse()

        assertThat(useCase(video)).isEqualTo(Outcome.Success(true))
        assertThat(awaitItem()).isTrue()
      }
    }

  @Test
  fun `removes a video that is already a favorite`() =
    runTest {
      repository.addFavorite(video)

      assertThat(useCase(video)).isEqualTo(Outcome.Success(false))
      repository.observeFavorites().test {
        assertThat(awaitItem()).isEmpty()
      }
    }

  @Test
  fun `reports a storage failure`() =
    runTest {
      repository.failWith = DataError.Storage

      assertThat(useCase(video)).isEqualTo(Outcome.Failure(DataError.Storage))
    }
}
