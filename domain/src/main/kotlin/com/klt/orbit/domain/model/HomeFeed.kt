package com.klt.orbit.domain.model

import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoCategory

/** Remote part of Home: hero picks plus one row per category that loaded. */
data class HomeFeed(
  val featured: List<Video>,
  val rows: List<CategoryRow>,
)

data class CategoryRow(
  val category: VideoCategory,
  val videos: List<Video>,
)
