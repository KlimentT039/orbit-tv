package com.klt.orbit.domain.repository

import androidx.paging.PagingData
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets
import kotlinx.coroutines.flow.Flow

/** Remote NASA video catalog. */
interface VideoRepository {
  /** First page of videos matching [query], for rows and the hero. */
  suspend fun getVideos(
    query: String,
    limit: Int,
  ): Outcome<List<Video>>

  /** One video by its NASA id, e.g. when opened from a deep link. */
  suspend fun getVideo(id: String): Outcome<Video>

  /** The playable files for one video; fails with `NoPlayableMedia` if it has none we support. */
  suspend fun getVideoAssets(id: String): Outcome<VideoAssets>

  /** All videos matching [query], paged. Load errors surface as `DataErrorException`. */
  fun searchVideos(query: String): Flow<PagingData<Video>>
}
