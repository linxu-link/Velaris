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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.ControlEvent
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlViewModel
import com.wujia.foundation.model.scene.NO_PARTICLE
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneControlDefaults
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.player.AudioMediaItem
import com.wujia.foundation.player.VelarisPlayerPool
import timber.log.Timber

private const val PLAYER_MEDIA_TAG = "ScenePlayer"

@Composable
internal fun PlayerMediaCoordinator(
    playerPool: VelarisPlayerPool,
    currentScene: SceneResource?,
    currentScenePage: Int,
    isPlaying: Boolean,
    isSwiping: Boolean,
    controlViewModel: SceneControlViewModel,
    clockVisible: Boolean,
    clockAudioVolume: Float,
    clockAudioUri: String?,
    onPlayingStateChanged: (Boolean) -> Unit,
    onTimerExpired: () -> Unit,
) {
    // 媒体源绑定：playerPool 重建（config 变化）或实际媒体源变化时触发
    val mediaKey by derivedStateOf {
        buildString {
            append(currentScenePage)
            append('|')
            append(currentScene?.id)
            append('|')
            append(currentScene?.video?.uri)
            append('|')
            append(currentScene?.video?.volume)
            append('|')
            currentScene?.audioTracks?.joinTo(this, ",") { "${it.id}:${it.uri}" }
        }
    }
    LaunchedEffect(playerPool, mediaKey) {
        val scene = currentScene ?: return@LaunchedEffect
        Timber.tag(PLAYER_MEDIA_TAG).d(
            "bind media page=%d sceneId=%s video=%s audioCount=%d audioIds=%s mediaKey=%s",
            currentScenePage,
            scene.id,
            scene.video?.uri,
            scene.audioTracks.size,
            scene.audioTracks.map { it.id },
            mediaKey,
        )

        playerPool.releaseOutsideRange(currentScenePage)
        // 关键修复：页面切换（mediaKey 变化）时，必须对非当前页（尤其是 retained 的相邻页）执行完整 pause，
        // 而不仅 pauseVideosExcept（只停视频）。否则上一页的音频播放器会继续运行（特别是循环音频），
        // 导致翻页后新旧音频同时播放。pause() 会暂停音频 + 放弃焦点 + 置 isVideoPlaying=false。
        // 仅在 isSwiping 手势中仍使用 pauseAllVideos（临时冻结画面，不立即杀当前音频）。
        playerPool.pauseExcept(currentScenePage)

        val controller = playerPool.get(currentScenePage)
        controller.setVideoUri(scene.video?.uri)
        controller.setVideoVolume(scene.video?.volume ?: 0f)
        controller.setAudioItems(scene.audioTracks.toAudioMediaItems())
        controller.pause()
        onPlayingStateChanged(false)
    }

    // 滑动控制：仅暂停视频，不在滑动结束或场景切换后自动恢复播放
    LaunchedEffect(isSwiping) {
        if (isSwiping) {
            playerPool.pauseAllVideos()
        }
    }

    // 定时结束时自动暂停播放，playerPool 变化时重新收集事件
    val currentPageForTimer = rememberUpdatedState(currentScenePage)
    val currentOnTimerExpired = rememberUpdatedState(onTimerExpired)
    LaunchedEffect(playerPool) {
        controlViewModel.events.collect { event ->
            when (event) {
                is ControlEvent.TimerExpired -> {
                    playerPool.get(currentPageForTimer.value).pause()
                    onPlayingStateChanged(false)
                    currentOnTimerExpired.value()
                }
            }
        }
    }

    // 播放状态驱动倒计时
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            controlViewModel.onPlaybackStarted()
        } else {
            controlViewModel.onPlaybackPaused()
        }
    }

    // 场景切换 → controlVM 桥接
    LaunchedEffect(currentScene?.id) {
        val settings = currentScene?.controlSettings
        Timber.tag(PLAYER_MEDIA_TAG).d(
            "scene changed page=%d sceneId=%s audioCount=%d audioIds=%s particle=%s brightness=%s darkness=%s",
            currentScenePage,
            currentScene?.id,
            currentScene?.audioTracks?.size ?: -1,
            currentScene?.audioTracks?.map { it.id },
            settings?.particle,
            settings?.brightness,
            settings?.darkness,
        )
        controlViewModel.onSceneChanged(
            sceneId = currentScene?.id,
            brightness = settings?.brightness ?: SceneControlDefaults.BRIGHTNESS,
            darkness = settings?.darkness ?: SceneControlDefaults.DARKNESS,
            timerDurationMillis = settings?.timerDurationMillis
                ?: SceneControlDefaults.TIMER_DURATION_MILLIS,
            timerMode = settings?.timerMode ?: SceneTimerMode.Countdown,
            showCountdownClock = settings?.showCountdownClock ?: true,
            alarmReminderEnabled = settings?.alarmReminderEnabled ?: false,
            countdownClockPosition = settings?.countdownClockPosition
                ?: SceneCountdownClockPosition.Center,
            clockAudioVolume = settings?.clockAudioVolume
                ?: SceneControlDefaults.CLOCK_AUDIO_VOLUME,
            particleSettings = settings?.particle ?: NO_PARTICLE,
            guideCompleted = settings?.guideCompleted ?: false,
        )
    }

    // 时钟音频管理：设置时钟音频配置并根据 clockVisible 控制播放
    // currentScenePage 作为 key，页面切换时重新绑定到新 page 的 controller
    LaunchedEffect(currentScenePage, clockAudioUri, clockAudioVolume) {
        playerPool.get(currentScenePage).setClockAudio(clockAudioUri, clockAudioVolume)
    }

    LaunchedEffect(currentScenePage, clockVisible, clockAudioUri) {
        val controller = playerPool.get(currentScenePage)
        if (clockAudioUri != null) {
            controller.setClockAudio(clockAudioUri, clockAudioVolume)
        }
        if (!clockVisible) {
            controller.stopClockAudio()
        }
    }

    LaunchedEffect(currentScenePage, clockAudioVolume) {
        if (clockVisible) {
            playerPool.get(currentScenePage).setClockAudioVolume(clockAudioVolume)
        }
    }

    DisposableEffect(currentScenePage) {
        onDispose {
            playerPool.get(currentScenePage).stopClockAudio()
        }
    }
}

private fun SceneAudioResource.toAudioMediaItem() = AudioMediaItem(
    id = id,
    uri = uri,
    volume = volume,
    title = title,
    loop = loop,
)

private fun List<SceneAudioResource>.toAudioMediaItems() = map { it.toAudioMediaItem() }
