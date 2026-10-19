package com.klt.orbit.feature.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.klt.orbit.core.designsystem.focus.revealItem
import com.klt.orbit.core.designsystem.focus.tryRequestFocus

/**
 * Focus bookkeeping for Home.
 *
 * [lastFocusedKey] (saved across navigation) identifies the last focused card as `rowKey/videoId`,
 * or is null while the hero has focus. It drives restoration when the user comes back from Details.
 */
@Stable
internal class HomeFocus(
  lastFocusedKey: MutableState<String?>,
  val listState: LazyListState,
) {
  var lastFocusedKey by lastFocusedKey
  val hero = FocusRequester()
  private val restore = FocusRequester()
  private val firstCard = FocusRequester()
  private var initialFocusHandled = false

  /** Attaches the requesters a card may need: restore target and/or first-card fallback. */
  fun Modifier.card(
    key: String,
    isFirstCard: Boolean,
  ): Modifier =
    then(if (key == lastFocusedKey) Modifier.focusRequester(restore) else Modifier)
      .then(if (isFirstCard) Modifier.focusRequester(firstCard) else Modifier)

  /**
   * On entry, restores the remembered card (or focuses the hero, or the first card when there is
   * no hero). On every call, makes sure the focused card's row is on screen: rows can be inserted
   * above it after returning, because the first frame may still show the previous rows.
   */
  suspend fun settle(
    rowKeys: List<String>,
    hasHero: Boolean,
  ) {
    // Wait one frame so lazy rows (composed during layout) exist before requesting focus.
    withFrameNanos { }
    if (!initialFocusHandled) {
      initialFocusHandled = true
      val restored = lastFocusedKey != null && restore.tryRequestFocus()
      if (!restored && !hero.tryRequestFocus()) firstCard.tryRequestFocus()
      withFrameNanos { }
    }
    val focusedRow = lastFocusedKey?.substringBefore('/')?.let(rowKeys::indexOf) ?: -1
    if (focusedRow >= 0) listState.revealItem(index = focusedRow + if (hasHero) 1 else 0)
  }

  /** Back from the rows: jump to the top and focus the hero. */
  suspend fun returnToHero() {
    listState.scrollToItem(0)
    hero.tryRequestFocus()
  }
}

@Composable
internal fun rememberHomeFocus(listState: LazyListState): HomeFocus {
  val lastFocusedKey = rememberSaveable { mutableStateOf<String?>(null) }
  return remember(lastFocusedKey, listState) { HomeFocus(lastFocusedKey, listState) }
}
