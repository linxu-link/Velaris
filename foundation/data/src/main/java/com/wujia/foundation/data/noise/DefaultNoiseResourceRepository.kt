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
package com.wujia.foundation.data.noise

import android.content.Context
import androidx.annotation.RawRes
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.model.noise.NoiseResource
import com.wujia.foundation.model.noise.NoiseResourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultNoiseResourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataSource: NoiseResourceLocalDataSource,
) : NoiseResourceRepository {

    override fun observeNoiseResources(): Flow<List<NoiseResource>> = localDataSource.observeNoiseResources()
        .map { noises -> noises.map { it.asExternalModel() } }

    override suspend fun getNoiseResources(): List<NoiseResource> = localDataSource.getNoiseResources().map {
        it.asExternalModel()
    }

    override suspend fun getNoiseResource(id: String): NoiseResource? =
        localDataSource.getNoiseResource(id)?.asExternalModel()

    override suspend fun getNoiseResourcesByCategory(category: NoiseCategory): List<NoiseResource> =
        localDataSource.getNoiseResourcesByCategory(category).map {
            it.asExternalModel()
        }

    private fun LocalNoiseResource.asExternalModel(): NoiseResource = NoiseResource(
        id = id,
        title = title,
        description = description,
        category = category,
        uri = rawResId.toAndroidResourceUri(),
        tags = tags,
        thumbnailResName = thumbnailResName,
        defaultVolume = defaultVolume,
        loop = loop,
    )

    private fun @receiver:RawRes Int.toAndroidResourceUri(): String = "android.resource://${context.packageName}/$this"
}
