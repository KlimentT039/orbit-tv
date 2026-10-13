package com.klt.orbit.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Focus treatment shared by every interactive component. Values are deliberately strong: the
 * focused element must be obvious from across the room.
 */
object OrbitFocus {
  /** Scale applied to focused cards. */
  const val CARD_FOCUSED_SCALE = 1.1f

  /** Scale applied to focused buttons and chips. */
  const val CONTROL_FOCUSED_SCALE = 1.05f

  val borderWidth = 3.dp
  val glowElevation = 16.dp
}
