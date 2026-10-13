plugins {
  alias(libs.plugins.orbit.android.library)
  alias(libs.plugins.orbit.android.hilt)
  alias(libs.plugins.orbit.android.room)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.model)
  implementation(projects.domain)

  implementation(libs.androidx.datastore.preferences)
}
