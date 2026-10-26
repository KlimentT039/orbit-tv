package com.klt.orbit.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klt.orbit.core.common.app.AppInfo
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.usecase.ClearWatchHistoryUseCase
import com.klt.orbit.domain.usecase.ObserveSettingsUseCase
import com.klt.orbit.domain.usecase.SetAutoplayNextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
  @Inject
  constructor(
    observeSettings: ObserveSettingsUseCase,
    private val setAutoplayNext: SetAutoplayNextUseCase,
    private val clearWatchHistory: ClearWatchHistoryUseCase,
    appInfo: AppInfo,
  ) : ViewModel() {
    private val isConfirmingClear = MutableStateFlow(false)

    private val eventChannel = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> =
      combine(observeSettings(), isConfirmingClear) { settings, confirming ->
        SettingsUiState.Content(
          autoplayNext = settings.autoplayNext,
          versionName = appInfo.versionName,
          isConfirmingClear = confirming,
        )
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState.Loading)

    fun onAutoplayNextChange(enabled: Boolean) {
      viewModelScope.launch {
        val result = setAutoplayNext(enabled)
        if (result is Outcome.Failure) eventChannel.send(SettingsEvent.ShowError(result.error))
      }
    }

    fun onClearHistoryClick() {
      isConfirmingClear.value = true
    }

    fun onClearHistoryDismiss() {
      isConfirmingClear.value = false
    }

    fun onClearHistoryConfirm() {
      isConfirmingClear.value = false
      viewModelScope.launch {
        val event =
          when (val result = clearWatchHistory()) {
            is Outcome.Success -> SettingsEvent.HistoryCleared
            is Outcome.Failure -> SettingsEvent.ShowError(result.error)
          }
        eventChannel.send(event)
      }
    }

    private companion object {
      const val STOP_TIMEOUT_MS = 5_000L
    }
  }
