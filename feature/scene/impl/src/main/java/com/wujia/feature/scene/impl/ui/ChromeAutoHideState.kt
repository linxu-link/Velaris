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
package com.wujia.feature.scene.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wujia.feature.scene.impl.ui.viewmodel.ScenePanelState
import kotlinx.coroutines.delay

@Stable
internal class ChromeAutoHideState(initialVisible: Boolean = true) {
    var visible: Boolean by mutableStateOf(initialVisible)
        private set

    fun show() {
        visible = true
    }

    internal fun hide() {
        visible = false
    }
}

@Composable
internal fun rememberChromeAutoHideState(
    isPlaying: Boolean,
    activePanel: ScenePanelState,
    isSoundDialogOpen: Boolean,
    isCustomTimerDialogOpen: Boolean,
    currentSceneId: String?,
): ChromeAutoHideState {
    val state = remember { ChromeAutoHideState() }

    val shouldAutoHideChrome = isPlaying &&
        activePanel == ScenePanelState.NONE &&
        !isSoundDialogOpen &&
        !isCustomTimerDialogOpen

    LaunchedEffect(currentSceneId) {
        state.show()
    }

    LaunchedEffect(shouldAutoHideChrome, state.visible) {
        if (shouldAutoHideChrome && state.visible) {
            delay(SCENE_CHROME_AUTO_HIDE_DELAY_MILLIS)
            state.hide()
        } else if (!shouldAutoHideChrome) {
            state.show()
        }
    }

    return state
}

private const val SCENE_CHROME_AUTO_HIDE_DELAY_MILLIS = 5_000L
