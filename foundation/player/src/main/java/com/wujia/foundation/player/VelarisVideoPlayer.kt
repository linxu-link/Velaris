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
package com.wujia.foundation.player

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import android.graphics.Color as AndroidColor

/**
 * 创建不自动管理媒体源的 [VelarisPlayerController]。
 * 调用方需自行通过 controller 的公共 API 管理媒体生命周期。
 *
 * 配置通过 [LocalVelarisPlayerConfig] 获取。
 */
@Composable
fun rememberVelarisPlayerController(): VelarisPlayerController {
    val context = LocalContext.current
    val config = LocalVelarisPlayerConfig.current
    val controller = remember(config) { VelarisPlayerController(context, config) }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    return controller
}

/**
 * 创建一个 Compose 感知的 [VelarisPlayerController]。
 *
 * 该方法封装了控制器的创建、更新和销毁逻辑：
 * - 通过 [remember] 确保同一 recomposition 中返回同一个控制器实例
 * - 通过 [LaunchedEffect] 监听 videoUri、audioItems、playWhenReady 的变化并更新控制器
 * - 通过 [DisposableEffect] 在离开 Composition 时自动释放控制器资源
 *
 * 配置通过 [LocalVelarisPlayerConfig] 获取。
 *
 * @param videoUri 视频资源的 URI，支持网络 URL 或本地路径
 * @param audioItems 音频媒体项列表，可同时播放多个音频
 * @param playWhenReady 是否立即开始播放
 *
 * 离开 Composition 时会自动调用 `controller.release()` 释放所有资源（音频播放器 + 视频 + 焦点）。
 * 历史上曾有 `releaseAudioOnDispose = false` 用于“保留后台 Service”，但 Service 已移除，该分支已删除。
 * 所有调用方都应让预览/临时控制器在 dispose 时彻底释放，避免 ExoPlayer 泄漏。
 */
@Composable
fun rememberVelarisPlayerController(
    videoUri: String? = null,
    audioItems: List<AudioMediaItem> = emptyList(),
    playWhenReady: Boolean = false,
): VelarisPlayerController {
    val context = LocalContext.current
    val config = LocalVelarisPlayerConfig.current

    // 创建控制器实例，config 变化时销毁旧实例并创建新实例
    val controller = remember(config) {
        VelarisPlayerController(context, config)
    }

    // 当 controller（由 config 驱动重建）或 videoUri 变化时更新视频源
    LaunchedEffect(controller, videoUri) {
        controller.setVideoUri(videoUri)
    }

    // 当 controller 或 audioItems 变化时更新音频列表
    LaunchedEffect(controller, audioItems) {
        controller.setAudioItems(audioItems)
    }

    // 当 controller 或 playWhenReady 变化时控制播放/暂停
    LaunchedEffect(controller, playWhenReady) {
        if (playWhenReady) {
            controller.play()
        } else {
            controller.pause()
        }
    }

    // 离开 Composition 时释放控制器资源（总是完整释放）
    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}

/**
 * Jetpack Compose 视频播放器组件，将 ExoPlayer 嵌入到 Compose 布局中。
 *
 * 使用 [AndroidView] 将原生 [PlayerView]（Media3 UI 组件）嵌入 Compose，
 * 并通过 [AspectRatioFrameLayout.RESIZE_MODE_ZOOM] 实现视频填充效果。
 *
 * Surface 类型由 [LocalVelarisPlayerConfig] 中的
 * [VelarisPlayerConfig.useTextureView] 决定。
 *
 * @param controller 播放器控制器，由 [rememberVelarisPlayerController] 创建
 * @param modifier Compose 修饰符
 */
@Composable
fun VelarisVideoPlayer(controller: VelarisPlayerController, modifier: Modifier = Modifier) {
    val config = LocalVelarisPlayerConfig.current
    // useTextureView 变化时强制销毁旧 AndroidView 并重建，使 surface 类型切换生效
    key(config.useTextureView) {
        val layoutId = if (config.useTextureView) {
            R.layout.foundation_player_velaris_video_player_texture
        } else {
            R.layout.foundation_player_velaris_video_player_surface
        }

        AndroidView(
            modifier = modifier,
            factory = { context ->
                (LayoutInflater.from(context).inflate(layoutId, null, false) as PlayerView).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(AndroidColor.TRANSPARENT)
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    player = controller.videoPlayer
                }
            },
            update = { playerView ->
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                playerView.setShutterBackgroundColor(AndroidColor.TRANSPARENT)
                playerView.setBackgroundColor(AndroidColor.TRANSPARENT)
                playerView.player = controller.videoPlayer
            },
            onRelease = { playerView ->
                playerView.player = null
            },
        )
    }
}
