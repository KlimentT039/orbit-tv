package com.klt.orbit.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.model.HomeFeed
import com.klt.orbit.domain.usecase.GetHomeFeedUseCase
import com.klt.orbit.domain.usecase.ObserveContinueWatchingUseCase
import com.klt.orbit.domain.usecase.ObserveFavoritesUseCase
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
class HomeViewModel
  @Inject
  constructor(
    private val getHomeFeed: GetHomeFeedUseCase,
    observeContinueWatching: ObserveContinueWatchingUseCase,
    observeFavorites: ObserveFavoritesUseCase,
  ) : ViewModel() {
    private val feed = MutableStateFlow<FeedState>(FeedState.Loading)
    private var feedJob: Job? = null

    private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> =
      combine(feed, observeContinueWatching(), observeFavorites(), ::reduce)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

    init {
      loadFeed()
    }

    fun onRetry() = loadFeed()

    fun onVideoClick(video: Video) {
      eventChannel.trySend(HomeEvent.OpenDetails(video.id))
    }

    fun onPlayClick(video: Video) {
      eventChannel.trySend(HomeEvent.Play(video.id))
    }

    private fun loadFeed() {
      if (feedJob?.isActive == true) return
      feed.value = FeedState.Loading
      feedJob =
        viewModelScope.launch {
          feed.value =
            when (val result = getHomeFeed()) {
              is Outcome.Success -> FeedState.Loaded(result.value)
              is Outcome.Failure -> FeedState.Failed(result.error)
            }
        }
    }

    private fun reduce(
      feed: FeedState,
      continueWatching: List<ContinueWatchingItem>,
      favorites: List<Video>,
    ): HomeUiState {
      val savedRows =
        listOfNotNull(
          continueWatching.takeIf { it.isNotEmpty() }?.let { items ->
            HomeRow(
              HomeRowKind.ContinueWatching,
              items.map { HomeItem(it.video, it.progress.fraction) }.toImmutableList(),
            )
          },
          favorites.takeIf { it.isNotEmpty() }?.let { videos ->
            HomeRow(HomeRowKind.Favorites, videos.map { HomeItem(it) }.toImmutableList())
          },
        )
      return when (feed) {
        FeedState.Loading -> {
          HomeUiState.Loading
        }

        is FeedState.Loaded -> {
          HomeUiState.Content(
            featured = feed.feed.featured.toImmutableList(),
            rows =
              (
                savedRows +
                  feed.feed.rows.map { row -> HomeRow(HomeRowKind.Category(row.category), row.videos.toItems()) }
              ).toImmutableList(),
          )
        }

        is FeedState.Failed -> {
          if (savedRows.isEmpty()) {
            HomeUiState.Error(feed.error)
          } else {
            HomeUiState.Content(
              featured = savedRows.featuredFallback(),
              rows = savedRows.toImmutableList(),
              feedError = feed.error,
            )
          }
        }
      }
    }

    private fun List<Video>.toItems() = map { HomeItem(it) }.toImmutableList()

    /** Offline, the hero features saved videos that have artwork. */
    private fun List<HomeRow>.featuredFallback() =
      flatMap { row -> row.items.map { it.video } }
        .filter { it.thumbnailUrl != null }
        .distinctBy { it.id }
        .take(MAX_FALLBACK_FEATURED)
        .toImmutableList()

    private sealed interface FeedState {
      data object Loading : FeedState

      data class Loaded(
        val feed: HomeFeed,
      ) : FeedState

      data class Failed(
        val error: DataError,
      ) : FeedState
    }

    private companion object {
      const val STOP_TIMEOUT_MS = 5_000L
      const val MAX_FALLBACK_FEATURED = 5
    }
  }
