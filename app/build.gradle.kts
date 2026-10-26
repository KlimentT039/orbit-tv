plugins {
  alias(libs.plugins.orbit.android.applicationCompose)
  alias(libs.plugins.orbit.android.hilt)
}

android {
  namespace = "com.klt.orbit"

  defaultConfig {
    applicationId = "com.klt.orbit"
    versionCode = 1
    versionName = "1.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Debug-signed so the release build can be installed on an emulator; replace for distribution.
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  buildFeatures {
    buildConfig = true
  }

  // Robolectric Compose tests for app-level UI (the navigation drawer).
  testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.database)
  implementation(projects.core.designsystem)
  implementation(projects.core.model)
  implementation(projects.core.network)
  implementation(projects.domain)
  implementation(projects.feature.details)
  implementation(projects.feature.favorites)
  implementation(projects.feature.home)
  implementation(projects.feature.player)
  implementation(projects.feature.search)
  implementation(projects.feature.settings)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.profileinstaller)
  implementation(libs.kotlinx.collections.immutable)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.robolectric)

  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
