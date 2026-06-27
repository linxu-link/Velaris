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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AnimatedEdgeVisibility(
    visible: Boolean,
    edge: ChromeEdge,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = sceneChromeTween()) + edge.enterTransition(),
        exit = fadeOut(animationSpec = sceneChromeTween()) + edge.exitTransition(),
    ) {
        content()
    }
}

internal enum class ChromeEdge {
    TopStart,
    TopEnd,
    BottomStart,
    BottomEnd,
    Bottom,
}

internal fun ChromeEdge.enterTransition(): EnterTransition = when (this) {
    ChromeEdge.TopStart -> slideInHorizontally(animationSpec = sceneChromeTween()) { -it }
    ChromeEdge.TopEnd -> slideInHorizontally(animationSpec = sceneChromeTween()) { it }
    ChromeEdge.BottomStart -> slideInHorizontally(animationSpec = sceneChromeTween()) { -it }
    ChromeEdge.BottomEnd -> slideInHorizontally(animationSpec = sceneChromeTween()) { it }
    ChromeEdge.Bottom -> slideInVertically(animationSpec = sceneChromeTween()) { it }
}

internal fun ChromeEdge.exitTransition(): ExitTransition = when (this) {
    ChromeEdge.TopStart -> slideOutHorizontally(animationSpec = sceneChromeTween()) { -it }
    ChromeEdge.TopEnd -> slideOutHorizontally(animationSpec = sceneChromeTween()) { it }
    ChromeEdge.BottomStart -> slideOutHorizontally(animationSpec = sceneChromeTween()) { -it }
    ChromeEdge.BottomEnd -> slideOutHorizontally(animationSpec = sceneChromeTween()) { it }
    ChromeEdge.Bottom -> slideOutVertically(animationSpec = sceneChromeTween()) { it }
}

private fun <T> sceneChromeTween() = tween<T>(
    durationMillis = SCENE_CHROME_ANIMATION_MILLIS,
    easing = FastOutSlowInEasing,
)

private const val SCENE_CHROME_ANIMATION_MILLIS = 640
