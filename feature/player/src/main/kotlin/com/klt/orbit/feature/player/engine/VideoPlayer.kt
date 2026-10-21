package com.klt.orbit.feature.player.engine

import androidx.media3.common.Player
import kotlinx.coroutines.flow.StateFlow

/**
 * The playback engine as the ViewModel sees it: commands in, [state] out. Hides ExoPlayer behind a
 * small surface so the player logic (progress saving, stream fallback, up next) is unit-testable
 * with a fake. All calls must happen on the main thread.
 */
internal interface VideoPlayer {
  val state: StateFlow<PlaybackSnapshot>

  /** Current playhead. Read on demand: ExoPlayer does not report position changes as events. */
  val positionMs: Long

  val bufferedPositionMs: Long

  /** The Media3 player to render on screen; null for fakes. */
  val media3Player: Player?

  /** Replaces the current media and starts playing it from [startPositionMs]. */
  fun load(
    url: String,
    startPositionMs: Long,
  )

  fun play()

  fun pause()

  fun seekTo(positionMs: Long)

  /** Prepares the current media again after a failure, keeping the position. */
  fun retry()

  fun release()
}

/** Creates one [VideoPlayer] per player screen. */
internal fun interface VideoPlayerFactory {
  fun create(): VideoPlayer
}

internal data class PlaybackSnapshot(
  val phase: PlaybackPhase = PlaybackPhase.Idle,
  /** Actually advancing: ready, wanted and not interrupted (e.g. by another app taking audio focus). */
  val isPlaying: Boolean = false,
  /** The user's intent: playing or paused. Drives the play/pause button. */
  val playWhenReady: Boolean = false,
  /** 0 while unknown. */
  val durationMs: Long = 0,
  val failure: PlaybackFailure? = null,
)

internal enum class PlaybackPhase {
  Idle,
  Buffering,
  Ready,
  Ended,
}

internal enum class PlaybackFailure {
  /** The connection dropped or timed out; retrying the same stream can work. */
  Network,

  /** This stream can't be fetched or decoded here; another rendition may work. */
  Unsupported,

  Unknown,
}
