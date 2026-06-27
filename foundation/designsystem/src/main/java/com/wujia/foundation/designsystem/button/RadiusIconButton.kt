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
package com.wujia.foundation.designsystem.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.icon.BreathingGlowIcon
import com.wujia.foundation.designsystem.tab.SceneTabItem
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Composable
fun RadiusIconButton(
    modifier: Modifier = Modifier,
    item: SceneTabItem,
    cornerRadius: Dp,
    contentPadding: PaddingValues,
    selectedBrush: Brush = VelarisTheme.spec.brushes.selectedPill,
) {
    val spec = VelarisTheme.spec
    val shape = RoundedCornerShape(cornerRadius)

    val textColor: Color = animateColorAsState(
        spec.colors.textPrimary.copy(alpha = 0.92f),
    ).value

    Box(
        modifier = modifier
            .clip(shape)
            .velarisGlassBlur(
                shape = shape,
                blurRadius = spec.blur.button,
            )
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
                shape = shape,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.text,
                color = textColor,
                fontSize = spec.typography.tab,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )

            item.icon?.let {
                BreathingGlowIcon(
                    imageVector = it,
                    selected = true,
                    contentDescription = item.text,
                    selectedTint = textColor,
                    unselectedTint = textColor,
                    iconSize = spec.size.iconSmall,
                    glowSize = spec.size.iconSmall + 6.dp,
                )

                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Preview
@Composable
private fun RadiusIconButtonPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadiusIconButton(
            item = SceneTabItem(
                text = "收藏场景",
                icon = Icons.Outlined.FavoriteBorder,
            ),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(36.dp),
        )

        RadiusIconButton(
            item = SceneTabItem(
                text = "收藏",
                icon = Icons.Outlined.FavoriteBorder,
            ),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(36.dp),
        )
    }
}
