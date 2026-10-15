package com.klt.orbit.domain.repository

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import kotlinx.coroutines.flow.Flow

/** Locally saved favorites. Stores a snapshot of each video so the list works offline. */
interface FavoritesRepository {
  /** Favorites, most recently added first. */
  fun observeFavorites(): Flow<List<Video>>

  fun observeIsFavorite(videoId: String): Flow<Boolean>

  suspend fun addFavorite(video: Video): Outcome<Unit>

  suspend fun removeFavorite(videoId: String): Outcome<Unit>
}
