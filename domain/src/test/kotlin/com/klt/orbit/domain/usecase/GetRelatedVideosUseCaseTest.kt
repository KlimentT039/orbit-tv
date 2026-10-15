package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetRelatedVideosUseCaseTest {
  private val repository = FakeVideoRepository()
  private val useCase = GetRelatedVideosUseCase(repository)

  @Test
  fun `searches by the first keyword and excludes the video itself`() =
    runTest {
      val video = TestData.video(id = "a", keywords = listOf("Mars", "Rover"))
      repository.videosByQuery["Mars"] = Outcome.Success(listOf(video, TestData.video(id = "b")))

      val result = useCase(video)

      assertThat(repository.getVideosCalls.single().first).isEqualTo("Mars")
      assertThat((result as Outcome.Success).value.map { it.id }).containsExactly("b")
    }

  @Test
  fun `caps the number of related videos`() =
    runTest {
      val video = TestData.video(id = "a", keywords = listOf("Mars"))
      repository.videosByQuery["Mars"] = Outcome.Success(TestData.videos(40))

      val result = useCase(video) as Outcome.Success

      assertThat(result.value).hasSize(GetRelatedVideosUseCase.LIMIT)
    }

  @Test
  fun `returns nothing without calling the network when there are no keywords`() =
    runTest {
      val result = useCase(TestData.video(keywords = listOf("  ")))

      assertThat(result).isEqualTo(Outcome.Success(emptyList<Nothing>()))
      assertThat(repository.getVideosCalls).isEmpty()
    }

  @Test
  fun `propagates repository errors`() =
    runTest {
      val video = TestData.video(keywords = listOf("Mars"))
      repository.videosByQuery["Mars"] = Outcome.Failure(DataError.Network)

      assertThat(useCase(video)).isEqualTo(Outcome.Failure(DataError.Network))
    }
}
