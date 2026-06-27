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
package com.wujia.foundation.domain.scene

import com.wujia.foundation.model.scene.SceneResourceRepository
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

/**
 * 观察场景资源列表。
 *
 * 统一封装列表读取入口，并在上游异常时回退为空列表，避免界面层直接处理流异常。
 */
class ObserveSceneResourcesUseCase @Inject constructor(private val sceneResourceRepository: SceneResourceRepository) {
    operator fun invoke() = sceneResourceRepository.observeSceneResources()
        .catch {
            emit(emptyList())
        }
}
