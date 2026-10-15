package com.klt.orbit.core.network

import com.klt.orbit.core.network.di.createNasaApi
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient

internal object Fixtures {
  fun read(name: String): String =
    requireNotNull(Fixtures::class.java.classLoader?.getResource(name)) { "Missing fixture $name" }.readText()

  fun api(server: MockWebServer): NasaApi = createNasaApi(server.url("/").toString(), OkHttpClient(), NetworkJson)

  fun json(
    body: String,
    code: Int = 200,
  ): MockResponse =
    MockResponse
      .Builder()
      .code(code)
      .addHeader("Content-Type", "application/json")
      .body(body)
      .build()

  /** A search response with [count] valid items, ids `prefix-<n>`, and paging metadata. */
  fun searchPage(
    ids: List<String>,
    totalHits: Int,
    hasNext: Boolean = true,
  ): String {
    val items =
      ids.joinToString(",") { id ->
        """{"data":[{"nasa_id":"$id","title":"Title $id","media_type":"video"}],"links":[]}"""
      }
    val links = if (hasNext) """[{"rel":"next","href":"http://images-api.nasa.gov/search?page=2"}]""" else "[]"
    return """{"collection":{"items":[$items],"metadata":{"total_hits":$totalHits},"links":$links}}"""
  }
}
