package com.klt.orbit.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitPalette
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing

private const val SHIMMER_DURATION_MS = 1_300
private const val SHIMMER_TRAVEL_PX = 1_600f
private const val SHIMMER_BAND_PX = 500f

/**
 * A diagonal highlight sweeping across navy placeholders. Create one per screen and share it with
 * every skeleton so all placeholders shimmer in sync.
 */
@Composable
fun rememberShimmerBrush(): Brush {
  val transition = rememberInfiniteTransition(label = "shimmer")
  val offset by transition.animateFloat(
    initialValue = -SHIMMER_BAND_PX,
    targetValue = SHIMMER_TRAVEL_PX,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "shimmerOffset",
  )
  return Brush.linearGradient(
    colors = listOf(OrbitPalette.Navy, OrbitPalette.NavyElevated, OrbitPalette.Navy),
    start = Offset(offset, 0f),
    end = Offset(offset + SHIMMER_BAND_PX, SHIMMER_BAND_PX / 2),
  )
}

/**
 * Skeleton placeholder for screens built from [ContentRow]s: [rowCount] rows of title + card
 * shapes laid out exactly like the real content, so nothing jumps when data arrives.
 */
@Composable
fun LoadingShimmer(
  modifier: Modifier = Modifier,
  rowCount: Int = 2,
  cardsPerRow: Int = 5,
) {
  val brush = rememberShimmerBrush()
  val loading = stringResource(R.string.designsystem_loading)
  Column(
    modifier = modifier.semantics { contentDescription = loading },
    verticalArrangement = Arrangement.spacedBy(OrbitSpacing.rowGap),
  ) {
    repeat(rowCount) {
      Column(modifier = Modifier.padding(start = OrbitSpacing.safeAreaHorizontal)) {
        Box(
          modifier =
            Modifier
              .width(180.dp)
              .height(22.dp)
              .background(brush, OrbitShape.Pill),
        )
        Row(
          modifier = Modifier.padding(vertical = OrbitSpacing.lg),
          horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.cardGap),
        ) {
          repeat(cardsPerRow) {
            Box(
              modifier =
                Modifier
                  .width(VideoCardDefaults.Width)
                  .aspectRatio(VideoCardDefaults.ASPECT_RATIO)
                  .background(brush, OrbitShape.Card),
            )
          }
        }
      }
    }
  }
}

/** Skeleton placeholder for a grid of [VideoCard]s, sized like the real cards. */
@Composable
fun LoadingShimmerGrid(
  modifier: Modifier = Modifier,
  rowCount: Int = 2,
  columnCount: Int = 3,
) {
  val brush = rememberShimmerBrush()
  val loading = stringResource(R.string.designsystem_loading)
  Column(
    modifier = modifier.semantics { contentDescription = loading },
    verticalArrangement = Arrangement.spacedBy(OrbitSpacing.rowGap),
  ) {
    repeat(rowCount) {
      Row(horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.cardGap)) {
        repeat(columnCount) {
          Box(
            modifier =
              Modifier
                .width(VideoCardDefaults.Width)
                .aspectRatio(VideoCardDefaults.ASPECT_RATIO)
                .background(brush, OrbitShape.Card),
          )
        }
      }
    }
  }
}

@PreviewTv
@Composable
private fun LoadingShimmerPreview() {
  OrbitThemePreview {
    LoadingShimmer()
  }
}
