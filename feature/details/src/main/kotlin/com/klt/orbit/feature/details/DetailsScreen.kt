package com.klt.orbit.feature.details

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.time.formatPlaybackTime
import com.klt.orbit.core.designsystem.component.ContentRow
import com.klt.orbit.core.designsystem.component.HeroBackdrop
import com.klt.orbit.core.designsystem.component.KeywordChip
import com.klt.orbit.core.designsystem.component.LoadingShimmer
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.component.VideoCard
import com.klt.orbit.core.designsystem.error.DataErrorState
import com.klt.orbit.core.designsystem.error.dataErrorIcon
import com.klt.orbit.core.designsystem.error.dataErrorMessage
import com.klt.orbit.core.designsystem.focus.OrbitScrolling
import com.klt.orbit.core.designsystem.focus.revealItem
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.designsystem.util.ObserveAsEvents
import com.klt.orbit.core.designsystem.util.videoOverline
import com.klt.orbit.core.model.Video
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun DetailsRoute(
  onPlay: (videoId: String, startPositionMs: Long) -> Unit,
  onOpenDetails: (videoId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DetailsViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is DetailsEvent.Play -> {
        onPlay(event.videoId, event.startPositionMs)
      }

      is DetailsEvent.OpenDetails -> {
        onOpenDetails(event.videoId)
      }

      is DetailsEvent.ShowError -> {
        Toast
          .makeText(
            context,
            dataErrorMessage(context, event.error),
            Toast.LENGTH_SHORT,
          ).show()
      }
    }
  }
  DetailsScreen(
    uiState = uiState,
    onPlayClick = viewModel::onPlayClick,
    onResumeClick = viewModel::onResumeClick,
    onToggleFavoriteClick = viewModel::onToggleFavoriteClick,
    onRelatedClick = viewModel::onRelatedClick,
    onRetry = viewModel::onRetry,
    modifier = modifier,
  )
}

@Composable
internal fun DetailsScreen(
  uiState: DetailsUiState,
  onPlayClick: () -> Unit,
  onResumeClick: () -> Unit,
  onToggleFavoriteClick: () -> Unit,
  onRelatedClick: (Video) -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    HeroBackdrop(
      imageUrl = (uiState as? DetailsUiState.Content)?.backdropUrl,
      modifier = Modifier.fillMaxSize(),
    )
    when (uiState) {
      DetailsUiState.Loading -> {
        DetailsLoading()
      }

      is DetailsUiState.Error -> {
        DataErrorState(error = uiState.error, onRetry = onRetry, modifier = Modifier.fillMaxSize())
      }

      is DetailsUiState.Content -> {
        DetailsContent(
          content = uiState,
          onPlayClick = onPlayClick,
          onResumeClick = onResumeClick,
          onToggleFavoriteClick = onToggleFavoriteClick,
          onRelatedClick = onRelatedClick,
        )
      }
    }
  }
}

/**
 * Focus model: lands on Resume when there is progress, otherwise Play (or Favorite when the video
 * can't be played). The last focused action or related card is saved, so returning from a related
 * video's Details (or the player) puts focus back where the user left it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailsContent(
  content: DetailsUiState.Content,
  onPlayClick: () -> Unit,
  onResumeClick: () -> Unit,
  onToggleFavoriteClick: () -> Unit,
  onRelatedClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focus = rememberDetailsFocus()
  val listState = rememberLazyListState()
  val canPlay = content.playback.canPlay

  LaunchedEffect(Unit) {
    withFrameNanos { }
    val restored = focus.lastFocusedKey != null && focus.restore.tryRequestFocus()
    if (restored && focus.lastFocusedKey?.startsWith(DetailsFocus.RELATED_PREFIX) == true) {
      withFrameNanos { }
      listState.revealItem(index = RELATED_ITEM_INDEX)
    }
    if (!restored && !(canPlay && focus.primary.tryRequestFocus())) focus.favorite.tryRequestFocus()
  }
  // If Play disappears while focused (the video turned out to be unplayable), keep focus on screen.
  LaunchedEffect(canPlay) {
    val playFocused = focus.lastFocusedKey == DetailsFocus.PLAY || focus.lastFocusedKey == DetailsFocus.RESUME
    if (!canPlay && playFocused) focus.favorite.tryRequestFocus()
  }

  CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Page) {
    LazyColumn(
      state = listState,
      modifier = modifier.fillMaxSize(),
      contentPadding = PaddingValues(top = OrbitSpacing.safeAreaVertical + OrbitSpacing.xxl, bottom = OrbitSpacing.xxl),
      verticalArrangement = Arrangement.spacedBy(OrbitSpacing.xl),
    ) {
      item(key = "info") {
        Column(Modifier.fillMaxWidth(INFO_WIDTH_FRACTION).padding(start = OrbitSpacing.safeAreaHorizontal)) {
          DetailsInfo(video = content.video)
          Spacer(Modifier.height(OrbitSpacing.xl))
          DetailsActions(
            content = content,
            focus = focus,
            onPlayClick = onPlayClick,
            onResumeClick = onResumeClick,
            onToggleFavoriteClick = onToggleFavoriteClick,
          )
          (content.playback as? Playback.Unavailable)?.let { unavailable ->
            Spacer(Modifier.height(OrbitSpacing.md))
            PlaybackUnavailableNote(error = unavailable.error)
          }
        }
      }
      when (val related = content.related) {
        Related.Loading -> {
          item(key = "related_loading") { LoadingShimmer(rowCount = 1) }
        }

        is Related.Loaded -> {
          item(key = "related") { RelatedRow(videos = related.videos, focus = focus, onRelatedClick = onRelatedClick) }
        }

        Related.None -> {}
      }
    }
  }
}

@Composable
private fun DetailsInfo(
  video: Video,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    videoOverline(video.dateCreated, video.center)?.let { overline ->
      Text(text = overline.uppercase(), style = OrbitTheme.typography.labelLarge, color = OrbitTheme.colors.primary)
      Spacer(Modifier.height(OrbitSpacing.sm))
    }
    Text(
      text = video.title,
      style = OrbitTheme.typography.displaySmall,
      color = OrbitTheme.colors.onSurface,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag(DetailsTestTags.TITLE),
    )
    if (video.keywords.isNotEmpty()) {
      Spacer(Modifier.height(OrbitSpacing.md))
      Row(horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.sm)) {
        video.keywords.take(MAX_CHIPS).forEach { KeywordChip(text = it) }
      }
    }
    if (video.description.isNotBlank()) {
      Spacer(Modifier.height(OrbitSpacing.lg))
      Text(
        text = video.description,
        style = OrbitTheme.typography.bodyLarge,
        color = OrbitTheme.colors.onSurfaceVariant,
        maxLines = MAX_DESCRIPTION_LINES,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** Resume (when there is progress), Play, and the favorite toggle. Play is hidden if unplayable. */
@Composable
private fun DetailsActions(
  content: DetailsUiState.Content,
  focus: DetailsFocus,
  onPlayClick: () -> Unit,
  onResumeClick: () -> Unit,
  onToggleFavoriteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val resumePosition = content.resumePositionMs
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.lg)) {
    with(focus) {
      if (content.playback.canPlay) {
        if (resumePosition != null) {
          OrbitButton(
            text = stringResource(R.string.details_resume, formatPlaybackTime(resumePosition)),
            icon = OrbitIcons.Play,
            onClick = onResumeClick,
            modifier = Modifier.testTag(DetailsTestTags.RESUME).focusRequester(primary).tracked(DetailsFocus.RESUME),
          )
        }
        OrbitButton(
          text =
            stringResource(
              if (resumePosition !=
                null
              ) {
                R.string.details_play_from_start
              } else {
                R.string.details_play
              },
            ),
          icon = if (resumePosition != null) OrbitIcons.Replay else OrbitIcons.Play,
          onClick = onPlayClick,
          modifier =
            Modifier
              .testTag(DetailsTestTags.PLAY)
              .then(if (resumePosition == null) Modifier.focusRequester(primary) else Modifier)
              .tracked(DetailsFocus.PLAY),
        )
      }
      OrbitButton(
        text =
          stringResource(
            if (content.isFavorite) R.string.details_remove_favorite else R.string.details_add_favorite,
          ),
        icon = if (content.isFavorite) OrbitIcons.Favorite else OrbitIcons.FavoriteBorder,
        onClick = onToggleFavoriteClick,
        modifier = Modifier.testTag(DetailsTestTags.FAVORITE).focusRequester(favorite).tracked(DetailsFocus.FAVORITE),
      )
    }
  }
}

@Composable
private fun PlaybackUnavailableNote(
  error: DataError,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.sm)) {
    Icon(imageVector = dataErrorIcon(error), contentDescription = null, tint = OrbitTheme.colors.error)
    Text(
      text = dataErrorMessage(error),
      style = OrbitTheme.typography.bodyMedium,
      color = OrbitTheme.colors.onSurfaceVariant,
      modifier = Modifier.testTag(DetailsTestTags.UNAVAILABLE),
    )
  }
}

@Composable
private fun RelatedRow(
  videos: ImmutableList<Video>,
  focus: DetailsFocus,
  onRelatedClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  ContentRow(
    title = stringResource(R.string.details_related),
    items = videos,
    key = { it.id },
    modifier = modifier.testTag(DetailsTestTags.RELATED),
  ) { video ->
    VideoCard(
      title = video.title,
      imageUrl = video.thumbnailUrl,
      onClick = { onRelatedClick(video) },
      modifier = with(focus) { Modifier.tracked("${DetailsFocus.RELATED_PREFIX}${video.id}") },
    )
  }
}

@Composable
private fun DetailsLoading(modifier: Modifier = Modifier) {
  Column(modifier.fillMaxSize().padding(top = OrbitSpacing.safeAreaVertical + OrbitSpacing.xxl)) {
    LoadingShimmer(rowCount = 2)
  }
}

private const val INFO_WIDTH_FRACTION = 0.62f
private const val MAX_CHIPS = 6
private const val MAX_DESCRIPTION_LINES = 5

/** The related row follows the info block. */
private const val RELATED_ITEM_INDEX = 1

internal object DetailsTestTags {
  const val TITLE = "details:title"
  const val PLAY = "details:play"
  const val RESUME = "details:resume"
  const val FAVORITE = "details:favorite"
  const val UNAVAILABLE = "details:unavailable"
  const val RELATED = "details:related"
}
