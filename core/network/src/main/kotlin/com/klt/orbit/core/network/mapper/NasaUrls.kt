package com.klt.orbit.core.network.mapper

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Makes a NASA asset URL safe to load: NASA returns `http://` links (blocked by Android's cleartext
 * policy) with raw spaces in the path. Parsing with OkHttp percent-encodes the path, and every NASA
 * host serves the same files over https. Returns null for anything that is not a valid http(s) URL.
 */
internal fun normalizeNasaUrl(raw: String?): String? {
  val url = raw?.trim()?.takeIf { it.isNotEmpty() }?.toHttpUrlOrNull() ?: return null
  return url
    .newBuilder()
    .scheme("https")
    .build()
    .toString()
}

/** The rendition token of a NASA file name, e.g. `large` for `.../abc~large.mp4`, or "" if none. */
internal fun nasaVariant(url: String): String =
  url
    .substringAfterLast('/')
    .substringBeforeLast('.')
    .substringAfterLast('~', missingDelimiterValue = "")
    .lowercase()

internal fun String.hasExtension(vararg extensions: String): Boolean {
  val path = substringBefore('?').lowercase()
  return extensions.any { path.endsWith(".$it") }
}
