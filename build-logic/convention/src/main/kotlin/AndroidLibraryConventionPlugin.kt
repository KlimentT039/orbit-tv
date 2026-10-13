import com.android.build.api.dsl.LibraryExtension
import com.klt.orbit.buildlogic.OrbitConfig
import com.klt.orbit.buildlogic.configureKotlinAndroid
import com.klt.orbit.buildlogic.configureQuality
import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("android-library"))

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this)
        // Namespace mirrors the module path, e.g. :core:designsystem -> com.klt.orbit.core.designsystem.
        namespace = "com.klt.orbit" + path.replace(':', '.')
        defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testOptions.targetSdk = OrbitConfig.TARGET_SDK
        lint.targetSdk = OrbitConfig.TARGET_SDK
      }
      configureQuality()
    }
  }
}
