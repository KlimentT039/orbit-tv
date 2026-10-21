package com.klt.orbit.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/** A spinning accent arc shown while video is loading or rebuffering. */
@Composable
fun BufferingIndicator(modifier: Modifier = Modifier) {
  val rotation =
    rememberInfiniteTransition(label = "buffering").animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(tween(durationMillis = 1_000, easing = LinearEasing)),
      label = "rotation",
    )
  val color = OrbitTheme.colors.primary
  val description = stringResource(R.string.designsystem_loading)
  Canvas(
    modifier =
      modifier
        .size(BufferingIndicatorDefaults.Size)
        .semantics { contentDescription = description }
        // Rotating in the layer keeps the animation out of composition and layout.
        .graphicsLayer { rotationZ = rotation.value },
  ) {
    val stroke = BufferingIndicatorDefaults.StrokeWidth.toPx()
    drawArc(
      color = color,
      startAngle = 0f,
      sweepAngle = 270f,
      useCenter = false,
      topLeft = Offset(stroke / 2, stroke / 2),
      size = Size(size.width - stroke, size.height - stroke),
      style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
  }
}

object BufferingIndicatorDefaults {
  val Size = 56.dp
  val StrokeWidth = 5.dp
}

@PreviewTv
@Composable
private fun BufferingIndicatorPreview() {
  OrbitThemePreview {
    BufferingIndicator()
  }
}
