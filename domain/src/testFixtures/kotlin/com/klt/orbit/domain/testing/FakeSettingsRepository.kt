package com.klt.orbit.domain.testing

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.UserSettings
import com.klt.orbit.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(
  initial: UserSettings = UserSettings(),
) : SettingsRepository {
  private val state = MutableStateFlow(initial)

  override val settings: Flow<UserSettings> = state

  override suspend fun setAutoplayNext(enabled: Boolean): Outcome<Unit> {
    state.update { it.copy(autoplayNext = enabled) }
    return Outcome.Success(Unit)
  }
}
