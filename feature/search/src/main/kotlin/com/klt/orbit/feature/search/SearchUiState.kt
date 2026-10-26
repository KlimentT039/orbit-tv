package com.klt.orbit.feature.search

import androidx.compose.runtime.Immutable

/**
 * The typed text lives in [SearchViewModel.queryState]; results are the separate paged
 * [SearchViewModel.results] stream (`PagingData` is not a value type, so it stays out of here).
 *
 * @property searchedQuery the query the results belong to, or null while there is nothing worth
 *   searching yet (empty or too short), when the screen shows suggestions instead.
 */
@Immutable
data class SearchUiState(
  val searchedQuery: String? = null,
)

sealed interface SearchEvent {
  data class OpenDetails(
    val videoId: String,
  ) : SearchEvent
}
