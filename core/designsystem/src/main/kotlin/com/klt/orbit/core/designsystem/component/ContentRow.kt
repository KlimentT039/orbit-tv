package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.focus.OrbitScrolling
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A titled horizontal row of items.
 *
 * Focus: the row is a focus group with [focusRestorer], so entering it from above or below lands
 * on the item that was focused last time instead of whichever card happens to be closest. Cards
 * scroll with [OrbitScrolling.Row], so they stay inside the overscan safe area. Hoist
 * [state] (e.g. with `rememberSaveable`) to also keep the scroll position across navigation.
 *
 * @param fallbackFocusRequester focused when the row is entered for the first time; defaults to
 *   the first visible item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ContentRow(
  title: String,
  items: ImmutableList<T>,
  key: (T) -> Any,
  modifier: Modifier = Modifier,
  state: LazyListState = rememberLazyListState(),
  fallbackFocusRequester: FocusRequester = FocusRequester.Default,
  itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
  Column(modifier = modifier) {
    Text(
      text = title,
      style = OrbitTheme.typography.titleLarge,
      color = OrbitTheme.colors.onSurface,
      modifier =
        Modifier
          .padding(start = OrbitSpacing.safeAreaHorizontal)
          .semantics { heading() },
    )
    CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Row) {
      LazyRow(
        state = state,
        modifier =
          Modifier
            .fillMaxWidth()
            .focusRestorer(fallbackFocusRequester),
        // Vertical padding leaves room for the focused card's scale and glow so they are not clipped.
        contentPadding =
          PaddingValues(
            horizontal = OrbitSpacing.safeAreaHorizontal,
            vertical = OrbitSpacing.lg,
          ),
        horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.cardGap),
      ) {
        items(items = items, key = key) { item -> itemContent(item) }
      }
    }
  }
}

@PreviewTv
@Composable
private fun ContentRowPreview() {
  OrbitThemePreview {
    ContentRow(
      title = "Mars",
      items = List(6) { "Mars video #$it" }.toImmutableList(),
      key = { it },
    ) { title ->
      VideoCard(title = title, imageUrl = null, onClick = {})
    }
  }
}
