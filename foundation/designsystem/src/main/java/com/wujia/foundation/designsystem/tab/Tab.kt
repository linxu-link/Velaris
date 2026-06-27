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
package com.wujia.foundation.designsystem.tab

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.icon.BreathingGlowIcon
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Composable
fun SceneSegmentedTabs(
    items: List<SceneTabItem>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 42.dp,
    cornerRadius: Dp = 21.dp,
    backgroundColor: Color = VelarisTheme.spec.colors.surfaceSubtle,
    borderColor: Color = VelarisTheme.spec.colors.stroke.copy(alpha = 0.2f),
    selectedBrush: Brush = VelarisTheme.spec.brushes.selectedPill,
    itemPadding: PaddingValues = PaddingValues(horizontal = 14.dp),
) {
    val spec = VelarisTheme.spec
    val containerShape = RoundedCornerShape(cornerRadius)
    require(items.isNotEmpty()) {
        "SceneSegmentedTabs items can not be empty"
    }

    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(containerShape)
            .velarisGlassBlur(
                shape = containerShape,
                blurRadius = spec.blur.button,
            )
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = containerShape,
            ),
    ) {
        val selectedTabWidth = maxWidth / items.size
        val coercedSelectedIndex = selectedIndex.coerceIn(items.indices)
        val compact = selectedTabWidth < 88.dp
        val resolvedItemPadding = if (compact) {
            PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        } else {
            itemPadding
        }
        val selectedTabOffset by animateDpAsState(
            targetValue = selectedTabWidth * coercedSelectedIndex,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "selectedTabOffset",
        )

        Box(
            modifier = Modifier
                .offset(x = selectedTabOffset)
                .width(selectedTabWidth)
                .heightIn(min = minHeight)
                .clip(containerShape)
                .background(selectedBrush),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex

                SceneSegmentedTabItem(
                    item = item,
                    selected = selected,
                    cornerRadius = cornerRadius,
                    contentPadding = resolvedItemPadding,
                    showIcon = !compact,
                    modifier = Modifier
                        .weight(1f)
                        .velarisClickable {
                            onSelectedChange(index)
                        },
                )
            }
        }
    }
}

@Composable
private fun SceneSegmentedTabItem(
    item: SceneTabItem,
    selected: Boolean,
    cornerRadius: Dp,
    contentPadding: PaddingValues,
    showIcon: Boolean,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val selectedTextColor = item.selectedTextColor.takeIf { it != Color.Unspecified } ?: spec.colors.textPrimary
    val unselectedTextColor = item.unselectedTextColor.takeIf { it != Color.Unspecified } ?: spec.colors.iconMuted
    val selectedIconTint = item.selectedIconTint.takeIf { it != Color.Unspecified } ?: spec.colors.gold
    val unselectedIconTint = item.unselectedIconTint.takeIf { it != Color.Unspecified } ?: spec.colors.iconMuted
    val textColor by animateColorAsState(
        targetValue = if (selected) selectedTextColor else unselectedTextColor,
        label = "textColor",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            item.icon?.takeIf { showIcon }?.let {
                BreathingGlowIcon(
                    imageVector = it,
                    selected = selected,
                    contentDescription = item.text,
                    selectedTint = selectedIconTint,
                    unselectedTint = unselectedIconTint,
                    iconSize = spec.size.iconSmall,
                    glowSize = spec.size.iconSmall + 14.dp,
                )

                Spacer(modifier = Modifier.width(3.dp))
            }

            Text(
                text = item.text,
                color = textColor,
                fontSize = spec.typography.tab,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@LandscapePreviews
@Composable
fun SceneTabsDemo() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    SceneSegmentedTabs(
        items = listOf(
            SceneTabItem(
                text = "专注场景",
                icon = Icons.Outlined.EnergySavingsLeaf,
            ),
            SceneTabItem(
                text = "助眠场景",
                icon = Icons.Outlined.DarkMode,
            ),
        ),
        selectedIndex = selectedIndex,
        onSelectedChange = { selectedIndex = it },
        modifier = Modifier.width(280.dp),
    )
}
