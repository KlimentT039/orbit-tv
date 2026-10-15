package com.klt.orbit.core.database

import androidx.room.TypeConverter

/** Stores keyword lists in one column, separated by the ASCII unit separator (never in keywords). */
internal class Converters {
  @TypeConverter
  fun fromKeywords(keywords: List<String>): String = keywords.joinToString(SEPARATOR)

  @TypeConverter
  fun toKeywords(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

  private companion object {
    const val SEPARATOR = "\u001F"
  }
}
