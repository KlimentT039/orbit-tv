plugins {
  alias(libs.plugins.orbit.android.library)
  alias(libs.plugins.orbit.android.hilt)
  alias(libs.plugins.kotlin.serialization)
}

android {
  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.model)
  implementation(projects.domain)

  api(platform(libs.okhttp.bom))
  api(libs.okhttp)
  implementation(libs.okhttp.logging)
  implementation(libs.retrofit)
  implementation(libs.retrofit.kotlinx.serialization)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(testFixtures(projects.domain))
  testImplementation(libs.okhttp.mockwebserver)
  testImplementation(libs.androidx.paging.testing)
}
