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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import kotlin.math.roundToInt

/**
 * 带倒计时圆环的圆形按钮。
 *
 * 复用 [GlowCircleIconButton] 的发光 + 磨砂玻璃结构，
 * 在内圆上叠加一层 Canvas 绘制的进度圆环。
 *
 * @param isRunning 倒计时是否正在运行（影响发光强度）
 * @param progress 倒计时进度，1f = 满，0f = 归零
 * @param onToggleClick 点击回调
 * @param icon 中心图标
 * @param size 整体直径
 */
@Composable
fun TimerCircleButton(
    isRunning: Boolean,
    progress: Float,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Timer,
    size: Dp = 78.dp,
    contentDescription: String? = "定时器",
) {
    val spec = VelarisTheme.spec

    val glowAlpha by animateFloatAsState(
        targetValue = if (isRunning) spec.alpha.glowActive else spec.alpha.glowIdle,
        animationSpec = tween(220),
        label = "glowAlpha",
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "progress",
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
                stateDescription = if (isRunning) {
                    "剩余 ${(progress * 100).roundToInt()}%"
                } else {
                    "已停止"
                }
            }
            .velarisClickable { onToggleClick() },
        contentAlignment = Alignment.Center,
    ) {
        // 外层发光
        val sizePx = with(LocalDensity.current) { size.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to spec.colors.gold.copy(alpha = glowAlpha),
                            0.28f to spec.colors.gold.copy(alpha = glowAlpha * 0.7f),
                            0.58f to spec.colors.gold.copy(alpha = glowAlpha * 0.12f),
                            0.82f to spec.colors.gold.copy(alpha = glowAlpha * 0.04f),
                            1.0f to Color.Transparent,
                        ),
                        radius = sizePx * spec.glow.circleRadiusScale,
                    ),
                    shape = CircleShape,
                ),
        )

        // 内层磨砂玻璃圆 + 进度圆环
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(CircleShape)
                .velarisGlassBlur(
                    shape = CircleShape,
                    blurRadius = spec.blur.button,
                )
                .background(spec.colors.controlSurface),
            contentAlignment = Alignment.Center,
        ) {
            // Canvas 绘制圆环进度
            val trackColor = spec.colors.gold.copy(alpha = 0.12f)
            val progressColor = spec.colors.goldBright
            val strokeWidth = spec.size.stroke * 2.2f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = strokeWidth.toPx()
                val canvasWidth = drawContext.size.width
                val arcSize = canvasWidth - stroke
                val topLeft = Offset(stroke / 2f, stroke / 2f)

                // 背景轨道
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )

                // 进度弧
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            // 中心图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = spec.colors.goldBright,
                modifier = Modifier.size(size * 0.30f),
            )
        }
    }
}

@Preview
@Composable
private fun TimerCircleButtonPreview() {
    TimerCircleButton(
        isRunning = true,
        progress = 0.65f,
        onToggleClick = {},
    )
}
