package com.klt.orbit.domain.usecase

import androidx.paging.PagingData
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Paged search. A blank or too-short query yields no results without hitting the network. */
class SearchVideosUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    operator fun invoke(query: String): Flow<PagingData<Video>> =
      searchableQuery(query)?.let(videoRepository::searchVideos) ?: flowOf(PagingData.empty())

    companion object {
      const val MIN_QUERY_LENGTH = 2
      private val WHITESPACE = Regex("\\s+")

      /**
       * [query] as it will be searched (trimmed, inner whitespace collapsed), or null when it is too
       * short to search. Lets callers tell "nothing typed yet" apart from "no results".
       */
      fun searchableQuery(query: String): String? =
        query.trim().replace(WHITESPACE, " ").takeIf { it.length >= MIN_QUERY_LENGTH }
    }
  }
