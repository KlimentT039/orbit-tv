package com.klt.orbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.klt.orbit.feature.details.navigation.detailsScreen
import com.klt.orbit.feature.details.navigation.navigateToDetails
import com.klt.orbit.feature.home.navigation.HomeDestination
import com.klt.orbit.feature.home.navigation.homeScreen

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
      // The player arrives in Phase 4; until then the hero's Play opens Details.
      onPlay = navController::navigateToDetails,
    )
    detailsScreen(
      // The player arrives in Phase 4.
      onPlay = { _, _ -> },
      onOpenDetails = navController::navigateToDetails,
    )
  }
}
