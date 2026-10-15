package com.klt.orbit.core.network

import com.klt.orbit.core.network.dto.AssetResponseDto
import com.klt.orbit.core.network.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** NASA Image and Video Library API (https://images.nasa.gov/docs). No API key required. */
internal interface NasaApi {
  @GET("search")
  suspend fun search(
    @Query("q") query: String? = null,
    @Query("nasa_id") nasaId: String? = null,
    @Query("media_type") mediaType: String = MEDIA_TYPE_VIDEO,
    @Query("page") page: Int = 1,
    @Query("page_size") pageSize: Int,
  ): SearchResponseDto

  @GET("asset/{nasa_id}")
  suspend fun asset(
    @Path("nasa_id") nasaId: String,
  ): AssetResponseDto

  companion object {
    const val BASE_URL = "https://images-api.nasa.gov/"
    const val MEDIA_TYPE_VIDEO = "video"
  }
}
