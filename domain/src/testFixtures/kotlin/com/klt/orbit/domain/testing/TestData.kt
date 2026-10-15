package com.klt.orbit.domain.testing

import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.core.model.VideoQuality
import com.klt.orbit.core.model.VideoStream
import java.time.Instant

/** Small builders for test data with sensible defaults. */
object TestData {
  fun video(
    id: String = "video-1",
    title: String = "Video $id",
    description: String = "Description of $id",
    thumbnailUrl: String? = "https://images-assets.nasa.gov/video/$id/$id~large.jpg",
    keywords: List<String> = listOf("Apollo", "NASA"),
    dateCreated: Instant? = Instant.parse("1969-07-20T00:00:00Z"),
    center: String? = "JSC",
  ) = Video(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    dateCreated = dateCreated,
    center = center,
    keywords = keywords,
  )

  fun videos(
    count: Int,
    prefix: String = "video",
  ): List<Video> = List(count) { video(id = "$prefix-${it + 1}") }

  fun assets(videoId: String = "video-1") =
    VideoAssets(
      videoId = videoId,
      streams =
        listOf(
          VideoStream("https://images-assets.nasa.gov/video/$videoId/$videoId~large.mp4", VideoQuality.LARGE),
          VideoStream("https://images-assets.nasa.gov/video/$videoId/$videoId~medium.mp4", VideoQuality.MEDIUM),
        ),
      thumbnailUrl = "https://images-assets.nasa.gov/video/$videoId/$videoId~orig.jpg",
    )
}
