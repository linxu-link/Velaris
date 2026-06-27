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
package com.wujia.feature.scenecontrol.impl.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.ui.R

/**
 * 时间/定时器控制面板。
 * 包含：
 * - 定时选项（15/25/45/自定义）
 * - 倒计时钟显示开关
 * - 闹钟提醒开关
 * - 自定义时长对话框入口
 *
 * 通过参数接收响应式布局尺寸（minWidth / 字体），保持与视觉面板一致的卡片风格。
 */
@Composable
fun SceneTimePanel(
    modifier: Modifier = Modifier,
    timerMode: SceneTimerMode = SceneTimerMode.Countdown,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    showCountdownClock: Boolean,
    onShowCountdownClockChange: (Boolean) -> Unit,
    alarmReminderEnabled: Boolean,
    onAlarmReminderChange: (Boolean) -> Unit,
    onCustomTimerClick: () -> Unit = {},
    minWidth: Dp = 280.dp,
    minHeight: Dp = 180.dp,
    maxHeight: Dp = 220.dp,
    titleFontSize: TextUnit = TextUnit.Unspecified,
    itemTitleFontSize: TextUnit = TextUnit.Unspecified,
    valueFontSize: TextUnit = TextUnit.Unspecified,
) {
    val spec = VelarisTheme.spec
    val resolvedTitleFontSize = titleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedItemTitleFontSize = itemTitleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedValueFontSize = valueFontSize.takeOrElse { spec.typography.body }
    val options = listOf(
        stringResource(R.string.timer_15min),
        stringResource(R.string.timer_25min),
        stringResource(R.string.timer_45min),
        stringResource(R.string.timer_custom),
    )

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .height(maxHeight)
            .clip(RoundedCornerShape(spec.radii.panel))
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
                shape = RoundedCornerShape(spec.radii.panel),
            )
            .padding(vertical = spec.spacing.edgeSmall, horizontal = spec.spacing.large),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(spec.spacing.xSmall))

            Text(
                text = stringResource(R.string.timer_close),
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = resolvedTitleFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(spec.spacing.medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spec.spacing.small),
        ) {
            options.forEachIndexed { index, text ->
                TimerOptionButton(
                    text = text,
                    selected = timerMode == SceneTimerMode.Countdown && index == selectedIndex,
                    onClick = {
                        if (index == 3) onCustomTimerClick() else onSelectedChange(index)
                    },
                    fontSize = resolvedItemTitleFontSize,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spec.size.stroke)
                .background(spec.colors.stroke.copy(alpha = spec.alpha.stroke)),
        )

        Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Nightlight,
                contentDescription = null,
                tint = spec.colors.goldSoft.copy(alpha = 0.82f),
                modifier = Modifier.size(spec.size.iconLarge - spec.spacing.xSmall),
            )

            Spacer(Modifier.width(spec.spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.scene_control_countdown_clock),
                    color = spec.colors.textSecondary.copy(alpha = 0.78f),
                    fontSize = resolvedItemTitleFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(spec.spacing.xSmall / 2))

                Text(
                    text = stringResource(R.string.scene_control_countdown_clock_desc),
                    color = spec.colors.textMuted.copy(alpha = 0.6f),
                    fontSize = resolvedValueFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            VelarisSwitch(
                checked = showCountdownClock,
                onCheckedChange = onShowCountdownClockChange,
            )
        }

        Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = spec.colors.goldSoft.copy(alpha = 0.82f),
                modifier = Modifier.size(spec.size.iconLarge - spec.spacing.xSmall),
            )

            Spacer(Modifier.width(spec.spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.scene_control_alarm_reminder),
                    color = spec.colors.textSecondary.copy(alpha = 0.78f),
                    fontSize = resolvedItemTitleFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(spec.spacing.xSmall / 2))

                Text(
                    text = stringResource(R.string.scene_control_alarm_reminder_desc),
                    color = spec.colors.textMuted.copy(alpha = 0.6f),
                    fontSize = resolvedValueFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            VelarisSwitch(
                checked = alarmReminderEnabled && timerMode == SceneTimerMode.Countdown,
                onCheckedChange = onAlarmReminderChange,
                enabled = timerMode == SceneTimerMode.Countdown,
            )
        }
    }
}

@LandscapePreviews
@Composable
private fun SceneTimePanelPreview() {
    val spec = VelarisTheme.spec
    var selected by remember { mutableStateOf(1) }
    var showClock by remember { mutableStateOf(true) }
    var alarmReminder by remember { mutableStateOf(false) }

    SceneTimePanel(
        timerMode = SceneTimerMode.Countdown,
        selectedIndex = selected,
        onSelectedChange = { selected = it },
        showCountdownClock = showClock,
        onShowCountdownClockChange = { showClock = it },
        alarmReminderEnabled = alarmReminder,
        onAlarmReminderChange = { alarmReminder = it },
        minWidth = 280.dp,
        minHeight = 172.dp,
        maxHeight = 208.dp,
        titleFontSize = spec.typography.body,
        itemTitleFontSize = spec.typography.label,
        valueFontSize = spec.typography.label,
        modifier = Modifier.width(300.dp),
    )
}
