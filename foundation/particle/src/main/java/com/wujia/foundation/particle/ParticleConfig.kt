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
package com.wujia.foundation.particle

import androidx.compose.runtime.Immutable

/**
 * 粒子效果类型
 */
enum class ParticleEffectType {
    /** 无效果 */
    None,

    /** 下雪 */
    Snow,

    /** 下雨 */
    Rain,

    /** 萤火虫 */
    Fireflies,
}

/**
 * 渲染质量等级
 */
enum class RenderQuality {
    /** 低质量：最少粒子，无溅射或水痕 */
    Low,

    /** 中等质量：默认行为 */
    Medium,

    /** 高质量：更密集粒子，更丰富的前景细节 */
    High,

    /** 超高质量：在高质量基础上翻倍粒子数量 */
    Ultra,
}

/**
 * 粒子配置数据类
 *
 * @property effect 粒子效果类型
 * @property intensity 强度 (0.0 - 1.0)
 * @property wind 风力 (0.0 - 1.0)
 * @property quality 质量等级
 * @property foregroundGlassEnabled 是否启用前景玻璃水痕效果（仅雨天有效）
 */
@Immutable
data class ParticleConfig(
    val effect: ParticleEffectType = ParticleEffectType.None,
    val intensity: Float = 0.72f,
    val wind: Float = 0.2f,
    val quality: RenderQuality = RenderQuality.Medium,
    val foregroundGlassEnabled: Boolean = true,
) {
    init {
        require(intensity in 0f..1f) { "intensity must be in 0..1, got $intensity" }
        require(wind in 0f..1f) { "wind must be in 0..1, got $wind" }
    }
}

/**
 * 雪花配置
 *
 * @property flakesPerMillionPixels 每百万像素的雪花数量
 * @property minFlakes 最少雪花数
 * @property maxFlakes 最多雪花数
 * @property nearFlakeRatio 近景雪花比例 (0.0 - 0.5)
 */
@Immutable
data class SnowConfig(
    val flakesPerMillionPixels: Int = 260,
    val minFlakes: Int = 90,
    val maxFlakes: Int = 380,
    val nearFlakeRatio: Float = 0.14f,
) {
    init {
        require(nearFlakeRatio in 0f..0.5f) { "nearFlakeRatio must be in 0..0.5, got $nearFlakeRatio" }
    }
}

/**
 * 雨滴配置
 *
 * @property dropsPerMillionPixels 每百万像素的雨滴数量
 * @property minDrops 最少雨滴数
 * @property maxDrops 最多雨滴数
 * @property splashRatio 溅射比例 (0.0 - 0.5)
 * @property screenStreakCount 屏幕水痕数量
 */
@Immutable
data class RainConfig(
    val dropsPerMillionPixels: Int = 360,
    val minDrops: Int = 120,
    val maxDrops: Int = 520,
    val splashRatio: Float = 0.12f,
    val screenStreakCount: Int = 18,
) {
    init {
        require(splashRatio in 0f..0.5f) { "splashRatio must be in 0..0.5, got $splashRatio" }
    }
}

/**
 * 创建基于质量等级的雪花配置
 */
fun snowConfigForQuality(quality: RenderQuality): SnowConfig = when (quality) {
    RenderQuality.Low -> SnowConfig(
        flakesPerMillionPixels = 140,
        minFlakes = 50,
        maxFlakes = 200,
        nearFlakeRatio = 0.08f,
    )
    RenderQuality.Medium -> SnowConfig()
    RenderQuality.High -> SnowConfig(
        flakesPerMillionPixels = 380,
        minFlakes = 140,
        maxFlakes = 520,
        nearFlakeRatio = 0.2f,
    )
    RenderQuality.Ultra -> SnowConfig(
        flakesPerMillionPixels = 760,
        minFlakes = 280,
        maxFlakes = 1040,
        nearFlakeRatio = 0.2f,
    )
}

/**
 * 创建基于质量等级的雨滴配置
 */
fun rainConfigForQuality(quality: RenderQuality): RainConfig = when (quality) {
    RenderQuality.Low -> RainConfig(
        dropsPerMillionPixels = 200,
        minDrops = 80,
        maxDrops = 300,
        splashRatio = 0f,
        screenStreakCount = 0,
    )
    RenderQuality.Medium -> RainConfig()
    RenderQuality.High -> RainConfig(
        dropsPerMillionPixels = 500,
        minDrops = 180,
        maxDrops = 700,
        splashRatio = 0.18f,
        screenStreakCount = 28,
    )
    RenderQuality.Ultra -> RainConfig(
        dropsPerMillionPixels = 1000,
        minDrops = 360,
        maxDrops = 1400,
        splashRatio = 0.18f,
        screenStreakCount = 56,
    )
}

/**
 * 萤火虫配置
 *
 * @property firefliesPerMillionPixels 每百万像素的萤火虫数量
 * @property minFireflies 最少萤火虫数
 * @property maxFireflies 最多萤火虫数
 */
@Immutable
data class FireflyConfig(
    val firefliesPerMillionPixels: Int = 15,
    val minFireflies: Int = 12,
    val maxFireflies: Int = 50,
)

/**
 * 创建基于质量等级的萤火虫配置
 */
fun fireflyConfigForQuality(quality: RenderQuality): FireflyConfig = when (quality) {
    RenderQuality.Low -> FireflyConfig(
        firefliesPerMillionPixels = 8,
        minFireflies = 6,
        maxFireflies = 25,
    )
    RenderQuality.Medium -> FireflyConfig()
    RenderQuality.High -> FireflyConfig(
        firefliesPerMillionPixels = 24,
        minFireflies = 18,
        maxFireflies = 80,
    )
    RenderQuality.Ultra -> FireflyConfig(
        firefliesPerMillionPixels = 40,
        minFireflies = 30,
        maxFireflies = 140,
    )
}
