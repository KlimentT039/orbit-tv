package com.klt.orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import com.klt.orbit.navigation.OrbitNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent {
      OrbitTheme {
        OrbitNavHost(modifier = Modifier.fillMaxSize().background(OrbitTheme.colors.background))
      }
    }
  }
}
