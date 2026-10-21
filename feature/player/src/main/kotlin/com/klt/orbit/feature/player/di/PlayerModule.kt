package com.klt.orbit.feature.player.di

import com.klt.orbit.feature.player.engine.ExoVideoPlayerFactory
import com.klt.orbit.feature.player.engine.VideoPlayerFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal interface PlayerModule {
  @Binds
  fun bindVideoPlayerFactory(factory: ExoVideoPlayerFactory): VideoPlayerFactory
}
