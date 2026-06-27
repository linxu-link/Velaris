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
package com.wujia.feature.sceneedit.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.sceneedit.impl.ui.SceneEditScreen
import com.wujia.foundation.designsystem.panel.SwipeUpPanel
import com.wujia.foundation.model.scene.SceneCategory

/**
 * 场景编辑面板（材质 + 声音 + 粒子 + 预览保存）。
 *
 * 包裹在 [SwipeUpPanel] 中，作为覆盖层嵌入主场景页（由 scene impl 宿主调用）。
 * 这是 feature 间集成的唯一公开入口（api/impl 拆分下的例外）。
 *
 * 职责：
 * - 响应 showPanel 控制显隐
 * - 透传 sceneId/category 给内部 Screen
 * - 保存成功后自动关闭面板
 */
@Composable
fun SceneEditPanel(
    modifier: Modifier = Modifier,
    showPanel: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    sceneId: String? = null,
    category: SceneCategory? = null,
    panelHeight: Dp = 520.dp,
    edgePadding: Dp = 8.dp,
) {
    SwipeUpPanel(
        visible = showPanel,
        onVisibleChange = onVisibleChange,
        debugName = "scene_edit",
        panelHeight = panelHeight,
        edgePadding = edgePadding,
        modifier = modifier,
    ) {
        SceneEditScreen(
            sceneId = sceneId,
            category = category,
            isActive = showPanel,
            onSaved = { onVisibleChange(false) },
        )
    }
}
