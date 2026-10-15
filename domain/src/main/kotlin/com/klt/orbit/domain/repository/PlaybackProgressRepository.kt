package com.klt.orbit.domain.repository

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import kotlinx.coroutines.flow.Flow

/** Locally saved playback positions (watch history). */
interface PlaybackProgressRepository {
  fun observeProgress(videoId: String): Flow<PlaybackProgress?>

  /** Every video with saved progress, most recently watched first. */
  fun observeWatchHistory(): Flow<List<ContinueWatchingItem>>

  suspend fun saveProgress(
    video: Video,
    positionMs: Long,
    durationMs: Long,
  ): Outcome<Unit>

  suspend fun clearProgress(videoId: String): Outcome<Unit>

  suspend fun clearAll(): Outcome<Unit>
}
