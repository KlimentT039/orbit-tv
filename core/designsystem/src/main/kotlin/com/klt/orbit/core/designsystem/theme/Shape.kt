package com.klt.orbit.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Shapes

internal val OrbitShapes: Shapes =
  Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = ShapeDefaults.ExtraLarge,
  )

/** Named shapes for components, so features never pick raw corner radii. */
object OrbitShape {
  val Card = RoundedCornerShape(12.dp)
  val Chip = RoundedCornerShape(50)
  val Pill = RoundedCornerShape(50)
}
