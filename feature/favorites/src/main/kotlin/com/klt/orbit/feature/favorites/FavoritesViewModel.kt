package com.klt.orbit.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.usecase.ObserveFavoritesUseCase
import com.klt.orbit.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel
  @Inject
  constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
  ) : ViewModel() {
    private val isEditing = MutableStateFlow(false)

    private val eventChannel = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events: Flow<FavoritesEvent> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<FavoritesUiState> =
      combine(
        // Nothing left to remove: leave edit mode so the next favorite does not open in it.
        observeFavorites().onEach { if (it.isEmpty()) isEditing.value = false },
        isEditing,
      ) { videos, editing ->
        if (videos.isEmpty()) FavoritesUiState.Empty else FavoritesUiState.Content(videos.toImmutableList(), editing)
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoritesUiState.Loading)

    fun onToggleEditing() {
      isEditing.value = !isEditing.value
    }

    fun onStopEditing() {
      isEditing.value = false
    }

    /** Opens the video, or removes it while editing. */
    fun onVideoClick(video: Video) {
      if (!isEditing.value) {
        eventChannel.trySend(FavoritesEvent.OpenDetails(video.id))
        return
      }
      viewModelScope.launch {
        val result = removeFavorite(video.id)
        if (result is Outcome.Failure) eventChannel.send(FavoritesEvent.ShowError(result.error))
      }
    }

    private companion object {
      const val STOP_TIMEOUT_MS = 5_000L
    }
  }
