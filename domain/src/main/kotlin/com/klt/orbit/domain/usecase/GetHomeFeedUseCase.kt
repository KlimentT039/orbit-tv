package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoCategory
import com.klt.orbit.domain.model.CategoryRow
import com.klt.orbit.domain.model.HomeFeed
import com.klt.orbit.domain.repository.VideoRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Loads every category row concurrently. A row that fails or comes back empty is dropped, so one
 * flaky query never blanks the whole screen; the feed fails only when no row loaded at all.
 *
 * The hero features the top video of each row, which gives a varied carousel without extra calls.
 */
class GetHomeFeedUseCase
  @Inject
  constructor(
    private val videoRepository: VideoRepository,
  ) {
    suspend operator fun invoke(): Outcome<HomeFeed> =
      coroutineScope {
        val results =
          VideoCategory.entries
            .map { category -> async { category to videoRepository.getVideos(category.query, ROW_SIZE) } }
            .awaitAll()

        val rows =
          results.mapNotNull { (category, outcome) ->
            (outcome as? Outcome.Success)?.value?.takeIf { it.isNotEmpty() }?.let { CategoryRow(category, it) }
          }
        if (rows.isEmpty()) {
          val firstError = results.firstNotNullOfOrNull { (it.second as? Outcome.Failure)?.error }
          return@coroutineScope Outcome.Failure(firstError ?: DataError.NotFound)
        }
        Outcome.Success(HomeFeed(featured = pickFeatured(rows), rows = rows))
      }

    private fun pickFeatured(rows: List<CategoryRow>): List<Video> =
      rows
        .mapNotNull { row -> row.videos.firstOrNull { it.thumbnailUrl != null } }
        .distinctBy { it.id }

    companion object {
      const val ROW_SIZE = 20
    }
  }
