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
package com.wujia.feature.scenelist.impl.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wujia.feature.scenelist.impl.entity.SceneListItem
import com.wujia.feature.scenelist.impl.ui.sampleSceneListItems
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import com.wujia.foundation.ui.R as UiR

@Composable
/**
 * 场景列表卡片。
 * 支持两种模式：
 * - 普通浏览模式：点击打开场景
 * - 管理模式（长按进入）：显示删除按钮（仅非预设），卡片轻微摇晃动画
 *
 * 封面优先级：coverUri（本地） > coverResId（内置） > 渐变 + accent 色
 */
internal fun SceneListCard(modifier: Modifier = Modifier, item: SceneListItem, isManageMode: Boolean = false, onDeleteClick: () -> Unit = {}) {
    val spec = VelarisTheme.spec
    val wobbleTransition = rememberInfiniteTransition(label = "scene_list_card_wobble")
    val wobbleRotation by wobbleTransition.animateFloat(
        initialValue = -0.65f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scene_list_card_wobble_rotation",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = if (isManageMode) wobbleRotation else 0f
            }
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = 0.34f),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            // 无障碍支持（中优先）
            .semantics {
                contentDescription = "${item.title}，${item.description}"
                if (isManageMode && !item.isPreset) {
                    // 管理模式下可删除
                }
            },
    ) {
        SceneCardBackground(item = item)

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.22f))
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.06f),
                        0.52f to Color.Black.copy(alpha = 0.24f),
                        1f to Color.Black.copy(alpha = 0.76f),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = spec.spacing.medium,
                    end = spec.spacing.medium,
                    bottom = spec.spacing.medium,
                ),
        ) {
            Text(
                text = item.title,
                color = spec.colors.textPrimary.copy(alpha = 0.96f),
                fontSize = spec.typography.body,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(spec.spacing.xSmall))

            Text(
                text = item.description,
                color = spec.colors.textSecondary.copy(alpha = 0.86f),
                fontSize = spec.typography.caption,
                lineHeight = spec.typography.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isManageMode) {
            val actionSize = spec.size.controlCompact - spec.spacing.small
            val actionIconSize = spec.size.iconSmall
            val actionBackground = Color.Black.copy(alpha = 0.46f)
            val actionBorder = spec.colors.stroke.copy(alpha = 0.42f)
            val actionTint = spec.colors.textSecondary.copy(alpha = 0.88f)

            if (item.isPreset) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spec.spacing.small)
                        .size(actionSize)
                        .clip(CircleShape)
                        .background(actionBackground)
                        .border(spec.size.stroke, actionBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(UiR.string.scene_list_preset_not_deletable),
                        tint = actionTint,
                        modifier = Modifier.size(actionIconSize),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spec.spacing.small)
                        .size(actionSize)
                        .clip(CircleShape)
                        .background(actionBackground)
                        .border(spec.size.stroke, actionBorder, CircleShape)
                        .velarisClickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(UiR.string.scene_list_delete_scene),
                        tint = actionTint,
                        modifier = Modifier.size(actionIconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneCardBackground(
    item: SceneListItem,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    when {
        item.coverResId != null -> {
            Image(
                painter = painterResource(item.coverResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxSize()
                    .blur(1.4.dp),
            )
        }
        item.coverUri != null -> {
            AsyncImage(
                model = item.coverUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxSize()
                    .blur(1.4.dp),
            )
        }
        else -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                item.accent.copy(alpha = 0.80f),
                                spec.colors.surface,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
internal fun AddSceneCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val cardShape = RoundedCornerShape(spec.radii.thumbnail)
    Column(
        modifier = modifier
            .velarisClickable(onClick = onClick)
            .clip(cardShape)
            .velarisGlassBlur(
                shape = cardShape,
                blurRadius = spec.blur.button,
            )
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.24f))
            .border(
                spec.size.stroke,
                spec.colors.gold.copy(alpha = 0.24f),
                cardShape,
            )
            .padding(spec.spacing.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(spec.size.controlCompact - spec.spacing.xSmall / 2)
                .clip(CircleShape)
                .background(spec.colors.gold.copy(alpha = 0.16f))
                .border(spec.size.stroke, spec.colors.gold.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = spec.colors.gold.copy(alpha = 0.88f),
                modifier = Modifier.size(spec.size.iconMedium),
            )
        }

        Spacer(modifier = Modifier.height(spec.spacing.small))

        Text(
            text = stringResource(UiR.string.scene_list_add_scene),
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.body,
            fontWeight = FontWeight.Medium,
        )
    }
}

@LandscapePreviews
@Composable
private fun SceneListCardPreview() {
    SceneListCard(
        item = sampleSceneListItems().first(),
        modifier = Modifier.fillMaxWidth(),
    )
}
