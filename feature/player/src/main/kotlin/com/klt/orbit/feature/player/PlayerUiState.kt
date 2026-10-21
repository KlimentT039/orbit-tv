package com.klt.orbit.feature.player

import androidx.compose.runtime.Immutable
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.model.Video
import com.klt.orbit.feature.player.engine.PlaybackFailure

@Immutable
internal sealed interface PlayerUiState {
  /** Fetching the video and its streams. */
  data object Loading : PlayerUiState

  /** The video could not be opened at all. */
  data class Error(
    val error: DataError,
  ) : PlayerUiState

  /**
   * @property isPlaying the user's intent (play or pause), not whether frames are advancing.
   * @property panel replaces the transport controls when playback stopped or finished.
   */
  data class Content(
    val title: String,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val bufferedPositionMs: Long = 0,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = true,
    val controlsVisible: Boolean = true,
    val panel: PlayerPanel? = null,
  ) : PlayerUiState {
    val progress: Float get() = fractionOf(positionMs)

    val bufferedProgress: Float get() = fractionOf(bufferedPositionMs)

    private fun fractionOf(ms: Long): Float = if (durationMs > 0) (ms.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
  }
}

@Immutable
internal sealed interface PlayerPanel {
  data class Failed(
    val failure: PlaybackFailure,
  ) : PlayerPanel

  data object Finished : PlayerPanel

  /** Autoplay is on: [video] starts when the countdown reaches zero. */
  data class UpNext(
    val video: Video,
    val secondsLeft: Int,
  ) : PlayerPanel
}

internal sealed interface PlayerAction {
  data object TogglePlayPause : PlayerAction

  data object Play : PlayerAction

  data object Pause : PlayerAction

  data object SeekForward : PlayerAction

  data object SeekBack : PlayerAction

  data object ShowControls : PlayerAction

  data object HideControls : PlayerAction

  data object Retry : PlayerAction

  data object Replay : PlayerAction

  data object PlayNextNow : PlayerAction

  data object CancelUpNext : PlayerAction
}

internal sealed interface PlayerEvent {
  data class PlayNext(
    val videoId: String,
  ) : PlayerEvent
}
