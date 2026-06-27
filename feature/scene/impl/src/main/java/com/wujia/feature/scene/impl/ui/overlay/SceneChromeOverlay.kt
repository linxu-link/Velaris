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
package com.wujia.feature.scene.impl.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.scene.impl.SceneCategoryTurnState
import com.wujia.feature.scene.impl.ui.AnimatedEdgeVisibility
import com.wujia.feature.scene.impl.ui.ChromeEdge
import com.wujia.feature.scene.impl.ui.SceneActionsBundle
import com.wujia.feature.scene.impl.ui.component.SceneSwipeHint
import com.wujia.feature.scene.impl.ui.previewSceneResources
import com.wujia.feature.scene.impl.ui.sceneCategoryTabs
import com.wujia.feature.scene.impl.ui.viewmodel.ScenePanelState
import com.wujia.feature.scene.impl.ui.viewmodel.SceneUiState
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.formatTimerText
import com.wujia.foundation.designsystem.pager.VerticalTurnDirection
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.tab.SceneSegmentedTabs
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.ui.LogoCard
import com.wujia.foundation.ui.PlayerCard
import com.wujia.foundation.ui.TimeCard
import timber.log.Timber

private const val SCENE_CHROME_TAG = "SceneChromeOverlay"

@Composable
internal fun SceneChromeOverlay(
    chromeVisible: Boolean,
    guideState: SceneGuideState,
    uiState: SceneUiState,
    controlState: SceneControlUiState,
    edgePadding: Dp,
    modeTabsWidth: Dp,
    controlSpacing: Dp,
    controlHeight: Dp,
    controlIconSize: Dp,
    compactChrome: Boolean,
    systemClockText: String,
    actions: SceneActionsBundle,
    onCategoryTurn: (SceneCategoryTurnState) -> Unit,
) {
    val spec = VelarisTheme.spec
    val bottomPadding = if (compactChrome) {
        spec.spacing.medium
    } else {
        edgePadding
    }

    LaunchedEffect(chromeVisible, uiState.currentScene?.id, controlHeight, controlIconSize) {
        Timber.tag(SCENE_CHROME_TAG).d(
            "PlayerCard host visible=%s sceneId=%s controlHeight=%s controlIconSize=%s isPlaying=%s",
            chromeVisible,
            uiState.currentScene?.id,
            controlHeight,
            controlIconSize,
            uiState.isPlaying,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 右侧边缘：场景左右切换引导目标（透明）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(edgePadding)
                .fillMaxHeight()
                .guideTarget(guideState, GuideTargetType.SceneSwipe),
        )

        // 左上角 Logo
        AnimatedEdgeVisibility(
            visible = chromeVisible,
            edge = ChromeEdge.TopStart,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = edgePadding, top = edgePadding)
                    .guideTarget(guideState, GuideTargetType.Logo),
            ) {
                LogoCard(
                    onClick = { actions.panel.onPanelChange(ScenePanelState.LIST) },
                )
            }
        }

        // 右上角模式切换
        AnimatedEdgeVisibility(
            visible = chromeVisible,
            edge = ChromeEdge.TopEnd,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .padding(end = edgePadding, top = edgePadding)
                    .width(modeTabsWidth)
                    .height(spec.size.controlSmall + spec.spacing.xSmall / 2)
                    .clip(RoundedCornerShape(spec.radii.pill)),
            ) {
                SceneSegmentedTabs(
                    items = sceneCategoryTabs(),
                    selectedIndex = uiState.selectedSceneMode,
                    onSelectedChange = { selectedIndex ->
                        if (selectedIndex == uiState.selectedSceneMode) {
                            actions.navigation.onSceneModeChange(selectedIndex)
                            return@SceneSegmentedTabs
                        }

                        uiState.currentScene?.let { scene ->
                            onCategoryTurn(
                                SceneCategoryTurnState(
                                    oldScene = scene,
                                    direction = if (selectedIndex > uiState.selectedSceneMode) {
                                        VerticalTurnDirection.TopToBottom
                                    } else {
                                        VerticalTurnDirection.BottomToTop
                                    },
                                ),
                            )
                        }
                        actions.navigation.onSceneModeChange(selectedIndex)
                    },
                )
            }
        }

        // 左下角播放控制
        AnimatedEdgeVisibility(
            visible = chromeVisible,
            edge = ChromeEdge.BottomStart,
            modifier = Modifier.align(Alignment.BottomStart),
        ) {
            Box(
                modifier = Modifier.padding(start = edgePadding, bottom = bottomPadding),
            ) {
                Row(
                    modifier = Modifier.guideTarget(guideState, GuideTargetType.Player),
                    horizontalArrangement = Arrangement.spacedBy(controlSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerCard(
                        isPlaying = uiState.isPlaying,
                        onIconClick = actions.panel.onPlayerClick,
                        onTextClick = { actions.panel.onSoundDialogVisibilityChange(true) },
                        modifier = Modifier.height(controlHeight),
                        minHeight = controlHeight,
                        controlSize = controlIconSize,
                    )
                }
            }
        }

        // 底部上滑提示
        AnimatedEdgeVisibility(
            visible = chromeVisible,
            edge = ChromeEdge.Bottom,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier
                    .guideTarget(guideState, GuideTargetType.Swipe)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -8) {
                                actions.panel.onPanelChange(ScenePanelState.CONTROL)
                            }
                        }
                    },
            ) {
                SceneSwipeHint(
                    visible = uiState.showControlPanel.not(),
                    bottomPadding = edgePadding,
                )
            }
        }

        // 右下角倒计时
        AnimatedEdgeVisibility(
            visible = chromeVisible,
            edge = ChromeEdge.BottomEnd,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Box(
                modifier = Modifier
                    .padding(end = edgePadding, bottom = edgePadding)
                    .guideTarget(guideState, GuideTargetType.Timer),
            ) {
                TimeCard(
                    onCardClick = actions.control.onCustomTimerClick,
                    showTitle = controlState.timerMode != SceneTimerMode.Clock,
                    timeText = if (controlState.timerMode == SceneTimerMode.Clock) {
                        systemClockText
                    } else {
                        controlState.timerRemainingMillis.formatTimerText()
                    },
                    modifier = Modifier.height(controlHeight),
                    minHeight = controlHeight,
                    controlSize = controlIconSize,
                )
            }
        }
    } // Box
}

@LandscapePreviews
@Composable
private fun SceneChromeOverlayPreview() {
    val scene = previewSceneResources().first()
    val guideState = rememberSceneGuideState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10181C)),
    ) {
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
            controlHeight = 60.dp,
            controlIconSize = 22.dp,
            compactChrome = false,
            systemClockText = "09:41",
            actions = SceneActionsBundle(
                navigation = com.wujia.feature.scene.impl.ui.SceneNavigationActions(
                    onScenePageChange = {},
                    onSceneModeChange = {},
                    onOpenSceneEditPage = { _, _ -> },
                ),
                panel = com.wujia.feature.scene.impl.ui.ScenePanelActions(
                    onPanelChange = {},
                    onSoundDialogVisibilityChange = {},
                    onEditingSceneIdChange = {},
                    onEditingSceneOriginChange = {},
                    onEditingSceneFinished = {},
                    onSwipeStateChanged = {},
                ),
                control = com.wujia.feature.scene.impl.ui.SceneControlActions(),
                particle = com.wujia.feature.scene.impl.ui.SceneParticleActions(),
                audio = com.wujia.feature.scene.impl.ui.SceneAudioActions(),
            ),
            onCategoryTurn = {},
        )
    }
}
