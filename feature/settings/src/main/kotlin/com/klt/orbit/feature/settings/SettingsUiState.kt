package com.klt.orbit.feature.settings

import androidx.compose.runtime.Immutable
import com.klt.orbit.core.common.result.DataError

@Immutable
sealed interface SettingsUiState {
  data object Loading : SettingsUiState

  /** @property isConfirmingClear the "clear watch history?" dialog is showing. */
  data class Content(
    val autoplayNext: Boolean,
    val versionName: String,
    val isConfirmingClear: Boolean = false,
  ) : SettingsUiState
}

sealed interface SettingsEvent {
  data object HistoryCleared : SettingsEvent

  data class ShowError(
    val error: DataError,
  ) : SettingsEvent
}
