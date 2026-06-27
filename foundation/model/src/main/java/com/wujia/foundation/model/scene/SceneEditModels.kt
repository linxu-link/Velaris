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

data class EditableScene(
    val id: String,
    val title: String,
    val description: String,
    val category: SceneCategory,
    val backgroundResName: String?,
    val backgroundUri: String?,
    val videoUri: String?,
    val videoVolume: Float = DEFAULT_SCENE_VIDEO_VOLUME,
    val audioTracks: List<SceneEditAudio>,
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

data class SceneEditInput(
    val id: String?,
    val title: String,
    val description: String,
    val category: SceneCategory,
    val backgroundResName: String?,
    val backgroundUri: String?,
    val videoUri: String?,
    val videoVolume: Float = DEFAULT_SCENE_VIDEO_VOLUME,
    val audioTracks: List<SceneEditAudio>,
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

data class SceneEditAudio(
    val id: String,
    val title: String,
    val uri: String,
    val volume: Float = DEFAULT_SCENE_AUDIO_VOLUME,
    val loop: Boolean = true,
)
