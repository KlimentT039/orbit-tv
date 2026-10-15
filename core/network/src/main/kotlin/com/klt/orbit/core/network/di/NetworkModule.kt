package com.klt.orbit.core.network.di

import android.content.Context
import com.klt.orbit.core.network.BuildConfig
import com.klt.orbit.core.network.NasaApi
import com.klt.orbit.core.network.NetworkJson
import com.klt.orbit.core.network.repository.NasaVideoRepository
import com.klt.orbit.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
  private const val HTTP_CACHE_SIZE_BYTES = 20L * 1024 * 1024
  private const val CONNECT_TIMEOUT_SECONDS = 15L
  private const val READ_TIMEOUT_SECONDS = 20L

  @Provides
  @Singleton
  fun provideJson(): Json = NetworkJson

  /** Shared client: one connection pool and disk cache for the API (and, later, images). */
  @Provides
  @Singleton
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
  ): OkHttpClient =
    OkHttpClient
      .Builder()
      .cache(Cache(File(context.cacheDir, "http"), HTTP_CACHE_SIZE_BYTES))
      .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .apply {
        if (BuildConfig.DEBUG) {
          addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }
      }.build()

  @Provides
  @Singleton
  fun provideNasaApi(
    client: OkHttpClient,
    json: Json,
  ): NasaApi = createNasaApi(NasaApi.BASE_URL, client, json)
}

internal fun createNasaApi(
  baseUrl: String,
  client: OkHttpClient,
  json: Json,
): NasaApi =
  Retrofit
    .Builder()
    .baseUrl(baseUrl)
    .client(client)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(NasaApi::class.java)

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkBindsModule {
  @Binds
  fun bindVideoRepository(repository: NasaVideoRepository): VideoRepository
}
