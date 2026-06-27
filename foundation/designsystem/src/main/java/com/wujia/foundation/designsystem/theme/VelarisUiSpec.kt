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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Immutable
data class VelarisUiSpec(
    val colors: VelarisColors = VelarisColors(),
    val alpha: VelarisAlpha = VelarisAlpha(),
    val radii: VelarisRadii = VelarisRadii(),
    val blur: VelarisBlur = VelarisBlur(),
    val spacing: VelarisSpacing = VelarisSpacing(),
    val size: VelarisSize = VelarisSize(),
    val typography: VelarisTypography = VelarisTypography(),
    val glow: VelarisGlow = VelarisGlow(),
    val brushes: VelarisBrushes = VelarisBrushes(),
)

@Immutable
data class VelarisColors(
    val gold: Color = VelarisColor.Gold,
    val goldSoft: Color = VelarisColor.GoldSoft,
    val goldBright: Color = VelarisColor.GoldBright,
    val onGold: Color = VelarisColor.OnGold,
    val textPrimary: Color = goldBright,
    val textSecondary: Color = goldSoft,
    val textMuted: Color = VelarisColor.TextMuted,
    val iconMuted: Color = VelarisColor.IconMuted,
    val surface: Color = VelarisColor.Surface,
    val surfaceSoft: Color = VelarisColor.SurfaceSoft,
    val surfaceSubtle: Color = VelarisColor.SurfaceSubtle,
    val controlSurface: Color = VelarisColor.ControlSurface,
    val stroke: Color = goldSoft,
    val trackInactive: Color = VelarisColor.TrackInactive,
)

@Immutable
data class VelarisAlpha(
    val textPrimary: Float = 0.92f,
    val textSecondary: Float = 0.8f,
    val textMuted: Float = 0.70f,
    val icon: Float = 0.78f,
    val iconMuted: Float = 0.62f,
    val stroke: Float = 0.14f,
    val strokeMedium: Float = 0.4f,
    val strokeStrong: Float = 0.6f,
    val panelScrim: Float = 0.14f,
    val glowIdle: Float = 0.18f,
    val glowActive: Float = 0.28f,
    val seekGlowIdle: Float = 0.28f,
    val seekGlowActive: Float = 0.48f,
)

@Immutable
data class VelarisRadii(
    val panel: Dp = 20.dp,
    val card: Dp = 28.dp,
    val pill: Dp = 50.dp,
    val thumbnail: Dp = 8.dp,
    val badge: Dp = 6.dp,
)

@Immutable
data class VelarisBlur(val button: Dp = 6.dp, val panel: Dp = 4.dp, val dialog: Dp = 4.dp)

@Immutable
data class VelarisSpacing(
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val xLarge: Dp = 24.dp,
    val edgeSmall: Dp = 16.dp,
    val edgeMedium: Dp = 24.dp,
    val edgeLarge: Dp = 32.dp,
)

@Immutable
data class VelarisSize(
    val stroke: Dp = 1.dp,
    val iconSmall: Dp = 18.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconXLarge: Dp = 36.dp,
    val controlCompact: Dp = 48.dp,
    val controlSmall: Dp = 40.dp,
    val controlMedium: Dp = 52.dp,
    val panelHandleWidth: Dp = 56.dp,
    val panelHandleHeight: Dp = 5.dp,
    val stepIndicator: Dp = 26.dp,
    val stepLineWidth: Dp = 80.dp,
    val dialogWidth: Dp = 280.dp,
)

@Immutable
data class VelarisTypography(
    val display: TextUnit = VelarisFontSize.Display,
    val valueLarge: TextUnit = VelarisFontSize.ValueLarge,
    val title: TextUnit = VelarisFontSize.Title,
    val sectionTitle: TextUnit = VelarisFontSize.SectionTitle,
    val tab: TextUnit = VelarisFontSize.Label,
    val label: TextUnit = VelarisFontSize.Label,
    val body: TextUnit = VelarisFontSize.Body,
    val bodySmall: TextUnit = VelarisFontSize.BodySmall,
    val caption: TextUnit = VelarisFontSize.Caption,
    val micro: TextUnit = VelarisFontSize.Micro,
    val controlValue: TextUnit = VelarisFontSize.ControlValue,
    val subtitle: TextUnit = VelarisFontSize.Subtitle,
)

@Immutable
data class VelarisGlow(
    val circleRadiusScale: Float = 0.62f,
    val iconMinAlpha: Float = 0.16f,
    val iconMaxAlpha: Float = 0.42f,
    val seekHighlightAlpha: Float = 0.16f,
    val controlBlurRadius: Dp = 10.dp,
    val buttonBlurRadius: Dp = 4.dp,
    val panelBlurRadius: Dp = 10.dp,
)

@Immutable
data class VelarisBrushes(
    val selectedPill: Brush = Brush.horizontalGradient(
        listOf(
            VelarisColor.GradientPillStart,
            VelarisColor.GradientPillEnd,
        ),
    ),
    val sceneOverlay: Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF07131A).copy(alpha = 0.40f),
            0.35f to Color(0xFF0B1620).copy(alpha = 0.12f),
            0.68f to Color(0xFF0E1822).copy(alpha = 0.24f),
            1.00f to Color(0xFF060D13).copy(alpha = 0.56f),
        ),
    ),
    val glassSurface: Brush = Brush.verticalGradient(
        listOf(
            VelarisColor.GradientGlassStart,
            VelarisColor.GradientGlassEnd,
        ),
    ),
)

val DefaultVelarisUiSpec = VelarisUiSpec()

val LocalVelarisUiSpec = staticCompositionLocalOf { DefaultVelarisUiSpec }

@Composable
fun ProvideVelarisUiSpec(spec: VelarisUiSpec, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalVelarisUiSpec provides spec,
        content = content,
    )
}
