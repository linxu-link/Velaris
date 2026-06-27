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
package com.wujia.feature.sceneedit.impl.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.feature.sceneedit.impl.ui.LocalSceneEditMediaStore
import com.wujia.foundation.domain.background.GetBackgroundResourcesUseCase
import com.wujia.foundation.domain.noise.GetNoiseResourcesUseCase
import com.wujia.foundation.domain.particle.GetParticleResourcesUseCase
import com.wujia.foundation.domain.scene.GetEditableSceneUseCase
import com.wujia.foundation.domain.scene.SaveSceneEditUseCase
import com.wujia.foundation.domain.video.GetVideoResourcesUseCase
import com.wujia.foundation.model.background.BackgroundResource
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.model.noise.NoiseResource
import com.wujia.foundation.model.particle.ParticleEffect
import com.wujia.foundation.model.particle.ParticleQuality
import com.wujia.foundation.model.particle.ParticleResource
import com.wujia.foundation.model.particle.toDisplayName
import com.wujia.foundation.model.scene.EditableScene
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneEditAudio
import com.wujia.foundation.model.scene.SceneEditInput
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.model.video.VideoResource
import com.wujia.foundation.toolkit.HiToolKit
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal sealed interface EditEvent {
    data object SaveSucceeded : EditEvent
    data class ShowToast(val message: UiText) : EditEvent
}

/**
 * 本地素材 typeLabel 统一常量。
 * 与 MaterialPreset / SoundPreset 中的 typeLabel / category 取值保持一致，
 * 供 UI 层（包括子包 panel）判断“当前选中是否为本地用户挑选的素材”。
 * 避免硬编码字符串散落导致的不一致。
 */
internal const val LOCAL_IMAGE_LABEL = "image"
internal const val LOCAL_VIDEO_LABEL = "video"
internal const val PRESET_IMAGE_LABEL = "image"
internal const val PRESET_VIDEO_LABEL = "video"

private const val DEFAULT_SCENE_TITLE_FALLBACK = "\u81ea\u5b9a\u4e49\u573a\u666f"

private fun defaultSceneTitle(context: Context): String = runCatching {
    context.getString(com.wujia.feature.sceneedit.impl.R.string.scene_edit_custom_scene)
}.getOrDefault(DEFAULT_SCENE_TITLE_FALLBACK)

internal enum class ValidationIssue {
    MaterialRequired,
    SoundRequired,
    TitleRequired,
}

internal enum class SceneEditStep(val number: Int) {
    Material(1),
    Sound(2),
    Particle(3),
    Preview(4),
}

internal data class MaterialPreset(
    val id: String,
    val title: String,
    val backgroundResName: String? = null,
    val backgroundUri: String? = null,
    val videoUri: String? = null,
    val coverResName: String? = null,
    val thumbnailUri: String? = backgroundUri ?: videoUri,
    val typeLabel: String = "",
    val durationText: String = "",
)

internal data class SoundPreset(
    val id: String,
    val title: String,
    val description: String,
    val uri: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val coverResName: String? = null,
)

@Stable
internal data class MaterialSelectionState(
    val sourceTab: Int = 0,
    val presets: List<MaterialPreset> = emptyList(),
    val selectedMaterial: MaterialPreset? = null,
) {
    val selectedMaterialId: String?
        get() = selectedMaterial?.id
}

@Stable
internal data class SoundSelectionState(
    val categories: List<String> = NoiseCategory.entries.map { it.displayName },
    val selectedCategory: Int = 0,
    val presets: List<SoundPreset> = emptyList(),
    val localPresets: List<SoundPreset> = emptyList(),
    val selectedSoundIds: List<String> = emptyList(),
    val isPreviewPlaying: Boolean = false,
) {
    val selectedSounds: List<SoundPreset>
        get() {
            val allPresets = presets + localPresets
            return selectedSoundIds.mapNotNull { id -> allPresets.firstOrNull { it.id == id } }
        }

    val selectedSound: SoundPreset?
        get() = selectedSounds.firstOrNull()

    val filteredPresets: List<SoundPreset>
        get() {
            val categoryName = categories.getOrNull(selectedCategory) ?: return presets
            if (categoryName == NoiseCategory.LOCAL.displayName) return localPresets
            return presets.filter { it.category == categoryName }
        }
}

internal data class ParticlePreset(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val effect: ParticleEffect,
    val effectLabel: String,
    val intensity: Float,
    val wind: Float,
    val quality: ParticleQuality,
    val foregroundGlassEnabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val coverResName: String? = null,
)

private fun defaultParticleCategoryLabels(): List<String> = runCatching {
    listOf(
        HiToolKit.res.getString(com.wujia.foundation.model.R.string.particle_category_rain),
        HiToolKit.res.getString(com.wujia.foundation.model.R.string.particle_category_snow),
        HiToolKit.res.getString(com.wujia.foundation.model.R.string.particle_category_calm),
        HiToolKit.res.getString(com.wujia.foundation.model.R.string.particle_category_storm),
    )
}.getOrElse {
    listOf("雨天", "雪天", "静谧", "风暴")
}

@Stable
internal data class ParticleSelectionState(
    val categories: List<String> = defaultParticleCategoryLabels(),
    val selectedCategory: Int = 0,
    val presets: List<ParticlePreset> = emptyList(),
    val selectedParticleId: String? = null,
) {
    val selectedParticle: ParticlePreset?
        get() = selectedParticleId?.let { id -> presets.firstOrNull { it.id == id } }

    val filteredPresets: List<ParticlePreset>
        get() {
            val categoryName = categories.getOrNull(selectedCategory) ?: return presets
            return presets.filter { it.category == categoryName }
        }
}

@Stable
internal data class SceneEditUiState(
    val currentStep: SceneEditStep = SceneEditStep.Material,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val sceneId: String? = null,
    val category: SceneCategory = SceneCategory.FOCUS,
    val title: String = "",
    val description: String = "",
    val materialState: MaterialSelectionState = MaterialSelectionState(),
    val soundState: SoundSelectionState = SoundSelectionState(),
    val particleState: ParticleSelectionState = ParticleSelectionState(),
    val controlSettings: SceneControlSettings = SceneControlSettings(),
) {
    // 委托访问器，用于迁移期间的向后兼容
    val selectedMaterial: MaterialPreset?
        get() = materialState.selectedMaterial

    val selectedSounds: List<SoundPreset>
        get() = soundState.selectedSounds

    val selectedParticle: ParticlePreset?
        get() = particleState.selectedParticle

    val canGoNext: Boolean
        get() = !isLoading && !isSaving
}

/**
 * 场景编辑 ViewModel（Hilt 注入）。
 *
 * 架构：
 * - 严格 MVVM + UDF：暴露只读 StateFlow<SceneEditUiState>，通过事件 Channel 发送一次性副作用（保存成功、Toast）。
 * - 所有状态变更走 _uiState.update，UI 层只读 + 回调事件给 VM。
 * - 支持两种初始化模式：独立导航（SceneEditNavKey）或作为面板嵌入（isActive 驱动 setSceneParameters / onEditorInactive）。
 *
 * 核心逻辑：
 * - 多步骤向导（Material → Sound → Particle → Preview），带前置校验（validationIssue）。
 * - 预设资源通过 UseCase 加载（背景/视频/噪声/粒子），本地用户选择通过系统 Picker 获得 content://，立即 copy 到私有 storage 得到稳定 file://。
 * - 本地媒体持久化策略（关键合规 + 可靠性）：
 *   - 选择后立即通过 LocalSceneEditMediaStore 复制到 files/scene_local_media/UUID 前缀文件。
 *   - 返回 file:// URI 存入 UiState / 保存到 DB，避免 content:// 临时权限在重启/横竖屏切换后失效。
 *   - 音频走 OpenDocument 的 audio MIME 过滤，图/视频走 PickVisualMedia（无需 READ_MEDIA 权限）。
 * - 保存时根据 soundSelectionChanged / particleSelectionChanged 决定是否覆盖原音频/粒子，否则保留 originalEditableScene 的值（P1 需求：未改动的不丢失）。
 *
 * 其他：
 * - 内部维护 originalEditableScene、sound/particle changed 标记。
 * - 所有中文注释/KDoc 遵循项目规范。
 */
@HiltViewModel
internal class SceneEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    private val getEditableScene: GetEditableSceneUseCase,
    private val saveSceneEdit: SaveSceneEditUseCase,
    private val localMediaStore: LocalSceneEditMediaStore,
    private val getBackgroundResources: GetBackgroundResourcesUseCase,
    private val getVideoResources: GetVideoResourcesUseCase,
    private val getNoiseResources: GetNoiseResourcesUseCase,
    private val getParticleResources: GetParticleResourcesUseCase,
) : ViewModel() {
    private companion object {
        // 复用包级内部常量，避免与 UI 层（panel 子包）判断逻辑不一致
        const val PRESET_IMAGE_LABEL =
            com.wujia.feature.sceneedit.impl.ui.viewmodel.PRESET_IMAGE_LABEL
        const val PRESET_VIDEO_LABEL =
            com.wujia.feature.sceneedit.impl.ui.viewmodel.PRESET_VIDEO_LABEL
        const val LOCAL_IMAGE_LABEL =
            com.wujia.feature.sceneedit.impl.ui.viewmodel.LOCAL_IMAGE_LABEL
        const val LOCAL_VIDEO_LABEL =
            com.wujia.feature.sceneedit.impl.ui.viewmodel.LOCAL_VIDEO_LABEL
        const val LOCAL_AUDIO_DESCRIPTION = "本地音频"
        const val LOCAL_AUDIO_COVER = "ic_noise_default"
        const val MATERIAL_SOURCE_IMAGES = 1
        const val MATERIAL_SOURCE_VIDEOS = 2
        const val MAX_SELECTED_SOUNDS = 3
        const val SCENE_EDIT_MEDIA_TAG = "SceneEditMedia"
        const val CURRENT_IMAGE_TITLE = "当前图片"
        const val CURRENT_VIDEO_TITLE = "当前视频"
    }

    private val _uiState = MutableStateFlow(
        createInitialState(isLoading = true),
    )
    val uiState: StateFlow<SceneEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var initializedKey: String? = null
    private var initializeJob: Job? = null
    private var originalEditableScene: EditableScene? = null
    private var soundSelectionChanged = false
    private var particleSelectionChanged = false

    private fun showToast(message: UiText) {
        // Toast 是一次性副作用，统一走事件流，避免保存在 UiState 中被重放。
        viewModelScope.launch {
            _events.send(EditEvent.ShowToast(message))
        }
    }

    init {
        val sceneId: String? = savedStateHandle["sceneId"]
        val category: SceneCategory? = savedStateHandle["category"]
        if (sceneId != null || category != null) {
            initialize(sceneId, category)
        }
    }

    fun setSceneParameters(sceneId: String?, category: SceneCategory?) {
        initialize(sceneId, category, force = true)
    }

    private fun initialize(sceneId: String?, category: SceneCategory?, force: Boolean = false) {
        val key = "${sceneId.orEmpty()}|${category?.name.orEmpty()}"
        if (!force && initializedKey == key) return
        initializedKey = key
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "initialize start force=%s sceneId=%s category=%s key=%s",
            force,
            sceneId,
            category,
            key,
        )

        initializeJob?.cancel()
        _uiState.value = createInitialState(isLoading = true)
        initializeJob = viewModelScope.launch {
            val materialPresets = loadMaterialPresets()
            val soundPresets = loadSoundPresets()
            val particlePresets = loadParticlePresets()
            _uiState.update {
                it.copy(
                    materialState = it.materialState.copy(presets = materialPresets),
                    soundState = it.soundState.copy(presets = soundPresets),
                    particleState = it.particleState.copy(presets = particlePresets),
                )
            }
            if (sceneId == null) {
                initializeNewScene(category ?: SceneCategory.FOCUS, materialPresets, soundPresets)
            } else {
                initializeExistingScene(
                    sceneId = sceneId,
                    fallbackCategory = category ?: SceneCategory.FOCUS,
                    materialPresets = materialPresets,
                    particlePresets = particlePresets,
                )
            }
        }
    }

    fun onEditorInactive() {
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "editor inactive reset originalScene=%s soundChanged=%s particleChanged=%s",
            originalEditableScene?.id,
            soundSelectionChanged,
            particleSelectionChanged,
        )
        initializedKey = null
        initializeJob?.cancel()
        initializeJob = null
        _uiState.value = createInitialState(isLoading = false)
        originalEditableScene = null
        soundSelectionChanged = false
        particleSelectionChanged = false
    }

    fun onMaterialSourceTabSelected(index: Int) {
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "source tab selected index=%d previous=%d selectedId=%s",
            index,
            _uiState.value.materialState.sourceTab,
            _uiState.value.materialState.selectedMaterialId,
        )
        _uiState.update {
            it.copy(
                materialState = it.materialState.copy(sourceTab = index),
            )
        }
    }

    fun onMaterialSelected(material: MaterialPreset) {
        _uiState.update { state ->
            state.copy(
                materialState = state.materialState.copy(selectedMaterial = material),
            )
        }
    }

    /**
     * 处理系统 Photo Picker 选择的图片，并先复制到应用私有目录，避免临时授权失效。
     */
    fun onLocalImagePicked(sourceUri: Uri) {
        viewModelScope.launch {
            try {
                val stableUri = localMediaStore.copyPickedMediaToInternalStorage(sourceUri, "img")
                val preset = MaterialPreset(
                    id = "local-image-${stableUri.lastPathSegment ?: stableUri.hashCode()}",
                    title = "Selected Image",
                    backgroundUri = stableUri.toString(),
                    typeLabel = LOCAL_IMAGE_LABEL,
                )
                onMaterialSelected(preset)
                _uiState.update {
                    it.copy(
                        materialState = it.materialState.copy(
                            sourceTab = MATERIAL_SOURCE_IMAGES,
                        ),
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "从 Photo Picker 持久化所选图片失败")
                _events.send(EditEvent.ShowToast(UiText.DynamicString("图片导入失败")))
            }
        }
    }

    /**
     * 处理系统 Photo Picker 选择的视频，并先复制到应用私有目录。
     */
    fun onLocalVideoPicked(sourceUri: Uri) {
        viewModelScope.launch {
            try {
                val stableUri = localMediaStore.copyPickedMediaToInternalStorage(sourceUri, "vid")
                val preset = MaterialPreset(
                    id = "local-video-${stableUri.lastPathSegment ?: stableUri.hashCode()}",
                    title = "Selected Video",
                    videoUri = stableUri.toString(),
                    typeLabel = LOCAL_VIDEO_LABEL,
                )
                onMaterialSelected(preset)
                _uiState.update {
                    it.copy(
                        materialState = it.materialState.copy(
                            sourceTab = MATERIAL_SOURCE_VIDEOS,
                        ),
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "从 Photo Picker 持久化所选视频失败")
                _events.send(EditEvent.ShowToast(UiText.DynamicString("视频导入失败")))
            }
        }
    }

    /**
     * 处理通过系统文档选择器（SAF / OpenDocument）选择的音频。
     * 与图片/视频类似：在保存前将内容复制到私有存储以确保持久化，创建 SoundPreset。
     *
     * 关于 UX 变更的说明（依据合规审查）：
     * 之前 LOCAL 音频分类允许通过 MediaStore + READ_MEDIA_AUDIO 权限浏览完整本机音频列表，
     * 并从中进行多选。
     * 为移除广域 READ_MEDIA_AUDIO 权限（符合 google-play-compliance-risks.md 的建议），
     * 我们现在仅支持“通过系统选择器挑选具体音频文件”。
     * 这是为合规而做出的有意权衡：不再支持“无需权限即可浏览全部本机音频列表”。
     * 用户可通过反复使用选择器添加本地音频（最多 MAX_SELECTED_SOUNDS 个）。
     * 如果产品要求恢复完整列表浏览，则需要重新引入权限（并在 Data Safety 中正确说明）。
     */
    fun onLocalAudioPicked(sourceUri: Uri) {
        viewModelScope.launch {
            try {
                val stableUri = localMediaStore.copyPickedMediaToInternalStorage(sourceUri, "aud")
                val preset = SoundPreset(
                    id = "local-audio-${stableUri.lastPathSegment ?: stableUri.hashCode()}",
                    title = "Selected Audio",
                    description = "本机音频",
                    uri = stableUri.toString(),
                    category = NoiseCategory.LOCAL.displayName,
                    tags = listOf(NoiseCategory.LOCAL.displayName),
                    coverResName = LOCAL_AUDIO_COVER, // 复用现有资源
                )
                onSoundPresetSelected(preset)
            } catch (e: Exception) {
                Timber.e(e, "从文档选择器持久化所选音频失败")
                _events.send(EditEvent.ShowToast(UiText.DynamicString("音频导入失败")))
            }
        }
    }

    fun onSoundCategorySelected(index: Int) {
        _uiState.update {
            it.copy(
                soundState = it.soundState.copy(selectedCategory = index),
            )
        }
    }

    fun onSoundSelected(id: String) {
        val sound = _uiState.value.soundState.findSoundPreset(id) ?: return
        onSoundPresetSelected(sound)
    }

    fun onSoundPresetSelected(sound: SoundPreset) {
        soundSelectionChanged = true
        val soundState = _uiState.value.soundState
        val isAtLimit = sound.id !in soundState.selectedSoundIds && soundState.selectedSoundIds.size >= MAX_SELECTED_SOUNDS
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "onSoundSelected id=%s currentIds=%s isAtLimit=%s originalAudioCount=%d",
            sound.id,
            soundState.selectedSoundIds,
            isAtLimit,
            originalEditableScene?.audioTracks?.size ?: -1,
        )
        if (isAtLimit) {
            viewModelScope.launch {
                _events.send(
                    EditEvent.ShowToast(
                        UiText.StringResource(
                            resId = R.string.scene_edit_max_sound_limit,
                            args = listOf(MAX_SELECTED_SOUNDS),
                        ),
                    ),
                )
            }
            return
        }
        _uiState.update { currentState ->
            val selectedSoundIds = when {
                sound.id in currentState.soundState.selectedSoundIds -> currentState.soundState.selectedSoundIds - sound.id
                else -> currentState.soundState.selectedSoundIds + sound.id
            }
            val localPresets = if (sound.isLocalAudioPreset()) {
                currentState.soundState.localPresets.upsert(sound)
            } else {
                currentState.soundState.localPresets
            }
            currentState.copy(
                soundState = currentState.soundState.copy(
                    localPresets = localPresets,
                    selectedSoundIds = selectedSoundIds,
                    isPreviewPlaying = false,
                ),
            )
        }
    }

    fun onParticleSelected(id: String) {
        particleSelectionChanged = true
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "onParticleSelected id=%s currentSelected=%s originalParticle=%s",
            id,
            _uiState.value.particleState.selectedParticleId,
            originalEditableScene?.controlSettings?.particle,
        )
        _uiState.update { currentState ->
            currentState.copy(
                particleState = currentState.particleState.copy(
                    selectedParticleId = id,
                ),
            )
        }
    }

    fun onParticleCategorySelected(index: Int) {
        _uiState.update {
            it.copy(
                particleState = it.particleState.copy(selectedCategory = index),
            )
        }
    }

    fun onSoundPreviewToggle() {
        val state = _uiState.value
        if (state.selectedSounds.isEmpty()) {
            showToast(ValidationIssue.SoundRequired.toUiText())
            return
        }
        _uiState.update {
            it.copy(
                soundState = it.soundState.copy(
                    isPreviewPlaying = !it.soundState.isPreviewPlaying,
                ),
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(
                description = description,
            )
        }
    }

    fun onPreviousClick() {
        onStepIndicatorSelected(_uiState.value.currentStep.number - 1)
    }

    fun onNextClick() {
        val state = _uiState.value
        if (state.currentStep == SceneEditStep.Preview) {
            onPreviewAction()
            return
        }
        advanceStep()
    }

    fun onStepIndicatorSelected(stepNumber: Int) {
        val targetStep = SceneEditStep.entries.firstOrNull { it.number == stepNumber } ?: return
        navigateToStep(targetStep)
    }

    /** 返回 true 表示成功推进了一步 */
    private fun advanceStep(): Boolean {
        val state = _uiState.value
        val nextStep = SceneEditStep.entries.firstOrNull { it.number == state.currentStep.number + 1 }
            ?: return false
        navigateToStep(nextStep)
        return _uiState.value.currentStep != state.currentStep
    }

    /** Preview 步骤的操作：校验标题 → 触发保存 */
    private fun onPreviewAction() {
        val state = _uiState.value
        if (state.currentStep != SceneEditStep.Preview) return
        val issue = state.validationIssue()
        if (issue != null) {
            showToast(issue.toUiText())
            return
        }
        viewModelScope.launch { saveCurrentScene() }
    }

    private suspend fun initializeNewScene(
        category: SceneCategory,
        materialPresets: List<MaterialPreset>,
        soundPresets: List<SoundPreset>,
    ) {
        originalEditableScene = null
        soundSelectionChanged = false
        particleSelectionChanged = false
        _uiState.update {
            it.copy(
                isLoading = false,
                sceneId = null,
                category = category,
                title = defaultSceneTitle(context),
                description = "",
                materialState = it.materialState.copy(selectedMaterial = null),
                soundState = it.soundState.copy(selectedSoundIds = emptyList()),
                controlSettings = SceneControlSettings(),
                currentStep = SceneEditStep.Material,
            )
        }
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "init new scene category=%s materialCount=%d soundCount=%d",
            category,
            materialPresets.size,
            soundPresets.size,
        )
    }

    private suspend fun initializeExistingScene(
        sceneId: String,
        fallbackCategory: SceneCategory,
        materialPresets: List<MaterialPreset>,
        particlePresets: List<ParticlePreset>,
    ) {
        val editableScene = getEditableScene(sceneId)
        if (editableScene == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sceneId = sceneId,
                    category = fallbackCategory,
                )
            }
            showToast(UiText.StringResource(R.string.scene_edit_no_scene_found))
            return
        }
        val existingContentMaterial = editableScene.asContentMaterial()
        val selectedMaterial = editableScene.findMatchingPreset(materialPresets)
            ?: existingContentMaterial
            ?: materialPresets.firstOrNull()
        val selectedParticle = editableScene.controlSettings.particle.findMatchingPreset(particlePresets)
        val localAudioPresets = editableScene.audioTracks
            .filter { it.id.isLocalAudioId() }
            .map { it.asLocalSoundPreset() }
        _uiState.update {
            it.copy(
                isLoading = false,
                sceneId = editableScene.id,
                category = editableScene.category,
                title = editableScene.title,
                description = editableScene.description,
                materialState = it.materialState.copy(
                    selectedMaterial = selectedMaterial,
                ),
                soundState = it.soundState.copy(
                    localPresets = localAudioPresets,
                    selectedSoundIds = editableScene.audioTracks.map { track -> track.id },
                ),
                particleState = it.particleState.copy(
                    // selectedCategory 是分类索引，不是 preset 索引；否则同一分类下多个粒子会恢复到错误 tab。
                    selectedCategory = selectedParticle?.let { particle ->
                        it.particleState.categories.indexOf(particle.category)
                    }?.takeIf { it >= 0 } ?: it.particleState.selectedCategory,
                    selectedParticleId = selectedParticle?.id,
                ),
                controlSettings = editableScene.controlSettings,
                currentStep = SceneEditStep.Material,
            )
        }
        originalEditableScene = editableScene
        soundSelectionChanged = false
        particleSelectionChanged = false
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "init existing scene id=%s title=%s audioCount=%d selectedSoundIds=%s originalParticle=%s selectedParticle=%s soundPresets=%d particlePresets=%d",
            editableScene.id,
            editableScene.title,
            editableScene.audioTracks.size,
            _uiState.value.soundState.selectedSoundIds,
            editableScene.controlSettings.particle,
            _uiState.value.particleState.selectedParticleId,
            _uiState.value.soundState.presets.size,
            particlePresets.size,
        )
    }

    private fun navigateToStep(targetStep: SceneEditStep) {
        val state = _uiState.value
        if (targetStep == state.currentStep) return

        if (targetStep.number < state.currentStep.number) {
            _uiState.update {
                it.copy(
                    currentStep = targetStep,
                )
            }
            return
        }

        if (!state.canGoNext) return

        var workingState = state
        while (workingState.currentStep.number < targetStep.number) {
            val issue = workingState.validationIssue()
            if (issue != null) {
                showToast(issue.toUiText())
                return
            }
            workingState = workingState.copy(
                currentStep = workingState.currentStep.nextStep(),
            )
        }

        _uiState.value = workingState
    }

    private fun SceneEditStep.nextStep(): SceneEditStep {
        if (this == SceneEditStep.Material) return SceneEditStep.Sound
        if (this == SceneEditStep.Sound) return SceneEditStep.Particle
        if (this == SceneEditStep.Particle) return SceneEditStep.Preview
        return SceneEditStep.Preview
    }

    private suspend fun saveCurrentScene() {
        val state = _uiState.value
        val material = state.selectedMaterial ?: state.materialState.presets.firstOrNull() ?: return
        val originalScene = originalEditableScene
        val particle = state.particleState.selectedParticle
        val particleSettings = when {
            particleSelectionChanged -> particle?.toParticleSettings() ?: SceneParticleSettings()
            else -> originalScene?.controlSettings?.particle ?: particle?.toParticleSettings() ?: SceneParticleSettings()
        }
        val audioTracks = when {
            soundSelectionChanged -> state.selectedSounds.map { sound ->
                SceneEditAudio(
                    id = sound.id,
                    title = sound.title,
                    uri = sound.uri,
                )
            }
            originalScene != null -> originalScene.audioTracks
            else -> state.selectedSounds.map { sound ->
                SceneEditAudio(
                    id = sound.id,
                    title = sound.title,
                    uri = sound.uri,
                )
            }
        }
        Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
            "save start sceneId=%s originalScene=%s soundChanged=%s particleChanged=%s currentSoundIds=%s originalAudioCount=%d currentParticleId=%s originalParticle=%s resolvedAudioCount=%d resolvedParticle=%s material=%s",
            state.sceneId,
            originalScene?.id,
            soundSelectionChanged,
            particleSelectionChanged,
            state.soundState.selectedSoundIds,
            originalScene?.audioTracks?.size ?: -1,
            state.particleState.selectedParticleId,
            originalScene?.controlSettings?.particle,
            audioTracks.size,
            particleSettings,
            material.id,
        )
        val input = SceneEditInput(
            id = state.sceneId,
            title = state.title.trim(),
            description = state.description.trim(),
            category = state.category,
            backgroundResName = material.backgroundResName,
            backgroundUri = material.backgroundUri,
            videoUri = material.videoUri,
            videoVolume = originalEditableScene?.videoVolume ?: 0f,
            audioTracks = audioTracks,
            controlSettings = state.controlSettings.copy(particle = particleSettings),
        )

        _uiState.update { it.copy(isSaving = true) }
        runCatching { saveSceneEdit(input) }
            .onSuccess { savedId ->
                Timber.tag(SCENE_EDIT_MEDIA_TAG).d(
                    "save success savedId=%s audioCount=%d particle=%s",
                    savedId,
                    input.audioTracks.size,
                    input.controlSettings.particle,
                )
                _uiState.update {
                    it.copy(
                        sceneId = savedId,
                        isSaving = false,
                    )
                }
                _events.send(EditEvent.ShowToast(UiText.StringResource(R.string.scene_edit_success)))
                _events.send(EditEvent.SaveSucceeded)
            }
            .onFailure { throwable ->
                Timber.tag(SCENE_EDIT_MEDIA_TAG).e(
                    throwable,
                    "save failed sceneId=%s audioCount=%d particle=%s",
                    state.sceneId,
                    input.audioTracks.size,
                    input.controlSettings.particle,
                )
                _uiState.update { it.copy(isSaving = false) }
                _events.send(
                    EditEvent.ShowToast(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.scene_edit_failed),
                    ),
                )
            }
    }

    private fun SceneEditUiState.validationIssue(): ValidationIssue? {
        if (currentStep == SceneEditStep.Material) {
            return if (selectedMaterial == null) ValidationIssue.MaterialRequired else null
        }
        if (currentStep == SceneEditStep.Sound) {
            val hasSoundData = selectedSounds.isNotEmpty() ||
                (!soundSelectionChanged && originalEditableScene?.audioTracks.orEmpty().isNotEmpty())
            return if (hasSoundData) null else ValidationIssue.SoundRequired
        }
        if (currentStep == SceneEditStep.Preview) {
            if (title.isBlank()) return ValidationIssue.TitleRequired
            return null
        }
        return null
    }

    private fun ValidationIssue.toUiText(): UiText {
        if (this == ValidationIssue.MaterialRequired) {
            return UiText.StringResource(R.string.scene_edit_select_scene_material)
        }
        if (this == ValidationIssue.SoundRequired) {
            return UiText.StringResource(R.string.scene_edit_select_scene_sound)
        }
        return UiText.StringResource(R.string.scene_edit_enter_title)
    }

    private fun EditableScene.findMatchingPreset(
        materialPresets: List<MaterialPreset>,
    ): MaterialPreset? = materialPresets.firstOrNull { preset ->
        preset.backgroundResName == backgroundResName &&
            preset.backgroundUri == backgroundUri &&
            preset.videoUri == videoUri
    }

    private fun EditableScene.asContentMaterial(): MaterialPreset? = when {
        backgroundUri != null -> MaterialPreset(
            id = "image-current-$id",
            title = title.ifBlank { CURRENT_IMAGE_TITLE },
            backgroundUri = backgroundUri,
            typeLabel = LOCAL_IMAGE_LABEL,
        )
        videoUri?.startsWith("content://") == true -> MaterialPreset(
            id = "video-current-$id",
            title = title.ifBlank { CURRENT_VIDEO_TITLE },
            videoUri = videoUri,
            typeLabel = LOCAL_VIDEO_LABEL,
        )
        else -> null
    }

    private fun SceneEditAudio.asLocalSoundPreset(): SoundPreset = SoundPreset(
        id = id,
        title = title,
        description = LOCAL_AUDIO_DESCRIPTION,
        uri = uri,
        category = NoiseCategory.LOCAL.displayName,
        tags = listOf(NoiseCategory.LOCAL.displayName),
        coverResName = LOCAL_AUDIO_COVER,
    )

    private suspend fun loadMaterialPresets(): List<MaterialPreset> {
        val backgrounds = runCatching {
            getBackgroundResources().first()
                .map { it.asMaterialPreset() }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to load background resources")
        }.getOrDefault(emptyList())

        val videos = runCatching {
            getVideoResources().first()
                .map { it.asMaterialPreset() }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to load video resources")
        }.getOrDefault(emptyList())

        return backgrounds + videos
    }

    private fun BackgroundResource.asMaterialPreset(): MaterialPreset = MaterialPreset(
        id = "preset-background-$id",
        title = title,
        backgroundResName = id,
        thumbnailUri = uri,
        typeLabel = PRESET_IMAGE_LABEL,
    )

    private fun VideoResource.asMaterialPreset(): MaterialPreset = MaterialPreset(
        id = "preset-video-$id",
        title = title,
        videoUri = uri,
        coverResName = thumbnailResName,
        thumbnailUri = uri,
        typeLabel = PRESET_VIDEO_LABEL,
    )

    private suspend fun loadSoundPresets(): List<SoundPreset> = runCatching {
        getNoiseResources().first()
            .map { it.asSoundPreset() }
    }.onFailure { throwable ->
        Timber.e(throwable, "Failed to load noise resources")
    }.getOrDefault(emptyList())

    private fun NoiseResource.asSoundPreset(): SoundPreset = SoundPreset(
        id = id,
        title = title,
        description = description,
        uri = uri,
        category = category.displayName,
        tags = tags,
        coverResName = thumbnailResName,
    )

    private suspend fun loadParticlePresets(): List<ParticlePreset> = runCatching {
        getParticleResources().first()
            .map { it.asParticlePreset() }
    }.onFailure { throwable ->
        Timber.e(throwable, "Failed to load particle resources")
    }.getOrDefault(emptyList())

    private fun ParticleResource.asParticlePreset(): ParticlePreset = ParticlePreset(
        id = id,
        title = title,
        description = description,
        category = category.toDisplayName(),
        effect = effect,
        effectLabel = effect.toDisplayName(),
        intensity = intensity,
        wind = wind,
        quality = quality,
        foregroundGlassEnabled = foregroundGlassEnabled,
        tags = tags,
        coverResName = thumbnailResName,
    )

    private fun ParticlePreset.toParticleSettings(): SceneParticleSettings {
        val particleEffect = when (effect) {
            ParticleEffect.RAIN -> {
                SceneParticleEffect.Rain
            }
            ParticleEffect.SNOW -> {
                SceneParticleEffect.Snow
            }
            ParticleEffect.FIREFLIES -> {
                SceneParticleEffect.Fireflies
            }
            else -> {
                SceneParticleEffect.None
            }
        }
        val particleQuality = when (quality) {
            ParticleQuality.LOW -> {
                SceneParticleQuality.Low
            }
            ParticleQuality.MEDIUM -> {
                SceneParticleQuality.Medium
            }
            else -> {
                SceneParticleQuality.High
            }
        }
        return SceneParticleSettings(
            effect = particleEffect,
            intensity = intensity,
            wind = wind,
            quality = particleQuality,
            foregroundGlassEnabled = foregroundGlassEnabled,
        )
    }

    private fun SceneParticleSettings.findMatchingPreset(
        particlePresets: List<ParticlePreset>,
    ): ParticlePreset? {
        if (effect == SceneParticleEffect.None) return null
        return particlePresets.firstOrNull { preset ->
            val presetSettings = preset.toParticleSettings()
            presetSettings.effect == effect &&
                presetSettings.intensity == intensity &&
                presetSettings.wind == wind &&
                presetSettings.quality == quality &&
                presetSettings.foregroundGlassEnabled == foregroundGlassEnabled
        } ?: particlePresets.firstOrNull { it.toParticleSettings().effect == effect }
    }

    private fun createInitialState(isLoading: Boolean): SceneEditUiState = SceneEditUiState(
        isLoading = isLoading,
        title = defaultSceneTitle(context),
        materialState = MaterialSelectionState(),
        soundState = SoundSelectionState(),
        particleState = ParticleSelectionState(),
    )
}

private fun SoundSelectionState.isLocalCategorySelected(): Boolean = categories.getOrNull(selectedCategory) == NoiseCategory.LOCAL.displayName

private fun SoundSelectionState.findSoundPreset(id: String): SoundPreset? = (presets + localPresets).firstOrNull { it.id == id }

private fun List<SoundPreset>.upsert(sound: SoundPreset): List<SoundPreset> = filterNot { it.id == sound.id } + sound

private fun SoundPreset.isLocalAudioPreset(): Boolean = id.isLocalAudioId() || category == NoiseCategory.LOCAL.displayName

private fun String.isLocalAudioId(): Boolean = startsWith("audio-")
