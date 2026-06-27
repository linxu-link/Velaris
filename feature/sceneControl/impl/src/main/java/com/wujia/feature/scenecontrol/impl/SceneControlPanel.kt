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
package com.wujia.feature.scenecontrol.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.scenecontrol.impl.ui.SceneControlScreen
import com.wujia.foundation.designsystem.panel.SwipeUpPanel
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneTimerMode

/**
 * 场景控制面板入口（由 scene 模块作为 overlay 嵌入）。
 *
 * 设计说明：
 * - 接收完整的 controlState（而非零散字段），减少 prop drilling。
 * - 仍保留细粒度回调，便于上层（scene）按需响应。
 * - 通过 SwipeUpPanel 提供上滑手势体验。
 */
/**
 * 场景控制面板公共入口（供 scene 模块调用）。
 *
 * 注意：未直接接收 UiState（避免暴露 internal 类型）。
 * 上层需将 UiState 字段拆解后传入对应回调和值。
 */
@Composable
fun SceneControlPanel(
    modifier: Modifier = Modifier,
    showPanel: Boolean,
    panelHeight: Dp = 380.dp,
    edgePadding: Dp = 8.dp,
    sceneName: String,
    isCustomScene: Boolean = true,
    onAdjustDetailsClick: () -> Unit = {},
    onEditSceneClick: () -> Unit = {},
    // 以下字段通常由上层从 SceneControlUiState 拆解后传入（避免在此暴露 internal 类型）
    timerMode: SceneTimerMode = SceneTimerMode.Countdown,
    timerSelectedOption: Int = 2,
    onTimerOptionChange: (Int) -> Unit = {},
    onCustomTimerClick: () -> Unit = {},
    showCountdownClock: Boolean = false,
    onShowCountdownClockChange: (Boolean) -> Unit = {},
    alarmReminderEnabled: Boolean = false,
    onAlarmReminderChange: (Boolean) -> Unit = {},
    countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center,
    onCountdownClockPositionChange: (SceneCountdownClockPosition) -> Unit = {},
    brightness: Float = 0f,
    darkness: Float = 0f,
    onVisualControlItemValueChange: (index: Int, value: Float) -> Unit = { _, _ -> },
    onVisibleChange: (Boolean) -> Unit,
) {
    SwipeUpPanel(
        visible = showPanel,
        onVisibleChange = onVisibleChange,
        panelHeight = panelHeight,
        edgePadding = edgePadding,
        modifier = modifier,
    ) {
        SceneControlScreen(
            sceneName = sceneName,
            isCustomScene = isCustomScene,
            onAdjustDetailsClick = onAdjustDetailsClick,
            timerMode = timerMode,
            timerSelectedOption = timerSelectedOption,
            onTimerOptionChange = onTimerOptionChange,
            onCustomTimerClick = onCustomTimerClick,
            showCountdownClock = showCountdownClock,
            onShowCountdownClockChange = onShowCountdownClockChange,
            alarmReminderEnabled = alarmReminderEnabled,
            onAlarmReminderChange = onAlarmReminderChange,
            countdownClockPosition = countdownClockPosition,
            onCountdownClockPositionChange = onCountdownClockPositionChange,
            brightness = brightness,
            darkness = darkness,
            onVisualControlItemValueChange = onVisualControlItemValueChange,
            onEditSceneClick = onEditSceneClick,
        )
    }
}

@LandscapePreviews
@Composable
private fun SceneControlPanelPreview() {
    SceneControlPanel(
        showPanel = true,
        panelHeight = 384.dp,
        sceneName = "风雪夜归人",
        onVisibleChange = {},
    )
}
