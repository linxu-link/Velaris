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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ParticleLayer 使用示例
 *
 * 展示如何在场景中集成粒子效果
 */
@Composable
fun ParticleLayerUsageExample() {
    // 示例 1: 基础下雨效果
    Box(modifier = Modifier.fillMaxSize()) {
        // 场景背景...
        // 暗色遮罩...

        // 粒子层
        ParticleLayer(
            config = ParticleConfig(
                effect = ParticleEffectType.Rain,
                intensity = 0.8f,
                wind = 0.3f,
                quality = RenderQuality.Medium,
            ),
            active = true,
            modifier = Modifier.fillMaxSize(),
        )

        // 标题层...
    }
}

/**
 * 示例 2: 下雪效果
 */
@Composable
fun SnowEffectExample() {
    ParticleLayer(
        config = ParticleConfig(
            effect = ParticleEffectType.Snow,
            intensity = 0.6f,
            wind = 0.15f,
            quality = RenderQuality.High,
        ),
        active = true,
        modifier = Modifier.fillMaxSize(),
    )
}

/**
 * 示例 4: 低质量模式（省电）
 */
@Composable
fun LowQualityRainExample() {
    ParticleLayer(
        config = ParticleConfig(
            effect = ParticleEffectType.Rain,
            intensity = 0.5f,
            wind = 0.1f,
            quality = RenderQuality.Low,
            foregroundGlassEnabled = false, // 禁用前景水痕效果
        ),
        active = true,
        modifier = Modifier.fillMaxSize(),
    )
}

/**
 * 示例 5: 根据配置动态切换
 */
@Composable
fun DynamicParticleExample(
    particleEffect: ParticleEffectType = ParticleEffectType.Rain,
    intensity: Float = 0.72f,
    wind: Float = 0.2f,
) {
    ParticleLayer(
        config = ParticleConfig(
            effect = particleEffect,
            intensity = intensity,
            wind = wind,
            quality = RenderQuality.Medium,
        ),
        active = true,
        modifier = Modifier.fillMaxSize(),
    )
}
