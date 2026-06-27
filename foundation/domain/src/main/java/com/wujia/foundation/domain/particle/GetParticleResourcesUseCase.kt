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
package com.wujia.foundation.domain.particle

import com.wujia.foundation.model.particle.ParticleCategory
import com.wujia.foundation.model.particle.ParticleResource
import com.wujia.foundation.model.particle.ParticleResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取粒子效果资源。
 *
 * 同时提供流式观察和一次性查询能力，供编辑页和控制面板复用。
 */
class GetParticleResourcesUseCase @Inject constructor(
    private val particleResourceRepository: ParticleResourceRepository,
) {
    /**
     * 观察全部粒子效果资源列表。
     */
    operator fun invoke(): Flow<List<ParticleResource>> = particleResourceRepository.observeParticleResources()

    /**
     * 一次性读取全部粒子效果资源。
     */
    suspend fun getAll(): List<ParticleResource> = particleResourceRepository.getParticleResources()

    /**
     * 根据资源 ID 获取单个粒子效果。
     */
    suspend fun getById(id: String): ParticleResource? = particleResourceRepository.getParticleResource(id)

    /**
     * 按分类筛选粒子效果资源。
     */
    suspend fun getByCategory(category: ParticleCategory): List<ParticleResource> =
        particleResourceRepository.getParticleResourcesByCategory(category)
}
