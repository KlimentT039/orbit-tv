package com.klt.orbit.core.database.repository

import androidx.room.withTransaction
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.database.OrbitDatabase
import com.klt.orbit.core.database.entity.PlaybackProgressEntity
import com.klt.orbit.core.database.entity.toEntity
import com.klt.orbit.core.database.entity.toModel
import com.klt.orbit.core.database.localWrite
import com.klt.orbit.core.database.orOnStorageError
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class LocalPlaybackProgressRepository
  @Inject
  constructor(
    private val database: OrbitDatabase,
    private val clock: Clock,
  ) : PlaybackProgressRepository {
    private val progressDao = database.playbackProgressDao()
    private val videoDao = database.videoDao()

    override fun observeProgress(videoId: String): Flow<PlaybackProgress?> =
      progressDao
        .observe(videoId)
        .map { it?.toModel() }
        .distinctUntilChanged()
        .orOnStorageError(null)

    override fun observeWatchHistory(): Flow<List<ContinueWatchingItem>> =
      progressDao
        .observeAllWithVideo()
        .map { rows -> rows.map { it.toModel() } }
        .orOnStorageError(emptyList())

    override suspend fun saveProgress(
      video: Video,
      positionMs: Long,
      durationMs: Long,
    ): Outcome<Unit> =
      localWrite {
        database.withTransaction {
          videoDao.upsert(video.toEntity())
          progressDao.upsert(PlaybackProgressEntity(video.id, positionMs, durationMs, clock.millis()))
        }
      }

    override suspend fun clearProgress(videoId: String): Outcome<Unit> =
      localWrite {
        database.withTransaction {
          progressDao.delete(videoId)
          videoDao.deleteUnreferenced()
        }
      }

    override suspend fun clearAll(): Outcome<Unit> =
      localWrite {
        database.withTransaction {
          progressDao.deleteAll()
          videoDao.deleteUnreferenced()
        }
      }
  }
