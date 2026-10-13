plugins {
  alias(libs.plugins.orbit.android.libraryCompose)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.ui)
  api(libs.androidx.tv.material)
  api(libs.kotlinx.collections.immutable)
  implementation(libs.coil.compose)

  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
