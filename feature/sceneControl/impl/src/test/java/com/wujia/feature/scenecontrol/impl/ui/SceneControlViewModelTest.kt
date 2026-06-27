/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.feature.scenecontrol.impl.ui

import com.wujia.feature.scenecontrol.impl.ui.viewmodel.ControlEvent
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlClock
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlViewModel
import com.wujia.foundation.domain.scene.UpdateSceneControlSettingsUseCase
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneResourceRepository
import com.wujia.foundation.testing.FakeSceneResourceRepository
import com.wujia.foundation.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneControlViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        updateSceneControlSettings: UpdateSceneControlSettingsUseCase =
            UpdateSceneControlSettingsUseCase(FakeSceneResourceRepository()),
        clock: SceneControlClock = FakeSceneControlClock(),
    ) = SceneControlViewModel(
        updateSceneControlSettings = updateSceneControlSettings,
        sceneControlClock = clock,
    )

    @Test
    fun onSceneChanged_setsVisualControlsAndTimerDuration() = runTest {
        val viewModel = createViewModel()

        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.7f,
            darkness = 0.3f,
            timerDurationMillis = 15 * 60 * 1000L,
            showCountdownClock = false,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.7f, state.brightness, 0.001f)
        assertEquals(0.3f, state.darkness, 0.001f)
        assertEquals(15 * 60 * 1000L, state.timerDurationMillis)
        assertEquals(0, state.timerSelectedOption)
        assertFalse(state.showCountdownClock)
    }

    @Test
    fun onTimerOptionChange_switchesPresetDuration() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onTimerOptionChange(0)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.timerSelectedOption)
        assertEquals(15 * 60 * 1000L, viewModel.uiState.value.timerDurationMillis)
        assertFalse(viewModel.uiState.value.isTimerRunning)

        viewModel.onTimerOptionChange(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.timerSelectedOption)
        assertEquals(25 * 60 * 1000L, viewModel.uiState.value.timerDurationMillis)
    }

    @Test
    fun onTimerOptionChange_whenPlaying_startsCountdownImmediately() = runTest {
        val clock = FakeSceneControlClock()
        val viewModel = createViewModel(clock = clock)
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onPlaybackStarted()
        assertTrue(viewModel.uiState.value.isTimerRunning)

        viewModel.onTimerOptionChange(0, resumeIfPlaying = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isTimerRunning)
        assertEquals(15 * 60 * 1000L, state.timerDurationMillis)
        assertTrue(state.timerEndTimestampMillis > 0L)
    }

    @Test
    fun onTimerOptionChange_customOption_showsDialog() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onTimerOptionChange(3)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCustomTimerDialog)
    }

    @Test
    fun onCustomTimerConfirm_setsDurationAndClosesDialog() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onCustomTimerConfirm(1, 30)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.timerSelectedOption)
        assertEquals(90 * 60 * 1000L, state.timerDurationMillis)
        assertFalse(state.showCustomTimerDialog)
    }

    @Test
    fun onCustomTimerConfirm_whenPlaying_startsCountdownImmediately() = runTest {
        val clock = FakeSceneControlClock()
        val viewModel = createViewModel(clock = clock)
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onPlaybackStarted()
        assertTrue(viewModel.uiState.value.isTimerRunning)

        viewModel.onCustomTimerConfirm(0, 30, resumeIfPlaying = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isTimerRunning)
        assertEquals(30 * 60 * 1000L, state.timerDurationMillis)
        assertTrue(state.timerEndTimestampMillis > 0L)
    }

    @Test
    fun onCustomTimerConfirm_zeroDuration_doesNothing() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        val stateBefore = viewModel.uiState.value

        viewModel.onCustomTimerConfirm(0, 0)
        advanceUntilIdle()

        assertEquals(stateBefore.timerDurationMillis, viewModel.uiState.value.timerDurationMillis)
    }

    @Test
    fun onTimerToggle_startsAndPausesTimer() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onTimerToggle()

        assertTrue(viewModel.uiState.value.isTimerRunning)
        assertTrue(viewModel.uiState.value.timerEndTimestampMillis > 0)

        viewModel.onTimerToggle()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(0L, viewModel.uiState.value.timerEndTimestampMillis)
    }

    @Test
    fun onPlaybackStarted_startsCountdown() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onPlaybackStarted()

        assertTrue(viewModel.uiState.value.isTimerRunning)

        viewModel.onPlaybackPaused()
        advanceUntilIdle()
    }

    @Test
    fun onPlaybackPaused_pausesRunningTimer() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onPlaybackStarted()
        assertTrue(viewModel.uiState.value.isTimerRunning)

        viewModel.onPlaybackPaused()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(0L, viewModel.uiState.value.timerEndTimestampMillis)
        assertTrue(viewModel.uiState.value.timerDurationMillis > 0)
    }

    @Test
    fun startCountdown_timerExpires_sendsEvent() = runTest {
        val clock = FakeSceneControlClock()
        val viewModel = createViewModel(clock = clock)
        viewModel.onCustomTimerConfirm(0, 1)
        advanceUntilIdle()

        viewModel.onTimerToggle()

        val events = async { viewModel.events.first() }

        clock.currentTimeMillis = 60_000L
        runCurrent()
        advanceUntilIdle()

        val event = events.await()
        assertTrue(event is ControlEvent.TimerExpired)

        val state = viewModel.uiState.value
        assertFalse(state.isTimerRunning)
        assertEquals(0L, state.timerRemainingMillis)
    }

    @Test
    fun onShowCountdownClockChange_persistsSettings() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onShowCountdownClockChange(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCountdownClock)
    }

    @Test
    fun onCountdownClockPositionChange_persistsSettings() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onCountdownClockPositionChange(SceneCountdownClockPosition.TopEnd)
        advanceUntilIdle()

        assertEquals(
            SceneCountdownClockPosition.TopEnd,
            viewModel.uiState.value.countdownClockPosition,
        )
    }

    @Test
    fun onSceneChanged_sameScene_timerNotRunning_syncsTimerFields() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.8f,
            darkness = 0.4f,
            timerDurationMillis = 25 * 60 * 1000L,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.8f, state.brightness, 0.001f)
        assertEquals(0.4f, state.darkness, 0.001f)
        assertEquals(25 * 60 * 1000L, state.timerDurationMillis)
        assertEquals(1, state.timerSelectedOption)
    }

    @Test
    fun onSceneChanged_sameScene_timerRunning_preservesTimerFields() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onTimerOptionChange(1)
        advanceUntilIdle()
        viewModel.onTimerToggle()
        val runningOption = viewModel.uiState.value.timerSelectedOption
        val runningDuration = viewModel.uiState.value.timerDurationMillis

        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.8f,
            darkness = 0.4f,
            timerDurationMillis = 15 * 60 * 1000L,
            showCountdownClock = true,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.8f, state.brightness, 0.001f)
        assertEquals(0.4f, state.darkness, 0.001f)
        assertTrue(state.isTimerRunning)
        assertEquals(runningOption, state.timerSelectedOption)
        assertEquals(runningDuration, state.timerDurationMillis)
        assertTrue(state.showCountdownClock)
        assertEquals(
            SceneCountdownClockPosition.Center,
            state.countdownClockPosition,
        )
    }

    @Test
    fun onVisualControlItemValueChange_updatesOnlyTargetItem() = runTest {
        val viewModel = createViewModel()
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.2f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onVisualControlItemValueChange(index = 0, value = 0.8f)
        advanceUntilIdle()

        assertEquals(0.8f, viewModel.uiState.value.brightness, 0.001f)
        assertEquals(0.2f, viewModel.uiState.value.darkness, 0.001f)

        viewModel.onVisualControlItemValueChange(index = 1, value = 0.4f)
        advanceUntilIdle()

        assertEquals(0.8f, viewModel.uiState.value.brightness, 0.001f)
        assertEquals(0.4f, viewModel.uiState.value.darkness, 0.001f)
    }

    @Test
    fun persistControlSettings_failure_doesNotCrash() = runTest {
        val failingUseCase = UpdateSceneControlSettingsUseCase(
            object : SceneResourceRepository by FakeSceneResourceRepository() {
                override suspend fun updateSceneControlSettings(
                    sceneId: String,
                    settings: SceneControlSettings,
                ): Unit = throw RuntimeException("DB error")
            },
        )
        val viewModel = createViewModel(failingUseCase)
        viewModel.onSceneChanged(
            sceneId = "scene-1",
            brightness = 0.5f,
            darkness = 0.5f,
            timerDurationMillis = 45 * 60 * 1000L,
        )
        advanceUntilIdle()

        viewModel.onShowCountdownClockChange(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCountdownClock)
    }
}

private class FakeSceneControlClock(var currentTimeMillis: Long = 0L) : SceneControlClock() {
    override fun currentTimeMillis(): Long = currentTimeMillis
}
