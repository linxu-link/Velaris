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

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.SeekbarControlPanel

@Composable
fun SceneSoundPanel(
    items: List<SoundControlItem>,
    onItemValueChange: (index: Int, value: Float) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.scene_control_sound),
    minWidth: Dp,
    minHeight: Dp,
    maxHeight: Dp,
    titleFontSize: TextUnit,
    itemTitleFontSize: TextUnit,
    valueFontSize: TextUnit,
    compact: Boolean = false,
) {
    SeekbarControlPanel(
        items = items,
        title = title,
        onValueChange = onItemValueChange,
        minWidth = minWidth,
        minHeight = minHeight,
        maxHeight = maxHeight,
        titleFontSize = titleFontSize,
        itemTitleFontSize = itemTitleFontSize,
        valueFontSize = valueFontSize,
        compact = compact,
        modifier = if (compact) {
            modifier
        } else {
            modifier.heightIn(min = minHeight, max = maxHeight)
        },
    )
}

@LandscapePreviews
@Composable
private fun SceneSoundPanelPreview() {
    val spec = VelarisTheme.spec
    SceneSoundPanel(
        items = sampleSoundItems(),
        onItemValueChange = { _, _ -> },
        minWidth = 280.dp,
        minHeight = 172.dp,
        maxHeight = 208.dp,
        titleFontSize = spec.typography.body,
        itemTitleFontSize = spec.typography.label,
        valueFontSize = spec.typography.label,
        modifier = Modifier.width(300.dp),
    )
}
