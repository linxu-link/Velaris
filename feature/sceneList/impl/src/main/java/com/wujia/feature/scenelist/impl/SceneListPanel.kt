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
package com.wujia.feature.scenelist.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.scenelist.impl.ui.SceneListScreen
import com.wujia.foundation.designsystem.panel.SwipeUpPanel
import com.wujia.foundation.model.scene.SceneCategory

/**
 * 场景列表面板（包裹在 SwipeUpPanel 中）。
 * 主要供 scene 模块作为 overlay 嵌入使用。
 *
 * 参数：
 * - category: 可指定初始分类（FOCUS/SLEEP）
 * - onAddScene: 添加场景时跳转编辑（仅依赖 sceneEdit api）
 */
@Composable
fun SceneListPanel(
    modifier: Modifier = Modifier,
    showPanel: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    category: SceneCategory? = null,
    onAddScene: (SceneCategory?) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSceneClick: (String, SceneCategory?) -> Unit = { _, _ -> },
    panelHeight: Dp = 520.dp,
    edgePadding: Dp = 8.dp,
) {
    SwipeUpPanel(
        visible = showPanel,
        onVisibleChange = onVisibleChange,
        panelHeight = panelHeight,
        edgePadding = edgePadding,
        modifier = modifier,
    ) {
        SceneListScreen(
            category = category,
            onBackClick = { onVisibleChange(false) },
            onAddScene = onAddScene,
            onOpenSettings = onOpenSettings,
            onSceneClick = onSceneClick,
        )
    }
}
