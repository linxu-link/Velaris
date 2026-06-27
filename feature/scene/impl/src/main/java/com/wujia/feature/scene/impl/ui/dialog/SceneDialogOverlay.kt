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
package com.wujia.feature.scene.impl.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.wujia.feature.scene.impl.ui.SceneActionsBundle
import com.wujia.feature.scene.impl.ui.viewmodel.SceneUiState
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.foundation.model.scene.SceneTimerMode

@Composable
internal fun SceneDialogOverlay(
    uiState: SceneUiState,
    controlState: SceneControlUiState,
    actions: SceneActionsBundle,
    showAlarmDialog: Boolean,
    onAlarmDialogConfirm: () -> Unit,
    clockVisible: Boolean,
    controlPanelMinWidth: Dp,
    controlPanelMinHeight: Dp,
    controlPanelMaxHeight: Dp,
    controlTitleFontSize: TextUnit,
    controlItemTitleFontSize: TextUnit,
    controlValueFontSize: TextUnit,
    edgePadding: Dp,
) {
    if (controlState.showCustomTimerDialog) {
        CustomTimerDialog(
            startOnClockPage = controlState.timerMode == SceneTimerMode.Clock,
            onConfirm = actions.control.onCustomTimerConfirm,
            onClockConfirm = actions.control.onClockTimerConfirm,
            onDismissRequest = actions.control.onCustomTimerDialogDismiss,
        )
    }

    if (showAlarmDialog) {
        AlarmReminderDialog(
            onConfirm = onAlarmDialogConfirm,
        )
    }

    if (uiState.showSoundDialog) {
        PlayerSoundDialog(
            audioTracks = uiState.currentScene?.audioTracks.orEmpty(),
            videoResource = uiState.currentScene?.video,
            clockVisible = clockVisible,
            clockAudioVolume = controlState.clockAudioVolume,
            onClockAudioVolumeChange = actions.audio.onClockAudioVolumeChange,
            onClockAudioVolumeSave = actions.audio.onClockAudioVolumeSave,
            onAudioVolumeChange = actions.audio.onVolumeChange,
            onAudioVolumesSave = actions.audio.onVolumesSave,
            onVideoVolumeChange = actions.audio.onVideoVolumeChange,
            onVideoVolumeSave = actions.audio.onVideoVolumeSave,
            minWidth = controlPanelMinWidth,
            minHeight = controlPanelMinHeight,
            maxHeight = controlPanelMaxHeight,
            titleFontSize = controlTitleFontSize,
            itemTitleFontSize = controlItemTitleFontSize,
            valueFontSize = controlValueFontSize,
            edgePadding = edgePadding,
            onDismissRequest = { actions.panel.onSoundDialogVisibilityChange(false) },
        )
    }
}
