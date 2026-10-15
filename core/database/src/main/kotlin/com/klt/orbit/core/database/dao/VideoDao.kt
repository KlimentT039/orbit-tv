package com.klt.orbit.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.klt.orbit.core.database.entity.VideoEntity

@Dao
internal interface VideoDao {
  @Upsert
  suspend fun upsert(video: VideoEntity)

  @Query("SELECT * FROM videos WHERE id = :id")
  suspend fun get(id: String): VideoEntity?

  /** Removes snapshots that neither a favorite nor a progress entry points to anymore. */
  @Query(
    """
    DELETE FROM videos
    WHERE id NOT IN (SELECT video_id FROM favorites)
      AND id NOT IN (SELECT video_id FROM playback_progress)
    """,
  )
  suspend fun deleteUnreferenced()
}
