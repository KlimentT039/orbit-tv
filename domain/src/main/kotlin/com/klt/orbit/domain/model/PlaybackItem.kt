package com.klt.orbit.domain.model

import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets

/** Everything the player needs to start a video. */
data class PlaybackItem(
  val video: Video,
  val assets: VideoAssets,
  val startPositionMs: Long,
)
