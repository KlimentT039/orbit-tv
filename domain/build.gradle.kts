plugins {
  alias(libs.plugins.orbit.jvm.library)
  alias(libs.plugins.ksp)
  `java-test-fixtures`
}

dependencies {
  api(projects.core.common)
  api(projects.core.model)
  api(libs.androidx.paging.common)
  api(libs.kotlinx.coroutines.core)
  implementation(libs.javax.inject)
  // Generates the use cases' Dagger factories once, here. Otherwise every feature module that injects
  // the same use case generates its own copy and the app fails to dex with duplicate classes.
  implementation(libs.dagger)
  ksp(libs.dagger.compiler)

  // Fakes and test helpers shared with feature ViewModel tests: testImplementation(testFixtures(projects.domain)).
  testFixturesApi(libs.junit4)
  testFixturesApi(libs.kotlinx.coroutines.test)
  testFixturesApi(libs.androidx.paging.common)
}
