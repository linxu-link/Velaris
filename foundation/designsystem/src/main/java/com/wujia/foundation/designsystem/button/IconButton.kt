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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Composable
fun GlowCircleIconButton(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Pause,
    size: Dp = 78.dp,
    contentDescription: String? = null,
) {
    val spec = VelarisTheme.spec
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) spec.alpha.glowActive else spec.alpha.glowIdle,
        animationSpec = tween(220),
        label = "glowAlpha",
    )

    val borderColor by animateColorAsState(
        targetValue = spec.colors.gold.copy(alpha = spec.alpha.strokeStrong),
        label = "borderColor",
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            }
            .velarisClickable {
                onSelectedChange(!selected)
            },
        contentAlignment = Alignment.Center,
    ) {
        val sizePx = with(LocalDensity.current) { size.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to spec.colors.gold.copy(alpha = glowAlpha),
                            0.28f to spec.colors.gold.copy(alpha = glowAlpha * 0.7f),
                            0.58f to spec.colors.gold.copy(alpha = glowAlpha * 0.5f),
                            0.82f to spec.colors.gold.copy(alpha = glowAlpha * 0.04f),
                            1.0f to Color.Transparent,
                        ),
                        radius = sizePx * spec.glow.circleRadiusScale,
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(CircleShape)
                .velarisGlassBlur(
                    shape = CircleShape,
                    blurRadius = spec.blur.button,
                )
                .background(spec.colors.controlSurface)
                .border(
                    width = spec.size.stroke,
                    color = borderColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // parent Box provides the semantics
                tint = spec.colors.goldBright,
                modifier = Modifier.size(size * 0.34f),
            )
        }
    }
}

@Preview()
@Composable
fun IconButtonDemo() {
    var playing by remember { mutableStateOf(false) }

    GlowCircleIconButton(
        selected = playing,
        onSelectedChange = { playing = it },
    )
}
