plugins {
  alias(libs.plugins.orbit.jvm.library)
}

dependencies {
  api(libs.kotlinx.coroutines.core)
  api(libs.javax.inject)
}
