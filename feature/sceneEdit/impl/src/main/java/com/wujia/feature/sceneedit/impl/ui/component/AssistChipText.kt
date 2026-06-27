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
package com.wujia.feature.sceneedit.impl.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
fun AssistChipText(
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val spec = VelarisTheme.spec
    Text(
        text = text,
        color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
        fontSize = spec.typography.micro,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .clip(RoundedCornerShape(spec.radii.badge))
            .border(
                width = 1.dp,
                color = spec.colors.textMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(spec.radii.badge),
            )
            .padding(horizontal = spec.spacing.medium),
    )
}
