package com.klt.orbit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "favorites",
  foreignKeys = [
    ForeignKey(
      entity = VideoEntity::class,
      parentColumns = ["id"],
      childColumns = ["video_id"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("added_at")],
)
internal data class FavoriteEntity(
  @PrimaryKey @ColumnInfo(name = "video_id") val videoId: String,
  @ColumnInfo(name = "added_at") val addedAtEpochMs: Long,
)
