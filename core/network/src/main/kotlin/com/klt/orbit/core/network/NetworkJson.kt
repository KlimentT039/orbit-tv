package com.klt.orbit.core.network

import kotlinx.serialization.json.Json

/** Tolerant JSON: unknown keys are ignored and explicit nulls fall back to defaults. */
internal val NetworkJson: Json =
  Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    isLenient = true
  }
