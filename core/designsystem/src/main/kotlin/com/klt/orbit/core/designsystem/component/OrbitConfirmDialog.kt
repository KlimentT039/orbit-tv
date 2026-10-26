package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * Asks the user to confirm a destructive action. Focus starts on the dismiss button, so an
 * accidental OK press on the remote never confirms. Back dismisses.
 */
@Composable
fun OrbitConfirmDialog(
  title: String,
  message: String,
  confirmLabel: String,
  dismissLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  confirmIcon: ImageVector? = null,
) {
  Dialog(onDismissRequest = onDismiss) {
    ConfirmDialogContent(
      title = title,
      message = message,
      confirmLabel = confirmLabel,
      dismissLabel = dismissLabel,
      onConfirm = onConfirm,
      onDismiss = onDismiss,
      confirmIcon = confirmIcon,
      modifier = modifier,
    )
  }
}

@Composable
private fun ConfirmDialogContent(
  title: String,
  message: String,
  confirmLabel: String,
  dismissLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  confirmIcon: ImageVector?,
  modifier: Modifier = Modifier,
) {
  val dismissFocus = remember { FocusRequester() }
  Column(
    modifier =
      modifier
        .widthIn(max = 520.dp)
        .background(OrbitTheme.colors.surface, OrbitShape.Card)
        .border(1.dp, OrbitTheme.colors.border, OrbitShape.Card)
        .padding(OrbitSpacing.xxl),
  ) {
    Text(text = title, style = OrbitTheme.typography.headlineSmall, color = OrbitTheme.colors.onSurface)
    Spacer(Modifier.height(OrbitSpacing.md))
    Text(text = message, style = OrbitTheme.typography.bodyLarge, color = OrbitTheme.colors.onSurfaceVariant)
    Spacer(Modifier.height(OrbitSpacing.xl))
    Row(horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.lg)) {
      OrbitButton(text = dismissLabel, onClick = onDismiss, modifier = Modifier.focusRequester(dismissFocus))
      OrbitButton(text = confirmLabel, icon = confirmIcon, onClick = onConfirm)
    }
  }
  LaunchedEffect(dismissFocus) { dismissFocus.tryRequestFocus() }
}

@PreviewTv
@Composable
private fun OrbitConfirmDialogPreview() {
  OrbitThemePreview {
    ConfirmDialogContent(
      title = "Clear watch history?",
      message = "Continue watching will be emptied and every video will start from the beginning.",
      confirmLabel = "Clear",
      dismissLabel = "Cancel",
      onConfirm = {},
      onDismiss = {},
      confirmIcon = OrbitIcons.Delete,
    )
  }
}
