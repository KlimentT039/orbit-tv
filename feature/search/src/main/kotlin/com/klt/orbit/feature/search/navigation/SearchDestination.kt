package com.klt.orbit.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.klt.orbit.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchDestination

fun NavGraphBuilder.searchScreen(onOpenDetails: (videoId: String) -> Unit) {
  composable<SearchDestination> {
    SearchRoute(onOpenDetails = onOpenDetails)
  }
}
