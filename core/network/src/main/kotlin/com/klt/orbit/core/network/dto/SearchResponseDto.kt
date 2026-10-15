package com.klt.orbit.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * `GET /search` response. Every field is optional. [SearchCollectionDto.items] stays raw JSON so
 * each item is decoded on its own: one malformed item is skipped instead of failing the whole page.
 */
@Serializable
internal data class SearchResponseDto(
  val collection: SearchCollectionDto? = null,
)

@Serializable
internal data class SearchCollectionDto(
  val items: List<JsonElement> = emptyList(),
  val metadata: SearchMetadataDto? = null,
  val links: List<CollectionLinkDto> = emptyList(),
)

@Serializable
internal data class SearchMetadataDto(
  @SerialName("total_hits") val totalHits: Int? = null,
)

@Serializable
internal data class CollectionLinkDto(
  val rel: String? = null,
  val href: String? = null,
)

@Serializable
internal data class SearchItemDto(
  val data: List<SearchItemDataDto> = emptyList(),
  val links: List<SearchItemLinkDto>? = null,
)

@Serializable
internal data class SearchItemDataDto(
  @SerialName("nasa_id") val nasaId: String? = null,
  val title: String? = null,
  val description: String? = null,
  val center: String? = null,
  @SerialName("date_created") val dateCreated: String? = null,
  @SerialName("media_type") val mediaType: String? = null,
  /** Usually an array of strings, occasionally a single comma-separated string. */
  val keywords: JsonElement? = null,
)

@Serializable
internal data class SearchItemLinkDto(
  val href: String? = null,
  val rel: String? = null,
  val render: String? = null,
)
