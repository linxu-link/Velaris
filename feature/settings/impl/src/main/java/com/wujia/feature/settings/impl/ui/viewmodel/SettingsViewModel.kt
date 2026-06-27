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
package com.wujia.feature.settings.impl.ui.viewmodel

import android.app.Activity
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.foundation.ads.AdsConsentManager
import com.wujia.foundation.model.settings.ThemeSettingsRepository
import com.wujia.foundation.model.theme.VelarisThemePreset
import com.wujia.foundation.player.PlaybackSettingsRepository
import com.wujia.foundation.player.VelarisPlayerConfig
import com.wujia.foundation.player.VelarisPlayerPerformanceProfile
import com.wujia.foundation.toolkit.AppVersionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
internal sealed interface SettingsDialogState {
    data object None : SettingsDialogState
    data object Playback : SettingsDialogState
    data object Theme : SettingsDialogState
    data object Privacy : SettingsDialogState
    data object About : SettingsDialogState
}

@Stable
/**
 * 设置界面的 UI 状态。
 * 包含当前选中的播放性能配置、主题预设、各个对话框的显示标志、
 * 广告同意状态、以及应用版本信息。
 *
 * 设计说明：
 * - 使用布尔标志控制对话框显示（小规模设置模块适用）。
 * - playerConfig 是计算属性，根据 profile 映射到 VelarisPlayerConfig，
 *   供 SettingsPanel 通过 CompositionLocal 提供给子树使用。
 */
internal data class SettingsUiState(val selectedProfile: VelarisPlayerPerformanceProfile = VelarisPlayerPerformanceProfile.Balanced, val selectedThemePreset: VelarisThemePreset = VelarisThemePreset.Ocean, val activeDialog: SettingsDialogState = SettingsDialogState.None, val consentCanRequestAds: Boolean = false, val consentPrivacyOptionsRequired: Boolean = false, val consentError: String? = null, val versionName: String = "", val versionCode: Int = 0) {
    /**
     * 根据当前选中的性能配置计算对应的播放器配置。
     * PowerSaver / Balanced / Quality 分别对应不同的缓存、解码器等策略。
     */
    val playerConfig: VelarisPlayerConfig
        get() = when (selectedProfile) {
            VelarisPlayerPerformanceProfile.PowerSaver -> VelarisPlayerConfig.PowerSaver
            VelarisPlayerPerformanceProfile.Balanced -> VelarisPlayerConfig.Balanced
            VelarisPlayerPerformanceProfile.Quality -> VelarisPlayerConfig.Quality
        }
}

/**
 * 设置模块的 ViewModel。
 * 负责：
 * - 从 Repository 初始化并观察主题/播放配置
 * - 响应用户点击事件，控制对话框显示
 * - 调用 Repository 持久化变更
 * - 与 AdsConsentManager 协作处理广告同意流程
 *
 * 遵循 UDF：Composable 只消费 StateFlow 并发送事件给 VM。
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val adsConsentManager: AdsConsentManager,
    private val appVersionInfo: AppVersionInfo,
    private val playbackSettingsRepository: PlaybackSettingsRepository,
    private val themeSettingsRepository: ThemeSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            selectedProfile = playbackSettingsRepository.getPerformanceProfile(),
            selectedThemePreset = themeSettingsRepository.getThemePreset(),
            versionName = appVersionInfo.versionName,
            versionCode = appVersionInfo.versionCode,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // 观察播放性能配置变化，实时同步到 UI
        viewModelScope.launch {
            playbackSettingsRepository.observePerformanceProfile().collect { profile ->
                _uiState.update { it.copy(selectedProfile = profile) }
            }
        }
        // 观察主题预设变化，实时同步到 UI
        viewModelScope.launch {
            themeSettingsRepository.observeThemePreset().collect { preset ->
                _uiState.update { it.copy(selectedThemePreset = preset) }
            }
        }
    }

    /** 点击“播放设置”行，打开对应对话框 */
    fun onPlaybackSettingsClick() {
        _uiState.update { it.copy(activeDialog = SettingsDialogState.Playback) }
    }

    /** 点击“主题设置”行，打开对应对话框 */
    fun onThemeClick() {
        _uiState.update { it.copy(activeDialog = SettingsDialogState.Theme) }
    }

    /** 点击“隐私与广告”，准备对话框状态并打开 */
    fun onPrivacyClick() {
        _uiState.update {
            it.copy(
                activeDialog = SettingsDialogState.Privacy,
                consentCanRequestAds = adsConsentManager.canRequestAds(),
                consentPrivacyOptionsRequired = adsConsentManager.isPrivacyOptionsRequired(),
            )
        }
    }

    /** 点击“关于”，打开关于对话框 */
    fun onAboutClick() {
        _uiState.update { it.copy(activeDialog = SettingsDialogState.About) }
    }

    /** 任意对话框关闭时统一清理所有 show 标志 */
    fun onDialogDismiss() {
        _uiState.update { it.copy(activeDialog = SettingsDialogState.None) }
    }

    /** 用户在播放设置对话框中选择了新的性能配置，立即持久化 */
    fun onProfileSelected(profile: VelarisPlayerPerformanceProfile) {
        viewModelScope.launch {
            playbackSettingsRepository.updatePerformanceProfile(profile)
        }
    }

    /** 用户在主题设置对话框中选择了新的主题预设，立即持久化 */
    fun onThemeSelected(preset: VelarisThemePreset) {
        viewModelScope.launch {
            themeSettingsRepository.updateThemePreset(preset)
        }
    }

    /** 在隐私对话框中触发“管理广告选项”，调用 UMP 并更新 consent 状态 */
    fun onShowPrivacyOptions(activity: Activity) {
        viewModelScope.launch {
            val state = adsConsentManager.showPrivacyOptions(activity)
            _uiState.update {
                it.copy(
                    consentCanRequestAds = state.canRequestAds,
                    consentPrivacyOptionsRequired = state.privacyOptionsRequired,
                    consentError = state.error?.message,
                )
            }
        }
    }
}
