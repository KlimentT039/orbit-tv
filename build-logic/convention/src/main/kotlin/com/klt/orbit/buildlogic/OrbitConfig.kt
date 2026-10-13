package com.klt.orbit.buildlogic

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/** Single source of truth for SDK levels and the JVM bytecode target used by every module. */
object OrbitConfig {
  const val COMPILE_SDK = 37
  const val TARGET_SDK = 37

  // Watch Next (androidx.tvprovider) needs API 26+; 28 keeps us on modern media and TLS stacks.
  const val MIN_SDK = 28

  val javaVersion = JavaVersion.VERSION_17
  val jvmTarget = JvmTarget.JVM_17
}
