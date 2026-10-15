package com.klt.orbit.domain.usecase

import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Partially watched videos (started, not finished), most recent first. */
class ObserveContinueWatchingUseCase
  @Inject
  constructor(
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    operator fun invoke(): Flow<List<ContinueWatchingItem>> =
      playbackProgressRepository
        .observeWatchHistory()
        .map { items -> items.filter { it.progress.isResumable }.take(LIMIT) }
        .distinctUntilChanged()

    companion object {
      const val LIMIT = 20
    }
  }
