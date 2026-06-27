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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme

@LandscapePreviews
@Composable
private fun StepHeaderPreview() {
    StepHeader()
}

@Composable
fun StepHeader(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.step_add_scene),
    stepTitle: String = stringResource(R.string.scene_edit_step1),
    nextText: String? = null,
    onNextClick: () -> Unit = {},
) {
    val spec = VelarisTheme.spec
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = spec.colors.goldSoft.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.title,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(spec.spacing.large))
        Text(
            text = stepTitle,
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.body,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        nextText?.let {
            Text(
                text = it,
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.body,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.velarisClickable { onNextClick() },
            )
        }
    }
}
