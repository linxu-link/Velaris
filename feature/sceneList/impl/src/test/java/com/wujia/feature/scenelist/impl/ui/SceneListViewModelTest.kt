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
package com.wujia.feature.scenelist.impl.ui

import com.wujia.feature.scenelist.impl.ui.viewmodel.SceneListViewModel
import com.wujia.foundation.domain.scene.DeleteSceneResourceUseCase
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.domain.scene.ReorderSceneResourcesUseCase
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.testing.FakeSceneResourceRepository
import com.wujia.foundation.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * 测试用场景数据（与 sampleSceneListItems 有部分重复，未来可复用）。
     */
    private val scenes = listOf(
        SceneResource(
            id = "focus-1",
            title = "Focus 1",
            subtitle = "First focus scene",
            category = SceneCategory.FOCUS,
            isPreset = true,
        ),
        SceneResource(
            id = "focus-2",
            title = "Focus 2",
            subtitle = "Second focus scene",
            category = SceneCategory.FOCUS,
        ),
        SceneResource(
            id = "sleep-1",
            title = "Sleep 1",
            subtitle = "First sleep scene",
            category = SceneCategory.SLEEP,
        ),
    )

    @Test
    fun onSceneReorder_withCategory_reordersOnlyCurrentCategory() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = SceneListViewModel(
            observeSceneResources = ObserveSceneResourcesUseCase(repository),
            reorderSceneResources = ReorderSceneResourcesUseCase(repository),
            deleteSceneResource = DeleteSceneResourceUseCase(repository),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setCategory(SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onSceneReorder(fromIndex = 0, toIndex = 1)
        advanceUntilIdle()

        assertEquals(SceneCategory.FOCUS, repository.reorderCategory)
        assertEquals(listOf("focus-2", "focus-1"), repository.reorderedIds)
    }

    @Test
    fun onSceneReorder_withoutCategory_reordersGlobalList() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = SceneListViewModel(
            observeSceneResources = ObserveSceneResourcesUseCase(repository),
            reorderSceneResources = ReorderSceneResourcesUseCase(repository),
            deleteSceneResource = DeleteSceneResourceUseCase(repository),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSceneReorder(fromIndex = 0, toIndex = 2)
        advanceUntilIdle()

        assertEquals(null, repository.reorderCategory)
        assertEquals(listOf("focus-2", "sleep-1", "focus-1"), repository.reorderedIds)
    }

    @Test
    fun onSceneDelete_deletesOnlyNonPresetScene() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val viewModel = SceneListViewModel(
            observeSceneResources = ObserveSceneResourcesUseCase(repository),
            reorderSceneResources = ReorderSceneResourcesUseCase(repository),
            deleteSceneResource = DeleteSceneResourceUseCase(repository),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSceneDelete("focus-2")
        advanceUntilIdle()

        assertEquals(listOf("focus-2"), repository.deletedIds)

        viewModel.onSceneDelete("focus-1")
        advanceUntilIdle()

        assertEquals(listOf("focus-2"), repository.deletedIds)
    }
}
