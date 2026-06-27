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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R

/**
 * 项目 GitHub 地址（关于页可点击跳转）。
 * 注意：上架/发布前请替换为真实的仓库地址。
 */
private const val GITHUB_URL = "https://github.com/user/velaris"

@Composable
internal fun AboutDialog(
    versionName: String,
    versionCode: Int,
    onDismissRequest: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val uriHandler = LocalUriHandler.current

    SettingsDialogContent {
        Text(
            text = stringResource(R.string.app_name),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.subtitle,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(spec.spacing.small))

        Text(
            text = stringResource(R.string.about_tagline),
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.bodySmall,
        )

        Spacer(Modifier.height(spec.spacing.large))

        AboutInfoRow(stringResource(R.string.about_version), versionName)
        AboutInfoRow(stringResource(R.string.about_author), "WuJia")

        Spacer(Modifier.height(spec.spacing.small))

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

@Composable
private fun AboutInfoRow(label: String, value: String) {
    val spec = VelarisTheme.spec
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spec.spacing.xSmall),
    ) {
        Text(
            text = label,
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.bodySmall,
            modifier = Modifier.width(60.dp),
        )
        Text(
            text = value,
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.bodySmall,
        )
    }
}

@LandscapePreviews
@Composable
private fun AboutDialogPreview() {
    AboutDialog(
        versionName = "1.0.0",
        versionCode = 7,
        onDismissRequest = {},
    )
}
