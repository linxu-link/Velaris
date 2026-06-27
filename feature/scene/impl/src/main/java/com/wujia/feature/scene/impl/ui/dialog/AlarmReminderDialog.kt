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
package com.wujia.feature.scene.impl.ui.dialog

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.VelarisDialogPanel

@Composable
internal fun AlarmReminderDialog(onConfirm: () -> Unit) {
    val spec = VelarisTheme.spec
    val shakeTransition = rememberInfiniteTransition(label = "alarm_shake")
    val shakeOffsetX by shakeTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake_offset_x",
    )
    VelarisDialogPanel(
        modifier = Modifier.widthIn(max = spec.size.dialogWidth),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = null,
                tint = spec.colors.goldBright,
                modifier = Modifier
                    .size(spec.size.iconXLarge)
                    .offset(x = shakeOffsetX.dp),
            )

            Spacer(Modifier.height(spec.spacing.medium))

            Text(
                text = stringResource(R.string.alarm_reminder_title),
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = spec.typography.subtitle,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(spec.spacing.medium))

            Text(
                text = stringResource(R.string.alarm_reminder_desc),
                color = spec.colors.textSecondary.copy(alpha = 0.78f),
                fontSize = spec.typography.body,
            )

            Spacer(Modifier.height(spec.spacing.xLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = stringResource(R.string.alarm_reminder_confirm),
                        color = spec.colors.goldBright,
                        fontSize = spec.typography.label,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun AlarmReminderDialogPreview() {
    AlarmReminderDialog(
        onConfirm = {},
    )
}
