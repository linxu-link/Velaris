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

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wujia.foundation.model.theme.VelarisThemePreset

private data class VelarisPresetPalette(
    val accent: Color,
    val accentSoft: Color,
    val accentBright: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val iconMuted: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val surfaceSubtle: Color,
    val controlSurface: Color,
    val stroke: Color,
    val trackInactive: Color,
    val pillStart: Color,
    val pillEnd: Color,
    val glassStart: Color,
    val glassEnd: Color,
    val background: Color,
    val backgroundDeep: Color,
)

internal fun VelarisThemePreset.themeBundle(darkTheme: Boolean): VelarisThemeBundle {
    val palette = palette()
    val uiSpec = palette.toUiSpec()
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.surfaceSoft,
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.accentSoft,
            onSecondary = palette.onAccent,
            secondaryContainer = palette.controlSurface,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.iconMuted,
            onTertiary = palette.onAccent,
            tertiaryContainer = palette.surfaceSubtle,
            onTertiaryContainer = palette.textSecondary,
            background = palette.backgroundDeep,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceSoft,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.stroke,
            inverseSurface = palette.accentBright,
            inverseOnSurface = palette.onAccent,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentBright,
            onPrimaryContainer = palette.onAccent,
            secondary = palette.accentSoft,
            onSecondary = palette.onAccent,
            secondaryContainer = palette.surfaceSubtle,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.iconMuted,
            onTertiary = palette.onAccent,
            tertiaryContainer = palette.surfaceSoft,
            onTertiaryContainer = palette.textPrimary,
            background = palette.accentBright,
            onBackground = palette.onAccent,
            surface = Color(0xFFF7FAFC),
            onSurface = palette.onAccent,
            surfaceVariant = Color(0xFFE8EEF2),
            onSurfaceVariant = palette.textSecondary,
            outline = palette.stroke,
            inverseSurface = palette.surface,
            inverseOnSurface = palette.textPrimary,
        )
    }

    return VelarisThemeBundle(
        colorScheme = colorScheme,
        uiSpec = uiSpec,
        gradientColors = VelarisGradientColors(
            top = palette.background,
            bottom = palette.accent.copy(alpha = if (darkTheme) 0.24f else 0.16f),
            container = colorScheme.surface,
        ),
        backgroundTheme = VelarisBackgroundTheme(
            color = colorScheme.surface,
            tonalElevation = 2.dp,
        ),
        tintTheme = VelarisTintTheme(
            iconTint = palette.accent,
        ),
    )
}

fun VelarisThemePreset.previewSwatches(): List<Color> {
    val palette = palette()
    return listOf(
        palette.accent,
        palette.accentSoft,
        palette.trackInactive,
    )
}

private fun VelarisThemePreset.palette(): VelarisPresetPalette = when (this) {
    VelarisThemePreset.Gold -> VelarisPresetPalette(
        accent = Color(0xFFE7C878),
        accentSoft = Color(0xFFF6E2AA),
        accentBright = Color(0xFFFFD98A),
        onAccent = Color(0xFF231A0E),
        textPrimary = Color(0xFFFFF3D6),
        textSecondary = Color(0xFFEADFC8),
        textMuted = Color(0xFFB09E79),
        iconMuted = Color(0xFF8F8064),
        surface = Color(0xB317130D),
        surfaceSoft = Color(0x661B1510),
        surfaceSubtle = Color(0x521E1812),
        controlSurface = Color(0x2644432B),
        stroke = Color(0xFFF6E2AA),
        trackInactive = Color(0xFF6F7A4F),
        pillStart = Color(0x80FFD98A),
        pillEnd = Color(0x80A8845C),
        glassStart = Color(0x34261F16),
        glassEnd = Color(0x240E0A06),
        background = Color(0xFF17130D),
        backgroundDeep = Color(0xFF0E0A06),
    )

    VelarisThemePreset.Ocean -> VelarisPresetPalette(
        accent = Color(0xFF7EDDE3),
        accentSoft = Color(0xFFB9F6F2),
        accentBright = Color(0xFFEAF8F6),
        onAccent = Color(0xFF102325),
        textPrimary = Color(0xFFF5F2E8),
        textSecondary = Color(0xFFD9E4DF),
        textMuted = Color(0xFF9DB1AB),
        iconMuted = Color(0xFF8EA7A2),
        surface = Color(0xB3071516),
        surfaceSoft = Color(0x660A1A1B),
        surfaceSubtle = Color(0x520D2021),
        controlSurface = Color(0x26305B5F),
        stroke = Color(0xFF7AA9A6),
        trackInactive = Color(0xFF244E53),
        pillStart = Color(0x80B9F6F2),
        pillEnd = Color(0x802E7F8A),
        glassStart = Color(0x34122526),
        glassEnd = Color(0x24071516),
        background = Color(0xFF102425),
        backgroundDeep = Color(0xFF071516),
    )

    VelarisThemePreset.Forest -> VelarisPresetPalette(
        accent = Color(0xFF8CBFA8),
        accentSoft = Color(0xFFA6CBB4),
        accentBright = Color(0xFFF3FAF6),
        onAccent = Color(0xFF10221A),
        textPrimary = Color(0xFFF3FAF6),
        textSecondary = Color(0xFFA6CBB4),
        textMuted = Color(0xFFC8DAD2),
        iconMuted = Color(0xFF9AB8AE),
        surface = Color(0xB3111D18),
        surfaceSoft = Color(0x66101A16),
        surfaceSubtle = Color(0x52111814),
        controlSurface = Color(0x26303D34),
        stroke = Color(0xFFA6CBB4),
        trackInactive = Color(0xFF283C33),
        pillStart = Color(0x808CD0B0),
        pillEnd = Color(0x806B8D7C),
        glassStart = Color(0x3413211A),
        glassEnd = Color(0x240A140F),
        background = Color(0xFF101F17),
        backgroundDeep = Color(0xFF07160F),
    )

    VelarisThemePreset.Twilight -> VelarisPresetPalette(
        accent = Color(0xFF8FA9FF),
        accentSoft = Color(0xFFB7C7FF),
        accentBright = Color(0xFFA8D8FF),
        onAccent = Color(0xFF111A28),
        textPrimary = Color(0xFFF3F7FF),
        textSecondary = Color(0xFFD6DEEE),
        textMuted = Color(0xFF919FBA),
        iconMuted = Color(0xFF73819B),
        surface = Color(0xB3131C28),
        surfaceSoft = Color(0x66111A24),
        surfaceSubtle = Color(0x52131C29),
        controlSurface = Color(0x26222E40),
        stroke = Color(0xFFDCE6FF),
        trackInactive = Color(0xFF344256),
        pillStart = Color(0x80B7C7FF),
        pillEnd = Color(0x808FA9FF),
        glassStart = Color(0x34182533),
        glassEnd = Color(0x240A1018),
        background = Color(0xFF131C28),
        backgroundDeep = Color(0xFF0A1018),
    )
}

private fun VelarisPresetPalette.toUiSpec(): VelarisUiSpec = DefaultVelarisUiSpec.copy(
    colors = VelarisColors(
        gold = accent,
        goldSoft = accentSoft,
        goldBright = accentBright,
        onGold = onAccent,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = textMuted,
        iconMuted = iconMuted,
        surface = surface,
        surfaceSoft = surfaceSoft,
        surfaceSubtle = surfaceSubtle,
        controlSurface = controlSurface,
        stroke = stroke,
        trackInactive = trackInactive,
    ),
    brushes = VelarisBrushes(
        selectedPill = Brush.horizontalGradient(
            listOf(
                pillStart,
                pillEnd,
            ),
        ),
        sceneOverlay = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to backgroundDeep.copy(alpha = 0.40f),
                0.35f to background.copy(alpha = 0.12f),
                0.68f to background.copy(alpha = 0.24f),
                1.00f to backgroundDeep.copy(alpha = 0.56f),
            ),
        ),
        glassSurface = Brush.verticalGradient(
            listOf(
                glassStart,
                glassEnd,
            ),
        ),
    ),
)
