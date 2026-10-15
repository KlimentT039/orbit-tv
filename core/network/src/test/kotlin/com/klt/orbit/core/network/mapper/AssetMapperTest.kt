package com.klt.orbit.core.network.mapper

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.model.VideoQuality
import com.klt.orbit.core.network.Fixtures
import com.klt.orbit.core.network.NetworkJson
import com.klt.orbit.core.network.dto.AssetCollectionDto
import com.klt.orbit.core.network.dto.AssetItemDto
import com.klt.orbit.core.network.dto.AssetResponseDto
import org.junit.Test

class AssetMapperTest {
  private fun assets(vararg hrefs: String?) =
    AssetResponseDto(AssetCollectionDto(hrefs.map { AssetItemDto(it) })).toVideoAssetsOrNull("id")

  @Test
  fun `picks large then orig then medium from a real asset listing`() {
    val dto = NetworkJson.decodeFromString(AssetResponseDto.serializer(), Fixtures.read("asset_apollo.json"))

    val assets = requireNotNull(dto.toVideoAssetsOrNull("apollo"))

    assertThat(assets.streams.map { it.quality })
      .containsExactly(VideoQuality.LARGE, VideoQuality.ORIGINAL, VideoQuality.MEDIUM)
      .inOrder()
    assertThat(assets.bestStream.url).startsWith("https://images-assets.nasa.gov/")
    assertThat(assets.bestStream.url).endsWith("~large.mp4")
    assertThat(assets.bestStream.url).doesNotContain(" ")
    assertThat(assets.thumbnailUrl).endsWith("~large.jpg")
    assertThat(assets.captionsUrl).endsWith(".srt")
  }

  @Test
  fun `falls back to medium when it is the only supported rendition`() {
    val result = assets("http://x.nasa.gov/a/a~mobile.mp4", "http://x.nasa.gov/a/a~medium.mp4")

    assertThat(result?.streams?.map { it.quality }).containsExactly(VideoQuality.MEDIUM)
  }

  @Test
  fun `returns null when there is no playable mp4`() {
    assertThat(assets("http://x.nasa.gov/a/a~small.mp4", "http://x.nasa.gov/a/a~large.jpg", null, "")).isNull()
    assertThat(AssetResponseDto().toVideoAssetsOrNull("id")).isNull()
  }

  @Test
  fun `prefers un-numbered stills and vtt captions`() {
    val result =
      assets(
        "http://x.nasa.gov/a/a~medium.mp4",
        "http://x.nasa.gov/a/a~orig_1.jpg",
        "http://x.nasa.gov/a/a~medium.jpg",
        "http://x.nasa.gov/a/a.srt",
        "http://x.nasa.gov/a/a.vtt",
      )

    assertThat(result?.thumbnailUrl).isEqualTo("https://x.nasa.gov/a/a~medium.jpg")
    assertThat(result?.captionsUrl).isEqualTo("https://x.nasa.gov/a/a.vtt")
  }
}
