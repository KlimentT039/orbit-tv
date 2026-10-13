package com.klt.orbit.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

/** Applies detekt and ktlint (with Compose rules) using the shared configs under /config. */
internal fun Project.configureQuality() {
  configureDetekt()
  configureKtlint()
}

private fun Project.configureDetekt() {
  pluginManager.apply(libs.pluginId("detekt"))
  val rootDir = isolated.rootProject.projectDirectory

  extensions.configure<DetektExtension> {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(rootDir.file("config/detekt/detekt.yml"))
  }
  tasks.withType<Detekt>().configureEach {
    jvmTarget = OrbitConfig.jvmTarget.target
    reports {
      html.required.set(true)
      sarif.required.set(true)
      xml.required.set(false)
      txt.required.set(false)
      md.required.set(false)
    }
  }
  // detekt 1.23.x embeds the compiler it was built with; keep the project's newer Kotlin off its classpath.
  val detektKotlin = libs.version("detektKotlin")
  configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
      if (requested.group == "org.jetbrains.kotlin") useVersion(detektKotlin)
    }
  }
}

private fun Project.configureKtlint() {
  pluginManager.apply(libs.pluginId("ktlint"))

  extensions.configure<KtlintExtension> {
    version.set(libs.version("ktlint"))
    android.set(true)
    reporters {
      reporter(ReporterType.PLAIN)
      reporter(ReporterType.SARIF)
    }
    filter {
      exclude { it.file.path.contains("/build/") }
    }
  }
  dependencies {
    add("ktlintRuleset", libs.library("compose-rules-ktlint"))
  }
}
