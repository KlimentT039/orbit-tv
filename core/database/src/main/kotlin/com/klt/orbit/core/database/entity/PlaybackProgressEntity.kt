package com.klt.orbit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
  tableName = "playback_progress",
  foreignKeys = [
    ForeignKey(
      entity = VideoEntity::class,
      parentColumns = ["id"],
      childColumns = ["video_id"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("updated_at")],
)
internal data class PlaybackProgressEntity(
  @PrimaryKey @ColumnInfo(name = "video_id") val videoId: String,
  @ColumnInfo(name = "position_ms") val positionMs: Long,
  @ColumnInfo(name = "duration_ms") val durationMs: Long,
  @ColumnInfo(name = "updated_at") val updatedAtEpochMs: Long,
)

internal data class PlaybackProgressWithVideo(
  @Embedded val progress: PlaybackProgressEntity,
  @Relation(parentColumn = "video_id", entityColumn = "id")
  val video: VideoEntity,
)
