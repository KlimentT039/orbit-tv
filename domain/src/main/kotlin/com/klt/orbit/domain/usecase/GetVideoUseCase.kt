package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.FavoritesRepository
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * One video by id. When the catalog can't be reached, falls back to the snapshot saved with a
 * favorite or watch-history entry, so saved videos still open offline.
 */
class GetVideoUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val playbackProgressRepository: PlaybackProgressRepository,
  ) {
    suspend operator fun invoke(videoId: String): Outcome<Video> {
      val remote = videoRepository.getVideo(videoId)
      if (remote is Outcome.Success) return remote
      val saved =
        favoritesRepository.observeFavorites().first().firstOrNull { it.id == videoId }
          ?: playbackProgressRepository
            .observeWatchHistory()
            .first()
            .firstOrNull { it.video.id == videoId }
            ?.video
      return saved?.let { Outcome.Success(it) } ?: remote
    }
  }
