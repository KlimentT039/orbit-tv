package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * Centered message for a screen with nothing to show yet (no favorites, no search results).
 *
 * @param action optional call to action below the message. On TV, give an empty screen something
 *   focusable whenever possible, so focus has a sensible place to land.
 */
@Composable
fun EmptyState(
  title: String,
  message: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = OrbitIcons.Orbit,
  action: (@Composable () -> Unit)? = null,
) {
  Column(
    modifier = modifier.padding(OrbitSpacing.SafeArea),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = OrbitTheme.colors.primary,
      modifier = Modifier.size(56.dp),
    )
    Spacer(Modifier.height(OrbitSpacing.lg))
    Text(
      text = title,
      style = OrbitTheme.typography.headlineSmall,
      color = OrbitTheme.colors.onSurface,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(OrbitSpacing.sm))
    Text(
      text = message,
      style = OrbitTheme.typography.bodyLarge,
      color = OrbitTheme.colors.onSurfaceVariant,
      textAlign = TextAlign.Center,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.widthIn(max = 560.dp),
    )
    if (action != null) {
      Spacer(Modifier.height(OrbitSpacing.xl))
      action()
    }
  }
}

@PreviewTv
@Composable
private fun EmptyStatePreview() {
  OrbitThemePreview {
    EmptyState(
      title = "No favorites yet",
      message = "Add videos to your favorites from their details page.",
      icon = OrbitIcons.FavoriteBorder,
      action = { OrbitButton(text = "Browse videos", icon = OrbitIcons.Home, onClick = {}) },
    )
  }
}
