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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.wujia.feature.scene.impl.ui.sampleAudioTracks
import com.wujia.feature.scene.impl.ui.viewmodel.toSoundControlItems
import com.wujia.feature.scenecontrol.impl.ui.component.SceneSoundPanel
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneVideoResource
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.VelarisDialogPanel

@Composable
internal fun PlayerSoundDialog(
    audioTracks: List<SceneAudioResource>,
    videoResource: SceneVideoResource?,
    clockVisible: Boolean = false,
    clockAudioVolume: Float = 0.5f,
    onClockAudioVolumeChange: (Float) -> Unit = {},
    onClockAudioVolumeSave: (Float) -> Unit = {},
    onAudioVolumeChange: (audioId: String, volume: Float) -> Unit,
    onAudioVolumesSave: (Map<String, Float>) -> Unit,
    onVideoVolumeChange: (Float) -> Unit,
    onVideoVolumeSave: (Float) -> Unit,
    minWidth: Dp,
    minHeight: Dp,
    maxHeight: Dp,
    titleFontSize: TextUnit,
    itemTitleFontSize: TextUnit,
    valueFontSize: TextUnit,
    edgePadding: Dp,
    onDismissRequest: () -> Unit,
    onDismissHandlerReady: ((() -> Unit) -> Unit)? = null,
) {
    val videoVolumeTitle = stringResource(R.string.scene_sound_video_volume)
    val clockAudioTitle = stringResource(R.string.clock_audio_label)
    val clockItemOffset = audioTracks.size + (if (videoResource != null) 1 else 0)
    var soundItems by remember(
        audioTracks,
        videoResource,
        videoVolumeTitle,
        clockVisible,
        clockAudioVolume,
        clockAudioTitle,
    ) {
        mutableStateOf(
            audioTracks.toSoundControlItems() +
                videoResource.toSoundControlItem(videoVolumeTitle) +
                if (clockVisible) {
                    listOf(
                        SoundControlItem(
                            title = clockAudioTitle,
                            icon = Icons.Outlined.AccessTime,
                            value = clockAudioVolume,
                        ),
                    )
                } else {
                    emptyList()
                },
        )
    }
    fun dismissWithSave() {
        val changedVolumes = soundItems
            .take(audioTracks.size)
            .mapIndexedNotNull { index, item ->
                val audioTrack = audioTracks.getOrNull(index) ?: return@mapIndexedNotNull null
                audioTrack.id.takeIf { item.value != audioTrack.volume }?.let { id ->
                    id to item.value
                }
            }
            .toMap()
        onAudioVolumesSave(changedVolumes)
        val videoItem = soundItems.getOrNull(audioTracks.size)
        if (videoResource != null && videoItem != null && videoItem.value != videoResource.volume) {
            onVideoVolumeSave(videoItem.value)
        }
        if (clockVisible) {
            val clockItem = soundItems.getOrNull(clockItemOffset)
            if (clockItem != null && clockItem.value != clockAudioVolume) {
                onClockAudioVolumeSave(clockItem.value)
            }
        }
        onDismissRequest()
    }

    SideEffect {
        onDismissHandlerReady?.invoke(::dismissWithSave)
    }

    val spec = VelarisTheme.spec
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = edgePadding),
        contentAlignment = Alignment.Center,
    ) {
        val dialogMaxWidth = minOf(maxWidth - edgePadding * 2, minWidth * 1.35f)
        VelarisDialogPanel(
            modifier = Modifier.widthIn(
                min = minWidth,
                max = dialogMaxWidth,
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            SceneSoundPanel(
                title = stringResource(R.string.sound_settings),
                items = soundItems,
                onItemValueChange = { index, value ->
                    soundItems = soundItems.toMutableList().also {
                        it[index] = it[index].copy(value = value)
                    }
                    when {
                        index < audioTracks.size -> {
                            onAudioVolumeChange(audioTracks[index].id, value)
                        }
                        clockVisible && index == clockItemOffset -> {
                            onClockAudioVolumeChange(value)
                        }
                        else -> {
                            onVideoVolumeChange(value)
                        }
                    }
                },
                minWidth = minWidth,
                minHeight = minHeight,
                maxHeight = maxHeight,
                titleFontSize = titleFontSize,
                itemTitleFontSize = itemTitleFontSize,
                valueFontSize = valueFontSize,
                compact = true,
                modifier = Modifier.padding(spec.spacing.large),
            )
        }
    }
}

private fun SceneVideoResource?.toSoundControlItem(title: String): List<SoundControlItem> = this?.let {
    listOf(
        SoundControlItem(
            title = title,
            icon = Icons.Outlined.Movie,
            value = volume,
        ),
    )
}.orEmpty()

@LandscapePreviews
@Composable
private fun PlayerSoundDialogPreview() {
    val spec = VelarisTheme.spec
    PlayerSoundDialog(
        audioTracks = sampleAudioTracks(),
        videoResource = null,
        onAudioVolumeChange = { _, _ -> },
        onAudioVolumesSave = {},
        onVideoVolumeChange = {},
        onVideoVolumeSave = {},
        minWidth = 280.dp,
        minHeight = 180.dp,
        maxHeight = 220.dp,
        titleFontSize = spec.typography.subtitle,
        itemTitleFontSize = spec.typography.subtitle,
        valueFontSize = spec.typography.body,
        edgePadding = spec.spacing.xLarge,
        onDismissRequest = {},
    )
}
