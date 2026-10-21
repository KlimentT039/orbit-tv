package com.klt.orbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.klt.orbit.feature.details.navigation.detailsScreen
import com.klt.orbit.feature.details.navigation.navigateToDetails
import com.klt.orbit.feature.home.navigation.HomeDestination
import com.klt.orbit.feature.home.navigation.homeScreen
import com.klt.orbit.feature.player.navigation.PlayerDestination
import com.klt.orbit.feature.player.navigation.navigateToPlayer
import com.klt.orbit.feature.player.navigation.playerScreen

/** App navigation graph. Features expose destinations; only the app knows how they connect. */
@Composable
fun OrbitNavHost(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  NavHost(
    navController = navController,
    startDestination = HomeDestination,
    modifier = modifier,
  ) {
    homeScreen(
      onOpenDetails = navController::navigateToDetails,
      // The hero's Play continues where the user left off.
      onPlay = { videoId -> navController.navigateToPlayer(videoId) },
    )
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
