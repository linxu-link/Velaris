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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
private fun Preview_DefaultPlayerCard() {
    // Preview using default values
    PlayerCard(
        modifier = Modifier,
    )
}

@Composable
fun PlayerCard(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onIconClick: () -> Unit = {},
    onTextClick: () -> Unit = {},
    title: String = stringResource(R.string.player_mix),
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
            .velarisGlassBlur(
                shape = cardShape,
                blurRadius = spec.blur.panel,
            )
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = 0.42f),
                shape = cardShape,
            )
            .velarisClickable { onTextClick() }
            .padding(
                start = spec.spacing.small,
                top = spec.spacing.xSmall / 2,
                end = spec.spacing.medium,
                bottom = spec.spacing.xSmall / 2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlowCircleIconButton(
            selected = isPlaying,
            icon = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
            size = controlSize,
            onSelectedChange = { onIconClick() },
            contentDescription = if (isPlaying) {
                stringResource(
                    R.string.common_pause,
                )
            } else {
                stringResource(R.string.common_play)
            },
        )

        Spacer(modifier = Modifier.width(spec.spacing.medium))

        Text(
            text = title,
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.label,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
        )

        Spacer(modifier = Modifier.width(spec.spacing.medium))

        Icon(
            imageVector = Icons.Outlined.Tune,
            contentDescription = null,
            tint = spec.colors.goldBright.copy(alpha = spec.alpha.icon),
            modifier = Modifier.size(spec.size.iconMedium).rotate(90f),
        )
    }
}
