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
package com.wujia.feature.scenecontrol.impl.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.WaterDrop
import com.wujia.foundation.model.soundcontrol.SoundControlItem

internal fun sampleSoundItems(): List<SoundControlItem> = listOf(
    SoundControlItem("雪声", Icons.Outlined.GraphicEq, 0.8f),
    SoundControlItem("雨声", Icons.Outlined.WaterDrop, 0.55f),
)

internal fun sampleLightItems(): List<SoundControlItem> = listOf(
    SoundControlItem("亮度", Icons.Outlined.GraphicEq, 0.8f),
    SoundControlItem("氛围", Icons.Outlined.Air, 0.6f),
)
