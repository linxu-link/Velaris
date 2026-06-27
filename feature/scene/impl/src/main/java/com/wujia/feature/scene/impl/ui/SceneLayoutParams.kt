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
package com.wujia.feature.scene.impl.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.wujia.foundation.designsystem.layout.LandscapeLayoutType
import com.wujia.foundation.designsystem.theme.VelarisFontSize

/**
 * SceneScreen 主页面布局参数
 */
internal val LandscapeLayoutType.sceneEdgePadding: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 12.dp
        LandscapeLayoutType.Medium -> 16.dp
        LandscapeLayoutType.Large -> 20.dp
    }

internal val LandscapeLayoutType.sceneTitleFontSize: TextUnit
    get() = when (this) {
        LandscapeLayoutType.Small -> VelarisFontSize.SceneTitleSmall
        LandscapeLayoutType.Medium -> VelarisFontSize.SceneTitleMedium
        LandscapeLayoutType.Large -> VelarisFontSize.SceneTitleLarge
    }

internal val LandscapeLayoutType.sceneModeTabsWidth: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 180.dp
        LandscapeLayoutType.Medium -> 200.dp
        LandscapeLayoutType.Large -> 220.dp
    }

internal val LandscapeLayoutType.sceneControlSpacing: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 10.dp
        LandscapeLayoutType.Medium -> 14.dp
        LandscapeLayoutType.Large -> 16.dp
    }

internal val LandscapeLayoutType.sceneControlHeight: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 50.dp
        LandscapeLayoutType.Medium -> 54.dp
        LandscapeLayoutType.Large -> 58.dp
    }

internal val LandscapeLayoutType.sceneControlIconSize: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 40.dp
        LandscapeLayoutType.Medium -> 42.dp
        LandscapeLayoutType.Large -> 44.dp
    }

internal data class SceneLayoutProfile(
    val layoutType: LandscapeLayoutType,
    val edgePadding: Dp,
    val titleFontSize: TextUnit,
    val modeTabsWidth: Dp,
    val compactChrome: Boolean,
    val controlHeight: Dp,
    val controlIconSize: Dp,
    val controlSpacing: Dp,
    val controlPanelHeight: Dp,
    val editPanelHeight: Dp,
    val listPanelHeight: Dp,
)

internal fun sceneLayoutProfile(windowSizeClass: WindowSizeClass, maxWidth: Dp, maxHeight: Dp): SceneLayoutProfile {
    val windowCompactWidth =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val windowExpandedWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val windowCompactHeight =
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val layoutType = when {
        windowCompactWidth -> LandscapeLayoutType.Small
        maxWidth < 720.dp -> LandscapeLayoutType.Small
        maxWidth < 1000.dp -> LandscapeLayoutType.Medium
        else -> LandscapeLayoutType.Large
    }
    val compactWidth = windowCompactWidth || maxWidth < 600.dp
    val compactHeight = windowCompactHeight || maxHeight < 420.dp
    val compactChrome = compactWidth || compactHeight
    val expandedWidth = windowExpandedWidth
    val largeWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(SCENE_LARGE_WIDTH_BREAKPOINT_DP) ||
            (expandedWidth && maxWidth >= SCENE_LARGE_WIDTH_BREAKPOINT_DP.dp)
    val xLargeWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(SCENE_XLARGE_WIDTH_BREAKPOINT_DP) ||
            (expandedWidth && maxWidth >= SCENE_XLARGE_WIDTH_BREAKPOINT_DP.dp)

    val edgePadding = when {
        compactWidth -> 12.dp
        compactHeight -> 14.dp
        largeWidth -> 24.dp
        else -> layoutType.sceneEdgePadding
    }
    val controlPanelVisualLimit = when {
        compactWidth -> 360.dp
        compactHeight -> 340.dp
        xLargeWidth -> 460.dp
        largeWidth -> 440.dp
        expandedWidth -> 420.dp
        layoutType == LandscapeLayoutType.Large -> 420.dp
        layoutType == LandscapeLayoutType.Medium -> 400.dp
        else -> 360.dp
    }
    val controlPanelHeightRatio = when {
        compactWidth -> 0.90f
        compactHeight -> 0.92f
        xLargeWidth -> 0.56f
        expandedWidth -> 0.58f
        layoutType == LandscapeLayoutType.Large -> 0.58f
        else -> 0.85f
    }
    val editPanelVisualLimit = when {
        compactWidth -> 360.dp
        compactHeight -> 340.dp
        xLargeWidth -> 560.dp
        largeWidth -> 540.dp
        expandedWidth -> 500.dp
        layoutType == LandscapeLayoutType.Large -> 500.dp
        layoutType == LandscapeLayoutType.Medium -> 440.dp
        else -> 360.dp
    }
    val editPanelHeightRatio = when {
        compactWidth -> 0.90f
        compactHeight -> 0.92f
        xLargeWidth -> 0.68f
        expandedWidth -> 0.64f
        layoutType == LandscapeLayoutType.Large -> 0.64f
        else -> 0.85f
    }

    return SceneLayoutProfile(
        layoutType = layoutType,
        edgePadding = edgePadding,
        titleFontSize = when {
            compactWidth -> VelarisFontSize.SceneTitleCompactWidth
            compactHeight -> VelarisFontSize.SceneTitleCompactHeight
            largeWidth -> VelarisFontSize.SceneTitleLarge
            else -> layoutType.sceneTitleFontSize
        },
        modeTabsWidth = when {
            compactWidth -> 188.dp
            compactHeight -> 188.dp
            largeWidth -> 232.dp
            else -> layoutType.sceneModeTabsWidth
        },
        compactChrome = compactChrome,
        controlHeight = when {
            compactChrome -> 50.dp
            largeWidth -> 58.dp
            else -> layoutType.sceneControlHeight
        },
        controlIconSize = when {
            compactChrome -> 42.dp
            largeWidth -> 50.dp
            else -> layoutType.sceneControlIconSize
        },
        controlSpacing = when {
            compactChrome -> 8.dp
            largeWidth -> 16.dp
            else -> layoutType.sceneControlSpacing
        },
        controlPanelHeight = minOf(controlPanelVisualLimit, maxHeight * controlPanelHeightRatio),
        editPanelHeight = minOf(editPanelVisualLimit, maxHeight * editPanelHeightRatio),
        listPanelHeight = minOf(controlPanelVisualLimit, maxHeight * controlPanelHeightRatio),
    )
}

private const val SCENE_LARGE_WIDTH_BREAKPOINT_DP = 1200
private const val SCENE_XLARGE_WIDTH_BREAKPOINT_DP = 1440
