package com.klt.orbit.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.model.VideoCategory
import com.klt.orbit.domain.testing.TestData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-pad focus behavior of Home, on a TV-sized screen. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi")
class HomeScreenFocusTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val featured =
    List(3) { TestData.video(id = "hero-${it + 1}", title = "Featured ${it + 1}", thumbnailUrl = null) }
      .toImmutableList()

  private fun row(
    category: VideoCategory,
    count: Int = 6,
  ) = HomeRow(
    HomeRowKind.Category(category),
    List(count) { HomeItem(TestData.video(id = "${category.name.lowercase()}-${it + 1}", thumbnailUrl = null)) }
      .toImmutableList(),
  )

  private val content =
    HomeUiState.Content(
      featured = featured,
      rows = persistentListOf(row(VideoCategory.APOLLO), row(VideoCategory.MARS)),
    )

  private fun card(
    rowKey: String,
    videoId: String,
  ) = composeRule.onNodeWithTag(HomeTestTags.card("$rowKey/$videoId"))

  private fun press(keys: List<Key>) {
    keys.forEach { key ->
      composeRule.onRoot().performKeyInput { pressKey(key) }
      composeRule.waitForIdle()
    }
  }

  private fun setHome(state: HomeUiState = content) {
    composeRule.setContent {
      OrbitTheme {
        HomeScreen(uiState = state, onVideoClick = {}, onPlayClick = {}, onRetry = {})
      }
    }
    composeRule.waitForIdle()
  }

  @Test
  fun `first launch focuses the hero play button`() {
    setHome()

    composeRule.onNodeWithTag(HomeTestTags.HERO_PLAY).assertIsFocused()
  }

  @Test
  fun `down from the hero focuses the first card of the first row`() {
    setHome()

    press(listOf(Key.DirectionDown))

    card("category_apollo", "apollo-1").assertIsFocused()
  }

  @Test
  fun `each row remembers its focused card`() {
    setHome()

    press(listOf(Key.DirectionDown, Key.DirectionRight, Key.DirectionRight, Key.DirectionDown, Key.DirectionUp))

    card("category_apollo", "apollo-3").assertIsFocused()
  }

  @Test
  fun `left and right on the hero page through featured videos without trapping focus`() {
    setHome()
    composeRule.onNodeWithText("Featured 1").assertExists()

    press(listOf(Key.DirectionRight))
    composeRule.onNodeWithText("Featured 2").assertExists()

    press(listOf(Key.DirectionLeft, Key.DirectionLeft))
    composeRule.onNodeWithText("Featured 1").assertExists()
    // Left on the first slide is not consumed, so it can reach a drawer; the hero keeps no hidden state.
    composeRule.onNodeWithTag(HomeTestTags.HERO_PLAY).assertIsFocused()
  }

  @Test
  fun `back while browsing the rows returns focus to the hero`() {
    setHome()
    press(listOf(Key.DirectionDown, Key.DirectionDown))
    card("category_mars", "mars-1").assertIsFocused()

    composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(HomeTestTags.HERO_PLAY).assertIsFocused()
  }

  @Test
  fun `returning to home restores focus to the last focused card`() {
    val restorationTester = StateRestorationTester(composeRule)
    restorationTester.setContent {
      OrbitTheme { HomeScreen(uiState = content, onVideoClick = {}, onPlayClick = {}, onRetry = {}) }
    }
    composeRule.waitForIdle()
    press(listOf(Key.DirectionDown, Key.DirectionDown, Key.DirectionRight))
    card("category_mars", "mars-2").assertIsFocused()

    // Same as navigating to Details and back: the composition is recreated from saved state.
    restorationTester.emulateSavedInstanceStateRestore()
    composeRule.waitForIdle()

    card("category_mars", "mars-2").assertIsFocused()
  }

  @Test
  fun `restored card stays on screen when a row is inserted above it after returning`() {
    var state: HomeUiState by mutableStateOf(content)
    val restorationTester = StateRestorationTester(composeRule)
    restorationTester.setContent {
      OrbitTheme { HomeScreen(uiState = state, onVideoClick = {}, onPlayClick = {}, onRetry = {}) }
    }
    composeRule.waitForIdle()
    // A card in the first row, so the list is still anchored on the hero (as on a real TV).
    press(listOf(Key.DirectionDown, Key.DirectionRight))

    // Returning from Details first shows the cached state; the fresh state, with a Favorites row
    // inserted above the focused one, arrives a moment later.
    restorationTester.emulateSavedInstanceStateRestore()
    composeRule.waitForIdle()
    val favoritesRow =
      HomeRow(HomeRowKind.Favorites, persistentListOf(HomeItem(TestData.video(id = "fav", thumbnailUrl = null))))
    state = content.copy(rows = (listOf(favoritesRow) + content.rows).toImmutableList())
    composeRule.waitForIdle()

    val focused = card("category_apollo", "apollo-2")
    focused.assertIsFocused()
    // boundsInRoot is clipped to the visible area, so use the unclipped position and full height.
    val node = focused.fetchSemanticsNode()
    val top = node.positionInRoot.y
    val bottom = top + node.size.height
    val screenHeight =
      composeRule
        .onRoot()
        .fetchSemanticsNode()
        .size.height
    assertThat(top).isAtLeast(0f)
    assertThat(bottom).isAtMost(screenHeight.toFloat())
  }
}
