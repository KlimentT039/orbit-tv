package com.klt.orbit.feature.details

import androidx.compose.runtime.Immutable
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.model.Video
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface DetailsUiState {
  data object Loading : DetailsUiState

  data class Error(
    val error: DataError,
  ) : DetailsUiState

  /**
   * @property backdropUrl the sharpest still available (from the asset listing once loaded).
   * @property resumePositionMs where to resume, or null when there is nothing worth resuming.
   */
  data class Content(
    val video: Video,
    val backdropUrl: String?,
    val isFavorite: Boolean,
    val resumePositionMs: Long?,
    val playback: Playback,
    val related: Related,
  ) : DetailsUiState
}

/** Whether the video can be played, known once its files have been listed. */
@Immutable
sealed interface Playback {
  /** Still checking; Play is offered optimistically. */
  data object Checking : Playback

  data object Available : Playback

  /** No playable file (or the listing failed for good); Play is hidden and the reason shown. */
  data class Unavailable(
    val error: DataError,
  ) : Playback

  val canPlay: Boolean get() = this !is Unavailable
}

@Immutable
sealed interface Related {
  data object Loading : Related

  data class Loaded(
    val videos: ImmutableList<Video>,
  ) : Related

  /** Failed or empty: the row is simply not shown. */
  data object None : Related
}

sealed interface DetailsEvent {
  data class Play(
    val videoId: String,
    val startPositionMs: Long,
  ) : DetailsEvent

  data class OpenDetails(
    val videoId: String,
  ) : DetailsEvent

  data class ShowError(
    val error: DataError,
  ) : DetailsEvent
}
