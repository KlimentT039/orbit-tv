package com.klt.orbit.core.network.repository

import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.VideoQuality
import com.klt.orbit.core.network.Fixtures
import com.klt.orbit.core.network.NetworkJson
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class NasaVideoRepositoryTest {
  private val server = MockWebServer()
  private lateinit var repository: NasaVideoRepository

  @Before
  fun setUp() {
    server.start()
    repository = NasaVideoRepository(Fixtures.api(server), NetworkJson)
  }

  @After
  fun tearDown() {
    server.close()
  }

  @Test
  fun `getVideos requests a video search and maps the results`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.read("search_apollo.json")))

      val result = repository.getVideos("apollo", limit = 2)

      assertThat((result as Outcome.Success).value).hasSize(2)
      val url = server.takeRequest().url
      assertThat(url.encodedPath).isEqualTo("/search")
      assertThat(url.queryParameter("q")).isEqualTo("apollo")
      assertThat(url.queryParameter("media_type")).isEqualTo("video")
      assertThat(url.queryParameter("page_size")).isEqualTo("2")
    }

  @Test
  fun `getVideos maps server errors`() =
    runTest {
      server.enqueue(Fixtures.json("""{"reason":"boom"}""", code = 500))

      assertThat(repository.getVideos("apollo", 10)).isEqualTo(Outcome.Failure(DataError.Http(500)))
    }

  @Test
  fun `getVideos maps unreadable responses to invalid data`() =
    runTest {
      server.enqueue(Fixtures.json("""{"collection": "#not json"""))

      assertThat(repository.getVideos("apollo", 10)).isEqualTo(Outcome.Failure(DataError.InvalidData))
    }

  @Test
  fun `getVideos maps connection failures to network errors`() =
    runTest {
      server.close()

      assertThat(repository.getVideos("apollo", 10)).isEqualTo(Outcome.Failure(DataError.Network))
    }

  @Test
  fun `getVideo looks the video up by nasa id`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("abc"), totalHits = 1, hasNext = false)))

      val result = repository.getVideo("abc")

      assertThat((result as Outcome.Success).value.id).isEqualTo("abc")
      assertThat(server.takeRequest().url.queryParameter("nasa_id")).isEqualTo("abc")
    }

  @Test
  fun `getVideo fails with not found when nothing matches`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(emptyList(), totalHits = 0, hasNext = false)))

      assertThat(repository.getVideo("missing")).isEqualTo(Outcome.Failure(DataError.NotFound))
    }

  @Test
  fun `getVideoAssets returns ordered streams`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.read("asset_apollo.json")))

      val result = repository.getVideoAssets("NDTV000908 Apollo")

      assertThat((result as Outcome.Success).value.bestStream.quality).isEqualTo(VideoQuality.LARGE)
      assertThat(server.takeRequest().url.encodedPath).isEqualTo("/asset/NDTV000908%20Apollo")
    }

  @Test
  fun `getVideoAssets reports a video without playable files`() =
    runTest {
      server.enqueue(Fixtures.json("""{"collection":{"items":[{"href":"http://x/a~thumb.jpg"}]}}"""))

      assertThat(repository.getVideoAssets("a")).isEqualTo(Outcome.Failure(DataError.NoPlayableMedia))
    }

  @Test
  fun `getVideoAssets maps 404 to not found`() =
    runTest {
      server.enqueue(Fixtures.json("""{"reason":"No AssetDB records"}""", code = 404))

      assertThat(repository.getVideoAssets("nope")).isEqualTo(Outcome.Failure(DataError.NotFound))
    }

  @Test
  fun `searchVideos pages through results`() =
    runTest {
      val size = NasaVideoRepository.SEARCH_PAGE_SIZE
      server.enqueue(Fixtures.json(Fixtures.searchPage(List(size) { "p1-$it" }, totalHits = size + 5)))
      server.enqueue(Fixtures.json(Fixtures.searchPage(List(5) { "p2-$it" }, totalHits = size + 5)))

      val snapshot = repository.searchVideos("mars").asSnapshot { scrollTo(size) }

      assertThat(snapshot).hasSize(size + 5)
      assertThat(server.requestCount).isEqualTo(2)
    }
}
