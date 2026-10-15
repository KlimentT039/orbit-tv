package com.klt.orbit.domain.testing

import androidx.paging.PagingData
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory [VideoRepository]. Program results per query/id; anything unprogrammed fails with
 * [DataError.NotFound]. Records calls so tests can assert on them.
 */
class FakeVideoRepository : VideoRepository {
  val videosByQuery = mutableMapOf<String, Outcome<List<Video>>>()
  val videosById = mutableMapOf<String, Outcome<Video>>()
  val assetsById = mutableMapOf<String, Outcome<VideoAssets>>()
  var searchResults: Flow<PagingData<Video>> = flowOf(PagingData.empty())

  val getVideosCalls = mutableListOf<Pair<String, Int>>()
  val searchCalls = mutableListOf<String>()

  override suspend fun getVideos(
    query: String,
    limit: Int,
  ): Outcome<List<Video>> {
    getVideosCalls += query to limit
    return videosByQuery[query] ?: Outcome.Failure(DataError.NotFound)
  }

  override suspend fun getVideo(id: String): Outcome<Video> = videosById[id] ?: Outcome.Failure(DataError.NotFound)

  override suspend fun getVideoAssets(id: String): Outcome<VideoAssets> =
    assetsById[id] ?: Outcome.Failure(DataError.NotFound)

  override fun searchVideos(query: String): Flow<PagingData<Video>> {
    searchCalls += query
    return searchResults
  }
}
