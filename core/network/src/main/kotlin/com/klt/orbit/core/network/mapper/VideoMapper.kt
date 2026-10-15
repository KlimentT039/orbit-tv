package com.klt.orbit.core.network.mapper

import com.klt.orbit.core.model.Video
import com.klt.orbit.core.network.dto.SearchItemDto
import com.klt.orbit.core.network.dto.SearchItemLinkDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal const val MAX_DESCRIPTION_LENGTH = 5_000
internal const val MAX_KEYWORDS = 20

/** Decodes raw search items one by one, dropping invalid, non-video and duplicate entries. */
internal fun List<JsonElement>.toVideos(json: Json): List<Video> =
  mapNotNull { element -> element.decodeItemOrNull(json)?.toVideoOrNull() }
    .distinctBy { it.id }

private fun JsonElement.decodeItemOrNull(json: Json): SearchItemDto? =
  try {
    json.decodeFromJsonElement(SearchItemDto.serializer(), this)
  } catch (_: SerializationException) {
    null
  } catch (_: IllegalArgumentException) {
    null
  }

/** Null when the item has no id or is not a video; every other field degrades gracefully. */
internal fun SearchItemDto.toVideoOrNull(): Video? {
  val data = data.firstOrNull()?.takeIf { it.mediaType == null || it.mediaType.equals("video", ignoreCase = true) }
  val id = data?.nasaId?.trim()?.takeIf { it.isNotEmpty() } ?: return null

  return Video(
    id = id,
    title = data.title?.collapseWhitespace()?.takeIf { it.isNotEmpty() } ?: id,
    description = data.description?.let(::cleanDescription).orEmpty(),
    thumbnailUrl = links.orEmpty().bestThumbnailUrl(),
    dateCreated = parseNasaDate(data.dateCreated),
    center = data.center?.trim()?.takeIf { it.isNotEmpty() },
    keywords = parseKeywords(data.keywords),
  )
}

private fun List<SearchItemLinkDto>.bestThumbnailUrl(): String? =
  mapNotNull { link ->
    val isImage =
      link.render.equals("image", ignoreCase = true) || link.href?.hasExtension("jpg", "jpeg", "png") == true
    if (isImage) normalizeNasaUrl(link.href) else null
  }.minByOrNull(::imageRank)

/**
 * Lower is better: un-numbered renditions from largest to smallest, then numbered alternates
 * (`~large_2.jpg`, extra frames from the same video), then anything else.
 */
internal fun imageRank(url: String): Int {
  val variant = nasaVariant(url)
  val rank = IMAGE_VARIANT_ORDER.indexOf(variant.substringBefore('_'))
  return when {
    rank < 0 -> UNKNOWN_RANK
    variant.contains('_') -> rank + NUMBERED_PENALTY
    else -> rank
  }
}

private val IMAGE_VARIANT_ORDER = listOf("orig", "large", "medium", "thumb", "small")
private const val NUMBERED_PENALTY = 10
private const val UNKNOWN_RANK = 100

internal fun parseKeywords(element: JsonElement?): List<String> {
  val raw =
    when (element) {
      is JsonArray -> element.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
      is JsonPrimitive -> listOfNotNull(element.contentOrNull)
      else -> emptyList()
    }
  return raw
    .flatMap { it.split(',', ';') }
    .map { it.collapseWhitespace() }
    .filter { it.isNotEmpty() }
    .distinctBy { it.lowercase() }
    .take(MAX_KEYWORDS)
}

/** NASA uses ISO instants, but some items only carry a date or something unparseable. */
internal fun parseNasaDate(raw: String?): Instant? {
  val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
  return try {
    Instant.parse(value)
  } catch (_: DateTimeParseException) {
    try {
      LocalDate.parse(value.take(ISO_DATE_LENGTH)).atStartOfDay(ZoneOffset.UTC).toInstant()
    } catch (_: DateTimeParseException) {
      null
    }
  }
}

private const val ISO_DATE_LENGTH = 10

private val HTML_BREAK = Regex("(?i)<br\\s*/?>|</p>")
private val HTML_TAG = Regex("<[^>]+>")
private val SPACES = Regex("[ \\t\\u00A0]+")
private val BLANK_LINES = Regex("\\s*\\n\\s*(\\n\\s*)+")
private val HTML_ENTITIES =
  mapOf(
    "&amp;" to "&",
    "&quot;" to "\"",
    "&#39;" to "'",
    "&apos;" to "'",
    "&lt;" to "<",
    "&gt;" to ">",
    "&nbsp;" to " ",
  )

/** Strips HTML, decodes common entities, tidies whitespace and caps the length at a word boundary. */
internal fun cleanDescription(raw: String): String {
  var text = raw.replace(HTML_BREAK, "\n").replace(HTML_TAG, "")
  HTML_ENTITIES.forEach { (entity, value) -> text = text.replace(entity, value) }
  text =
    text
      .lines()
      .joinToString("\n") { it.replace(SPACES, " ").trim() }
      .replace(BLANK_LINES, "\n\n")
      .trim()
  if (text.length <= MAX_DESCRIPTION_LENGTH) return text
  val cut = text.lastIndexOf(' ', MAX_DESCRIPTION_LENGTH).takeIf { it > 0 } ?: MAX_DESCRIPTION_LENGTH
  return text.take(cut).trimEnd() + "…"
}

private fun String.collapseWhitespace(): String = trim().replace(Regex("\\s+"), " ")
