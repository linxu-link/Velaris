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
package com.wujia.foundation.domain.video

import com.wujia.foundation.model.video.VideoResource
import com.wujia.foundation.model.video.VideoResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 观察视频资源列表。
 *
 * 供场景编辑页选择预制或自定义视频资源。
 */
class GetVideoResourcesUseCase @Inject constructor(private val videoResourceRepository: VideoResourceRepository) {
    operator fun invoke(): Flow<List<VideoResource>> = videoResourceRepository.observeVideoResources()
}
