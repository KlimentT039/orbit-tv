import androidx.room.gradle.RoomExtension
import com.klt.orbit.buildlogic.libs
import com.klt.orbit.buildlogic.library
import com.klt.orbit.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(libs.pluginId("ksp"))
      pluginManager.apply(libs.pluginId("room"))

      extensions.configure<RoomExtension> {
        // Exported schemas are checked in so migrations can be verified with MigrationTestHelper.
        schemaDirectory("$projectDir/schemas")
      }
      dependencies {
        add("implementation", libs.library("androidx-room-runtime"))
        add("implementation", libs.library("androidx-room-ktx"))
        add("ksp", libs.library("androidx-room-compiler"))
        add("testImplementation", libs.library("androidx-room-testing"))
      }
    }
  }
}
