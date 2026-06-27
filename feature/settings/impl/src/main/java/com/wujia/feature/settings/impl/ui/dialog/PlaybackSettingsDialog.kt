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
import com.wujia.foundation.player.VelarisPlayerPerformanceProfile
import com.wujia.foundation.ui.R

@Composable
internal fun PlaybackSettingsDialog(
    selectedProfile: VelarisPlayerPerformanceProfile,
    onProfileSelected: (VelarisPlayerPerformanceProfile) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val spec = VelarisTheme.spec

    SettingsDialogContent {
        Text(
            text = stringResource(R.string.settings_playback),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.subtitle,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(spec.spacing.large))

        Text(
            text = stringResource(R.string.playback_select_profile),
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.bodySmall,
        )

        Spacer(Modifier.height(spec.spacing.medium))

        SettingsRadioOption(
            title = stringResource(R.string.playback_power_saver),
            description = stringResource(R.string.playback_power_saver_desc),
            selected = selectedProfile == VelarisPlayerPerformanceProfile.PowerSaver,
            onClick = { onProfileSelected(VelarisPlayerPerformanceProfile.PowerSaver) },
        )
        SettingsRadioOption(
            title = stringResource(R.string.playback_balanced),
            description = stringResource(R.string.playback_balanced_desc),
            selected = selectedProfile == VelarisPlayerPerformanceProfile.Balanced,
            onClick = { onProfileSelected(VelarisPlayerPerformanceProfile.Balanced) },
        )
        SettingsRadioOption(
            title = stringResource(R.string.playback_quality),
            description = stringResource(R.string.playback_quality_desc),
            selected = selectedProfile == VelarisPlayerPerformanceProfile.Quality,
            onClick = { onProfileSelected(VelarisPlayerPerformanceProfile.Quality) },
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
private fun PlaybackSettingsDialogPreview() {
    PlaybackSettingsDialog(
        selectedProfile = VelarisPlayerPerformanceProfile.Balanced,
        onProfileSelected = {},
        onDismissRequest = {},
    )
}
