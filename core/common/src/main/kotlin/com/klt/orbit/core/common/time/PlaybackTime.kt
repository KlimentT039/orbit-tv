package com.klt.orbit.core.common.time

private const val MS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L

/** Formats a playback position as `m:ss`, or `h:mm:ss` from one hour. Negative values show as 0:00. */
fun formatPlaybackTime(positionMs: Long): String {
  val totalSeconds = positionMs.coerceAtLeast(0) / MS_PER_SECOND
  val hours = totalSeconds / SECONDS_PER_HOUR
  val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
  val seconds = totalSeconds % SECONDS_PER_MINUTE
  return if (hours > 0) {
    "%d:%02d:%02d".format(hours, minutes, seconds)
  } else {
    "%d:%02d".format(minutes, seconds)
  }
}
