package com.klt.orbit.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * Focus bookkeeping for Details: which element was focused last (saved across navigation) and the
 * requesters used to restore it or to land on the primary action.
 */
@Stable
internal class DetailsFocus(
  lastFocusedKey: MutableState<String?>,
) {
  var lastFocusedKey by lastFocusedKey
  val primary = FocusRequester()
  val favorite = FocusRequester()
  val restore = FocusRequester()

  /** Remembers this element when it gains focus, and makes it the restore target if it was last. */
  fun Modifier.tracked(key: String): Modifier =
    onFocusChanged { if (it.isFocused) lastFocusedKey = key }
      .then(if (key == lastFocusedKey) Modifier.focusRequester(restore) else Modifier)

  companion object {
    const val PLAY = "play"
    const val RESUME = "resume"
    const val FAVORITE = "favorite"
    const val RELATED_PREFIX = "related/"
  }
}

@Composable
internal fun rememberDetailsFocus(): DetailsFocus {
  val lastFocusedKey = rememberSaveable { mutableStateOf<String?>(null) }
  return remember(lastFocusedKey) { DetailsFocus(lastFocusedKey) }
}
