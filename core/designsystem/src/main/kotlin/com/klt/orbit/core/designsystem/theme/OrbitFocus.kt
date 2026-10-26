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

  /** Scale applied to focused full-width list rows; wide rows need less to read as lifted. */
  const val LIST_ITEM_FOCUSED_SCALE = 1.02f

  val borderWidth = 3.dp
  val glowElevation = 16.dp
}
