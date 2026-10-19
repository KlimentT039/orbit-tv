package com.klt.orbit.core.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Formats a NASA date in the user's locale (e.g. "Jul 20, 1969"). NASA dates are midnight UTC, so
 * they are formatted in UTC to avoid showing the previous day west of Greenwich.
 */
@Composable
fun formatDisplayDate(instant: Instant?): String? {
  if (instant == null) return null
  val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
  val formatter =
    remember(locale) {
      DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneOffset.UTC)
        .let { if (locale != null) it.withLocale(locale) else it }
    }
  return formatter.format(instant)
}

/** "Jul 20, 1969 · JSC", skipping whichever part is missing; null if both are. */
@Composable
fun videoOverline(
  date: Instant?,
  center: String?,
): String? = listOfNotNull(formatDisplayDate(date), center).joinToString(" · ").ifEmpty { null }
