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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import com.wujia.foundation.model.soundcontrol.SoundControlItem

@Composable
fun ButtonControlPanel(
    items: List<SoundControlItem>,
    onValueChange: (index: Int, value: Float) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.timer_label),
) {
    val spec = VelarisTheme.spec
    val panelShape = RoundedCornerShape(spec.radii.panel)
    Box(
        modifier = modifier
            .clip(panelShape)
            .velarisGlassBlur(
                shape = panelShape,
                blurRadius = spec.blur.panel,
            )
            .background(spec.colors.surfaceSoft)
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                shape = panelShape,
            )
            .padding(vertical = spec.spacing.medium, horizontal = spec.spacing.large),
    ) {
    }
}

@Preview
@Composable
private fun ButtonControlPanelPreview() {
}
