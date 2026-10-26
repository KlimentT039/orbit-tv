package com.klt.orbit.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-pad behavior of the navigation drawer around a stand-in screen with two buttons. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
// A plain Application: the drawer needs no Hilt graph.
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi", application = Application::class)
class OrbitNavigationDrawerTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val navigations = mutableListOf<TopLevelDestination>()

  private fun setDrawer(current: TopLevelDestination? = TopLevelDestination.SEARCH) {
    composeRule.setContent {
      OrbitTheme {
        OrbitNavigationDrawer(current = current, onNavigate = { navigations += it }) {
          val first = remember { FocusRequester() }
          Column {
            OrbitButton(text = "First", onClick = {}, modifier = Modifier.focusRequester(first).testTag(FIRST))
            OrbitButton(text = "Second", onClick = {}, modifier = Modifier.testTag(SECOND))
          }
          LaunchedEffect(first) { first.requestFocus() }
        }
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

  private fun item(destination: TopLevelDestination) =
    composeRule.onNodeWithTag(OrbitNavigationDrawerTestTags.item(destination))

  @Test
  fun `left from the screen enters the drawer on the current screen's item`() {
    setDrawer(current = TopLevelDestination.SEARCH)

    press(listOf(Key.DirectionLeft))

    item(TopLevelDestination.SEARCH).assertIsFocused()
  }

  @Test
  fun `right from the drawer returns to where focus was in the screen`() {
    setDrawer()
    press(listOf(Key.DirectionDown, Key.DirectionLeft))
    item(TopLevelDestination.SEARCH).assertIsFocused()

    press(listOf(Key.DirectionRight))

    composeRule.onNodeWithTag(SECOND).assertIsFocused()
  }

  @Test
  fun `choosing another screen navigates to it`() {
    setDrawer(current = TopLevelDestination.SEARCH)

    press(listOf(Key.DirectionLeft, Key.DirectionDown, Key.DirectionCenter))

    assertThat(navigations).containsExactly(TopLevelDestination.FAVORITES)
  }

  @Test
  fun `choosing the current screen just returns to it`() {
    setDrawer(current = TopLevelDestination.SEARCH)

    press(listOf(Key.DirectionLeft, Key.DirectionCenter))

    assertThat(navigations).isEmpty()
    composeRule.onNodeWithTag(FIRST).assertIsFocused()
  }

  @Test
  fun `back in the drawer returns to the screen`() {
    setDrawer()
    press(listOf(Key.DirectionLeft))

    composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(FIRST).assertIsFocused()
  }

  @Test
  fun `full-screen destinations have no drawer`() {
    setDrawer(current = null)

    press(listOf(Key.DirectionLeft))

    item(TopLevelDestination.HOME).assertDoesNotExist()
    composeRule.onNodeWithTag(FIRST).assertIsFocused()
  }

  private companion object {
    const val FIRST = "content:first"
    const val SECOND = "content:second"
  }
}
