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
package com.wujia.feature.scenecontrol.impl.ui.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.layout.LandscapeLayoutType
import com.wujia.foundation.designsystem.theme.VelarisFontSize

/**
 * 控制面板布局参数
 */

// ─── Public: used by SceneScreen for PlayerSoundDialog dimensions ───

public val LandscapeLayoutType.controlPanelMinWidth: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 260.dp
        LandscapeLayoutType.Medium -> 280.dp
        LandscapeLayoutType.Large -> 320.dp
    }

public val LandscapeLayoutType.controlPanelMinHeight: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 176.dp
        LandscapeLayoutType.Medium -> 192.dp
        LandscapeLayoutType.Large -> 200.dp
    }

public val LandscapeLayoutType.controlPanelMaxHeight: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 228.dp
        LandscapeLayoutType.Medium -> 248.dp
        LandscapeLayoutType.Large -> 260.dp
    }

public val LandscapeLayoutType.particlePanelMaxHeight: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 238.dp
        LandscapeLayoutType.Medium -> 258.dp
        LandscapeLayoutType.Large -> 270.dp
    }

public val LandscapeLayoutType.controlTitleFontSize: TextUnit
    get() = when (this) {
        LandscapeLayoutType.Small -> VelarisFontSize.ControlTitleSmall
        LandscapeLayoutType.Medium -> VelarisFontSize.ControlTitleMedium
        LandscapeLayoutType.Large -> VelarisFontSize.ControlTitleLarge
    }

public val LandscapeLayoutType.controlItemTitleFontSize: TextUnit
    get() = when (this) {
        LandscapeLayoutType.Small -> VelarisFontSize.ControlItemTitleSmall
        LandscapeLayoutType.Medium -> VelarisFontSize.ControlItemTitleMedium
        LandscapeLayoutType.Large -> VelarisFontSize.ControlItemTitleLarge
    }

public val LandscapeLayoutType.controlValueFontSize: TextUnit
    get() = when (this) {
        LandscapeLayoutType.Small -> VelarisFontSize.ControlValueSmall
        LandscapeLayoutType.Medium -> VelarisFontSize.ControlValueMedium
        LandscapeLayoutType.Large -> VelarisFontSize.ControlValueLarge
    }

// ─── Internal: used only within sceneControl module ───

internal val LandscapeLayoutType.controlPanelHorizontalPadding: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 12.dp
        LandscapeLayoutType.Medium -> 20.dp
        LandscapeLayoutType.Large -> 28.dp
    }

internal val LandscapeLayoutType.controlPanelSpacing: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 12.dp
        LandscapeLayoutType.Medium -> 20.dp
        LandscapeLayoutType.Large -> 20.dp
    }

internal val LandscapeLayoutType.controlHeaderHeight: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 40.dp
        LandscapeLayoutType.Medium -> 44.dp
        LandscapeLayoutType.Large -> 50.dp
    }

internal val LandscapeLayoutType.controlHeaderBottomSpacing: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 4.dp
        LandscapeLayoutType.Medium -> 8.dp
        LandscapeLayoutType.Large -> 12.dp
    }

internal val LandscapeLayoutType.controlThumbnailSize: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 44.dp
        LandscapeLayoutType.Medium -> 48.dp
        LandscapeLayoutType.Large -> 54.dp
    }

internal val LandscapeLayoutType.controlActionSize: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 32.dp
        LandscapeLayoutType.Medium -> 34.dp
        LandscapeLayoutType.Large -> 38.dp
    }

internal val LandscapeLayoutType.controlSceneNameFontSize: TextUnit
    get() = when (this) {
        LandscapeLayoutType.Small -> VelarisFontSize.ControlSceneNameSmall
        LandscapeLayoutType.Medium -> VelarisFontSize.ControlSceneNameMedium
        LandscapeLayoutType.Large -> VelarisFontSize.ControlSceneNameLarge
    }

internal val LandscapeLayoutType.controlSceneEditIconSize: Dp
    get() = when (this) {
        LandscapeLayoutType.Small -> 16.dp
        LandscapeLayoutType.Medium -> 17.dp
        LandscapeLayoutType.Large -> 18.dp
    }
