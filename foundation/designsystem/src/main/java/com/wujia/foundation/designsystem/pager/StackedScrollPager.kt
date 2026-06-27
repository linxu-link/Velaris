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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisFontSize
import kotlin.math.abs

private const val PAGE_TURN_THRESHOLD = 0.35f
private const val FLING_VELOCITY_THRESHOLD = 900f
private const val EDGE_RESISTANCE = 0.32f

@Composable
fun StackedScrollPager(
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    animationDurationMillis: Int = 320,
    content: @Composable BoxScope.(page: Int) -> Unit,
) {
    val coercedPage = currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    val offsetX = remember { Animatable(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var rawOffset by remember { mutableStateOf(0f) }
    var flingTrigger by remember { mutableIntStateOf(0) }
    var flingTarget by remember { mutableStateOf(0f) }
    var pageToChange by remember { mutableStateOf(-1) }

    LaunchedEffect(coercedPage, pageCount) {
        offsetX.snapTo(0f)
    }

    // 拖拽跟手：将 rawOffset 同步到 Animatable
    LaunchedEffect(rawOffset) {
        offsetX.snapTo(rawOffset)
    }

    // 回弹/翻页动画
    LaunchedEffect(flingTrigger) {
        if (flingTrigger == 0) return@LaunchedEffect
        if (flingTarget != offsetX.value) {
            offsetX.animateTo(
                targetValue = flingTarget,
                animationSpec = tween(
                    durationMillis = animationDurationMillis,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        if (pageToChange >= 0) {
            onPageChange(pageToChange)
            pageToChange = -1
        }
        rawOffset = 0f
        offsetX.snapTo(0f)
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { containerSize = it }
            .stackedPagerDrag(
                enabled = enabled && pageCount > 1 && containerSize.width > 0,
                currentPage = coercedPage,
                pageCount = pageCount,
                width = containerSize.width.toFloat(),
                onDragOffsetChange = { rawOffset = it },
                onFlingRequest = { finalOffset, target, page ->
                    rawOffset = finalOffset
                    flingTarget = target
                    pageToChange = page
                    flingTrigger++
                },
            ),
    ) {
        if (pageCount <= 0) {
            return@Box
        }

        val width = containerSize.width.toFloat().coerceAtLeast(1f)
        val rawOffset = offsetX.value
        val direction = rawOffset.turnDirection()
        val targetPage = (coercedPage + direction).takeIf { it in 0 until pageCount }
        val progress = (abs(rawOffset) / width).coerceIn(0f, 1f)

        if (targetPage != null && progress > 0f) {
            StackedUnderPage(
                progress = progress,
                direction = direction,
                content = { content(targetPage) },
            )
        }

        TurningPage(
            offsetX = rawOffset,
            progress = progress,
            direction = direction,
            width = width,
            content = { content(coercedPage) },
        )

        if (targetPage != null && progress > 0f) {
            RevealFeatherOverlay(
                revealWidth = width - abs(rawOffset),
                containerWidth = width,
                progress = progress,
                direction = direction,
            )
        }
    }
}

private fun Modifier.stackedPagerDrag(
    enabled: Boolean,
    currentPage: Int,
    pageCount: Int,
    width: Float,
    onDragOffsetChange: (Float) -> Unit,
    onFlingRequest: (finalOffset: Float, target: Float, page: Int) -> Unit,
): Modifier {
    if (!enabled) return this

    return pointerInput(currentPage, pageCount, width) {
        val touchSlop = viewConfiguration.touchSlop

        forEachGesture {
            awaitPointerEventScope {
                val down = awaitFirstDown(requireUnconsumed = false)
                val velocityTracker = VelocityTracker()
                var accumulated = Offset.Zero
                var dragOffset = 0f
                var dragging = false

                velocityTracker.addPosition(down.uptimeMillis, down.position)

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.pressed }
                        ?: break

                    velocityTracker.addPosition(change.uptimeMillis, change.position)

                    val delta = change.positionChange()
                    if (!dragging) {
                        accumulated += delta
                        if (abs(accumulated.x) > touchSlop || abs(accumulated.y) > touchSlop) {
                            if (abs(accumulated.x) <= abs(accumulated.y)) break
                            dragging = true
                            dragOffset = accumulated.x
                            change.consume()
                            onDragOffsetChange(
                                resistedPagerOffset(currentPage, pageCount, dragOffset),
                            )
                        }
                    } else {
                        dragOffset += delta.x
                        change.consume()
                        onDragOffsetChange(
                            resistedPagerOffset(currentPage, pageCount, dragOffset),
                        )
                    }
                }

                if (dragging) {
                    val velocityX = velocityTracker.calculateVelocity().x
                    val finalOffset = resistedPagerOffset(
                        currentPage = currentPage,
                        pageCount = pageCount,
                        rawOffset = dragOffset,
                    )
                    val targetPage = resolveTargetPage(
                        currentPage = currentPage,
                        pageCount = pageCount,
                        width = width,
                        offset = finalOffset,
                        velocityX = velocityX,
                    )
                    val flingTarget = if (targetPage == currentPage) {
                        0f
                    } else if (targetPage > currentPage) {
                        -width
                    } else {
                        width
                    }
                    val page = if (targetPage != currentPage) targetPage else -1
                    onFlingRequest(finalOffset, flingTarget, page)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.StackedUnderPage(progress: Float, direction: Int, content: @Composable BoxScope.() -> Unit) {
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.16f * (1f - progress))),
        )
        val edgeAlignment = if (direction > 0) Alignment.CenterEnd else Alignment.CenterStart
        Box(
            modifier = Modifier
                .align(edgeAlignment)
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        if (direction < 0) {
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
private fun BoxScope.TurningPage(
    offsetX: Float,
    progress: Float,
    direction: Int,
    width: Float,
    content: @Composable BoxScope.() -> Unit,
) {
    val revealWidth = (width - abs(offsetX)).coerceIn(0f, width)
    val pivot = if (direction > 0) 0f else 1f

    Box(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
                clip = true
                shape = PagerRevealShape(
                    direction = direction,
                    revealWidth = revealWidth,
                )
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(pivot, 0.5f)
                rotationY = -direction * progress * 7f
                cameraDistance = 18f * density
                shadowElevation = progress * 18f
            },
    ) {
        content()

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = progress * 0.18f)),
        )
    }
}

@Composable
private fun BoxScope.RevealFeatherOverlay(revealWidth: Float, containerWidth: Float, progress: Float, direction: Int) {
    val edgeX = if (direction > 0) {
        revealWidth
    } else {
        containerWidth - revealWidth
    }

    Box(
        modifier = Modifier
            .width(112.dp)
            .fillMaxHeight()
            .graphicsLayer {
                translationX = edgeX - size.width / 2f
                alpha = (0.45f + progress * 0.55f).coerceIn(0f, 1f)
            }
            .background(
                Brush.horizontalGradient(
                    colorStops = pagerRevealFeatherStops(),
                ),
            ),
    )
}

private fun resistedPagerOffset(currentPage: Int, pageCount: Int, rawOffset: Float): Float {
    val canMoveForward = currentPage < pageCount - 1
    val canMoveBack = currentPage > 0
    return when {
        rawOffset < 0f && !canMoveForward -> rawOffset * EDGE_RESISTANCE
        rawOffset > 0f && !canMoveBack -> rawOffset * EDGE_RESISTANCE
        else -> rawOffset
    }
}

private fun resolveTargetPage(currentPage: Int, pageCount: Int, width: Float, offset: Float, velocityX: Float): Int {
    val progress = abs(offset) / width.coerceAtLeast(1f)
    val wantsNext = offset < 0f &&
        (progress >= PAGE_TURN_THRESHOLD || velocityX <= -FLING_VELOCITY_THRESHOLD)
    val wantsPrevious = offset > 0f &&
        (progress >= PAGE_TURN_THRESHOLD || velocityX >= FLING_VELOCITY_THRESHOLD)

    return when {
        wantsNext && currentPage < pageCount - 1 -> currentPage + 1
        wantsPrevious && currentPage > 0 -> currentPage - 1
        else -> currentPage
    }
}

private fun Float.turnDirection(): Int = when {
    this < 0f -> 1
    this > 0f -> -1
    else -> 0
}

private fun pagerRevealFeatherStops(): Array<Pair<Float, Color>> {
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

private class PagerRevealShape(private val direction: Int, private val revealWidth: Float) :
    androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): androidx.compose.ui.graphics.Outline {
        val width = revealWidth.coerceIn(0f, size.width)
        val left = if (direction > 0) 0f else size.width - width
        val right = if (direction > 0) width else size.width
        return androidx.compose.ui.graphics.Outline.Rectangle(
            androidx.compose.ui.geometry.Rect(
                left = left,
                top = 0f,
                right = right,
                bottom = size.height,
            ),
        )
    }
}

@LandscapePreviews
@Composable
private fun StackedScrollPagerPreview() {
    var page by remember { mutableIntStateOf(1) }
    val pages = listOf(
        PreviewPage("Morning", VelarisColor.PreviewGreenDark, VelarisColor.Gold),
        PreviewPage("Evening", VelarisColor.NoiseTealDark, VelarisColor.GoldSoft),
        PreviewPage("Night", VelarisColor.MaterialSlateDark, VelarisColor.GoldBright),
    )

    StackedScrollPager(
        currentPage = page,
        pageCount = pages.size,
        onPageChange = { page = it },
        modifier = Modifier.fillMaxSize(),
    ) { index ->
        val item = pages[index]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            item.background,
                            item.accent.copy(alpha = 0.54f),
                            VelarisColor.BgGradientDarkSlate,
                        ),
                    ),
                )
                .padding(48.dp),
        ) {
            Text(
                text = item.title,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = VelarisFontSize.PreviewTitle,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(84.dp)
                    .background(
                        color = item.accent.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                    ),
            )
        }
    }
}

private data class PreviewPage(val title: String, val background: Color, val accent: Color)
