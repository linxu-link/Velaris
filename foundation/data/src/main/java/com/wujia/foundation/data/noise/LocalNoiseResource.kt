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
package com.wujia.foundation.data.noise

import com.wujia.foundation.model.noise.NoiseCategory

/**
 * Local sound material used inside the data layer.
 *
 * The raw resource id is resolved to an android.resource URI by the repository
 * so upper layers can consume the same URI shape as scene audio tracks.
 */
internal data class LocalNoiseResource(
    val id: String,
    val title: String,
    val description: String,
    val category: NoiseCategory,
    val rawResId: Int,
    val tags: List<String> = emptyList(),
    val thumbnailResName: String? = null,
    val defaultVolume: Float = 1f,
    val loop: Boolean = true,
)
