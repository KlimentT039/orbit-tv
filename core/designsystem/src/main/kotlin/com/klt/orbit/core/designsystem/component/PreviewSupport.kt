package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitSpacing
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/** Wraps preview content in the theme and space background. Preview-only helper. */
@Composable
internal fun OrbitThemePreview(content: @Composable () -> Unit) {
  OrbitTheme {
    Box(
      modifier =
        Modifier
          .background(OrbitGradients.SpaceBackground)
          .padding(OrbitSpacing.xl),
    ) {
      content()
    }
  }
}
