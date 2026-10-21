package com.klt.orbit.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.klt.orbit.core.common.dispatcher.ApplicationScope
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.common.result.getOrNull
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.model.PlaybackItem
import com.klt.orbit.domain.usecase.GetRelatedVideosUseCase
import com.klt.orbit.domain.usecase.ObserveSettingsUseCase
import com.klt.orbit.domain.usecase.PreparePlaybackUseCase
import com.klt.orbit.domain.usecase.SavePlaybackProgressUseCase
import com.klt.orbit.feature.player.engine.PlaybackFailure
import com.klt.orbit.feature.player.engine.PlaybackPhase
import com.klt.orbit.feature.player.engine.PlaybackSnapshot
import com.klt.orbit.feature.player.engine.VideoPlayerFactory
import com.klt.orbit.feature.player.navigation.PlayerDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Drives one playback session: loads the video, owns the player, saves progress (every few
 * seconds, on pause and on exit), falls back to another rendition when a stream can't be decoded,
 * auto-hides the controls and offers the next video when autoplay is on.
 *
 * Unlike the other screens, state is held in a [MutableStateFlow] instead of derived with
 * `stateIn`: it changes imperatively (player callbacks, timers) and the session must keep running
 * while the screen is briefly not collecting.
 */
@HiltViewModel
internal class PlayerViewModel
  @Inject
  constructor(
    savedStateHandle: SavedStateHandle,
    playerFactory: VideoPlayerFactory,
    private val preparePlayback: PreparePlaybackUseCase,
    private val getRelatedVideos: GetRelatedVideosUseCase,
    private val observeSettings: ObserveSettingsUseCase,
    private val savePlaybackProgress: SavePlaybackProgressUseCase,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
  ) : ViewModel() {
    private val videoId: String = checkNotNull(savedStateHandle[PlayerDestination.VIDEO_ID_ARG])
    private val requestedStartMs: Long? =
      savedStateHandle
        .get<Long>(PlayerDestination.START_POSITION_ARG)
        ?.takeIf { it != PlayerDestination.RESUME_SAVED_POSITION }

    private val videoPlayer = playerFactory.create()

    /** The Media3 player to render; null in tests. */
    val player: Player? get() = videoPlayer.media3Player

    private val state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = state.asStateFlow()

    private val eventChannel = Channel<PlayerEvent>(Channel.BUFFERED)
    val events: Flow<PlayerEvent> = eventChannel.receiveAsFlow()

    private var session: Session? = null
    private var loadJob: Job? = null
    private var hideControlsJob: Job? = null
    private var upNextJob: Job? = null

    /** Saves run one at a time in the order they were requested, so an older position never lands last. */
    private val saveMutex = Mutex()

    init {
      observePlayback()
      load()
    }

    fun onAction(action: PlayerAction) {
      when (action) {
        PlayerAction.TogglePlayPause -> if (content()?.isPlaying == true) pause() else play()
        PlayerAction.Play -> play()
        PlayerAction.Pause -> pause()
        PlayerAction.SeekForward -> seekBy(SEEK_STEP_MS)
        PlayerAction.SeekBack -> seekBy(-SEEK_STEP_MS)
        PlayerAction.ShowControls -> showControls()
        PlayerAction.HideControls -> updateContent { it.copy(controlsVisible = false) }
        PlayerAction.Retry -> retry()
        PlayerAction.Replay -> replay()
        PlayerAction.PlayNextNow -> playNextNow()
        PlayerAction.CancelUpNext -> cancelUpNext()
      }
    }

    override fun onCleared() {
      // viewModelScope is already cancelled; the final save must outlive the screen.
      saveProgress(applicationScope)
      videoPlayer.release()
    }

    private fun load() {
      if (loadJob?.isActive == true) return
      state.value = PlayerUiState.Loading
      loadJob =
        viewModelScope.launch {
          when (val result = preparePlayback(videoId, requestedStartMs)) {
            is Outcome.Success -> start(result.value)
            is Outcome.Failure -> state.value = PlayerUiState.Error(result.error)
          }
        }
    }

    private fun start(item: PlaybackItem) {
      session = Session(item.video, item.assets.streams.map { it.url })
      state.value = PlayerUiState.Content(title = item.video.title, positionMs = item.startPositionMs)
      videoPlayer.load(item.assets.bestStream.url, item.startPositionMs)
      showControls()
      // Picked now so the end of the video doesn't wait on the network.
      viewModelScope.launch {
        val next = getRelatedVideos(item.video).getOrNull()?.firstOrNull()
        session = session?.copy(upNext = next)
      }
    }

    private fun observePlayback() {
      viewModelScope.launch {
        var previous = videoPlayer.state.value
        videoPlayer.state.collect { snapshot ->
          onSnapshot(previous, snapshot)
          previous = snapshot
        }
      }
      // ExoPlayer doesn't report position changes, so poll while playing; the same clock saves progress.
      viewModelScope.launch {
        videoPlayer.state.map { it.isPlaying }.distinctUntilChanged().collectLatest { playing ->
          var sinceSaveMs = 0L
          while (playing) {
            delay(TICK_MS)
            updateContent { it.withPositions() }
            sinceSaveMs += TICK_MS
            if (sinceSaveMs >= SAVE_INTERVAL_MS) {
              sinceSaveMs = 0
              saveProgress()
            }
          }
        }
      }
    }

    private fun onSnapshot(
      previous: PlaybackSnapshot,
      snapshot: PlaybackSnapshot,
    ) {
      val current = session ?: return
      updateContent {
        it
          .copy(
            durationMs = snapshot.durationMs,
            isPlaying = snapshot.playWhenReady && snapshot.phase != PlaybackPhase.Ended,
            isBuffering =
              snapshot.failure == null &&
                (snapshot.phase == PlaybackPhase.Buffering || snapshot.phase == PlaybackPhase.Idle),
          ).withPositions()
      }
      val failure = snapshot.failure
      when {
        failure != null && previous.failure == null -> onPlaybackFailed(current, failure)
        snapshot.phase == PlaybackPhase.Ended && previous.phase != PlaybackPhase.Ended -> onFinished(current)
        previous.isPlaying && !snapshot.isPlaying -> saveProgress()
      }
      if (snapshot.isPlaying && !previous.isPlaying) scheduleHideControls()
    }

    private fun onPlaybackFailed(
      current: Session,
      failure: PlaybackFailure,
    ) {
      val nextStream = current.streamIndex + 1
      if (failure == PlaybackFailure.Unsupported && nextStream < current.streamUrls.size) {
        session = current.copy(streamIndex = nextStream)
        videoPlayer.load(current.streamUrls[nextStream], videoPlayer.positionMs)
        return
      }
      saveProgress()
      hideControlsJob?.cancel()
      updateContent { it.copy(panel = PlayerPanel.Failed(failure)) }
    }

    private fun onFinished(current: Session) {
      saveProgress()
      hideControlsJob?.cancel()
      val next = current.upNext
      viewModelScope.launch {
        if (next != null && observeSettings().first().autoplayNext) {
          startUpNext(next)
        } else {
          updateContent { it.copy(panel = PlayerPanel.Finished) }
        }
      }
    }

    private fun startUpNext(next: Video) {
      upNextJob?.cancel()
      upNextJob =
        viewModelScope.launch {
          for (secondsLeft in UP_NEXT_COUNTDOWN_SECONDS downTo 1) {
            updateContent { it.copy(panel = PlayerPanel.UpNext(next, secondsLeft)) }
            delay(ONE_SECOND_MS)
          }
          eventChannel.send(PlayerEvent.PlayNext(next.id))
        }
    }

    private fun playNextNow() {
      val upNext = content()?.panel as? PlayerPanel.UpNext ?: return
      upNextJob?.cancel()
      eventChannel.trySend(PlayerEvent.PlayNext(upNext.video.id))
    }

    private fun cancelUpNext() {
      if (content()?.panel !is PlayerPanel.UpNext) return
      upNextJob?.cancel()
      updateContent { it.copy(panel = PlayerPanel.Finished) }
    }

    private fun play() {
      if (!canControlPlayback()) return
      videoPlayer.play()
      showControls()
    }

    private fun pause() {
      if (!canControlPlayback()) return
      videoPlayer.pause()
      // Saved explicitly: pausing while buffering (e.g. right after a seek) is no playing-to-paused transition.
      saveProgress()
      showControls()
    }

    private fun seekBy(deltaMs: Long) {
      val duration = videoPlayer.state.value.durationMs
      if (!canControlPlayback() || duration <= 0) return
      val target = (videoPlayer.positionMs + deltaMs).coerceIn(0, duration)
      videoPlayer.seekTo(target)
      updateContent { it.withPositions() }
      showControls()
    }

    private fun retry() {
      when (val panel = content()?.panel) {
        null -> {
          if (state.value is PlayerUiState.Error) load()
        }

        is PlayerPanel.Failed -> {
          val current = session ?: return
          updateContent { it.copy(panel = null) }
          if (panel.failure == PlaybackFailure.Unsupported) {
            // Every rendition failed; start over from the best one in case the cause was transient.
            session = current.copy(streamIndex = 0)
            videoPlayer.load(current.streamUrls.first(), videoPlayer.positionMs)
          } else {
            videoPlayer.retry()
          }
          showControls()
        }

        PlayerPanel.Finished, is PlayerPanel.UpNext -> {}
      }
    }

    private fun replay() {
      upNextJob?.cancel()
      updateContent { it.copy(panel = null, positionMs = 0) }
      videoPlayer.seekTo(0)
      videoPlayer.play()
      showControls()
    }

    private fun showControls() {
      updateContent { it.copy(controlsVisible = true) }
      scheduleHideControls()
    }

    /** Controls fade out after a while, but only during playback: paused, they stay up. */
    private fun scheduleHideControls() {
      hideControlsJob?.cancel()
      hideControlsJob =
        viewModelScope.launch {
          delay(CONTROLS_TIMEOUT_MS)
          updateContent { if (it.isPlaying && it.panel == null) it.copy(controlsVisible = false) else it }
        }
    }

    /** Play, pause and seek only apply while the transport controls are on screen. */
    private fun canControlPlayback(): Boolean = content()?.panel == null && session != null

    /** Saves the current position; a finished video clears its progress instead (see the use case). */
    private fun saveProgress(scope: CoroutineScope = viewModelScope) {
      val current = session ?: return
      val snapshot = videoPlayer.state.value
      val duration = snapshot.durationMs
      if (duration <= 0) return
      val position = if (snapshot.phase == PlaybackPhase.Ended) duration else videoPlayer.positionMs
      // A failed save only costs the resume point; it's not worth interrupting playback for.
      scope.launch { saveMutex.withLock { savePlaybackProgress(current.video, position, duration) } }
    }

    private fun PlayerUiState.Content.withPositions() =
      copy(positionMs = videoPlayer.positionMs, bufferedPositionMs = videoPlayer.bufferedPositionMs)

    private fun content(): PlayerUiState.Content? = state.value as? PlayerUiState.Content

    private inline fun updateContent(transform: (PlayerUiState.Content) -> PlayerUiState.Content) {
      state.update { current -> (current as? PlayerUiState.Content)?.let(transform) ?: current }
    }

    /**
     * @property streamUrls playable renditions, best first; [streamIndex] is the one playing.
     * @property upNext what autoplay would start next, once known.
     */
    private data class Session(
      val video: Video,
      val streamUrls: List<String>,
      val streamIndex: Int = 0,
      val upNext: Video? = null,
    )

    companion object {
      const val SEEK_STEP_MS = 10_000L
      const val SAVE_INTERVAL_MS = 5_000L
      const val CONTROLS_TIMEOUT_MS = 5_000L
      const val UP_NEXT_COUNTDOWN_SECONDS = 10
      private const val TICK_MS = 500L
      private const val ONE_SECOND_MS = 1_000L
    }
  }
