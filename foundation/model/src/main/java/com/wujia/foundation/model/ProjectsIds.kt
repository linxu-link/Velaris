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
package com.wujia.foundation.model

/**
 * 统一管理项目默认 seed 资源的固定 ID。
 *
 * 这些常量会同时被 data 层 seed 数据和 feature 层预览/测试使用。
 */
object ProjectsIds {
    object Scene {
        const val SNOW_NIGHT = "snow-night"
        const val AFTER_RAIN = "after-rain"
        const val UNDER_MOON = "under-moon"
        const val WIND_CHIME = "wind-chime"
        const val CITY_SNOW = "city-snow"
        const val BOOK_ROOM = "book-room"
        const val FIREPLACE_SNOW = "fireplace-snow"
        const val RAINY_CAFE = "rainy-cafe"
        const val STORMY_BEDROOM = "stormy-bedroom"
        const val TRAIN_NIGHT = "train-night"
    }

    object Noise {
        const val RAIN = "rain"
        const val RAIN_HEAVY = "rain-heavy"
        const val FIREPLACE = "fireplace"
        const val WIND = "wind"
        const val OCEAN = "ocean"
        const val PIANO = "piano"
        const val THUNDER = "thunder"
        const val THUNDER_AND_RAIN = "thunder-and-rain"
        const val SEAGULL = "seagull"
        const val INSECTS = "insects"
        const val TRAIN = "train"
        const val WIND_CHIME = "wind-chime"
        const val PAGE_TURN = "page-turn"
        const val RIVER = "river"
        const val TYPING = "typing"
        const val WRITING = "writing"
    }

    object Background {
        const val MORNING_MIST = "bg_morning_mist"
        const val SNOW_NIGHT = "bg_snow_night"
        const val AFTER_RAIN = "bg_after_rain"
        const val UNDER_MOON = "bg_under_moon"
        const val RURAL_NIGHT = "bg_rural_night"
        const val CITY_SNOW = "bg_city_snow"
        const val RIVER = "bg_river"
        const val CABER_CITY = "bg_caber_city"
    }

    object Video {
        const val FOCUS_WIND_CHIME = "focus_wind_chime"
        const val FOCUS_CAFE_RAIN = "focus_cafe_rain"
        const val FOCUS_BOOK_ROOM = "focus_book_room"
        const val SLEEP_FIREPLACE = "sleep_cabin_snow"
        const val SLEEP_STORMY_BEDROOM = "sleep_stormy_bedroom"
        const val SLEEP_TRAIN_NIGHT = "sleep_train_night"
        const val SLEEP_TRAIN_NIGHT_V2 = "sleep_train_night_v2"
    }

    object Particle {
        const val LIGHT_RAIN = "light-rain"
        const val MODERATE_RAIN = "moderate-rain"
        const val HEAVY_RAIN = "heavy-rain"
        const val LIGHT_SNOW = "light-snow"
        const val MODERATE_SNOW = "moderate-snow"
        const val BLIZZARD = "blizzard"
        const val FIREFLIES = "fireflies"
    }
}
