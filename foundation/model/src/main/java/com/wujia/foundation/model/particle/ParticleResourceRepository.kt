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

import kotlinx.coroutines.flow.Flow

/**
 * 粒子效果资源仓库接口
 *
 * 定义在 foundation/model 中，由 foundation/data 实现。
 * 提供粒子效果预设的查询能力。
 */
interface ParticleResourceRepository {
    /**
     * 观察所有粒子效果资源
     */
    fun observeParticleResources(): Flow<List<ParticleResource>>

    /**
     * 获取所有粒子效果资源
     */
    suspend fun getParticleResources(): List<ParticleResource>

    /**
     * 根据 ID 获取粒子效果资源
     */
    suspend fun getParticleResource(id: String): ParticleResource?

    /**
     * 根据分类获取粒子效果资源
     */
    suspend fun getParticleResourcesByCategory(category: ParticleCategory): List<ParticleResource>
}
