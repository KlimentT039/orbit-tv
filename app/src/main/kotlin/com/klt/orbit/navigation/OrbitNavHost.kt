package com.klt.orbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.klt.orbit.feature.details.navigation.detailsScreen
import com.klt.orbit.feature.details.navigation.navigateToDetails
import com.klt.orbit.feature.favorites.navigation.favoritesScreen
import com.klt.orbit.feature.home.navigation.HomeDestination
import com.klt.orbit.feature.home.navigation.homeScreen
import com.klt.orbit.feature.player.navigation.PlayerDestination
import com.klt.orbit.feature.player.navigation.navigateToPlayer
import com.klt.orbit.feature.player.navigation.playerScreen
import com.klt.orbit.feature.search.navigation.searchScreen
import com.klt.orbit.feature.settings.navigation.settingsScreen

/** App navigation graph. Features expose destinations; only the app knows how they connect. */
@Composable
fun OrbitNavHost(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentTopLevel =
    TopLevelDestination.entries.firstOrNull { backStackEntry?.destination?.hasRoute(it.routeClass) == true }

  OrbitNavigationDrawer(
    current = currentTopLevel,
    onNavigate = navController::navigateToTopLevel,
    modifier = modifier,
  ) {
    NavHost(
      navController = navController,
      startDestination = HomeDestination,
    ) {
      homeScreen(
        onOpenDetails = navController::navigateToDetails,
        // The hero's Play continues where the user left off.
        onPlay = { videoId -> navController.navigateToPlayer(videoId) },
      )
      searchScreen(onOpenDetails = navController::navigateToDetails)
      favoritesScreen(
        onOpenDetails = navController::navigateToDetails,
        onBrowse = { navController.navigateToTopLevel(TopLevelDestination.HOME) },
      )
      settingsScreen()
      detailsScreen(
        onPlay = { videoId, startPositionMs -> navController.navigateToPlayer(videoId, startPositionMs) },
        onOpenDetails = navController::navigateToDetails,
      )
      playerScreen(
        // Autoplay replaces the finished video, so Back still returns to where playback started.
        onPlayNext = { videoId ->
          navController.navigateToPlayer(videoId) {
            popUpTo<PlayerDestination> { inclusive = true }
          }
        },
      )
    }
  }
}

/**
 * Switches between drawer screens. Each keeps its own state (scroll position, search query,
 * focused card) while the user is elsewhere, and Back from any of them returns to Home.
 */
private fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
  navigate(destination.route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}
