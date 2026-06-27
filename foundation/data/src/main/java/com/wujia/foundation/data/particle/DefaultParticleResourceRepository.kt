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
import com.wujia.foundation.model.particle.ParticleResource
import com.wujia.foundation.model.particle.ParticleResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 粒子效果资源仓库实现
 *
 * 核心职责：LocalParticleResource -> ParticleResource 的模型转换。
 */
class DefaultParticleResourceRepository @Inject constructor(
    private val localDataSource: ParticleResourceLocalDataSource,
) : ParticleResourceRepository {

    override fun observeParticleResources(): Flow<List<ParticleResource>> =
        localDataSource.observeParticleResources().map { particles ->
            particles.map { it.asExternalModel() }
        }

    override suspend fun getParticleResources(): List<ParticleResource> = localDataSource.getParticleResources().map {
        it.asExternalModel()
    }

    override suspend fun getParticleResource(id: String): ParticleResource? =
        localDataSource.getParticleResource(id)?.asExternalModel()

    override suspend fun getParticleResourcesByCategory(category: ParticleCategory): List<ParticleResource> =
        localDataSource.getParticleResourcesByCategory(category).map {
            it.asExternalModel()
        }

    /**
     * 将本地资源转换为外部领域模型
     */
    private fun LocalParticleResource.asExternalModel(): ParticleResource = ParticleResource(
        id = id,
        title = title,
        description = description,
        category = category,
        effect = effect,
        intensity = intensity,
        wind = wind,
        quality = quality,
        foregroundGlassEnabled = foregroundGlassEnabled,
        tags = tags,
        thumbnailResName = thumbnailResName,
    )
}
