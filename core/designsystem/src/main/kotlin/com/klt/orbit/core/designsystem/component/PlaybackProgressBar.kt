package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * The player's timeline: played part in the accent with a thumb at the playhead, buffered part in
 * a lighter tone. Display only; seeking is driven by the D-pad, not by focusing the bar.
 *
 * @param progress played fraction in 0..1.
 * @param bufferedProgress buffered fraction in 0..1.
 */
@Composable
fun PlaybackProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  bufferedProgress: Float = 0f,
) {
  val played = progress.coerceIn(0f, 1f)
  val buffered = bufferedProgress.coerceIn(played, 1f)
  val trackColor = OrbitTheme.colors.onSurface.copy(alpha = 0.24f)
  val bufferedColor = OrbitTheme.colors.onSurface.copy(alpha = 0.5f)
  val playedColor = OrbitTheme.colors.primary
  Canvas(
    modifier =
      modifier
        .fillMaxWidth()
        .height(PlaybackProgressBarDefaults.Height)
        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(played, 0f..1f) },
  ) {
    val stroke = PlaybackProgressBarDefaults.TrackHeight.toPx()
    val y = size.height / 2
    val radius = stroke / 2

    fun segment(
      fraction: Float,
      color: Color,
    ) {
      // Round caps extend past the end points, so inset them to keep the bar within its bounds.
      val end = radius + (size.width - 2 * radius) * fraction
      drawLine(color, Offset(radius, y), Offset(end, y), strokeWidth = stroke, cap = StrokeCap.Round)
    }
    segment(1f, trackColor)
    if (buffered > 0f) segment(buffered, bufferedColor)
    if (played > 0f) segment(played, playedColor)
    drawCircle(
      color = playedColor,
      radius = PlaybackProgressBarDefaults.ThumbRadius.toPx(),
      center = Offset(radius + (size.width - 2 * radius) * played, y),
    )
  }
}

object PlaybackProgressBarDefaults {
  val Height = 16.dp
  val TrackHeight = 6.dp
  val ThumbRadius = 8.dp
}

@PreviewTv
@Composable
private fun PlaybackProgressBarPreview() {
  OrbitThemePreview {
    PlaybackProgressBar(progress = 0.35f, bufferedProgress = 0.6f)
  }
}
