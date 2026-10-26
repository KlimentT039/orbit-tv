package com.klt.orbit.feature.favorites

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.component.EmptyState
import com.klt.orbit.core.designsystem.component.LoadingShimmerGrid
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.component.VideoCard
import com.klt.orbit.core.designsystem.component.VideoCardDefaults
import com.klt.orbit.core.designsystem.error.dataErrorMessage
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
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun FavoritesRoute(
  onOpenDetails: (videoId: String) -> Unit,
  onBrowse: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: FavoritesViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is FavoritesEvent.OpenDetails -> {
        onOpenDetails(event.videoId)
      }

      is FavoritesEvent.ShowError -> {
        Toast.makeText(context, dataErrorMessage(context, event.error), Toast.LENGTH_SHORT).show()
      }
    }
  }
  FavoritesScreen(
    uiState = uiState,
    onVideoClick = viewModel::onVideoClick,
    onToggleEditing = viewModel::onToggleEditing,
    onStopEditing = viewModel::onStopEditing,
    onBrowse = onBrowse,
    modifier = modifier,
  )
}

@Composable
internal fun FavoritesScreen(
  uiState: FavoritesUiState,
  onVideoClick: (Video) -> Unit,
  onToggleEditing: () -> Unit,
  onStopEditing: () -> Unit,
  onBrowse: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize().background(OrbitGradients.SpaceBackground)) {
    when (uiState) {
      FavoritesUiState.Loading -> {
        LoadingShimmerGrid(Modifier.padding(OrbitSpacing.SafeArea))
      }

      FavoritesUiState.Empty -> {
        FavoritesEmpty(onBrowse = onBrowse)
      }

      is FavoritesUiState.Content -> {
        FavoritesContent(
          content = uiState,
          onVideoClick = onVideoClick,
          onToggleEditing = onToggleEditing,
          onStopEditing = onStopEditing,
        )
      }
    }
  }
}

/**
 * Focus model:
 * - Entering focuses the last focused card (e.g. after Details), otherwise the first card.
 * - While editing, removing a card moves focus to the card that takes its place (or the new last
 *   one), so focus never disappears with the removed card.
 * - Back while editing leaves edit mode; otherwise it leaves the screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoritesContent(
  content: FavoritesUiState.Content,
  onVideoClick: (Video) -> Unit,
  onToggleEditing: () -> Unit,
  onStopEditing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val videos = content.videos
  val focusMemory = rememberFocusMemory()
  val gridState = rememberLazyGridState()
  val firstCardFocus = remember { FocusRequester() }
  val replacementFocus = remember { FocusRequester() }
  // Index of a card being removed; the card that ends up there receives focus.
  var removedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  val replacementIndex = removedIndex?.coerceAtMost(videos.lastIndex)

  LaunchedEffect(Unit) {
    withFrameNanos { }
    if (!focusMemory.restore()) firstCardFocus.tryRequestFocus()
  }
  LaunchedEffect(videos) {
    if (removedIndex == null) return@LaunchedEffect
    withFrameNanos { }
    replacementFocus.tryRequestFocus()
    removedIndex = null
  }
  BackHandler(enabled = content.isEditing, onBack = onStopEditing)

  Column(modifier = modifier.fillMaxSize()) {
    FavoritesHeader(
      count = videos.size,
      isEditing = content.isEditing,
      onToggleEditing = onToggleEditing,
    )
    CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Page) {
      LazyVerticalGrid(
        columns = GridCells.FixedSize(VideoCardDefaults.Width),
        state = gridState,
        modifier = Modifier.fillMaxSize().testTag(FavoritesTestTags.GRID),
        // Padding inside the grid, not around it, so focused cards can scale without being clipped.
        contentPadding =
          PaddingValues(
            start = OrbitSpacing.safeAreaHorizontal,
            end = OrbitSpacing.safeAreaHorizontal,
            // Room for the focused card's scale and glow below the header.
            top = OrbitSpacing.xl,
            bottom = OrbitSpacing.safeAreaVertical + OrbitSpacing.xl,
          ),
        horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.cardGap),
        verticalArrangement = Arrangement.spacedBy(OrbitSpacing.rowGap),
      ) {
        itemsIndexed(items = videos, key = { _, video -> video.id }) { index, video ->
          val removeLabel = stringResource(R.string.favorites_remove_action)
          VideoCard(
            title = video.title,
            imageUrl = video.thumbnailUrl,
            subtitle = videoOverline(video.dateCreated, video.center),
            actionIcon = if (content.isEditing) OrbitIcons.Close else null,
            actionLabel = removeLabel,
            onClick = {
              if (content.isEditing) removedIndex = index
              onVideoClick(video)
            },
            modifier =
              with(focusMemory) { Modifier.restorableItem(video.id) }
                .then(if (index == 0) Modifier.focusRequester(firstCardFocus) else Modifier)
                .then(if (index == replacementIndex) Modifier.focusRequester(replacementFocus) else Modifier)
                .testTag(FavoritesTestTags.card(video.id)),
          )
        }
      }
    }
  }
}

@Composable
private fun FavoritesHeader(
  count: Int,
  isEditing: Boolean,
  onToggleEditing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(
          start = OrbitSpacing.safeAreaHorizontal,
          end = OrbitSpacing.safeAreaHorizontal,
          top = OrbitSpacing.safeAreaVertical,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.favorites_title),
        style = OrbitTheme.typography.headlineSmall,
        color = OrbitTheme.colors.onSurface,
      )
      Text(
        text =
          if (isEditing) {
            stringResource(R.string.favorites_edit_hint)
          } else {
            pluralStringResource(R.plurals.favorites_count, count, count)
          },
        style = OrbitTheme.typography.bodyMedium,
        color = if (isEditing) OrbitTheme.colors.primary else OrbitTheme.colors.onSurfaceVariant,
      )
    }
    Spacer(Modifier.width(OrbitSpacing.xl))
    OrbitButton(
      text = stringResource(if (isEditing) R.string.favorites_done else R.string.favorites_edit),
      icon = if (isEditing) null else OrbitIcons.Delete,
      onClick = onToggleEditing,
      modifier = Modifier.testTag(FavoritesTestTags.EDIT),
    )
  }
}

@Composable
private fun FavoritesEmpty(
  onBrowse: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val browseFocus = remember { FocusRequester() }
  EmptyState(
    title = stringResource(R.string.favorites_empty_title),
    message = stringResource(R.string.favorites_empty_message),
    icon = OrbitIcons.FavoriteBorder,
    modifier = modifier.fillMaxSize(),
    action = {
      OrbitButton(
        text = stringResource(R.string.favorites_browse),
        icon = OrbitIcons.Home,
        onClick = onBrowse,
        modifier = Modifier.focusRequester(browseFocus).testTag(FavoritesTestTags.BROWSE),
      )
    },
  )
  LaunchedEffect(browseFocus) { browseFocus.tryRequestFocus() }
}

@PreviewTv
@Composable
private fun FavoritesScreenPreview() {
  OrbitTheme {
    FavoritesScreen(
      uiState =
        FavoritesUiState.Content(
          videos =
            persistentListOf(
              Video(id = "1", title = "Apollo 11 Launch"),
              Video(id = "2", title = "Perseverance Lands on Mars"),
              Video(id = "3", title = "Hubble Deep Field"),
            ),
          isEditing = true,
        ),
      onVideoClick = {},
      onToggleEditing = {},
      onStopEditing = {},
      onBrowse = {},
    )
  }
}

internal object FavoritesTestTags {
  const val GRID = "favorites:grid"
  const val EDIT = "favorites:edit"
  const val BROWSE = "favorites:browse"

  fun card(videoId: String) = "favorites:card:$videoId"
}
