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

package com.wujia.foundation.data.video

import androidx.annotation.RawRes
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.R as ModelR

internal data class LocalSeedVideo(
    val id: String,
    @param:RawRes val rawResId: Int,
    val thumbnailResName: String? = null,
)

internal fun defaultLocalVideos(): List<LocalSeedVideo> = listOf(
    LocalSeedVideo(
        id = ProjectsIds.Video.FOCUS_CAFE_RAIN,
        rawResId = ModelR.raw.focus_rainy_cafe,
        thumbnailResName = "ic_cover_cafe_rain",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.FOCUS_WIND_CHIME,
        rawResId = ModelR.raw.focus_wind_chime,
        thumbnailResName = "ic_cover_wind_chime",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.FOCUS_BOOK_ROOM,
        rawResId = ModelR.raw.focus_book_room,
        thumbnailResName = "ic_cover_book_room",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.SLEEP_TRAIN_NIGHT,
        rawResId = ModelR.raw.sleep_train_night,
        thumbnailResName = "ic_cover_train_night",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.SLEEP_FIREPLACE,
        rawResId = ModelR.raw.sleep_cabin_snow,
        thumbnailResName = "ic_cover_snow_room",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.SLEEP_STORMY_BEDROOM,
        rawResId = ModelR.raw.sleep_stormy_bedroom,
        thumbnailResName = "ic_cover_bedroom",
    ),
    LocalSeedVideo(
        id = ProjectsIds.Video.SLEEP_TRAIN_NIGHT_V2,
        rawResId = ModelR.raw.sleep_train_night_v2,
        thumbnailResName = "ic_cover_train_night_v2",
    ),
)
