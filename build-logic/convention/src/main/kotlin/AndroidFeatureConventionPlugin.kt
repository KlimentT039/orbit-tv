import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module: Compose UI + Hilt ViewModels. Features only see domain use cases, models and
 * the design system — never the data modules or each other.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("orbit.android.library.compose")
      pluginManager.apply("orbit.android.hilt")

      dependencies {
        add("implementation", project(":core:common"))
        add("implementation", project(":core:designsystem"))
        add("implementation", project(":core:model"))
        add("implementation", project(":domain"))

        add("implementation", libs.library("androidx-tv-material"))
        add("implementation", libs.library("androidx-hilt-navigation-compose"))
        add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
        add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
        add("implementation", libs.library("androidx-navigation-compose"))
        add("implementation", libs.library("kotlinx-collections-immutable"))

        add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
        add("androidTestImplementation", libs.library("androidx-test-ext-junit"))
        add("androidTestImplementation", libs.library("androidx-test-runner"))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
      }
    }
  }
}
