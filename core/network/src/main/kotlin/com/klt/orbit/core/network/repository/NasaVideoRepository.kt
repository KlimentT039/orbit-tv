package com.klt.orbit.core.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.core.network.NasaApi
import com.klt.orbit.core.network.mapper.toVideoAssetsOrNull
import com.klt.orbit.core.network.mapper.toVideos
import com.klt.orbit.core.network.networkCall
import com.klt.orbit.core.network.paging.SearchPagingSource
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

internal class NasaVideoRepository
  @Inject
  constructor(
    private val api: NasaApi,
    private val json: Json,
    private val cache: VideoMemoryCache,
  ) : VideoRepository {
    override suspend fun getVideos(
      query: String,
      limit: Int,
    ): Outcome<List<Video>> =
      networkCall {
        api
          .search(query = query, pageSize = limit)
          .collection
          ?.items
          .orEmpty()
          .toVideos(json)
          .take(limit)
          .also(cache::putAll)
      }

    override suspend fun getVideo(id: String): Outcome<Video> {
      cache.get(id)?.let { return Outcome.Success(it) }
      val result =
        networkCall {
          api
            .search(nasaId = id, pageSize = 1)
            .collection
            ?.items
            .orEmpty()
            .toVideos(json)
            .firstOrNull { it.id == id }
            ?.also { cache.putAll(listOf(it)) }
        }
      return when (result) {
        is Outcome.Success -> result.value?.let { Outcome.Success(it) } ?: Outcome.Failure(DataError.NotFound)
        is Outcome.Failure -> result
      }
    }

    override suspend fun getVideoAssets(id: String): Outcome<VideoAssets> =
      when (val result = networkCall { api.asset(id).toVideoAssetsOrNull(id) }) {
        is Outcome.Success -> result.value?.let { Outcome.Success(it) } ?: Outcome.Failure(DataError.NoPlayableMedia)
        is Outcome.Failure -> result
      }

    override fun searchVideos(query: String): Flow<PagingData<Video>> =
      Pager(
        config =
          PagingConfig(
            pageSize = SEARCH_PAGE_SIZE,
            // Page numbers assume every load has the same size, so the first load must match too.
            initialLoadSize = SEARCH_PAGE_SIZE,
            prefetchDistance = SEARCH_PREFETCH_DISTANCE,
            enablePlaceholders = false,
          ),
        pagingSourceFactory = { SearchPagingSource(api, json, query, SEARCH_PAGE_SIZE, onLoaded = cache::putAll) },
      ).flow

    companion object {
      const val SEARCH_PAGE_SIZE = 30
      const val SEARCH_PREFETCH_DISTANCE = 12
    }
  }
