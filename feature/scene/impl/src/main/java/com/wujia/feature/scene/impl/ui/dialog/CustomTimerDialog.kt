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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.wujia.feature.scene.impl.ui.component.rememberSystemClockDisplay
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.VelarisDialogPanel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CustomTimerDialog(
    startOnClockPage: Boolean = false,
    onConfirm: (hours: Int, minutes: Int) -> Unit,
    onClockConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(45) }
    var page by remember(startOnClockPage) {
        mutableStateOf(
            if (startOnClockPage) {
                TimerDialogPage.Clock
            } else {
                TimerDialogPage.Countdown
            },
        )
    }

    val spec = VelarisTheme.spec
    VelarisDialogPanel(
        modifier = Modifier.widthIn(max = spec.size.dialogWidth),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DialogHeader(
                title = stringResource(
                    if (page == TimerDialogPage.Countdown) {
                        R.string.timer_custom_time
                    } else {
                        R.string.timer_clock
                    },
                ),
                page = page,
                onPageChange = { page = it },
            )

            Spacer(Modifier.height(spec.spacing.xLarge))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    if (targetState == TimerDialogPage.Clock) {
                        slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { it },
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(260),
                            targetOffsetX = { -it },
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { -it },
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(260),
                            targetOffsetX = { it },
                        )
                    }
                },
                label = "TimerModeContent",
            ) {
                when (it) {
                    TimerDialogPage.Countdown -> CountdownTimerContent(
                        hours = hours,
                        minutes = minutes,
                        onHoursChange = { value -> hours = value },
                        onMinutesChange = { value -> minutes = value },
                    )

                    TimerDialogPage.Clock -> ClockTimerContent()
                }
            }

            Spacer(Modifier.height(spec.spacing.xLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = spec.colors.textSecondary.copy(alpha = 0.68f),
                        fontSize = spec.typography.label,
                    )
                }

                Spacer(Modifier.width(spec.spacing.small))

                TextButton(
                    onClick = {
                        if (page == TimerDialogPage.Clock) {
                            onClockConfirm()
                        } else {
                            onConfirm(hours, minutes)
                        }
                    },
                    enabled = page == TimerDialogPage.Clock || hours > 0 || minutes > 0,
                ) {
                    val enabled = page == TimerDialogPage.Clock || hours > 0 || minutes > 0
                    Text(
                        text = stringResource(R.string.common_confirm),
                        color = if (enabled) {
                            spec.colors.goldBright
                        } else {
                            spec.colors.textSecondary.copy(alpha = 0.38f)
                        },
                        fontSize = spec.typography.label,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private enum class TimerDialogPage {
    Countdown,
    Clock,
}

@Composable
private fun DialogHeader(title: String, page: TimerDialogPage, onPageChange: (TimerDialogPage) -> Unit) {
    val spec = VelarisTheme.spec
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.size(spec.size.iconLarge))
        Text(
            text = title,
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.subtitle,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                onPageChange(
                    if (page == TimerDialogPage.Countdown) {
                        TimerDialogPage.Clock
                    } else {
                        TimerDialogPage.Countdown
                    },
                )
            },
            modifier = Modifier.size(spec.size.iconLarge),
        ) {
            Icon(
                imageVector = if (page == TimerDialogPage.Countdown) {
                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                } else {
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                },
                contentDescription = null,
                tint = spec.colors.goldBright.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun CountdownTimerContent(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
) {
    val spec = VelarisTheme.spec
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeColumn(
            label = stringResource(R.string.timer_hour),
            value = hours,
            onIncrement = { onHoursChange((hours + 1) % 24) },
            onDecrement = { onHoursChange((hours - 1 + 24) % 24) },
        )

        Text(
            text = ":",
            color = spec.colors.goldBright,
            fontSize = spec.typography.valueLarge,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = spec.spacing.small),
        )

        TimeColumn(
            label = stringResource(R.string.timer_minute),
            value = minutes,
            onIncrement = { onMinutesChange((minutes + 5) % 60) },
            onDecrement = { onMinutesChange((minutes - 5 + 60) % 60) },
        )
    }
}

@Composable
private fun ClockTimerContent() {
    val spec = VelarisTheme.spec
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val clock = rememberSystemClockDisplay(now)
    Text(
        text = clock.text,
        color = spec.colors.textPrimary,
        fontSize = spec.typography.display,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .background(spec.colors.controlSurface.copy(alpha = 0.5f))
            .padding(
                horizontal = spec.spacing.large,
                vertical = spec.spacing.medium,
            ),
    )
}

@Composable
private fun TimeColumn(label: String, value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val spec = VelarisTheme.spec
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrement, modifier = Modifier.size(spec.size.iconXLarge)) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = spec.colors.goldBright.copy(alpha = 0.7f),
            )
        }

        Text(
            text = "%02d".format(value),
            color = spec.colors.textPrimary,
            fontSize = spec.typography.display,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(spec.size.controlCompact + spec.spacing.large)
                .clip(RoundedCornerShape(spec.radii.thumbnail))
                .background(spec.colors.controlSurface.copy(alpha = 0.5f))
                .padding(vertical = spec.spacing.small),
        )

        IconButton(onClick = onDecrement, modifier = Modifier.size(spec.size.iconXLarge)) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = spec.colors.goldBright.copy(alpha = 0.7f),
            )
        }

        Text(
            text = label,
            color = spec.colors.textSecondary.copy(alpha = 0.5f),
            fontSize = spec.typography.bodySmall,
        )
    }
}

@LandscapePreviews
@Composable
private fun CustomTimerDialogPreview() {
    CustomTimerDialog(
        onConfirm = { _, _ -> },
        onClockConfirm = {},
        onDismissRequest = {},
    )
}
