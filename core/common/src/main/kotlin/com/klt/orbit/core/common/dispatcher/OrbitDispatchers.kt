package com.klt.orbit.core.common.dispatcher

import javax.inject.Qualifier

/** Qualifies an injected `CoroutineDispatcher`, so tests can swap in a test dispatcher. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
  val dispatcher: OrbitDispatchers,
)

enum class OrbitDispatchers {
  Default,
  IO,
}
