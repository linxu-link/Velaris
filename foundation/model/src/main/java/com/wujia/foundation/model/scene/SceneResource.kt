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
package com.wujia.foundation.model.scene

/**
 * 场景资源数据类，对外暴露的领域模型。
 *
 * 代表一个完整的场景配置，包含背景、视频和音轨信息。
 *
 * @param id 场景的唯一标识符
 * @param title 场景的展示标题（如古诗词风格的中文标题）
 * @param subtitle 场景的副标题/描述
 * @param category 场景分类，用于主场景页按模式筛选
 * @param coverResId 场景封面资源 ID，优先用于列表/卡片展示
 * @param coverUri 场景封面 URI，通常用于自定义场景的本地图片封面
 * @param backgroundResId 背景图片的资源 ID
 * @param backgroundUri 设备媒体库图片 URI
 * @param video 视频资源，可为空（部分场景无视频）
 * @param audioTracks 音频轨道列表，支持同时播放多个音轨（如风声、雨声等）
 */
data class SceneResource(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SceneCategory = SceneCategory.FOCUS,
    val isPreset: Boolean = false,
    val coverResId: Int? = null,
    val coverUri: String? = null,
    val backgroundResId: Int? = null,
    val backgroundUri: String? = null,
    val video: SceneVideoResource? = null,
    val audioTracks: List<SceneAudioResource> = emptyList(),
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

object SceneControlDefaults {
    const val BRIGHTNESS: Float = 0.5f
    const val BRIGHTNESS_DB: String = "0.5"
    const val DARKNESS: Float = 0.05f
    const val DARKNESS_DB: String = "0.05"
    const val TIMER_DURATION_MILLIS: Long = 45 * 60 * 1000L
    const val TIMER_DURATION_DB: String = "2700000"
    const val CLOCK_AUDIO_VOLUME: Float = 0.5f
}

data class SceneControlSettings(
    val brightness: Float = SceneControlDefaults.BRIGHTNESS,
    val darkness: Float = SceneControlDefaults.DARKNESS,
    val timerMode: SceneTimerMode = SceneTimerMode.Countdown,
    val timerDurationMillis: Long = SceneControlDefaults.TIMER_DURATION_MILLIS,
    val fadeOutEnabled: Boolean = true,
    val guideCompleted: Boolean = false,
    val showCountdownClock: Boolean = true,
    val alarmReminderEnabled: Boolean = false,
    val countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center,
    val clockAudioVolume: Float = SceneControlDefaults.CLOCK_AUDIO_VOLUME,
    val particle: SceneParticleSettings = NO_PARTICLE,
)

enum class SceneTimerMode {
    Countdown,
    Clock,
}

enum class SceneCountdownClockPosition {
    Center,
    TopStart,
    BottomStart,
    TopEnd,
    BottomEnd,
}

/**
 * 场景视频资源。
 *
 * @param uri 视频资源的 URI（android.resource:// 格式）
 * @param volume 视频音量，范围 0f ~ 1f，默认为 0f
 */
data class SceneVideoResource(val uri: String, val volume: Float = DEFAULT_SCENE_VIDEO_VOLUME)

/**
 * 场景音频资源，代表一个可独立播放的音轨。
 *
 * @param id 音轨的唯一标识符
 * @param title 音轨的展示标题（如"风声"、"雨声"）
 * @param uri 音频资源的 URI
 * @param volume 播放音量，范围 0f ~ 1f，默认为 [DEFAULT_SCENE_AUDIO_VOLUME]
 * @param loop 是否循环播放，默认为 true
 */
data class SceneAudioResource(
    val id: String,
    val title: String,
    val uri: String,
    val volume: Float = DEFAULT_SCENE_AUDIO_VOLUME,
    val loop: Boolean = true,
)

/** 场景音频的默认音量（最大音量） */
const val DEFAULT_SCENE_AUDIO_VOLUME = 0.6f

/** 场景视频的默认音量 */
const val DEFAULT_SCENE_VIDEO_VOLUME = 0f
