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
package com.wujia.foundation.model.scene

import kotlinx.coroutines.flow.Flow

/**
 * 场景资源仓库接口，定义在 model 层（属于领域驱动设计中的"端口"）。
 *
 * 该接口由 [com.wujia.foundation.data.scene.DefaultSceneResourceRepository] 实现，
 * 领域层通过 Use Case 调用此接口，无需关心数据来源。
 */
interface SceneResourceRepository {

    /**
     * 观察所有场景资源的实时更新流。
     * @return 一个 [Flow]， emits 场景资源列表
     */
    fun observeSceneResources(): Flow<List<SceneResource>>

    /**
     * 根据 ID 获取单个场景资源。
     * @param id 场景资源的唯一标识符
     * @return 对应的场景资源，如果不存在则返回 null
     */
    suspend fun getSceneResource(id: String): SceneResource?

    /**
     * 获取可编辑的场景原始数据。
     */
    suspend fun getEditableScene(id: String): EditableScene?

    /**
     * 创建或更新场景编辑结果，并返回保存后的场景 ID。
     */
    suspend fun saveSceneEdit(input: SceneEditInput): String

    /**
     * 删除非预制场景。预制场景由数据层保护，不会被删除。
     */
    suspend fun deleteSceneResource(id: String)

    /**
     * 保存当前场景控制面板的画面、定时与智能淡出配置。
     */
    suspend fun updateSceneControlSettings(sceneId: String, settings: SceneControlSettings)

    /**
     * 保存当前场景混音里单个音轨的音量。
     */
    suspend fun updateSceneAudioVolume(sceneId: String, audioId: String, volume: Float)

    /**
     * 保存当前场景视频的音量。
     */
    suspend fun updateSceneVideoVolume(sceneId: String, volume: Float)

    /**
     * 保存场景资源的展示顺序。
     *
     * @param category 为 null 时更新全局顺序；否则只更新该分类内的展示顺序。
     * @param orderedIds 按展示顺序排列的场景 ID。
     */
    suspend fun reorderSceneResources(category: SceneCategory?, orderedIds: List<String>)
}
