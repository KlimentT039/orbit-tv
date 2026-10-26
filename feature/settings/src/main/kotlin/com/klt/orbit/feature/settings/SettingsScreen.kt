package com.klt.orbit.feature.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.component.OrbitConfirmDialog
import com.klt.orbit.core.designsystem.component.OrbitListItem
import com.klt.orbit.core.designsystem.component.OrbitSwitchListItem
import com.klt.orbit.core.designsystem.error.dataErrorMessage
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.designsystem.util.ObserveAsEvents

@Composable
internal fun SettingsRoute(
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val historyCleared = stringResource(R.string.settings_history_cleared)
  ObserveAsEvents(viewModel.events) { event ->
    val message =
      when (event) {
        SettingsEvent.HistoryCleared -> historyCleared
        is SettingsEvent.ShowError -> dataErrorMessage(context, event.error)
      }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }
  SettingsScreen(
    uiState = uiState,
    onAutoplayNextChange = viewModel::onAutoplayNextChange,
    onClearHistoryClick = viewModel::onClearHistoryClick,
    onClearHistoryConfirm = viewModel::onClearHistoryConfirm,
    onClearHistoryDismiss = viewModel::onClearHistoryDismiss,
    modifier = modifier,
  )
}

/** Settings on the left, About on the right; entering the screen focuses the first setting. */
@Composable
internal fun SettingsScreen(
  uiState: SettingsUiState,
  onAutoplayNextChange: (Boolean) -> Unit,
  onClearHistoryClick: () -> Unit,
  onClearHistoryConfirm: () -> Unit,
  onClearHistoryDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val content = uiState as? SettingsUiState.Content
  Row(
    modifier =
      modifier
        .fillMaxSize()
        .background(OrbitGradients.SpaceBackground)
        .padding(OrbitSpacing.SafeArea),
    horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.xxxl),
  ) {
    Column(Modifier.weight(SETTINGS_WEIGHT)) {
      Text(
        text = stringResource(R.string.settings_title),
        style = OrbitTheme.typography.headlineSmall,
        color = OrbitTheme.colors.onSurface,
      )
      if (content != null) {
        SettingsList(
          content = content,
          onAutoplayNextChange = onAutoplayNextChange,
          onClearHistoryClick = onClearHistoryClick,
        )
      }
    }
    AboutPanel(versionName = content?.versionName, modifier = Modifier.weight(1f))
  }
  if (content?.isConfirmingClear == true) {
    OrbitConfirmDialog(
      title = stringResource(R.string.settings_clear_dialog_title),
      message = stringResource(R.string.settings_clear_dialog_message),
      confirmLabel = stringResource(R.string.settings_clear_confirm),
      dismissLabel = stringResource(R.string.settings_clear_cancel),
      confirmIcon = OrbitIcons.Delete,
      onConfirm = onClearHistoryConfirm,
      onDismiss = onClearHistoryDismiss,
    )
  }
}

@Composable
private fun SettingsList(
  content: SettingsUiState.Content,
  onAutoplayNextChange: (Boolean) -> Unit,
  onClearHistoryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val firstItemFocus = remember { FocusRequester() }
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(OrbitSpacing.md)) {
    SectionLabel(stringResource(R.string.settings_section_playback))
    OrbitSwitchListItem(
      headline = stringResource(R.string.settings_autoplay_title),
      supporting =
        stringResource(if (content.autoplayNext) R.string.settings_autoplay_on else R.string.settings_autoplay_off),
      icon = OrbitIcons.Play,
      checked = content.autoplayNext,
      onCheckedChange = onAutoplayNextChange,
      modifier = Modifier.focusRequester(firstItemFocus).testTag(SettingsTestTags.AUTOPLAY),
    )
    SectionLabel(stringResource(R.string.settings_section_history))
    OrbitListItem(
      headline = stringResource(R.string.settings_clear_history_title),
      supporting = stringResource(R.string.settings_clear_history_summary),
      icon = OrbitIcons.Delete,
      onClick = onClearHistoryClick,
      modifier = Modifier.testTag(SettingsTestTags.CLEAR_HISTORY),
    )
  }
  LaunchedEffect(firstItemFocus) { firstItemFocus.tryRequestFocus() }
}

@Composable
private fun SectionLabel(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    style = OrbitTheme.typography.labelLarge,
    color = OrbitTheme.colors.primary,
    modifier = modifier.padding(top = OrbitSpacing.xl, bottom = OrbitSpacing.xs),
  )
}

/** Not focusable: it is information only, and it always fits on screen next to the settings. */
@Composable
private fun AboutPanel(
  versionName: String?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .padding(top = SettingsDefaults.AboutTopOffset)
        .background(OrbitTheme.colors.onSurface.copy(alpha = 0.05f), OrbitShape.Card)
        .border(1.dp, OrbitTheme.colors.border, OrbitShape.Card)
        .padding(OrbitSpacing.xl),
  ) {
    Text(
      text = stringResource(R.string.settings_about_title),
      style = OrbitTheme.typography.labelLarge,
      color = OrbitTheme.colors.primary,
    )
    Spacer(Modifier.height(OrbitSpacing.lg))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = OrbitIcons.Orbit,
        contentDescription = null,
        tint = OrbitTheme.colors.primary,
        modifier = Modifier.size(40.dp),
      )
      Spacer(Modifier.width(OrbitSpacing.md))
      Column {
        Text(
          text = stringResource(R.string.settings_about_app_name),
          style = OrbitTheme.typography.titleLarge,
          color = OrbitTheme.colors.onSurface,
        )
        if (versionName != null) {
          Text(
            text = stringResource(R.string.settings_about_version, versionName),
            style = OrbitTheme.typography.bodyMedium,
            color = OrbitTheme.colors.onSurfaceVariant,
            modifier = Modifier.testTag(SettingsTestTags.VERSION),
          )
        }
      }
    }
    Spacer(Modifier.height(OrbitSpacing.lg))
    Text(
      text = stringResource(R.string.settings_about_attribution),
      style = OrbitTheme.typography.bodyMedium,
      color = OrbitTheme.colors.onSurface,
    )
    Spacer(Modifier.height(OrbitSpacing.sm))
    Text(
      text = stringResource(R.string.settings_about_disclaimer),
      style = OrbitTheme.typography.bodySmall,
      color = OrbitTheme.colors.onSurfaceVariant,
    )
  }
}

private const val SETTINGS_WEIGHT = 1.4f

@PreviewTv
@Composable
private fun SettingsScreenPreview() {
  OrbitTheme {
    SettingsScreen(
      uiState = SettingsUiState.Content(autoplayNext = true, versionName = "1.0.0"),
      onAutoplayNextChange = {},
      onClearHistoryClick = {},
      onClearHistoryConfirm = {},
      onClearHistoryDismiss = {},
    )
  }
}

internal object SettingsDefaults {
  /** Lines the About panel up with the first section label, below the screen title. */
  val AboutTopOffset = 48.dp
}

internal object SettingsTestTags {
  const val AUTOPLAY = "settings:autoplay"
  const val CLEAR_HISTORY = "settings:clear_history"
  const val VERSION = "settings:version"
}
