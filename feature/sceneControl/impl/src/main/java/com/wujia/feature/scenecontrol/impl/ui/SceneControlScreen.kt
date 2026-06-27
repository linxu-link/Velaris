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
package com.wujia.feature.scenecontrol.impl.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wujia.feature.scenecontrol.impl.ui.component.ScenePanelShell
import com.wujia.feature.scenecontrol.impl.ui.component.SceneTimePanel
import com.wujia.feature.scenecontrol.impl.ui.component.SceneVisualPanel
import com.wujia.feature.scenecontrol.impl.ui.component.controlItemTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMaxHeight
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinHeight
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinWidth
import com.wujia.feature.scenecontrol.impl.ui.component.controlTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlValueFontSize
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import com.wujia.foundation.ui.R

@Composable
/**
 * 场景控制主屏幕（非粒子部分）。
 *
 * 接收扁平化的状态和回调（上层已从 UiState 展开）。
 * 通过 ScenePanelShell 统一处理响应式布局参数
 * （minWidth、字体大小等），减少各子面板重复解包 layoutType 的代码。
 */
internal fun SceneControlScreen(modifier: Modifier = Modifier, sceneName: String = "", isCustomScene: Boolean = true, onAdjustDetailsClick: () -> Unit = {}, timerMode: SceneTimerMode = SceneTimerMode.Countdown, timerSelectedOption: Int = 2, onTimerOptionChange: (Int) -> Unit = {}, onCustomTimerClick: () -> Unit = {}, showCountdownClock: Boolean = false, onShowCountdownClockChange: (Boolean) -> Unit = {}, alarmReminderEnabled: Boolean = false, onAlarmReminderChange: (Boolean) -> Unit = {}, countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center, onCountdownClockPositionChange: (SceneCountdownClockPosition) -> Unit = {}, brightness: Float = 0f, darkness: Float = 0f, onVisualControlItemValueChange: (index: Int, value: Float) -> Unit = { _, _ -> }, onEditSceneClick: () -> Unit = {}) {
    ScenePanelShell(
        sceneName = sceneName,
        isCustomScene = isCustomScene,
        onAdjustDetailsClick = onAdjustDetailsClick,
        onEditSceneClick = onEditSceneClick,
        modifier = modifier,
    ) { layoutType ->
        // 集中从 layoutType 解包，避免在多个子面板里重复写相同的 takeOrElse 逻辑
        val minWidth = layoutType.controlPanelMinWidth
        val minHeight = layoutType.controlPanelMinHeight
        val maxHeight = layoutType.controlPanelMaxHeight
        val titleFontSize = layoutType.controlTitleFontSize
        val itemTitleFontSize = layoutType.controlItemTitleFontSize
        val valueFontSize = layoutType.controlValueFontSize

        val brightnessLabel = stringResource(R.string.scene_control_brightness)
        val darkLabel = stringResource(R.string.scene_control_dark)
        val visualControlItems = remember(brightness, darkness, brightnessLabel, darkLabel) {
            listOf(
                SoundControlItem(brightnessLabel, Icons.Outlined.GraphicEq, brightness),
                SoundControlItem(darkLabel, Icons.Outlined.Notifications, darkness),
            )
        }

        SceneVisualPanel(
            items = visualControlItems,
            onItemValueChange = onVisualControlItemValueChange,
            countdownClockPosition = countdownClockPosition,
            onCountdownClockPositionChange = onCountdownClockPositionChange,
            title = stringResource(R.string.scene_control_settings),
            minWidth = minWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            titleFontSize = titleFontSize,
            itemTitleFontSize = itemTitleFontSize,
            valueFontSize = valueFontSize,
            modifier = Modifier.weight(1f),
        )

        SceneTimePanel(
            timerMode = timerMode,
            selectedIndex = timerSelectedOption,
            onSelectedChange = onTimerOptionChange,
            showCountdownClock = showCountdownClock,
            onShowCountdownClockChange = onShowCountdownClockChange,
            alarmReminderEnabled = alarmReminderEnabled,
            onAlarmReminderChange = onAlarmReminderChange,
            onCustomTimerClick = onCustomTimerClick,
            minWidth = minWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            titleFontSize = titleFontSize,
            itemTitleFontSize = itemTitleFontSize,
            valueFontSize = valueFontSize,
            modifier = Modifier.weight(1f),
        )
    }
}

@LandscapePreviews
@Composable
private fun SceneControlScreenPreview() {
    var timerSelectedOption by remember { mutableIntStateOf(1) }
    var showCountdownClock by remember { mutableStateOf(true) }
    var alarmReminderEnabled by remember { mutableStateOf(false) }
    var countdownClockPosition by remember { mutableStateOf(SceneCountdownClockPosition.Center) }
    var brightness by remember { mutableFloatStateOf(0.72f) }
    var darkness by remember { mutableFloatStateOf(0.38f) }

    SceneControlScreen(
        sceneName = "风雪夜归人",
        isCustomScene = true,
        onAdjustDetailsClick = {},
        timerSelectedOption = timerSelectedOption,
        onTimerOptionChange = { timerSelectedOption = it },
        onCustomTimerClick = {},
        showCountdownClock = showCountdownClock,
        onShowCountdownClockChange = { showCountdownClock = it },
        alarmReminderEnabled = alarmReminderEnabled,
        onAlarmReminderChange = { alarmReminderEnabled = it },
        countdownClockPosition = countdownClockPosition,
        onCountdownClockPositionChange = { countdownClockPosition = it },
        brightness = brightness,
        darkness = darkness,
        onVisualControlItemValueChange = { index, value ->
            when (index) {
                0 -> brightness = value
                1 -> darkness = value
            }
        },
        onEditSceneClick = {},
        modifier = Modifier.width(1180.dp),
    )
}
