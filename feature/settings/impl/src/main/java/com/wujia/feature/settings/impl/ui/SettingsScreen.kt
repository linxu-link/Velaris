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
package com.wujia.feature.settings.impl.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wujia.feature.settings.impl.ui.viewmodel.SettingsViewModel
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.theme.VelarisThemePreset
import com.wujia.foundation.ui.R

/**
 * 隐私政策 URL。
 * 注意：上架前必须替换为真实的公网地址，并确保与 Google Play Console 中 Data safety / 隐私政策声明一致。
 */
private const val PRIVACY_POLICY_URL = "https://example.com/velaris/privacy-policy"

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    SettingsContent(
        modifier = modifier,
        selectedThemePreset = state.selectedThemePreset,
        onPlaybackSettingsClick = viewModel::onPlaybackSettingsClick,
        onThemeClick = viewModel::onThemeClick,
        onPrivacyClick = {
            // 打开外部隐私政策（合规要求必须可访问真实地址）
            uriHandler.openUri(PRIVACY_POLICY_URL)
        },
        onAboutClick = viewModel::onAboutClick,
    )
}

@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    selectedThemePreset: VelarisThemePreset,
    onPlaybackSettingsClick: () -> Unit,
    onThemeClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = spec.spacing.large, vertical = spec.spacing.medium),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.title,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(spec.spacing.large + 8.dp))

        SettingsRow(
            title = stringResource(R.string.settings_theme),
            description = stringResource(
                R.string.settings_theme_desc,
                stringResource(selectedThemePreset.labelResId()),
            ),
            icon = Icons.Outlined.Palette,
            onClick = onThemeClick,
        )
        Spacer(modifier = Modifier.height(spec.spacing.medium))

        SettingsRow(
            title = stringResource(R.string.settings_playback),
            description = stringResource(R.string.settings_playback_desc),
            icon = Icons.Outlined.Settings,
            onClick = onPlaybackSettingsClick,
        )
        Spacer(modifier = Modifier.height(spec.spacing.medium))

        SettingsRow(
            title = stringResource(R.string.settings_privacy),
            description = stringResource(R.string.settings_privacy_desc),
            icon = Icons.Outlined.PrivacyTip,
            onClick = onPrivacyClick,
        )
        Spacer(modifier = Modifier.height(spec.spacing.medium))

        SettingsRow(
            title = stringResource(
                R.string.settings_about,
                stringResource(R.string.app_name),
            ),
            description = stringResource(R.string.settings_about_desc),
            icon = Icons.Outlined.Info,
            onClick = onAboutClick,
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .velarisClickable(onClick = onClick)
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .padding(horizontal = spec.spacing.large, vertical = spec.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spec.spacing.medium),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = spec.colors.gold,
        )
        Column {
            Text(
                text = title,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = spec.typography.body,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.bodySmall,
            )
        }
    }
}

@LandscapePreviews
@Composable
private fun SettingsScreenPreview() {
    SettingsContent(
        selectedThemePreset = VelarisThemePreset.Gold,
        onPlaybackSettingsClick = {},
        onThemeClick = {},
        onPrivacyClick = {},
        onAboutClick = {},
    )
}

/**
 * 将 VelarisThemePreset 映射到对应的字符串资源 ID。
 * 用于在设置列表中显示“当前主题：XXX”。
 * 注意：与 foundation:model 中的定义保持同步。
 */
private fun VelarisThemePreset.labelResId(): Int = when (this) {
    VelarisThemePreset.Gold -> R.string.settings_theme_gold
    VelarisThemePreset.Ocean -> R.string.settings_theme_ocean
    VelarisThemePreset.Forest -> R.string.settings_theme_forest
    VelarisThemePreset.Twilight -> R.string.settings_theme_twilight
}
