package com.klt.orbit.core.model

import java.time.Instant

/** How far the user got in a video. */
data class PlaybackProgress(
  val videoId: String,
  val positionMs: Long,
  val durationMs: Long,
  val updatedAt: Instant,
) {
  /** Watched fraction in 0..1; 0 when the duration is unknown. */
  val fraction: Float
    get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

  /** Close enough to the end that resuming makes no sense; such entries leave Continue Watching. */
  val isFinished: Boolean
    get() = durationMs > 0 && (fraction >= FINISHED_FRACTION || durationMs - positionMs <= FINISHED_REMAINING_MS)

  /** Worth offering a Resume for: started, not finished. */
  val isResumable: Boolean
    get() = positionMs >= MIN_RESUME_POSITION_MS && !isFinished

  companion object {
    const val FINISHED_FRACTION = 0.95f
    const val FINISHED_REMAINING_MS = 20_000L
    const val MIN_RESUME_POSITION_MS = 5_000L
  }
}

/** A partially watched video, for the Continue Watching row and the Watch Next channel. */
data class ContinueWatchingItem(
  val video: Video,
  val progress: PlaybackProgress,
)
