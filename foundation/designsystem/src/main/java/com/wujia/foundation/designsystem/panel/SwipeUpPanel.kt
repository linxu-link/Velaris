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
package com.wujia.foundation.designsystem.panel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

private const val SWIPE_UP_PANEL_TAG = "SwipeUpPanel"

@Composable
fun SwipeUpPanel(
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    debugName: String? = null,
    panelHeight: Dp = 380.dp,
    edgePadding: Dp = VelarisTheme.spec.spacing.small,
    threshold: Float = 0.8f,
    canStartDragDown: (() -> Boolean)? = null,
    borderColor: Color = VelarisTheme.spec.colors.stroke.copy(alpha = VelarisTheme.spec.alpha.strokeStrong),
    content: @Composable BoxScope.() -> Unit,
) {
    val spec = VelarisTheme.spec
    val density = LocalDensity.current
    val panelDebugName = remember(debugName) { debugName ?: "unnamed" }
    val currentVisible by rememberUpdatedState(visible)
    val currentCanStartDragDown by rememberUpdatedState(canStartDragDown)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val useFullScreenPanel = remember(maxHeight, panelHeight) {
            maxHeight < 420.dp || panelHeight >= maxHeight
        }
        val resolvedPanelHeight = if (useFullScreenPanel) maxHeight else panelHeight
        val panelHeightPx = with(density) { resolvedPanelHeight.toPx() }
        val panelCornerRadius = spec.radii.panel

        val offsetY = remember(resolvedPanelHeight) {
            Animatable(panelHeightPx)
        }
        var isDragClosing by remember(resolvedPanelHeight) { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val settlePanel: () -> Unit = {
            isDragClosing = false
            val shouldShow = offsetY.value < panelHeightPx * (1f - threshold)
            Timber.tag(SWIPE_UP_PANEL_TAG).d(
                "[%s] settle offset=%.1f threshold=%.2f shouldShow=%s",
                panelDebugName,
                offsetY.value,
                threshold,
                shouldShow,
            )
            onVisibleChange(shouldShow)
            scope.launch {
                offsetY.animateTo(
                    targetValue = if (shouldShow) 0f else panelHeightPx,
                    animationSpec = tween(
                        durationMillis = 260,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
        val nestedScrollConnection = remember(panelHeightPx, threshold, panelDebugName, canStartDragDown) {
            if (canStartDragDown == null) {
                null
            } else {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        if (source != NestedScrollSource.UserInput || offsetY.value <= 0f) {
                            return Offset.Zero
                        }
                        val previousOffset = offsetY.value
                        val newOffset = (previousOffset + available.y).coerceIn(0f, panelHeightPx)
                        val consumedY = newOffset - previousOffset
                        if (consumedY == 0f) {
                            return Offset.Zero
                        }
                        isDragClosing = currentVisible
                        Timber.tag(SWIPE_UP_PANEL_TAG).d(
                            "[%s] nested pre consumed availableY=%.1f fromOffset=%.1f toOffset=%.1f",
                            panelDebugName,
                            available.y,
                            previousOffset,
                            newOffset,
                        )
                        scope.launch {
                            offsetY.snapTo(newOffset)
                        }
                        return Offset(0f, consumedY)
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        if (source != NestedScrollSource.UserInput || available.y <= 0f) {
                            return Offset.Zero
                        }
                        val canDragDown = currentCanStartDragDown?.invoke() ?: true
                        if (!canDragDown) {
                            return Offset.Zero
                        }
                        val previousOffset = offsetY.value
                        val newOffset = (previousOffset + available.y).coerceIn(0f, panelHeightPx)
                        val consumedY = newOffset - previousOffset
                        if (consumedY == 0f) {
                            return Offset.Zero
                        }
                        isDragClosing = currentVisible
                        Timber.tag(SWIPE_UP_PANEL_TAG).d(
                            "[%s] nested post consumed availableY=%.1f childConsumedY=%.1f fromOffset=%.1f toOffset=%.1f",
                            panelDebugName,
                            available.y,
                            consumed.y,
                            previousOffset,
                            newOffset,
                        )
                        scope.launch {
                            offsetY.snapTo(newOffset)
                        }
                        return Offset(0f, consumedY)
                    }

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                        if (offsetY.value > 0f) {
                            Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                "[%s] nested post fling offset=%.1f consumedY=%.1f availableY=%.1f",
                                panelDebugName,
                                offsetY.value,
                                consumed.y,
                                available.y,
                            )
                            settlePanel()
                        }
                        return Velocity.Zero
                    }
                }
            }
        }

        LaunchedEffect(panelHeightPx) {
            offsetY.snapTo(if (visible) 0f else panelHeightPx)
        }

        LaunchedEffect(visible) {
            Timber.tag(SWIPE_UP_PANEL_TAG).d(
                "[%s] visible changed visible=%s offset=%.1f panelHeightPx=%.1f",
                panelDebugName,
                visible,
                offsetY.value,
                panelHeightPx,
            )
            offsetY.animateTo(
                targetValue = if (visible) 0f else panelHeightPx,
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            )
        }

        val progress = 1f - (offsetY.value / panelHeightPx).coerceIn(0f, 1f)
        val shouldComposeContent = visible || offsetY.value < panelHeightPx - 0.5f

        // 玻璃效果与展开动画解耦：
        // - 展开过程中使用轻量表面（blur=0、延迟启用 lens/vibrancy）
        // - 接近完全展开或处于稳定态时恢复完整液态玻璃
        // - 关闭过程中尽量保持完整玻璃，避免视觉突变
        val isSettledVisible = visible && offsetY.value < 1f
        val isSettledHidden = !visible && offsetY.value >= panelHeightPx - 0.5f
        val isClosingAnimation = !visible && offsetY.value > 0.5f && !isSettledHidden
        val isDismissing = isDragClosing || isClosingAnimation

        val glassProgress = when {
            isSettledVisible -> 1f
            isDismissing -> 1f
            isSettledHidden -> 0f
            else -> ((progress - 0.78f) / 0.22f).coerceIn(0f, 1f)
        }

        val effectiveBlurRadius = lerp(0.dp, spec.blur.panel, glassProgress)
        val enableFullVibrancy = glassProgress > 0.35f || isSettledVisible || isDismissing
        val enableFullLens = glassProgress > 0.92f || isSettledVisible

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = spec.alpha.panelScrim * progress)),
        ) {
            Box(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(resolvedPanelHeight)
                    .padding(horizontal = edgePadding, vertical = edgePadding)
                    .then(
                        if (nestedScrollConnection != null) {
                            Modifier.nestedScroll(nestedScrollConnection)
                        } else {
                            Modifier
                        },
                    )
                    .offset {
                        IntOffset(
                            x = 0,
                            y = offsetY.value.roundToInt(),
                        )
                    }
                    .clip(RoundedCornerShape(panelCornerRadius))
                    .velarisGlassBlur(
                        shape = RoundedCornerShape(panelCornerRadius),
                        blurRadius = effectiveBlurRadius,
                        enableVibrancy = enableFullVibrancy,
                        enableLens = enableFullLens,
                    )
                    .border(
                        width = spec.size.stroke,
                        color = borderColor,
                        shape = RoundedCornerShape(panelCornerRadius),
                    )
                    .pointerInput(panelHeightPx) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isDragClosing = visible
                                Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                    "[%s] drag start visible=%s offset=%.1f panelHeightPx=%.1f canStartDragDown=%s",
                                    panelDebugName,
                                    visible,
                                    offsetY.value,
                                    panelHeightPx,
                                    canStartDragDown?.invoke(),
                                )
                                scope.launch {
                                    offsetY.stop()
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                val shouldConsumeDrag = when {
                                    offsetY.value > 0f -> true
                                    dragAmount <= 0f -> false
                                    else -> canStartDragDown?.invoke() ?: true
                                }
                                if (!shouldConsumeDrag) {
                                    Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                        "[%s] drag skipped dragAmount=%.1f offset=%.1f reason=%s canStartDragDown=%s",
                                        panelDebugName,
                                        dragAmount,
                                        offsetY.value,
                                        if (dragAmount <= 0f) "upward_child_scroll" else "child_not_at_top",
                                        canStartDragDown?.invoke(),
                                    )
                                    return@detectVerticalDragGestures
                                }
                                change.consume()

                                val newOffset = (offsetY.value + dragAmount)
                                    .coerceIn(0f, panelHeightPx)

                                Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                    "[%s] drag consumed dragAmount=%.1f fromOffset=%.1f toOffset=%.1f visible=%s",
                                    panelDebugName,
                                    dragAmount,
                                    offsetY.value,
                                    newOffset,
                                    visible,
                                )

                                scope.launch {
                                    offsetY.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                    "[%s] drag end offset=%.1f",
                                    panelDebugName,
                                    offsetY.value,
                                )
                                settlePanel()
                            },
                            onDragCancel = {
                                isDragClosing = false
                                Timber.tag(SWIPE_UP_PANEL_TAG).d(
                                    "[%s] drag cancel visible=%s offset=%.1f",
                                    panelDebugName,
                                    visible,
                                    offsetY.value,
                                )
                                scope.launch {
                                    offsetY.animateTo(
                                        targetValue = if (visible) 0f else panelHeightPx,
                                        animationSpec = tween(260),
                                    )
                                }
                            },
                        )
                    },
            ) {
                if (shouldComposeContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(spec.brushes.glassSurface),
                    )

                    // 顶部手柄
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = spec.spacing.medium)
                            .width(spec.size.panelHandleWidth)
                            .height(spec.size.panelHandleHeight)
                            .clip(RoundedCornerShape(50))
                            .background(spec.colors.stroke.copy(alpha = 0.32f)),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun PreviewSwipeUpPanel() {
    ProvideVelarisTheme {
        SwipeUpPanel(
            visible = true,
            onVisibleChange = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray),
                contentAlignment = Alignment.Center,
            ) {
                Text("面板内容")
            }
        }
    }
}
