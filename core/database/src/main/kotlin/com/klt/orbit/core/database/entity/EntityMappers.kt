package com.klt.orbit.core.database.entity

import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import java.time.Instant

internal fun Video.toEntity() =
  VideoEntity(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    dateCreatedEpochMs = dateCreated?.toEpochMilli(),
    center = center,
    keywords = keywords,
  )

internal fun VideoEntity.toModel() =
  Video(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    dateCreated = dateCreatedEpochMs?.let(Instant::ofEpochMilli),
    center = center,
    keywords = keywords,
  )

internal fun PlaybackProgressEntity.toModel() =
  PlaybackProgress(
    videoId = videoId,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs),
  )

internal fun PlaybackProgressWithVideo.toModel() =
  ContinueWatchingItem(
    video = video.toModel(),
    progress = progress.toModel(),
  )
