package com.klt.orbit.domain.testing

import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [FavoritesRepository]. Set [failWith] to make writes fail. */
class FakeFavoritesRepository(
  initial: List<Video> = emptyList(),
) : FavoritesRepository {
  private val favorites = MutableStateFlow(initial)
  var failWith: DataError? = null

  override fun observeFavorites(): Flow<List<Video>> = favorites

  override fun observeIsFavorite(videoId: String): Flow<Boolean> =
    favorites.map { list -> list.any { it.id == videoId } }

  override suspend fun addFavorite(video: Video): Outcome<Unit> =
    write { favorites.update { list -> listOf(video) + list.filterNot { it.id == video.id } } }

  override suspend fun removeFavorite(videoId: String): Outcome<Unit> =
    write { favorites.update { list -> list.filterNot { it.id == videoId } } }

  private inline fun write(block: () -> Unit): Outcome<Unit> {
    failWith?.let { return Outcome.Failure(it) }
    block()
    return Outcome.Success(Unit)
  }
}
