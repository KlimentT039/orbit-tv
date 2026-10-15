package com.klt.orbit.core.model

/** The fixed topic rows on Home. Display names are UI strings and live in the feature. */
enum class VideoCategory(
  val query: String,
) {
  APOLLO("apollo"),
  MARS("mars"),
  HUBBLE("hubble"),
  ISS("international space station"),
  EARTH("earth"),
}
