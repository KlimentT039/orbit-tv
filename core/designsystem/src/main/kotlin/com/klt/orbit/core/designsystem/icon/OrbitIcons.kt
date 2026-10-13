package com.klt.orbit.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The app's icon set, built from Material Icons path data (Apache 2.0). Defined locally instead of
 * depending on the frozen `material-icons-extended` artifact, which would add thousands of unused
 * classes. Icons are lazily built once and tinted by `Icon` at the call site.
 */
object OrbitIcons {
  val Home: ImageVector by lazy { icon("Home", "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z") }

  val Search: ImageVector by lazy {
    icon(
      "Search",
      "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 " +
        "5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5," +
        "14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
    )
  }

  val Favorite: ImageVector by lazy {
    icon(
      "Favorite",
      "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5," +
        "2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z",
    )
  }

  val FavoriteBorder: ImageVector by lazy {
    icon(
      "FavoriteBorder",
      "M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3 4.42,3 2,5.42 2,8.5c0,3.78 " +
        "3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5 22,5.42 19.58,3 16.5," +
        "3zM12.1,18.55l-0.1,0.1 -0.1,-0.1C7.14,14.24 4,11.39 4,8.5 4,6.5 5.5,5 7.5,5c1.54,0 3.04," +
        "0.99 3.57,2.36h1.87C13.46,5.99 14.96,5 16.5,5c2,0 3.5,1.5 3.5,3.5 0,2.89 -3.14,5.74 " +
        "-7.9,10.05z",
    )
  }

  val Settings: ImageVector by lazy {
    icon(
      "Settings",
      "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18," +
        "-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5," +
        "-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 " +
        "-0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47," +
        "0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8," +
        "12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 " +
        "0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 " +
        "0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39," +
        "0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12," +
        "15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z",
    )
  }

  val Play: ImageVector by lazy { icon("Play", "M8,5v14l11,-7z") }

  val Pause: ImageVector by lazy { icon("Pause", "M6,19h4V5H6v14zm8,-14v14h4V5h-4z") }

  val Replay: ImageVector by lazy {
    icon(
      "Replay",
      "M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84," +
        "-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 " +
        "3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z",
    )
  }

  val Error: ImageVector by lazy {
    icon(
      "Error",
      "M11,15h2v2h-2zM11,7h2v6h-2zM11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 " +
        "22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8z",
    )
  }

  val CloudOff: ImageVector by lazy {
    icon(
      "CloudOff",
      "M19.35,10.04C18.67,6.59 15.64,4 12,4c-1.48,0 -2.85,0.43 -4.01,1.17l1.46,1.46C10.21,6.23 " +
        "11.08,6 12,6c3.04,0 5.5,2.46 5.5,5.5v0.5H19c1.66,0 3,1.34 3,3 0,1.13 -0.64,2.11 -1.56,2.62l1.45," +
        "1.45C23.16,18.16 24,16.68 24,15c0,-2.64 -2.05,-4.78 -4.65,-4.96zM3,5.27l2.75,2.74C2.56,8.15 " +
        "0,10.77 0,14c0,3.31 2.69,6 6,6h11.73l2,2L21,20.73 4.27,4 3,5.27zM7.73,10l8,8H6c-2.21,0 -4,-1.79 " +
        "-4,-4s1.79,-4 4,-4h1.73z",
    )
  }

  /** Brand mark: a planet with a tilted orbit ring, used for missing artwork and app chrome. */
  val Orbit: ImageVector by lazy {
    ImageVector
      .Builder(
        name = "Orbit",
        defaultWidth = ICON_SIZE.dp,
        defaultHeight = ICON_SIZE.dp,
        viewportWidth = ICON_SIZE,
        viewportHeight = ICON_SIZE,
      ).addPath(
        pathData = addPathNodes("M12,7.5a4.5,4.5 0,1 1,0 9a4.5,4.5 0,1 1,0 -9z"),
        fill = SolidColor(Color.Black),
      ).addGroup(rotate = -25f, pivotX = 12f, pivotY = 12f)
      .addPath(
        pathData = addPathNodes("M1.5,12a10.5,3.5 0,1 0,21 0a10.5,3.5 0,1 0,-21 0z"),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.4f,
      ).clearGroup()
      .build()
  }
}

private const val ICON_SIZE = 24f

private fun icon(
  name: String,
  pathData: String,
): ImageVector =
  ImageVector
    .Builder(
      name = name,
      defaultWidth = ICON_SIZE.dp,
      defaultHeight = ICON_SIZE.dp,
      viewportWidth = ICON_SIZE,
      viewportHeight = ICON_SIZE,
    ).addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
    .build()
