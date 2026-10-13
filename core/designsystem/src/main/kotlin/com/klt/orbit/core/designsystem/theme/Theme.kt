package com.klt.orbit.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Shapes
import androidx.tv.material3.Typography

/** Root theme for Orbit TV. Always dark: the app is designed for a dimmed living room. */
@Composable
fun OrbitTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = OrbitColorScheme,
    typography = OrbitTypography,
    shapes = OrbitShapes,
    content = content,
  )
}

/** Convenience accessors mirroring `MaterialTheme`, so call sites read `OrbitTheme.colors.primary`. */
object OrbitTheme {
  val colors: ColorScheme
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

  val typography: Typography
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.typography

  val shapes: Shapes
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.shapes
}
