package com.klt.orbit.core.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.klt.orbit.core.common.result.DataErrorException
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.network.HTTP_BAD_REQUEST
import com.klt.orbit.core.network.NasaApi
import com.klt.orbit.core.network.mapper.toVideos
import com.klt.orbit.core.network.toNetworkDataError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Page-numbered search. Lazy grids require unique keys, so a video already emitted by an earlier
 * page is filtered out (NASA's relevance ordering can repeat items across pages). The API caps deep
 * pagination and answers HTTP 400 past the cap; that is treated as the end of results.
 */
internal class SearchPagingSource(
  private val api: NasaApi,
  private val json: Json,
  private val query: String,
  private val pageSize: Int,
) : PagingSource<Int, Video>() {
  private val seenIds = mutableSetOf<String>()

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
    val page = params.key ?: FIRST_PAGE
    return try {
      val collection = api.search(query = query, page = page, pageSize = pageSize).collection
      val rawItems = collection?.items.orEmpty()
      val videos = rawItems.toVideos(json).filter { seenIds.add(it.id) }
      val totalHits = collection?.metadata?.totalHits
      val hasNextLink = collection?.links.orEmpty().any { it.rel.equals("next", ignoreCase = true) }
      val reachedEnd =
        rawItems.isEmpty() || !hasNextLink || (totalHits != null && page.toLong() * pageSize >= totalHits)

      LoadResult.Page(
        data = videos,
        prevKey = if (page == FIRST_PAGE) null else page - 1,
        nextKey = if (reachedEnd) null else page + 1,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: HttpException) {
      if (e.code() == HTTP_BAD_REQUEST && page > FIRST_PAGE) {
        LoadResult.Page(data = emptyList(), prevKey = page - 1, nextKey = null)
      } else {
        LoadResult.Error(DataErrorException(e.toNetworkDataError(), e))
      }
    } catch (
      @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
      LoadResult.Error(DataErrorException(e.toNetworkDataError(), e))
    }
  }

  override fun getRefreshKey(state: PagingState<Int, Video>): Int? =
    state.anchorPosition?.let { anchor ->
      val page = state.closestPageToPosition(anchor)
      page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

  companion object {
    const val FIRST_PAGE = 1
  }
}
