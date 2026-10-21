package com.klt.orbit.feature.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.klt.orbit.feature.player.PlayerRoute
import kotlinx.serialization.Serializable

/** @property startPositionMs where to start, or [RESUME_SAVED_POSITION] to continue where the user left off. */
@Serializable
data class PlayerDestination(
  val videoId: String,
  val startPositionMs: Long = RESUME_SAVED_POSITION,
) {
  companion object {
    /** SavedStateHandle keys; type-safe navigation stores arguments under their property names. */
    const val VIDEO_ID_ARG = "videoId"
    const val START_POSITION_ARG = "startPositionMs"

    const val RESUME_SAVED_POSITION = -1L
  }
}

fun NavController.navigateToPlayer(
  videoId: String,
  startPositionMs: Long = PlayerDestination.RESUME_SAVED_POSITION,
  builder: NavOptionsBuilder.() -> Unit = {},
) = navigate(PlayerDestination(videoId, startPositionMs), builder)

/** @param onPlayNext autoplay moved on to another video; the host replaces the player with it. */
fun NavGraphBuilder.playerScreen(onPlayNext: (videoId: String) -> Unit) {
  composable<PlayerDestination> {
    PlayerRoute(onPlayNext = onPlayNext)
  }
}
