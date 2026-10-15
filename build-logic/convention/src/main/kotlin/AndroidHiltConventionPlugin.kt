import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.library
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("ksp"))
      pluginManager.apply(libs.pluginId("hilt"))

      extensions.configure<KspExtension> {
        // Validate every binding at compile time, not only those reachable from today's entry points.
        arg("dagger.fullBindingGraphValidation", "ERROR")
      }
      dependencies {
        add("implementation", libs.library("hilt-android"))
        add("ksp", libs.library("hilt-compiler"))
      }
    }
  }
}
