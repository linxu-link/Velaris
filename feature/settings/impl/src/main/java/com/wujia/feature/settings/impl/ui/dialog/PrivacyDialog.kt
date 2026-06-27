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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R

@Composable
internal fun PrivacyDialog(
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    consentError: String?,
    onShowPrivacyOptions: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val spec = VelarisTheme.spec

    SettingsDialogContent {
        Text(
            text = stringResource(R.string.settings_privacy),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.subtitle,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(spec.spacing.large))

        val bodyStyle = spec.typography.bodySmall
        val bodyColor = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)

        Text(
            text = stringResource(R.string.privacy_no_collect),
            color = bodyColor,
            fontSize = bodyStyle,
        )
        Spacer(Modifier.height(spec.spacing.small))
        Text(
            text = stringResource(R.string.privacy_google_ads),
            color = bodyColor,
            fontSize = bodyStyle,
        )
        Spacer(Modifier.height(spec.spacing.small))
        Text(
            text = stringResource(R.string.privacy_consent_management),
            color = bodyColor,
            fontSize = bodyStyle,
        )

        if (consentError != null) {
            Spacer(Modifier.height(spec.spacing.medium))
            Text(
                text = stringResource(R.string.privacy_consent_status, consentError),
                color = spec.colors.gold,
                fontSize = spec.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(spec.spacing.large))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (privacyOptionsRequired) {
                TextButton(onClick = onShowPrivacyOptions) {
                    Text(
                        text = stringResource(R.string.privacy_manage_options),
                        color = spec.colors.goldBright,
                        fontSize = spec.typography.label,
                        fontWeight = FontWeight.Medium,
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

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
private fun PrivacyDialogPreview() {
    PrivacyDialog(
        canRequestAds = true,
        privacyOptionsRequired = true,
        consentError = "Consent form unavailable",
        onShowPrivacyOptions = {},
        onDismissRequest = {},
    )
}
