package com.klt.orbit.feature.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.usecase.SearchVideosUseCase
import com.klt.orbit.domain.usecase.SearchVideosUseCase.Companion.searchableQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Search as you type: the query is debounced so typing with a remote does not fire a request per
 * letter, and confirming on the keyboard searches right away.
 *
 * The text is held in a [TextFieldState] (Compose's recommended text field state). Updating a
 * text field through an asynchronous `StateFlow` round trip can drop characters while typing.
 * The query is also kept in [SavedStateHandle], so it survives process death.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel
  @Inject
  constructor(
    private val savedStateHandle: SavedStateHandle,
    searchVideos: SearchVideosUseCase,
  ) : ViewModel() {
    val queryState = TextFieldState(initialText = savedStateHandle[QUERY_KEY] ?: "")

    private val typedQuery: Flow<String> = snapshotFlow { queryState.text.toString() }
    private val submittedQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val searchedQuery: StateFlow<String?> =
      merge(
        // Clearing the field returns to suggestions at once; real queries wait for a typing pause.
        typedQuery.debounce { if (searchableQuery(it) == null) 0 else DEBOUNCE_MS },
        submittedQuery,
      ).map(::searchableQuery)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, searchableQuery(queryState.text.toString()))

    val results: Flow<PagingData<Video>> =
      searchedQuery
        .flatMapLatest { query -> if (query == null) flowOf(NotSearching) else searchVideos(query) }
        .cachedIn(viewModelScope)

    val uiState: StateFlow<SearchUiState> =
      searchedQuery
        .map(::SearchUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SearchUiState(searchedQuery.value))

    private val eventChannel = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = eventChannel.receiveAsFlow()

    init {
      viewModelScope.launch { typedQuery.collect { savedStateHandle[QUERY_KEY] = it } }
    }

    /** The keyboard's Search action: skip the debounce. */
    fun onSearch() {
      submittedQuery.tryEmit(queryState.text.toString())
    }

    fun onSuggestionClick(suggestion: String) {
      queryState.setTextAndPlaceCursorAtEnd(suggestion)
      submittedQuery.tryEmit(suggestion)
    }

    fun onVideoClick(video: Video) {
      eventChannel.trySend(SearchEvent.OpenDetails(video.id))
    }

    companion object {
      const val DEBOUNCE_MS = 400L
      private const val STOP_TIMEOUT_MS = 5_000L
      internal const val QUERY_KEY = "query"

      /**
       * Results while there is nothing to search. They report Loading, so that a query that was just
       * entered shows the loading state, not "no results", until its own first page arrives.
       */
      private val NotSearching: PagingData<Video> =
        PagingData.empty(
          LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
          ),
        )
    }
  }
