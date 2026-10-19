package com.klt.orbit.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.designsystem.component.ContentRow
import com.klt.orbit.core.designsystem.component.HeroBackdrop
import com.klt.orbit.core.designsystem.component.HeroBanner
import com.klt.orbit.core.designsystem.component.HeroBannerDefaults
import com.klt.orbit.core.designsystem.component.HeroCarousel
import com.klt.orbit.core.designsystem.component.LoadingShimmer
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.component.VideoCard
import com.klt.orbit.core.designsystem.error.DataErrorState
import com.klt.orbit.core.designsystem.error.dataErrorMessage
import com.klt.orbit.core.designsystem.focus.OrbitScrolling
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.designsystem.util.ObserveAsEvents
import com.klt.orbit.core.designsystem.util.videoOverline
import com.klt.orbit.core.model.Video
import com.klt.orbit.core.model.VideoCategory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
internal fun HomeRoute(
  onOpenDetails: (videoId: String) -> Unit,
  onPlay: (videoId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is HomeEvent.OpenDetails -> onOpenDetails(event.videoId)
      is HomeEvent.Play -> onPlay(event.videoId)
    }
  }
  HomeScreen(
    uiState = uiState,
    onVideoClick = viewModel::onVideoClick,
    onPlayClick = viewModel::onPlayClick,
    onRetry = viewModel::onRetry,
    modifier = modifier,
  )
}

@Composable
internal fun HomeScreen(
  uiState: HomeUiState,
  onVideoClick: (Video) -> Unit,
  onPlayClick: (Video) -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    HomeUiState.Loading -> {
      HomeLoading(modifier)
    }

    is HomeUiState.Error -> {
      Box(modifier.fillMaxSize()) {
        HeroBackdrop(imageUrl = null, modifier = Modifier.fillMaxSize())
        DataErrorState(error = uiState.error, onRetry = onRetry, modifier = Modifier.fillMaxSize())
      }
    }

    is HomeUiState.Content -> {
      HomeContent(
        content = uiState,
        onVideoClick = onVideoClick,
        onPlayClick = onPlayClick,
        onRetry = onRetry,
        modifier = modifier,
      )
    }
  }
}

/**
 * Focus model:
 * - First visit focuses the hero's Play button (or the first card if there is no hero).
 * - Every focused card is remembered (saved state), so coming back from Details lands on it.
 * - Each row restores its own last focused card via [ContentRow]'s focus restorer.
 * - Back while browsing the rows returns to the hero first; Back on the hero leaves the app.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
  content: HomeUiState.Content,
  onVideoClick: (Video) -> Unit,
  onPlayClick: (Video) -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val featured = content.featured
  var heroIndex by rememberSaveable { mutableIntStateOf(0) }
  var heroHasFocus by remember { mutableStateOf(false) }
  var focusedCardImage by remember { mutableStateOf<String?>(null) }
  val focus = rememberHomeFocus(rememberLazyListState())
  val scope = rememberCoroutineScope()
  val firstCardKey = content.rows.firstOrNull()?.let { row -> row.items.firstOrNull()?.let { cardKey(row, it) } }

  val heroVideo = featured.getOrNull(heroIndex.coerceIn(0, (featured.size - 1).coerceAtLeast(0)))
  val backdropUrl = if (heroHasFocus || focusedCardImage == null) heroVideo?.thumbnailUrl else focusedCardImage

  val rowKeys = content.rows.map { it.key }
  LaunchedEffect(rowKeys) { focus.settle(rowKeys, hasHero = heroVideo != null) }
  BackHandler(enabled = !heroHasFocus && featured.isNotEmpty()) { scope.launch { focus.returnToHero() } }

  Box(modifier = modifier.fillMaxSize()) {
    HeroBackdrop(imageUrl = backdropUrl, modifier = Modifier.fillMaxSize())
    CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Page) {
      LazyColumn(
        state = focus.listState,
        modifier = Modifier.fillMaxSize().testTag(HomeTestTags.LIST),
        contentPadding = PaddingValues(bottom = OrbitSpacing.safeAreaVertical + OrbitSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(OrbitSpacing.sm),
      ) {
        if (heroVideo != null) {
          item(key = "hero") {
            HomeHero(
              featured = featured,
              selectedIndex = heroIndex,
              onSelectedIndexChange = { heroIndex = it },
              onPlayClick = onPlayClick,
              playModifier = Modifier.focusRequester(focus.hero),
              modifier =
                Modifier.onFocusChanged {
                  heroHasFocus = it.hasFocus
                  if (it.hasFocus) focus.lastFocusedKey = null
                },
            )
          }
        }
        items(items = content.rows, key = { it.key }) { row ->
          ContentRow(
            title = row.title(),
            items = row.items,
            key = { it.video.id },
            modifier = Modifier.testTag(HomeTestTags.row(row.key)),
          ) { item ->
            val key = cardKey(row, item)
            HomeCard(
              item = item,
              onClick = { onVideoClick(item.video) },
              onFocus = {
                focus.lastFocusedKey = key
                focusedCardImage = item.video.thumbnailUrl
              },
              modifier =
                with(focus) {
                  Modifier.testTag(HomeTestTags.card(key)).card(
                    key,
                    isFirstCard =
                      key == firstCardKey,
                  )
                },
            )
          }
        }
        content.feedError?.let { error ->
          item(key = "feed_error") { FeedErrorRow(error = error, onRetry = onRetry) }
        }
      }
    }
  }
}

@Composable
private fun HomeHero(
  featured: ImmutableList<Video>,
  selectedIndex: Int,
  onSelectedIndexChange: (Int) -> Unit,
  onPlayClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
  playModifier: Modifier = Modifier,
) {
  HeroCarousel(
    itemCount = featured.size,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = onSelectedIndexChange,
    modifier = modifier.fillMaxWidth(),
  ) { index ->
    val video = featured[index]
    HeroBanner(
      title = video.title,
      description = video.description,
      imageUrl = null,
      showBackdrop = false,
      overline = videoOverline(video.dateCreated, video.center),
      actionLabel = stringResource(R.string.home_play),
      onAction = { onPlayClick(video) },
      actionModifier = playModifier.testTag(HomeTestTags.HERO_PLAY),
      modifier = Modifier.testTag(HomeTestTags.HERO),
    )
  }
}

@Composable
private fun HomeCard(
  item: HomeItem,
  onClick: () -> Unit,
  onFocus: () -> Unit,
  modifier: Modifier = Modifier,
) {
  VideoCard(
    title = item.video.title,
    imageUrl = item.video.thumbnailUrl,
    progress = item.progress,
    onClick = onClick,
    onFocus = onFocus,
    modifier = modifier,
  )
}

/** Compact retry shown below saved rows when the catalog could not be loaded. */
@Composable
private fun FeedErrorRow(
  error: DataError,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().padding(
        horizontal = OrbitSpacing.safeAreaHorizontal,
        vertical = OrbitSpacing.lg,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.home_feed_error_title),
        style = OrbitTheme.typography.titleMedium,
        color = OrbitTheme.colors.onSurface,
      )
      Text(
        text = dataErrorMessage(error),
        style = OrbitTheme.typography.bodyMedium,
        color = OrbitTheme.colors.onSurfaceVariant,
      )
    }
    Spacer(Modifier.width(OrbitSpacing.xl))
    OrbitButton(
      text = stringResource(com.klt.orbit.core.designsystem.R.string.designsystem_retry),
      icon = OrbitIcons.Replay,
      onClick = onRetry,
      modifier = Modifier.testTag(HomeTestTags.FEED_RETRY),
    )
  }
}

@Composable
private fun HomeLoading(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize()) {
    HeroBackdrop(imageUrl = null, modifier = Modifier.fillMaxSize())
    Column(Modifier.fillMaxSize()) {
      Spacer(Modifier.height(HeroBannerDefaults.MinHeight - 48.dp))
      LoadingShimmer(rowCount = 2)
    }
  }
}

@Composable
private fun HomeRow.title(): String =
  when (val kind = kind) {
    HomeRowKind.ContinueWatching -> {
      stringResource(R.string.home_row_continue_watching)
    }

    HomeRowKind.Favorites -> {
      stringResource(R.string.home_row_favorites)
    }

    is HomeRowKind.Category -> {
      stringResource(
        when (kind.category) {
          VideoCategory.APOLLO -> R.string.home_row_apollo
          VideoCategory.MARS -> R.string.home_row_mars
          VideoCategory.HUBBLE -> R.string.home_row_hubble
          VideoCategory.ISS -> R.string.home_row_iss
          VideoCategory.EARTH -> R.string.home_row_earth
        },
      )
    }
  }

/** The same video can appear in several rows, so a card is identified by row and video. */
private fun cardKey(
  row: HomeRow,
  item: HomeItem,
) = "${row.key}/${item.video.id}"

internal object HomeTestTags {
  const val LIST = "home:list"
  const val HERO = "home:hero"
  const val HERO_PLAY = "home:hero_play"
  const val FEED_RETRY = "home:feed_retry"

  fun row(rowKey: String) = "home:row:$rowKey"

  fun card(cardKey: String) = "home:card:$cardKey"
}
