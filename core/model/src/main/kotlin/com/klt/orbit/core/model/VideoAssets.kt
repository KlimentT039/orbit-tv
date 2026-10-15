package com.klt.orbit.core.model

/**
 * The playable files for one video.
 *
 * @property streams playable MP4s ordered best first; never empty. The player starts with the first
 *   and may fall back to the next one if a stream fails to decode.
 * @property thumbnailUrl the largest still image available, for backdrops.
 */
data class VideoAssets(
  val videoId: String,
  val streams: List<VideoStream>,
  val thumbnailUrl: String? = null,
  val captionsUrl: String? = null,
) {
  init {
    require(streams.isNotEmpty()) { "VideoAssets needs at least one playable stream" }
  }

  val bestStream: VideoStream get() = streams.first()
}

data class VideoStream(
  val url: String,
  val quality: VideoQuality,
)

/** NASA's MP4 renditions we accept, in order of preference. */
enum class VideoQuality {
  LARGE,
  ORIGINAL,
  MEDIUM,
}
