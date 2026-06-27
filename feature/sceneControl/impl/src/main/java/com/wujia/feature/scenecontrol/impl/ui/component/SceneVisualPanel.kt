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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.SeekbarControlRow

@Composable
fun SceneVisualPanel(
    items: List<SoundControlItem>,
    onItemValueChange: (index: Int, value: Float) -> Unit,
    countdownClockPosition: SceneCountdownClockPosition,
    onCountdownClockPositionChange: (SceneCountdownClockPosition) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.scene_control_settings),
    minWidth: Dp,
    minHeight: Dp,
    maxHeight: Dp,
    titleFontSize: TextUnit,
    itemTitleFontSize: TextUnit,
    valueFontSize: TextUnit,
) {
    val spec = VelarisTheme.spec
    val resolvedTitleFontSize = titleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedItemTitleFontSize = itemTitleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedValueFontSize = valueFontSize.takeOrElse { spec.typography.subtitle }
    val positionOptions = listOf(
        SceneCountdownClockPosition.Center to stringResource(R.string.scene_control_countdown_clock_position_center),
        SceneCountdownClockPosition.TopStart to stringResource(
            R.string.scene_control_countdown_clock_position_top_left,
        ),
        SceneCountdownClockPosition.BottomStart to
            stringResource(R.string.scene_control_countdown_clock_position_bottom_left),
        SceneCountdownClockPosition.TopEnd to stringResource(R.string.scene_control_countdown_clock_position_top_right),
        SceneCountdownClockPosition.BottomEnd to
            stringResource(R.string.scene_control_countdown_clock_position_bottom_right),
    )

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .height(maxHeight)
            .clip(RoundedCornerShape(spec.radii.panel))
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
                shape = RoundedCornerShape(spec.radii.panel),
            )
            .padding(vertical = spec.spacing.edgeSmall, horizontal = spec.spacing.large),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(spec.spacing.xSmall))

            Text(
                text = title,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = resolvedTitleFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(spec.spacing.medium))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spec.spacing.small),
        ) {
            items.forEachIndexed { index, item ->
                SeekbarControlRow(
                    item = item,
                    onValueChange = { onItemValueChange(index, it) },
                    titleFontSize = resolvedItemTitleFontSize,
                    valueFontSize = resolvedValueFontSize,
                )
            }
        }

        Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spec.size.stroke)
                .background(spec.colors.stroke.copy(alpha = spec.alpha.stroke)),
        )

        Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

        Text(
            text = stringResource(R.string.scene_control_countdown_clock_position),
            color = spec.colors.textSecondary.copy(alpha = 0.78f),
            fontSize = resolvedItemTitleFontSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(spec.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spec.spacing.xSmall),
        ) {
            positionOptions.forEach { (position, label) ->
                TimerOptionButton(
                    text = label,
                    selected = position == countdownClockPosition,
                    onClick = { onCountdownClockPositionChange(position) },
                    fontSize = resolvedItemTitleFontSize,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun SceneVisualPanelPreview() {
    val spec = VelarisTheme.spec
    var position by remember { mutableStateOf(SceneCountdownClockPosition.Center) }

    SceneVisualPanel(
        items = sampleLightItems(),
        onItemValueChange = { _, _ -> },
        countdownClockPosition = position,
        onCountdownClockPositionChange = { position = it },
        minWidth = 280.dp,
        minHeight = 172.dp,
        maxHeight = 228.dp,
        titleFontSize = spec.typography.body,
        itemTitleFontSize = spec.typography.label,
        valueFontSize = spec.typography.label,
        modifier = Modifier.width(300.dp),
    )
}
