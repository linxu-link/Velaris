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

import com.wujia.feature.scene.impl.ui.viewmodel.SceneEditOrigin
import com.wujia.feature.scene.impl.ui.viewmodel.ScenePanelState
import com.wujia.foundation.designsystem.guide.GuidePlacement
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality

internal data class SceneNavigationActions(
    val onScenePageChange: (Int) -> Unit,
    val onSceneModeChange: (Int) -> Unit,
    val onOpenSceneEditPage: (sceneId: String?, category: SceneCategory?) -> Unit = { _, _ -> },
    val onSceneSelectById: (String) -> Unit = {},
)

internal data class ScenePanelActions(
    val onPanelChange: (ScenePanelState) -> Unit,
    val onSoundDialogVisibilityChange: (Boolean) -> Unit,
    val onEditingSceneIdChange: (String?) -> Unit,
    val onEditingSceneOriginChange: (SceneEditOrigin) -> Unit,
    val onEditingSceneFinished: () -> Unit,
    val onSwipeStateChanged: (Boolean) -> Unit,
    val onPlayerClick: () -> Unit = {},
    val onGuideCompleted: () -> Unit = {},
)

internal data class SceneControlActions(
    val onTimerOptionChange: (Int) -> Unit = {},
    val onCustomTimerClick: () -> Unit = {},
    val onCustomTimerConfirm: (Int, Int) -> Unit = { _, _ -> },
    val onClockTimerConfirm: () -> Unit = {},
    val onCustomTimerDialogDismiss: () -> Unit = {},
    val onShowCountdownClockChange: (Boolean) -> Unit = {},
    val onAlarmReminderChange: (Boolean) -> Unit = {},
    val onCountdownClockPositionChange: (SceneCountdownClockPosition) -> Unit = {},
    val onVisualControlItemValueChange: (index: Int, value: Float) -> Unit = { _, _ -> },
)

internal data class SceneParticleActions(
    val onEffectChange: (SceneParticleEffect) -> Unit = {},
    val onIntensityChange: (Float) -> Unit = {},
    val onWindChange: (Float) -> Unit = {},
    val onQualityChange: (SceneParticleQuality) -> Unit = {},
    val onForegroundGlassChange: (Boolean) -> Unit = {},
)

internal data class SceneAudioActions(
    val onVolumeChange: (String, Float) -> Unit = { _, _ -> },
    val onVolumesSave: (Map<String, Float>) -> Unit = {},
    val onVideoVolumeChange: (Float) -> Unit = {},
    val onVideoVolumeSave: (Float) -> Unit = {},
    val onClockAudioVolumeChange: (Float) -> Unit = {},
    val onClockAudioVolumeSave: (Float) -> Unit = {},
)

internal data class SceneActionsBundle(
    val navigation: SceneNavigationActions,
    val panel: ScenePanelActions,
    val control: SceneControlActions,
    val particle: SceneParticleActions,
    val audio: SceneAudioActions,
)

internal data class SceneGuideStep(val titleResId: Int, val descriptionResId: Int, val placement: GuidePlacement)
