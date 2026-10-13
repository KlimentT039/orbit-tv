package com.klt.orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.showcase.DesignSystemShowcase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent {
      OrbitTheme {
        DesignSystemShowcase()
      }
    }
  }
}
