package com.klt.orbit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.klt.orbit.core.database.dao.FavoriteDao
import com.klt.orbit.core.database.dao.PlaybackProgressDao
import com.klt.orbit.core.database.dao.VideoDao
import com.klt.orbit.core.database.entity.FavoriteEntity
import com.klt.orbit.core.database.entity.PlaybackProgressEntity
import com.klt.orbit.core.database.entity.VideoEntity

@Database(
  entities = [VideoEntity::class, FavoriteEntity::class, PlaybackProgressEntity::class],
  version = 1,
  exportSchema = true,
)
@TypeConverters(Converters::class)
internal abstract class OrbitDatabase : RoomDatabase() {
  abstract fun videoDao(): VideoDao

  abstract fun favoriteDao(): FavoriteDao

  abstract fun playbackProgressDao(): PlaybackProgressDao

  companion object {
    const val NAME = "orbit.db"
  }
}
