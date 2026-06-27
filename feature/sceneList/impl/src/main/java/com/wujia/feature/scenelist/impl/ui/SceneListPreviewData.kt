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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Spa
import com.wujia.feature.scenelist.impl.entity.SceneListItem
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.model.R

/**
 * 示例数据（用于 Preview 和测试）。
 * 注意：与 VMTest 中硬编码的 scenes 有重复定义，未来可统一。
 */
internal fun sampleSceneListItems(): List<SceneListItem> = listOf(
    SceneListItem(
        id = "snow-night",
        title = "雪照归灯",
        description = "风雪掠过山前，窗内灯火把夜色按住",
        icon = Icons.Outlined.AcUnit,
        accent = VelarisColor.PaletteIce,
        coverResId = R.drawable.bg_snow_night,
    ),
    SceneListItem(
        id = "forest",
        title = "雾入松窗",
        description = "晨雾沿湖心缓缓漫开，松影与木屋一起沉进微白天光",
        icon = Icons.Outlined.Forest,
        accent = VelarisColor.PaletteForest,
        coverResId = R.drawable.bg_morning_mist,
    ),
    SceneListItem(
        id = "tea",
        title = "雨收长巷",
        description = "雨后的石路还亮着散光，巷尾云雾迟迟不散",
        icon = Icons.Outlined.Coffee,
        accent = VelarisColor.PaletteAmber,
        coverResId = R.drawable.bg_after_rain,
    ),
    SceneListItem(
        id = "sleep",
        title = "月泊寒湖",
        description = "月光泊在湖心，岸灯的晕薄得快要融进水里",
        icon = Icons.Outlined.DarkMode,
        accent = VelarisColor.PaletteTwilight,
        coverResId = R.drawable.bg_under_moon,
    ),
    SceneListItem(
        id = "breath",
        title = "风过檐铃",
        description = "风掠檐角，铃声稀疏响起，把午后安静拉得更长",
        icon = Icons.Outlined.Spa,
        accent = VelarisColor.PaletteSpa,
        coverResId = R.drawable.bg_after_rain,
    ),
)
