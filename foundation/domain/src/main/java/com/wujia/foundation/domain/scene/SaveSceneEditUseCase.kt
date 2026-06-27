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

import com.wujia.foundation.model.scene.SceneEditInput
import com.wujia.foundation.model.scene.SceneResourceRepository
import javax.inject.Inject

/**
 * 保存场景编辑结果。
 *
 * 统一承接新增和更新场景的保存入口，并返回最终场景 ID。
 */
class SaveSceneEditUseCase @Inject constructor(private val sceneResourceRepository: SceneResourceRepository) {
    suspend operator fun invoke(input: SceneEditInput): String = sceneResourceRepository.saveSceneEdit(input)
}
