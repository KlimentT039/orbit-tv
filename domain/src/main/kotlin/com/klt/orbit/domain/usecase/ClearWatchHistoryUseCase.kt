package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

class ClearWatchHistoryUseCase
  @Inject
  constructor(
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    suspend operator fun invoke(): Outcome<Unit> = playbackProgressRepository.clearAll()
  }
