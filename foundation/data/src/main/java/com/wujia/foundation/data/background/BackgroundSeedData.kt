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

package com.wujia.foundation.data.background

import androidx.annotation.DrawableRes
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.R as ModelR

internal data class LocalSeedBackground(val id: String, @param:DrawableRes val drawableResId: Int)

internal fun defaultLocalBackgrounds(): List<LocalSeedBackground> = listOf(
    LocalSeedBackground(
        id = ProjectsIds.Background.MORNING_MIST,
        drawableResId = ModelR.drawable.bg_morning_mist,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.SNOW_NIGHT,
        drawableResId = ModelR.drawable.bg_snow_night,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.AFTER_RAIN,
        drawableResId = ModelR.drawable.bg_after_rain,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.UNDER_MOON,
        drawableResId = ModelR.drawable.bg_under_moon,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.RURAL_NIGHT,
        drawableResId = ModelR.drawable.bg_rural_night,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.CITY_SNOW,
        drawableResId = ModelR.drawable.bg_city_snow,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.RIVER,
        drawableResId = ModelR.drawable.bg_river,
    ),
    LocalSeedBackground(
        id = ProjectsIds.Background.CABER_CITY,
        drawableResId = ModelR.drawable.bg_caber_city,
    ),
)
