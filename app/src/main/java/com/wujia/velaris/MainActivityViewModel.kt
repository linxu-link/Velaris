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
package com.wujia.velaris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {

    data class Startup(val sceneReady: Boolean, val appOpenAdState: AppOpenAdStartupState) : MainActivityUiState {
        override fun shouldKeepSplashScreen(): Boolean = !sceneReady
    }

    /**
     * 返回 true 如果状态尚未加载完成，应该保持显示启动屏幕
     */
    fun shouldKeepSplashScreen(): Boolean
}

enum class AppOpenAdStartupState {
    Idle,
    Pending,
    Showing,
    Finished,
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(observeSceneResources: ObserveSceneResourcesUseCase) : ViewModel() {
    private val appOpenAdState = MutableStateFlow(AppOpenAdStartupState.Idle)

    val uiState: StateFlow<MainActivityUiState> = combine(
        observeSceneResources()
            .map { scenes ->
                if (scenes.isNotEmpty()) {
                    delay(100)
                    true
                } else {
                    false
                }
            },
        appOpenAdState,
    ) { sceneReady, adState ->
        MainActivityUiState.Startup(
            sceneReady = sceneReady,
            appOpenAdState = adState,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainActivityUiState.Startup(
                sceneReady = false,
                appOpenAdState = AppOpenAdStartupState.Idle,
            ),
        )

    fun onColdStartAppOpenPending() {
        appOpenAdState.value = AppOpenAdStartupState.Pending
    }

    fun onAppOpenAdShown() {
        appOpenAdState.value = AppOpenAdStartupState.Showing
    }

    fun onAppOpenAdFinished() {
        appOpenAdState.value = AppOpenAdStartupState.Finished
    }

    fun onAppOpenAdNotRequested() {
        appOpenAdState.value = AppOpenAdStartupState.Idle
    }
}
