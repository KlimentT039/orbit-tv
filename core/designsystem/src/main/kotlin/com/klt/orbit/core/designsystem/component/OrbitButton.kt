package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitFocus
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * The app's standard TV button. Unfocused it is a quiet translucent pill; focused it fills with the
 * accent, scales up and glows so it reads clearly from across the room.
 */
@Composable
fun OrbitButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
) {
  val colors = OrbitTheme.colors
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButtonDefaults.shape(shape = OrbitShape.Pill),
    scale = ButtonDefaults.scale(focusedScale = OrbitFocus.CONTROL_FOCUSED_SCALE),
    colors =
      ButtonDefaults.colors(
        containerColor = colors.onSurface.copy(alpha = 0.12f),
        contentColor = colors.onSurface,
        focusedContainerColor = colors.primary,
        focusedContentColor = colors.onPrimary,
        pressedContainerColor = colors.primary,
        pressedContentColor = colors.onPrimary,
      ),
    border =
      ButtonDefaults.border(
        focusedBorder =
          Border(
            border = BorderStroke(width = 2.dp, color = colors.onSurface),
            shape = OrbitShape.Pill,
          ),
      ),
    glow =
      ButtonDefaults.glow(
        focusedGlow = Glow(elevationColor = colors.primary.copy(alpha = 0.5f), elevation = 12.dp),
      ),
    contentPadding = PaddingValues(horizontal = OrbitSpacing.xl, vertical = OrbitSpacing.sm),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(OrbitSpacing.sm))
    }
    Text(text = text, style = OrbitTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
}

@PreviewTv
@Composable
private fun OrbitButtonPreview() {
  OrbitThemePreview {
    Row {
      OrbitButton(text = "Play", icon = OrbitIcons.Play, onClick = {})
      Spacer(Modifier.width(OrbitSpacing.lg))
      OrbitButton(text = "Add to favorites", icon = OrbitIcons.FavoriteBorder, onClick = {})
    }
  }
}
