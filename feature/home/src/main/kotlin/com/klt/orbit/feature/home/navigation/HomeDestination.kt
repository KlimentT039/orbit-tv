package com.klt.orbit.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.klt.orbit.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen(
  onOpenDetails: (videoId: String) -> Unit,
  onPlay: (videoId: String) -> Unit,
) {
  composable<HomeDestination> {
    HomeRoute(onOpenDetails = onOpenDetails, onPlay = onPlay)
  }
}
