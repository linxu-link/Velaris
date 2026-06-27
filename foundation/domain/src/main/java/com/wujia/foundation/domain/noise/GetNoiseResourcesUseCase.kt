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
package com.wujia.foundation.domain.noise

import com.wujia.foundation.model.noise.NoiseResource
import com.wujia.foundation.model.noise.NoiseResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 观察环境音资源列表。
 *
 * 供场景编辑和控制面板展示可选音频资源。
 */
class GetNoiseResourcesUseCase @Inject constructor(private val noiseResourceRepository: NoiseResourceRepository) {
    operator fun invoke(): Flow<List<NoiseResource>> = noiseResourceRepository.observeNoiseResources()
}
