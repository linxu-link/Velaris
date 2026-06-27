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
package com.wujia.foundation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.button.GlowCircleIconButton
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Preview
@Composable
private fun PreviewTimerCard() {
    TimeCard(
        modifier = Modifier,
    )
}

@Composable
fun TimeCard(
    modifier: Modifier = Modifier,
    timeText: String = "45:00",
    onCardClick: () -> Unit = {},
    title: String = stringResource(R.string.timer_close),
    showTitle: Boolean = true,
    icon: ImageVector = Icons.Outlined.Timer,
    minWidth: Dp = 156.dp,
    minHeight: Dp = 68.dp,
    controlSize: Dp = 48.dp,
) {
    val spec = VelarisTheme.spec
    val cardShape = RoundedCornerShape(spec.radii.card)
    Row(
        modifier = modifier
            .widthIn(min = minWidth)
            .heightIn(min = minHeight)
            .clip(cardShape)
            .velarisGlassBlur(shape = cardShape, blurRadius = spec.blur.panel)
            .background(spec.colors.controlSurface)
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
                shape = cardShape,
            )
            .velarisClickable { onCardClick() }
            .padding(
                start = spec.spacing.small,
                top = spec.spacing.xSmall / 2,
                end = spec.spacing.medium,
                bottom = spec.spacing.xSmall / 2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlowCircleIconButton(
            selected = false,
            icon = icon,
            size = controlSize,
            onSelectedChange = { onCardClick() },
            contentDescription = stringResource(R.string.common_pause),
        )

        Spacer(modifier = Modifier.width(spec.spacing.medium))

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = if (showTitle) Arrangement.Center else Arrangement.Center,
        ) {
            if (showTitle) {
                Text(
                    text = title,
                    color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                    fontSize = spec.typography.label,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }

            Text(
                text = timeText,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = spec.typography.controlValue,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
            )
        }

        Spacer(modifier = Modifier.width(spec.spacing.medium))
    }
}
