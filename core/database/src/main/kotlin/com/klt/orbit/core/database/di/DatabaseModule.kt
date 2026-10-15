package com.klt.orbit.core.database.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.klt.orbit.core.common.dispatcher.ApplicationScope
import com.klt.orbit.core.common.dispatcher.Dispatcher
import com.klt.orbit.core.common.dispatcher.OrbitDispatchers
import com.klt.orbit.core.database.OrbitDatabase
import com.klt.orbit.core.database.repository.LocalFavoritesRepository
import com.klt.orbit.core.database.repository.LocalPlaybackProgressRepository
import com.klt.orbit.core.database.settings.DataStoreSettingsRepository
import com.klt.orbit.domain.repository.FavoritesRepository
import com.klt.orbit.domain.repository.PlaybackProgressRepository
import com.klt.orbit.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
  @Provides
  @Singleton
  fun provideDatabase(
    @ApplicationContext context: Context,
  ): OrbitDatabase = Room.databaseBuilder(context, OrbitDatabase::class.java, OrbitDatabase.NAME).build()

  @Provides
  @Singleton
  fun provideSettingsDataStore(
    @ApplicationContext context: Context,
    @ApplicationScope scope: CoroutineScope,
    @Dispatcher(OrbitDispatchers.IO) ioDispatcher: CoroutineDispatcher,
  ): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
      scope = scope + ioDispatcher,
      produceFile = { context.preferencesDataStoreFile("settings") },
    )
}

@Module
@InstallIn(SingletonComponent::class)
internal interface DatabaseBindsModule {
  @Binds
  fun bindFavoritesRepository(repository: LocalFavoritesRepository): FavoritesRepository

  @Binds
  fun bindPlaybackProgressRepository(repository: LocalPlaybackProgressRepository): PlaybackProgressRepository

  @Binds
  fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
}
