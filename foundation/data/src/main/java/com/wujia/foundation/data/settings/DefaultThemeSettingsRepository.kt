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
package com.wujia.foundation.data.settings

import com.wujia.foundation.model.settings.ThemeSettingsRepository
import com.wujia.foundation.model.theme.VelarisThemePreset
import com.wujia.foundation.toolkit.storage.SPUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class DefaultThemeSettingsRepository @Inject constructor() : ThemeSettingsRepository {

    private val presetFlow = MutableStateFlow(loadPersistedPreset())

    override fun observeThemePreset(): Flow<VelarisThemePreset> = presetFlow

    override fun getThemePreset(): VelarisThemePreset = presetFlow.value

    override suspend fun updateThemePreset(preset: VelarisThemePreset) {
        SPUtils.put(KEY_THEME_PRESET, preset.name)
        presetFlow.value = preset
    }

    companion object {
        private const val KEY_THEME_PRESET = "settings_theme_preset"

        fun loadPersistedPreset(): VelarisThemePreset {
            val saved = SPUtils.getString(KEY_THEME_PRESET, "")
            return try {
                VelarisThemePreset.valueOf(saved)
            } catch (_: Exception) {
                VelarisThemePreset.Ocean
            }
        }
    }
}
