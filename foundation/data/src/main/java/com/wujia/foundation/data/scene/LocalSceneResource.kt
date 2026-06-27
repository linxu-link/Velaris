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

import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings

/**
 * 本地场景资源（内部数据模型）。
 *
 * 与 [com.wujia.foundation.model.scene.SceneResource] 的区别：
 * - 使用数据库里保存的 URI / 资源名称，不向上层泄漏 Room Entity
 * - 仅供 data 层内部使用，不对外暴露
 *
 * @param id 场景唯一标识符
 * @param title 场景标题
 * @param subtitle 场景副标题
 * @param category 场景分类
 * @param coverResName 场景封面资源名称
 * @param backgroundResName 背景图片资源名称
 * @param videoUri 视频资源 URI，可为空
 * @param videoVolume 视频音量，默认为 0f
 * @param audioTracks 音频轨道列表
 */
internal data class LocalSceneResource(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SceneCategory = SceneCategory.FOCUS,
    val isPreset: Boolean = false,
    val coverResName: String? = null,
    val backgroundResName: String? = null,
    val backgroundUri: String? = null,
    val videoUri: String? = null,
    val videoVolume: Float = 0f,
    val audioTracks: List<LocalSceneAudioResource> = emptyList(),
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

/**
 * 本地音频资源（内部数据模型）。
 *
 * @param id 音频唯一标识符
 * @param title 音频标题
 * @param uri 音频资源 URI
 * @param volume 播放音量，默认为 1f
 * @param loop 是否循环，默认为 true
 */
internal data class LocalSceneAudioResource(
    val id: String,
    val title: String,
    val uri: String,
    val volume: Float = 1f,
    val loop: Boolean = true,
)

internal data class LocalSceneEditInput(
    val id: String?,
    val title: String,
    val description: String,
    val category: SceneCategory,
    val backgroundResName: String?,
    val backgroundUri: String?,
    val videoUri: String?,
    val videoVolume: Float,
    val audioTracks: List<LocalSceneAudioResource>,
    val controlSettings: SceneControlSettings,
)
