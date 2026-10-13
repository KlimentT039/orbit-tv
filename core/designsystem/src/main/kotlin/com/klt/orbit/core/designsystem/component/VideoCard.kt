package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitFocus
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import kotlin.math.roundToInt

/**
 * A 16:9 video thumbnail card. On focus it scales up with an accent border and glow (animated by
 * tv-material). Shows an optional watch [progress] (0..1) along the bottom edge.
 *
 * @param onFocus invoked when the card gains focus, e.g. to crossfade a hero backdrop or to
 *   remember the focused item for focus restoration.
 */
@Composable
fun VideoCard(
  title: String,
  imageUrl: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  progress: Float? = null,
  onFocus: () -> Unit = {},
) {
  val colors = OrbitTheme.colors
  Card(
    onClick = onClick,
    modifier =
      modifier
        .width(VideoCardDefaults.Width)
        .aspectRatio(VideoCardDefaults.ASPECT_RATIO)
        .onFocusChanged { if (it.isFocused) onFocus() },
    shape = CardDefaults.shape(shape = OrbitShape.Card),
    colors = CardDefaults.colors(containerColor = colors.surfaceVariant),
    scale = CardDefaults.scale(focusedScale = OrbitFocus.CARD_FOCUSED_SCALE),
    border =
      CardDefaults.border(
        focusedBorder =
          Border(
            border = BorderStroke(width = OrbitFocus.borderWidth, color = colors.primary),
            shape = OrbitShape.Card,
          ),
      ),
    glow =
      CardDefaults.glow(
        focusedGlow =
          Glow(
            elevationColor = colors.primary.copy(alpha = 0.55f),
            elevation = OrbitFocus.glowElevation,
          ),
      ),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Artwork(imageUrl = imageUrl, modifier = Modifier.fillMaxSize())
      Column(
        modifier =
          Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .background(OrbitGradients.CardTitleScrim)
            .padding(
              start = OrbitSpacing.md,
              end = OrbitSpacing.md,
              top = OrbitSpacing.xl,
              bottom = if (progress != null) OrbitSpacing.md else OrbitSpacing.sm,
            ),
      ) {
        Text(
          text = title,
          style = OrbitTheme.typography.titleSmall,
          color = colors.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = OrbitTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      if (progress != null) {
        WatchProgressBar(
          progress = progress,
          modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
        )
      }
    }
  }
}

object VideoCardDefaults {
  /** Fits ~3.5 cards inside the safe area so the next card peeks in and invites scrolling. */
  val Width = 236.dp
  const val ASPECT_RATIO = 16f / 9f
}

@Composable
private fun WatchProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
) {
  val fraction = progress.coerceIn(0f, 1f)
  val percent = (fraction * 100).roundToInt()
  val description = stringResource(R.string.designsystem_watch_progress, percent)
  Box(
    modifier =
      modifier
        .height(4.dp)
        .background(OrbitTheme.colors.onSurface.copy(alpha = 0.25f))
        .semantics { stateDescription = description },
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction)
          .background(OrbitTheme.colors.primary),
    )
  }
}

@PreviewTv
@Composable
private fun VideoCardPreview() {
  OrbitThemePreview {
    Row {
      VideoCard(title = "Apollo 11 Launch", imageUrl = null, subtitle = "Jul 16, 1969", onClick = {})
      Spacer(Modifier.width(OrbitSpacing.cardGap))
      VideoCard(
        title = "A very long title for a NASA video that should be ellipsized after two lines",
        imageUrl = null,
        progress = 0.4f,
        onClick = {},
      )
    }
  }
}
