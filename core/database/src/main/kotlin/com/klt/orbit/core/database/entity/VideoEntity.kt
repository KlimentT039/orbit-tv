package com.klt.orbit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Snapshot of a video's metadata, shared by favorites and playback progress so both lists render
 * offline. Rows no longer referenced by either table are pruned.
 */
@Entity(tableName = "videos")
internal data class VideoEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
  @ColumnInfo(name = "date_created") val dateCreatedEpochMs: Long?,
  val center: String?,
  val keywords: List<String>,
)
