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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme

@LandscapePreviews
@Composable
private fun StepIndicatorPreview() {
    StepIndicator(1)
}

@Composable
fun StepIndicator(currentStep: Int, onStepSelected: (Int) -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepItem(
            number = 1,
            title = stringResource(R.string.scene_edit_select_material),
            selected = currentStep == 1,
            onClick = { onStepSelected(1) },
            modifier = Modifier.weight(1f),
            vertical = isTablet,
        )

        StepLine(modifier = Modifier.weight(0.55f))

        StepItem(
            number = 2,
            title = stringResource(R.string.scene_edit_select_sound),
            selected = currentStep == 2,
            onClick = { onStepSelected(2) },
            modifier = Modifier.weight(1f),
            vertical = isTablet,
        )

        StepLine(modifier = Modifier.weight(0.55f))

        StepItem(
            number = 3,
            title = stringResource(R.string.scene_edit_particle_effect),
            selected = currentStep == 3,
            onClick = { onStepSelected(3) },
            modifier = Modifier.weight(1f),
            vertical = isTablet,
        )

        StepLine(modifier = Modifier.weight(0.55f))

        StepItem(
            number = 4,
            title = stringResource(R.string.scene_edit_preview_save),
            selected = currentStep == 4,
            onClick = { onStepSelected(4) },
            modifier = Modifier.weight(1f),
            vertical = isTablet,
        )
    }
}

@Composable
private fun StepLine(modifier: Modifier = Modifier) {
    val spec = VelarisTheme.spec
    Box(
        modifier = modifier
            .padding(horizontal = spec.spacing.large)
            .height(2.dp)
            .fillMaxWidth()
            .background(spec.colors.gold),
    )
}

@Composable
private fun StepItem(
    number: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    val spec = VelarisTheme.spec
    if (vertical) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.velarisClickable(onClick = onClick),
        ) {
            StepItemDot(number = number, selected = selected)
            Spacer(Modifier.height(spec.spacing.small))
            StepItemLabel(title = title, selected = selected)
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.velarisClickable(onClick = onClick),
        ) {
            StepItemDot(number = number, selected = selected)
            Spacer(Modifier.width(spec.spacing.large))
            StepItemLabel(title = title, selected = selected)
        }
    }
}

@Composable
private fun StepItemDot(number: Int, selected: Boolean) {
    val spec = VelarisTheme.spec
    Box(
        modifier = Modifier
            .size(spec.size.stepIndicator)
            .clip(CircleShape)
            .background(
                if (selected) spec.colors.gold else Color.Transparent,
            )
            .border(
                width = spec.size.stroke,
                color = if (selected) {
                    spec.colors.gold
                } else {
                    spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong)
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = if (selected) {
                spec.colors.onGold
            } else {
                spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
            },
            fontSize = spec.typography.title,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StepItemLabel(title: String, selected: Boolean) {
    val spec = VelarisTheme.spec
    Text(
        text = title,
        color = if (selected) {
            spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary)
        } else {
            spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
        },
        fontSize = spec.typography.sectionTitle,
        fontWeight = FontWeight.SemiBold,
    )
}
