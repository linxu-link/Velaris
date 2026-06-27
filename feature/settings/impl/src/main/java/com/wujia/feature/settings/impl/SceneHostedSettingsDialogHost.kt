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
package com.wujia.feature.settings.impl

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wujia.feature.settings.impl.ui.dialog.AboutDialog
import com.wujia.feature.settings.impl.ui.dialog.PlaybackSettingsDialog
import com.wujia.feature.settings.impl.ui.dialog.PrivacyDialog
import com.wujia.feature.settings.impl.ui.dialog.ThemeSettingsDialog
import com.wujia.feature.settings.impl.ui.viewmodel.SettingsDialogState
import com.wujia.feature.settings.impl.ui.viewmodel.SettingsViewModel
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
fun SceneHostedSettingsDialogHost(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    val viewModel: SettingsViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    if (state.activeDialog == SettingsDialogState.None) return

    val activity = LocalContext.current as? Activity
    val spec = VelarisTheme.spec

    BackHandler(onBack = viewModel::onDialogDismiss)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .velarisClickable(onClick = viewModel::onDialogDismiss),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spec.spacing.large),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.velarisClickable {},
            ) {
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
                        onShowPrivacyOptions = { activity?.let(viewModel::onShowPrivacyOptions) },
                        onDismissRequest = viewModel::onDialogDismiss,
                    )

                    SettingsDialogState.About -> AboutDialog(
                        versionName = state.versionName,
                        versionCode = state.versionCode,
                        onDismissRequest = viewModel::onDialogDismiss,
                    )
                }
            }
        }
    }
}
