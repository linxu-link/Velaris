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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wujia.feature.scene.impl.ui.SceneGuideStep
import com.wujia.foundation.designsystem.guide.GuideOverlay
import com.wujia.foundation.designsystem.guide.GuidePlacement
import com.wujia.foundation.designsystem.guide.GuideTargetState
import com.wujia.foundation.designsystem.guide.guideTarget
import com.wujia.foundation.designsystem.guide.rememberGuideTargetState
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R

@Stable
internal class SceneGuideState(
    val logoTarget: GuideTargetState,
    val playerTarget: GuideTargetState,
    val sceneSwipeTarget: GuideTargetState,
    val swipeTarget: GuideTargetState,
    val timerTarget: GuideTargetState,
    private val _activeIndex: MutableIntState,
    private val _initialized: MutableState<Boolean>,
) {
    private val targets = listOf(logoTarget, playerTarget, sceneSwipeTarget, swipeTarget, timerTarget)
    private val steps = listOf(
        SceneGuideStep(
            titleResId = R.string.scene_guide_logo_title,
            descriptionResId = R.string.scene_guide_logo_desc,
            placement = GuidePlacement.End,
        ),
        SceneGuideStep(
            titleResId = R.string.scene_guide_player_title,
            descriptionResId = R.string.scene_guide_player_desc,
            placement = GuidePlacement.Above,
        ),
        SceneGuideStep(
            titleResId = R.string.scene_guide_scene_swipe_title,
            descriptionResId = R.string.scene_guide_scene_swipe_desc,
            placement = GuidePlacement.Start,
        ),
        SceneGuideStep(
            titleResId = R.string.scene_guide_swipe_title,
            descriptionResId = R.string.scene_guide_swipe_desc,
            placement = GuidePlacement.Above,
        ),
        SceneGuideStep(
            titleResId = R.string.scene_guide_timer_title,
            descriptionResId = R.string.scene_guide_timer_desc,
            placement = GuidePlacement.Start,
        ),
    )

    var activeIndex by _activeIndex
    var initialized by _initialized

    val isActive: Boolean get() = activeIndex in targets.indices

    fun onHasCompletedGuideChanged(hasCompleted: Boolean) {
        if (hasCompleted) {
            activeIndex = -1
            initialized = true
        } else if (!initialized && activeIndex < 0) {
            activeIndex = 0
            initialized = true
        }
    }

    fun advanceOrDismiss(onGuideCompleted: () -> Unit) {
        if (activeIndex == steps.lastIndex) {
            onGuideCompleted()
        }
        activeIndex = if (activeIndex == steps.lastIndex) -1 else activeIndex + 1
        initialized = true
    }

    @Composable
    fun ContentIfActive(onGuideCompleted: () -> Unit) {
        if (!isActive) return
        val spec = VelarisTheme.spec
        val currentGuide = steps[activeIndex]
        GuideOverlay(
            targetState = targets[activeIndex],
            title = stringResource(currentGuide.titleResId),
            description = stringResource(currentGuide.descriptionResId),
            placement = currentGuide.placement,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spec.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { advanceOrDismiss(onGuideCompleted) },
                ) {
                    Text(
                        text = if (activeIndex == steps.lastIndex) {
                            stringResource(R.string.common_close)
                        } else {
                            stringResource(R.string.common_next)
                        },
                        color = spec.colors.goldBright,
                        fontSize = spec.typography.label,
                    )
                }
            }
        }
    }
}

@Composable
internal fun rememberSceneGuideState(): SceneGuideState {
    val logoTarget = rememberGuideTargetState()
    val playerTarget = rememberGuideTargetState()
    val sceneSwipeTarget = rememberGuideTargetState()
    val swipeTarget = rememberGuideTargetState()
    val timerTarget = rememberGuideTargetState()
    val activeIndex = rememberSaveable { mutableIntStateOf(-1) }
    val initialized = rememberSaveable { mutableStateOf(false) }
    return remember {
        SceneGuideState(
            logoTarget = logoTarget,
            playerTarget = playerTarget,
            sceneSwipeTarget = sceneSwipeTarget,
            swipeTarget = swipeTarget,
            timerTarget = timerTarget,
            _activeIndex = activeIndex,
            _initialized = initialized,
        )
    }
}

internal fun Modifier.guideTarget(state: SceneGuideState, type: GuideTargetType): Modifier = when (type) {
    GuideTargetType.Logo -> guideTarget(state.logoTarget)
    GuideTargetType.Player -> guideTarget(state.playerTarget)
    GuideTargetType.SceneSwipe -> guideTarget(state.sceneSwipeTarget)
    GuideTargetType.Swipe -> guideTarget(state.swipeTarget)
    GuideTargetType.Timer -> guideTarget(state.timerTarget)
}

internal enum class GuideTargetType {
    Logo,
    Player,
    SceneSwipe,
    Swipe,
    Timer,
}
