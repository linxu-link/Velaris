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
package com.wujia.feature.settings.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.settings.impl.ui.SettingsScreen
import com.wujia.foundation.designsystem.panel.SwipeUpPanel
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.player.ProvideVelarisPlayerConfig
import com.wujia.foundation.player.VelarisPlayerConfig

/**
 * 设置面板的公共入口。
 * 通常由 scene 模块作为 overlay 嵌入使用。
 *
 * - 通过 ProvideVelarisPlayerConfig 向子树注入播放器配置（影响性能相关行为）。
 * - SwipeUpPanel 提供上滑手势面板体验。
 */
@Composable
fun SettingsPanel(
    modifier: Modifier = Modifier,
    showPanel: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    panelHeight: Dp = 520.dp,
    edgePadding: Dp = 8.dp,
    playerConfig: VelarisPlayerConfig = VelarisPlayerConfig.Balanced,
) {
    // 向整个设置面板子树提供播放器配置（PowerSaver/Balanced/Quality 会影响缓存策略等）
    ProvideVelarisPlayerConfig(playerConfig) {
        SwipeUpPanel(
            visible = showPanel,
            onVisibleChange = onVisibleChange,
            panelHeight = panelHeight,
            edgePadding = edgePadding,
            modifier = modifier,
        ) {
            SettingsScreen(
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@LandscapePreviews
@Composable
private fun PreviewSettingsPanel() {
    ProvideVelarisTheme {
        SwipeUpPanel(
            visible = true,
            onVisibleChange = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("设置面板")
            }
        }
    }
}
