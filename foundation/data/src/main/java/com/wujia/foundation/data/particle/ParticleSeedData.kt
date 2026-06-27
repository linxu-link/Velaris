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

package com.wujia.foundation.data.particle

import androidx.annotation.StringRes
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.particle.DEFAULT_PARTICLE_INTENSITY
import com.wujia.foundation.model.particle.DEFAULT_PARTICLE_WIND
import com.wujia.foundation.model.particle.ParticleCategory
import com.wujia.foundation.model.particle.ParticleEffect
import com.wujia.foundation.model.particle.ParticleQuality
import com.wujia.foundation.ui.R

/**
 * 粒子效果种子数据模板
 *
 * 使用资源 ID 而非硬编码字符串，支持多语言。
 */
internal data class LocalSeedParticle(
    val id: String,
    @param:StringRes val titleResId: Int,
    @param:StringRes val descriptionResId: Int,
    val category: ParticleCategory,
    val effect: ParticleEffect,
    val intensity: Float = DEFAULT_PARTICLE_INTENSITY,
    val wind: Float = DEFAULT_PARTICLE_WIND,
    val quality: ParticleQuality = ParticleQuality.MEDIUM,
    val foregroundGlassEnabled: Boolean = true,
    val thumbnailResName: String? = null,
)

/**
 * 默认粒子效果列表
 */
internal fun defaultLocalParticles(): List<LocalSeedParticle> = listOf(
    // 雨天效果
    LocalSeedParticle(
        id = ProjectsIds.Particle.LIGHT_RAIN,
        titleResId = R.string.seed_particle_light_rain_title,
        descriptionResId = R.string.seed_particle_light_rain_description,
        category = ParticleCategory.RAIN,
        effect = ParticleEffect.RAIN,
        intensity = 0.4f,
        wind = 0.1f,
        quality = ParticleQuality.LOW,
        foregroundGlassEnabled = false,
        thumbnailResName = "ic_rain_1",
    ),
    LocalSeedParticle(
        id = ProjectsIds.Particle.MODERATE_RAIN,
        titleResId = R.string.seed_particle_moderate_rain_title,
        descriptionResId = R.string.seed_particle_moderate_rain_description,
        category = ParticleCategory.RAIN,
        effect = ParticleEffect.RAIN,
        intensity = 0.72f,
        wind = 0.3f,
        quality = ParticleQuality.MEDIUM,
        foregroundGlassEnabled = true,
        thumbnailResName = "ic_rain_2",
    ),
    LocalSeedParticle(
        id = ProjectsIds.Particle.HEAVY_RAIN,
        titleResId = R.string.seed_particle_heavy_rain_title,
        descriptionResId = R.string.seed_particle_heavy_rain_description,
        category = ParticleCategory.STORM,
        effect = ParticleEffect.RAIN,
        intensity = 0.95f,
        wind = 0.7f,
        quality = ParticleQuality.HIGH,
        foregroundGlassEnabled = true,
        thumbnailResName = "ic_rain_3",
    ),

    // 雪天效果
    LocalSeedParticle(
        id = ProjectsIds.Particle.LIGHT_SNOW,
        titleResId = R.string.seed_particle_light_snow_title,
        descriptionResId = R.string.seed_particle_light_snow_description,
        category = ParticleCategory.SNOW,
        effect = ParticleEffect.SNOW,
        intensity = 0.35f,
        wind = 0.05f,
        quality = ParticleQuality.LOW,
        foregroundGlassEnabled = false,
        thumbnailResName = "ic_snow_1",
    ),
    LocalSeedParticle(
        id = ProjectsIds.Particle.MODERATE_SNOW,
        titleResId = R.string.seed_particle_moderate_snow_title,
        descriptionResId = R.string.seed_particle_moderate_snow_description,
        category = ParticleCategory.SNOW,
        effect = ParticleEffect.SNOW,
        intensity = 0.65f,
        wind = 0.15f,
        quality = ParticleQuality.MEDIUM,
        foregroundGlassEnabled = false,
        thumbnailResName = "ic_snow_2",
    ),
    LocalSeedParticle(
        id = ProjectsIds.Particle.BLIZZARD,
        titleResId = R.string.seed_particle_blizzard_title,
        descriptionResId = R.string.seed_particle_blizzard_description,
        category = ParticleCategory.STORM,
        effect = ParticleEffect.SNOW,
        intensity = 0.9f,
        wind = 0.6f,
        quality = ParticleQuality.HIGH,
        foregroundGlassEnabled = false,
        thumbnailResName = "ic_snow_3",
    ),
    // 萤火虫效果
    LocalSeedParticle(
        id = ProjectsIds.Particle.FIREFLIES,
        titleResId = R.string.seed_particle_fireflies_title,
        descriptionResId = R.string.seed_particle_fireflies_description,
        category = ParticleCategory.CALM,
        effect = ParticleEffect.FIREFLIES,
        intensity = 0.72f,
        wind = 0.1f,
        quality = ParticleQuality.MEDIUM,
        foregroundGlassEnabled = false,
        thumbnailResName = "ic_fireflies",
    ),
)
