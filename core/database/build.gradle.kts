plugins {
  alias(libs.plugins.orbit.android.library)
  alias(libs.plugins.orbit.android.hilt)
  alias(libs.plugins.orbit.android.room)
}

android {
  testOptions {
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.model)
  implementation(projects.domain)

  implementation(libs.androidx.datastore.preferences)

  testImplementation(testFixtures(projects.domain))
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.robolectric)
}
