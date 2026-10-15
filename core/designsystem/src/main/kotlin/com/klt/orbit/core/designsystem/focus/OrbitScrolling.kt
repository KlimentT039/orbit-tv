package com.klt.orbit.core.designsystem.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec

/**
 * How lists scroll to reveal the focused item on TV.
 *
 * Compose's default TV behavior pins every focused item at ~30% of the viewport. That moves the
 * page even when the item is already comfortably visible (pushing a hero header off screen on
 * launch) and parks row cards outside the overscan safe area. These specs scroll only when needed.
 * All margins are fractions of the container, so they match the 5% overscan safe area at any size.
 *
 * Uses the experimental `BringIntoViewSpec` API: it is the supported hook for this behavior and is
 * what tv-foundation itself uses.
 */
@OptIn(ExperimentalFoundationApi::class)
object OrbitScrolling {
  /**
   * For vertical page lists (hero + rows). Nothing moves while the focused item sits inside the
   * comfortable band; otherwise it is moved to the pivot so the next row stays in view below it.
   */
  val Page: BringIntoViewSpec =
    object : BringIntoViewSpec {
      override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
      ): Float = pageScrollDistance(offset, size, containerSize)
    }

  /**
   * For horizontal rows. Scrolls the minimum needed to keep the focused card inside the safe area,
   * so the first card stays aligned with the row title and neighbours peek in from the edges.
   */
  val Row: BringIntoViewSpec =
    object : BringIntoViewSpec {
      override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
      ): Float = rowScrollDistance(offset, size, containerSize)
    }
}

private const val SAFE_AREA_FRACTION = 0.05f
private const val PAGE_COMFORT_END_FRACTION = 0.9f
private const val PAGE_PIVOT_FRACTION = 0.3f

internal fun pageScrollDistance(
  offset: Float,
  size: Float,
  containerSize: Float,
): Float {
  val safeStart = containerSize * SAFE_AREA_FRACTION
  val trailing = offset + size
  return when {
    offset >= safeStart && trailing <= containerSize * PAGE_COMFORT_END_FRACTION -> 0f

    // Too tall to fit: show its start.
    size > containerSize * (PAGE_COMFORT_END_FRACTION - SAFE_AREA_FRACTION) -> offset - safeStart

    else -> offset - containerSize * PAGE_PIVOT_FRACTION
  }
}

internal fun rowScrollDistance(
  offset: Float,
  size: Float,
  containerSize: Float,
): Float {
  val safeStart = containerSize * SAFE_AREA_FRACTION
  val safeEnd = containerSize * (1 - SAFE_AREA_FRACTION)
  val trailing = offset + size
  return when {
    offset < safeStart -> offset - safeStart
    trailing > safeEnd -> trailing - safeEnd
    else -> 0f
  }
}
