package com.klt.orbit.core.designsystem.focus

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.focus.FocusRequester
import kotlin.math.roundToInt

/**
 * Requests focus without crashing when the target is not composed (e.g. a card that scrolled out
 * of a lazy list). Returns whether focus was requested, so callers can fall back to another target.
 */
fun FocusRequester.tryRequestFocus(): Boolean =
  try {
    requestFocus()
  } catch (_: IllegalStateException) {
    false
  }

/**
 * Makes sure the item at [index] is fully on screen, scrolling only if it is not.
 *
 * Used when restoring focus after returning to a screen: if content above the target changed in
 * the meantime (e.g. a Favorites row was inserted), the list's restored offset can leave the
 * focused row below the fold, and focus-driven scrolling does not reliably correct it. Reading the
 * actual layout and scrolling directly is deterministic. The item is placed slightly below the top
 * edge ([leadingFraction] of the viewport) so a row's title stays visible.
 */
suspend fun LazyListState.revealItem(
  index: Int,
  leadingFraction: Float = 0.08f,
) {
  val info = layoutInfo
  val item = info.visibleItemsInfo.firstOrNull { it.index == index }
  val fullyVisible =
    item != null && item.offset >= info.viewportStartOffset && item.offset + item.size <= info.viewportEndOffset
  if (fullyVisible) return
  val viewport = info.viewportEndOffset - info.viewportStartOffset
  scrollToItem(index, scrollOffset = -(viewport * leadingFraction).roundToInt())
}
