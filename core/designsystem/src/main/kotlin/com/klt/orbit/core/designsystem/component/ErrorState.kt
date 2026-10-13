package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * Centered error message with a Retry action.
 *
 * @param requestFocusOnShow moves focus to Retry when shown. On TV, an error that replaces focused
 *   content would otherwise leave focus nowhere and the remote would appear dead.
 */
@Composable
fun ErrorState(
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  title: String = stringResource(R.string.designsystem_error_title),
  icon: ImageVector = OrbitIcons.Error,
  requestFocusOnShow: Boolean = true,
) {
  val retryFocusRequester = remember { FocusRequester() }
  Column(
    modifier = modifier.padding(OrbitSpacing.SafeArea),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = OrbitTheme.colors.error,
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
      maxLines = 4,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.widthIn(max = 560.dp),
    )
    Spacer(Modifier.height(OrbitSpacing.xl))
    OrbitButton(
      text = stringResource(R.string.designsystem_retry),
      icon = OrbitIcons.Replay,
      onClick = onRetry,
      modifier = Modifier.focusRequester(retryFocusRequester),
    )
  }
  if (requestFocusOnShow) {
    LaunchedEffect(retryFocusRequester) { retryFocusRequester.requestFocus() }
  }
}

@PreviewTv
@Composable
private fun ErrorStatePreview() {
  OrbitThemePreview {
    ErrorState(
      message = "Check your internet connection and try again.",
      icon = OrbitIcons.CloudOff,
      onRetry = {},
      requestFocusOnShow = false,
    )
  }
}
