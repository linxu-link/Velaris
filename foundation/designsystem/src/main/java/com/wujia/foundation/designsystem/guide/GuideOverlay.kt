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
package com.wujia.foundation.designsystem.guide

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.designsystem.theme.VelarisTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Immutable
enum class GuidePlacement {
    Auto,
    Above,
    Below,
    Start,
    End,
}

@Composable
fun GuideOverlay(
    targetState: GuideTargetState,
    description: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    title: String? = null,
    placement: GuidePlacement = GuidePlacement.Auto,
    highlightPadding: Dp = 12.dp,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    if (!visible) return

    val targetBounds = targetState.bounds ?: return
    val spec = VelarisTheme.spec
    val density = LocalDensity.current
    val highlightPaddingPx = with(density) { highlightPadding.toPx() }
    val bubbleSpacingPx = with(density) { (spec.spacing.large * 4.75f).toPx() }
    val bubblePaddingPx = with(density) { spec.spacing.edgeSmall.toPx() }
    val arrowAmplitudePx = with(density) { 18.dp.toPx() }
    val arrowHeadPx = with(density) { 10.dp.toPx() }
    val strokeWidthPx = with(density) { 2.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val rootWidthPx = with(density) { maxWidth.toPx() }
        val rootHeightPx = with(density) { maxHeight.toPx() }

        var bubbleSize by remember { mutableStateOf(IntSize.Zero) }
        val bubbleRect = remember(targetBounds, bubbleSize, placement, rootWidthPx, rootHeightPx) {
            resolveBubbleRect(
                targetBounds = targetBounds,
                bubbleSize = bubbleSize,
                placement = placement,
                rootWidth = rootWidthPx,
                rootHeight = rootHeightPx,
                spacing = bubbleSpacingPx,
                screenPadding = bubblePaddingPx,
            )
        }
        val spotlightRect = remember(targetBounds, highlightPaddingPx) {
            targetBounds.inflate(highlightPaddingPx)
        }

        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .background(Color.Transparent)
                    .clearAndSetSemantics { }
                    .velarisClickable { },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xC7000000))
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = spotlightRect.topLeft,
                        size = spotlightRect.size,
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        blendMode = BlendMode.Clear,
                    )
                    drawRoundRect(
                        color = spec.colors.gold.copy(alpha = 0.72f),
                        topLeft = spotlightRect.topLeft,
                        size = spotlightRect.size,
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        style = Stroke(width = strokeWidthPx),
                    )

                    if (bubbleSize != IntSize.Zero) {
                        val start = bubbleAnchorPoint(
                            bubbleRect = bubbleRect,
                            targetRect = spotlightRect,
                        )
                        val end = targetAnchorPoint(
                            targetRect = spotlightRect,
                            bubbleRect = bubbleRect,
                        )
                        val curve = buildGuideCurve(
                            start = start,
                            end = end,
                            bend = arrowAmplitudePx,
                        )
                        drawPath(
                            path = curve.path,
                            color = spec.colors.gold,
                            style = Stroke(width = strokeWidthPx),
                        )
                        drawArrowHead(
                            end = end,
                            angle = curve.endAngle,
                            size = arrowHeadPx,
                            color = spec.colors.gold,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = bubbleRect.left.roundToInt(),
                            y = bubbleRect.top.roundToInt(),
                        )
                    }
                    .defaultMinSize(minWidth = 220.dp)
                    .onGloballyPositioned { coordinates: LayoutCoordinates ->
                        bubbleSize = coordinates.size
                    }
                    .background(
                        color = spec.colors.surface,
                        shape = RoundedCornerShape(spec.radii.thumbnail),
                    )
                    .border(
                        width = spec.size.stroke,
                        color = spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong),
                        shape = RoundedCornerShape(spec.radii.thumbnail),
                    )
                    .padding(spec.spacing.large)
                    .velarisClickable(enabled = false) { },
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                        fontSize = spec.typography.sectionTitle,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.size(spec.spacing.small))
                }
                Text(
                    text = description,
                    color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                    fontSize = spec.typography.body,
                )
                if (content != null) {
                    Spacer(modifier = Modifier.size(spec.spacing.medium))
                    content()
                }
            }
        }
    }
}

private fun Rect.inflate(padding: Float): Rect = Rect(
    left = left - padding,
    top = top - padding,
    right = right + padding,
    bottom = bottom + padding,
)

private fun resolveBubbleRect(
    targetBounds: Rect,
    bubbleSize: IntSize,
    placement: GuidePlacement,
    rootWidth: Float,
    rootHeight: Float,
    spacing: Float,
    screenPadding: Float,
): Rect {
    val bubbleWidth = bubbleSize.width.toFloat().takeIf { it > 0f } ?: 280f
    val bubbleHeight = bubbleSize.height.toFloat().takeIf { it > 0f } ?: 120f
    val resolvedPlacement = when (placement) {
        GuidePlacement.Auto -> {
            val aboveTop = targetBounds.top - spacing - bubbleHeight
            if (aboveTop >= screenPadding) GuidePlacement.Above else GuidePlacement.Below
        }
        else -> placement
    }

    val rawLeft = when (resolvedPlacement) {
        GuidePlacement.Start -> targetBounds.left - spacing - bubbleWidth
        GuidePlacement.End -> targetBounds.right + spacing
        else -> targetBounds.center.x - (bubbleWidth / 2f)
    }
    val rawTop = when (resolvedPlacement) {
        GuidePlacement.Above -> targetBounds.top - spacing - bubbleHeight
        GuidePlacement.Below -> targetBounds.bottom + spacing
        GuidePlacement.Start, GuidePlacement.End -> targetBounds.center.y - (bubbleHeight / 2f)
        GuidePlacement.Auto -> targetBounds.bottom + spacing
    }

    val left = rawLeft.coerceIn(screenPadding, max(screenPadding, rootWidth - bubbleWidth - screenPadding))
    val top = rawTop.coerceIn(screenPadding, max(screenPadding, rootHeight - bubbleHeight - screenPadding))

    return Rect(
        left = left,
        top = top,
        right = left + bubbleWidth,
        bottom = top + bubbleHeight,
    )
}

private fun bubbleAnchorPoint(bubbleRect: Rect, targetRect: Rect): Offset = when {
    bubbleRect.bottom <= targetRect.top -> Offset(bubbleRect.center.x, bubbleRect.bottom)
    bubbleRect.top >= targetRect.bottom -> Offset(bubbleRect.center.x, bubbleRect.top)
    bubbleRect.right <= targetRect.left -> Offset(bubbleRect.right, bubbleRect.center.y)
    else -> Offset(bubbleRect.left, bubbleRect.center.y)
}

private fun targetAnchorPoint(targetRect: Rect, bubbleRect: Rect): Offset = when {
    bubbleRect.bottom <= targetRect.top -> Offset(targetRect.center.x, targetRect.top)
    bubbleRect.top >= targetRect.bottom -> Offset(targetRect.center.x, targetRect.bottom)
    bubbleRect.right <= targetRect.left -> Offset(targetRect.left, targetRect.center.y)
    else -> Offset(targetRect.right, targetRect.center.y)
}

private data class GuideCurve(val path: Path, val endAngle: Float)

private fun buildGuideCurve(start: Offset, end: Offset, bend: Float): GuideCurve {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = max(1f, sqrt((dx * dx) + (dy * dy)))
    val unitX = dx / length
    val unitY = dy / length
    val perpendicularX = -unitY
    val perpendicularY = unitX
    val bendAmount = minOf(length * 0.22f, bend * 2.4f)
    val mid = Offset(
        x = start.x + dx * 0.5f,
        y = start.y + dy * 0.5f,
    )
    val control = Offset(
        x = mid.x + perpendicularX * bendAmount,
        y = mid.y + perpendicularY * bendAmount,
    )
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }
    val tangent = Offset(
        x = end.x - control.x,
        y = end.y - control.y,
    )
    return GuideCurve(
        path = path,
        endAngle = atan2(tangent.y, tangent.x),
    )
}

private fun DrawScope.drawArrowHead(end: Offset, angle: Float, size: Float, color: Color) {
    val left = Offset(
        x = end.x - (cos(angle - ARROW_HEAD_SPREAD_RADIANS) * size),
        y = end.y - (sin(angle - ARROW_HEAD_SPREAD_RADIANS) * size),
    )
    val right = Offset(
        x = end.x - (cos(angle + ARROW_HEAD_SPREAD_RADIANS) * size),
        y = end.y - (sin(angle + ARROW_HEAD_SPREAD_RADIANS) * size),
    )
    drawLine(color = color, start = end, end = left, strokeWidth = size / 4f)
    drawLine(color = color, start = end, end = right, strokeWidth = size / 4f)
}

private const val ARROW_HEAD_SPREAD_RADIANS = 0.45f

@Preview(widthDp = 720, heightDp = 360)
@Composable
private fun GuideOverlayPreview() {
    val targetState = rememberGuideTargetState()
    ProvideVelarisTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF11161D))) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 72.dp)
                    .size(width = 140.dp, height = 56.dp)
                    .background(
                        color = VelarisTheme.spec.colors.controlSurface,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .guideTarget(targetState),
            )
            GuideOverlay(
                targetState = targetState,
                title = "播放按钮",
                description = "这里可以快速开始或暂停当前场景的声音与画面。",
                placement = GuidePlacement.End,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = VelarisTheme.spec.colors.gold,
                                shape = RoundedCornerShape(99.dp),
                            ),
                    )
                    Text(
                        text = "支持继续扩展操作区",
                        color = VelarisTheme.spec.colors.textMuted.copy(alpha = VelarisTheme.spec.alpha.textMuted),
                        fontSize = VelarisTheme.spec.typography.bodySmall,
                    )
                }
            }
        }
    }
}
