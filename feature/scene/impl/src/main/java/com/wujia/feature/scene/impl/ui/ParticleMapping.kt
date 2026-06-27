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
package com.wujia.feature.scene.impl.ui

import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.particle.ParticleConfig
import com.wujia.foundation.particle.ParticleEffectType
import com.wujia.foundation.particle.RenderQuality

/**
 * 将持久化的 [SceneParticleSettings] 映射为渲染用的 [ParticleConfig]
 */
fun SceneParticleSettings.toParticleConfig(): ParticleConfig = ParticleConfig(
    effect = effect.toParticleEffect(),
    intensity = intensity,
    wind = wind,
    quality = quality.toParticleQuality(),
    foregroundGlassEnabled = foregroundGlassEnabled,
)

/**
 * 将 [SceneParticleEffect] 映射为 [ParticleEffectType]
 */
fun SceneParticleEffect.toParticleEffect(): ParticleEffectType = when (this) {
    SceneParticleEffect.None -> ParticleEffectType.None
    SceneParticleEffect.Snow -> ParticleEffectType.Snow
    SceneParticleEffect.Rain -> ParticleEffectType.Rain
    SceneParticleEffect.Fireflies -> ParticleEffectType.Fireflies
}

/**
 * 将 [SceneParticleQuality] 映射为 [RenderQuality]
 */
fun SceneParticleQuality.toParticleQuality(): RenderQuality = when (this) {
    SceneParticleQuality.Low -> RenderQuality.Low
    SceneParticleQuality.Medium -> RenderQuality.Medium
    SceneParticleQuality.High -> RenderQuality.High
    SceneParticleQuality.Ultra -> RenderQuality.Ultra
}
