package com.klt.orbit.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Typography

/**
 * 10-foot type scale. TV renders at a 960x540dp canvas, so these sizes are tuned to stay legible
 * from ~3 m: body text never goes below 14sp and labels never below 12sp.
 */
internal val OrbitTypography: Typography =
  Typography(
    displayLarge = style(FontWeight.Bold, size = 52, line = 60, tracking = -0.02),
    displayMedium = style(FontWeight.Bold, size = 44, line = 52, tracking = -0.015),
    displaySmall = style(FontWeight.Bold, size = 36, line = 44, tracking = -0.01),
    headlineLarge = style(FontWeight.SemiBold, size = 32, line = 40),
    headlineMedium = style(FontWeight.SemiBold, size = 28, line = 36),
    headlineSmall = style(FontWeight.SemiBold, size = 24, line = 32),
    titleLarge = style(FontWeight.SemiBold, size = 22, line = 28),
    titleMedium = style(FontWeight.Medium, size = 18, line = 24, tracking = 0.005),
    titleSmall = style(FontWeight.Medium, size = 16, line = 22, tracking = 0.005),
    bodyLarge = style(FontWeight.Normal, size = 18, line = 26),
    bodyMedium = style(FontWeight.Normal, size = 16, line = 24),
    bodySmall = style(FontWeight.Normal, size = 14, line = 20),
    labelLarge = style(FontWeight.Medium, size = 16, line = 20, tracking = 0.01),
    labelMedium = style(FontWeight.Medium, size = 14, line = 18, tracking = 0.02),
    labelSmall = style(FontWeight.Medium, size = 12, line = 16, tracking = 0.03),
  )

private fun style(
  weight: FontWeight,
  size: Int,
  line: Int,
  tracking: Double = 0.0,
) = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = weight,
  fontSize = size.sp,
  lineHeight = line.sp,
  letterSpacing = tracking.em,
)
