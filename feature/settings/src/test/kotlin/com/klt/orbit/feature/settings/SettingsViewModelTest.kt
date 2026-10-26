package com.klt.orbit.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.klt.orbit.core.common.app.AppInfo
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.model.UserSettings
import com.klt.orbit.domain.testing.FakePlaybackProgressRepository
import com.klt.orbit.domain.testing.FakeSettingsRepository
import com.klt.orbit.domain.testing.MainDispatcherRule
import com.klt.orbit.domain.testing.TestData
import com.klt.orbit.domain.usecase.ClearWatchHistoryUseCase
import com.klt.orbit.domain.usecase.ObserveSettingsUseCase
import com.klt.orbit.domain.usecase.SetAutoplayNextUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private val settings = FakeSettingsRepository(UserSettings(autoplayNext = true))
  private val progress = FakePlaybackProgressRepository()

  private fun TestScope.viewModel(): SettingsViewModel =
    SettingsViewModel(
      ObserveSettingsUseCase(settings),
      SetAutoplayNextUseCase(settings),
      ClearWatchHistoryUseCase(progress),
      AppInfo(versionName = "1.2.3", versionCode = 7),
    ).also { viewModel ->
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

  private val SettingsViewModel.content get() = uiState.value as SettingsUiState.Content

  @Test
  fun `shows the saved settings and the app version`() =
    runTest {
      val viewModel = viewModel()

      assertThat(viewModel.content.autoplayNext).isTrue()
      assertThat(viewModel.content.versionName).isEqualTo("1.2.3")
      assertThat(viewModel.content.isConfirmingClear).isFalse()
    }

  @Test
  fun `toggling autoplay saves it`() =
    runTest {
      val viewModel = viewModel()

      viewModel.onAutoplayNextChange(false)

      assertThat(settings.settings.first().autoplayNext).isFalse()
      assertThat(viewModel.content.autoplayNext).isFalse()
    }

  @Test
  fun `clearing history asks for confirmation first`() =
    runTest {
      progress.saveProgress(TestData.video(), positionMs = 60_000, durationMs = 600_000)
      val viewModel = viewModel()

      viewModel.onClearHistoryClick()
      assertThat(viewModel.content.isConfirmingClear).isTrue()

      viewModel.onClearHistoryDismiss()
      assertThat(viewModel.content.isConfirmingClear).isFalse()
      assertThat(progress.observeWatchHistory().first()).isNotEmpty()
    }

  @Test
  fun `confirming clears the watch history`() =
    runTest {
      progress.saveProgress(TestData.video(), positionMs = 60_000, durationMs = 600_000)
      val viewModel = viewModel()
      viewModel.onClearHistoryClick()

      viewModel.events.test {
        viewModel.onClearHistoryConfirm()
        assertThat(awaitItem()).isEqualTo(SettingsEvent.HistoryCleared)
      }
      assertThat(progress.observeWatchHistory().first()).isEmpty()
      assertThat(viewModel.content.isConfirmingClear).isFalse()
    }

  @Test
  fun `a failed clear is reported`() =
    runTest {
      progress.failWith = DataError.Storage
      val viewModel = viewModel()
      viewModel.onClearHistoryClick()

      viewModel.events.test {
        viewModel.onClearHistoryConfirm()
        assertThat(awaitItem()).isEqualTo(SettingsEvent.ShowError(DataError.Storage))
      }
    }
}
