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
package com.wujia.foundation.data.scene

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlDefaults
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.ui.R
import com.wujia.foundation.model.R as ModelR

data class LocalSeedScene(
    val id: String,
    @param:StringRes val titleResId: Int,
    @param:StringRes val subtitleResId: Int,
    val category: SceneCategory,
    val sortOrder: Int,
    val isPreset: Boolean = true,
    val coverResName: String? = null,
    val backgroundResName: String? = null,
    @param:RawRes val videoResId: Int? = null,
    val videoVolume: Float = 0f,
    val brightness: Float = SceneControlDefaults.BRIGHTNESS,
    val darkness: Float = SceneControlDefaults.DARKNESS,
    val showCountdownClock: Boolean = true,
    val alarmReminderEnabled: Boolean = false,
    val particleSettings: SceneParticleSettings = SceneParticleSettings(),
    val audioTracks: List<LocalSeedAudio> = emptyList(),
)

data class LocalSeedAudio(
    val id: String,
    @param:StringRes val titleResId: Int,
    @param:RawRes val rawResId: Int,
    val volume: Float = 1f,
    val loop: Boolean = true,
)

fun defaultLocalScenes(): List<LocalSeedScene> = listOf(
    // 专注场景：风过檐铃、雨收长巷、雨落杯沿、雪压城灯、书灯微寂
    LocalSeedScene(
        id = ProjectsIds.Scene.WIND_CHIME,
        titleResId = R.string.seed_scene_wind_chime_title,
        subtitleResId = R.string.seed_scene_wind_chime_subtitle,
        category = SceneCategory.FOCUS,
        sortOrder = 0,
        brightness = 0.4f,
        showCountdownClock = true,
        alarmReminderEnabled = true,
        coverResName = "ic_cover_wind_chime",
        videoResId = ModelR.raw.focus_wind_chime,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.WIND_CHIME,
                titleResId = R.string.seed_noise_wind_chime_title,
                rawResId = ModelR.raw.wind_chime_1,
                volume = 0.7f,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.AFTER_RAIN,
        titleResId = R.string.seed_scene_after_rain_title,
        subtitleResId = R.string.seed_scene_after_rain_subtitle,
        category = SceneCategory.FOCUS,
        sortOrder = 1,
        brightness = 0.4f,
        showCountdownClock = true,
        alarmReminderEnabled = true,
        backgroundResName = ProjectsIds.Background.AFTER_RAIN,
        particleSettings = SceneParticleSettings(
            effect = SceneParticleEffect.Rain,
            intensity = 0.6f,
            wind = 0.3f,
            quality = SceneParticleQuality.Medium,
            foregroundGlassEnabled = false,
        ),
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.RAIN,
                titleResId = R.string.seed_noise_rain_title,
                rawResId = ModelR.raw.rain_1,
                volume = 0.7f,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.RAINY_CAFE,
        titleResId = R.string.seed_scene_rainy_cafe_title,
        subtitleResId = R.string.seed_scene_rainy_cafe_subtitle,
        category = SceneCategory.FOCUS,
        sortOrder = 2,
        brightness = 0.4f,
        showCountdownClock = true,
        alarmReminderEnabled = true,
        coverResName = "ic_cover_cafe_rain",
        videoResId = ModelR.raw.focus_rainy_cafe,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.RAIN,
                titleResId = R.string.seed_noise_rain_title,
                rawResId = ModelR.raw.rain_1,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.CITY_SNOW,
        titleResId = R.string.seed_scene_city_snow_title,
        subtitleResId = R.string.seed_scene_city_snow_subtitle,
        category = SceneCategory.FOCUS,
        sortOrder = 3,
        brightness = 0.4f,
        showCountdownClock = true,
        alarmReminderEnabled = true,
        backgroundResName = ProjectsIds.Background.CITY_SNOW,
        videoVolume = 0f,
        particleSettings = SceneParticleSettings(
            effect = SceneParticleEffect.Snow,
            intensity = 0.9f,
            wind = 0.6f,
            quality = SceneParticleQuality.High,
            foregroundGlassEnabled = false,
        ),
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.WIND,
                titleResId = R.string.seed_noise_wind_title,
                rawResId = ModelR.raw.wind_1,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.BOOK_ROOM,
        titleResId = R.string.seed_scene_book_room_title,
        subtitleResId = R.string.seed_scene_book_room_subtitle,
        category = SceneCategory.FOCUS,
        sortOrder = 4,
        brightness = 0.4f,
        showCountdownClock = true,
        alarmReminderEnabled = true,
        coverResName = "ic_cover_book_room",
        videoResId = ModelR.raw.focus_book_room,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.WIND,
                titleResId = R.string.seed_noise_wind_title,
                rawResId = ModelR.raw.wind_1,
                volume = 0.7f,
            ),
        ),
    ),

    // 助眠场景：炉雪深眠、月泊寒湖、雨宿灯眠、雪照归灯、轨光渡夜
    LocalSeedScene(
        id = ProjectsIds.Scene.FIREPLACE_SNOW,
        titleResId = R.string.seed_scene_fireplace_snow_title,
        subtitleResId = R.string.seed_scene_fireplace_snow_subtitle,
        category = SceneCategory.SLEEP,
        sortOrder = 0,
        brightness = 0.4f,
        showCountdownClock = false,
        alarmReminderEnabled = false,
        coverResName = "ic_cover_snow_room",
        backgroundResName = ProjectsIds.Background.SNOW_NIGHT,
        videoResId = ModelR.raw.sleep_cabin_snow,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.FIREPLACE,
                titleResId = R.string.seed_noise_fireplace_title,
                rawResId = ModelR.raw.fireplace_1,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.UNDER_MOON,
        titleResId = R.string.seed_scene_under_moon_title,
        subtitleResId = R.string.seed_scene_under_moon_subtitle,
        category = SceneCategory.SLEEP,
        sortOrder = 1,
        brightness = 0.3f,
        coverResName = ProjectsIds.Background.UNDER_MOON,
        backgroundResName = ProjectsIds.Background.UNDER_MOON,
        showCountdownClock = false,
        alarmReminderEnabled = false,
        particleSettings = SceneParticleSettings(
            effect = SceneParticleEffect.Fireflies,
            intensity = 0.9f,
            wind = 0.8f,
            quality = SceneParticleQuality.High,
            foregroundGlassEnabled = false,
        ),
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.INSECTS,
                titleResId = R.string.seed_noise_insects_title,
                rawResId = ModelR.raw.insects_1,
                volume = 0.8f,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.STORMY_BEDROOM,
        titleResId = R.string.seed_scene_stormy_bedroom_title,
        subtitleResId = R.string.seed_scene_stormy_bedroom_subtitle,
        category = SceneCategory.SLEEP,
        sortOrder = 2,
        brightness = 0.4f,
        showCountdownClock = false,
        alarmReminderEnabled = false,
        coverResName = "ic_cover_bedroom",
        videoResId = ModelR.raw.sleep_stormy_bedroom,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.FIREPLACE,
                titleResId = R.string.seed_noise_fireplace_title,
                rawResId = ModelR.raw.fireplace_1,
            ),
            LocalSeedAudio(
                id = ProjectsIds.Noise.THUNDER_AND_RAIN,
                titleResId = R.string.seed_noise_thunder_and_rain_title,
                rawResId = ModelR.raw.thunder_rain_1,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.SNOW_NIGHT,
        titleResId = R.string.seed_scene_snow_night_title,
        subtitleResId = R.string.seed_scene_snow_night_subtitle,
        category = SceneCategory.SLEEP,
        sortOrder = 3,
        brightness = 0.3f,
        coverResName = ProjectsIds.Background.SNOW_NIGHT,
        backgroundResName = ProjectsIds.Background.SNOW_NIGHT,
        showCountdownClock = false,
        alarmReminderEnabled = false,
        particleSettings = SceneParticleSettings(
            effect = SceneParticleEffect.Snow,
            intensity = 0.8f,
            wind = 0.1f,
            quality = SceneParticleQuality.High,
            foregroundGlassEnabled = false,
        ),
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.WIND,
                titleResId = R.string.seed_noise_wind_title,
                rawResId = ModelR.raw.wind_1,
                volume = 0.6f,
            ),
            LocalSeedAudio(
                id = ProjectsIds.Noise.FIREPLACE,
                titleResId = R.string.seed_noise_fireplace_title,
                rawResId = ModelR.raw.fireplace_1,
                volume = 0.6f,
            ),
        ),
    ),
    LocalSeedScene(
        id = ProjectsIds.Scene.TRAIN_NIGHT,
        titleResId = R.string.seed_scene_train_night_title,
        subtitleResId = R.string.seed_scene_train_night_subtitle,
        category = SceneCategory.SLEEP,
        sortOrder = 4,
        brightness = 0.3f,
        showCountdownClock = false,
        alarmReminderEnabled = false,
        coverResName = "ic_cover_train_night",
        videoResId = ModelR.raw.sleep_train_night,
        videoVolume = 0f,
        audioTracks = listOf(
            LocalSeedAudio(
                id = ProjectsIds.Noise.TRAIN,
                titleResId = R.string.seed_noise_train_title,
                rawResId = ModelR.raw.old_train_1,
                volume = 0.6f,
            ),
        ),
    ),
)
