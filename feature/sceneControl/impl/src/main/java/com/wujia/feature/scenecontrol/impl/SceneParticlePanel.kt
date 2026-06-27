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
package com.wujia.feature.scenecontrol.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.scenecontrol.impl.ui.SceneParticleScreen
import com.wujia.foundation.designsystem.panel.SwipeUpPanel
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.model.scene.NO_PARTICLE
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings

/**
 * 粒子效果独立面板入口。
 * 与 SceneControlPanel 风格一致，便于 scene 作为 overlay 单独唤起粒子调节。
 *
 * 注：粒子设置也通过 SceneControlSettings 统一持久化（见 VM）。
 */
@Composable
fun SceneParticlePanel(
    modifier: Modifier = Modifier,
    showPanel: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    panelHeight: Dp = 380.dp,
    edgePadding: Dp = 8.dp,
    sceneName: String,
    isCustomScene: Boolean = false,
    onEditSceneClick: () -> Unit = {},
    particleSettings: SceneParticleSettings = NO_PARTICLE,
    onEffectChange: (SceneParticleEffect) -> Unit = {},
    onIntensityChange: (Float) -> Unit = {},
    onWindChange: (Float) -> Unit = {},
    onQualityChange: (SceneParticleQuality) -> Unit = {},
    onForegroundGlassChange: (Boolean) -> Unit = {},
) {
    SwipeUpPanel(
        visible = showPanel,
        onVisibleChange = onVisibleChange,
        debugName = "scene_particle",
        panelHeight = panelHeight,
        edgePadding = edgePadding,
        modifier = modifier,
    ) {
        SceneParticleScreen(
            sceneName = sceneName,
            isCustomScene = isCustomScene,
            onEditSceneClick = onEditSceneClick,
            particleSettings = particleSettings,
            onEffectChange = onEffectChange,
            onIntensityChange = onIntensityChange,
            onWindChange = onWindChange,
            onQualityChange = onQualityChange,
            onForegroundGlassChange = onForegroundGlassChange,
        )
    }
}

@LandscapePreviews
@Composable
private fun SceneParticlePanelPreview() {
    SceneParticlePanel(
        showPanel = true,
        panelHeight = 384.dp,
        sceneName = "风雪夜归人",
        onVisibleChange = {},
    )
}
