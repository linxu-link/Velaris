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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.wujia.foundation.model.theme.VelarisThemePreset

val LocalVelarisThemePreset = staticCompositionLocalOf { VelarisThemePreset.Ocean }

private val M3Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = VelarisFontSize.M3BodyLarge,
        lineHeight = VelarisFontSize.M3BodyLarge * 1.5,
        letterSpacing = VelarisFontSize.M3BodyLarge * 0.03,
    ),
)

/**
 * Velaris 应用主题入口
 *
 * 同时提供 Material3 基础主题和 Velaris 自定义设计系统。
 * Material3 的 colorScheme/Typography 仅作为框架初始化用途，
 * 实际 UI 样式由 [VelarisTheme.spec] 中的自定义 token 控制。
 */
@Composable
fun ProvideVelarisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: VelarisThemePreset = VelarisThemePreset.Ocean,
    content: @Composable () -> Unit,
) {
    val themeBundle = themePreset.themeBundle(darkTheme = darkTheme)

    CompositionLocalProvider(
        LocalVelarisThemePreset provides themePreset,
        LocalVelarisGradientColors provides themeBundle.gradientColors,
        LocalVelarisBackgroundTheme provides themeBundle.backgroundTheme,
        LocalVelarisTintTheme provides themeBundle.tintTheme,
    ) {
        MaterialTheme(
            colorScheme = themeBundle.colorScheme,
            typography = M3Typography,
            content = {
                ProvideVelarisUiSpec(
                    spec = themeBundle.uiSpec,
                    content = content,
                )
            },
        )
    }
}

@androidx.compose.runtime.Immutable
internal data class VelarisThemeBundle(
    val colorScheme: ColorScheme,
    val uiSpec: VelarisUiSpec,
    val gradientColors: VelarisGradientColors,
    val backgroundTheme: VelarisBackgroundTheme,
    val tintTheme: VelarisTintTheme,
)

object VelarisTheme {
    val currentPreset: VelarisThemePreset
        @Composable
        @ReadOnlyComposable
        get() = LocalVelarisThemePreset.current

    val spec: VelarisUiSpec
        @Composable
        @ReadOnlyComposable
        get() = LocalVelarisUiSpec.current

    val gradientColors: VelarisGradientColors
        @Composable
        @ReadOnlyComposable
        get() = LocalVelarisGradientColors.current

    val backgroundTheme: VelarisBackgroundTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalVelarisBackgroundTheme.current

    val tintTheme: VelarisTintTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalVelarisTintTheme.current
}
