package com.klt.orbit.domain.usecase

import com.klt.orbit.core.model.UserSettings
import com.klt.orbit.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase
  @Inject
  constructor(
    private val settingsRepository: SettingsRepository,
  ) {
    operator fun invoke(): Flow<UserSettings> = settingsRepository.settings
  }
