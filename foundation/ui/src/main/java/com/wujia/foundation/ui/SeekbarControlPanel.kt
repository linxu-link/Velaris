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
package com.wujia.foundation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.wujia.foundation.designsystem.bar.GlowSeekBar
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import kotlin.math.roundToInt

@Composable
fun SeekbarControlPanel(
    items: List<SoundControlItem>,
    onValueChange: (index: Int, value: Float) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.player_sound),
    minWidth: Dp = 280.dp,
    minHeight: Dp = 180.dp,
    maxHeight: Dp = Dp.Unspecified,
    titleFontSize: TextUnit = TextUnit.Unspecified,
    itemTitleFontSize: TextUnit = TextUnit.Unspecified,
    valueFontSize: TextUnit = TextUnit.Unspecified,
    compact: Boolean = false,
) {
    val spec = VelarisTheme.spec
    val resolvedTitleFontSize = titleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedItemTitleFontSize = itemTitleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedValueFontSize = valueFontSize.takeOrElse { spec.typography.subtitle }
    val panelModifier = if (compact) {
        modifier
    } else {
        modifier.defaultMinSize(minWidth = minWidth, minHeight = minHeight)
    }
    Box(
        modifier = panelModifier,
    ) {
        Column(modifier = if (compact) Modifier.fillMaxWidth() else Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(spec.spacing.xSmall))

                Text(
                    text = title,
                    color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                    fontSize = resolvedTitleFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(spec.spacing.medium))

            val listState = rememberLazyListState()

            val showTopFade by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex > 0 ||
                        listState.firstVisibleItemScrollOffset > 0
                }
            }

            val showBottomFade by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
                    val totalCount = layoutInfo.totalItemsCount

                    if (lastVisible == null) return@derivedStateOf false

                    lastVisible.index < totalCount - 1 ||
                        lastVisible.offset + lastVisible.size > layoutInfo.viewportEndOffset
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = if (compact) {
                        if (maxHeight != Dp.Unspecified) {
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    } else {
                        Modifier.fillMaxSize()
                    },
                    verticalArrangement = Arrangement.spacedBy(spec.spacing.small),
                    contentPadding = PaddingValues(
                        top = spec.spacing.xSmall,
                        bottom = spec.spacing.medium,
                    ),
                ) {
                    itemsIndexed(items, key = { _, item -> item.title }) { index, item ->
                        SeekbarControlRow(
                            item = item,
                            titleFontSize = resolvedItemTitleFontSize,
                            valueFontSize = resolvedValueFontSize,
                            onValueChange = {
                                onValueChange(index, it)
                            },
                        )
                    }
                }

                // 顶部 fade（只有可继续上滑才显示）
                if (showTopFade) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(spec.spacing.xLarge)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        spec.colors.surface.copy(alpha = spec.alpha.textPrimary),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }

                // 底部 fade（只有可继续下滑才显示）
                if (showBottomFade) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(spec.spacing.xLarge)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        spec.colors.surface.copy(alpha = spec.alpha.textPrimary),
                                    ),
                                ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun SeekbarControlRow(
    item: SoundControlItem,
    onValueChange: (Float) -> Unit,
    titleFontSize: TextUnit = TextUnit.Unspecified,
    valueFontSize: TextUnit = TextUnit.Unspecified,
) {
    val spec = VelarisTheme.spec
    val resolvedTitleFontSize = titleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedValueFontSize = valueFontSize.takeOrElse { spec.typography.subtitle }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(spec.size.controlSmall)
                .clip(CircleShape)
                .background(spec.colors.stroke.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = spec.colors.goldSoft.copy(alpha = 0.82f),
                modifier = Modifier.size(spec.size.iconMedium + spec.spacing.xSmall / 2),
            )
        }

        Spacer(modifier = Modifier.width(spec.spacing.small))

        Text(
            text = item.title,
            color = spec.colors.textSecondary.copy(alpha = 0.78f),
            fontSize = resolvedTitleFontSize,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(min = 48.dp, max = 96.dp),
        )

        Spacer(modifier = Modifier.width(spec.spacing.small))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(spec.size.controlSmall),
        ) {
            GlowSeekBar(
                value = item.value,
                onValueChange = onValueChange,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.width(spec.spacing.small))

        Text(
            text = "${(item.value * 100).roundToInt()}%",
            color = spec.colors.textSecondary.copy(alpha = 0.78f),
            fontSize = resolvedValueFontSize,
            modifier = Modifier.widthIn(min = 44.dp),
            textAlign = TextAlign.End,
        )
    }
}

@LandscapePreviews
@Composable
private fun SeekbarControlPanelPreview() {
    var items by remember {
        mutableStateOf(
            listOf(
                SoundControlItem("雪声", Icons.Outlined.GraphicEq, 0.8f),
                SoundControlItem("雨声", Icons.Outlined.WaterDrop, 0.55f),
                SoundControlItem("风声", Icons.Outlined.Air, 0.35f),
            ),
        )
    }

    SeekbarControlPanel(
        items = items,
        onValueChange = { index, value ->
            items = items.toMutableList().also {
                it[index] = it[index].copy(value = value)
            }
        },
        modifier = Modifier
            .widthIn(min = 280.dp, max = 400.dp)
            .heightIn(min = 180.dp, max = 220.dp),
    )
}
