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
package com.wujia.foundation.designsystem.theme

import android.os.Build
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import timber.log.Timber

private const val LIQUID_GLASS_TAG = "VelarisLiquidGlass"

class VelarisBlurState internal constructor(internal val source: LayerBackdrop) {
    val debugId: Int
        get() = source.hashCode()
}

class VelarisBackdropState internal constructor(internal val source: LayerBackdrop) {
    val debugId: Int
        get() = source.hashCode()
}

val LocalVelarisBlurState = staticCompositionLocalOf<VelarisBlurState?> { null }
val LocalVelarisBackdrop = staticCompositionLocalOf<VelarisBackdropState?> { null }

@Composable
private fun createVelarisLayerBackdrop(): LayerBackdrop = rememberLayerBackdrop {
    drawRect(VelarisColor.GlassRegularBackground.copy(alpha = 0.72f))
    drawContent()
}

@Composable
fun rememberVelarisBlurState(): VelarisBlurState = VelarisBlurState(createVelarisLayerBackdrop())

@Composable
fun rememberVelarisBackdropState(): VelarisBackdropState = VelarisBackdropState(createVelarisLayerBackdrop())

@Composable
fun ProvideVelarisBlurState(state: VelarisBlurState?, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalVelarisBlurState provides state,
        content = content,
    )
}

@Composable
fun ProvideVelarisBackdrop(backdrop: VelarisBackdropState?, content: @Composable () -> Unit) {
    Timber.tag(LIQUID_GLASS_TAG).d(
        "ProvideVelarisBackdrop backdropProvided=%s sourceHash=%s",
        backdrop != null,
        backdrop?.debugId,
    )
    CompositionLocalProvider(
        LocalVelarisBackdrop provides backdrop,
        content = content,
    )
}

fun Modifier.velarisBlurSource(state: VelarisBlurState): Modifier = layerBackdrop(state.source)

fun Modifier.velarisBackdropSource(backdrop: VelarisBackdropState): Modifier = layerBackdrop(backdrop.source)

enum class VelarisGlassMaterial {
    Thin,
    Regular,
}

@Composable
fun Modifier.velarisGlassBlur(
    shape: Shape,
    blurRadius: Dp = 0.dp,
    surfaceColor: Color = VelarisTheme.spec.colors.surface,
    surfaceAlpha: Float = 0f,
): Modifier {
    val state = LocalVelarisBlurState.current ?: return this
    return this.drawBackdrop(
        backdrop = state.source,
        shape = { shape },
        effects = {
            vibrancy()
            if (blurRadius > 0.dp) {
                blur(blurRadius.toPx())
            }
            if (Build.VERSION.SDK_INT >= 33 && shape is CornerBasedShape) {
                lens(
                    refractionHeight = 16.dp.toPx(),
                    refractionAmount = 63.dp.toPx(),
                )
            } else {
                blur(4.dp.toPx())
            }
        },
        onDrawSurface = {
            drawRect(surfaceColor.copy(alpha = surfaceAlpha))
        },
    )
}

@Composable
fun Modifier.velarisLiquidGlass(shape: Shape, tint: Color, fallbackTint: Color = tint): Modifier {
    val backdropState = LocalVelarisBackdrop.current
    if (backdropState == null) {
        Timber.tag(LIQUID_GLASS_TAG).w(
            "velarisLiquidGlass skipped because LocalVelarisBackdrop is null",
        )
        return this
    }
    val backdrop = backdropState.source
    Timber.tag(LIQUID_GLASS_TAG).d(
        "velarisLiquidGlass applied sourceHash=%s tintAlpha=%.2f fallbackAlpha=%.2f",
        backdrop.hashCode(),
        tint.alpha,
        fallbackTint.alpha,
    )
    return this
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(4.dp.toPx())
                if (Build.VERSION.SDK_INT >= 33) {
                    lens(
                        refractionHeight = 18.dp.toPx(),
                        refractionAmount = 34.dp.toPx(),
                        chromaticAberration = true,
                    )
                }
            },
            onDrawSurface = {
                drawRect(tint)
            },
        )
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.9f, size.height * 0.55f),
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        VelarisColor.Gold.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.12f),
                    radius = size.minDimension * 0.9f,
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        fallbackTint.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.22f),
                    ),
                    startY = size.height * 0.35f,
                    endY = size.height,
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.32f),
                        Color.White.copy(alpha = 0.12f),
                    ),
                ),
                style = Stroke(width = 1.6.dp.toPx()),
            )
        }
}

@Composable
fun Modifier.velarisBackdropReferenceGlass(shape: Shape, surfaceAlpha: Float = 0f): Modifier {
    val backdropState = LocalVelarisBackdrop.current ?: return this
    return this.drawBackdrop(
        backdrop = backdropState.source,
        shape = { shape },
        effects = {
            vibrancy()
            if (Build.VERSION.SDK_INT >= 33) {
                lens(13.dp.toPx(), 64.dp.toPx())
            }
        },
        onDrawSurface = {
            drawRect(Color.White.copy(alpha = surfaceAlpha))
        },
    )
}
