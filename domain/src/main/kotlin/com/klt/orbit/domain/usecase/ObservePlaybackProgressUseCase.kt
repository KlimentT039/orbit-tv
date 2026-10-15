package com.klt.orbit.domain.usecase

import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePlaybackProgressUseCase
  @Inject
  constructor(
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    operator fun invoke(videoId: String): Flow<PlaybackProgress?> = playbackProgressRepository.observeProgress(videoId)
  }
