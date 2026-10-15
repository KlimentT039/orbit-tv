package com.klt.orbit.di

import com.klt.orbit.core.common.dispatcher.ApplicationScope
import com.klt.orbit.core.common.dispatcher.Dispatcher
import com.klt.orbit.core.common.dispatcher.OrbitDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
  @Provides
  @Dispatcher(OrbitDispatchers.IO)
  fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

  @Provides
  @Dispatcher(OrbitDispatchers.Default)
  fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

  /** Lives as long as the process; a failing child never cancels its siblings. */
  @Provides
  @Singleton
  @ApplicationScope
  fun provideApplicationScope(
    @Dispatcher(OrbitDispatchers.Default) dispatcher: CoroutineDispatcher,
  ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
