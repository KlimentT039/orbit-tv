package com.klt.orbit.feature.favorites

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.domain.testing.FakeFavoritesRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.ObserveFavoritesUseCase
import com.klt.orbit.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val apollo = TestData.video(id = "apollo")
  private val mars = TestData.video(id = "mars")
  private val repository = FakeFavoritesRepository(initial = listOf(apollo, mars))

  private fun TestScope.viewModel(): FavoritesViewModel =
    FavoritesViewModel(ObserveFavoritesUseCase(repository), RemoveFavoriteUseCase(repository)).also { viewModel ->
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

  private val FavoritesViewModel.content get() = uiState.value as FavoritesUiState.Content

  @Test
  fun `lists favorites, most recently added first`() =
    runTest {
      val viewModel = viewModel()

      assertThat(viewModel.content.videos).containsExactly(apollo, mars).inOrder()
      assertThat(viewModel.content.isEditing).isFalse()
    }

  @Test
  fun `shows the empty state when there are no favorites`() =
    runTest {
      repository.removeFavorite(apollo.id)
      repository.removeFavorite(mars.id)

      assertThat(viewModel().uiState.value).isEqualTo(FavoritesUiState.Empty)
    }

  @Test
  fun `clicking a card opens its details`() =
    runTest {
      val viewModel = viewModel()

      viewModel.events.test {
        viewModel.onVideoClick(mars)
        assertThat(awaitItem()).isEqualTo(FavoritesEvent.OpenDetails("mars"))
      }
      assertThat(repository.observeFavorites().first()).hasSize(2)
    }

  @Test
  fun `while editing, clicking a card removes it`() =
    runTest {
      val viewModel = viewModel()

      viewModel.onToggleEditing()
      viewModel.onVideoClick(apollo)

      assertThat(viewModel.content.videos).containsExactly(mars)
      assertThat(viewModel.content.isEditing).isTrue()
    }

  @Test
  fun `back leaves edit mode`() =
    runTest {
      val viewModel = viewModel()
      viewModel.onToggleEditing()

      viewModel.onStopEditing()

      assertThat(viewModel.content.isEditing).isFalse()
    }

  @Test
  fun `removing the last favorite leaves edit mode`() =
    runTest {
      val viewModel = viewModel()
      viewModel.onToggleEditing()

      viewModel.onVideoClick(apollo)
      viewModel.onVideoClick(mars)
      assertThat(viewModel.uiState.value).isEqualTo(FavoritesUiState.Empty)

      // The next favorite must not appear already in edit mode.
      repository.addFavorite(apollo)
      assertThat(viewModel.content.isEditing).isFalse()
    }

  @Test
  fun `a failed removal keeps the card and reports the error`() =
    runTest {
      val viewModel = viewModel()
      viewModel.onToggleEditing()
      repository.failWith = DataError.Storage

      viewModel.events.test {
        viewModel.onVideoClick(apollo)
        assertThat(awaitItem()).isEqualTo(FavoritesEvent.ShowError(DataError.Storage))
      }
      assertThat(viewModel.content.videos).containsExactly(apollo, mars).inOrder()
    }
}
