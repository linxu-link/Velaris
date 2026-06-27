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
package com.wujia.foundation.player

import kotlinx.coroutines.flow.Flow

/**
 * 播放配置仓库接口，负责播放性能档位的持久化与观察。
 *
 * 由 [com.wujia.foundation.data.settings.DefaultPlaybackSettingsRepository] 实现，
 * ViewModel 通过此接口读写配置，无需关心底层存储方式。
 */
interface PlaybackSettingsRepository {

    /**
     * 观察当前播放性能档位的实时更新流。
     */
    fun observePerformanceProfile(): Flow<VelarisPlayerPerformanceProfile>

    /**
     * 获取当前持久化的播放性能档位。
     */
    fun getPerformanceProfile(): VelarisPlayerPerformanceProfile

    /**
     * 更新播放性能档位并持久化。
     */
    suspend fun updatePerformanceProfile(profile: VelarisPlayerPerformanceProfile)
}
