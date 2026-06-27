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
import javax.inject.Inject

/**
 * 更新场景内单个音频资源的音量。
 */
class UpdateSceneAudioVolumeUseCase @Inject constructor(private val sceneResourceRepository: SceneResourceRepository) {
    suspend operator fun invoke(sceneId: String, audioId: String, volume: Float) =
        sceneResourceRepository.updateSceneAudioVolume(sceneId, audioId, volume)
}
