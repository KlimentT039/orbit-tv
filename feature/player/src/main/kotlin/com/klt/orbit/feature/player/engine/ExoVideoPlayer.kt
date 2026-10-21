package com.klt.orbit.feature.player.engine

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [VideoPlayer] backed by ExoPlayer. Handles audio focus (pauses when another app plays, resumes
 * after a transient interruption) and pauses when audio output disconnects.
 */
internal class ExoVideoPlayer(
  context: Context,
) : VideoPlayer {
  private val exoPlayer: ExoPlayer =
    ExoPlayer
      .Builder(context)
      .setAudioAttributes(
        AudioAttributes
          .Builder()
          .setUsage(C.USAGE_MEDIA)
          .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
          .build(),
        // handleAudioFocus =
        true,
      ).setHandleAudioBecomingNoisy(true)
      .build()

  private val mutableState = MutableStateFlow(PlaybackSnapshot())
  override val state: StateFlow<PlaybackSnapshot> = mutableState.asStateFlow()

  private val listener =
    object : Player.Listener {
      override fun onEvents(
        player: Player,
        events: Player.Events,
      ) {
        mutableState.value = snapshot()
      }
    }

  init {
    exoPlayer.addListener(listener)
  }

  override val positionMs: Long get() = exoPlayer.currentPosition

  override val bufferedPositionMs: Long get() = exoPlayer.bufferedPosition

  override val media3Player: Player get() = exoPlayer

  override fun load(
    url: String,
    startPositionMs: Long,
  ) {
    exoPlayer.setMediaItem(MediaItem.fromUri(url), startPositionMs)
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true
  }

  override fun play() = exoPlayer.play()

  override fun pause() = exoPlayer.pause()

  override fun seekTo(positionMs: Long) = exoPlayer.seekTo(positionMs)

  override fun retry() {
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true
  }

  override fun release() {
    exoPlayer.removeListener(listener)
    exoPlayer.release()
  }

  private fun snapshot() =
    PlaybackSnapshot(
      phase =
        when (exoPlayer.playbackState) {
          Player.STATE_BUFFERING -> PlaybackPhase.Buffering
          Player.STATE_READY -> PlaybackPhase.Ready
          Player.STATE_ENDED -> PlaybackPhase.Ended
          else -> PlaybackPhase.Idle
        },
      isPlaying = exoPlayer.isPlaying,
      playWhenReady = exoPlayer.playWhenReady,
      durationMs = exoPlayer.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0,
      failure = exoPlayer.playerError?.toPlaybackFailure(),
    )
}

internal class ExoVideoPlayerFactory
  @Inject
  constructor(
    @param:ApplicationContext private val context: Context,
  ) : VideoPlayerFactory {
    override fun create(): VideoPlayer = ExoVideoPlayer(context)
  }

private fun PlaybackException.toPlaybackFailure(): PlaybackFailure =
  when (errorCode) {
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
    PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
    PlaybackException.ERROR_CODE_TIMEOUT,
    -> PlaybackFailure.Network

    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
    PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE,
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
    -> PlaybackFailure.Unsupported

    else -> PlaybackFailure.Unknown
  }
