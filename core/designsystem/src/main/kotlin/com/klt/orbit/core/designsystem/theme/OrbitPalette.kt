package com.klt.orbit.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.darkColorScheme

/**
 * Raw palette. Features should read colors through [OrbitTheme.colors]; the palette is public only
 * for places outside a composition (e.g. gradients built once) and previews.
 */
object OrbitPalette {
  val SpaceBlack = Color(0xFF04060C)
  val DeepNavy = Color(0xFF0A1022)
  val Navy = Color(0xFF111A33)
  val NavyElevated = Color(0xFF1A2544)
  val NavyOutline = Color(0xFF2B3961)

  /** The single accent: a bright "ion engine" cyan, readable on every navy surface. */
  val Accent = Color(0xFF5CD6FF)
  val AccentMuted = Color(0xFF1F6E8C)
  val OnAccent = Color(0xFF00202D)

  val TextPrimary = Color(0xFFF2F5FF)
  val TextSecondary = Color(0xFFB4BDD8)
  val TextTertiary = Color(0xFF7D88A8)

  val Error = Color(0xFFFF8A80)
  val OnError = Color(0xFF3B0907)
}

internal val OrbitColorScheme: ColorScheme =
  darkColorScheme(
    primary = OrbitPalette.Accent,
    onPrimary = OrbitPalette.OnAccent,
    primaryContainer = OrbitPalette.AccentMuted,
    onPrimaryContainer = OrbitPalette.TextPrimary,
    // One accent by design: secondary/tertiary roles reuse the accent family.
    secondary = OrbitPalette.Accent,
    onSecondary = OrbitPalette.OnAccent,
    secondaryContainer = OrbitPalette.NavyElevated,
    onSecondaryContainer = OrbitPalette.TextPrimary,
    tertiary = OrbitPalette.Accent,
    onTertiary = OrbitPalette.OnAccent,
    tertiaryContainer = OrbitPalette.NavyElevated,
    onTertiaryContainer = OrbitPalette.TextPrimary,
    background = OrbitPalette.SpaceBlack,
    onBackground = OrbitPalette.TextPrimary,
    surface = OrbitPalette.DeepNavy,
    onSurface = OrbitPalette.TextPrimary,
    surfaceVariant = OrbitPalette.Navy,
    onSurfaceVariant = OrbitPalette.TextSecondary,
    surfaceTint = OrbitPalette.Accent,
    inverseSurface = OrbitPalette.TextPrimary,
    inverseOnSurface = OrbitPalette.SpaceBlack,
    inversePrimary = OrbitPalette.AccentMuted,
    error = OrbitPalette.Error,
    onError = OrbitPalette.OnError,
    errorContainer = OrbitPalette.Error,
    onErrorContainer = OrbitPalette.OnError,
    border = OrbitPalette.NavyOutline,
    borderVariant = OrbitPalette.NavyElevated,
    scrim = OrbitPalette.SpaceBlack,
  )
