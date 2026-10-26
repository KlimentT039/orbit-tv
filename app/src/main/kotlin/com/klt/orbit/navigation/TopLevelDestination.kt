package com.klt.orbit.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.klt.orbit.R
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.feature.favorites.navigation.FavoritesDestination
import com.klt.orbit.feature.home.navigation.HomeDestination
import com.klt.orbit.feature.search.navigation.SearchDestination
import com.klt.orbit.feature.settings.navigation.SettingsDestination
import kotlin.reflect.KClass

/** The screens reachable from the navigation drawer, in drawer order. */
enum class TopLevelDestination(
  val route: Any,
  val routeClass: KClass<*>,
  val icon: ImageVector,
  @param:StringRes val labelRes: Int,
) {
  HOME(HomeDestination, HomeDestination::class, OrbitIcons.Home, R.string.nav_home),
  SEARCH(SearchDestination, SearchDestination::class, OrbitIcons.Search, R.string.nav_search),
  FAVORITES(FavoritesDestination, FavoritesDestination::class, OrbitIcons.Favorite, R.string.nav_favorites),
  SETTINGS(SettingsDestination, SettingsDestination::class, OrbitIcons.Settings, R.string.nav_settings),
}
