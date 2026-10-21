package com.klt.orbit.feature.player

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Maps remote keys to player actions during playback, or null to let the key through (e.g. OK to
 * the focused button, Back to the navigation).
 *
 * Left/Right always seek, whether or not the controls are showing, so the transport row never needs
 * horizontal focus movement. With the controls hidden nothing focusable is on screen, so OK toggles
 * play/pause directly and Up/Down bring the controls back.
 */
internal fun playerKeyAction(
  event: KeyEvent,
  controlsVisible: Boolean,
): PlayerAction? {
  if (event.type != KeyEventType.KeyDown) return null
  return when (event.key) {
    Key.DirectionRight, Key.MediaFastForward -> PlayerAction.SeekForward
    Key.DirectionLeft, Key.MediaRewind -> PlayerAction.SeekBack
    Key.MediaPlayPause -> PlayerAction.TogglePlayPause
    Key.MediaPlay -> PlayerAction.Play
    Key.MediaPause -> PlayerAction.Pause
    Key.DirectionUp, Key.DirectionDown -> PlayerAction.ShowControls
    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> if (controlsVisible) null else PlayerAction.TogglePlayPause
    else -> null
  }
}
