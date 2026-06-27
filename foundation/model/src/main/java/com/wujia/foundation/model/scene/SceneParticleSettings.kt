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
package com.wujia.foundation.model.scene

import com.wujia.foundation.model.particle.DEFAULT_PARTICLE_INTENSITY
import com.wujia.foundation.model.particle.DEFAULT_PARTICLE_WIND

/**
 * 场景粒子效果类型
 *
 * 与 `foundation:particle.ParticleEffectType` 对应，但作为持久化模型保持独立。
 */
enum class SceneParticleEffect {
    None,
    Snow,
    Rain,
    Fireflies,
}

/**
 * 场景粒子质量等级
 *
 * 与 `foundation:particle.RenderQuality` 对应，但作为持久化模型保持独立。
 */
enum class SceneParticleQuality {
    Low,
    Medium,
    High,
    Ultra,
}

/**
 * 场景粒子设置
 *
 * 可持久化的粒子配置模型，存储在场景设置中。
 * 通过映射函数转换为 `foundation:particle.ParticleConfig` 进行渲染。
 *
 * @property effect 粒子效果类型
 * @property intensity 强度 (0.0 - 1.0)
 * @property wind 风力 (0.0 - 1.0)
 * @property quality 质量等级
 * @property foregroundGlassEnabled 是否启用前景玻璃水痕效果（仅雨天有效）
 */
data class SceneParticleSettings(
    val effect: SceneParticleEffect = SceneParticleEffect.None,
    val intensity: Float = DEFAULT_PARTICLE_INTENSITY,
    val wind: Float = DEFAULT_PARTICLE_WIND,
    val quality: SceneParticleQuality = SceneParticleQuality.Medium,
    val foregroundGlassEnabled: Boolean = true,
) {
    init {
        require(intensity in 0f..1f) { "intensity must be in 0..1, got $intensity" }
        require(wind in 0f..1f) { "wind must be in 0..1, got $wind" }
    }
}

/** 无粒子设置 */
val NO_PARTICLE = SceneParticleSettings(effect = SceneParticleEffect.None)
