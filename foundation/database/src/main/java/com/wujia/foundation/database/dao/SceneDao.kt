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
package com.wujia.foundation.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.wujia.foundation.database.model.SceneAudioEntity
import com.wujia.foundation.database.model.SceneEntity
import com.wujia.foundation.database.model.SceneWithAudio
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * 场景数据访问对象。
 */
@Dao
interface SceneDao {

    /**
     * 查询所有场景，按 sortOrder 升序排列。
     */
    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC, createdAt ASC, id ASC")
    fun observeAll(): Flow<List<SceneEntity>>

    /**
     * 查询所有场景和音轨，按场景展示顺序排列。
     */
    @Transaction
    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC, createdAt ASC, id ASC")
    fun observeAllWithAudio(): Flow<List<SceneWithAudio>>

    /**
     * 查询所有场景（一次性）。
     */
    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC, createdAt ASC, id ASC")
    suspend fun getAll(): List<SceneEntity>

    /**
     * 查询所有场景和音轨（一次性）。
     */
    @Transaction
    @Query("SELECT * FROM scenes ORDER BY sortOrder ASC, createdAt ASC, id ASC")
    suspend fun getAllWithAudio(): List<SceneWithAudio>

    /**
     * 根据 ID 查询单个场景。
     */
    @Query("SELECT * FROM scenes WHERE id = :id")
    suspend fun getById(id: String): SceneEntity?

    /**
     * 根据 ID 查询单个场景（Flow）。
     */
    @Query("SELECT * FROM scenes WHERE id = :id")
    fun observeById(id: String): Flow<SceneEntity?>

    /**
     * 根据分类查询场景。
     */
    @Query("SELECT * FROM scenes WHERE category = :category ORDER BY sortOrder ASC, createdAt ASC, id ASC")
    fun observeByCategory(category: String): Flow<List<SceneEntity>>

    /**
     * 新增或更新单个场景。
     */
    @Upsert
    suspend fun upsert(scene: SceneEntity)

    /**
     * 新增或更新批量场景。
     */
    @Upsert
    suspend fun upsertAll(scenes: List<SceneEntity>)

    /**
     * 更新单个场景。
     */
    @Update
    suspend fun update(scene: SceneEntity)

    /**
     * 更新场景排序。
     */
    @Query("UPDATE scenes SET sortOrder = :sortOrder, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int, updatedAt: Instant = Clock.System.now())

    /**
     * 批量更新场景排序，保证一次拖拽提交在同一个事务内完成。
     */
    @Transaction
    suspend fun updateSortOrders(orderedIds: List<String>) {
        val updatedAt = Clock.System.now()
        orderedIds.forEachIndexed { index, id ->
            updateSortOrder(
                id = id,
                sortOrder = index,
                updatedAt = updatedAt,
            )
        }
    }

    /**
     * 批量更新指定分类内的场景排序，避免筛选列表拖拽时污染其他分类。
     */
    @Transaction
    suspend fun updateSortOrdersByCategory(category: String, orderedIds: List<String>) {
        val updatedAt = Clock.System.now()
        orderedIds.forEachIndexed { index, id ->
            updateSortOrderByCategory(
                category = category,
                id = id,
                sortOrder = index,
                updatedAt = updatedAt,
            )
        }
    }

    @Query(
        """
            UPDATE scenes
            SET sortOrder = :sortOrder, updatedAt = :updatedAt
            WHERE id = :id AND category = :category
        """,
    )
    suspend fun updateSortOrderByCategory(
        category: String,
        id: String,
        sortOrder: Int,
        updatedAt: Instant = Clock.System.now(),
    )

    @Query(
        """
            UPDATE scenes
            SET brightness = :brightness,
                darkness = :darkness,
                timerMode = :timerMode,
                timerDurationMillis = :timerDurationMillis,
                fadeOutEnabled = :fadeOutEnabled,
                guideCompleted = :guideCompleted,
                showCountdownClock = :showCountdownClock,
                alarmReminderEnabled = :alarmReminderEnabled,
                countdownClockPosition = :countdownClockPosition,
                clockAudioVolume = :clockAudioVolume,
                particleEffect = :particleEffect,
                particleIntensity = :particleIntensity,
                particleWind = :particleWind,
                particleQuality = :particleQuality,
                particleForegroundGlassEnabled = :particleForegroundGlassEnabled,
                updatedAt = :updatedAt
            WHERE id = :id
        """,
    )
    suspend fun updateControlSettings(
        id: String,
        brightness: Float,
        darkness: Float,
        timerMode: String,
        timerDurationMillis: Long,
        fadeOutEnabled: Boolean,
        guideCompleted: Boolean,
        showCountdownClock: Boolean,
        alarmReminderEnabled: Boolean,
        countdownClockPosition: String,
        clockAudioVolume: Float,
        particleEffect: String,
        particleIntensity: Float,
        particleWind: Float,
        particleQuality: String,
        particleForegroundGlassEnabled: Boolean,
        updatedAt: Instant = Clock.System.now(),
    )

    @Query(
        """
            UPDATE scenes
            SET videoVolume = :videoVolume,
                updatedAt = :updatedAt
            WHERE id = :id
        """,
    )
    suspend fun updateVideoVolume(id: String, videoVolume: Float, updatedAt: Instant = Clock.System.now())

    /**
     * 根据 ID 删除场景。
     */
    @Delete
    suspend fun delete(scene: SceneEntity)

    /**
     * 根据 ID 删除场景。
     */
    @Query("DELETE FROM scenes WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 删除所有场景。
     */
    @Query("DELETE FROM scenes")
    suspend fun deleteAll()

    /**
     * 获取场景数量。
     */
    @Query("SELECT COUNT(*) FROM scenes")
    suspend fun getCount(): Int

    /**
     * 获取最大的 sortOrder 值。
     */
    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM scenes")
    suspend fun getMaxSortOrder(): Int

    /**
     * 判断指定场景是否存在。
     */
    @Query("SELECT EXISTS(SELECT 1 FROM scenes WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    /**
     * 事务保存场景及其音轨：先 upsert 场景，再替换全部音轨。
     * 保证三步操作在同一事务内，避免中间失败导致音轨丢失。
     */
    @Transaction
    suspend fun saveSceneWithAudio(scene: SceneEntity, audioTracks: List<SceneAudioEntity>) {
        upsert(scene)
        deleteAudioBySceneId(scene.id)
        upsertAudioAll(audioTracks)
    }

    @Query("DELETE FROM scene_audio_tracks WHERE sceneId = :sceneId")
    suspend fun deleteAudioBySceneId(sceneId: String)

    @Upsert
    suspend fun upsertAudioAll(audioTracks: List<SceneAudioEntity>)
}
