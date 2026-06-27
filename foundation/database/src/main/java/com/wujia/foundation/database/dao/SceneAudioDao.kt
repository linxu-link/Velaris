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
import androidx.room.Query
import androidx.room.Upsert
import com.wujia.foundation.database.model.SceneAudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneAudioDao {

    @Upsert
    suspend fun upsertAll(audioTracks: List<SceneAudioEntity>)

    @Query("SELECT * FROM scene_audio_tracks WHERE sceneId = :sceneId ORDER BY sortOrder ASC")
    suspend fun getBySceneId(sceneId: String): List<SceneAudioEntity>

    @Query("SELECT * FROM scene_audio_tracks WHERE sceneId = :sceneId ORDER BY sortOrder ASC")
    fun observeBySceneId(sceneId: String): Flow<List<SceneAudioEntity>>

    @Query("DELETE FROM scene_audio_tracks WHERE sceneId = :sceneId")
    suspend fun deleteBySceneId(sceneId: String)

    @Query("UPDATE scene_audio_tracks SET volume = :volume WHERE sceneId = :sceneId AND dataId = :audioId")
    suspend fun updateVolume(sceneId: String, audioId: String, volume: Float)
}
