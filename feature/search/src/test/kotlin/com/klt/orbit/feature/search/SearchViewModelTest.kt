package com.klt.orbit.feature.search

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.domain.testing.FakeVideoRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.SearchVideosUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val repository = FakeVideoRepository()
  private val savedStateHandle = SavedStateHandle()

  private fun TestScope.viewModel(): SearchViewModel =
    SearchViewModel(savedStateHandle, SearchVideosUseCase(repository)).also { viewModel ->
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

  /** What the field does as the user types: edits the state, which Compose then publishes. */
  private fun SearchViewModel.type(text: String) {
    queryState.setTextAndPlaceCursorAtEnd(text)
    Snapshot.sendApplyNotifications()
  }

  private val SearchViewModel.searchedQuery get() = uiState.value.searchedQuery

  @Test
  fun `waits for a pause in typing before searching`() =
    runTest {
      val viewModel = viewModel()

      viewModel.type("ma")
      advanceTimeBy(SearchViewModel.DEBOUNCE_MS / 2)
      viewModel.type("mars")
      advanceTimeBy(SearchViewModel.DEBOUNCE_MS - 1)
      assertThat(viewModel.searchedQuery).isNull()

      advanceTimeBy(2)
      assertThat(viewModel.searchedQuery).isEqualTo("mars")
    }

  @Test
  fun `the keyboard search action searches without waiting`() =
    runTest {
      val viewModel = viewModel()

      viewModel.type("  space   station ")
      viewModel.onSearch()
      runCurrent()

      assertThat(viewModel.searchedQuery).isEqualTo("space station")
    }

  @Test
  fun `clearing the field goes straight back to suggestions`() =
    runTest {
      val viewModel = viewModel()
      viewModel.type("mars")
      viewModel.onSearch()
      runCurrent()

      viewModel.type("")
      runCurrent()

      assertThat(viewModel.searchedQuery).isNull()
    }

  @Test
  fun `one letter is not worth searching`() =
    runTest {
      val viewModel = viewModel()

      viewModel.type("m")
      viewModel.onSearch()
      advanceTimeBy(SearchViewModel.DEBOUNCE_MS * 2)

      assertThat(viewModel.searchedQuery).isNull()
    }

  @Test
  fun `a suggestion fills the field and searches it`() =
    runTest {
      val viewModel = viewModel()

      viewModel.onSuggestionClick("Black hole")
      runCurrent()

      assertThat(viewModel.queryState.text.toString()).isEqualTo("Black hole")
      assertThat(viewModel.searchedQuery).isEqualTo("Black hole")
    }

  @Test
  fun `results come from the repository, searched once per query`() =
    runTest {
      val viewModel = viewModel()
      backgroundScope.launch { viewModel.results.collect {} }

      viewModel.type("jupiter")
      viewModel.onSearch()
      // The debounced copy of the same text arrives later and must not search again.
      advanceTimeBy(SearchViewModel.DEBOUNCE_MS * 2)
      viewModel.type("jupiter moons")
      advanceTimeBy(SearchViewModel.DEBOUNCE_MS * 2)

      assertThat(repository.searchCalls).containsExactly("jupiter", "jupiter moons").inOrder()
    }

  @Test
  fun `the query survives process death`() =
    runTest {
      viewModel().type("artemis")
      assertThat(savedStateHandle.get<String>(SearchViewModel.QUERY_KEY)).isEqualTo("artemis")

      val restored = viewModel()

      assertThat(restored.queryState.text.toString()).isEqualTo("artemis")
      assertThat(restored.searchedQuery).isEqualTo("artemis")
    }

  @Test
  fun `clicking a result opens its details`() =
    runTest {
      val viewModel = viewModel()

      viewModel.events.test {
        viewModel.onVideoClick(TestData.video(id = "apollo-11"))
        assertThat(awaitItem()).isEqualTo(SearchEvent.OpenDetails("apollo-11"))
      }
    }
}
