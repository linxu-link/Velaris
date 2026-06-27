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
package com.wujia.foundation.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wujia.foundation.model.scene.SceneControlDefaults
import kotlinx.datetime.Instant

/**
 * 场景实体，对应数据库中的 scenes 表。
 */
@Entity(
    tableName = "scenes",
    indices = [
        Index(value = ["category", "sortOrder"]),
        Index(value = ["sortOrder"]),
    ],
)
data class SceneEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconName: String,
    val accentColor: String,
    val sortOrder: Int,
    val videoUri: String?,
    @ColumnInfo(defaultValue = "0")
    val videoVolume: Float = 0f,
    val backgroundResName: String?,
    val backgroundUri: String? = null,
    @ColumnInfo(defaultValue = SceneControlDefaults.BRIGHTNESS_DB)
    val brightness: Float = SceneControlDefaults.BRIGHTNESS,
    @ColumnInfo(defaultValue = SceneControlDefaults.DARKNESS_DB)
    val darkness: Float = SceneControlDefaults.DARKNESS,
    @ColumnInfo(defaultValue = "Countdown")
    val timerMode: String = "Countdown",
    @ColumnInfo(defaultValue = SceneControlDefaults.TIMER_DURATION_DB)
    val timerDurationMillis: Long = SceneControlDefaults.TIMER_DURATION_MILLIS,
    @ColumnInfo(defaultValue = "1")
    val fadeOutEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val guideCompleted: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val showCountdownClock: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val alarmReminderEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "Center")
    val countdownClockPosition: String = "Center",
    @ColumnInfo(defaultValue = "0.5")
    val clockAudioVolume: Float = 0.5f,
    @ColumnInfo(name = "particleEffect", defaultValue = "None")
    val particleEffect: String = "None",
    @ColumnInfo(name = "particleIntensity", defaultValue = "0.72")
    val particleIntensity: Float = 0.72f,
    @ColumnInfo(name = "particleWind", defaultValue = "0.2")
    val particleWind: Float = 0.2f,
    @ColumnInfo(name = "particleQuality", defaultValue = "Medium")
    val particleQuality: String = "Medium",
    @ColumnInfo(name = "particleForegroundGlassEnabled", defaultValue = "1")
    val particleForegroundGlassEnabled: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
)
