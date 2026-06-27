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
package com.wujia.feature.scene.impl.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wujia.feature.scene.impl.ui.SceneActionsBundle
import com.wujia.feature.scene.impl.ui.SceneLayoutProfile
import com.wujia.feature.scene.impl.ui.viewmodel.SceneEditOrigin
import com.wujia.feature.scene.impl.ui.viewmodel.ScenePanelState
import com.wujia.feature.scene.impl.ui.viewmodel.SceneUiState
import com.wujia.feature.scenecontrol.impl.SceneControlPanel
import com.wujia.feature.scenecontrol.impl.SceneParticlePanel
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.feature.sceneedit.impl.SceneEditPanel
import com.wujia.feature.scenelist.impl.SceneListPanel
import com.wujia.feature.settings.impl.SettingsPanel

@Composable
internal fun ScenePanelOverlay(
    uiState: SceneUiState,
    controlState: SceneControlUiState,
    actions: SceneActionsBundle,
    profile: SceneLayoutProfile,
) {
    SceneControlPanel(
        modifier = Modifier,
        showPanel = uiState.showControlPanel,
        panelHeight = profile.controlPanelHeight,
        edgePadding = profile.edgePadding,
        sceneName = uiState.currentScene?.title.orEmpty(),
        isCustomScene = uiState.currentScene?.isPreset == false,
        timerMode = controlState.timerMode,
        timerSelectedOption = controlState.timerSelectedOption,
        showCountdownClock = controlState.showCountdownClock,
        alarmReminderEnabled = controlState.alarmReminderEnabled,
        countdownClockPosition = controlState.countdownClockPosition,
        brightness = controlState.brightness,
        darkness = controlState.darkness,
        onAdjustDetailsClick = {
            actions.panel.onPanelChange(ScenePanelState.PARTICLE)
        },
        onTimerOptionChange = actions.control.onTimerOptionChange,
        onCustomTimerClick = actions.control.onCustomTimerClick,
        onShowCountdownClockChange = actions.control.onShowCountdownClockChange,
        onAlarmReminderChange = actions.control.onAlarmReminderChange,
        onCountdownClockPositionChange = actions.control.onCountdownClockPositionChange,
        onVisualControlItemValueChange = actions.control.onVisualControlItemValueChange,
        onEditSceneClick = {
            uiState.currentScene?.id?.let { sceneId ->
                actions.panel.onEditingSceneIdChange(sceneId)
                actions.panel.onEditingSceneOriginChange(SceneEditOrigin.CONTROL)
                actions.panel.onPanelChange(ScenePanelState.EDIT)
            }
        },
        onVisibleChange = { visible ->
            if (visible) {
                actions.panel.onPanelChange(ScenePanelState.CONTROL)
            } else {
                actions.panel.onPanelChange(ScenePanelState.NONE)
            }
        },
    )

    SceneParticlePanel(
        modifier = Modifier,
        showPanel = uiState.showParticlePanel,
        edgePadding = profile.edgePadding,
        onVisibleChange = { visible ->
            if (visible) {
                actions.panel.onPanelChange(ScenePanelState.PARTICLE)
            } else {
                actions.panel.onPanelChange(ScenePanelState.CONTROL)
            }
        },
        panelHeight = profile.controlPanelHeight,
        sceneName = uiState.currentScene?.title.orEmpty(),
        particleSettings = controlState.particleSettings,
        onEffectChange = actions.particle.onEffectChange,
        onIntensityChange = actions.particle.onIntensityChange,
        onWindChange = actions.particle.onWindChange,
        onQualityChange = actions.particle.onQualityChange,
        onForegroundGlassChange = actions.particle.onForegroundGlassChange,
    )

    SceneEditPanel(
        modifier = Modifier,
        showPanel = uiState.showEditScenePanel,
        edgePadding = profile.edgePadding,
        onVisibleChange = { visible ->
            if (visible) {
                actions.panel.onPanelChange(ScenePanelState.EDIT)
            } else {
                actions.panel.onEditingSceneFinished()
            }
        },
        sceneId = uiState.editingSceneId,
        category = uiState.selectedCategory,
        panelHeight = profile.editPanelHeight,
    )

    SceneListPanel(
        modifier = Modifier,
        showPanel = uiState.showSceneListPanel,
        edgePadding = profile.edgePadding,
        onVisibleChange = { visible ->
            if (visible) {
                actions.panel.onPanelChange(ScenePanelState.LIST)
            } else {
                actions.panel.onPanelChange(ScenePanelState.NONE)
            }
        },
        category = uiState.selectedCategory,
        onAddScene = { category ->
            actions.panel.onEditingSceneIdChange(null)
            actions.panel.onEditingSceneOriginChange(SceneEditOrigin.LIST)
            actions.panel.onPanelChange(ScenePanelState.EDIT)
        },
        onSceneClick = { sceneId, _ ->
            actions.navigation.onSceneSelectById(sceneId)
        },
        onOpenSettings = {
            actions.panel.onPanelChange(ScenePanelState.SETTINGS)
        },
        panelHeight = profile.listPanelHeight,
    )

    SettingsPanel(
        modifier = Modifier,
        showPanel = uiState.showSettingsPanel,
        edgePadding = profile.edgePadding,
        onVisibleChange = { visible ->
            if (visible) {
                actions.panel.onPanelChange(ScenePanelState.SETTINGS)
            } else {
                actions.panel.onPanelChange(ScenePanelState.LIST)
            }
        },
        panelHeight = profile.listPanelHeight,
    )
}
