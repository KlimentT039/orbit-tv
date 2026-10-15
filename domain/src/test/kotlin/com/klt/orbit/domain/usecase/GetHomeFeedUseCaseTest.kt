package com.klt.orbit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.VideoCategory
import com.klt.orbit.domain.model.HomeFeed
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetHomeFeedUseCaseTest {
  private val repository = FakeVideoRepository()
  private val useCase = GetHomeFeedUseCase(repository)

  private fun programAll() {
    VideoCategory.entries.forEach { category ->
      repository.videosByQuery[category.query] = Outcome.Success(TestData.videos(3, prefix = category.name))
    }
  }

  @Test
  fun `loads one row per category in category order`() =
    runTest {
      programAll()

      val feed = (useCase() as Outcome.Success<HomeFeed>).value

      assertThat(feed.rows.map { it.category }).containsExactlyElementsIn(VideoCategory.entries).inOrder()
      assertThat(repository.getVideosCalls.map { it.second }).containsNoneOf(0, -1)
    }

  @Test
  fun `features the first video with artwork from each row`() =
    runTest {
      programAll()
      repository.videosByQuery[VideoCategory.MARS.query] =
        Outcome.Success(
          listOf(
            TestData.video(id = "no-art", thumbnailUrl = null),
            TestData.video(id = "mars-art"),
          ),
        )

      val feed = (useCase() as Outcome.Success<HomeFeed>).value

      assertThat(feed.featured.map { it.id }).contains("mars-art")
      assertThat(feed.featured.map { it.id }).doesNotContain("no-art")
      assertThat(feed.featured).hasSize(VideoCategory.entries.size)
    }

  @Test
  fun `drops failed and empty rows but keeps the rest`() =
    runTest {
      programAll()
      repository.videosByQuery[VideoCategory.APOLLO.query] = Outcome.Failure(DataError.Network)
      repository.videosByQuery[VideoCategory.EARTH.query] = Outcome.Success(emptyList())

      val feed = (useCase() as Outcome.Success<HomeFeed>).value

      assertThat(feed.rows.map { it.category })
        .containsExactly(VideoCategory.MARS, VideoCategory.HUBBLE, VideoCategory.ISS)
        .inOrder()
    }

  @Test
  fun `fails with the underlying error when every row fails`() =
    runTest {
      VideoCategory.entries.forEach { repository.videosByQuery[it.query] = Outcome.Failure(DataError.Network) }

      assertThat(useCase()).isEqualTo(Outcome.Failure(DataError.Network))
    }

  @Test
  fun `fails with not found when every row is empty`() =
    runTest {
      VideoCategory.entries.forEach { repository.videosByQuery[it.query] = Outcome.Success(emptyList()) }

      assertThat(useCase()).isEqualTo(Outcome.Failure(DataError.NotFound))
    }
}
