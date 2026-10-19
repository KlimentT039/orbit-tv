package com.klt.orbit.feature.home

import androidx.compose.runtime.Immutable
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoCategory
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface HomeUiState {
  data object Loading : HomeUiState

  /** Nothing to show at all: the catalog failed and there is no saved content. */
  data class Error(
    val error: DataError,
  ) : HomeUiState

  /**
   * @property feedError set when the catalog failed but saved rows (Continue Watching, Favorites)
   *   can still be shown; the screen offers a retry below them.
   */
  data class Content(
    val featured: ImmutableList<Video>,
    val rows: ImmutableList<HomeRow>,
    val feedError: DataError? = null,
  ) : HomeUiState
}

@Immutable
data class HomeRow(
  val kind: HomeRowKind,
  val items: ImmutableList<HomeItem>,
) {
  /** Stable across loads; used for lazy keys and focus restoration. */
  val key: String
    get() =
      when (kind) {
        HomeRowKind.ContinueWatching -> "continue_watching"
        HomeRowKind.Favorites -> "favorites"
        is HomeRowKind.Category -> "category_${kind.category.name.lowercase()}"
      }
}

@Immutable
sealed interface HomeRowKind {
  data object ContinueWatching : HomeRowKind

  data object Favorites : HomeRowKind

  data class Category(
    val category: VideoCategory,
  ) : HomeRowKind
}

/** A card: the video plus its watch progress (0..1) where relevant. */
@Immutable
data class HomeItem(
  val video: Video,
  val progress: Float? = null,
)

sealed interface HomeEvent {
  data class OpenDetails(
    val videoId: String,
  ) : HomeEvent

  data class Play(
    val videoId: String,
  ) : HomeEvent
}
