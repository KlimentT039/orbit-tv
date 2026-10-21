package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitFocus
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/** A round icon-only button with the same focus treatment as [OrbitButton], e.g. play/pause. */
@Composable
fun OrbitIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = OrbitTheme.colors
  IconButton(
    onClick = onClick,
    modifier = modifier.size(OrbitIconButtonDefaults.Size),
    shape = IconButtonDefaults.shape(shape = CircleShape),
    scale = IconButtonDefaults.scale(focusedScale = OrbitFocus.CONTROL_FOCUSED_SCALE),
    colors =
      IconButtonDefaults.colors(
        containerColor = colors.onSurface.copy(alpha = 0.12f),
        contentColor = colors.onSurface,
        focusedContainerColor = colors.primary,
        focusedContentColor = colors.onPrimary,
        pressedContainerColor = colors.primary,
        pressedContentColor = colors.onPrimary,
      ),
    border =
      IconButtonDefaults.border(
        focusedBorder = Border(border = BorderStroke(width = 2.dp, color = colors.onSurface), shape = CircleShape),
      ),
    glow =
      IconButtonDefaults.glow(
        focusedGlow = Glow(elevationColor = colors.primary.copy(alpha = 0.5f), elevation = 12.dp),
      ),
  ) {
    Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(28.dp))
  }
}

object OrbitIconButtonDefaults {
  val Size = 56.dp
}

@PreviewTv
@Composable
private fun OrbitIconButtonPreview() {
  OrbitThemePreview {
    Row {
      OrbitIconButton(icon = OrbitIcons.Pause, contentDescription = "Pause", onClick = {})
      Spacer(Modifier.width(OrbitSpacing.lg))
      OrbitIconButton(icon = OrbitIcons.Play, contentDescription = "Play", onClick = {})
    }
  }
}
