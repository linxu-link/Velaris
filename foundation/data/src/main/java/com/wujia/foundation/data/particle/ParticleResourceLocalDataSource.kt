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

import android.content.Context
import com.wujia.foundation.model.particle.ParticleCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * 粒子效果本地数据源
 *
 * 数据常驻内存，不持久化到 Room。
 * 负责将 LocalSeedParticle (资源 ID) 转换为 LocalParticleResource (实际字符串)。
 */
class ParticleResourceLocalDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val localParticles: List<LocalParticleResource> =
        defaultLocalParticles().map { it.asLocalModel() }

    /**
     * 观察所有粒子效果资源
     */
    internal fun observeParticleResources(): Flow<List<LocalParticleResource>> = flowOf(localParticles)

    /**
     * 获取所有粒子效果资源
     */
    internal suspend fun getParticleResources(): List<LocalParticleResource> = localParticles

    /**
     * 根据 ID 获取粒子效果资源
     */
    internal suspend fun getParticleResource(id: String): LocalParticleResource? = localParticles.find { it.id == id }

    /**
     * 根据分类获取粒子效果资源
     */
    internal suspend fun getParticleResourcesByCategory(category: ParticleCategory): List<LocalParticleResource> =
        localParticles.filter {
            it.category ==
                category
        }

    /**
     * 将种子数据转换为本地资源模型
     */
    private fun LocalSeedParticle.asLocalModel(): LocalParticleResource = LocalParticleResource(
        id = id,
        title = context.getString(titleResId),
        description = context.getString(descriptionResId),
        category = category,
        effect = effect,
        intensity = intensity,
        wind = wind,
        quality = quality,
        foregroundGlassEnabled = foregroundGlassEnabled,
        tags = emptyList(),
        thumbnailResName = thumbnailResName,
    )
}
