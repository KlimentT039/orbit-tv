package com.klt.orbit.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/** Spacing tokens on a 4dp grid, plus the TV overscan safe area. */
object OrbitSpacing {
  val xxs = 2.dp
  val xs = 4.dp
  val sm = 8.dp
  val md = 12.dp
  val lg = 16.dp
  val xl = 24.dp
  val xxl = 32.dp
  val xxxl = 48.dp

  /** 5% of the 960dp-wide TV canvas: content inside this inset is never cut off by overscan. */
  val safeAreaHorizontal = 48.dp

  /** 5% of the 540dp-tall TV canvas. */
  val safeAreaVertical = 27.dp

  /** Padding that keeps a full-screen layout inside the overscan safe area. */
  val SafeArea = PaddingValues(horizontal = safeAreaHorizontal, vertical = safeAreaVertical)

  /** Gap between cards inside a row. */
  val cardGap = 20.dp

  /** Vertical gap between content rows; leaves room for the focused card's scale and glow. */
  val rowGap = 28.dp
}
