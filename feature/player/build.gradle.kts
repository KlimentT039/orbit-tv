plugins {
  alias(libs.plugins.orbit.android.feature)
}

dependencies {
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui.compose)
}
