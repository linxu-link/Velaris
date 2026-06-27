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

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Paint as AndroidPaint

fun Modifier.selectedBorderGlow(
    selected: Boolean,
    cornerRadius: Dp,
    selectedColor: Color,
    unselectedColor: Color,
    borderWidth: Dp = 1.dp,
    glowStrokeWidth: Dp = 5.dp,
    glowBlurRadius: Dp = 7.dp,
): Modifier = drawWithContent {
    drawContent()

    val radius = cornerRadius.toPx()
    if (selected) {
        val glowStroke = glowStrokeWidth.toPx()
        val inset = glowStroke / 2f
        val adjustedRadius = (radius - inset).coerceAtLeast(0f)

        drawIntoCanvas { canvas ->
            val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                color = selectedColor.copy(alpha = 0.58f).toArgb()
                style = AndroidPaint.Style.STROKE
                strokeWidth = glowStroke
                maskFilter = BlurMaskFilter(glowBlurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
            }

            canvas.nativeCanvas.drawRoundRect(
                inset,
                inset,
                size.width - inset,
                size.height - inset,
                adjustedRadius,
                adjustedRadius,
                paint,
            )
        }
    }

    drawRoundRect(
        color = if (selected) selectedColor.copy(alpha = 0.78f) else unselectedColor,
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = borderWidth.toPx()),
    )
}
