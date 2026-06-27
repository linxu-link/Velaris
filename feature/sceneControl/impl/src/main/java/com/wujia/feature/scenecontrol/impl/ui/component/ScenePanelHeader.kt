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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.wujia.foundation.designsystem.button.RadiusIconButton
import com.wujia.foundation.designsystem.layout.LandscapeLayoutType
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.tab.SceneTabItem
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.R as UiR

@Composable
internal fun ScenePanelHeader(
    modifier: Modifier = Modifier,
    sceneName: String,
    layoutType: LandscapeLayoutType,
    isCustomScene: Boolean = true,
    onAdjustDetailsClick: () -> Unit,
    onEditSceneClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val actionSize = layoutType.controlActionSize
    val editIconSize = layoutType.controlSceneEditIconSize

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sceneName,
                modifier = Modifier,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = layoutType.controlSceneNameFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (isCustomScene) {
                Spacer(modifier = Modifier.width(spec.spacing.xSmall))

                IconButton(
                    onClick = onEditSceneClick,
                    modifier = Modifier.size(actionSize),
                ) {
                    Icon(
                        tint = spec.colors.gold,
                        painter = painterResource(id = UiR.drawable.ic_edit),
                        contentDescription = stringResource(UiR.string.scene_edit_title),
                        modifier = Modifier.size(editIconSize),
                    )
                }
            }
        }

        if (isCustomScene) {
            Spacer(modifier = Modifier.width(spec.spacing.medium))

            RadiusIconButton(
                item = SceneTabItem(
                    text = stringResource(UiR.string.scene_control_adjust_details),
                    icon = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                ),
                cornerRadius = actionSize / 2,
                contentPadding = PaddingValues(horizontal = spec.spacing.large),
                modifier = Modifier
                    .height(actionSize)
                    .velarisClickable { onAdjustDetailsClick() },
            )
        }
    }
}

@LandscapePreviews
@Composable
private fun ScenePanelHeaderPreview() {
    ScenePanelHeader(
        sceneName = "风雪夜归人",
        layoutType = LandscapeLayoutType.Medium,
        isCustomScene = true,
        onAdjustDetailsClick = {},
        onEditSceneClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VelarisTheme.spec.spacing.large)
            .height(LandscapeLayoutType.Medium.controlHeaderHeight),
    )
}
