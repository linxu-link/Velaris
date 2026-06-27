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
package com.wujia.foundation.testing

import com.wujia.foundation.model.scene.EditableScene
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneEditInput
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.model.scene.SceneResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 共享的 [SceneResourceRepository] 测试替身。
 *
 * @param initialScenes 初始场景列表，用于 [observeSceneResources] 和 [getSceneResource]
 * @param editableScene 返回给 [getEditableScene] 的可编辑场景
 * @param savedId [saveSceneEdit] 成功时返回的 ID
 * @param saveFailure 若非 null，[saveSceneEdit] 会抛出此异常
 */
class FakeSceneResourceRepository(
    initialScenes: List<SceneResource> = emptyList(),
    private val editableScene: EditableScene? = null,
    private val savedId: String = "saved-scene",
    private val saveFailure: Throwable? = null,
) : SceneResourceRepository {

    private val scenes = MutableStateFlow(initialScenes)

    /** [saveSceneEdit] 最近一次接收到的输入 */
    var savedInput: SceneEditInput? = null
        private set

    /** [deleteSceneResource] 被调用时记录的 ID 列表 */
    val deletedIds = mutableListOf<String>()

    /** [reorderSceneResources] 最近一次接收到的 category */
    var reorderCategory: SceneCategory? = null
        private set

    /** [reorderSceneResources] 最近一次接收到的 orderedIds */
    var reorderedIds: List<String> = emptyList()
        private set

    fun updateScenes(newScenes: List<SceneResource>) {
        scenes.value = newScenes
    }

    override fun observeSceneResources(): Flow<List<SceneResource>> = scenes

    override suspend fun getSceneResource(id: String): SceneResource? = scenes.value.firstOrNull { it.id == id }

    override suspend fun getEditableScene(id: String): EditableScene? = editableScene

    override suspend fun saveSceneEdit(input: SceneEditInput): String {
        saveFailure?.let { throw it }
        savedInput = input
        return savedId
    }

    override suspend fun deleteSceneResource(id: String) {
        deletedIds += id
    }

    override suspend fun updateSceneControlSettings(sceneId: String, settings: SceneControlSettings) {
        scenes.value = scenes.value.map {
            if (it.id == sceneId) it.copy(controlSettings = settings) else it
        }
    }

    override suspend fun updateSceneAudioVolume(sceneId: String, audioId: String, volume: Float) = Unit

    override suspend fun updateSceneVideoVolume(sceneId: String, volume: Float) = Unit

    override suspend fun reorderSceneResources(category: SceneCategory?, orderedIds: List<String>) {
        reorderCategory = category
        reorderedIds = orderedIds
    }
}
