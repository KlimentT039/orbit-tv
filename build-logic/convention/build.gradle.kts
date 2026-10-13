import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.klt.orbit.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

dependencies {
  compileOnly(libs.android.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin)
  compileOnly(libs.compose.gradle.plugin)
  compileOnly(libs.room.gradle.plugin)
  compileOnly(libs.detekt.gradle.plugin)
  compileOnly(libs.ktlint.gradle.plugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = libs.plugins.orbit.android.application.get().pluginId
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = libs.plugins.orbit.android.applicationCompose.get().pluginId
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidLibrary") {
      id = libs.plugins.orbit.android.library.get().pluginId
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = libs.plugins.orbit.android.libraryCompose.get().pluginId
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("androidFeature") {
      id = libs.plugins.orbit.android.feature.get().pluginId
      implementationClass = "AndroidFeatureConventionPlugin"
    }
    register("androidHilt") {
      id = libs.plugins.orbit.android.hilt.get().pluginId
      implementationClass = "AndroidHiltConventionPlugin"
    }
    register("androidRoom") {
      id = libs.plugins.orbit.android.room.get().pluginId
      implementationClass = "AndroidRoomConventionPlugin"
    }
    register("jvmLibrary") {
      id = libs.plugins.orbit.jvm.library.get().pluginId
      implementationClass = "JvmLibraryConventionPlugin"
    }
  }
}
