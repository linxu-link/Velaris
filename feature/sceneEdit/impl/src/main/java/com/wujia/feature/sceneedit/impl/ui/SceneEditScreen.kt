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
package com.wujia.feature.sceneedit.impl.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.wujia.feature.sceneedit.impl.ui.panel.SceneEditMaterialContent
import com.wujia.feature.sceneedit.impl.ui.panel.SceneEditNoiseContent
import com.wujia.feature.sceneedit.impl.ui.panel.SceneEditParticleContent
import com.wujia.feature.sceneedit.impl.ui.panel.SceneEditPreviewContent
import com.wujia.feature.sceneedit.impl.ui.viewmodel.EditEvent
import com.wujia.feature.sceneedit.impl.ui.viewmodel.MaterialPreset
import com.wujia.feature.sceneedit.impl.ui.viewmodel.MaterialSelectionState
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SceneEditStep
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SceneEditUiState
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SceneEditViewModel
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SoundPreset
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SoundSelectionState
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.player.AudioMediaItem
import com.wujia.foundation.player.rememberVelarisPlayerController
import com.wujia.foundation.ui.StepHeader
import com.wujia.foundation.ui.StepIndicator
import com.wujia.foundation.ui.resolve
import timber.log.Timber
import com.wujia.foundation.ui.R as UiR

private const val SCENE_EDIT_SCREEN_TAG = "SceneEditScreen"

/**
 * 场景编辑核心屏幕。
 *
 * 采用 MVVM + UDF：通过 hiltViewModel 获取内部 SceneEditViewModel，
 * 仅消费 uiState 和事件回调，不持有可变状态。
 *
 * 主要职责：
 * - 集成系统媒体选择器（Photo Picker 用于图/视频，OpenDocument/SAF 用于音频），避免广域 READ_MEDIA_* 权限，符合合规要求。
 * - 监听 isActive 变化，驱动 VM 的 initialize / onEditorInactive（支持面板嵌入和独立导航两种模式）。
 * - 预览音效：使用 rememberVelarisPlayerController 在 Sound 步骤播放选中音频。
 * - 步骤切换、保存成功后回调 onSaved。
 *
 * 可见性标记为 internal，供同模块的 Entry 和 Panel 调用（保持与其他 feature 一致性）。
 */
@Composable
internal fun SceneEditScreen(
    modifier: Modifier = Modifier,
    sceneId: String? = null,
    category: SceneCategory? = null,
    isActive: Boolean = true,
    onSaved: () -> Unit = {},
) {
    val viewModel: SceneEditViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 使用系统 Photo Picker 进行本地图片/视频选择，以避免广域媒体权限。
    // 这符合 Google Play 针对用户主动媒体选择的推荐做法。
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalImagePicked(it) }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalVideoPicked(it) }
    }

    // 对于本地音频，使用 OpenDocument（SAF）而非 READ_MEDIA_AUDIO + 全量 MediaStore。
    // 这是针对音频文件的、与 Photo Picker 类似的做法。
    val pickLocalAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalAudioPicked(it) }
    }

    LaunchedEffect(sceneId, category, isActive) {
        if (isActive) {
            viewModel.setSceneParameters(sceneId, category)
        } else {
            viewModel.onEditorInactive()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditEvent.SaveSucceeded -> onSaved()
                is EditEvent.ShowToast -> {
                    Toast.makeText(context, event.message.resolve(context), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
    val soundPreviewItems =
        if (uiState.currentStep == SceneEditStep.Sound && uiState.soundState.isPreviewPlaying) {
            uiState.selectedSounds.map { sound ->
                AudioMediaItem(
                    id = sound.id,
                    uri = sound.uri,
                    title = sound.title,
                    loop = true,
                )
            }
        } else {
            emptyList()
        }
    // 预览音频在组件 dispose 时会通过 rememberVelarisPlayerController 自动完整释放（无残留分支）。
    rememberVelarisPlayerController(
        audioItems = soundPreviewItems,
        playWhenReady = soundPreviewItems.isNotEmpty(),
    )

    val onMaterialSourceTabSelected: (Int) -> Unit = { index ->
        Timber.tag(SCENE_EDIT_SCREEN_TAG).d(
            "tab selected index=%d currentTab=%d",
            index,
            uiState.materialState.sourceTab,
        )
        viewModel.onMaterialSourceTabSelected(index)
        // 注意：对于本地 tab，实际的 picker 是从素材 UI 中的 pick 按钮触发的
        // （使用系统 Photo Picker 以符合媒体权限政策）。
    }
    val onSoundCategorySelected: (Int) -> Unit = { index ->
        viewModel.onSoundCategorySelected(index)
        // 对于 LOCAL 音频分类，现在使用系统文档选择器（SAF），而非请求 READ_MEDIA_AUDIO + 加载全库。
        // 下方 UI 会在选择 LOCAL 时显示“从设备选择”的提示。
        //
        // 这是为合规而做的有意调整（移除广域音频权限）。
        // 详见 onLocalAudioPicked 中的 review 说明：原“浏览完整本机音频列表”的 UX 已被 picker-only 方式取代，以彻底避免权限。
    }

    SceneEditContent(
        uiState = uiState,
        onNextClick = viewModel::onNextClick,
        onMaterialSourceTabSelected = onMaterialSourceTabSelected,
        onMaterialSelected = viewModel::onMaterialSelected,
        onSoundCategorySelected = onSoundCategorySelected,
        onSoundSelected = viewModel::onSoundSelected,
        onSoundPresetSelected = viewModel::onSoundPresetSelected,
        onSoundPreviewToggle = viewModel::onSoundPreviewToggle,
        onParticleCategorySelected = viewModel::onParticleCategorySelected,
        onParticleSelected = viewModel::onParticleSelected,
        onStepSelected = viewModel::onStepIndicatorSelected,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onPickLocalImage = { pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onPickLocalVideo = { pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
        onPickLocalAudio = { pickLocalAudioLauncher.launch(arrayOf("audio/*")) },
        modifier = modifier,
    )
}

@Composable
private fun SceneEditContent(
    uiState: SceneEditUiState,
    onNextClick: () -> Unit,
    onMaterialSourceTabSelected: (Int) -> Unit,
    onMaterialSelected: (MaterialPreset) -> Unit,
    onSoundCategorySelected: (Int) -> Unit,
    onSoundSelected: (String) -> Unit,
    onSoundPresetSelected: (SoundPreset) -> Unit,
    onSoundPreviewToggle: () -> Unit,
    onParticleCategorySelected: (Int) -> Unit,
    onParticleSelected: (String) -> Unit,
    onStepSelected: (Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPickLocalImage: () -> Unit,
    onPickLocalVideo: () -> Unit,
    onPickLocalAudio: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentSceneEditWindowSizeClass(),
) {
    val spec = VelarisTheme.spec
    val currentStep = uiState.currentStep
    val stepTitle = when (currentStep) {
        SceneEditStep.Material -> stringResource(UiR.string.scene_edit_step1)
        SceneEditStep.Sound -> stringResource(UiR.string.scene_edit_step2)
        SceneEditStep.Particle -> stringResource(UiR.string.scene_edit_step3)
        SceneEditStep.Preview -> stringResource(UiR.string.scene_edit_step4)
    }
    val nextText = when (currentStep) {
        SceneEditStep.Preview if uiState.isSaving ->
            stringResource(UiR.string.scene_edit_saving)

        SceneEditStep.Preview ->
            stringResource(UiR.string.scene_edit_save)

        SceneEditStep.Material if uiState.selectedMaterial == null -> null
        SceneEditStep.Sound if uiState.selectedSounds.isEmpty() -> null
        else -> stringResource(UiR.string.common_next)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val profile = sceneEditLayoutProfile(
            windowSizeClass = windowSizeClass,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = profile.edgePadding),
        ) {
            StepHeader(
                stepTitle = stepTitle,
                nextText = nextText,
                onNextClick = onNextClick,
            )

            Spacer(Modifier.height(spec.spacing.small))

            StepIndicator(
                currentStep = uiState.currentStep.number,
                onStepSelected = onStepSelected,
            )

            Spacer(Modifier.height(spec.spacing.medium))

            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (targetState.number > initialState.number) {
                        slideInHorizontally(animationSpec = tween()) { it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween()) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween()) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween()) { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "step_transition",
            ) { step ->
                when (step) {
                    SceneEditStep.Material -> SceneEditMaterialContent(
                        materials = uiState.materialState.presets,
                        selectedMaterialId = uiState.materialState.selectedMaterialId,
                        selectedMaterial = uiState.selectedMaterial,
                        selectedSourceTab = uiState.materialState.sourceTab,
                        onSourceTabSelected = onMaterialSourceTabSelected,
                        onMaterialSelected = onMaterialSelected,
                        onPickLocalImage = onPickLocalImage,
                        onPickLocalVideo = onPickLocalVideo,
                    )

                    SceneEditStep.Sound -> SceneEditNoiseContent(
                        categories = uiState.soundState.categories,
                        sounds = uiState.soundState.filteredPresets,
                        selectedCategory = uiState.soundState.selectedCategory,
                        selectedSoundIds = uiState.soundState.selectedSoundIds,
                        selectedSounds = uiState.selectedSounds,
                        isPreviewPlaying = uiState.soundState.isPreviewPlaying,
                        onCategoryChange = onSoundCategorySelected,
                        onSoundChange = onSoundSelected,
                        onSoundPresetChange = onSoundPresetSelected,
                        onPreviewToggle = onSoundPreviewToggle,
                        onPickLocalAudio = onPickLocalAudio,
                    )

                    SceneEditStep.Particle -> SceneEditParticleContent(
                        categories = uiState.particleState.categories,
                        selectedCategory = uiState.particleState.selectedCategory,
                        particles = uiState.particleState.filteredPresets,
                        selectedParticleId = uiState.particleState.selectedParticleId,
                        onCategorySelected = onParticleCategorySelected,
                        onParticleSelected = onParticleSelected,
                    )

                    SceneEditStep.Preview -> SceneEditPreviewContent(
                        title = uiState.title,
                        description = uiState.description,
                        material = uiState.selectedMaterial,
                        sounds = uiState.selectedSounds,
                        particle = uiState.selectedParticle,
                        onTitleChange = onTitleChanged,
                        onDescriptionChange = onDescriptionChanged,
                    )
                }
            }

            Spacer(Modifier.height(spec.spacing.medium))
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun currentSceneEditWindowSizeClass(): WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

@LandscapePreviews
@Composable
private fun SceneEditScreenPreview() {
    SceneEditContent(
        uiState = SceneEditUiState(
            isLoading = false,
            materialState = MaterialSelectionState(
                presets = listOf(
                    MaterialPreset(
                        id = ProjectsIds.Scene.TRAIN_NIGHT,
                        title = "雾隐山居",
                    ),
                ),
                selectedMaterial = MaterialPreset(
                    id = ProjectsIds.Scene.TRAIN_NIGHT,
                    title = "雾隐山居",
                ),
            ),
            soundState = SoundSelectionState(
                presets = listOf(
                    SoundPreset(
                        id = ProjectsIds.Noise.RAIN,
                        title = "雨声",
                        description = "雨声轻敲窗棂",
                        uri = "",
                        category = "自然",
                    ),
                ),
                selectedSoundIds = listOf(ProjectsIds.Noise.RAIN),
            ),
            title = "雾隐山居",
            description = "薄雾穿林，清风醒神",
        ),
        onNextClick = {},
        onMaterialSourceTabSelected = {},
        onMaterialSelected = { _ -> },
        onSoundCategorySelected = {},
        onSoundSelected = {},
        onSoundPresetSelected = {},
        onSoundPreviewToggle = {},
        onParticleCategorySelected = {},
        onParticleSelected = {},
        onStepSelected = {},
        onTitleChanged = {},
        onDescriptionChanged = {},
        onPickLocalImage = {},
        onPickLocalVideo = {},
        onPickLocalAudio = {},
    )
}
