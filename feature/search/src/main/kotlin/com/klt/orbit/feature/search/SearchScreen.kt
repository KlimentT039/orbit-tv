package com.klt.orbit.feature.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.tv.material3.Text
import com.klt.orbit.core.common.result.toDataError
import com.klt.orbit.core.designsystem.component.BufferingIndicator
import com.klt.orbit.core.designsystem.component.EmptyState
import com.klt.orbit.core.designsystem.component.LoadingShimmerGrid
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.component.OrbitSearchField
import com.klt.orbit.core.designsystem.component.VideoCard
import com.klt.orbit.core.designsystem.component.VideoCardDefaults
import com.klt.orbit.core.designsystem.error.DataErrorState
import com.klt.orbit.core.designsystem.focus.FocusMemory
import com.klt.orbit.core.designsystem.focus.OrbitScrolling
import com.klt.orbit.core.designsystem.focus.rememberFocusMemory
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.designsystem.util.ObserveAsEvents
import com.klt.orbit.core.designsystem.util.videoOverline
import com.klt.orbit.core.model.Video
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
internal fun SearchRoute(
  onOpenDetails: (videoId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SearchViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val results = viewModel.results.collectAsLazyPagingItems()
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is SearchEvent.OpenDetails -> onOpenDetails(event.videoId)
    }
  }
  SearchScreen(
    uiState = uiState,
    queryState = viewModel.queryState,
    results = results,
    onSearch = viewModel::onSearch,
    onSuggestionClick = viewModel::onSuggestionClick,
    onVideoClick = viewModel::onVideoClick,
    modifier = modifier,
  )
}

/**
 * Focus model:
 * - Entering the screen focuses the search field, or the last focused result when coming back
 *   from Details.
 * - Confirming on the keyboard, or picking a suggestion, moves focus to the first result as soon
 *   as it loads.
 * - Back from the results returns to the search field; Back on the field leaves the screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SearchScreen(
  uiState: SearchUiState,
  queryState: TextFieldState,
  results: LazyPagingItems<Video>,
  onSearch: () -> Unit,
  onSuggestionClick: (String) -> Unit,
  onVideoClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  val fieldFocus = remember { FocusRequester() }
  val firstResultFocus = remember { FocusRequester() }
  val focusMemory = rememberFocusMemory()
  val gridState = rememberLazyGridState()
  val scope = rememberCoroutineScope()
  var fieldHasFocus by remember { mutableStateOf(false) }
  var focusResultsWhenLoaded by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    // Wait a frame so lazily composed results exist before restoring focus to one of them.
    withFrameNanos { }
    if (!focusMemory.restore()) fieldFocus.tryRequestFocus()
  }
  LaunchedEffect(focusResultsWhenLoaded, results.itemCount) {
    if (focusResultsWhenLoaded && results.itemCount > 0) {
      withFrameNanos { }
      firstResultFocus.tryRequestFocus()
      focusResultsWhenLoaded = false
    }
  }
  BackHandler(enabled = !fieldHasFocus) {
    scope.launch {
      gridState.scrollToItem(0)
      fieldFocus.tryRequestFocus()
    }
  }

  Box(modifier = modifier.fillMaxSize().background(OrbitGradients.SpaceBackground)) {
    Column(Modifier.fillMaxSize()) {
      OrbitSearchField(
        state = queryState,
        onSearch = {
          onSearch()
          focusResultsWhenLoaded = true
        },
        placeholder = stringResource(R.string.search_placeholder),
        focusedPlaceholder = stringResource(R.string.search_placeholder_focused),
        modifier =
          Modifier
            .padding(
              start = OrbitSpacing.safeAreaHorizontal,
              end = OrbitSpacing.safeAreaHorizontal,
              top = OrbitSpacing.safeAreaVertical,
            ).widthIn(max = SearchDefaults.FieldMaxWidth)
            .fillMaxWidth()
            .focusRequester(fieldFocus)
            .onFocusChanged {
              fieldHasFocus = it.isFocused
              // Focus restoration should return to the field, not to a result left earlier.
              if (it.isFocused) focusMemory.forget()
            }.testTag(SearchTestTags.FIELD),
      )
      Spacer(Modifier.height(OrbitSpacing.lg))
      val query = uiState.searchedQuery
      if (query == null) {
        SearchSuggestions(
          suggestions = stringArrayResource(R.array.search_suggestions).toImmutableList(),
          onClick = { suggestion ->
            onSuggestionClick(suggestion)
            // The suggestion buttons disappear with the results, so focus must move somewhere.
            focusResultsWhenLoaded = true
          },
        )
      } else {
        SearchResults(
          query = query,
          results = results,
          gridState = gridState,
          focusMemory = focusMemory,
          firstResultFocus = firstResultFocus,
          onVideoClick = onVideoClick,
        )
      }
    }
  }
}

@Composable
private fun SearchSuggestions(
  suggestions: ImmutableList<String>,
  onClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = OrbitSpacing.safeAreaHorizontal),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    EmptyState(
      title = stringResource(R.string.search_idle_title),
      message = stringResource(R.string.search_idle_message),
      icon = OrbitIcons.Search,
    )
    Text(
      text = stringResource(R.string.search_suggestions_label),
      style = OrbitTheme.typography.labelLarge,
      color = OrbitTheme.colors.onSurfaceVariant,
    )
    Spacer(Modifier.height(OrbitSpacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.md)) {
      suggestions.forEach { suggestion ->
        OrbitButton(
          text = suggestion,
          onClick = { onClick(suggestion) },
          modifier = Modifier.testTag(SearchTestTags.suggestion(suggestion)),
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResults(
  query: String,
  results: LazyPagingItems<Video>,
  gridState: LazyGridState,
  focusMemory: FocusMemory,
  firstResultFocus: FocusRequester,
  onVideoClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  val refresh = results.loadState.refresh
  val isEmpty = results.itemCount == 0
  when {
    isEmpty && refresh is LoadState.Loading -> {
      LoadingShimmerGrid(
        modifier = modifier.padding(horizontal = OrbitSpacing.safeAreaHorizontal, vertical = OrbitSpacing.md),
      )
    }

    isEmpty && refresh is LoadState.Error -> {
      DataErrorState(
        error = refresh.error.toDataError(),
        onRetry = results::retry,
        // Keep focus in the search field while typing; Retry is one press down.
        requestFocusOnShow = false,
        modifier = modifier.fillMaxSize(),
      )
    }

    isEmpty -> {
      EmptyState(
        title = stringResource(R.string.search_no_results_title),
        message = stringResource(R.string.search_no_results_message, query),
        icon = OrbitIcons.Search,
        modifier = modifier.fillMaxSize(),
      )
    }

    else -> {
      CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Page) {
        LazyVerticalGrid(
          columns = GridCells.FixedSize(VideoCardDefaults.Width),
          state = gridState,
          modifier = modifier.fillMaxSize().testTag(SearchTestTags.GRID),
          // Padding inside the grid, not around it, so focused cards can scale without being clipped.
          contentPadding =
            PaddingValues(
              start = OrbitSpacing.safeAreaHorizontal,
              end = OrbitSpacing.safeAreaHorizontal,
              top = OrbitSpacing.md,
              bottom = OrbitSpacing.safeAreaVertical + OrbitSpacing.xl,
            ),
          horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.cardGap),
          verticalArrangement = Arrangement.spacedBy(OrbitSpacing.rowGap),
        ) {
          item(span = { GridItemSpan(maxLineSpan) }, key = "title", contentType = "title") {
            Text(
              text = stringResource(R.string.search_results_title, query),
              style = OrbitTheme.typography.titleMedium,
              color = OrbitTheme.colors.onSurfaceVariant,
            )
          }
          items(count = results.itemCount, key = results.itemKey { it.id }, contentType = { "video" }) { index ->
            val video = results[index] ?: return@items
            VideoCard(
              title = video.title,
              imageUrl = video.thumbnailUrl,
              subtitle = videoOverline(video.dateCreated, video.center),
              onClick = { onVideoClick(video) },
              modifier =
                with(focusMemory) { Modifier.restorableItem(video.id) }
                  .then(if (index == 0) Modifier.focusRequester(firstResultFocus) else Modifier)
                  .testTag(SearchTestTags.result(video.id)),
            )
          }
          appendState(results)
        }
      }
    }
  }
}

private fun LazyGridScope.appendState(results: LazyPagingItems<Video>) {
  when (results.loadState.append) {
    is LoadState.Loading -> {
      item(span = { GridItemSpan(maxLineSpan) }, key = "append_loading", contentType = "footer") {
        Box(Modifier.fillMaxWidth().padding(OrbitSpacing.lg), contentAlignment = Alignment.Center) {
          BufferingIndicator(Modifier.size(40.dp))
        }
      }
    }

    is LoadState.Error -> {
      item(span = { GridItemSpan(maxLineSpan) }, key = "append_error", contentType = "footer") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          OrbitButton(
            text = stringResource(R.string.search_load_more_error),
            icon = OrbitIcons.Replay,
            onClick = results::retry,
          )
        }
      }
    }

    is LoadState.NotLoading -> {}
  }
}

@Composable
@PreviewTv
private fun SearchScreenPreview() {
  OrbitTheme {
    SearchScreen(
      uiState = SearchUiState(),
      queryState = rememberTextFieldState(),
      results = flowOf(PagingData.empty<Video>()).collectAsLazyPagingItems(),
      onSearch = {},
      onSuggestionClick = {},
      onVideoClick = {},
    )
  }
}

internal object SearchDefaults {
  val FieldMaxWidth = 640.dp
}

internal object SearchTestTags {
  const val FIELD = "search:field"
  const val GRID = "search:grid"

  fun suggestion(text: String) = "search:suggestion:$text"

  fun result(videoId: String) = "search:result:$videoId"
}
