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
package com.wujia.foundation.designsystem.bar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.theme.VelarisFontSize
import com.wujia.foundation.designsystem.theme.VelarisTheme
import kotlin.math.roundToInt

@Composable
fun GlowSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    trackHeight: Dp = 6.dp,
    thumbRadius: Dp = 10.dp,
    glowRadius: Dp = 24.dp,
    activeColor: Color = VelarisTheme.spec.colors.gold,
    inactiveColor: Color = VelarisTheme.spec.colors.trackInactive,
    thumbColor: Color = VelarisTheme.spec.colors.gold,
    glowColor: Color = VelarisTheme.spec.colors.gold,
) {
    val spec = VelarisTheme.spec
    var dragging by remember { mutableStateOf(false) }

    val glowScale by animateFloatAsState(
        targetValue = if (dragging) 1.25f else 1f,
        animationSpec = tween(180),
        label = "glowScale",
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (dragging) spec.alpha.seekGlowActive else spec.alpha.seekGlowIdle,
        animationSpec = tween(180),
        label = "glowAlpha",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "trackFlow")

    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flowProgress",
    )

    Canvas(
        modifier = modifier
            .height(glowRadius * 2)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                stateDescription = "${(value * 100).roundToInt()}%"
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = value,
                    range = 0f..1f,
                )
                setProgress { targetValue ->
                    onValueChange(targetValue.coerceIn(0f, 1f))
                    true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val horizontalInset = thumbRadius.toPx()
                    val usableWidth = (size.width - horizontalInset * 2).coerceAtLeast(1f)
                    val newValue = ((offset.x - horizontalInset) / usableWidth).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        val horizontalInset = thumbRadius.toPx()
                        val usableWidth = (size.width - horizontalInset * 2).coerceAtLeast(1f)
                        val newValue = ((offset.x - horizontalInset) / usableWidth).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    },
                    onDragEnd = {
                        dragging = false
                    },
                    onDragCancel = {
                        dragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()

                        val horizontalInset = thumbRadius.toPx()
                        val usableWidth = (size.width - horizontalInset * 2).coerceAtLeast(1f)
                        val newValue = ((change.position.x - horizontalInset) / usableWidth)
                            .coerceIn(0f, 1f)

                        onValueChange(newValue)
                    },
                )
            },
    ) {
        val trackH = trackHeight.toPx()
        val thumbR = thumbRadius.toPx()
        val glowR = glowRadius.toPx() * glowScale

        val centerY = size.height / 2f
        val horizontalInset = thumbR
        val trackStartX = horizontalInset
        val trackEndX = (size.width - horizontalInset).coerceAtLeast(trackStartX)
        val trackWidth = (trackEndX - trackStartX).coerceAtLeast(1f)
        val thumbX = trackStartX + value.coerceIn(0f, 1f) * trackWidth

        drawRoundRect(
            color = inactiveColor,
            topLeft = Offset(trackStartX, centerY - trackH / 2),
            size = Size(trackWidth, trackH),
            cornerRadius = CornerRadius(trackH / 2, trackH / 2),
        )

        if (thumbX > trackStartX) {
            val activeWidth = thumbX - trackStartX
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to activeColor.copy(alpha = 0.55f),
                        0.5f to activeColor.copy(alpha = 0.75f),
                        0.85f to activeColor.copy(alpha = 0.95f),
                        1.0f to activeColor.copy(alpha = 1f),
                    ),
                    startX = trackStartX,
                    endX = thumbX,
                ),
                topLeft = Offset(trackStartX, centerY - trackH / 2),
                size = Size(activeWidth, trackH),
                cornerRadius = CornerRadius(trackH / 2, trackH / 2),
            )

            // 流动高光
            val highlightCenter = trackStartX + activeWidth * flowProgress
            val highlightWidth = trackWidth * 0.18f

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.5f to Color.White.copy(alpha = spec.glow.seekHighlightAlpha),
                        1.0f to Color.Transparent,
                    ),
                    startX = highlightCenter - highlightWidth,
                    endX = highlightCenter + highlightWidth,
                ),
                topLeft = Offset(trackStartX, centerY - trackH / 2),
                size = Size(activeWidth, trackH),
                cornerRadius = CornerRadius(trackH / 2, trackH / 2),
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = glowAlpha),
                    glowColor.copy(alpha = glowAlpha * 0.28f),
                    glowColor.copy(alpha = glowAlpha * 0.06f),
                    Color.Transparent,
                ),
                center = Offset(thumbX, centerY),
                radius = glowR,
            ),
            radius = glowR,
            center = Offset(thumbX, centerY),
        )

        drawCircle(
            color = thumbColor,
            radius = thumbR,
            center = Offset(thumbX, centerY),
        )

//        drawCircle(
//            color = Color.White.copy(alpha = 0.28f),
//            radius = thumbR * 0.55f,
//            center = Offset(
//                x = thumbX - thumbR * 0.25f,
//                y = centerY - thumbR * 0.25f
//            )
//        )
    }
}

@Preview
@Composable
fun GlowSeekBarDemo() {
    var progress by remember { mutableFloatStateOf(0.8f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlowSeekBar(
            value = progress,
            onValueChange = { progress = it },
            modifier = Modifier
                .width(340.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${(progress * 100).roundToInt()}%",
            color = VelarisTheme.spec.colors.textSecondary.copy(alpha = VelarisTheme.spec.alpha.textSecondary),
            fontSize = VelarisFontSize.SeekPercent,
        )
    }
}
