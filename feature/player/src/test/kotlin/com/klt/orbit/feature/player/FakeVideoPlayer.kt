package com.klt.orbit.feature.player

import androidx.media3.common.Player
import com.klt.orbit.feature.player.engine.PlaybackFailure
import com.klt.orbit.feature.player.engine.PlaybackPhase
import com.klt.orbit.feature.player.engine.PlaybackSnapshot
import com.klt.orbit.feature.player.engine.VideoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Scriptable [VideoPlayer]: tests drive buffering, readiness, failures and the end of a video. */
internal class FakeVideoPlayer : VideoPlayer {
  override val state = MutableStateFlow(PlaybackSnapshot())
  override var positionMs: Long = 0
  override var bufferedPositionMs: Long = 0
  override val media3Player: Player? = null

  val loads = mutableListOf<Pair<String, Long>>()
  var retries = 0
    private set
  var released = false
    private set

  override fun load(
    url: String,
    startPositionMs: Long,
  ) {
    loads += url to startPositionMs
    positionMs = startPositionMs
    state.update { it.copy(phase = PlaybackPhase.Buffering, isPlaying = false, playWhenReady = true, failure = null) }
  }

  override fun play() {
    state.update { it.copy(playWhenReady = true, isPlaying = it.phase == PlaybackPhase.Ready) }
  }

  override fun pause() {
    state.update { it.copy(playWhenReady = false, isPlaying = false) }
  }

  override fun seekTo(positionMs: Long) {
    this.positionMs = positionMs
    state.update { if (it.phase == PlaybackPhase.Ended) it.copy(phase = PlaybackPhase.Ready) else it }
  }

  override fun retry() {
    retries++
    state.update { it.copy(phase = PlaybackPhase.Buffering, playWhenReady = true, failure = null) }
  }

  override fun release() {
    released = true
  }

  /** Finishes buffering; plays if the user hasn't paused. */
  fun becomeReady(durationMs: Long = DURATION_MS) {
    state.update {
      it.copy(phase = PlaybackPhase.Ready, durationMs = durationMs, isPlaying = it.playWhenReady)
    }
  }

  /** Rebuffers, e.g. after a seek: playback stalls but the user still wants it playing. */
  fun startBuffering() {
    state.update { it.copy(phase = PlaybackPhase.Buffering, isPlaying = false) }
  }

  fun fail(failure: PlaybackFailure) {
    state.update { it.copy(phase = PlaybackPhase.Idle, isPlaying = false, failure = failure) }
  }

  fun reachEnd() {
    positionMs = state.value.durationMs
    state.update { it.copy(phase = PlaybackPhase.Ended, isPlaying = false) }
  }

  companion object {
    const val DURATION_MS = 600_000L
  }
}
