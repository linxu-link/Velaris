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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.wujia.feature.scene.impl.ui.SceneActionsBundle
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.model.scene.SceneVideoResource

internal sealed interface SceneRootDialogState {
    val dismissible: Boolean

    data object None : SceneRootDialogState {
        override val dismissible: Boolean = false
    }

    data class CustomTimer(
        val startOnClockPage: Boolean,
        val onConfirm: (hours: Int, minutes: Int) -> Unit,
        val onClockConfirm: () -> Unit,
        val onDismissRequest: () -> Unit,
    ) : SceneRootDialogState {
        override val dismissible: Boolean = true
    }

    data class AlarmReminder(val onConfirm: () -> Unit) : SceneRootDialogState {
        override val dismissible: Boolean = false
    }

    data class PlayerSound(
        val audioTracks: List<SceneAudioResource>,
        val videoResource: SceneVideoResource?,
        val clockVisible: Boolean,
        val clockAudioVolume: Float,
        val onClockAudioVolumeChange: (Float) -> Unit,
        val onClockAudioVolumeSave: (Float) -> Unit,
        val onAudioVolumeChange: (audioId: String, volume: Float) -> Unit,
        val onAudioVolumesSave: (Map<String, Float>) -> Unit,
        val onVideoVolumeChange: (Float) -> Unit,
        val onVideoVolumeSave: (Float) -> Unit,
        val minWidth: Dp,
        val minHeight: Dp,
        val maxHeight: Dp,
        val titleFontSize: TextUnit,
        val itemTitleFontSize: TextUnit,
        val valueFontSize: TextUnit,
        val edgePadding: Dp,
        val onDismissRequest: () -> Unit,
    ) : SceneRootDialogState {
        override val dismissible: Boolean = true
    }
}

@Composable
internal fun rememberSceneRootDialogState(
    controlState: SceneControlUiState,
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
    actions: SceneActionsBundle,
    currentSceneAudioTracks: List<SceneAudioResource>,
    currentSceneVideoResource: SceneVideoResource?,
    showSoundDialog: Boolean,
): SceneRootDialogState = when {
    showAlarmDialog -> SceneRootDialogState.AlarmReminder(
        onConfirm = onAlarmDialogConfirm,
    )

    controlState.showCustomTimerDialog -> SceneRootDialogState.CustomTimer(
        startOnClockPage = controlState.timerMode == SceneTimerMode.Clock,
        onConfirm = actions.control.onCustomTimerConfirm,
        onClockConfirm = actions.control.onClockTimerConfirm,
        onDismissRequest = actions.control.onCustomTimerDialogDismiss,
    )

    showSoundDialog -> SceneRootDialogState.PlayerSound(
        audioTracks = currentSceneAudioTracks,
        videoResource = currentSceneVideoResource,
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
    else -> SceneRootDialogState.None
}

@Composable
internal fun SceneRootDialogHost(activeDialog: SceneRootDialogState, modifier: Modifier = Modifier) {
    if (activeDialog == SceneRootDialogState.None) return

    val spec = VelarisTheme.spec
    var playerSoundDismissHandler by remember(activeDialog) {
        mutableStateOf<(() -> Unit)?>(null)
    }
    val dismiss = {
        when (activeDialog) {
            is SceneRootDialogState.CustomTimer -> activeDialog.onDismissRequest()
            is SceneRootDialogState.PlayerSound -> {
                playerSoundDismissHandler?.invoke() ?: activeDialog.onDismissRequest()
            }
            else -> Unit
        }
    }

    if (activeDialog.dismissible) {
        BackHandler(onBack = dismiss)
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .then(
                    if (activeDialog.dismissible) {
                        Modifier.velarisClickable(onClick = dismiss)
                    } else {
                        Modifier
                    },
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spec.spacing.large),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.velarisClickable {},
            ) {
                when (activeDialog) {
                    SceneRootDialogState.None -> Unit
                    is SceneRootDialogState.CustomTimer -> CustomTimerDialog(
                        startOnClockPage = activeDialog.startOnClockPage,
                        onConfirm = activeDialog.onConfirm,
                        onClockConfirm = activeDialog.onClockConfirm,
                        onDismissRequest = activeDialog.onDismissRequest,
                    )

                    is SceneRootDialogState.AlarmReminder -> AlarmReminderDialog(
                        onConfirm = activeDialog.onConfirm,
                    )

                    is SceneRootDialogState.PlayerSound -> PlayerSoundDialog(
                        audioTracks = activeDialog.audioTracks,
                        videoResource = activeDialog.videoResource,
                        clockVisible = activeDialog.clockVisible,
                        clockAudioVolume = activeDialog.clockAudioVolume,
                        onClockAudioVolumeChange = activeDialog.onClockAudioVolumeChange,
                        onClockAudioVolumeSave = activeDialog.onClockAudioVolumeSave,
                        onAudioVolumeChange = activeDialog.onAudioVolumeChange,
                        onAudioVolumesSave = activeDialog.onAudioVolumesSave,
                        onVideoVolumeChange = activeDialog.onVideoVolumeChange,
                        onVideoVolumeSave = activeDialog.onVideoVolumeSave,
                        minWidth = activeDialog.minWidth,
                        minHeight = activeDialog.minHeight,
                        maxHeight = activeDialog.maxHeight,
                        titleFontSize = activeDialog.titleFontSize,
                        itemTitleFontSize = activeDialog.itemTitleFontSize,
                        valueFontSize = activeDialog.valueFontSize,
                        edgePadding = activeDialog.edgePadding,
                        onDismissRequest = activeDialog.onDismissRequest,
                        onDismissHandlerReady = { playerSoundDismissHandler = it },
                    )
                }
            }
        }
    }
}
