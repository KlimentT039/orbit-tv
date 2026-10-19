package com.klt.orbit

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OrbitApplication :
  Application(),
  SingletonImageLoader.Factory {
  @Inject
  lateinit var imageLoader: Lazy<ImageLoader>

  override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
