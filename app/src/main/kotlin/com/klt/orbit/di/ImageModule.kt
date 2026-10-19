package com.klt.orbit.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.klt.orbit.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
  private const val MEMORY_CACHE_PERCENT = 0.25
  private const val DISK_CACHE_BYTES = 150L * 1024 * 1024

  /**
   * Coil shares the app's OkHttp connection pool and timeouts, but not its HTTP cache: images get
   * Coil's own disk cache, so they are not stored twice.
   */
  @Provides
  @Singleton
  fun provideImageLoader(
    @ApplicationContext context: Context,
    okHttpClient: OkHttpClient,
  ): ImageLoader {
    val imageClient = okHttpClient.newBuilder().cache(null).build()
    return ImageLoader
      .Builder(context)
      .components { add(OkHttpNetworkFetcherFactory(callFactory = { imageClient })) }
      .memoryCache { MemoryCache.Builder().maxSizePercent(context, MEMORY_CACHE_PERCENT).build() }
      .diskCache {
        DiskCache
          .Builder()
          .directory(context.cacheDir.resolve("images"))
          .maxSizeBytes(DISK_CACHE_BYTES)
          .build()
      }.crossfade(true)
      .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
      .build()
  }
}
