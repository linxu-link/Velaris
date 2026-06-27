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
package com.wujia.foundation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur

@Composable
fun VelarisDialogPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    scrollable: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit,
) {
    val spec = VelarisTheme.spec
    val shape = RoundedCornerShape(spec.radii.panel)
    val resolvedContentPadding = contentPadding ?: PaddingValues(spec.spacing.xLarge)
    val panelModifier = modifier
        .clip(shape)
        .velarisGlassBlur(
            shape = shape,
            blurRadius = spec.blur.dialog,
            surfaceAlpha = 0.2f,
        )
        .border(
            width = spec.size.stroke,
            color = spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
            shape = shape,
        )
        .padding(resolvedContentPadding)

    if (scrollable) {
        Column(
            modifier = panelModifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    } else {
        Column(
            modifier = panelModifier,
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    }
}
