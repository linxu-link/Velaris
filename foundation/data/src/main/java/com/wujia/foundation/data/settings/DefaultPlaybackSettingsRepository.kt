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

import com.wujia.foundation.player.PlaybackSettingsRepository
import com.wujia.foundation.player.VelarisPlayerPerformanceProfile
import com.wujia.foundation.toolkit.storage.SPUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class DefaultPlaybackSettingsRepository @Inject constructor() : PlaybackSettingsRepository {

    private val profileFlow = MutableStateFlow(loadPersistedProfile())

    override fun observePerformanceProfile(): Flow<VelarisPlayerPerformanceProfile> = profileFlow

    override fun getPerformanceProfile(): VelarisPlayerPerformanceProfile = profileFlow.value

    override suspend fun updatePerformanceProfile(profile: VelarisPlayerPerformanceProfile) {
        SPUtils.put(KEY_PLAYBACK_PROFILE, profile.name)
        profileFlow.value = profile
    }

    companion object {
        private const val KEY_PLAYBACK_PROFILE = "settings_playback_profile"

        fun loadPersistedProfile(): VelarisPlayerPerformanceProfile {
            val saved = SPUtils.getString(KEY_PLAYBACK_PROFILE, "")
            return try {
                VelarisPlayerPerformanceProfile.valueOf(saved)
            } catch (_: Exception) {
                VelarisPlayerPerformanceProfile.Balanced
            }
        }
    }
}
