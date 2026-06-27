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
package com.wujia.feature.scene.impl.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.domain.scene.UpdateSceneAudioVolumeUseCase
import com.wujia.foundation.domain.scene.UpdateSceneControlSettingsUseCase
import com.wujia.foundation.domain.scene.UpdateSceneVideoVolumeUseCase
import com.wujia.foundation.model.scene.SceneAudioResource
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal enum class ScenePanelState {
    NONE,
    CONTROL,
    PARTICLE,
    EDIT,
    LIST,
    SETTINGS,
}

internal enum class SceneEditOrigin {
    NONE,
    CONTROL,
    LIST,
}

@Stable
internal data class SceneUiState(
    val scenes: List<SceneResource> = emptyList(),
    val allScenes: List<SceneResource> = emptyList(),
    val isPlaying: Boolean = false,
    val currentSceneId: String? = null,
    val selectedCategory: SceneCategory = SceneCategory.FOCUS,
    val showSoundDialog: Boolean = false,
    val activePanel: ScenePanelState = ScenePanelState.NONE,
    val editingSceneId: String? = null,
    val editingSceneOrigin: SceneEditOrigin = SceneEditOrigin.NONE,
) {
    val currentScenePage: Int
        get() = currentSceneId?.let { id ->
            scenes.indexOfFirst { it.id == id }.takeIf { it >= 0 }
        } ?: 0

    val currentScene: SceneResource?
        get() = currentSceneId?.let { id -> scenes.firstOrNull { it.id == id } }
            ?: scenes.getOrNull(currentScenePage)

    val selectedSceneMode: Int
        get() = selectedCategory.ordinal

    val showControlPanel get() = activePanel == ScenePanelState.CONTROL
    val showParticlePanel get() = activePanel == ScenePanelState.PARTICLE
    val showEditScenePanel get() = activePanel == ScenePanelState.EDIT
    val showSceneListPanel get() = activePanel == ScenePanelState.LIST
    val showSettingsPanel get() = activePanel == ScenePanelState.SETTINGS
    val hasCompletedGuide: Boolean
        get() = allScenes.any { it.controlSettings.guideCompleted }
}

@HiltViewModel
internal class SceneViewModel @Inject constructor(
    observeSceneResources: ObserveSceneResourcesUseCase,
    private val updateSceneAudioVolume: UpdateSceneAudioVolumeUseCase,
    private val updateSceneVideoVolume: UpdateSceneVideoVolumeUseCase,
    private val updateSceneControlSettings: UpdateSceneControlSettingsUseCase,
) : ViewModel() {
    private val interactionState = MutableStateFlow(SceneUiState())
    private val _isSwiping = MutableStateFlow(false)
    val isSwiping: StateFlow<Boolean> = _isSwiping

    val uiState: StateFlow<SceneUiState> =
        combine(
            observeSceneResources(),
            interactionState,
        ) { allScenes, state ->
            val filtered = allScenes.filter { it.category == state.selectedCategory }
            val currentSceneId = state.currentSceneId
                ?.takeIf { id -> filtered.any { it.id == id } }
                ?: filtered.firstOrNull()?.id
            state.copy(
                scenes = filtered,
                allScenes = allScenes,
                currentSceneId = currentSceneId,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = interactionState.value,
        )

    fun onScenePageChange(page: Int) {
        val scenes = uiState.value.scenes
        val coercedPage = page.coerceInSceneRange(scenes)
        interactionState.update { state ->
            state.copy(currentSceneId = scenes.getOrNull(coercedPage)?.id)
        }
    }

    fun onSceneModeChange(index: Int) {
        val category = SceneCategory.entries.getOrNull(index) ?: SceneCategory.FOCUS
        interactionState.update { state ->
            if (state.selectedCategory == category) return@update state
            state.copy(
                selectedCategory = category,
                currentSceneId = null,
            )
        }
    }

    fun onSceneSelectById(sceneId: String) {
        val allScenes = uiState.value.allScenes
        val targetScene = allScenes.firstOrNull { it.id == sceneId } ?: return
        interactionState.update { state ->
            state.copy(
                selectedCategory = targetScene.category,
                currentSceneId = sceneId,
            )
        }
    }

    fun onSoundDialogVisibilityChange(visible: Boolean) {
        interactionState.update { state ->
            state.copy(showSoundDialog = visible)
        }
    }

    fun onPanelChange(panel: ScenePanelState) {
        interactionState.update { it.copy(activePanel = panel) }
    }

    fun onEditingSceneIdChange(sceneId: String?) {
        interactionState.update { it.copy(editingSceneId = sceneId) }
    }

    fun onEditingSceneOriginChange(origin: SceneEditOrigin) {
        interactionState.update { it.copy(editingSceneOrigin = origin) }
    }

    fun onEditingSceneFinished() {
        interactionState.update { state ->
            val targetPanel = when (state.editingSceneOrigin) {
                SceneEditOrigin.CONTROL -> ScenePanelState.CONTROL
                SceneEditOrigin.LIST -> ScenePanelState.LIST
                SceneEditOrigin.NONE -> ScenePanelState.NONE
            }
            state.copy(
                activePanel = targetPanel,
                editingSceneId = null,
                editingSceneOrigin = SceneEditOrigin.NONE,
            )
        }
    }

    fun onSwipeStateChanged(swiping: Boolean) {
        _isSwiping.value = swiping
    }

    fun onPlayingStateChanged(playing: Boolean) {
        interactionState.update { it.copy(isPlaying = playing) }
    }

    fun onAudioVolumesSave(volumes: Map<String, Float>) {
        val sceneId = uiState.value.currentScene?.id ?: return
        if (volumes.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                volumes.forEach { (audioId, volume) ->
                    updateSceneAudioVolume(
                        sceneId = sceneId,
                        audioId = audioId,
                        volume = volume,
                    )
                }
            }.onFailure { Timber.w(it, "保存音频音量失败: sceneId=$sceneId") }
        }
    }

    fun onVideoVolumeSave(volume: Float) {
        val sceneId = uiState.value.currentScene?.id ?: return

        viewModelScope.launch {
            runCatching {
                updateSceneVideoVolume(
                    sceneId = sceneId,
                    volume = volume,
                )
            }.onFailure { Timber.w(it, "保存视频音量失败: sceneId=$sceneId") }
        }
    }

    fun onGuideCompleted() {
        val scene = uiState.value.currentScene ?: return
        if (uiState.value.hasCompletedGuide || scene.controlSettings.guideCompleted) return

        viewModelScope.launch {
            runCatching {
                updateSceneControlSettings(
                    sceneId = scene.id,
                    settings = scene.controlSettings.copy(guideCompleted = true),
                )
            }.onFailure { Timber.w(it, "保存引导完成状态失败: sceneId=${scene.id}") }
        }
    }

    private fun Int.coerceInSceneRange(scenes: List<SceneResource>): Int = if (scenes.isEmpty()) {
        0
    } else {
        coerceIn(0, scenes.lastIndex)
    }
}

internal fun List<SceneAudioResource>.toSoundControlItems() = map { it.toSoundControlItem() }

private fun SceneAudioResource.toSoundControlItem() = SoundControlItem(
    title = title,
    icon = Icons.Outlined.MusicNote,
    value = volume,
)
