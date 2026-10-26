package com.klt.orbit.feature.favorites

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.testing.TestData
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-pad focus behavior of Favorites, including focus surviving removals in edit mode. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi")
class FavoritesScreenFocusTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private var videos by mutableStateOf(List(4) { TestData.video(id = "fav-${it + 1}", thumbnailUrl = null) })
  private var isEditing by mutableStateOf(false)
  private var openedVideo: Video? = null
  private var browseClicked = false

  /** Behaves like the ViewModel: clicking removes while editing, opens otherwise. */
  private fun setFavorites() {
    composeRule.setContent {
      OrbitTheme {
        FavoritesScreen(
          uiState =
            if (videos.isEmpty()) {
              FavoritesUiState.Empty
            } else {
              FavoritesUiState.Content(videos.toImmutableList(), isEditing)
            },
          onVideoClick = { video -> if (isEditing) videos = videos - video else openedVideo = video },
          onToggleEditing = { isEditing = !isEditing },
          onStopEditing = { isEditing = false },
          onBrowse = { browseClicked = true },
        )
      }
    }
    composeRule.waitForIdle()
  }

  private fun card(id: String) = composeRule.onNodeWithTag(FavoritesTestTags.card(id))

  private fun press(keys: List<Key>) {
    keys.forEach { key ->
      composeRule.onRoot().performKeyInput { pressKey(key) }
      composeRule.waitForIdle()
    }
  }

  @Test
  fun `entering focuses the first card`() {
    setFavorites()

    card("fav-1").assertIsFocused()
  }

  @Test
  fun `ok on a card opens it`() {
    setFavorites()

    press(listOf(Key.DirectionRight, Key.DirectionCenter))

    assertThat(openedVideo?.id).isEqualTo("fav-2")
  }

  @Test
  fun `removing a card moves focus to the card that takes its place`() {
    isEditing = true
    setFavorites()

    press(listOf(Key.DirectionRight, Key.DirectionCenter))

    assertThat(videos.map { it.id }).containsExactly("fav-1", "fav-3", "fav-4").inOrder()
    card("fav-3").assertIsFocused()
  }

  @Test
  fun `removing the last card moves focus to the new last card`() {
    isEditing = true
    setFavorites()

    // Three columns fit on screen, so fav-4 starts the second row.
    press(listOf(Key.DirectionDown, Key.DirectionCenter))

    assertThat(videos.map { it.id }).containsExactly("fav-1", "fav-2", "fav-3").inOrder()
    card("fav-3").assertIsFocused()
  }

  @Test
  fun `back leaves edit mode and keeps focus`() {
    isEditing = true
    setFavorites()
    press(listOf(Key.DirectionRight))

    composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    assertThat(isEditing).isFalse()
    card("fav-2").assertIsFocused()
  }

  @Test
  fun `the empty state focuses its browse button`() {
    videos = emptyList()
    setFavorites()

    composeRule.onNodeWithTag(FavoritesTestTags.BROWSE).assertIsFocused()
    press(listOf(Key.DirectionCenter))
    assertThat(browseClicked).isTrue()
  }
}
