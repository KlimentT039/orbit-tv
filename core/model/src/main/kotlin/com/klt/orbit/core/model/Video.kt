package com.klt.orbit.core.model

import java.time.Instant

/**
 * A NASA video as listed by search. Always has an [id] and a non-blank [title]; every other field
 * may be missing in the source data and is represented as null or empty rather than guessed.
 */
data class Video(
  val id: String,
  val title: String,
  val description: String = "",
  val thumbnailUrl: String? = null,
  val dateCreated: Instant? = null,
  val center: String? = null,
  val keywords: List<String> = emptyList(),
)
