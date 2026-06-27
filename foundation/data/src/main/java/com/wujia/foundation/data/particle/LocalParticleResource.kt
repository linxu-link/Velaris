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
package com.wujia.foundation.data.particle

import com.wujia.foundation.model.particle.ParticleCategory
import com.wujia.foundation.model.particle.ParticleEffect
import com.wujia.foundation.model.particle.ParticleQuality

/**
 * 本地粒子效果资源（内部模型）
 *
 * data 层内部使用，不对外暴露。
 * 与 ParticleResource 的区别：存储实际配置参数而非资源 URI。
 */
internal data class LocalParticleResource(
    val id: String,
    val title: String,
    val description: String,
    val category: ParticleCategory,
    val effect: ParticleEffect,
    val intensity: Float,
    val wind: Float,
    val quality: ParticleQuality,
    val foregroundGlassEnabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val thumbnailResName: String? = null,
)
