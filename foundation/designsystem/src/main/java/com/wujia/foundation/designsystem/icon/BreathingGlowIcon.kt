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
package com.wujia.foundation.designsystem.icon

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
fun BreathingGlowIcon(
    imageVector: ImageVector,
    selected: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    selectedTint: Color = VelarisTheme.spec.colors.goldSoft,
    unselectedTint: Color = VelarisTheme.spec.colors.iconMuted,
    glowColor: Color = VelarisTheme.spec.colors.gold,
    iconSize: Dp = 18.dp,
    glowSize: Dp = 36.dp,
) {
    val transition = rememberInfiniteTransition(label = "glowTransition")
    val spec = VelarisTheme.spec

    val glowAlpha by transition.animateFloat(
        initialValue = spec.glow.iconMinAlpha,
        targetValue = spec.glow.iconMaxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) selectedTint else unselectedTint,
        label = "iconTint",
    )

    Box(
        modifier = modifier.size(glowSize),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = glowAlpha),
                                glowColor.copy(alpha = glowAlpha * 0.3f),
                                glowColor.copy(alpha = glowAlpha * 0.1f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
        }

        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@LandscapePreviews
@Composable
private fun BreathingGlowIconPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BreathingGlowIcon(
            imageVector = Icons.Outlined.Alarm,
            selected = false,
            contentDescription = "未选中闹钟图标",
        )
        BreathingGlowIcon(
            imageVector = Icons.Outlined.Notifications,
            selected = true,
            contentDescription = "选中提醒图标",
        )
    }
}
