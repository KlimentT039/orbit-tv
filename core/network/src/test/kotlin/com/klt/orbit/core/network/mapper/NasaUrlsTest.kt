package com.klt.orbit.core.network.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NasaUrlsTest {
  @Test
  fun `upgrades to https and encodes spaces`() {
    assertThat(normalizeNasaUrl(" http://images-assets.nasa.gov/video/a b/a b~large.mp4 "))
      .isEqualTo("https://images-assets.nasa.gov/video/a%20b/a%20b~large.mp4")
  }

  @Test
  fun `rejects blank and non http urls`() {
    assertThat(normalizeNasaUrl(null)).isNull()
    assertThat(normalizeNasaUrl("  ")).isNull()
    assertThat(normalizeNasaUrl("ftp://example.com/a.mp4")).isNull()
    assertThat(normalizeNasaUrl("not a url")).isNull()
  }

  @Test
  fun `extracts the rendition variant`() {
    assertThat(nasaVariant("https://x/a~Large.mp4")).isEqualTo("large")
    assertThat(nasaVariant("https://x/a~large_2.jpg")).isEqualTo("large_2")
    assertThat(nasaVariant("https://x/a.srt")).isEmpty()
  }
}
