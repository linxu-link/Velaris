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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class BackgroundResourceLocalDataSource @Inject constructor() {
    private val localBackgrounds: List<LocalBackgroundResource> =
        defaultLocalBackgrounds().map { it.asLocalModel() }

    internal fun observeBackgroundResources(): Flow<List<LocalBackgroundResource>> = flowOf(localBackgrounds)

    internal fun getBackgroundResources(): List<LocalBackgroundResource> = localBackgrounds

    internal fun getBackgroundResource(id: String): LocalBackgroundResource? = localBackgrounds.firstOrNull {
        it.id ==
            id
    }

    private fun LocalSeedBackground.asLocalModel(): LocalBackgroundResource = LocalBackgroundResource(
        id = id,
        title = "",
        description = "",
        drawableResId = drawableResId,
    )
}
