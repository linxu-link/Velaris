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
package com.wujia.feature.scene.impl.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
internal fun SceneTitleLayer(
    title: String,
    subtitle: String,
    titleFontSize: TextUnit,
    edgePadding: Dp,
    showSubtitle: Boolean = true,
    visible: Boolean = true,
) {
    val spec = VelarisTheme.spec

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(spec.brushes.sceneOverlay),
        )

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(SCENE_CHROME_ANIMATION_MILLIS)) +
                slideInVertically(animationSpec = tween(SCENE_CHROME_ANIMATION_MILLIS)) { it / 3 },
            exit = fadeOut(animationSpec = tween(SCENE_CHROME_ANIMATION_MILLIS)) +
                slideOutVertically(animationSpec = tween(SCENE_CHROME_ANIMATION_MILLIS)) { it / 2 },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = edgePadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (titleFontSize.value * 0.10f).sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = spec.colors.goldBright.copy(alpha = 0.24f),
                            offset = Offset(0f, 0f),
                            blurRadius = 18f,
                        ),
                    ),
                )

                if (showSubtitle) {
                    Spacer(modifier = Modifier.height(spec.spacing.xLarge + spec.spacing.small))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(spec.size.stepLineWidth + spec.spacing.large)
                                .height(spec.size.stroke)
                                .background(spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium)),
                        )
                        Spacer(modifier = Modifier.width(spec.spacing.medium))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .rotate(45f)
                                .border(
                                    width = spec.size.stroke,
                                    color = spec.colors.goldSoft.copy(alpha = spec.alpha.strokeStrong),
                                )
                                .background(Color.Transparent),
                        )
                        Spacer(modifier = Modifier.width(spec.spacing.medium))
                        Text(
                            text = subtitle,
                            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                            fontSize = spec.typography.body,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = spec.colors.goldBright.copy(alpha = 0.12f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 10f,
                                ),
                            ),
                        )
                        Spacer(modifier = Modifier.width(spec.spacing.medium))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .rotate(45f)
                                .border(
                                    width = spec.size.stroke,
                                    color = spec.colors.goldSoft.copy(alpha = spec.alpha.strokeStrong),
                                )
                                .background(Color.Transparent),
                        )
                        Spacer(modifier = Modifier.width(spec.spacing.medium))
                        Box(
                            modifier = Modifier
                                .width(spec.size.stepLineWidth + spec.spacing.large)
                                .height(spec.size.stroke)
                                .background(spec.colors.stroke.copy(alpha = spec.alpha.strokeMedium)),
                        )
                    }
                }
            }
        }
    }
}

private const val SCENE_CHROME_ANIMATION_MILLIS = 420

@LandscapePreviews
@Composable
private fun SceneTitleLayerPreview() {
    SceneTitleLayer(
        title = "风雪夜归人",
        subtitle = "静谧山林 · 夜雪",
        titleFontSize = VelarisTheme.spec.typography.title,
        edgePadding = 24.dp,
        showSubtitle = true,
        visible = true,
    )
}
