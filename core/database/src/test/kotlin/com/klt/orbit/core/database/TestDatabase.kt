package com.klt.orbit.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

internal fun inMemoryDatabase(): OrbitDatabase =
  Room
    .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), OrbitDatabase::class.java)
    .allowMainThreadQueries()
    .build()

/** A clock tests can move forward to control timestamps and ordering. */
internal class MutableClock(
  var now: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) : Clock() {
  override fun getZone(): ZoneId = ZoneOffset.UTC

  override fun withZone(zone: ZoneId): Clock = this

  override fun instant(): Instant = now

  fun advanceSeconds(seconds: Long) {
    now = now.plusSeconds(seconds)
  }
}
