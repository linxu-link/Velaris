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
package com.wujia.feature.sceneedit.impl.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

internal data class SceneEditLayoutProfile(
    val edgePadding: Dp,
    val verticalSpacing: Dp,
)

internal fun sceneEditLayoutProfile(
    windowSizeClass: WindowSizeClass,
    maxWidth: Dp,
    maxHeight: Dp,
): SceneEditLayoutProfile {
    val compactHeight =
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) ||
            maxHeight < 420.dp
    val compactWidth =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ||
            maxWidth < 600.dp

    return SceneEditLayoutProfile(
        edgePadding = when {
            compactWidth -> 8.dp
            maxWidth >= 1000.dp -> 16.dp
            else -> 12.dp
        },
        verticalSpacing = if (compactHeight) 6.dp else 8.dp,
    )
}
