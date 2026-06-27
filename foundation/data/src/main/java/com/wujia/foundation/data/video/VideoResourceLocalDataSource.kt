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
package com.wujia.foundation.data.video

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class VideoResourceLocalDataSource @Inject constructor() {
    private val localVideos: List<LocalVideoResource> =
        defaultLocalVideos().map { it.asLocalModel() }

    internal fun observeVideoResources(): Flow<List<LocalVideoResource>> = flowOf(localVideos)

    internal suspend fun getVideoResources(): List<LocalVideoResource> = localVideos

    internal suspend fun getVideoResource(id: String): LocalVideoResource? = localVideos.firstOrNull { it.id == id }

    private fun LocalSeedVideo.asLocalModel(): LocalVideoResource = LocalVideoResource(
        id = id,
        title = "",
        description = "",
        rawResId = rawResId,
        thumbnailResName = thumbnailResName,
    )
}
