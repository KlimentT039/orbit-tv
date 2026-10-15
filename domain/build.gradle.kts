plugins {
  alias(libs.plugins.orbit.jvm.library)
  `java-test-fixtures`
}

dependencies {
  api(projects.core.common)
  api(projects.core.model)
  api(libs.androidx.paging.common)
  api(libs.kotlinx.coroutines.core)
  implementation(libs.javax.inject)

  // Fakes and test helpers shared with feature ViewModel tests: testImplementation(testFixtures(projects.domain)).
  testFixturesApi(libs.junit4)
  testFixturesApi(libs.kotlinx.coroutines.test)
  testFixturesApi(libs.androidx.paging.common)
}
