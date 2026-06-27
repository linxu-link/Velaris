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
package com.wujia.foundation.designsystem.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

enum class VerticalTurnDirection { TopToBottom, BottomToTop }

@Composable
fun VerticalPageTurnLayer(
    active: Boolean,
    direction: VerticalTurnDirection,
    modifier: Modifier = Modifier,
    animationDurationMillis: Int = 600,
    onFinished: () -> Unit,
    oldContent: @Composable BoxScope.() -> Unit,
    newContent: @Composable BoxScope.() -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(active, direction) {
        if (!active) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDurationMillis,
                easing = FastOutSlowInEasing,
            ),
        )
        onFinished()
    }

    val p = progress.value
    val dirSign = if (direction == VerticalTurnDirection.TopToBottom) 1 else -1

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { containerSize = it },
    ) {
        // Layer 1: New content (base layer)
        Box(modifier = Modifier.fillMaxSize()) {
            newContent()
        }

        // Layer 2: Under page — revealed with scale/alpha/edge effects
        if (p > 0f) {
            VerticalUnderPage(
                progress = p,
                dirSign = dirSign,
                content = newContent,
            )
        }

        // Layer 3: Turning page — old content clipped + 3D rotation
        VerticalTurningPage(
            progress = p,
            dirSign = dirSign,
            content = oldContent,
        )

        // Layer 4: Feather overlay at the turn seam
        if (p > 0f) {
            VerticalRevealFeather(
                progress = p,
                dirSign = dirSign,
                containerHeight = containerSize.height.toFloat(),
            )
        }
    }
}

@Composable
private fun BoxScope.VerticalUnderPage(progress: Float, dirSign: Int, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
                scaleX = 0.98f + 0.02f * progress
                scaleY = 0.98f + 0.02f * progress
                alpha = 0.84f + 0.16f * progress
            },
    ) {
        content()
        // Dark overlay that fades as progress increases
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.16f * (1f - progress))),
        )
        // Edge gradient at the reveal edge
        val edgeAlignment = if (dirSign > 0) Alignment.BottomCenter else Alignment.TopCenter
        Box(
            modifier = Modifier
                .align(edgeAlignment)
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        if (dirSign < 0) {
                            listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent)
                        } else {
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f))
                        },
                    ),
                ),
        )
    }
}

@Composable
private fun BoxScope.VerticalTurningPage(progress: Float, dirSign: Int, content: @Composable BoxScope.() -> Unit) {
    val pivot = if (dirSign > 0) 0f else 1f

    Box(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
                clip = true
                shape = VerticalRevealShape(
                    dirSign = dirSign,
                    revealFraction = progress,
                )
                transformOrigin = TransformOrigin(0.5f, pivot)
                rotationX = -dirSign * progress * 7f
                cameraDistance = 18f * density
                shadowElevation = progress * 18f
            },
    ) {
        content()

        // Dark overlay that darkens as page turns
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = progress * 0.18f)),
        )
    }
}

@Composable
private fun BoxScope.VerticalRevealFeather(progress: Float, dirSign: Int, containerHeight: Float) {
    val height = containerHeight.coerceAtLeast(1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .graphicsLayer {
                val edgeY = if (dirSign > 0) {
                    height * progress
                } else {
                    height * (1f - progress)
                }
                translationY = edgeY - size.height / 2f
                alpha = (0.45f + progress * 0.55f).coerceIn(0f, 1f)
            }
            .background(
                Brush.verticalGradient(
                    colorStops = verticalRevealFeatherStops(),
                ),
            ),
    )
}

private fun verticalRevealFeatherStops(): Array<Pair<Float, Color>> {
    val clear = Color.Transparent
    val shade = Color.Black.copy(alpha = 0.30f)
    val highlight = Color.White.copy(alpha = 0.12f)
    return arrayOf(
        0.00f to clear,
        0.28f to shade,
        0.50f to highlight,
        0.72f to shade,
        1.00f to clear,
    )
}

/**
 * Clips the visible area along the Y axis based on the reveal fraction.
 *
 * - `dirSign > 0` (TopToBottom): turning page's top peels away; visible rect
 *   shrinks from the top: `top = 0, bottom = height * (1 - fraction)`.
 * - `dirSign < 0` (BottomToTop): turning page's bottom peels away; visible rect
 *   shrinks from the bottom: `top = height * fraction, bottom = height`.
 */
private class VerticalRevealShape(private val dirSign: Int, private val revealFraction: Float) :
    androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): androidx.compose.ui.graphics.Outline {
        val fraction = revealFraction.coerceIn(0f, 1f)
        val h = size.height
        val w = size.width
        val top: Float
        val bottom: Float
        if (dirSign > 0) {
            top = 0f
            bottom = h * (1f - fraction)
        } else {
            top = h * fraction
            bottom = h
        }
        return androidx.compose.ui.graphics.Outline.Rectangle(
            androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = top,
                right = w,
                bottom = bottom,
            ),
        )
    }
}
