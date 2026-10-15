package com.klt.orbit.domain.repository

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  /** Current settings; falls back to defaults if storage cannot be read. */
  val settings: Flow<UserSettings>

  suspend fun setAutoplayNext(enabled: Boolean): Outcome<Unit>
}
