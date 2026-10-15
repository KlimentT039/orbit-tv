package com.klt.orbit.domain.testing

import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.ContinueWatchingItem
import com.klt.orbit.core.model.PlaybackProgress
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [PlaybackProgressRepository] with a controllable clock. */
class FakePlaybackProgressRepository(
  private val now: () -> Instant = { Instant.parse("2026-01-01T00:00:00Z") },
) : PlaybackProgressRepository {
  private val history = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
  var failWith: DataError? = null

  /** Seeds history directly, bypassing [saveProgress]. */
  fun setHistory(items: List<ContinueWatchingItem>) {
    history.value = items
  }

  override fun observeProgress(videoId: String): Flow<PlaybackProgress?> =
    history.map { items -> items.firstOrNull { it.video.id == videoId }?.progress }

  override fun observeWatchHistory(): Flow<List<ContinueWatchingItem>> = history

  override suspend fun saveProgress(
    video: Video,
    positionMs: Long,
    durationMs: Long,
  ): Outcome<Unit> =
    write {
      val item = ContinueWatchingItem(video, PlaybackProgress(video.id, positionMs, durationMs, now()))
      history.update { items -> listOf(item) + items.filterNot { it.video.id == video.id } }
    }

  override suspend fun clearProgress(videoId: String): Outcome<Unit> =
    write { history.update { items -> items.filterNot { it.video.id == videoId } } }

  override suspend fun clearAll(): Outcome<Unit> = write { history.value = emptyList() }

  private inline fun write(block: () -> Unit): Outcome<Unit> {
    failWith?.let { return Outcome.Failure(it) }
    block()
    return Outcome.Success(Unit)
  }
}
