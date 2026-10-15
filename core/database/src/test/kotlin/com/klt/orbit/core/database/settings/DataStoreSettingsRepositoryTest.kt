package com.klt.orbit.core.database.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.model.UserSettings
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSettingsRepositoryTest {
  @get:Rule
  val tmp = TemporaryFolder()

  private fun TestScope.repository() =
    DataStoreSettingsRepository(
      PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { tmp.newFile("settings.preferences_pb").also { it.delete() } },
      ),
    )

  @Test
  fun `defaults to autoplay on`() =
    runTest {
      repository().settings.test {
        assertThat(awaitItem()).isEqualTo(UserSettings(autoplayNext = true))
      }
    }

  @Test
  fun `persists the autoplay toggle`() =
    runTest {
      val repository = repository()
      repository.settings.test {
        assertThat(awaitItem().autoplayNext).isTrue()

        assertThat(repository.setAutoplayNext(false)).isEqualTo(Outcome.Success(Unit))

        assertThat(awaitItem().autoplayNext).isFalse()
      }
    }
}
