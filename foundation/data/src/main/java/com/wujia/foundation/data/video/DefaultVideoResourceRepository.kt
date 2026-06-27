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

import android.content.Context
import androidx.annotation.RawRes
import com.wujia.foundation.model.video.VideoResource
import com.wujia.foundation.model.video.VideoResourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultVideoResourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataSource: VideoResourceLocalDataSource,
) : VideoResourceRepository {

    override fun observeVideoResources(): Flow<List<VideoResource>> = localDataSource.observeVideoResources()
        .map { videos -> videos.map { it.asExternalModel() } }

    override suspend fun getVideoResources(): List<VideoResource> = localDataSource.getVideoResources().map {
        it.asExternalModel()
    }

    override suspend fun getVideoResource(id: String): VideoResource? =
        localDataSource.getVideoResource(id)?.asExternalModel()

    private fun LocalVideoResource.asExternalModel(): VideoResource = VideoResource(
        id = id,
        title = title,
        description = description,
        uri = rawResId.toAndroidResourceUri(),
        thumbnailResName = thumbnailResName,
    )

    private fun @receiver:RawRes Int.toAndroidResourceUri(): String = "android.resource://${context.packageName}/$this"
}
