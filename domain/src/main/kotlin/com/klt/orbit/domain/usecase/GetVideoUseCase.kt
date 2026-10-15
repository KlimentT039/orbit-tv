package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideoUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    suspend operator fun invoke(videoId: String): Outcome<Video> = videoRepository.getVideo(videoId)
  }
