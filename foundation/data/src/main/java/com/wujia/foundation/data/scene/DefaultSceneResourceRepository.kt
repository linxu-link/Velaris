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
package com.wujia.foundation.data.scene

import android.content.Context
import com.wujia.foundation.model.scene.EditableScene
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneEditAudio
import com.wujia.foundation.model.scene.SceneEditInput
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.model.scene.SceneResourceRepository
import com.wujia.foundation.model.scene.SceneVideoResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [SceneResourceRepository] 的默认实现。
 *
 * 负责：
 * 1. 调用本地数据源获取场景数据
 * 2. 将内部数据模型（LocalSceneResource）转换为对外暴露的领域模型（SceneResource）
 * 3. 将本地资源名称解析为 UI 层可使用的资源 ID
 *
 * 这是 Clean Architecture 中 data 层的典型实现：
 * - 输入：内部数据模型
 * - 输出：领域模型（domain model）
 */
class DefaultSceneResourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataSource: SceneResourceLocalDataSource,
) : SceneResourceRepository {

    /**
     * 观察所有场景资源。
     */
    override fun observeSceneResources(): Flow<List<SceneResource>> = localDataSource.observeSceneResources()
        .map { scenes -> scenes.map { it.asExternalModel() } }
        .catch {
            emit(emptyList())
        }

    /**
     * 根据 ID 获取单个场景资源。
     */
    override suspend fun getSceneResource(id: String): SceneResource? = localDataSource.getSceneResources()
        .firstOrNull { it.id == id }
        ?.asExternalModel()

    override suspend fun getEditableScene(id: String): EditableScene? =
        localDataSource.getEditableScene(id)?.asEditableScene()

    override suspend fun saveSceneEdit(input: SceneEditInput): String =
        localDataSource.saveSceneEdit(input.asLocalInput())

    override suspend fun deleteSceneResource(id: String) {
        localDataSource.deleteSceneResource(id)
    }

    override suspend fun updateSceneControlSettings(sceneId: String, settings: SceneControlSettings) {
        localDataSource.updateSceneControlSettings(
            sceneId = sceneId,
            settings = settings,
        )
    }

    override suspend fun updateSceneAudioVolume(sceneId: String, audioId: String, volume: Float) {
        localDataSource.updateSceneAudioVolume(
            sceneId = sceneId,
            audioId = audioId,
            volume = volume,
        )
    }

    override suspend fun updateSceneVideoVolume(sceneId: String, volume: Float) {
        localDataSource.updateSceneVideoVolume(
            sceneId = sceneId,
            volume = volume,
        )
    }

    override suspend fun reorderSceneResources(category: SceneCategory?, orderedIds: List<String>) {
        localDataSource.reorderSceneResources(
            category = category,
            orderedIds = orderedIds,
        )
    }

    // ============================================================
    // 内部转换方法：将 LocalSceneResource 映射为 SceneResource
    // ============================================================

    /**
     * 将本地场景资源转换为对外暴露的领域模型。
     *
     * 转换内容：
     * - 背景资源名称解析为资源 ID
     * - 视频和音频 URI 保持为播放器可消费的格式
     * - 音频轨道列表分别转换
     */
    private fun LocalSceneResource.asExternalModel(): SceneResource = SceneResource(
        id = id,
        title = title,
        subtitle = subtitle,
        category = category,
        isPreset = isPreset,
        coverResId = coverResName?.toDrawableResourceId() ?: backgroundResName?.toDrawableResourceId(),
        coverUri = backgroundUri,
        backgroundResId = backgroundResName?.toDrawableResourceId(),
        backgroundUri = backgroundUri,
        video = videoUri?.let { SceneVideoResource(uri = it, volume = videoVolume) },
        audioTracks = audioTracks.map { it.asExternalModel() },
        controlSettings = controlSettings,
    )

    /** 将本地音频资源转换为领域模型 */
    private fun LocalSceneAudioResource.asExternalModel(): SceneAudioResource = SceneAudioResource(
        id = id,
        title = title,
        uri = uri,
        volume = volume,
        loop = loop,
    )

    private fun LocalSceneResource.asEditableScene(): EditableScene = EditableScene(
        id = id,
        title = title,
        description = subtitle,
        category = category,
        backgroundResName = backgroundResName,
        backgroundUri = backgroundUri,
        videoUri = videoUri,
        videoVolume = videoVolume,
        audioTracks = audioTracks.map { it.asEditAudio() },
        controlSettings = controlSettings,
    )

    private fun LocalSceneAudioResource.asEditAudio(): SceneEditAudio = SceneEditAudio(
        id = id,
        title = title,
        uri = uri,
        volume = volume,
        loop = loop,
    )

    private fun SceneEditInput.asLocalInput(): LocalSceneEditInput = LocalSceneEditInput(
        id = id,
        title = title,
        description = description,
        category = category,
        backgroundResName = backgroundResName,
        backgroundUri = backgroundUri,
        videoUri = videoUri,
        videoVolume = videoVolume,
        audioTracks = audioTracks.map { it.asLocalAudio() },
        controlSettings = controlSettings,
    )

    private fun SceneEditAudio.asLocalAudio(): LocalSceneAudioResource = LocalSceneAudioResource(
        id = id,
        title = title,
        uri = uri,
        volume = volume,
        loop = loop,
    )

    private fun String.toDrawableResourceId(): Int? = context.resources
        .getIdentifier(this, "drawable", context.packageName)
        .takeIf { it != 0 }
}
