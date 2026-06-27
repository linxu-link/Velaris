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
package com.wujia.foundation.model.settings

import com.wujia.foundation.model.theme.VelarisThemePreset
import kotlinx.coroutines.flow.Flow

/**
 * 主题设置仓库。
 *
 * 负责持久化和观察当前的主题预设，供应用根主题和设置页共享。
 */
interface ThemeSettingsRepository {

    /**
     * 观察当前主题预设。
     */
    fun observeThemePreset(): Flow<VelarisThemePreset>

    /**
     * 读取当前主题预设。
     */
    fun getThemePreset(): VelarisThemePreset

    /**
     * 更新主题预设并持久化。
     */
    suspend fun updateThemePreset(preset: VelarisThemePreset)
}
