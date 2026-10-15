package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.common.result.map
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject

/** Videos related to [Video] by its first keyword, excluding the video itself. */
class GetRelatedVideosUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    suspend operator fun invoke(video: Video): Outcome<List<Video>> {
      val keyword =
        video.keywords
          .firstOrNull()
          ?.trim()
          .orEmpty()
      if (keyword.isEmpty()) return Outcome.Success(emptyList())
      return videoRepository
        .getVideos(keyword, limit = LIMIT + 1)
        .map { videos -> videos.filterNot { it.id == video.id }.take(LIMIT) }
    }

    companion object {
      const val LIMIT = 15
    }
  }
