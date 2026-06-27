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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * 播放器性能档位。
 *
 * - [PowerSaver]：低端或发热设备，使用 SurfaceView、限制音轨和缓存
 * - [Balanced]：默认档位，平衡性能与体验
 * - [Quality]：高端设备，保留更多相邻页播放器、更大首帧缓存
 */
enum class VelarisPlayerPerformanceProfile {
    PowerSaver,
    Balanced,
    Quality,
}

/**
 * 播放器性能配置。
 *
 * 通过 [LocalVelarisPlayerConfig] 在 Compose 树中传递，
 * 默认为 [Balanced]，消费者无需显式传参。
 *
 * @param profile 性能档位标识
 * @param useTextureView `true` 使用 TextureView（兼容性好，开销较高），
 *   `false` 使用 SurfaceView（省电，需验证叠加层兼容性）
 * @param enableDecoderFallback 解码器回退，`true` 在首选解码器失败时尝试备选
 * @param retainedPageDistance 播放器池保留的相邻页面数，`0` 仅保留当前页
 * @param maxSimultaneousAudioTracks 同时播放的最大音频轨道数
 * @param firstFrameCacheSizeKb 视频首帧缓存大小（KB）
 */
data class VelarisPlayerConfig(
    val profile: VelarisPlayerPerformanceProfile = VelarisPlayerPerformanceProfile.Balanced,
    val useTextureView: Boolean = true,
    val enableDecoderFallback: Boolean = true,
    val retainedPageDistance: Int = 1,
    val maxSimultaneousAudioTracks: Int = Int.MAX_VALUE,
    val firstFrameCacheSizeKb: Int = 12 * 1024,
) {
    init {
        require(retainedPageDistance >= 0) {
            "retainedPageDistance must be >= 0, got $retainedPageDistance"
        }
        require(maxSimultaneousAudioTracks >= 1) {
            "maxSimultaneousAudioTracks must be >= 1, got $maxSimultaneousAudioTracks"
        }
        require(firstFrameCacheSizeKb > 0) {
            "firstFrameCacheSizeKb must be > 0, got $firstFrameCacheSizeKb"
        }
    }
    companion object {
        /** 低端/发热设备：SurfaceView、限制音轨、小缓存 */
        val PowerSaver = VelarisPlayerConfig(
            profile = VelarisPlayerPerformanceProfile.PowerSaver,
            useTextureView = false,
            enableDecoderFallback = false,
            retainedPageDistance = 0,
            maxSimultaneousAudioTracks = 2,
            firstFrameCacheSizeKb = 4 * 1024,
        )

        /** 默认档位：TextureView、全功能、标准缓存 */
        val Balanced = VelarisPlayerConfig()

        /** 高端设备：保留更多页面、更大缓存 */
        val Quality = VelarisPlayerConfig(
            profile = VelarisPlayerPerformanceProfile.Quality,
            useTextureView = true,
            enableDecoderFallback = true,
            retainedPageDistance = 2,
            maxSimultaneousAudioTracks = Int.MAX_VALUE,
            firstFrameCacheSizeKb = 16 * 1024,
        )
    }
}

/**
 * 通过 CompositionLocal 向下传递播放器配置。
 *
 * 默认值为 [VelarisPlayerConfig.Balanced]，消费者无需显式提供。
 * 如需自定义，在 Compose 树顶层使用 [ProvideVelarisPlayerConfig]。
 */
val LocalVelarisPlayerConfig = compositionLocalOf { VelarisPlayerConfig.Balanced }

/**
 * 在 Compose 子树中提供自定义的播放器配置。
 *
 * ```kotlin
 * ProvideVelarisPlayerConfig(VelarisPlayerConfig.PowerSaver) {
 *     SceneScreen(...)
 * }
 * ```
 */
@Composable
fun ProvideVelarisPlayerConfig(config: VelarisPlayerConfig, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalVelarisPlayerConfig provides config) {
        content()
    }
}
