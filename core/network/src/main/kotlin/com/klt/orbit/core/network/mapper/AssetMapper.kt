package com.klt.orbit.core.network.mapper

import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.core.model.VideoQuality
import com.klt.orbit.core.model.VideoStream
import com.klt.orbit.core.network.dto.AssetResponseDto

/**
 * Picks the playable files from an asset listing.
 *
 * MP4 preference is `~large`, then `~orig`, then `~medium`: `large` is NASA's web encode and streams
 * reliably, while `orig` is the master file, which can run to several gigabytes and may use codecs a
 * TV cannot decode. Every candidate is kept, best first, so the player can fall back.
 *
 * Returns null when there is no playable MP4.
 */
internal fun AssetResponseDto.toVideoAssetsOrNull(videoId: String): VideoAssets? {
  val urls =
    collection
      ?.items
      .orEmpty()
      .mapNotNull { normalizeNasaUrl(it.href) }
      .distinct()

  val streams =
    urls
      .filter { it.hasExtension("mp4") }
      .mapNotNull { url -> qualityOf(url)?.let { VideoStream(url, it) } }
      .distinctBy { it.quality }
      .sortedBy { it.quality.ordinal }
  if (streams.isEmpty()) return null

  return VideoAssets(
    videoId = videoId,
    streams = streams,
    thumbnailUrl = urls.filter { it.hasExtension("jpg", "jpeg", "png") }.minByOrNull(::imageRank),
    captionsUrl = urls.firstOrNull { it.hasExtension("vtt") } ?: urls.firstOrNull { it.hasExtension("srt") },
  )
}

private fun qualityOf(url: String): VideoQuality? =
  when (nasaVariant(url)) {
    "large" -> VideoQuality.LARGE
    "orig" -> VideoQuality.ORIGINAL
    "medium" -> VideoQuality.MEDIUM
    else -> null
  }
