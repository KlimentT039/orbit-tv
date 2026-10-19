package com.klt.orbit.core.common.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackTimeTest {
  @Test
  fun `formats minutes and seconds`() {
    assertThat(formatPlaybackTime(0)).isEqualTo("0:00")
    assertThat(formatPlaybackTime(65_400)).isEqualTo("1:05")
    assertThat(formatPlaybackTime(59 * 60_000L + 59_999)).isEqualTo("59:59")
  }

  @Test
  fun `adds hours from one hour`() {
    assertThat(formatPlaybackTime(3_600_000)).isEqualTo("1:00:00")
    assertThat(formatPlaybackTime(2 * 3_600_000L + 3 * 60_000 + 4_000)).isEqualTo("2:03:04")
  }

  @Test
  fun `clamps negative positions`() {
    assertThat(formatPlaybackTime(-5_000)).isEqualTo("0:00")
  }
}
