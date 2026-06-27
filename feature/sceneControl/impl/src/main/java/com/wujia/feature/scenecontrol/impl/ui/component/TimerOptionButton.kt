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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Composable
fun TimerOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val shape = RoundedCornerShape(spec.radii.pill)
    val border: Color = animateColorAsState(
        targetValue = if (selected) {
            spec.colors.goldSoft.copy(alpha = 0.5f)
        } else {
            spec.colors.stroke.copy(alpha = 0.08f)
        },
        label = "optionBorder",
    ).value

    val textColor: Color = animateColorAsState(
        targetValue = if (selected) {
            spec.colors.textPrimary.copy(alpha = 0.92f)
        } else {
            spec.colors.textSecondary.copy(alpha = 0.68f)
        },
        label = "optionText",
    ).value

    Box(
        modifier = modifier
            .height(spec.size.controlSmall)
            .clip(shape)
            .velarisGlassBlur(
                shape = shape,
                blurRadius = if (selected) spec.blur.button * 2 else spec.blur.button,
                surfaceAlpha = if (selected) 0.2f else 0.0f,
            )
            .border(spec.size.stroke, border, shape)
            .velarisClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = spec.spacing.xSmall),
        )
    }
}
