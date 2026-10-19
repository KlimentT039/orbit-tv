package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.klt.orbit.core.designsystem.theme.OrbitShape
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * A non-interactive tag. Deliberately not focusable: on TV every focusable element is a D-pad stop,
 * and tags that do nothing would only slow the user down on the way to the actions.
 */
@Composable
fun KeywordChip(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    style = OrbitTheme.typography.labelMedium,
    color = OrbitTheme.colors.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier =
      modifier
        .border(1.dp, OrbitTheme.colors.border, OrbitShape.Chip)
        .padding(horizontal = OrbitSpacing.md, vertical = OrbitSpacing.xs),
  )
}
