package com.klt.orbit.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/** Enables Compose with the TV material BOM-aligned dependencies and opt-in compiler reports. */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
  pluginManager.apply(libs.pluginId("kotlin-compose"))

  commonExtension.buildFeatures.compose = true

  dependencies {
    val bom = libs.library("androidx-compose-bom")
    add("implementation", platform(bom))
    add("androidTestImplementation", platform(bom))
    add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
    add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
  }

  extensions.configure<ComposeCompilerGradlePluginExtension> {
    // `./gradlew assembleRelease -Porbit.composeReports=true` writes stability reports to build/compose.
    if (providers.gradleProperty("orbit.composeReports").orNull == "true") {
      val dir = layout.buildDirectory.dir("compose")
      reportsDestination = dir
      metricsDestination = dir
    }
    stabilityConfigurationFiles.add(
      isolated.rootProject.projectDirectory.file("config/compose/stability.conf"),
    )
  }
}
