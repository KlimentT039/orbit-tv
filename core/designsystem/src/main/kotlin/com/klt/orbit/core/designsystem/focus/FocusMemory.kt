package com.klt.orbit.core.designsystem.focus

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
 * Remembers which item of a list or grid last had focus, in saved state, so a screen can put focus
 * back on it after the user returns from another screen (e.g. Details).
 *
 * Mark each item with [restorableItem]; on entry, call [restore] once the items are composed and
 * fall back to a default target when it returns false.
 */
@Stable
class FocusMemory internal constructor(
  lastFocusedKey: MutableState<String?>,
) {
  /** Key of the last focused item, or null if none has been focused yet. */
  var lastFocusedKey: String? by lastFocusedKey
    private set

  private val requester = FocusRequester()

  fun Modifier.restorableItem(key: String): Modifier =
    onFocusChanged { if (it.isFocused) lastFocusedKey = key }
      .then(if (key == lastFocusedKey) Modifier.focusRequester(requester) else Modifier)

  /** Focuses the remembered item. False if there is none or it is no longer composed. */
  fun restore(): Boolean = lastFocusedKey != null && requester.tryRequestFocus()

  fun forget() {
    lastFocusedKey = null
  }
}

@Composable
fun rememberFocusMemory(): FocusMemory {
  val lastFocusedKey = rememberSaveable { mutableStateOf<String?>(null) }
  return remember(lastFocusedKey) { FocusMemory(lastFocusedKey) }
}
