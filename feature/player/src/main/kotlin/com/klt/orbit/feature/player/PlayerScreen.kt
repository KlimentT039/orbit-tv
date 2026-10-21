package com.klt.orbit.feature.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import androidx.tv.material3.Text
import com.klt.orbit.core.common.time.formatPlaybackTime
import com.klt.orbit.core.designsystem.component.BufferingIndicator
import com.klt.orbit.core.designsystem.component.ErrorState
import com.klt.orbit.core.designsystem.component.OrbitButton
import com.klt.orbit.core.designsystem.component.OrbitIconButton
import com.klt.orbit.core.designsystem.component.PlaybackProgressBar
import com.klt.orbit.core.designsystem.error.DataErrorState
import com.klt.orbit.core.designsystem.focus.tryRequestFocus
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitPalette
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.core.designsystem.util.ObserveAsEvents
import com.klt.orbit.feature.player.engine.PlaybackFailure

@Composable
internal fun PlayerRoute(
  onPlayNext: (videoId: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PlayerViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ObserveAsEvents(viewModel.events) { event ->
    when (event) {
      is PlayerEvent.PlayNext -> onPlayNext(event.videoId)
    }
  }
  // Leaving the app (Home button, another app) pauses; the pause also saves progress.
  LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAction(PlayerAction.Pause) }
  PlayerScreen(
    uiState = uiState,
    onAction = viewModel::onAction,
    modifier = modifier,
    videoSurface = { VideoSurface(player = viewModel.player) },
  )
}

/**
 * Focus model: while the transport controls show, play/pause has focus; when they hide, the screen
 * itself holds focus so the remote keeps working. Panels (error, finished, up next) focus their
 * primary action. Back first hides the controls during playback, then leaves the player.
 */
@Composable
internal fun PlayerScreen(
  uiState: PlayerUiState,
  onAction: (PlayerAction) -> Unit,
  modifier: Modifier = Modifier,
  videoSurface: @Composable () -> Unit = {},
) {
  val rootFocus = remember { FocusRequester() }
  val content = uiState as? PlayerUiState.Content
  val transportActive = content != null && content.panel == null
  val controlsVisible = transportActive && content.controlsVisible
  BackHandler(enabled = controlsVisible && content.isPlaying) { onAction(PlayerAction.HideControls) }
  KeepScreenOn(enabled = content?.isPlaying == true)
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Color.Black)
        .onPreviewKeyEvent { event ->
          val action = if (transportActive) playerKeyAction(event, controlsVisible) else null
          action?.let(onAction)
          action != null
        }.focusRequester(rootFocus)
        .focusable()
        .testTag(PlayerTestTags.ROOT),
  ) {
    videoSurface()
    when (uiState) {
      PlayerUiState.Loading -> {
        BufferingIndicator(modifier = Modifier.align(Alignment.Center))
      }

      is PlayerUiState.Error -> {
        DataErrorState(
          error = uiState.error,
          onRetry = { onAction(PlayerAction.Retry) },
          modifier = Modifier.fillMaxSize(),
        )
      }

      is PlayerUiState.Content -> {
        PlayerContent(content = uiState, onAction = onAction)
      }
    }
  }
  val rootHoldsFocus = uiState is PlayerUiState.Loading || (transportActive && !controlsVisible)
  LaunchedEffect(rootHoldsFocus) {
    if (rootHoldsFocus) rootFocus.tryRequestFocus()
  }
}

@Composable
private fun PlayerContent(
  content: PlayerUiState.Content,
  onAction: (PlayerAction) -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    if (content.isBuffering && content.panel == null) {
      BufferingIndicator(modifier = Modifier.align(Alignment.Center))
    }
    AnimatedVisibility(
      visible = content.controlsVisible && content.panel == null,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize(),
    ) {
      PlayerControls(content = content, onTogglePlayPause = { onAction(PlayerAction.TogglePlayPause) })
    }
    when (val panel = content.panel) {
      null -> {}

      is PlayerPanel.Failed -> {
        ErrorState(
          title = stringResource(R.string.player_error_title),
          message = stringResource(panel.failure.messageRes()),
          icon = if (panel.failure == PlaybackFailure.Network) OrbitIcons.CloudOff else OrbitIcons.Error,
          onRetry = { onAction(PlayerAction.Retry) },
          modifier = Modifier.fillMaxSize().background(PlayerScrim),
        )
      }

      PlayerPanel.Finished -> {
        FinishedPanel(title = content.title, onReplay = { onAction(PlayerAction.Replay) })
      }

      is PlayerPanel.UpNext -> {
        UpNextPanel(
          panel = panel,
          onPlayNow = { onAction(PlayerAction.PlayNextNow) },
          onCancel = { onAction(PlayerAction.CancelUpNext) },
        )
      }
    }
  }
}

@Composable
private fun PlayerControls(
  content: PlayerUiState.Content,
  onTogglePlayPause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playPauseFocus = remember { FocusRequester() }
  Box(modifier = modifier.fillMaxSize()) {
    Text(
      text = content.title,
      style = OrbitTheme.typography.headlineSmall,
      color = OrbitTheme.colors.onSurface,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .fillMaxWidth()
          .background(Brush.verticalGradient(listOf(PlayerScrim, Color.Transparent)))
          .padding(OrbitSpacing.SafeArea)
          .padding(bottom = OrbitSpacing.xxl)
          .testTag(PlayerTestTags.TITLE),
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .background(ControlsScrimBottom)
          .padding(OrbitSpacing.SafeArea)
          .padding(top = OrbitSpacing.xxxl),
    ) {
      OrbitIconButton(
        icon = if (content.isPlaying) OrbitIcons.Pause else OrbitIcons.Play,
        contentDescription = stringResource(if (content.isPlaying) R.string.player_pause else R.string.player_play),
        onClick = onTogglePlayPause,
        modifier = Modifier.focusRequester(playPauseFocus).testTag(PlayerTestTags.PLAY_PAUSE),
      )
      Spacer(Modifier.width(OrbitSpacing.xl))
      Column(modifier = Modifier.weight(1f)) {
        PlaybackProgressBar(
          progress = content.progress,
          bufferedProgress = content.bufferedProgress,
          modifier = Modifier.testTag(PlayerTestTags.PROGRESS),
        )
        Spacer(Modifier.height(OrbitSpacing.sm))
        Row {
          Text(
            text = formatPlaybackTime(content.positionMs),
            style = OrbitTheme.typography.titleMedium,
            color = OrbitTheme.colors.onSurface,
            modifier = Modifier.testTag(PlayerTestTags.ELAPSED),
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (content.durationMs > 0) formatPlaybackTime(content.durationMs) else "",
            style = OrbitTheme.typography.titleMedium,
            color = OrbitTheme.colors.onSurfaceVariant,
            modifier = Modifier.testTag(PlayerTestTags.DURATION),
          )
        }
      }
    }
  }
  LaunchedEffect(playPauseFocus) { playPauseFocus.tryRequestFocus() }
}

@Composable
private fun FinishedPanel(
  title: String,
  onReplay: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val replayFocus = remember { FocusRequester() }
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier =
      modifier
        .fillMaxSize()
        .background(PlayerScrim)
        .padding(OrbitSpacing.SafeArea),
  ) {
    Text(
      text = stringResource(R.string.player_finished),
      style = OrbitTheme.typography.headlineSmall,
      color = OrbitTheme.colors.onSurface,
    )
    Spacer(Modifier.height(OrbitSpacing.sm))
    Text(
      text = title,
      style = OrbitTheme.typography.bodyLarge,
      color = OrbitTheme.colors.onSurfaceVariant,
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.widthIn(max = 560.dp),
    )
    Spacer(Modifier.height(OrbitSpacing.xl))
    OrbitButton(
      text = stringResource(R.string.player_replay),
      icon = OrbitIcons.Replay,
      onClick = onReplay,
      modifier = Modifier.focusRequester(replayFocus).testTag(PlayerTestTags.REPLAY),
    )
  }
  LaunchedEffect(replayFocus) { replayFocus.tryRequestFocus() }
}

@Composable
private fun UpNextPanel(
  panel: PlayerPanel.UpNext,
  onPlayNow: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playNowFocus = remember { FocusRequester() }
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier =
      modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(Color.Transparent, PlayerScrim)))
        .padding(OrbitSpacing.SafeArea),
  ) {
    Column(modifier = Modifier.widthIn(max = 420.dp)) {
      Text(
        text = stringResource(R.string.player_up_next),
        style = OrbitTheme.typography.labelLarge,
        color = OrbitTheme.colors.primary,
      )
      Spacer(Modifier.height(OrbitSpacing.xs))
      Text(
        text = panel.video.title,
        style = OrbitTheme.typography.titleLarge,
        color = OrbitTheme.colors.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.height(OrbitSpacing.xs))
      Text(
        text = pluralStringResource(R.plurals.player_up_next_countdown, panel.secondsLeft, panel.secondsLeft),
        style = OrbitTheme.typography.bodyMedium,
        color = OrbitTheme.colors.onSurfaceVariant,
      )
      Spacer(Modifier.height(OrbitSpacing.lg))
      Row {
        OrbitButton(
          text = stringResource(R.string.player_play_now),
          icon = OrbitIcons.Play,
          onClick = onPlayNow,
          modifier = Modifier.focusRequester(playNowFocus).testTag(PlayerTestTags.PLAY_NOW),
        )
        Spacer(Modifier.width(OrbitSpacing.lg))
        OrbitButton(
          text = stringResource(R.string.player_cancel),
          onClick = onCancel,
          modifier = Modifier.testTag(PlayerTestTags.CANCEL_UP_NEXT),
        )
      }
    }
  }
  LaunchedEffect(playNowFocus) { playNowFocus.tryRequestFocus() }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoSurface(
  player: Player?,
  modifier: Modifier = Modifier,
) {
  // Keeps the video's aspect ratio and shows black until the first frame renders.
  ContentFrame(player = player, modifier = modifier.fillMaxSize())
}

/** Keeps the TV from dimming or starting the screensaver while a video plays. */
@Composable
private fun KeepScreenOn(enabled: Boolean) {
  val view = LocalView.current
  DisposableEffect(view, enabled) {
    view.keepScreenOn = enabled
    onDispose { view.keepScreenOn = false }
  }
}

@StringRes
private fun PlaybackFailure.messageRes(): Int =
  when (this) {
    PlaybackFailure.Network -> R.string.player_error_network
    PlaybackFailure.Unsupported -> R.string.player_error_unsupported
    PlaybackFailure.Unknown -> R.string.player_error_unknown
  }

private val PlayerScrim = OrbitPalette.SpaceBlack.copy(alpha = 0.8f)

/** Darkens quickly so the time labels stay readable over bright video and burned-in captions. */
private val ControlsScrimBottom =
  Brush.verticalGradient(
    0f to Color.Transparent,
    0.35f to OrbitPalette.SpaceBlack.copy(alpha = 0.75f),
    1f to OrbitPalette.SpaceBlack.copy(alpha = 0.92f),
  )

internal object PlayerTestTags {
  const val ROOT = "player_root"
  const val TITLE = "player_title"
  const val PLAY_PAUSE = "player_play_pause"
  const val PROGRESS = "player_progress"
  const val ELAPSED = "player_elapsed"
  const val DURATION = "player_duration"
  const val REPLAY = "player_replay"
  const val PLAY_NOW = "player_play_now"
  const val CANCEL_UP_NEXT = "player_cancel_up_next"
}

@PreviewTv
@Composable
private fun PlayerControlsPreview() {
  OrbitTheme {
    PlayerScreen(
      uiState =
        PlayerUiState.Content(
          title = "Apollo 11: One Giant Leap",
          positionMs = 754_000,
          durationMs = 1_820_000,
          bufferedPositionMs = 900_000,
          isBuffering = false,
        ),
      onAction = {},
    )
  }
}
