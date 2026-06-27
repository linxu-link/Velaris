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
package com.wujia.feature.scene.impl

import com.wujia.feature.scene.impl.ui.viewmodel.ScenePanelState
import com.wujia.feature.scene.impl.ui.viewmodel.SceneViewModel
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.domain.scene.UpdateSceneAudioVolumeUseCase
import com.wujia.foundation.domain.scene.UpdateSceneControlSettingsUseCase
import com.wujia.foundation.domain.scene.UpdateSceneVideoVolumeUseCase
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.model.scene.SceneResourceRepository
import com.wujia.foundation.testing.FakeSceneResourceRepository
import com.wujia.foundation.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scenes = listOf(
        SceneResource(
            id = "snow-night",
            title = "风 雪 夜 归 人",
            subtitle = "风雪轻落，木屋暖灯",
            backgroundResId = 1,
        ),
        SceneResource(
            id = "morning-mist",
            title = "松 间 晨 雾",
            subtitle = "薄雾穿林，清风醒神",
            backgroundResId = 2,
        ),
    )

    private fun viewModel(repository: SceneResourceRepository) = SceneViewModel(
        observeSceneResources = ObserveSceneResourcesUseCase(repository),
        updateSceneAudioVolume = UpdateSceneAudioVolumeUseCase(repository),
        updateSceneVideoVolume = UpdateSceneVideoVolumeUseCase(repository),
        updateSceneControlSettings = UpdateSceneControlSettingsUseCase(repository),
    )

    @Test
    fun initialState_loadsScenes() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val uiState = viewModel.uiState.value

        assertTrue(uiState.scenes.isNotEmpty())
        assertEquals(0, uiState.currentScenePage)
        assertEquals(uiState.scenes.first(), uiState.currentScene)
    }

    @Test
    fun onScenePageChange_coercesPageIntoSceneRange() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val lastIndex = viewModel.uiState.value.scenes.lastIndex

        viewModel.onScenePageChange(lastIndex + 10)
        advanceUntilIdle()
        assertEquals(lastIndex, viewModel.uiState.value.currentScenePage)

        viewModel.onScenePageChange(-1)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.currentScenePage)
    }

    @Test
    fun visibilityEvents_updateUiState() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSoundDialogVisibilityChange(true)
        viewModel.onPanelChange(ScenePanelState.CONTROL)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showSoundDialog)
        assertTrue(viewModel.uiState.value.showControlPanel)

        viewModel.onSoundDialogVisibilityChange(false)
        viewModel.onPanelChange(ScenePanelState.NONE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showSoundDialog)
        assertFalse(viewModel.uiState.value.showControlPanel)
    }

    @Test
    fun reorderingScenes_keepsCurrentSceneById() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onScenePageChange(1)
        advanceUntilIdle()
        assertEquals("morning-mist", viewModel.uiState.value.currentScene?.id)
        assertEquals(1, viewModel.uiState.value.currentScenePage)

        val current = repository.observeSceneResources().first()
        repository.updateScenes(current.reversed())
        advanceUntilIdle()

        assertEquals("morning-mist", viewModel.uiState.value.currentScene?.id)
        assertEquals(0, viewModel.uiState.value.currentScenePage)
    }

    @Test
    fun onGuideCompleted_setsGuideCompletedForCurrentScene() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasCompletedGuide)

        viewModel.onGuideCompleted()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCompletedGuide)
    }

    @Test
    fun onGuideCompleted_noCurrentScene_doesNothing() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = emptyList())
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // 不应抛异常
        viewModel.onGuideCompleted()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasCompletedGuide)
    }

    @Test
    fun onGuideCompleted_alreadyCompleted_doesNotUpdateAgain() = runTest {
        val completedScenes = scenes.map {
            it.copy(controlSettings = it.controlSettings.copy(guideCompleted = true))
        }
        val repository = FakeSceneResourceRepository(initialScenes = completedScenes)
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCompletedGuide)

        // 重复调用不应抛异常
        viewModel.onGuideCompleted()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCompletedGuide)
    }
}
