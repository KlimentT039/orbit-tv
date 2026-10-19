package com.klt.orbit.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoAssets
import com.klt.orbit.domain.usecase.GetRelatedVideosUseCase
import com.klt.orbit.domain.usecase.GetVideoAssetsUseCase
import com.klt.orbit.domain.usecase.GetVideoUseCase
import com.klt.orbit.domain.usecase.ObserveIsFavoriteUseCase
import com.klt.orbit.domain.usecase.ObservePlaybackProgressUseCase
import com.klt.orbit.domain.usecase.ToggleFavoriteUseCase
import com.klt.orbit.feature.details.navigation.DetailsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DetailsViewModel
  @Inject
  constructor(
    savedStateHandle: SavedStateHandle,
    private val getVideo: GetVideoUseCase,
    private val getVideoAssets: GetVideoAssetsUseCase,
    private val getRelatedVideos: GetRelatedVideosUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    observePlaybackProgress: ObservePlaybackProgressUseCase,
  ) : ViewModel() {
    private val videoId: String = checkNotNull(savedStateHandle[DetailsDestination.VIDEO_ID_ARG])

    private val video = MutableStateFlow<VideoState>(VideoState.Loading)
    private val assets = MutableStateFlow<AssetsState>(AssetsState.Checking)
    private val related = MutableStateFlow<Related>(Related.Loading)
    private var loadJob: Job? = null

    private val eventChannel = Channel<DetailsEvent>(Channel.BUFFERED)
    val events: Flow<DetailsEvent> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<DetailsUiState> =
      combine(video, assets, related, observeIsFavorite(videoId), observePlaybackProgress(videoId), ::reduce)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DetailsUiState.Loading)

    init {
      load()
    }

    fun onRetry() = load()

    fun onPlayClick() {
      eventChannel.trySend(DetailsEvent.Play(videoId, startPositionMs = 0))
    }

    fun onResumeClick() {
      val position = (uiState.value as? DetailsUiState.Content)?.resumePositionMs ?: 0
      eventChannel.trySend(DetailsEvent.Play(videoId, startPositionMs = position))
    }

    fun onToggleFavoriteClick() {
      val current = (video.value as? VideoState.Loaded)?.video ?: return
      viewModelScope.launch {
        val result = toggleFavorite(current)
        if (result is Outcome.Failure) eventChannel.send(DetailsEvent.ShowError(result.error))
      }
    }

    fun onRelatedClick(video: Video) {
      eventChannel.trySend(DetailsEvent.OpenDetails(video.id))
    }

    private fun load() {
      if (loadJob?.isActive == true) return
      video.value = VideoState.Loading
      loadJob =
        viewModelScope.launch {
          when (val result = getVideo(videoId)) {
            is Outcome.Success -> {
              video.value = VideoState.Loaded(result.value)
              launch { loadAssets() }
              launch { loadRelated(result.value) }
            }

            is Outcome.Failure -> {
              video.value = VideoState.Failed(result.error)
            }
          }
        }
    }

    private suspend fun loadAssets() {
      assets.value = AssetsState.Checking
      assets.value =
        when (val result = getVideoAssets(videoId)) {
          is Outcome.Success -> {
            AssetsState.Ready(result.value)
          }

          // Only a definitive answer hides Play; a network blip lets the player try (and retry) itself.
          is Outcome.Failure -> {
            if (result.error == DataError.NoPlayableMedia || result.error == DataError.NotFound) {
              AssetsState.Unplayable(result.error)
            } else {
              AssetsState.Unknown
            }
          }
        }
    }

    private suspend fun loadRelated(video: Video) {
      related.value = Related.Loading
      related.value =
        when (val result = getRelatedVideos(video)) {
          is Outcome.Success -> {
            result.value.takeIf { it.isNotEmpty() }?.let { Related.Loaded(it.toImmutableList()) } ?: Related.None
          }

          is Outcome.Failure -> {
            Related.None
          }
        }
    }

    private fun reduce(
      video: VideoState,
      assets: AssetsState,
      related: Related,
      isFavorite: Boolean,
      progress: PlaybackProgress?,
    ): DetailsUiState =
      when (video) {
        VideoState.Loading -> {
          DetailsUiState.Loading
        }

        is VideoState.Failed -> {
          DetailsUiState.Error(video.error)
        }

        is VideoState.Loaded -> {
          val playback =
            when (assets) {
              AssetsState.Checking, AssetsState.Unknown -> Playback.Checking
              is AssetsState.Ready -> Playback.Available
              is AssetsState.Unplayable -> Playback.Unavailable(assets.error)
            }
          DetailsUiState.Content(
            video = video.video,
            backdropUrl = (assets as? AssetsState.Ready)?.assets?.thumbnailUrl ?: video.video.thumbnailUrl,
            isFavorite = isFavorite,
            resumePositionMs = progress?.takeIf { it.isResumable && playback.canPlay }?.positionMs,
            playback = playback,
            related = related,
          )
        }
      }

    private sealed interface VideoState {
      data object Loading : VideoState

      data class Loaded(
        val video: Video,
      ) : VideoState

      data class Failed(
        val error: DataError,
      ) : VideoState
    }

    private sealed interface AssetsState {
      data object Checking : AssetsState

      data class Ready(
        val assets: VideoAssets,
      ) : AssetsState

      data class Unplayable(
        val error: DataError,
      ) : AssetsState

      /** The listing could not be fetched; playability is unknown. */
      data object Unknown : AssetsState
    }

    private companion object {
      const val STOP_TIMEOUT_MS = 5_000L
    }
  }
