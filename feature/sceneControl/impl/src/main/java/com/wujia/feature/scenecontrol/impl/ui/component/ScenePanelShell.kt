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
package com.wujia.feature.scenecontrol.impl.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.layout.LandscapeLayoutType
import com.wujia.foundation.designsystem.layout.toLandscapeLayoutType
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
internal fun ScenePanelShell(
    modifier: Modifier = Modifier,
    sceneName: String,
    isCustomScene: Boolean,
    onAdjustDetailsClick: () -> Unit = {},
    onEditSceneClick: () -> Unit,
    content: @Composable RowScope.(LandscapeLayoutType) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layoutType = maxWidth.toLandscapeLayoutType()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = layoutType.controlPanelHorizontalPadding),
        ) {
            ScenePanelHeader(
                sceneName = sceneName,
                layoutType = layoutType,
                isCustomScene = isCustomScene,
                onAdjustDetailsClick = onAdjustDetailsClick,
                onEditSceneClick = onEditSceneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layoutType.controlHeaderHeight),
            )

            Spacer(modifier = Modifier.height(layoutType.controlHeaderBottomSpacing))

            Row(
                horizontalArrangement = Arrangement.spacedBy(layoutType.controlPanelSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                content(layoutType)
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun ScenePanelShellPreview() {
    ScenePanelShell(
        sceneName = "风雪夜归人",
        isCustomScene = true,
        onEditSceneClick = {},
        onAdjustDetailsClick = {},
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = VelarisTheme.spec.spacing.large),
    ) { layoutType ->
        repeat(2) { index ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (index == 0) "视觉面板" else "声音面板",
                    color = VelarisTheme.spec.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "layout=${layoutType.name}",
                    color = VelarisTheme.spec.colors.textSecondary,
                )
            }
        }
    }
}
