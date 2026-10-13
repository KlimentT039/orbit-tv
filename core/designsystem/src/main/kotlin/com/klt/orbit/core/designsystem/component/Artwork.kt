package com.klt.orbit.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.theme.OrbitGradients
import com.klt.orbit.core.designsystem.theme.OrbitTheme

/**
 * Remote artwork drawn over a branded placeholder. The placeholder stays underneath, so a missing
 * URL, a slow network or a failed load all degrade to the same calm visual instead of a blank hole.
 */
@Composable
internal fun Artwork(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  placeholder: Brush = OrbitGradients.ArtworkPlaceholder,
  placeholderIconSize: Dp = 40.dp,
) {
  Box(modifier = modifier.background(placeholder), contentAlignment = Alignment.Center) {
    if (placeholderIconSize > 0.dp) {
      Icon(
        imageVector = OrbitIcons.Orbit,
        contentDescription = null,
        tint = OrbitTheme.colors.border,
        modifier = Modifier.size(placeholderIconSize),
      )
    }
    if (!imageUrl.isNullOrBlank()) {
      AsyncImage(
        model =
          ImageRequest
            .Builder(LocalPlatformContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
