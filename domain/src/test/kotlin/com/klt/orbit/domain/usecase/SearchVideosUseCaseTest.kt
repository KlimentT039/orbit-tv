package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.domain.testing.FakeVideoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchVideosUseCaseTest {
  private val repository = FakeVideoRepository()
  private val useCase = SearchVideosUseCase(repository)

  @Test
  fun `normalizes whitespace before searching`() =
    runTest {
      useCase("  space   station ")

      assertThat(repository.searchCalls).containsExactly("space station")
    }

  @Test
  fun `does not search for blank or one-letter queries`() =
    runTest {
      useCase("   ")
      useCase("a")

      assertThat(repository.searchCalls).isEmpty()
    }
}
