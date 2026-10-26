package com.klt.orbit.feature.search

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.testing.TestData
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-pad behavior of Search: the field, suggestions and results. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi")
class SearchScreenFocusTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val queryState = TextFieldState()
  private var searchedQuery by mutableStateOf<String?>(null)
  private val results = MutableStateFlow(PagingData.empty<Video>())
  private var clickedSuggestion: String? = null

  private fun setSearch() {
    composeRule.setContent {
      OrbitTheme {
        SearchScreen(
          uiState = SearchUiState(searchedQuery),
          queryState = queryState,
          results = results.collectAsLazyPagingItems(),
          onSearch = {},
          onSuggestionClick = { suggestion ->
            clickedSuggestion = suggestion
            searchedQuery = suggestion
            results.value = PagingData.from(List(5) { TestData.video(id = "result-${it + 1}", thumbnailUrl = null) })
          },
          onVideoClick = {},
        )
      }
    }
    composeRule.waitForIdle()
  }

  private fun press(keys: List<Key>) {
    keys.forEach { key ->
      composeRule.onRoot().performKeyInput { pressKey(key) }
      composeRule.waitForIdle()
    }
  }

  private val field get() = composeRule.onNodeWithTag(SearchTestTags.FIELD)

  /** Results take focus one frame after their first page is laid out. */
  private fun awaitFocusedResult(videoId: String) {
    composeRule.waitUntil {
      composeRule
        .onAllNodes(
          hasTestTag(SearchTestTags.result(videoId)) and isFocused(),
        ).fetchSemanticsNodes()
        .isNotEmpty()
    }
  }

  @Test
  fun `entering focuses the search field`() {
    setSearch()

    field.assertIsFocused()
  }

  @Test
  fun `ok on the field does not type anything`() {
    setSearch()

    press(listOf(Key.DirectionCenter))

    field.assertIsFocused()
    assertThat(queryState.text.toString()).isEmpty()
  }

  @Test
  fun `a suggestion searches it and moves focus to the first result`() {
    setSearch()

    press(listOf(Key.DirectionDown, Key.DirectionCenter))

    assertThat(clickedSuggestion).isEqualTo("Apollo 11")
    awaitFocusedResult("result-1")
  }

  @Test
  fun `back from the results returns to the field without editing the query`() {
    queryState.setTextAndPlaceCursorAtEnd("mars")
    searchedQuery = "mars"
    results.value = PagingData.from(List(5) { TestData.video(id = "result-${it + 1}", thumbnailUrl = null) })
    setSearch()
    press(listOf(Key.DirectionDown))
    awaitFocusedResult("result-1")

    composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    field.assertIsFocused()
    assertThat(queryState.text.toString()).isEqualTo("mars")
  }
}
