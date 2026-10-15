package com.klt.orbit.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.klt.orbit.core.database.entity.FavoriteEntity
import com.klt.orbit.core.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteDao {
  @Query(
    """
    SELECT videos.* FROM videos
    INNER JOIN favorites ON favorites.video_id = videos.id
    ORDER BY favorites.added_at DESC
    """,
  )
  fun observeFavoriteVideos(): Flow<List<VideoEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE video_id = :videoId)")
  fun observeIsFavorite(videoId: String): Flow<Boolean>

  @Upsert
  suspend fun upsert(favorite: FavoriteEntity)

  @Query("DELETE FROM favorites WHERE video_id = :videoId")
  suspend fun delete(videoId: String)
}
