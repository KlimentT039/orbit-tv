package com.klt.orbit.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

private const val BACKDROP_CROSSFADE_MS = 600
private const val HERO_TEXT_WIDTH_FRACTION = 0.5f

/**
 * Full-bleed backdrop that crossfades whenever [imageUrl] changes, with scrims on the left and
 * bottom so foreground text and rows stay readable. Use it behind a whole screen (e.g. Home, where
 * it follows the focused card) or inside [HeroBanner].
 */
@Composable
fun HeroBackdrop(
  imageUrl: String?,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.background(OrbitGradients.SpaceBackground)) {
    Crossfade(
      targetState = imageUrl,
      animationSpec = tween(durationMillis = BACKDROP_CROSSFADE_MS),
      label = "heroBackdrop",
    ) { url ->
      Artwork(
        imageUrl = url,
        placeholder = OrbitGradients.SpaceBackground,
        placeholderIconSize = 0.dp,
        modifier = Modifier.fillMaxSize(),
      )
    }
    Box(Modifier.fillMaxSize().background(OrbitGradients.HeroScrimStart))
    Box(Modifier.fillMaxSize().background(OrbitGradients.HeroScrimBottom))
  }
}

/**
 * A featured video: backdrop, overline, title, clamped description and a primary action.
 *
 * @param actionModifier applied to the action button, e.g. a `focusRequester` so the hero receives
 *   initial focus on first launch.
 * @param overline short context above the title, e.g. the date or NASA center.
 * @param showBackdrop false when a screen-level [HeroBackdrop] is already drawn behind the banner.
 */
@Composable
fun HeroBanner(
  title: String,
  description: String?,
  imageUrl: String?,
  actionLabel: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier,
  actionModifier: Modifier = Modifier,
  overline: String? = null,
  showBackdrop: Boolean = true,
) {
  Box(modifier = modifier) {
    if (showBackdrop) {
      HeroBackdrop(imageUrl = imageUrl, modifier = Modifier.fillMaxSize())
    }
    Column(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth(HERO_TEXT_WIDTH_FRACTION)
          .padding(
            start = OrbitSpacing.safeAreaHorizontal,
            top = OrbitSpacing.safeAreaVertical,
            bottom = OrbitSpacing.xl,
          ),
    ) {
      if (!overline.isNullOrBlank()) {
        Text(
          text = overline.uppercase(),
          style = OrbitTheme.typography.labelLarge,
          color = OrbitTheme.colors.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(OrbitSpacing.sm))
      }
      Text(
        text = title,
        style = OrbitTheme.typography.displaySmall,
        color = OrbitTheme.colors.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (!description.isNullOrBlank()) {
        Spacer(Modifier.height(OrbitSpacing.md))
        Text(
          text = description,
          style = OrbitTheme.typography.bodyLarge,
          color = OrbitTheme.colors.onSurfaceVariant,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.height(OrbitSpacing.xl))
      OrbitButton(
        text = actionLabel,
        icon = OrbitIcons.Play,
        onClick = onAction,
        modifier = actionModifier,
      )
    }
  }
}

@PreviewTv
@Composable
private fun HeroBannerPreview() {
  OrbitThemePreview {
    HeroBanner(
      title = "Apollo 11: One Giant Leap",
      description =
        "Footage from the first crewed landing on the Moon, from launch at Kennedy Space Center " +
          "to the first steps on the lunar surface and the return home.",
      imageUrl = null,
      overline = "Jul 20, 1969 · JSC",
      actionLabel = "Play",
      onAction = {},
      modifier = Modifier.fillMaxWidth().height(360.dp),
    )
  }
}
