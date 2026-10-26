package com.klt.orbit.feature.favorites.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.klt.orbit.feature.favorites.FavoritesRoute
import kotlinx.serialization.Serializable

@Serializable
data object FavoritesDestination

/** @param onBrowse the list is empty and the user wants to find something to add. */
fun NavGraphBuilder.favoritesScreen(
  onOpenDetails: (videoId: String) -> Unit,
  onBrowse: () -> Unit,
) {
  composable<FavoritesDestination> {
    FavoritesRoute(onOpenDetails = onOpenDetails, onBrowse = onBrowse)
  }
}
