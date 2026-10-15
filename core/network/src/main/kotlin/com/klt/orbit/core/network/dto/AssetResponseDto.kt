package com.klt.orbit.core.network.dto

import kotlinx.serialization.Serializable

/** `GET /asset/{nasa_id}` response: a flat list of file URLs for one item. */
@Serializable
internal data class AssetResponseDto(
  val collection: AssetCollectionDto? = null,
)

@Serializable
internal data class AssetCollectionDto(
  val items: List<AssetItemDto> = emptyList(),
)

@Serializable
internal data class AssetItemDto(
  val href: String? = null,
)
