package com.klt.orbit.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi")
class SettingsScreenTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private var state by mutableStateOf(SettingsUiState.Content(autoplayNext = true, versionName = "1.0.0"))
  private var cleared = false

  private fun setSettings() {
    composeRule.setContent {
      OrbitTheme {
        SettingsScreen(
          uiState = state,
          onAutoplayNextChange = { state = state.copy(autoplayNext = it) },
          onClearHistoryClick = { state = state.copy(isConfirmingClear = true) },
          onClearHistoryConfirm = {
            cleared = true
            state = state.copy(isConfirmingClear = false)
          },
          onClearHistoryDismiss = { state = state.copy(isConfirmingClear = false) },
        )
      }
    }
    composeRule.waitForIdle()
  }

  private fun pressOn(
    tag: String,
    key: Key,
  ) {
    composeRule.onNodeWithTag(tag).performKeyInput { pressKey(key) }
    composeRule.waitForIdle()
  }

  @Test
  fun `entering focuses autoplay, and ok toggles it`() {
    setSettings()
    val autoplay = composeRule.onNodeWithTag(SettingsTestTags.AUTOPLAY)
    autoplay.assertIsFocused().assertIsOn()

    pressOn(SettingsTestTags.AUTOPLAY, Key.DirectionCenter)

    autoplay.assertIsOff()
  }

  @Test
  fun `shows the app version`() {
    setSettings()

    composeRule.onNodeWithTag(SettingsTestTags.VERSION).assertTextEquals("Version 1.0.0")
  }

  @Test
  fun `the clear history dialog starts on cancel, so an accidental ok clears nothing`() {
    setSettings()
    pressOn(SettingsTestTags.AUTOPLAY, Key.DirectionDown)
    composeRule.onNodeWithTag(SettingsTestTags.CLEAR_HISTORY).assertIsFocused()

    pressOn(SettingsTestTags.CLEAR_HISTORY, Key.DirectionCenter)
    val cancel = composeRule.onNodeWithText("Cancel")
    cancel.assertIsFocused()
    cancel.performKeyInput { pressKey(Key.DirectionCenter) }
    composeRule.waitForIdle()

    assertThat(cleared).isFalse()
    assertThat(state.isConfirmingClear).isFalse()
  }

  @Test
  fun `confirming in the dialog clears history`() {
    state = state.copy(isConfirmingClear = true)
    setSettings()

    val cancel = composeRule.onNodeWithText("Cancel")
    cancel.performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()
    val confirm = composeRule.onNodeWithText("Clear history")
    confirm.assertIsFocused()
    confirm.performKeyInput { pressKey(Key.DirectionCenter) }
    composeRule.waitForIdle()

    assertThat(cleared).isTrue()
  }
}
