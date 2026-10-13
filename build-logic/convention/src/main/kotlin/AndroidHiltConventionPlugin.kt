import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.library
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("ksp"))
      pluginManager.apply(libs.pluginId("hilt"))

      dependencies {
        add("implementation", libs.library("hilt-android"))
        add("ksp", libs.library("hilt-compiler"))
      }
    }
  }
}
