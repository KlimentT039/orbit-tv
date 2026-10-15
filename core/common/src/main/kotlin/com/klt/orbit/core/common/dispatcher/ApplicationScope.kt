package com.klt.orbit.core.common.dispatcher

import javax.inject.Qualifier

/**
 * Qualifies the app-wide `CoroutineScope`, for work that must outlive a screen (e.g. saving
 * playback progress while the player is closing).
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
