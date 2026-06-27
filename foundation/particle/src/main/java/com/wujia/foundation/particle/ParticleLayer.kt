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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 粒子效果层
 *
 * 根据 [ParticleConfig] 自动切换粒子效果，对外部只暴露这一个入口。
 * 内部使用 TextureView + 独立渲染线程，不阻塞主线程。
 *
 * 使用示例：
 * ```kotlin
 * ParticleLayer(
 *     config = ParticleConfig(
 *         effect = ParticleEffectType.Rain,
 *         intensity = 0.8f,
 *         wind = 0.3f,
 *         quality = RenderQuality.Medium,
 *     ),
 *     active = isCurrentPage,
 * )
 * ```
 *
 * @param modifier Modifier
 * @param config 粒子配置
 * @param active 是否激活（仅当前页面激活时才运行动画，节省性能）
 */
@Composable
fun ParticleLayer(modifier: Modifier = Modifier, config: ParticleConfig = ParticleConfig(), active: Boolean = true) {
    if (config.effect == ParticleEffectType.None) return
    TextureParticleLayer(
        modifier = modifier,
        config = config,
        active = active,
    )
}
