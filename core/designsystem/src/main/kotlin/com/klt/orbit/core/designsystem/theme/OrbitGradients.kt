package com.klt.orbit.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Subtle gradients used behind hero imagery and as the app background. */
object OrbitGradients {
  /** Darkens the left side of a backdrop so hero text stays legible. */
  val HeroScrimStart: Brush =
    Brush.horizontalGradient(
      0f to OrbitPalette.SpaceBlack,
      0.35f to OrbitPalette.SpaceBlack.copy(alpha = 0.85f),
      0.7f to OrbitPalette.SpaceBlack.copy(alpha = 0.2f),
      1f to Color.Transparent,
    )

  /** Blends the bottom of a backdrop into the rows below it. */
  val HeroScrimBottom: Brush =
    Brush.verticalGradient(
      0f to Color.Transparent,
      0.6f to OrbitPalette.SpaceBlack.copy(alpha = 0.6f),
      1f to OrbitPalette.SpaceBlack,
    )

  /** Deep-space page background: navy glow in the upper right fading to black. */
  val SpaceBackground: Brush =
    Brush.linearGradient(
      0f to OrbitPalette.Navy,
      0.45f to OrbitPalette.DeepNavy,
      1f to OrbitPalette.SpaceBlack,
    )

  /** Placeholder fill for artwork that has not loaded or does not exist. */
  val ArtworkPlaceholder: Brush =
    Brush.linearGradient(
      0f to OrbitPalette.NavyElevated,
      1f to OrbitPalette.DeepNavy,
    )

  /** Bottom fade on cards so overlaid titles stay readable on bright thumbnails. */
  val CardTitleScrim: Brush =
    Brush.verticalGradient(
      0f to Color.Transparent,
      1f to OrbitPalette.SpaceBlack.copy(alpha = 0.9f),
    )

  /** Behind the collapsed navigation rail: solid at the screen edge, fading into the content. */
  val NavigationRail: Brush =
    Brush.horizontalGradient(
      0f to OrbitPalette.SpaceBlack,
      0.7f to OrbitPalette.SpaceBlack.copy(alpha = 0.85f),
      1f to Color.Transparent,
    )

  /** Dims the content while the navigation drawer is open, keeping the labels readable. */
  val NavigationDrawerScrim: Brush =
    Brush.horizontalGradient(
      0f to OrbitPalette.SpaceBlack.copy(alpha = 0.95f),
      0.6f to OrbitPalette.SpaceBlack.copy(alpha = 0.6f),
      1f to OrbitPalette.SpaceBlack.copy(alpha = 0.3f),
    )
}
