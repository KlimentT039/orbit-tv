plugins {
  alias(libs.plugins.orbit.jvm.library)
}

dependencies {
  api(projects.core.model)
  implementation(projects.core.common)
  api(libs.androidx.paging.common)
  implementation(libs.javax.inject)
}
