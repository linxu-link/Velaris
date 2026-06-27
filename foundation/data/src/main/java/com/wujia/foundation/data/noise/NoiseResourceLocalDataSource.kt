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
import com.wujia.foundation.model.noise.NoiseCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class NoiseResourceLocalDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val localNoises: List<LocalNoiseResource> =
        defaultLocalNoises().map { it.asLocalModel() }

    internal fun observeNoiseResources(): Flow<List<LocalNoiseResource>> = flowOf(localNoises)

    internal suspend fun getNoiseResources(): List<LocalNoiseResource> = localNoises

    internal suspend fun getNoiseResource(id: String): LocalNoiseResource? = localNoises.firstOrNull { it.id == id }

    internal suspend fun getNoiseResourcesByCategory(category: NoiseCategory): List<LocalNoiseResource> =
        localNoises.filter {
            it.category ==
                category
        }

    private fun LocalSeedNoise.asLocalModel(): LocalNoiseResource = LocalNoiseResource(
        id = id,
        title = context.getString(titleResId),
        description = context.getString(descriptionResId),
        category = category,
        rawResId = rawResId,
        tags = emptyList(),
        thumbnailResName = thumbnailResName,
        defaultVolume = defaultVolume,
        loop = loop,
    )
}
