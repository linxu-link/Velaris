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
package com.wujia.foundation.data.scene

import android.content.Context
import com.wujia.foundation.database.dao.SceneAudioDao
import com.wujia.foundation.database.dao.SceneDao
import com.wujia.foundation.database.model.SceneAudioEntity
import com.wujia.foundation.database.model.SceneEntity
import com.wujia.foundation.database.model.SceneWithAudio
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.model.scene.SceneTimerMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * 场景资源的本地数据源。
 *
 * 以 Room 作为本地事实来源，并在首次启动时写入 demo seed 数据。
 */
class SceneResourceLocalDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sceneDao: SceneDao,
    private val sceneAudioDao: SceneAudioDao,
) {
    private val seedMutex = Mutex()

    /**
     * 观察所有场景资源，顺序由数据库 sortOrder 决定。
     */
    internal fun observeSceneResources(): Flow<List<LocalSceneResource>> = flow {
        seedDatabaseIfEmpty()
        emitAll(
            sceneDao.observeAllWithAudio()
                .map { scenes -> scenes.map { it.asLocalModel() } },
        )
    }.catch {
        emit(emptyList())
    }

    internal suspend fun getSceneResources(): List<LocalSceneResource> {
        seedDatabaseIfEmpty()
        return sceneDao.getAllWithAudio().map { it.asLocalModel() }
    }

    internal suspend fun getEditableScene(id: String): LocalSceneResource? {
        seedDatabaseIfEmpty()
        val scene = sceneDao.getById(id) ?: return null
        val audioTracks = sceneAudioDao.getBySceneId(id)
        Timber.tag(TAG).d(
            "getEditableScene id=%s dbAudioCount=%d sceneParticle=%s sceneBrightness=%s sceneDarkness=%s",
            id,
            audioTracks.size,
            scene.particleEffect,
            scene.brightness,
            scene.darkness,
        )
        return SceneWithAudio(scene = scene, audioTracks = audioTracks).asLocalModel().also { local ->
            Timber.tag(TAG).d(
                "getEditableScene mapped id=%s mappedAudioCount=%d mappedParticle=%s mappedSoundIds=%s",
                id,
                local.audioTracks.size,
                local.controlSettings.particle,
                local.audioTracks.map { it.id },
            )
        }
    }

    internal suspend fun saveSceneEdit(input: LocalSceneEditInput): String {
        seedDatabaseIfEmpty()
        val now = Clock.System.now()
        val sceneId = input.id ?: UUID.randomUUID().toString()
        val existing = sceneDao.getById(sceneId)
        val sortOrder = existing?.sortOrder ?: sceneDao.getMaxSortOrder() + 1
        Timber.tag(TAG).d(
            "saveSceneEdit start id=%s existingAudioCount=%d inputAudioCount=%d inputSoundIds=%s inputParticle=%s inputBrightness=%s inputDarkness=%s",
            sceneId,
            sceneAudioDao.getBySceneId(sceneId).size,
            input.audioTracks.size,
            input.audioTracks.map { it.id },
            input.controlSettings.particle,
            input.controlSettings.brightness,
            input.controlSettings.darkness,
        )

        sceneDao.saveSceneWithAudio(
            scene = SceneEntity(
                id = sceneId,
                title = input.title,
                description = input.description,
                category = input.category.name,
                iconName = existing?.iconName.orEmpty(),
                accentColor = existing?.accentColor.orEmpty(),
                sortOrder = sortOrder,
                videoUri = input.videoUri,
                videoVolume = existing?.videoVolume ?: 0f,
                backgroundResName = input.backgroundResName,
                backgroundUri = input.backgroundUri,
                brightness = input.controlSettings.brightness,
                darkness = input.controlSettings.darkness,
                timerMode = input.controlSettings.timerMode.name,
                timerDurationMillis = input.controlSettings.timerDurationMillis,
                fadeOutEnabled = input.controlSettings.fadeOutEnabled,
                guideCompleted = input.controlSettings.guideCompleted,
                showCountdownClock = input.controlSettings.showCountdownClock,
                alarmReminderEnabled = input.controlSettings.alarmReminderEnabled,
                countdownClockPosition = input.controlSettings.countdownClockPosition.name,
                clockAudioVolume = input.controlSettings.clockAudioVolume,
                particleEffect = input.controlSettings.particle.effect.name,
                particleIntensity = input.controlSettings.particle.intensity,
                particleWind = input.controlSettings.particle.wind,
                particleQuality = input.controlSettings.particle.quality.name,
                particleForegroundGlassEnabled = input.controlSettings.particle.foregroundGlassEnabled,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
            audioTracks = input.audioTracks.mapIndexed { index, audio ->
                SceneAudioEntity(
                    sceneId = sceneId,
                    dataId = audio.id,
                    title = audio.title,
                    uri = audio.uri,
                    volume = audio.volume,
                    loop = audio.loop,
                    sortOrder = index,
                )
            },
        )
        Timber.tag(TAG).d(
            "saveSceneEdit done id=%s wroteAudioCount=%d wroteParticle=%s",
            sceneId,
            input.audioTracks.size,
            input.controlSettings.particle,
        )
        return sceneId
    }

    internal suspend fun deleteSceneResource(id: String) {
        seedDatabaseIfEmpty()
        if (id in presetSceneIds) return
        sceneDao.deleteById(id)
    }

    internal suspend fun reorderSceneResources(category: SceneCategory?, orderedIds: List<String>) {
        if (category == null) {
            sceneDao.updateSortOrders(orderedIds)
        } else {
            sceneDao.updateSortOrdersByCategory(
                category = category.name,
                orderedIds = orderedIds,
            )
        }
    }

    internal suspend fun updateSceneControlSettings(sceneId: String, settings: SceneControlSettings) {
        seedDatabaseIfEmpty()
        sceneDao.updateControlSettings(
            id = sceneId,
            brightness = settings.brightness.coerceIn(0f, 1f),
            darkness = settings.darkness.coerceIn(0f, 1f),
            timerMode = settings.timerMode.name,
            timerDurationMillis = settings.timerDurationMillis.coerceAtLeast(0L),
            fadeOutEnabled = settings.fadeOutEnabled,
            guideCompleted = settings.guideCompleted,
            showCountdownClock = settings.showCountdownClock,
            alarmReminderEnabled = settings.alarmReminderEnabled,
            countdownClockPosition = settings.countdownClockPosition.name,
            clockAudioVolume = settings.clockAudioVolume.coerceIn(0f, 1f),
            particleEffect = settings.particle.effect.name,
            particleIntensity = settings.particle.intensity.coerceIn(0f, 1f),
            particleWind = settings.particle.wind.coerceIn(0f, 1f),
            particleQuality = settings.particle.quality.name,
            particleForegroundGlassEnabled = settings.particle.foregroundGlassEnabled,
        )
    }

    internal suspend fun updateSceneAudioVolume(sceneId: String, audioId: String, volume: Float) {
        seedDatabaseIfEmpty()
        sceneAudioDao.updateVolume(
            sceneId = sceneId,
            audioId = audioId,
            volume = volume.coerceIn(0f, 1f),
        )
    }

    internal suspend fun updateSceneVideoVolume(sceneId: String, volume: Float) {
        seedDatabaseIfEmpty()
        sceneDao.updateVideoVolume(
            id = sceneId,
            videoVolume = volume.coerceIn(0f, 1f),
        )
    }

    private suspend fun seedDatabaseIfEmpty() {
        seedMutex.withLock {
            val seedScenes = defaultLocalScenes()
            if (sceneDao.getCount() > 0) {
                syncSeedParticlesIfUnset(seedScenes)
                return@withLock
            }

            val now = Clock.System.now()
            sceneDao.upsertAll(
                seedScenes.map { seed ->
                    SceneEntity(
                        id = seed.id,
                        title = context.getString(seed.titleResId),
                        description = context.getString(seed.subtitleResId),
                        category = seed.category.name,
                        iconName = "",
                        accentColor = "",
                        sortOrder = seed.sortOrder,
                        videoUri = seed.videoResId?.toAndroidResourceUri(),
                        videoVolume = seed.videoVolume.coerceIn(0f, 1f),
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
                        createdAt = now,
                        updatedAt = now,
                    )
                },
            )
            sceneAudioDao.upsertAll(
                seedScenes.flatMap { seed ->
                    seed.audioTracks.mapIndexed { index, audio ->
                        SceneAudioEntity(
                            sceneId = seed.id,
                            dataId = audio.id,
                            title = context.getString(audio.titleResId),
                            uri = audio.rawResId.toAndroidResourceUri(),
                            volume = audio.volume,
                            loop = audio.loop,
                            sortOrder = index,
                        )
                    }
                },
            )
        }
    }

    private suspend fun syncSeedParticlesIfUnset(seedScenes: List<LocalSeedScene>) {
        seedScenes
            .filter { it.isPreset && it.particleSettings.effect != SceneParticleEffect.None }
            .forEach { seed ->
                val scene = sceneDao.getById(seed.id) ?: return@forEach
                if (scene.particleEffect != SceneParticleEffect.None.name) return@forEach

                val particle = seed.particleSettings
                sceneDao.updateControlSettings(
                    id = scene.id,
                    brightness = scene.brightness,
                    darkness = scene.darkness,
                    timerMode = scene.timerMode,
                    timerDurationMillis = scene.timerDurationMillis,
                    fadeOutEnabled = scene.fadeOutEnabled,
                    guideCompleted = scene.guideCompleted,
                    showCountdownClock = scene.showCountdownClock,
                    alarmReminderEnabled = scene.alarmReminderEnabled,
                    countdownClockPosition = scene.countdownClockPosition,
                    clockAudioVolume = scene.clockAudioVolume,
                    particleEffect = particle.effect.name,
                    particleIntensity = particle.intensity.coerceIn(0f, 1f),
                    particleWind = particle.wind.coerceIn(0f, 1f),
                    particleQuality = particle.quality.name,
                    particleForegroundGlassEnabled = particle.foregroundGlassEnabled,
                )
            }
    }

    private fun SceneWithAudio.asLocalModel(): LocalSceneResource = LocalSceneResource(
        coverResName = presetSceneCovers[scene.id],
        id = scene.id,
        title = scene.title,
        subtitle = scene.description,
        category = scene.category.toSceneCategory(),
        isPreset = scene.id in presetSceneIds,
        backgroundResName = scene.backgroundResName,
        backgroundUri = scene.backgroundUri,
        videoUri = scene.videoUri,
        videoVolume = scene.videoVolume,
        controlSettings = SceneControlSettings(
            brightness = scene.brightness,
            darkness = scene.darkness,
            timerMode = scene.timerMode.toSceneTimerMode(),
            timerDurationMillis = scene.timerDurationMillis,
            fadeOutEnabled = scene.fadeOutEnabled,
            guideCompleted = scene.guideCompleted,
            showCountdownClock = scene.showCountdownClock,
            alarmReminderEnabled = scene.alarmReminderEnabled,
            countdownClockPosition = scene.countdownClockPosition.toSceneCountdownClockPosition(),
            clockAudioVolume = scene.clockAudioVolume,
            particle = SceneParticleSettings(
                effect = scene.particleEffect.toSceneParticleEffect(),
                intensity = scene.particleIntensity,
                wind = scene.particleWind,
                quality = scene.particleQuality.toSceneParticleQuality(),
                foregroundGlassEnabled = scene.particleForegroundGlassEnabled,
            ),
        ),
        audioTracks = audioTracks
            .sortedBy { it.sortOrder }
            .distinctBy { it.dataId }
            .map { audio ->
                LocalSceneAudioResource(
                    id = audio.dataId,
                    title = audio.title,
                    uri = audio.uri,
                    volume = audio.volume,
                    loop = audio.loop,
                )
            },
    )

    private fun String.toSceneCategory(): SceneCategory = runCatching {
        SceneCategory.valueOf(this)
    }.getOrDefault(SceneCategory.FOCUS)

    private fun String.toSceneParticleEffect(): SceneParticleEffect = runCatching {
        SceneParticleEffect.valueOf(this)
    }.getOrDefault(SceneParticleEffect.None)

    private fun String.toSceneParticleQuality(): SceneParticleQuality = runCatching {
        SceneParticleQuality.valueOf(this)
    }.getOrDefault(SceneParticleQuality.Medium)

    private fun String.toSceneTimerMode(): SceneTimerMode = runCatching {
        SceneTimerMode.valueOf(this)
    }.getOrDefault(SceneTimerMode.Countdown)

    private fun String.toSceneCountdownClockPosition(): SceneCountdownClockPosition = runCatching {
        SceneCountdownClockPosition.valueOf(this)
    }
        .getOrDefault(SceneCountdownClockPosition.Center)

    private fun Int.toAndroidResourceUri(): String = "android.resource://${context.packageName}/$this"

    private companion object {
        const val TAG = "SceneData"
        val presetSceneCovers: Map<String, String?> = defaultLocalScenes()
            .associate { it.id to it.coverResName }
        val presetSceneIds: Set<String> = defaultLocalScenes()
            .filter { it.isPreset }
            .map { it.id }
            .toSet()
    }
}
