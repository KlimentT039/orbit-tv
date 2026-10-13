import com.android.build.api.dsl.ApplicationExtension
import com.klt.orbit.buildlogic.OrbitConfig
import com.klt.orbit.buildlogic.configureKotlinAndroid
import com.klt.orbit.buildlogic.configureQuality
import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("android-application"))

      extensions.configure<ApplicationExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = OrbitConfig.TARGET_SDK
        lint {
          // Lint the app together with every library it depends on, in one report.
          checkDependencies = true
          abortOnError = true
          warningsAsErrors = true
          checkReleaseBuilds = false
          disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
        }
      }
      configureQuality()
    }
  }
}
