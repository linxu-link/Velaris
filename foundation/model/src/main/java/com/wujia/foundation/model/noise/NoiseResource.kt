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
package com.wujia.foundation.model.noise

/**
 * Sound material resource exposed to feature modules.
 *
 * A noise resource is an independently selectable audio asset that can be used
 * when creating or editing scene audio tracks.
 */
data class NoiseResource(
    val id: String,
    val title: String,
    val description: String,
    val category: NoiseCategory,
    val uri: String,
    val tags: List<String> = emptyList(),
    val thumbnailResName: String? = null,
    val defaultVolume: Float = DEFAULT_NOISE_VOLUME,
    val loop: Boolean = true,
)

enum class NoiseCategory(val displayName: String) {
    NATURE("自然"),
    HEALING("治愈"),
    FOCUS("专注"),
    SLEEP("助眠"),
    LOCAL("本地"),
}

const val DEFAULT_NOISE_VOLUME = 1f
