package com.klt.orbit.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import kotlinx.coroutines.delay

/**
 * A hero that pages through [itemCount] slides with D-pad Left/Right while it has focus.
 *
 * Unlike a wrap-around carousel it never traps focus: Left on the first slide and Right on the last
 * pass through, so the user can still reach a navigation drawer or neighbouring content. The
 * [content] for the selected slide is re-rendered in place rather than swapped out, so the focused
 * action inside it keeps focus while slides change. Slides auto-advance only while the hero is
 * focused, so the page never shifts under a user who is browsing the rows below.
 *
 * @param selectedIndex hoisted so the screen can save it and react (e.g. update the backdrop).
 */
@Composable
fun HeroCarousel(
  itemCount: Int,
  selectedIndex: Int,
  onSelectedIndexChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  autoAdvanceMillis: Long = HeroCarouselDefaults.AUTO_ADVANCE_MILLIS,
  content: @Composable (index: Int) -> Unit,
) {
  var hasFocus by remember { mutableStateOf(false) }
  val currentOnChange by rememberUpdatedState(onSelectedIndexChange)
  val index = selectedIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))

  if (hasFocus && itemCount > 1 && autoAdvanceMillis > 0) {
    LaunchedEffect(index, itemCount) {
      delay(autoAdvanceMillis)
      currentOnChange((index + 1) % itemCount)
    }
  }

  Box(
    modifier =
      modifier
        .onFocusChanged { hasFocus = it.hasFocus }
        .onPreviewKeyEvent { event ->
          if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
          when {
            event.key == Key.DirectionRight && index < itemCount - 1 -> {
              currentOnChange(index + 1)
              true
            }

            event.key == Key.DirectionLeft && index > 0 -> {
              currentOnChange(index - 1)
              true
            }

            else -> {
              false
            }
          }
        },
  ) {
    if (itemCount > 0) content(index)
    if (itemCount > 1) {
      CarouselIndicator(
        count = itemCount,
        selectedIndex = index,
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .padding(end = OrbitSpacing.safeAreaHorizontal, bottom = OrbitSpacing.xl + OrbitSpacing.md),
      )
    }
  }
}

@Composable
private fun CarouselIndicator(
  count: Int,
  selectedIndex: Int,
  modifier: Modifier = Modifier,
) {
  val description = stringResource(R.string.designsystem_carousel_position, selectedIndex + 1, count)
  Row(
    modifier = modifier.semantics { contentDescription = description },
    horizontalArrangement = Arrangement.spacedBy(OrbitSpacing.sm),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(count) { i ->
      val selected = i == selectedIndex
      val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "indicatorWidth")
      val color by animateColorAsState(
        if (selected) OrbitTheme.colors.primary else OrbitTheme.colors.onSurface.copy(alpha = 0.4f),
        label = "indicatorColor",
      )
      Box(
        Modifier
          .width(width)
          .height(8.dp)
          .background(color, OrbitShape.Pill),
      )
    }
  }
}

object HeroCarouselDefaults {
  const val AUTO_ADVANCE_MILLIS = 8_000L
}
