package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Records where playback is. A finished video has its progress cleared instead, so it leaves
 * Continue Watching (and the Watch Next channel) and starts from the beginning next time.
 */
class SavePlaybackProgressUseCase
  @Inject
  constructor(
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    suspend operator fun invoke(
      video: Video,
      positionMs: Long,
      durationMs: Long,
    ): Outcome<SavedProgress> {
      if (durationMs <= 0 || positionMs < 0) return Outcome.Success(SavedProgress.Ignored)
      val position = positionMs.coerceAtMost(durationMs)
      val finished =
        PlaybackProgress(video.id, position, durationMs, updatedAt = Instant.EPOCH).isFinished
      return if (finished) {
        when (val result = playbackProgressRepository.clearProgress(video.id)) {
          is Outcome.Success -> Outcome.Success(SavedProgress.Finished)
          is Outcome.Failure -> result
        }
      } else {
        when (val result = playbackProgressRepository.saveProgress(video, position, durationMs)) {
          is Outcome.Success -> Outcome.Success(SavedProgress.Saved)
          is Outcome.Failure -> result
        }
      }
    }
  }

/** What [SavePlaybackProgressUseCase] did, so callers can e.g. update the Watch Next channel. */
enum class SavedProgress {
  Saved,
  Finished,
  Ignored,
}
