package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideoAssetsUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    suspend operator fun invoke(videoId: String): Outcome<VideoAssets> = videoRepository.getVideoAssets(videoId)
  }
