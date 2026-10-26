package com.klt.orbit.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import com.klt.orbit.R
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * The app's left navigation: a collapsed icon rail that expands over the content (with a scrim)
 * while it has focus, as on the Google TV home screen.
 *
 * Shown only on top-level screens ([current] non-null); Details and the player use the full
 * screen. The content is inset by the rail's width so nothing interactive sits underneath it.
 *
 * Focus:
 * - D-pad Left at the left edge of a screen enters the rail on the current screen's item.
 * - Right, Back or choosing the current screen returns to where focus was in the content.
 * - Choosing another screen navigates; that screen then takes focus, which closes the drawer.
 */
@Composable
fun OrbitNavigationDrawer(
  current: TopLevelDestination?,
  onNavigate: (TopLevelDestination) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val contentFocus = remember { FocusRequester() }
  var drawerHasFocus by remember { mutableStateOf(false) }
  val contentInset by animateDpAsState(
    targetValue = if (current != null) OrbitNavigationDrawerDefaults.ContentInset else 0.dp,
    label = "drawerInset",
  )
  // Explicit, not a D-pad move: the open drawer covers the screen's left edge, so nothing lies to its right.
  val leaveDrawer = { contentFocus.tryRequestFocus() }

  BackHandler(enabled = drawerHasFocus) { leaveDrawer() }

  ModalNavigationDrawer(
    modifier = modifier,
    scrimBrush = OrbitGradients.NavigationDrawerScrim,
    drawerContent = { drawerValue ->
      if (current != null) {
        DrawerItems(
          current = current,
          drawerValue = drawerValue,
          contentFocus = contentFocus,
          onSelect = { destination -> if (destination == current) leaveDrawer() else onNavigate(destination) },
          modifier = Modifier.onFocusChanged { drawerHasFocus = it.hasFocus },
        )
      }
    },
  ) {
    // Coming back from the drawer lands on the element that had focus before.
    Box(
      Modifier
        .fillMaxSize()
        .padding(start = contentInset)
        .focusRequester(contentFocus)
        .focusRestorer()
        .focusGroup(),
    ) {
      content()
    }
  }
}

@Composable
private fun NavigationDrawerScope.DrawerItems(
  current: TopLevelDestination,
  drawerValue: DrawerValue,
  contentFocus: FocusRequester,
  onSelect: (TopLevelDestination) -> Unit,
  modifier: Modifier = Modifier,
) {
  val currentItemFocus = remember { FocusRequester() }
  val isOpen = drawerValue == DrawerValue.Open
  Column(
    modifier =
      modifier
        .fillMaxHeight()
        .background(if (isOpen) SolidColor(OrbitTheme.colors.background) else OrbitGradients.NavigationRail)
        .padding(
          start = OrbitNavigationDrawerDefaults.RailPaddingStart,
          end = OrbitNavigationDrawerDefaults.RailPaddingEnd,
          top = OrbitSpacing.safeAreaVertical,
          bottom = OrbitSpacing.safeAreaVertical,
        ).selectableGroup()
        // Entering the rail lands on the current screen's item, or on the item last left.
        .focusRestorer(currentItemFocus)
        .focusGroup()
        .testTag(OrbitNavigationDrawerTestTags.DRAWER),
    verticalArrangement = Arrangement.spacedBy(OrbitSpacing.sm),
  ) {
    Brand(expanded = isOpen)
    Spacer(Modifier.height(OrbitSpacing.xxl))
    TopLevelDestination.entries.forEach { destination ->
      val selected = destination == current
      NavigationDrawerItem(
        selected = selected,
        onClick = { onSelect(destination) },
        leadingContent = { Icon(imageVector = destination.icon, contentDescription = null) },
        shape = NavigationDrawerItemDefaults.shape(shape = OrbitShape.Pill),
        colors =
          NavigationDrawerItemDefaults.colors(
            containerColor = OrbitTheme.colors.background.copy(alpha = 0f),
            contentColor = OrbitTheme.colors.onSurface,
            inactiveContentColor = OrbitTheme.colors.onSurfaceVariant,
            focusedContainerColor = OrbitTheme.colors.primary,
            focusedContentColor = OrbitTheme.colors.onPrimary,
            selectedContainerColor = OrbitTheme.colors.onSurface.copy(alpha = 0.12f),
            selectedContentColor = OrbitTheme.colors.primary,
            focusedSelectedContainerColor = OrbitTheme.colors.primary,
            focusedSelectedContentColor = OrbitTheme.colors.onPrimary,
          ),
        modifier =
          (if (selected) Modifier.focusRequester(currentItemFocus) else Modifier)
            .focusProperties { right = contentFocus }
            .testTag(OrbitNavigationDrawerTestTags.item(destination)),
      ) {
        Text(text = stringResource(destination.labelRes), style = OrbitTheme.typography.titleSmall, maxLines = 1)
      }
    }
  }
}

/** App mark at the top of the rail; the name appears while the drawer is open. */
@Composable
private fun Brand(
  expanded: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.height(NavigationDrawerItemDefaults.ContainerHeightOneLine),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.width(NavigationDrawerItemDefaults.CollapsedDrawerItemWidth), contentAlignment = Alignment.Center) {
      Icon(
        imageVector = OrbitIcons.Orbit,
        contentDescription = null,
        tint = OrbitTheme.colors.primary,
        modifier = Modifier.size(32.dp),
      )
    }
    if (expanded) {
      Text(
        text = stringResource(R.string.app_name),
        style = OrbitTheme.typography.titleMedium,
        color = OrbitTheme.colors.onSurface,
        maxLines = 1,
      )
    }
  }
}

object OrbitNavigationDrawerDefaults {
  val RailPaddingStart = OrbitSpacing.xl
  val RailPaddingEnd = OrbitSpacing.sm

  /** Width of the collapsed rail: its padding plus one collapsed item. */
  val RailWidth = RailPaddingStart + NavigationDrawerItemDefaults.CollapsedDrawerItemWidth + RailPaddingEnd

  /**
   * How far top-level screens are pushed right. Screens already keep the overscan safe area, so the
   * inset only adds what the rail needs on top of it, leaving a small gap after the rail.
   */
  val ContentInset = RailWidth - OrbitSpacing.safeAreaHorizontal + OrbitSpacing.sm
}

internal object OrbitNavigationDrawerTestTags {
  const val DRAWER = "drawer"

  fun item(destination: TopLevelDestination) = "drawer:${destination.name.lowercase()}"
}
