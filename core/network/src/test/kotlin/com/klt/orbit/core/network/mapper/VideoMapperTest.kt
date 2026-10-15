package com.klt.orbit.core.network.mapper

import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.network.Fixtures
import com.klt.orbit.core.network.NetworkJson
import com.klt.orbit.core.network.dto.SearchResponseDto
import java.time.Instant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class VideoMapperTest {
  private fun parse(fixture: String) =
    NetworkJson
      .decodeFromString(SearchResponseDto.serializer(), Fixtures.read(fixture))
      .collection
      ?.items
      .orEmpty()
      .toVideos(NetworkJson)

  @Test
  fun `maps a real search response`() {
    val videos = parse("search_apollo.json")

    assertThat(videos).hasSize(3)
    val first = videos.first()
    assertThat(first.id).isEqualTo("NDTV000908_Apollo_Digest_Series_Spacecraft for Apollo")
    assertThat(first.title).isEqualTo("Apollo Digest Series: Spacecraft for Apollo")
    assertThat(first.center).isEqualTo("AFRC")
    assertThat(first.keywords).containsExactly("Apollo", "AFRC", "NASA", "filmstock").inOrder()
    assertThat(first.dateCreated).isEqualTo(Instant.parse("2021-01-30T00:00:00Z"))
    assertThat(first.thumbnailUrl)
      .isEqualTo(
        "https://images-assets.nasa.gov/video/NDTV000908_Apollo_Digest_Series_Spacecraft%20for%20Apollo/" +
          "NDTV000908_Apollo_Digest_Series_Spacecraft%20for%20Apollo~large.jpg",
      )
  }

  @Test
  fun `survives malformed items and keeps the valid ones`() {
    val videos = parse("search_messy.json")

    assertThat(videos.map { it.id }).containsExactly("sparse one", "html-desc").inOrder()
  }

  @Test
  fun `fills sparse fields safely`() {
    val sparse = parse("search_messy.json").first { it.id == "sparse one" }

    assertThat(sparse.title).isEqualTo("Sparse item")
    assertThat(sparse.description).isEmpty()
    assertThat(sparse.center).isNull()
    assertThat(sparse.keywords).containsExactly("Mars", "Rover", "Curiosity").inOrder()
    assertThat(sparse.dateCreated).isEqualTo(Instant.parse("2012-08-06T00:00:00Z"))
    // http upgraded to https, spaces encoded, medium preferred over thumb.
    assertThat(
      sparse.thumbnailUrl,
    ).isEqualTo("https://images-assets.nasa.gov/video/sparse%20one/sparse%20one~medium.jpg")
  }

  @Test
  fun `falls back to the id for a missing title and cleans html descriptions`() {
    val video = parse("search_messy.json").first { it.id == "html-desc" }

    assertThat(video.title).isEqualTo("html-desc")
    assertThat(video.description).isEqualTo("Line one & more\n\nLine two link")
    assertThat(video.dateCreated).isNull()
    assertThat(video.thumbnailUrl).isNull()
    assertThat(video.keywords).isEmpty()
  }

  @Test
  fun `caps very long descriptions at a word boundary`() {
    val long = "word ".repeat(3_000)

    val cleaned = cleanDescription(long)

    assertThat(cleaned.length).isAtMost(MAX_DESCRIPTION_LENGTH + 1)
    assertThat(cleaned).endsWith("word…")
  }

  @Test
  fun `parses keywords from arrays and comma separated strings`() {
    assertThat(parseKeywords(JsonArray(listOf(JsonPrimitive("A"), JsonPrimitive("a"), JsonPrimitive(" B ")))))
      .containsExactly("A", "B")
      .inOrder()
    assertThat(parseKeywords(JsonPrimitive("x; y, z"))).containsExactly("x", "y", "z").inOrder()
    assertThat(parseKeywords(null)).isEmpty()
  }

  @Test
  fun `caps the number of keywords`() {
    val many = JsonArray(List(50) { JsonPrimitive("k$it") })

    assertThat(parseKeywords(many)).hasSize(MAX_KEYWORDS)
  }
}
