package com.klt.orbit.feature.player

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.feature.player.engine.PlaybackFailure
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-pad, media-key and focus behavior of the player controls, on a TV-sized screen. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w960dp-h540dp-land-xhdpi")
class PlayerScreenControlsTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val actions = mutableListOf<PlayerAction>()
  private var uiState by mutableStateOf<PlayerUiState>(PlayerUiState.Loading)

  private val playing =
    PlayerUiState.Content(
      title = "Apollo 11: One Giant Leap",
      positionMs = 75_000,
      durationMs = 3_725_000,
      isPlaying = true,
      isBuffering = false,
    )

  private fun setPlayer(state: PlayerUiState) {
    uiState = state
    composeRule.setContent {
      OrbitTheme {
        PlayerScreen(uiState = uiState, onAction = { actions += it })
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

  @Test
  fun `showing the controls focuses play pause`() {
    setPlayer(playing)

    composeRule.onNodeWithTag(PlayerTestTags.PLAY_PAUSE).assertIsFocused()
  }

  @Test
  fun `shows the title with elapsed and total time`() {
    setPlayer(playing)

    composeRule.onNodeWithTag(PlayerTestTags.TITLE).assertTextEquals(playing.title)
    composeRule.onNodeWithTag(PlayerTestTags.ELAPSED).assertTextEquals("1:15")
    composeRule.onNodeWithTag(PlayerTestTags.DURATION).assertTextEquals("1:02:05")
  }

  @Test
  fun `OK on play pause toggles playback`() {
    setPlayer(playing)

    press(listOf(Key.DirectionCenter))

    assertThat(actions).containsExactly(PlayerAction.TogglePlayPause)
  }

  @Test
  fun `left and right seek and keep focus on play pause`() {
    setPlayer(playing)

    press(listOf(Key.DirectionRight, Key.DirectionRight, Key.DirectionLeft))

    assertThat(actions)
      .containsExactly(PlayerAction.SeekForward, PlayerAction.SeekForward, PlayerAction.SeekBack)
      .inOrder()
    composeRule.onNodeWithTag(PlayerTestTags.PLAY_PAUSE).assertIsFocused()
  }

  @Test
  fun `media keys control playback`() {
    setPlayer(playing)

    press(listOf(Key.MediaPlayPause, Key.MediaPause, Key.MediaPlay, Key.MediaFastForward, Key.MediaRewind))

    assertThat(actions)
      .containsExactly(
        PlayerAction.TogglePlayPause,
        PlayerAction.Pause,
        PlayerAction.Play,
        PlayerAction.SeekForward,
        PlayerAction.SeekBack,
      ).inOrder()
  }

  @Test
  fun `with the controls hidden the screen keeps focus, OK toggles playback and down reveals the controls`() {
    setPlayer(playing.copy(controlsVisible = false))

    composeRule.onNodeWithTag(PlayerTestTags.ROOT).assertIsFocused()
    press(listOf(Key.DirectionCenter, Key.DirectionDown))

    assertThat(actions).containsExactly(PlayerAction.TogglePlayPause, PlayerAction.ShowControls).inOrder()
  }

  @Test
  fun `hiding the controls moves focus to the screen so the remote keeps working`() {
    setPlayer(playing)

    uiState = playing.copy(controlsVisible = false)
    composeRule.waitForIdle()

    composeRule.onNodeWithTag(PlayerTestTags.ROOT).assertIsFocused()
    press(listOf(Key.DirectionRight))
    assertThat(actions).containsExactly(PlayerAction.SeekForward)
  }

  @Test
  fun `back while playing hides the controls first`() {
    setPlayer(playing)

    composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    composeRule.waitForIdle()

    assertThat(actions).containsExactly(PlayerAction.HideControls)
  }

  @Test
  fun `a playback error focuses retry and the arrows no longer seek`() {
    setPlayer(playing.copy(panel = PlayerPanel.Failed(PlaybackFailure.Network)))

    val retry = composeRule.onNodeWithText("Try again")
    retry.assertIsFocused()
    press(listOf(Key.DirectionLeft, Key.DirectionRight, Key.DirectionCenter))

    assertThat(actions).containsExactly(PlayerAction.Retry)
  }

  @Test
  fun `up next focuses play now and right moves to cancel instead of seeking`() {
    setPlayer(playing.copy(panel = PlayerPanel.UpNext(TestData.video(id = "b"), secondsLeft = 7)))

    composeRule.onNodeWithTag(PlayerTestTags.PLAY_NOW).assertIsFocused()
    composeRule.onNodeWithText("Starting in 7 seconds").assertExists()
    press(listOf(Key.DirectionRight))
    composeRule.onNodeWithTag(PlayerTestTags.CANCEL_UP_NEXT).assertIsFocused()
    press(listOf(Key.DirectionCenter))

    assertThat(actions).containsExactly(PlayerAction.CancelUpNext)
  }

  @Test
  fun `the end screen focuses watch again`() {
    setPlayer(playing.copy(isPlaying = false, panel = PlayerPanel.Finished))

    composeRule.onNodeWithTag(PlayerTestTags.REPLAY).assertIsFocused()
    press(listOf(Key.DirectionCenter))

    assertThat(actions).containsExactly(PlayerAction.Replay)
  }
}
