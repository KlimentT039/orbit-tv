package com.klt.orbit.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.klt.orbit.core.database.entity.PlaybackProgressEntity
import com.klt.orbit.core.database.entity.PlaybackProgressWithVideo
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlaybackProgressDao {
  @Query("SELECT * FROM playback_progress WHERE video_id = :videoId")
  fun observe(videoId: String): Flow<PlaybackProgressEntity?>

  @Transaction
  @Query("SELECT * FROM playback_progress ORDER BY updated_at DESC")
  fun observeAllWithVideo(): Flow<List<PlaybackProgressWithVideo>>

  @Upsert
  suspend fun upsert(progress: PlaybackProgressEntity)

  @Query("DELETE FROM playback_progress WHERE video_id = :videoId")
  suspend fun delete(videoId: String)

  @Query("DELETE FROM playback_progress")
  suspend fun deleteAll()
}
