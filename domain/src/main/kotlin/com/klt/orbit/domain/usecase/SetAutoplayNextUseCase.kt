package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.repository.SettingsRepository
import javax.inject.Inject

class SetAutoplayNextUseCase
  @Inject
  constructor(
    private val settingsRepository: SettingsRepository,
  ) {
    suspend operator fun invoke(enabled: Boolean): Outcome<Unit> = settingsRepository.setAutoplayNext(enabled)
  }
