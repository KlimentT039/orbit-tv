package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.model.PlaybackItem
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Loads a video and its playable streams in parallel and works out where playback starts.
 *
 * Metadata falls back to the saved snapshot offline (see [GetVideoUseCase]); the streams cannot,
 * so without a connection this fails with the stream listing's error.
 */
class PreparePlaybackUseCase
  @Inject
  constructor(
    private val getVideo: GetVideoUseCase,
    private val videoRepository: VideoRepository,
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    /**
     * @param startPositionMs where to start, or null to resume from saved progress. Progress that is
     *   not worth resuming (barely started or finished) starts from the beginning.
     */
    suspend operator fun invoke(
      videoId: String,
      startPositionMs: Long?,
    ): Outcome<PlaybackItem> =
      coroutineScope {
        val videoResult = async { getVideo(videoId) }
        val assetsResult = async { videoRepository.getVideoAssets(videoId) }
        val video =
          when (val result = videoResult.await()) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> return@coroutineScope result
          }
        val assets =
          when (val result = assetsResult.await()) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> return@coroutineScope result
          }
        val start = startPositionMs?.coerceAtLeast(0) ?: resumePosition(videoId)
        Outcome.Success(PlaybackItem(video, assets, start))
      }

    private suspend fun resumePosition(videoId: String): Long =
      playbackProgressRepository
        .observeProgress(videoId)
        .first()
        ?.takeIf { it.isResumable }
        ?.positionMs ?: 0
  }
