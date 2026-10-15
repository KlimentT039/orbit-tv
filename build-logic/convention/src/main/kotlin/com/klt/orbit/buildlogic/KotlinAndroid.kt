package com.klt.orbit.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
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
  // Robolectric's Android 16 runtime reads FileDescriptor internals that JDK 17+ hides by default.
  tasks.withType<Test>().configureEach {
    jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED")
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
  freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
}

private fun Project.addCommonTestDependencies() {
  dependencies {
    add("testImplementation", libs.library("junit4"))
    add("testImplementation", libs.library("truth"))
    add("testImplementation", libs.library("turbine"))
    add("testImplementation", libs.library("kotlinx-coroutines-test"))
  }
}
