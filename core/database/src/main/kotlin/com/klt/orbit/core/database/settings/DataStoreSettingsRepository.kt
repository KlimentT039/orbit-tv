package com.klt.orbit.core.database.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.UserSettings
import com.klt.orbit.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DataStoreSettingsRepository
  @Inject
  constructor(
    private val dataStore: DataStore<Preferences>,
  ) : SettingsRepository {
    private val defaults = UserSettings()

    override val settings: Flow<UserSettings> =
      dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> UserSettings(autoplayNext = preferences[AUTOPLAY_NEXT] ?: defaults.autoplayNext) }
        .distinctUntilChanged()

    override suspend fun setAutoplayNext(enabled: Boolean): Outcome<Unit> =
      try {
        dataStore.edit { it[AUTOPLAY_NEXT] = enabled }
        Outcome.Success(Unit)
      } catch (_: IOException) {
        Outcome.Failure(DataError.Storage)
      }

    private companion object {
      val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
    }
  }
