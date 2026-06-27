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
package com.wujia.feature.scene.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.wujia.feature.scene.impl.ui.PlayerMediaCoordinator
import com.wujia.feature.scene.impl.ui.SceneActionsBundle
import com.wujia.feature.scene.impl.ui.SceneAudioActions
import com.wujia.feature.scene.impl.ui.SceneControlActions
import com.wujia.feature.scene.impl.ui.SceneNavigationActions
import com.wujia.feature.scene.impl.ui.ScenePageContent
import com.wujia.feature.scene.impl.ui.ScenePanelActions
import com.wujia.feature.scene.impl.ui.SceneParticleActions
import com.wujia.feature.scene.impl.ui.component.rememberSystemClockDisplay
import com.wujia.feature.scene.impl.ui.dialog.SceneRootDialogHost
import com.wujia.feature.scene.impl.ui.dialog.rememberSceneRootDialogState
import com.wujia.feature.scene.impl.ui.overlay.SceneChromeOverlay
import com.wujia.feature.scene.impl.ui.overlay.SceneClockOverlay
import com.wujia.feature.scene.impl.ui.overlay.ScenePanelOverlay
import com.wujia.feature.scene.impl.ui.overlay.rememberSceneGuideState
import com.wujia.feature.scene.impl.ui.previewSceneResources
import com.wujia.feature.scene.impl.ui.rememberChromeAutoHideState
import com.wujia.feature.scene.impl.ui.sceneLayoutProfile
import com.wujia.feature.scene.impl.ui.viewmodel.SceneUiState
import com.wujia.feature.scene.impl.ui.viewmodel.SceneViewModel
import com.wujia.feature.scenecontrol.impl.ui.component.controlItemTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMaxHeight
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinHeight
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinWidth
import com.wujia.feature.scenecontrol.impl.ui.component.controlTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlValueFontSize
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlViewModel
import com.wujia.feature.settings.impl.SceneHostedSettingsDialogHost
import com.wujia.foundation.alarm.VelarisAlarmController
import com.wujia.foundation.designsystem.pager.StackedScrollPager
import com.wujia.foundation.designsystem.pager.VerticalPageTurnLayer
import com.wujia.foundation.designsystem.pager.VerticalTurnDirection
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.ProvideVelarisBackdrop
import com.wujia.foundation.designsystem.theme.ProvideVelarisBlurState
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.rememberVelarisBackdropState
import com.wujia.foundation.designsystem.theme.rememberVelarisBlurState
import com.wujia.foundation.designsystem.theme.velarisBackdropSource
import com.wujia.foundation.designsystem.theme.velarisBlurSource
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.player.VelarisPlayerPool
import com.wujia.foundation.player.rememberVelarisPlayerPool
import com.wujia.foundation.toolkit.display.KeepScreenOnEffect
import com.wujia.foundation.toolkit.display.WindowBrightnessEffect
import timber.log.Timber

internal data class SceneCategoryTurnState(val oldScene: SceneResource, val direction: VerticalTurnDirection)

private const val SCENE_SCREEN_TAG = "SceneScreen"

@Composable
fun SceneScreen(
    modifier: Modifier = Modifier,
    onOpenSceneEditPage: (sceneId: String?, category: SceneCategory?) -> Unit = { _, _ -> },
) {
    val viewModel: SceneViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSwiping by viewModel.isSwiping.collectAsStateWithLifecycle()

    val controlViewModel: SceneControlViewModel = hiltViewModel()
    val controlState by controlViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val alarmController = remember(context) { VelarisAlarmController(context) }
    var showAlarmDialog by rememberSaveable { mutableStateOf(false) }

    val playerPool = rememberVelarisPlayerPool()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentScenePageState = rememberUpdatedState(uiState.currentScenePage)
    val currentSceneIdState = rememberUpdatedState(uiState.currentScene?.id)
    val isPlayingState = rememberUpdatedState(uiState.isPlaying)

    DisposableEffect(lifecycleOwner, playerPool) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_STOP || !isPlayingState.value) return@LifecycleEventObserver

            val currentScenePage = currentScenePageState.value
            Timber.tag(SCENE_SCREEN_TAG).d(
                "app background pause sceneId=%s page=%d",
                currentSceneIdState.value,
                currentScenePage,
            )
            playerPool.get(currentScenePage).pause()
            viewModel.onPlayingStateChanged(false)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 时钟音频 URI（flip_clock_1.mp3）
    val clockAudioUri = remember(context) {
        val resId = context.resources.getIdentifier(
            "flip_clock_1",
            "raw",
            context.packageName,
        )
        if (resId != 0) "android.resource://${context.packageName}/$resId" else null
    }
    // 时钟实际可见状态（由 SceneScreenContent 回调更新，包含 !chromeVisible）
    var clockActuallyVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.currentScene?.id, uiState.currentScene?.audioTracks?.size) {
        Timber.tag(SCENE_SCREEN_TAG).d(
            "scene ui currentScene=%s audioCount=%d audioIds=%s video=%s page=%d playing=%s",
            uiState.currentScene?.id,
            uiState.currentScene?.audioTracks?.size ?: -1,
            uiState.currentScene?.audioTracks?.map { it.id },
            uiState.currentScene?.video?.uri,
            uiState.currentScenePage,
            uiState.isPlaying,
        )
    }

    DisposableEffect(alarmController) {
        onDispose { alarmController.release() }
    }

    LaunchedEffect(controlState.alarmReminderEnabled) {
        if (!controlState.alarmReminderEnabled) {
            showAlarmDialog = false
            alarmController.stop()
        }
    }

    // 专注场景保持屏幕常亮，助眠场景允许息屏
    KeepScreenOnEffect(
        keepScreenOn = uiState.currentScene?.category == SceneCategory.FOCUS,
    )

    PlayerMediaCoordinator(
        playerPool = playerPool,
        currentScene = uiState.currentScene,
        currentScenePage = uiState.currentScenePage,
        isPlaying = uiState.isPlaying,
        isSwiping = isSwiping,
        controlViewModel = controlViewModel,
        clockVisible = clockActuallyVisible,
        clockAudioVolume = controlState.clockAudioVolume,
        clockAudioUri = clockAudioUri,
        onPlayingStateChanged = viewModel::onPlayingStateChanged,
        onTimerExpired = {
            if (controlState.alarmReminderEnabled && alarmController.start()) {
                showAlarmDialog = true
            }
        },
    )

    val onPlayerClick: () -> Unit = onPlayerClick@{
        val scene = uiState.currentScene ?: return@onPlayerClick
        val controller = playerPool.get(uiState.currentScenePage)
        if (uiState.isPlaying) {
            Timber.tag(SCENE_SCREEN_TAG).d(
                "player click pause sceneId=%s page=%d audioCount=%d audioIds=%s",
                scene.id,
                uiState.currentScenePage,
                scene.audioTracks.size,
                scene.audioTracks.map { it.id },
            )
            controller.pause()
            viewModel.onPlayingStateChanged(false)
        } else {
            val hasMedia = scene.video != null || scene.audioTracks.isNotEmpty()
            val hasParticleEffect =
                scene.controlSettings.particle.effect != SceneParticleEffect.None
            Timber.tag(SCENE_SCREEN_TAG).d(
                "player click play sceneId=%s page=%d hasMedia=%s hasParticleEffect=%s audioCount=%d audioIds=%s",
                scene.id,
                uiState.currentScenePage,
                hasMedia,
                hasParticleEffect,
                scene.audioTracks.size,
                scene.audioTracks.map { it.id },
            )
            if (hasMedia) {
                controller.play()
            }
            viewModel.onPlayingStateChanged(hasMedia || hasParticleEffect)
        }
    }

    val onAudioVolumeChange: (String, Float) -> Unit = { audioId, volume ->
        playerPool.get(uiState.currentScenePage).setAudioVolume(audioId, volume)
    }

    SceneScreenContent(
        uiState = uiState,
        controlState = controlState,
        playerPool = playerPool,
        modifier = modifier,
        showAlarmDialog = showAlarmDialog,
        onAlarmDialogConfirm = {
            alarmController.stop()
            showAlarmDialog = false
        },
        onClockVisibleChange = { visible -> clockActuallyVisible = visible },
        actions = SceneActionsBundle(
            navigation = SceneNavigationActions(
                onScenePageChange = viewModel::onScenePageChange,
                onSceneModeChange = viewModel::onSceneModeChange,
                onOpenSceneEditPage = onOpenSceneEditPage,
                onSceneSelectById = viewModel::onSceneSelectById,
            ),
            panel = ScenePanelActions(
                onPanelChange = viewModel::onPanelChange,
                onSoundDialogVisibilityChange = viewModel::onSoundDialogVisibilityChange,
                onEditingSceneIdChange = viewModel::onEditingSceneIdChange,
                onEditingSceneOriginChange = viewModel::onEditingSceneOriginChange,
                onEditingSceneFinished = viewModel::onEditingSceneFinished,
                onSwipeStateChanged = viewModel::onSwipeStateChanged,
                onPlayerClick = onPlayerClick,
                onGuideCompleted = viewModel::onGuideCompleted,
            ),
            control = SceneControlActions(
                onTimerOptionChange = { index ->
                    controlViewModel.onTimerOptionChange(
                        index = index,
                        resumeIfPlaying = uiState.isPlaying,
                    )
                },
                onCustomTimerClick = { controlViewModel.onCustomTimerDialogVisibilityChange(true) },
                onCustomTimerConfirm = { hours, minutes ->
                    controlViewModel.onCustomTimerConfirm(
                        hours = hours,
                        minutes = minutes,
                        resumeIfPlaying = uiState.isPlaying,
                    )
                },
                onClockTimerConfirm = controlViewModel::onClockTimerConfirm,
                onCustomTimerDialogDismiss = {
                    controlViewModel.onCustomTimerDialogVisibilityChange(
                        false,
                    )
                },
                onShowCountdownClockChange = controlViewModel::onShowCountdownClockChange,
                onAlarmReminderChange = controlViewModel::onAlarmReminderChange,
                onCountdownClockPositionChange = controlViewModel::onCountdownClockPositionChange,
                onVisualControlItemValueChange = controlViewModel::onVisualControlItemValueChange,
            ),
            particle = SceneParticleActions(
                onEffectChange = controlViewModel::onParticleEffectChange,
                onIntensityChange = controlViewModel::onParticleIntensityChange,
                onWindChange = controlViewModel::onParticleWindChange,
                onQualityChange = controlViewModel::onParticleQualityChange,
                onForegroundGlassChange = controlViewModel::onParticleForegroundGlassChange,
            ),
            audio = SceneAudioActions(
                onVolumeChange = onAudioVolumeChange,
                onVolumesSave = viewModel::onAudioVolumesSave,
                onVideoVolumeChange = { volume ->
                    playerPool.get(uiState.currentScenePage).setVideoVolume(volume)
                },
                onVideoVolumeSave = viewModel::onVideoVolumeSave,
                onClockAudioVolumeChange = { volume ->
                    playerPool.get(uiState.currentScenePage).setClockAudioVolume(volume)
                },
                onClockAudioVolumeSave = controlViewModel::onClockAudioVolumeChange,
            ),
        ),
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun SceneScreenContent(
    modifier: Modifier = Modifier,
    uiState: SceneUiState,
    controlState: SceneControlUiState = SceneControlUiState(),
    playerPool: VelarisPlayerPool,
    actions: SceneActionsBundle,
    showAlarmDialog: Boolean = false,
    onAlarmDialogConfirm: () -> Unit = {},
    onClockVisibleChange: (Boolean) -> Unit = {},
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val blurState = rememberVelarisBlurState()
    val backdrop = rememberVelarisBackdropState()
    val inspectionMode = LocalInspectionMode.current

    LaunchedEffect(uiState.currentScene?.id, uiState.currentScenePage, inspectionMode) {
        Timber.tag(SCENE_SCREEN_TAG).d(
            "liquidGlass sceneId=%s page=%s inspectionMode=%s blurSourceHash=%s backdropSourceHash=%s",
            uiState.currentScene?.id,
            uiState.currentScenePage,
            inspectionMode,
            blurState.debugId,
            backdrop.debugId,
        )
    }

    WindowBrightnessEffect(
        brightness = controlState.brightness,
    )

    // 追踪 Composition 中的页面数（1=已停止，2=正在滑动）
    val renderingPageCount = remember { mutableIntStateOf(0) }
    val isSwiping by remember { derivedStateOf { renderingPageCount.intValue > 1 } }
    var categoryTurnState by remember { mutableStateOf<SceneCategoryTurnState?>(null) }
    val chromeAutoHideState = rememberChromeAutoHideState(
        isPlaying = uiState.isPlaying,
        activePanel = uiState.activePanel,
        isSoundDialogOpen = uiState.showSoundDialog,
        isCustomTimerDialogOpen = controlState.showCustomTimerDialog,
        currentSceneId = uiState.currentScene?.id,
    )

    // 通知 ViewModel 滑动状态变化
    LaunchedEffect(isSwiping) {
        actions.panel.onSwipeStateChanged(isSwiping)
    }

    ProvideVelarisBlurState(blurState) {
        ProvideVelarisBackdrop(backdrop) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                val spec = VelarisTheme.spec
                val scenes = uiState.scenes
                val currentScenePage = uiState.currentScenePage
                val profile = sceneLayoutProfile(
                    windowSizeClass = windowSizeClass,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                )
                val layoutType = profile.layoutType
                val edgePadding = profile.edgePadding
                val titleFontSize = profile.titleFontSize
                val modeTabsWidth = profile.modeTabsWidth
                val controlPanelMinWidth = layoutType.controlPanelMinWidth
                val controlPanelMinHeight = layoutType.controlPanelMinHeight
                val controlPanelMaxHeight = layoutType.controlPanelMaxHeight
                val controlTitleFontSize = layoutType.controlTitleFontSize
                val controlItemTitleFontSize = layoutType.controlItemTitleFontSize
                val controlValueFontSize = layoutType.controlValueFontSize
                val activeDialog = rememberSceneRootDialogState(
                    controlState = controlState,
                    showAlarmDialog = showAlarmDialog,
                    onAlarmDialogConfirm = onAlarmDialogConfirm,
                    clockVisible = controlState.showCountdownClock,
                    controlPanelMinWidth = controlPanelMinWidth,
                    controlPanelMinHeight = controlPanelMinHeight,
                    controlPanelMaxHeight = controlPanelMaxHeight,
                    controlTitleFontSize = controlTitleFontSize,
                    controlItemTitleFontSize = controlItemTitleFontSize,
                    controlValueFontSize = controlValueFontSize,
                    edgePadding = edgePadding,
                    actions = actions,
                    currentSceneAudioTracks = uiState.currentScene?.audioTracks.orEmpty(),
                    currentSceneVideoResource = uiState.currentScene?.video,
                    showSoundDialog = uiState.showSoundDialog,
                )
                val guideState = rememberSceneGuideState()
                val chromeVisible = chromeAutoHideState.visible || guideState.isActive
                val systemClock = rememberSystemClockDisplay(controlState.currentTimestampMillis)
                LaunchedEffect(guideState.isActive) {
                    if (guideState.isActive) {
                        chromeAutoHideState.show()
                    }
                }

                LaunchedEffect(uiState.hasCompletedGuide) {
                    guideState.onHasCompletedGuideChanged(uiState.hasCompletedGuide)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.isPlaying, chromeVisible) {
                            if (chromeVisible && uiState.isPlaying) {
                                detectTapGestures {
                                    chromeAutoHideState.hide()
                                }
                            }
                        },
                ) {
                    StackedScrollPager(
                        currentPage = currentScenePage,
                        pageCount = scenes.size,
                        onPageChange = actions.navigation.onScenePageChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .velarisBackdropSource(backdrop),
                    ) { page ->

                        // 追踪页面进出 Composition（用于检测滑动状态）
                        DisposableEffect(Unit) {
                            renderingPageCount.intValue++
                            onDispose { renderingPageCount.intValue-- }
                        }

                        ScenePageContent(
                            scene = scenes[page],
                            titleFontSize = titleFontSize,
                            showSubtitle = true,
                            edgePadding = edgePadding,
                            modifier = if (page == currentScenePage) {
                                Modifier.velarisBlurSource(blurState)
                            } else {
                                Modifier
                            },
                            isCurrentPage = page == currentScenePage,
                            isScenePlaying = uiState.isPlaying && page == currentScenePage,
                            playerController = if (inspectionMode) null else playerPool.get(page),
                            renderVideo = !inspectionMode,
                            chromeVisible = chromeVisible,
                        )
                    }
                }

                SceneChromeOverlay(
                    chromeVisible = chromeVisible,
                    guideState = guideState,
                    uiState = uiState,
                    controlState = controlState,
                    edgePadding = edgePadding,
                    modeTabsWidth = modeTabsWidth,
                    controlSpacing = profile.controlSpacing,
                    controlHeight = profile.controlHeight,
                    controlIconSize = profile.controlIconSize,
                    compactChrome = profile.compactChrome,
                    systemClockText = systemClock.text,
                    actions = actions,
                    onCategoryTurn = { categoryTurnState = it },
                )

                SceneClockOverlay(
                    controlState = controlState,
                    isPlaying = uiState.isPlaying,
                    chromeVisible = chromeVisible,
                    edgePadding = edgePadding,
                    currentScenePage = currentScenePage,
                    playerPool = playerPool,
                    onClockVisibleChange = onClockVisibleChange,
                )

                // 面板
                ScenePanelOverlay(
                    uiState = uiState,
                    controlState = controlState,
                    actions = actions,
                    profile = profile,
                )

                SceneRootDialogHost(
                    activeDialog = activeDialog,
                )

                SceneHostedSettingsDialogHost(
                    enabled = activeDialog == com.wujia.feature.scene.impl.ui.dialog.SceneRootDialogState.None,
                )

                guideState.ContentIfActive(
                    onGuideCompleted = actions.panel.onGuideCompleted,
                )

                val turnState = categoryTurnState
                val newScene = uiState.currentScene
                if (turnState != null && newScene != null) {
                    VerticalPageTurnLayer(
                        active = true,
                        direction = turnState.direction,
                        modifier = Modifier.fillMaxSize(),
                        onFinished = { categoryTurnState = null },
                        oldContent = {
                            ScenePageContent(
                                scene = turnState.oldScene,
                                titleFontSize = titleFontSize,
                                showSubtitle = true,
                                edgePadding = edgePadding,
                                modifier = Modifier,
                                isCurrentPage = false,
                                isScenePlaying = false,
                                playerController = null,
                                renderVideo = false,
                                chromeVisible = false,
                            )
                        },
                        newContent = {
                            ScenePageContent(
                                scene = newScene,
                                titleFontSize = titleFontSize,
                                showSubtitle = true,
                                edgePadding = edgePadding,
                                modifier = Modifier.velarisBlurSource(blurState),
                                isCurrentPage = false,
                                isScenePlaying = false,
                                playerController = null,
                                renderVideo = false,
                                chromeVisible = false,
                            )
                        },
                    )
                }

                if (!chromeVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    chromeAutoHideState.show()
                                }
                            },
                    )
                }
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun SceneScreenPreview() {
    val scene = previewSceneResources().first()
    val guideState = rememberSceneGuideState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ScenePageContent(
            scene = scene,
            titleFontSize = 36.sp,
            showSubtitle = true,
            edgePadding = 24.dp,
            modifier = Modifier.fillMaxSize(),
            isCurrentPage = true,
            isScenePlaying = false,
            playerController = null,
            renderVideo = false,
            chromeVisible = true,
        )

        SceneChromeOverlay(
            chromeVisible = true,
            guideState = guideState,
            uiState = SceneUiState(
                scenes = listOf(scene),
                allScenes = listOf(scene),
                isPlaying = true,
                currentSceneId = scene.id,
            ),
            controlState = SceneControlUiState(),
            edgePadding = 24.dp,
            modeTabsWidth = 180.dp,
            controlSpacing = 12.dp,
            controlHeight = 58.dp,
            controlIconSize = 34.dp,
            compactChrome = false,
            systemClockText = "09:41",
            actions = SceneActionsBundle(
                navigation = SceneNavigationActions(
                    onScenePageChange = {},
                    onSceneModeChange = {},
                    onOpenSceneEditPage = { _, _ -> },
                ),
                panel = ScenePanelActions(
                    onPanelChange = {},
                    onSoundDialogVisibilityChange = {},
                    onEditingSceneIdChange = {},
                    onEditingSceneOriginChange = {},
                    onEditingSceneFinished = {},
                    onSwipeStateChanged = {},
                ),
                control = SceneControlActions(),
                particle = SceneParticleActions(),
                audio = SceneAudioActions(),
            ),
            onCategoryTurn = {},
        )
    }
}
