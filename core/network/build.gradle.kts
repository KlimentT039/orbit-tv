plugins {
  alias(libs.plugins.orbit.android.library)
  alias(libs.plugins.orbit.android.hilt)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.model)
  implementation(projects.domain)

  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging)
  implementation(libs.retrofit)
  implementation(libs.retrofit.kotlinx.serialization)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(platform(libs.okhttp.bom))
  testImplementation(libs.okhttp.mockwebserver)
  testImplementation(libs.androidx.paging.testing)
}
