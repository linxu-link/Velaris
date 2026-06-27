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
@file:Suppress("ktlint:standard:filename")

package com.wujia.foundation.data.noise

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.ui.R
import com.wujia.foundation.model.R as ModelR

internal data class LocalSeedNoise(
    val id: String,
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int,
    val category: NoiseCategory,
    @param:RawRes val rawResId: Int,
    val thumbnailResName: String? = "ic_rain_1",
    val defaultVolume: Float = 1f,
    val loop: Boolean = true,
)

internal fun defaultLocalNoises(): List<LocalSeedNoise> = listOf(
    // 自然声：雨声、暴雨声、风声、雷声、暴雨与雷声
    LocalSeedNoise(
        id = ProjectsIds.Noise.RAIN,
        titleResId = R.string.seed_noise_rain_title,
        descriptionResId = R.string.seed_noise_rain_description,
        category = NoiseCategory.NATURE,
        rawResId = ModelR.raw.rain_1,
        thumbnailResName = "ic_noise_rain",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.RAIN_HEAVY,
        titleResId = R.string.seed_noise_rain_heavy_title,
        descriptionResId = R.string.seed_noise_rain_heavy_description,
        category = NoiseCategory.NATURE,
        rawResId = ModelR.raw.rain_2,
        thumbnailResName = "ic_noise_heavy_rain",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.WIND,
        titleResId = R.string.seed_noise_wind_title,
        descriptionResId = R.string.seed_noise_wind_description,
        category = NoiseCategory.NATURE,
        rawResId = ModelR.raw.wind_1,
        thumbnailResName = "ic_noise_wind",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.THUNDER,
        titleResId = R.string.seed_noise_thunder_title,
        descriptionResId = R.string.seed_noise_thunder_description,
        category = NoiseCategory.NATURE,
        rawResId = ModelR.raw.thunder_1,
        thumbnailResName = "ic_noise_thunder",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.THUNDER_AND_RAIN,
        titleResId = R.string.seed_noise_thunder_and_rain_title,
        descriptionResId = R.string.seed_noise_thunder_and_rain_description,
        category = NoiseCategory.NATURE,
        rawResId = ModelR.raw.thunder_rain_1,
        thumbnailResName = "ic_noise_thunder_and_rain",
    ),
    // 治疗声：风铃、海浪、钢琴、海鸥、河流
    LocalSeedNoise(
        id = ProjectsIds.Noise.WIND_CHIME,
        titleResId = R.string.seed_noise_wind_chime_title,
        descriptionResId = R.string.seed_noise_wind_chime_description,
        category = NoiseCategory.HEALING,
        rawResId = ModelR.raw.wind_chime_1,
        thumbnailResName = "ic_noise_wind_chime",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.OCEAN,
        titleResId = R.string.seed_noise_ocean_title,
        descriptionResId = R.string.seed_noise_ocean_description,
        category = NoiseCategory.HEALING,
        rawResId = ModelR.raw.ocean_1,
        thumbnailResName = "ic_noise_ocean_seagull",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.PIANO,
        titleResId = R.string.seed_noise_piano_title,
        descriptionResId = R.string.seed_noise_piano_description,
        category = NoiseCategory.HEALING,
        rawResId = ModelR.raw.piano_1,
        thumbnailResName = "ic_noise_piano",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.SEAGULL,
        titleResId = R.string.seed_noise_seagull_title,
        descriptionResId = R.string.seed_noise_seagull_description,
        category = NoiseCategory.HEALING,
        rawResId = ModelR.raw.seagull_1,
        thumbnailResName = "ic_noise_seagull",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.RIVER,
        titleResId = R.string.seed_noise_river_title,
        descriptionResId = R.string.seed_noise_river_description,
        category = NoiseCategory.HEALING,
        rawResId = ModelR.raw.river_1,
        thumbnailResName = "ic_noise_river",
    ),

    // 专注: 虫鸣、风铃声、翻书声、翻页时钟、打字声、写字声
    LocalSeedNoise(
        id = ProjectsIds.Noise.INSECTS,
        titleResId = R.string.seed_noise_insects_title,
        descriptionResId = R.string.seed_noise_insects_description,
        category = NoiseCategory.FOCUS,
        rawResId = ModelR.raw.insects_1,
        thumbnailResName = "ic_noise_insects",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.PAGE_TURN,
        titleResId = R.string.seed_noise_page_turn_title,
        descriptionResId = R.string.seed_noise_page_turn_description,
        category = NoiseCategory.FOCUS,
        rawResId = ModelR.raw.page_turn_1,
        thumbnailResName = "ic_noise_page_turn",
    ),
//    LocalSeedNoise(
//        id = ProjectsIds.Noise.FLIP_CLOCK,
//        titleResId = R.string.seed_noise_flip_clock_title,
//        descriptionResId = R.string.seed_noise_flip_clock_description,
//        category = NoiseCategory.FOCUS,
//        rawResId = ModelR.raw.flip_clock_1,
//        thumbnailResName = "ic_noise_flip_clock",
//    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.TYPING,
        titleResId = R.string.seed_noise_typing_title,
        descriptionResId = R.string.seed_noise_typing_description,
        category = NoiseCategory.FOCUS,
        rawResId = ModelR.raw.typing_1,
        thumbnailResName = "ic_noise_typing",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.WRITING,
        titleResId = R.string.seed_noise_writing_title,
        descriptionResId = R.string.seed_noise_writing_description,
        category = NoiseCategory.FOCUS,
        rawResId = ModelR.raw.pencil_1,
        thumbnailResName = "ic_noise_writing",
    ),

    // 助眠：列车、炉火
    LocalSeedNoise(
        id = ProjectsIds.Noise.TRAIN,
        titleResId = R.string.seed_noise_train_title,
        descriptionResId = R.string.seed_noise_train_description,
        category = NoiseCategory.SLEEP,
        rawResId = ModelR.raw.old_train_1,
        thumbnailResName = "ic_noise_train",
    ),
    LocalSeedNoise(
        id = ProjectsIds.Noise.FIREPLACE,
        titleResId = R.string.seed_noise_fireplace_title,
        descriptionResId = R.string.seed_noise_fireplace_description,
        category = NoiseCategory.SLEEP,
        rawResId = ModelR.raw.fireplace_1,
        thumbnailResName = "ic_noise_hearth_fire",
    ),
)
