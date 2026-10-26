package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Switch
import androidx.tv.material3.SwitchDefaults
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitFocus
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * A full-width, focusable row for settings-style lists. Focus lifts the row with an accent border
 * and glow instead of filling it, so trailing controls such as [OrbitSwitchListItem]'s switch stay
 * readable while focused.
 */
@Composable
fun OrbitListItem(
  headline: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  supporting: String? = null,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  trailingContent: (@Composable () -> Unit)? = null,
) {
  val colors = OrbitTheme.colors
  ListItem(
    selected = false,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    headlineContent = {
      Text(text = headline, style = OrbitTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    },
    supportingContent =
      supporting?.let {
        {
          Text(
            text = it,
            style = OrbitTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      },
    leadingContent =
      icon?.let {
        { Icon(imageVector = it, contentDescription = null, modifier = Modifier.width(ListItemDefaults.IconSize)) }
      },
    trailingContent = trailingContent,
    shape = ListItemDefaults.shape(shape = OrbitShape.Card),
    scale = ListItemDefaults.scale(focusedScale = OrbitFocus.LIST_ITEM_FOCUSED_SCALE),
    colors =
      ListItemDefaults.colors(
        containerColor = colors.onSurface.copy(alpha = 0.06f),
        contentColor = colors.onSurface,
        focusedContainerColor = colors.onSurface.copy(alpha = 0.14f),
        focusedContentColor = colors.onSurface,
        pressedContainerColor = colors.onSurface.copy(alpha = 0.2f),
        pressedContentColor = colors.onSurface,
        disabledContainerColor = colors.onSurface.copy(alpha = 0.03f),
        disabledContentColor = colors.onSurface.copy(alpha = 0.4f),
      ),
    border =
      ListItemDefaults.border(
        focusedBorder =
          Border(
            border = BorderStroke(width = OrbitFocus.borderWidth, color = colors.primary),
            shape = OrbitShape.Card,
          ),
      ),
    glow =
      ListItemDefaults.glow(
        focusedGlow = Glow(elevationColor = colors.primary.copy(alpha = 0.35f), elevation = 12.dp),
      ),
  )
}

/** An [OrbitListItem] that toggles a setting; the whole row is the click target. */
@Composable
fun OrbitSwitchListItem(
  headline: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  supporting: String? = null,
  icon: ImageVector? = null,
) {
  val colors = OrbitTheme.colors
  OrbitListItem(
    headline = headline,
    supporting = supporting,
    icon = icon,
    onClick = { onCheckedChange(!checked) },
    modifier =
      modifier.semantics {
        role = Role.Switch
        toggleableState = ToggleableState(checked)
      },
    trailingContent = {
      Switch(
        checked = checked,
        // The row handles clicks; the switch only shows the state.
        onCheckedChange = null,
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            uncheckedThumbColor = colors.onSurfaceVariant,
            uncheckedTrackColor = colors.onSurface.copy(alpha = 0.12f),
            uncheckedBorderColor = colors.onSurfaceVariant,
          ),
      )
    },
  )
}

@PreviewTv
@Composable
private fun OrbitListItemPreview() {
  OrbitThemePreview {
    Column(verticalArrangement = Arrangement.spacedBy(OrbitSpacing.md), modifier = Modifier.width(480.dp)) {
      OrbitSwitchListItem(
        headline = "Autoplay next video",
        supporting = "Start a related video when one ends",
        icon = OrbitIcons.Play,
        checked = true,
        onCheckedChange = {},
      )
      OrbitListItem(
        headline = "Clear watch history",
        supporting = "Removes 3 videos from Continue watching",
        icon = OrbitIcons.Delete,
        onClick = {},
      )
    }
  }
}
