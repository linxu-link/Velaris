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
package com.wujia.foundation.domain

import com.wujia.foundation.domain.scene.GetSceneResourceUseCase
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.testing.FakeSceneResourceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SceneResourceUseCaseTest {
    private val scenes = listOf(
        SceneResource(
            id = "snow-night",
            title = "风 雪 夜 归 人",
            subtitle = "风雪轻落，木屋暖灯",
            backgroundResId = 1,
        ),
    )

    @Test
    fun observeSceneResourcesUseCase_returnsRepositoryScenes() = runTest {
        val repository = FakeSceneResourceRepository(initialScenes = scenes)
        val result = ObserveSceneResourcesUseCase(repository)().first()

        assertEquals(scenes.size, result.size)
        assertEquals(scenes.first().id, result.first().id)
    }

    @Test
    fun getSceneResourceUseCase_returnsMatchingScene() = runTest {
        val scene = GetSceneResourceUseCase(FakeSceneResourceRepository(initialScenes = scenes))("snow-night")

        assertEquals("风 雪 夜 归 人", scene?.title)
    }

    @Test
    fun getSceneResourceUseCase_returnsNullForMissingScene() = runTest {
        val scene = GetSceneResourceUseCase(FakeSceneResourceRepository(initialScenes = scenes))("missing")

        assertNull(scene)
    }
}
