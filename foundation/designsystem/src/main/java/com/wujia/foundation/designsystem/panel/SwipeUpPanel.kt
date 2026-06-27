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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeUpPanel(
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    panelHeight: Dp = 380.dp,
    edgePadding: Dp = VelarisTheme.spec.spacing.small,
    threshold: Float = 0.8f,
    borderColor: Color = VelarisTheme.spec.colors.stroke.copy(alpha = VelarisTheme.spec.alpha.strokeStrong),
    content: @Composable BoxScope.() -> Unit,
) {
    val spec = VelarisTheme.spec
    val density = LocalDensity.current

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

        val scope = rememberCoroutineScope()

        LaunchedEffect(panelHeightPx) {
            offsetY.snapTo(if (visible) 0f else panelHeightPx)
        }

        LaunchedEffect(visible) {
            offsetY.animateTo(
                targetValue = if (visible) 0f else panelHeightPx,
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            )
        }

        val progress = 1f - (offsetY.value / panelHeightPx).coerceIn(0f, 1f)
        val shouldComposeContent = visible || offsetY.value < panelHeightPx - 0.5f

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
                    .offset {
                        IntOffset(
                            x = 0,
                            y = offsetY.value.roundToInt(),
                        )
                    }
                    .clip(RoundedCornerShape(panelCornerRadius))
                    .velarisGlassBlur(
                        shape = RoundedCornerShape(panelCornerRadius),
                        blurRadius = spec.blur.panel,
                    )
                    .border(
                        width = spec.size.stroke,
                        color = borderColor,
                        shape = RoundedCornerShape(panelCornerRadius),
                    )
                    .pointerInput(panelHeightPx) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                scope.launch {
                                    offsetY.stop()
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()

                                val newOffset = (offsetY.value + dragAmount)
                                    .coerceIn(0f, panelHeightPx)

                                scope.launch {
                                    offsetY.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                val shouldShow =
                                    offsetY.value < panelHeightPx * (1f - threshold)

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
                            },
                            onDragCancel = {
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
