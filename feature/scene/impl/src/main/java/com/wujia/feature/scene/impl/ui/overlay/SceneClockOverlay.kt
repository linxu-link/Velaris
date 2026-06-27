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
package com.wujia.feature.scene.impl.ui.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.wujia.feature.scene.impl.ui.component.rememberSystemClockDisplay
import com.wujia.feature.scenecontrol.impl.ui.viewmodel.SceneControlUiState
import com.wujia.foundation.designsystem.clock.FlipClockTime
import com.wujia.foundation.designsystem.clock.FlipClockUnit
import com.wujia.foundation.designsystem.clock.FlipCountdownClock
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneTimerMode
import com.wujia.foundation.player.VelarisPlayerPool

@Composable
internal fun SceneClockOverlay(
    controlState: SceneControlUiState,
    isPlaying: Boolean,
    chromeVisible: Boolean,
    edgePadding: Dp,
    currentScenePage: Int,
    playerPool: VelarisPlayerPool,
    onClockVisibleChange: (Boolean) -> Unit,
) {
    val clockVisible =
        controlState.showCountdownClock &&
            (
                controlState.timerMode == SceneTimerMode.Clock ||
                    (isPlaying && controlState.isTimerRunning)
                ) &&
            !chromeVisible

    LaunchedEffect(clockVisible) {
        onClockVisibleChange(clockVisible)
    }

    if (!clockVisible) return

    val clockTime = when (controlState.timerMode) {
        SceneTimerMode.Clock -> {
            val systemClock = rememberSystemClockDisplay(controlState.currentTimestampMillis)
            systemClock.flipTime
        }
        SceneTimerMode.Countdown -> remember(controlState.timerRemainingMillis) {
            FlipClockTime.fromTotalSeconds(controlState.timerRemainingMillis / 1000L)
        }
    }
    val clockUnits = when (controlState.timerMode) {
        SceneTimerMode.Clock -> listOf(
            FlipClockUnit.Hour,
            FlipClockUnit.Minute,
            FlipClockUnit.Second,
        )
        SceneTimerMode.Countdown -> remember(controlState.timerDurationMillis) {
            if (controlState.timerDurationMillis >= 60 * 60 * 1000L) {
                listOf(FlipClockUnit.Hour, FlipClockUnit.Minute, FlipClockUnit.Second)
            } else {
                listOf(FlipClockUnit.Minute, FlipClockUnit.Second)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        val clockModifier = when (controlState.countdownClockPosition) {
            SceneCountdownClockPosition.Center -> Modifier.align(Alignment.Center)
            SceneCountdownClockPosition.TopStart ->
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = edgePadding, top = edgePadding)

            SceneCountdownClockPosition.BottomStart ->
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = edgePadding, bottom = edgePadding)

            SceneCountdownClockPosition.TopEnd ->
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = edgePadding, top = edgePadding)

            SceneCountdownClockPosition.BottomEnd ->
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = edgePadding, bottom = edgePadding)
        }

        Box(modifier = clockModifier) {
            FlipCountdownClock(
                time = clockTime,
                units = clockUnits,
                onFlip = {
                    playerPool.get(currentScenePage).playClockAudio()
                },
            )
        }
    }
}
