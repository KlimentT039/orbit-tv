package com.klt.orbit.feature.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.klt.orbit.feature.details.DetailsRoute
import kotlinx.serialization.Serializable

@Serializable
data class DetailsDestination(
  val videoId: String,
) {
  companion object {
    /** SavedStateHandle key; type-safe navigation stores arguments under their property names. */
    const val VIDEO_ID_ARG = "videoId"
  }
}

fun NavController.navigateToDetails(
  videoId: String,
  builder: NavOptionsBuilder.() -> Unit = {},
) = navigate(DetailsDestination(videoId), builder)

fun NavGraphBuilder.detailsScreen(
  onPlay: (videoId: String, startPositionMs: Long) -> Unit,
  onOpenDetails: (videoId: String) -> Unit,
) {
  composable<DetailsDestination> {
    DetailsRoute(onPlay = onPlay, onOpenDetails = onOpenDetails)
  }
}
