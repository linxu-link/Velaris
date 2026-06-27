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
package com.wujia.feature.settings.impl.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.theme.VelarisThemePreset
import com.wujia.foundation.ui.R

@Composable
internal fun ThemeSettingsDialog(
    selectedPreset: VelarisThemePreset,
    onThemeSelected: (VelarisThemePreset) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val spec = VelarisTheme.spec

    SettingsDialogContent {
        Text(
            text = stringResource(R.string.settings_theme),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.subtitle,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(spec.spacing.large))

        Text(
            text = stringResource(R.string.settings_theme_dialog_desc),
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.bodySmall,
        )

        Spacer(Modifier.height(spec.spacing.medium))

        SettingsRadioOption(
            title = stringResource(R.string.settings_theme_gold),
            description = stringResource(R.string.settings_theme_gold_desc),
            selected = selectedPreset == VelarisThemePreset.Gold,
            onClick = { onThemeSelected(VelarisThemePreset.Gold) },
        )
        SettingsRadioOption(
            title = stringResource(R.string.settings_theme_ocean),
            description = stringResource(R.string.settings_theme_ocean_desc),
            selected = selectedPreset == VelarisThemePreset.Ocean,
            onClick = { onThemeSelected(VelarisThemePreset.Ocean) },
        )
        SettingsRadioOption(
            title = stringResource(R.string.settings_theme_forest),
            description = stringResource(R.string.settings_theme_forest_desc),
            selected = selectedPreset == VelarisThemePreset.Forest,
            onClick = { onThemeSelected(VelarisThemePreset.Forest) },
        )
        SettingsRadioOption(
            title = stringResource(R.string.settings_theme_twilight),
            description = stringResource(R.string.settings_theme_twilight_desc),
            selected = selectedPreset == VelarisThemePreset.Twilight,
            onClick = { onThemeSelected(VelarisThemePreset.Twilight) },
        )

        Spacer(Modifier.height(spec.spacing.large))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.common_close),
                    color = spec.colors.textSecondary.copy(alpha = 0.68f),
                    fontSize = spec.typography.label,
                )
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun ThemeSettingsDialogPreview() {
    ThemeSettingsDialog(
        selectedPreset = VelarisThemePreset.Gold,
        onThemeSelected = {},
        onDismissRequest = {},
    )
}
