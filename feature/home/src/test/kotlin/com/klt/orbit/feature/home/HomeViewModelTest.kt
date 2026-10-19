package com.klt.orbit.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.VideoCategory
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.GetHomeFeedUseCase
import com.klt.orbit.domain.usecase.ObserveContinueWatchingUseCase
import com.klt.orbit.domain.usecase.ObserveFavoritesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val videos = FakeVideoRepository()
  private val favorites = FakeFavoritesRepository()
  private val progress = FakePlaybackProgressRepository()

  private fun viewModel() =
    HomeViewModel(
      GetHomeFeedUseCase(videos),
      ObserveContinueWatchingUseCase(progress),
      ObserveFavoritesUseCase(favorites),
    )

  private fun programCatalog() {
    VideoCategory.entries.forEach { category ->
      videos.videosByQuery[category.query] = Outcome.Success(TestData.videos(3, prefix = category.name.lowercase()))
    }
  }

  private fun programCatalogFailure(error: DataError = DataError.Network) {
    VideoCategory.entries.forEach { videos.videosByQuery[it.query] = Outcome.Failure(error) }
  }

  @Test
  fun `shows the hero and one row per category`() =
    runTest {
      programCatalog()

      viewModel().uiState.test {
        val content = awaitItem() as HomeUiState.Content
        assertThat(content.featured).hasSize(VideoCategory.entries.size)
        assertThat(content.rows.map { it.kind })
          .containsExactlyElementsIn(VideoCategory.entries.map { HomeRowKind.Category(it) })
          .inOrder()
        assertThat(content.feedError).isNull()
      }
    }

  @Test
  fun `puts continue watching and favorites first when they have content`() =
    runTest {
      programCatalog()
      progress.saveProgress(TestData.video(id = "watching"), positionMs = 60_000, durationMs = 600_000)
      favorites.addFavorite(TestData.video(id = "fav"))

      viewModel().uiState.test {
        val rows = (awaitItem() as HomeUiState.Content).rows
        assertThat(rows[0].kind).isEqualTo(HomeRowKind.ContinueWatching)
        assertThat(rows[0].items.single().progress).isEqualTo(0.1f)
        assertThat(rows[1].kind).isEqualTo(HomeRowKind.Favorites)
        assertThat(
          rows[1]
            .items
            .single()
            .video.id,
        ).isEqualTo("fav")
      }
    }

  @Test
  fun `adds the favorites row live when a favorite is added`() =
    runTest {
      programCatalog()
      viewModel().uiState.test {
        assertThat((awaitItem() as HomeUiState.Content).rows.none { it.kind == HomeRowKind.Favorites }).isTrue()

        favorites.addFavorite(TestData.video(id = "fav"))

        assertThat((awaitItem() as HomeUiState.Content).rows.first().kind).isEqualTo(HomeRowKind.Favorites)
      }
    }

  @Test
  fun `shows a full screen error when the catalog fails and nothing is saved`() =
    runTest {
      programCatalogFailure()

      viewModel().uiState.test {
        assertThat(awaitItem()).isEqualTo(HomeUiState.Error(DataError.Network))
      }
    }

  @Test
  fun `keeps saved rows with an inline error when the catalog fails`() =
    runTest {
      programCatalogFailure()
      favorites.addFavorite(TestData.video(id = "fav"))

      viewModel().uiState.test {
        val content = awaitItem() as HomeUiState.Content
        assertThat(content.rows.map { it.kind }).containsExactly(HomeRowKind.Favorites)
        assertThat(content.featured.map { it.id }).containsExactly("fav")
        assertThat(content.feedError).isEqualTo(DataError.Network)
      }
    }

  @Test
  fun `retry reloads the catalog after a failure`() =
    runTest {
      programCatalogFailure()
      val viewModel = viewModel()

      viewModel.uiState.test {
        assertThat(awaitItem()).isInstanceOf(HomeUiState.Error::class.java)

        programCatalog()
        viewModel.onRetry()

        assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
        assertThat(awaitItem()).isInstanceOf(HomeUiState.Content::class.java)
      }
    }

  @Test
  fun `card clicks open details and hero play starts playback`() =
    runTest {
      programCatalog()
      val viewModel = viewModel()
      val video = TestData.video(id = "a")

      viewModel.events.test {
        viewModel.onVideoClick(video)
        assertThat(awaitItem()).isEqualTo(HomeEvent.OpenDetails("a"))

        viewModel.onPlayClick(video)
        assertThat(awaitItem()).isEqualTo(HomeEvent.Play("a"))
      }
    }
}
