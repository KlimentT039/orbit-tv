package com.klt.orbit.feature.favorites

import androidx.compose.runtime.Immutable
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.model.Video
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface FavoritesUiState {
  data object Loading : FavoritesUiState

  data object Empty : FavoritesUiState

  /** @property isEditing clicking a card removes it instead of opening it. */
  data class Content(
    val videos: ImmutableList<Video>,
    val isEditing: Boolean = false,
  ) : FavoritesUiState
}

sealed interface FavoritesEvent {
  data class OpenDetails(
    val videoId: String,
  ) : FavoritesEvent

  data class ShowError(
    val error: DataError,
  ) : FavoritesEvent
}
