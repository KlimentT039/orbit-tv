package com.klt.orbit.core.model

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class PlaybackProgressTest {
  private fun progress(
    positionMs: Long,
    durationMs: Long,
  ) = PlaybackProgress("id", positionMs, durationMs, Instant.EPOCH)

  @Test
  fun `fraction is clamped and zero for unknown duration`() {
    assertThat(progress(positionMs = 30, durationMs = 120).fraction).isEqualTo(0.25f)
    assertThat(progress(positionMs = 500, durationMs = 100).fraction).isEqualTo(1f)
    assertThat(progress(positionMs = 500, durationMs = 0).fraction).isEqualTo(0f)
  }

  @Test
  fun `finished at 95 percent or within the last 20 seconds`() {
    assertThat(progress(positionMs = 950_000, durationMs = 1_000_000).isFinished).isTrue()
    assertThat(progress(positionMs = 100_000, durationMs = 115_000).isFinished).isTrue()
    assertThat(progress(positionMs = 500_000, durationMs = 1_000_000).isFinished).isFalse()
  }

  @Test
  fun `resumable only after the first seconds and before the end`() {
    assertThat(progress(positionMs = 4_000, durationMs = 600_000).isResumable).isFalse()
    assertThat(progress(positionMs = 60_000, durationMs = 600_000).isResumable).isTrue()
    assertThat(progress(positionMs = 590_000, durationMs = 600_000).isResumable).isFalse()
  }
}
