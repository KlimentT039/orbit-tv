package com.klt.orbit.core.network.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.DataErrorException
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.network.Fixtures
import com.klt.orbit.core.network.NetworkJson
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchPagingSourceTest {
  private val server = MockWebServer()
  private val pageSize = 3
  private lateinit var pager: TestPager<Int, Video>

  @Before
  fun setUp() {
    server.start()
    val source = SearchPagingSource(Fixtures.api(server), NetworkJson, query = "mars", pageSize = pageSize)
    pager = TestPager(PagingConfig(pageSize = pageSize, initialLoadSize = pageSize), source)
  }

  @After
  fun tearDown() {
    server.close()
  }

  @Test
  fun `first page has a next key while more results exist`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("a", "b", "c"), totalHits = 10)))

      val page = pager.refresh() as LoadResult.Page

      assertThat(page.data.map { it.id }).containsExactly("a", "b", "c").inOrder()
      assertThat(page.prevKey).isNull()
      assertThat(page.nextKey).isEqualTo(2)
      assertThat(server.takeRequest().url.queryParameter("page")).isEqualTo("1")
    }

  @Test
  fun `stops when total hits are reached`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("a", "b", "c"), totalHits = 3)))

      assertThat((pager.refresh() as LoadResult.Page).nextKey).isNull()
    }

  @Test
  fun `stops when the next link is missing`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("a", "b", "c"), totalHits = 100, hasNext = false)))

      assertThat((pager.refresh() as LoadResult.Page).nextKey).isNull()
    }

  @Test
  fun `drops videos already emitted by an earlier page`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("a", "b", "c"), totalHits = 10)))
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("c", "d", "a"), totalHits = 10)))

      pager.refresh()
      val second = pager.append() as LoadResult.Page

      assertThat(second.data.map { it.id }).containsExactly("d")
    }

  @Test
  fun `treats the api pagination cap as the end of results`() =
    runTest {
      server.enqueue(Fixtures.json(Fixtures.searchPage(listOf("a", "b", "c"), totalHits = 100_000)))
      server.enqueue(Fixtures.json("""{"reason":"Maximum number of search results"}""", code = 400))

      pager.refresh()
      val end = pager.append() as LoadResult.Page

      assertThat(end.data).isEmpty()
      assertThat(end.nextKey).isNull()
    }

  @Test
  fun `wraps failures in a typed data error`() =
    runTest {
      server.enqueue(Fixtures.json("""{"reason":"boom"}""", code = 503))

      val error = pager.refresh() as LoadResult.Error

      assertThat((error.throwable as DataErrorException).error).isEqualTo(DataError.Http(503))
    }
}
