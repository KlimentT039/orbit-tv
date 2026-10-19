import com.android.build.api.dsl.LibraryExtension
import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.library
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * A feature module: Compose UI + Hilt ViewModels + a type-safe navigation destination. Features only
 * see domain use cases, models and the design system — never the data modules or each other.
 *
 * Unit tests include Robolectric Compose UI tests (focus, keys), so they run in CI without a device.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("orbit.android.library.compose")
      pluginManager.apply("orbit.android.hilt")
      pluginManager.apply(libs.pluginId("kotlin-serialization"))

      extensions.configure<LibraryExtension> {
        testOptions.unitTests.isIncludeAndroidResources = true
      }
      // Gradle 9 fails a test task that discovers no tests. A feature whose screen arrives in a later
      // phase has no tests yet; once src/test exists the safety check applies again.
      if (!file("src/test").exists()) {
        tasks.withType<Test>().configureEach { failOnNoDiscoveredTests.set(false) }
      }

      dependencies {
        add("implementation", project(":core:common"))
        add("implementation", project(":core:designsystem"))
        add("implementation", project(":core:model"))
        add("implementation", project(":domain"))

        add("implementation", libs.library("androidx-activity-compose"))
        add("implementation", libs.library("androidx-tv-material"))
        add("implementation", libs.library("androidx-hilt-lifecycle-viewmodel-compose"))
        add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
        add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
        add("implementation", libs.library("androidx-navigation-compose"))
        add("implementation", libs.library("kotlinx-collections-immutable"))
        add("implementation", libs.library("kotlinx-serialization-core"))

        add("testImplementation", testFixtures(project(":domain")))
        add("testImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("testImplementation", libs.library("androidx-test-core"))
        add("testImplementation", libs.library("robolectric"))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))

        add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("androidTestImplementation", libs.library("androidx-test-ext-junit"))
        add("androidTestImplementation", libs.library("androidx-test-runner"))
      }
    }
  }
}
