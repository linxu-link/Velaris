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
package com.wujia.foundation.designsystem.clock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.designsystem.theme.VelarisFontSize

/**
 * 倒计时翻页时钟。
 *
 * 日历式上下翻页效果：上半页向下翻转，下半页保持静止，新数字从背后自然露出。
 * 卡片默认使用淡白色半透明表面，适合叠在深色背景上。
 */
@Composable
fun FlipCountdownClock(
    time: FlipClockTime,
    modifier: Modifier = Modifier,
    units: List<FlipClockUnit> = listOf(
        FlipClockUnit.Hour,
        FlipClockUnit.Minute,
        FlipClockUnit.Second,
    ),
    onFlip: (() -> Unit)? = null,
    cardWidth: Dp = 90.dp,
    cardHeight: Dp = 94.dp,
    digitGap: Dp = 0.dp,
    unitGap: Dp = 1.dp,
    separatorGap: Dp = 4.dp,
    cornerRadius: Dp = 0.dp,
    animationDurationMillis: Int = 1000,
    cardBackgroundColor: Color = Color.Black.copy(alpha = 0.1f),
    cardHighlightColor: Color = Color.Black.copy(alpha = 0.18f),
    cardBorderColor: Color = Color.White.copy(alpha = 0.06f),
    digitColor: Color = Color.White.copy(alpha = 0.92f),
    separatorColor: Color = Color.White.copy(alpha = 0.78f),
    textStyle: TextStyle = TextStyle(
        fontSize = VelarisFontSize.FlipClockDigit,
        fontWeight = FontWeight.SemiBold,
        lineHeight = VelarisFontSize.FlipClockDigit,
        letterSpacing = 0.sp,
    ),
) {
    val displayUnits = remember(units) {
        units.ifEmpty {
            listOf(
                FlipClockUnit.Hour,
                FlipClockUnit.Minute,
                FlipClockUnit.Second,
            )
        }.distinct()
    }

    Row(
        modifier = modifier.semantics {
            contentDescription = time.format(displayUnits)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(unitGap),
    ) {
        displayUnits.forEachIndexed { index, unit ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(separatorGap))
                FlipClockSeparator(
                    color = separatorColor,
                    textStyle = textStyle,
                    height = cardHeight,
                )
                Spacer(modifier = Modifier.width(separatorGap))
            }

            FlipClockUnitGroup(
                value = time.valueFor(unit),
                onFlip = onFlip,
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight),
                cardWidth = cardWidth,
                digitGap = digitGap,
                cardHeight = cardHeight,
                cornerRadius = cornerRadius,
                animationDurationMillis = animationDurationMillis,
                backgroundColor = cardBackgroundColor,
                highlightColor = cardHighlightColor,
                borderColor = cardBorderColor,
                digitColor = digitColor,
                textStyle = textStyle,
            )
        }
    }
}

@Immutable
data class FlipClockTime(val hours: Int = 0, val minutes: Int = 0, val seconds: Int = 0) {
    companion object {
        fun fromTotalSeconds(totalSeconds: Long): FlipClockTime {
            val safeSeconds = totalSeconds.coerceAtLeast(0L)
            val hours = (safeSeconds / 3600L).toInt()
            val minutes = ((safeSeconds % 3600L) / 60L).toInt()
            val seconds = (safeSeconds % 60L).toInt()
            return FlipClockTime(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
            )
        }
    }
}

enum class FlipClockUnit {
    Hour,
    Minute,
    Second,
}

/** 一个时间单位，内部按位拆分，只有变化的数字位会触发翻牌。 */
@Composable
private fun FlipClockUnitGroup(
    value: String,
    modifier: Modifier = Modifier,
    onFlip: (() -> Unit)? = null,
    cardWidth: Dp,
    digitGap: Dp,
    cardHeight: Dp,
    cornerRadius: Dp,
    animationDurationMillis: Int,
    backgroundColor: Color,
    highlightColor: Color,
    borderColor: Color,
    digitColor: Color,
    textStyle: TextStyle,
) {
    val digits = remember(value) {
        value.map(Char::toString)
    }
    val digitWidth = digitCardWidth(
        cardWidth = cardWidth,
        digitGap = digitGap,
        digitCount = digits.size,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(digitGap),
    ) {
        digits.forEach { digit ->
            FlipDigitCard(
                value = digit,
                onFlip = onFlip,
                modifier = Modifier
                    .width(digitWidth)
                    .height(cardHeight),
                cardHeight = cardHeight,
                cornerRadius = cornerRadius,
                animationDurationMillis = animationDurationMillis,
                backgroundColor = backgroundColor,
                highlightColor = highlightColor,
                borderColor = borderColor,
                digitColor = digitColor,
                textStyle = textStyle,
            )
        }
    }
}

/**
 * 单位数字翻牌卡片。
 *
 * 静态层会避开正在动画的半片，防止不同数字叠在一起。
 * 动画层：旧数字上半片 0° -> 90°，随后新数字下半片 90° -> 0°。
 */
@Composable
private fun FlipDigitCard(
    value: String,
    modifier: Modifier = Modifier,
    onFlip: (() -> Unit)? = null,
    cardHeight: Dp,
    cornerRadius: Dp,
    animationDurationMillis: Int,
    backgroundColor: Color,
    highlightColor: Color,
    borderColor: Color,
    digitColor: Color,
    textStyle: TextStyle,
) {
    val flipProgress = remember { Animatable(1f) }
    var currentValue by remember { mutableStateOf(value) }
    var previousValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            onFlip?.invoke()
            previousValue = currentValue
            currentValue = value
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationDurationMillis,
                    easing = FastOutSlowInEasing,
                ),
            )
            previousValue = currentValue
        }
    }

    val progress = flipProgress.value.coerceIn(0f, 1f)
    val isTransitioning = previousValue != currentValue || progress < 1f
    val shape = RoundedCornerShape(cornerRadius)
    val centerGap = 2.dp
    val visibleHalfHeight = cardHalfHeight(
        cardHeight = cardHeight,
        centerGap = centerGap,
    )
    val bottomValue = if (isTransitioning) previousValue else currentValue
    val oldTopAnimating = isTransitioning && progress < 0.5f
    val newBottomAnimating = isTransitioning && progress >= 0.5f

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // 顶部高光渐变
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(highlightColor, Color.Transparent),
                    ),
                ),
        )

        if (!oldTopAnimating) {
            CalendarDigitHalf(
                value = currentValue,
                half = DigitHalf.Top,
                cardHeight = cardHeight,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(visibleHalfHeight),
                textStyle = textStyle,
                digitColor = digitColor,
            )
        }

        if (!newBottomAnimating) {
            CalendarDigitHalf(
                value = bottomValue,
                half = DigitHalf.Bottom,
                cardHeight = cardHeight,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(visibleHalfHeight),
                textStyle = textStyle,
                digitColor = digitColor,
            )
        }

        if (isTransitioning) {
            SplitFlapAnimationLayers(
                previousValue = previousValue,
                currentValue = currentValue,
                progress = progress,
                cardHeight = cardHeight,
                visibleHalfHeight = visibleHalfHeight,
                backgroundColor = backgroundColor,
                textStyle = textStyle,
                digitColor = digitColor,
            )
        }

        CalendarCenterBreak(centerGap = centerGap)
    }
}

/**
 * 按卡片完整高度裁剪数字。
 *
 * 半区内部仍按完整卡片高度测量文字，避免上下半数字各自居中导致错位。
 */
@Composable
private fun CalendarDigitHalf(
    value: String,
    half: DigitHalf,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    digitColor: Color,
) {
    val density = LocalDensity.current
    val fullHeightPx = with(density) {
        cardHeight.roundToPx()
    }

    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value,
                    color = digitColor,
                    style = textStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val visibleHeight = constraints.maxHeight
        val measuredFullHeight = fullHeightPx.coerceAtLeast(visibleHeight)
        val placeable = measurables.first().measure(
            Constraints.fixed(
                width = width,
                height = measuredFullHeight,
            ),
        )

        layout(width = width, height = visibleHeight) {
            val y = when (half) {
                DigitHalf.Top -> 0
                DigitHalf.Bottom -> visibleHeight - measuredFullHeight
            }
            placeable.place(x = 0, y = y)
        }
    }
}

@Composable
private fun BoxScope.SplitFlapAnimationLayers(
    previousValue: String,
    currentValue: String,
    progress: Float,
    cardHeight: Dp,
    visibleHalfHeight: Dp,
    backgroundColor: Color,
    textStyle: TextStyle,
    digitColor: Color,
) {
    val density = LocalDensity.current
    val oldTopProgress = FastOutSlowInEasing.transform(progress.phase(0f, 0.5f))
    val newBottomProgress = FastOutSlowInEasing.transform(progress.phase(0.5f, 1f))
    val oldTopVisible = progress < 0.5f
    val newBottomVisible = progress >= 0.5f
    val oldTopSurfaceColor = backgroundColor.copy(
        alpha = backgroundColor.alpha * 0.42f * oldTopProgress,
    )
    val newBottomSurfaceColor = backgroundColor.copy(
        alpha = backgroundColor.alpha * 0.42f * (1f - newBottomProgress),
    )

    if (oldTopVisible) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(visibleHalfHeight)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    rotationX = -90f * oldTopProgress
                    cameraDistance = 80f * density.density
                }
                .background(oldTopSurfaceColor),
        ) {
            CalendarDigitHalf(
                value = previousValue,
                half = DigitHalf.Top,
                cardHeight = cardHeight,
                modifier = Modifier.fillMaxSize(),
                textStyle = textStyle,
                digitColor = digitColor,
            )

            SplitFlapShadow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(visibleHalfHeight),
                alpha = 0.08f + oldTopProgress * 0.16f,
                fromTop = false,
            )
        }
    }

    if (newBottomVisible) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(visibleHalfHeight)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = 90f * (1f - newBottomProgress)
                    cameraDistance = 80f * density.density
                }
                .background(newBottomSurfaceColor),
        ) {
            CalendarDigitHalf(
                value = currentValue,
                half = DigitHalf.Bottom,
                cardHeight = cardHeight,
                modifier = Modifier.fillMaxSize(),
                textStyle = textStyle,
                digitColor = digitColor,
            )

            SplitFlapShadow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(visibleHalfHeight),
                alpha = 0.08f + (1f - newBottomProgress) * 0.16f,
                fromTop = true,
            )
        }
    }
}

@Composable
private fun SplitFlapShadow(modifier: Modifier, alpha: Float, fromTop: Boolean) {
    val colors = if (fromTop) {
        listOf(
            Color.Black.copy(alpha = alpha.coerceIn(0f, 0.26f)),
            Color.Transparent,
        )
    } else {
        listOf(
            Color.Transparent,
            Color.Black.copy(alpha = alpha.coerceIn(0f, 0.26f)),
        )
    }

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = colors,
            ),
        ),
    )
}

@Composable
private fun BoxScope.CalendarCenterBreak(centerGap: Dp) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .height(centerGap + 1.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

@Composable
private fun FlipClockSeparator(color: Color, textStyle: TextStyle, height: Dp) {
    Box(
        modifier = Modifier.height(height),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ":",
            color = color,
            style = textStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private enum class DigitHalf {
    Top,
    Bottom,
}

private fun cardHalfHeight(cardHeight: Dp, centerGap: Dp): Dp =
    ((cardHeight.value - centerGap.value).coerceAtLeast(0f) / 2f).dp

private fun digitCardWidth(cardWidth: Dp, digitGap: Dp, digitCount: Int): Dp {
    if (digitCount <= 1) return cardWidth
    val totalGap = digitGap.value * (digitCount - 1)
    return ((cardWidth.value - totalGap).coerceAtLeast(0f) / digitCount).dp
}

private fun Float.phase(start: Float, end: Float): Float = ((this - start) / (end - start)).coerceIn(0f, 1f)

private fun FlipClockTime.valueFor(unit: FlipClockUnit): String = when (unit) {
    FlipClockUnit.Hour -> hours.coerceAtLeast(0).toClockText()
    FlipClockUnit.Minute -> minutes.coerceAtLeast(0).toClockText()
    FlipClockUnit.Second -> seconds.coerceAtLeast(0).toClockText()
}

private fun FlipClockTime.format(units: List<FlipClockUnit>): String =
    units.joinToString(separator = ":") { valueFor(it) }

private fun Int.toClockText(): String = when {
    this < 10 -> "0$this"
    this < 100 -> toString().padStart(2, '0')
    else -> toString()
}

@LandscapePreviews
@Composable
private fun FlipCountdownClockPreview() {
    ProvideVelarisTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlipCountdownClock(
                time = FlipClockTime(hours = 2, minutes = 18, seconds = 42),
            )

            FlipCountdownClock(
                time = FlipClockTime(hours = 0, minutes = 9, seconds = 5),
                units = listOf(FlipClockUnit.Minute, FlipClockUnit.Second),
                cardWidth = 76.dp,
                cardHeight = 96.dp,
            )
        }
    }
}

@Preview
@Composable
private fun FlipCountdownClockSinglePreview() {
    ProvideVelarisTheme {
        FlipCountdownClock(
            time = FlipClockTime(hours = 12, minutes = 34, seconds = 56),
            units = listOf(FlipClockUnit.Hour),
            cardWidth = 84.dp,
            cardHeight = 104.dp,
        )
    }
}
