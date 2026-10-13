import com.android.build.api.dsl.ApplicationExtension
import com.klt.orbit.buildlogic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("orbit.android.application")
      configureAndroidCompose(extensions.getByType<ApplicationExtension>())
    }
  }
}
