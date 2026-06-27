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
@file:Suppress("ktlint:standard:function-naming")

package com.wujia.velaris.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.wujia.feature.scene.api.SceneNavKey
import com.wujia.feature.scene.impl.navigation.sceneEntry
import com.wujia.foundation.navigation.Navigator
import com.wujia.foundation.navigation.rememberNavigationState
import com.wujia.foundation.navigation.toEntries

@Composable
fun VelarisApp(modifier: Modifier = Modifier) {
    val navigationState = rememberNavigationState(
        startKey = SceneNavKey,
        topLevelKeys = setOf(SceneNavKey),
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entryProvider = entryProvider {
        sceneEntry(navigator)
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier.fillMaxSize(),
        onBack = { navigator.goBack() },
    )
}
