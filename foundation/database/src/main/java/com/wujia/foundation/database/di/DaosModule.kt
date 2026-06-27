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
package com.wujia.foundation.database.di

import com.wujia.foundation.database.VelarisDatabase
import com.wujia.foundation.database.dao.SceneAudioDao
import com.wujia.foundation.database.dao.SceneDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 提供 DAO 的 Hilt 模块。
 *
 * 按 NIA 的做法，DAO 不直接创建，而是从数据库统一拆分出来。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesSceneDao(database: VelarisDatabase): SceneDao = database.sceneDao()

    @Provides
    fun providesSceneAudioDao(database: VelarisDatabase): SceneAudioDao = database.sceneAudioDao()
}
