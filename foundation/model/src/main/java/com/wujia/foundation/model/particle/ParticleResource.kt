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
package com.wujia.foundation.model.particle

import com.wujia.foundation.model.R
import com.wujia.foundation.toolkit.HiToolKit

/**
 * 粒子效果资源
 *
 * 代表一个可选择的粒子效果预设，用于场景编辑器的粒子效果选择步骤。
 * 与 SceneParticleSettings 对应，但作为资源模型保持独立。
 */
data class ParticleResource(
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

private fun particleEffectDisplayName(effect: ParticleEffect): String = runCatching {
    when (effect) {
        ParticleEffect.NONE -> HiToolKit.res.getString(R.string.particle_effect_none)
        ParticleEffect.RAIN -> HiToolKit.res.getString(R.string.particle_effect_rain)
        ParticleEffect.SNOW -> HiToolKit.res.getString(R.string.particle_effect_snow)
        ParticleEffect.FIREFLIES -> HiToolKit.res.getString(R.string.particle_effect_fireflies)
    }
}.getOrElse {
    when (effect) {
        ParticleEffect.NONE -> "无"
        ParticleEffect.RAIN -> "雨"
        ParticleEffect.SNOW -> "雪"
        ParticleEffect.FIREFLIES -> "萤火虫"
    }
}

/**
 * 粒子效果类型
 *
 * 与 foundation:particle.ParticleEffectType 对应
 */
enum class ParticleEffect {
    NONE,
    RAIN,
    SNOW,
    FIREFLIES,
    ;

    val displayName: String
        get() = particleEffectDisplayName(this)
}

fun ParticleEffect.toDisplayName(): String = displayName

/**
 * 粒子效果分类
 */
private fun particleCategoryDisplayName(category: ParticleCategory): String = runCatching {
    when (category) {
        ParticleCategory.RAIN -> HiToolKit.res.getString(R.string.particle_category_rain)
        ParticleCategory.SNOW -> HiToolKit.res.getString(R.string.particle_category_snow)
        ParticleCategory.CALM -> HiToolKit.res.getString(R.string.particle_category_calm)
        ParticleCategory.STORM -> HiToolKit.res.getString(R.string.particle_category_storm)
    }
}.getOrElse {
    when (category) {
        ParticleCategory.RAIN -> "雨天"
        ParticleCategory.SNOW -> "雪天"
        ParticleCategory.CALM -> "静谧"
        ParticleCategory.STORM -> "风暴"
    }
}

enum class ParticleCategory {
    RAIN,
    SNOW,
    CALM,
    STORM,
    ;

    val displayName: String
        get() = particleCategoryDisplayName(this)
}

fun ParticleCategory.toDisplayName(): String = displayName

/**
 * 粒子效果质量
 *
 * 与 foundation:particle.RenderQuality 对应
 */
private fun particleQualityDisplayName(quality: ParticleQuality): String = runCatching {
    when (quality) {
        ParticleQuality.LOW -> HiToolKit.res.getString(R.string.particle_quality_low)
        ParticleQuality.MEDIUM -> HiToolKit.res.getString(R.string.particle_quality_medium)
        ParticleQuality.HIGH -> HiToolKit.res.getString(R.string.particle_quality_high)
    }
}.getOrElse {
    when (quality) {
        ParticleQuality.LOW -> "低"
        ParticleQuality.MEDIUM -> "中"
        ParticleQuality.HIGH -> "高"
    }
}

enum class ParticleQuality {
    LOW,
    MEDIUM,
    HIGH,
    ;

    val displayName: String
        get() = particleQualityDisplayName(this)
}

/**
 * 默认粒子效果强度
 */
const val DEFAULT_PARTICLE_INTENSITY = 0.72f

/**
 * 默认粒子效果风力
 */
const val DEFAULT_PARTICLE_WIND = 0.2f
