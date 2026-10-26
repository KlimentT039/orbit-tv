package com.klt.orbit.core.common.app

/**
 * Build metadata of the running app. Provided by `app` (which owns `BuildConfig`), so features can
 * show it without depending on the application module.
 */
data class AppInfo(
  val versionName: String,
  val versionCode: Int,
)
