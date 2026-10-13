import com.klt.orbit.buildlogic.configureKotlinJvm
import com.klt.orbit.buildlogic.configureQuality
import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Pure Kotlin module: no Android dependencies allowed (core:model, core:common, domain). */
class JvmLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("kotlin-jvm"))
      pluginManager.apply(libs.pluginId("android-lint"))
      configureKotlinJvm()
      configureQuality()
    }
  }
}
