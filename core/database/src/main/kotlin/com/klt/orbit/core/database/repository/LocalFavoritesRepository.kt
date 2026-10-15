package com.klt.orbit.core.database.repository

import androidx.room.withTransaction
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.database.OrbitDatabase
import com.klt.orbit.core.database.entity.FavoriteEntity
import com.klt.orbit.core.database.entity.toEntity
import com.klt.orbit.core.database.entity.toModel
import com.klt.orbit.core.database.localWrite
import com.klt.orbit.core.database.orOnStorageError
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.FavoritesRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class LocalFavoritesRepository
  @Inject
  constructor(
    private val database: OrbitDatabase,
    private val clock: Clock,
  ) : FavoritesRepository {
    private val favoriteDao = database.favoriteDao()
    private val videoDao = database.videoDao()

    override fun observeFavorites(): Flow<List<Video>> =
      favoriteDao
        .observeFavoriteVideos()
        .map { entities -> entities.map { it.toModel() } }
        .orOnStorageError(emptyList())

    override fun observeIsFavorite(videoId: String): Flow<Boolean> =
      favoriteDao
        .observeIsFavorite(videoId)
        .distinctUntilChanged()
        .orOnStorageError(false)

    override suspend fun addFavorite(video: Video): Outcome<Unit> =
      localWrite {
        database.withTransaction {
          videoDao.upsert(video.toEntity())
          favoriteDao.upsert(FavoriteEntity(video.id, clock.millis()))
        }
      }

    override suspend fun removeFavorite(videoId: String): Outcome<Unit> =
      localWrite {
        database.withTransaction {
          favoriteDao.delete(videoId)
          videoDao.deleteUnreferenced()
        }
      }
  }
