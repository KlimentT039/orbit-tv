package com.klt.orbit.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Shared Android + Kotlin setup for application and library modules (AGP built-in Kotlin). */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
  commonExtension.apply {
    compileSdk = OrbitConfig.COMPILE_SDK
    defaultConfig.minSdk = OrbitConfig.MIN_SDK
    compileOptions.sourceCompatibility = OrbitConfig.javaVersion
    compileOptions.targetCompatibility = OrbitConfig.javaVersion
    testOptions.unitTests.isReturnDefaultValues = true
  }
  extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions { configureOrbitCompilerOptions() }
  }
  addCommonTestDependencies()
}

/** Shared setup for pure Kotlin/JVM modules. */
internal fun Project.configureKotlinJvm() {
  extensions.configure<JavaPluginExtension> {
    sourceCompatibility = OrbitConfig.javaVersion
    targetCompatibility = OrbitConfig.javaVersion
  }
  extensions.configure<KotlinJvmProjectExtension> {
    compilerOptions { configureOrbitCompilerOptions() }
  }
  addCommonTestDependencies()
}

private fun KotlinJvmCompilerOptions.configureOrbitCompilerOptions() {
  jvmTarget = OrbitConfig.jvmTarget
  allWarningsAsErrors = true
  freeCompilerArgs.addAll(
    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    "-Xconsistent-data-class-copy-visibility",
  )
}

private fun Project.addCommonTestDependencies() {
  dependencies {
    add("testImplementation", libs.library("junit4"))
    add("testImplementation", libs.library("truth"))
    add("testImplementation", libs.library("turbine"))
    add("testImplementation", libs.library("kotlinx-coroutines-test"))
  }
}
