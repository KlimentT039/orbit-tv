package com.klt.orbit.domain.usecase

import androidx.paging.PagingData
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Paged search. A blank query yields no results without hitting the network. */
class SearchVideosUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    operator fun invoke(query: String): Flow<PagingData<Video>> {
      val normalized = query.trim().replace(WHITESPACE, " ")
      return if (normalized.length < MIN_QUERY_LENGTH) {
        flowOf(PagingData.empty())
      } else {
        videoRepository.searchVideos(normalized)
      }
    }

    companion object {
      const val MIN_QUERY_LENGTH = 2
      private val WHITESPACE = Regex("\\s+")
    }
  }
