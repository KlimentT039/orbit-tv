package com.klt.orbit.di

import com.klt.orbit.BuildConfig
import com.klt.orbit.core.common.app.AppInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppInfoModule {
  @Provides
  fun provideAppInfo(): AppInfo =
    AppInfo(
      versionName = BuildConfig.VERSION_NAME,
      versionCode = BuildConfig.VERSION_CODE,
    )
}
