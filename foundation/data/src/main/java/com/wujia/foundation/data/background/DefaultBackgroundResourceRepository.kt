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
package com.wujia.foundation.data.background

import android.content.Context
import androidx.annotation.DrawableRes
import com.wujia.foundation.model.background.BackgroundResource
import com.wujia.foundation.model.background.BackgroundResourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultBackgroundResourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataSource: BackgroundResourceLocalDataSource,
) : BackgroundResourceRepository {

    override fun observeBackgroundResources(): Flow<List<BackgroundResource>> =
        localDataSource.observeBackgroundResources()
            .map { backgrounds -> backgrounds.map { it.asExternalModel() } }

    override suspend fun getBackgroundResources(): List<BackgroundResource> =
        localDataSource.getBackgroundResources().map {
            it.asExternalModel()
        }

    override suspend fun getBackgroundResource(id: String): BackgroundResource? =
        localDataSource.getBackgroundResource(id)?.asExternalModel()

    private fun LocalBackgroundResource.asExternalModel(): BackgroundResource = BackgroundResource(
        id = id,
        title = title,
        description = description,
        uri = drawableResId.toAndroidResourceUri(),
    )

    private fun @receiver:DrawableRes Int.toAndroidResourceUri(): String =
        "android.resource://${context.packageName}/$this"
}
