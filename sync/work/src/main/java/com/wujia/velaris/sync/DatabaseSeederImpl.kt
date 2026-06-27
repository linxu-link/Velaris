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
package com.wujia.velaris.sync

import android.content.Context
import androidx.annotation.RawRes
import com.wujia.foundation.data.scene.defaultLocalScenes
import com.wujia.foundation.database.dao.SceneAudioDao
import com.wujia.foundation.database.dao.SceneDao
import com.wujia.foundation.database.model.SceneAudioEntity
import com.wujia.foundation.database.model.SceneEntity
import com.wujia.foundation.model.scene.SceneTimerMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class DatabaseSeederImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sceneDao: SceneDao,
    private val sceneAudioDao: SceneAudioDao,
) : DatabaseSeeder {

    override suspend fun seedIfEmpty() {
        if (sceneDao.getCount() > 0) return

        val now = Clock.System.now()
        val scenes = defaultLocalScenes()

        scenes.forEachIndexed { index, seed ->
            sceneDao.upsert(
                SceneEntity(
                    id = seed.id,
                    title = context.getString(seed.titleResId),
                    description = context.getString(seed.subtitleResId),
                    category = seed.category.name,
                    iconName = "",
                    accentColor = "",
                    sortOrder = seed.sortOrder,
                    videoVolume = seed.videoVolume.coerceIn(0f, 1f),
                    createdAt = now,
                    updatedAt = now,
                    videoUri = seed.videoResId?.toAndroidResourceUri(),
                    backgroundResName = seed.backgroundResName,
                    backgroundUri = null,
                    brightness = seed.brightness.coerceIn(0f, 1f),
                    darkness = seed.darkness.coerceIn(0f, 1f),
                    timerMode = SceneTimerMode.Countdown.name,
                    showCountdownClock = seed.showCountdownClock,
                    alarmReminderEnabled = seed.alarmReminderEnabled,
                    particleEffect = seed.particleSettings.effect.name,
                    particleIntensity = seed.particleSettings.intensity,
                    particleWind = seed.particleSettings.wind,
                    particleQuality = seed.particleSettings.quality.name,
                    particleForegroundGlassEnabled = seed.particleSettings.foregroundGlassEnabled,
                ),
            )

            if (seed.audioTracks.isNotEmpty()) {
                sceneAudioDao.upsertAll(
                    seed.audioTracks.mapIndexed { audioIndex, audio ->
                        SceneAudioEntity(
                            sceneId = seed.id,
                            dataId = audio.id,
                            title = context.getString(audio.titleResId),
                            uri = audio.rawResId.toAndroidResourceUri(),
                            volume = audio.volume,
                            loop = audio.loop,
                            sortOrder = audioIndex,
                        )
                    },
                )
            }
        }
    }

    private fun @receiver:RawRes Int.toAndroidResourceUri(): String = "android.resource://${context.packageName}/$this"
}
