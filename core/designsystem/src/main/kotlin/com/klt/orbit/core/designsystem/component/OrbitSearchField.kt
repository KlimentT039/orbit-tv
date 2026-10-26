package com.klt.orbit.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.preview.PreviewTv
import com.klt.orbit.core.designsystem.theme.OrbitFocus
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * A search box built for the remote.
 *
 * Moving focus onto it does not pop up the on-screen keyboard (which would cover the screen every
 * time the user passes by on the way to the results); OK opens it. The keyboard's Search action
 * hides it again and calls [onSearch], e.g. to move focus to the results.
 *
 * @param focusedPlaceholder shown instead of [placeholder] while focused and empty, to tell the
 *   user how to start typing.
 */
@Composable
fun OrbitSearchField(
  state: TextFieldState,
  onSearch: () -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  focusedPlaceholder: String = placeholder,
) {
  val colors = OrbitTheme.colors
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()
  val borderColor by animateColorAsState(if (focused) colors.primary else colors.border, label = "searchBorder")
  val borderWidth by animateDpAsState(if (focused) OrbitFocus.borderWidth else 1.dp, label = "searchBorderWidth")
  val container by animateColorAsState(
    colors.onSurface.copy(alpha = if (focused) 0.16f else 0.08f),
    label = "searchContainer",
  )

  BasicTextField(
    state = state,
    lineLimits = TextFieldLineLimits.SingleLine,
    textStyle = OrbitTheme.typography.titleMedium.copy(color = colors.onSurface),
    cursorBrush = SolidColor(colors.primary),
    interactionSource = interactionSource,
    keyboardOptions =
      KeyboardOptions(
        imeAction = ImeAction.Search,
        autoCorrectEnabled = false,
        showKeyboardOnFocus = false,
      ),
    onKeyboardAction = {
      keyboard?.hide()
      onSearch()
    },
    modifier =
      modifier.onPreviewKeyEvent { event ->
        when (event.key) {
          // OK on the remote opens the keyboard. Both halves of the press are consumed, so the text
          // field does not also treat it as the IME action.
          Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
            if (event.type == KeyEventType.KeyUp) keyboard?.show()
            true
          }

          // Up and Down always leave the field. A single line has nothing to move between, and
          // otherwise the first press only moves the cursor, which feels like a dead remote.
          Key.DirectionUp, Key.DirectionDown -> {
            if (event.type == KeyEventType.KeyDown) {
              focusManager.moveFocus(if (event.key == Key.DirectionUp) FocusDirection.Up else FocusDirection.Down)
            }
            true
          }

          else -> {
            false
          }
        }
      },
    decorator = { innerTextField ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier
            .height(OrbitSearchFieldDefaults.Height)
            .background(container, OrbitShape.Pill)
            .border(borderWidth, borderColor, OrbitShape.Pill)
            .padding(horizontal = OrbitSpacing.xl),
      ) {
        Icon(
          imageVector = OrbitIcons.Search,
          contentDescription = null,
          tint = if (focused) colors.primary else colors.onSurfaceVariant,
          modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(OrbitSpacing.md))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          if (state.text.isEmpty()) {
            Text(
              text = if (focused) focusedPlaceholder else placeholder,
              style = OrbitTheme.typography.titleMedium,
              color = colors.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          innerTextField()
        }
      }
    },
  )
}

object OrbitSearchFieldDefaults {
  val Height = 56.dp
}

@PreviewTv
@Composable
private fun OrbitSearchFieldPreview() {
  OrbitThemePreview {
    OrbitSearchField(
      state = rememberTextFieldState(),
      onSearch = {},
      placeholder = "Search NASA videos",
      modifier = Modifier.width(560.dp),
    )
  }
}
