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
package com.wujia.feature.scene.impl.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import com.wujia.foundation.designsystem.tab.SceneTabItem
import com.wujia.foundation.model.R
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneResource

internal fun sceneCategoryTabs(): List<SceneTabItem> = listOf(
    SceneTabItem(
        text = "专注",
        icon = Icons.Outlined.EnergySavingsLeaf,
    ),
    SceneTabItem(
        text = "助眠",
        icon = Icons.Outlined.DarkMode,
    ),
)

internal fun previewSceneResources(): List<SceneResource> = listOf(
    SceneResource(
        id = "snow-night",
        title = "风 雪 夜 归 人",
        subtitle = "风雪轻落，木屋暖灯",
        backgroundResId = R.drawable.bg_snow_night,
        audioTracks = listOf(
            SceneAudioResource(
                id = "wind",
                title = "风声",
                uri = "android.resource://preview/${R.raw.piano_1}",
            ),
        ),
    ),
)

internal fun sampleAudioTracks(): List<SceneAudioResource> = listOf(
    SceneAudioResource(id = "snow", title = "雪声", uri = "", volume = 0.8f),
    SceneAudioResource(id = "rain", title = "雨声", uri = "", volume = 0.55f),
)
