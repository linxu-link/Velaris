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
package com.wujia.feature.settings.impl.ui.overlay

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wujia.feature.settings.impl.ui.dialog.AboutDialog
import com.wujia.feature.settings.impl.ui.dialog.PlaybackSettingsDialog
import com.wujia.feature.settings.impl.ui.dialog.PrivacyDialog
import com.wujia.feature.settings.impl.ui.dialog.ThemeSettingsDialog
import com.wujia.feature.settings.impl.ui.viewmodel.SettingsDialogState
import com.wujia.feature.settings.impl.ui.viewmodel.SettingsViewModel

/**
 * 设置对话框的统一 Overlay。
 * 根据 ViewModel 中的布尔标志，条件渲染各个设置对话框。
 * 这样可以在面板或页面中统一管理对话框生命周期，避免在多个地方直接声明 Dialog。
 */
@Composable
internal fun SettingsDialogOverlay(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    when (state.activeDialog) {
        SettingsDialogState.None -> Unit
        SettingsDialogState.Playback -> PlaybackSettingsDialog(
            selectedProfile = state.selectedProfile,
            onProfileSelected = viewModel::onProfileSelected,
            onDismissRequest = viewModel::onDialogDismiss,
        )
        SettingsDialogState.Theme -> ThemeSettingsDialog(
            selectedPreset = state.selectedThemePreset,
            onThemeSelected = viewModel::onThemeSelected,
            onDismissRequest = viewModel::onDialogDismiss,
        )
        SettingsDialogState.Privacy -> PrivacyDialog(
            canRequestAds = state.consentCanRequestAds,
            privacyOptionsRequired = state.consentPrivacyOptionsRequired,
            consentError = state.consentError,
            onShowPrivacyOptions = {
                activity?.let { viewModel.onShowPrivacyOptions(it) }
            },
            onDismissRequest = viewModel::onDialogDismiss,
        )
        SettingsDialogState.About -> AboutDialog(
            versionName = state.versionName,
            versionCode = state.versionCode,
            onDismissRequest = viewModel::onDialogDismiss,
        )
    }
}
