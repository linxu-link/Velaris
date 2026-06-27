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
package com.wujia.feature.sceneedit.impl.ui.panel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.sceneedit.impl.ui.viewmodel.ParticlePreset
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.particle.ParticleEffect
import com.wujia.foundation.model.particle.ParticleQuality
import com.wujia.foundation.toolkit.device.DeviceUtils
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.selectedBorderGlow

/**
 * 粒子效果选择内容
 *
 * 布局与 SceneEditNoiseContent 保持一致：
 * - 左侧：分类列表
 * - 右侧：粒子效果卡片（水平滚动）
 * - 底部：返回按钮 + 已选效果信息
 */
@Composable
internal fun SceneEditParticleContent(
    categories: List<String>,
    selectedCategory: Int,
    particles: List<ParticlePreset>,
    selectedParticleId: String?,
    onCategorySelected: (Int) -> Unit,
    onParticleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val selectedParticle = particles.firstOrNull { it.id == selectedParticleId }
    val selectedTitle = selectedParticle?.title ?: stringResource(R.string.scene_edit_no_particle)

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // 主体内容：分类 + 卡片列表
        ParticleCategoryAndList(
            categories = categories,
            selectedCategory = selectedCategory,
            particles = particles,
            selectedParticleId = selectedParticleId,
            onCategorySelected = onCategorySelected,
            onParticleSelected = onParticleSelected,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.height(spec.spacing.medium))

        Row {
            ParticleInfoBar(
                title = selectedTitle,
                effect = selectedParticle?.effect,
                effectLabel = selectedParticle?.effectLabel,
                intensity = selectedParticle?.intensity,
                wind = selectedParticle?.wind,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ParticleCategoryAndList(
    categories: List<String>,
    selectedCategory: Int,
    particles: List<ParticlePreset>,
    selectedParticleId: String?,
    onCategorySelected: (Int) -> Unit,
    onParticleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec

    Row(modifier = modifier) {
        // 左侧分类列表
        ParticleCategoryList(
            categories = categories,
            selectedIndex = selectedCategory,
            onSelectedChange = onCategorySelected,
            modifier = Modifier.width(130.dp),
        )

        Spacer(Modifier.width(spec.spacing.large))

        // 右侧粒子卡片列表
        ParticleCardList(
            particles = particles,
            selectedParticleId = selectedParticleId,
            onSelectedChange = onParticleSelected,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ParticleCategoryList(
    categories: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .padding(spec.spacing.small),
        verticalArrangement = Arrangement.spacedBy(spec.spacing.small),
    ) {
        itemsIndexed(categories) { index, title ->
            val selected = index == selectedIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(spec.radii.thumbnail + spec.spacing.xSmall / 2))
                    .background(
                        if (selected) {
                            spec.colors.gold.copy(alpha = 0.14f)
                        } else {
                            Color.Transparent
                        },
                    )
                    .border(
                        width = if (selected) spec.size.stroke else 0.dp,
                        color = if (selected) {
                            spec.colors.gold
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(spec.radii.thumbnail + spec.spacing.xSmall / 2),
                    )
                    .velarisClickable { onSelectedChange(index) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = when (index) {
                        0 -> Icons.Default.WaterDrop
                        1 -> Icons.Default.Eco
                        2 -> Icons.Default.Thunderstorm
                        else -> Icons.Outlined.DarkMode
                    },
                    contentDescription = null,
                    tint = if (selected) {
                        spec.colors.gold
                    } else {
                        spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
                    },
                    modifier = Modifier.size(18.dp),
                )

                Spacer(Modifier.width(spec.spacing.medium))

                Text(
                    text = title,
                    color = if (selected) {
                        spec.colors.gold
                    } else {
                        spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
                    },
                    fontSize = spec.typography.label,
                )
            }
        }
    }
}

@Composable
private fun ParticleCardList(
    particles: List<ParticlePreset>,
    selectedParticleId: String?,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec

    val context = LocalContext.current
    val isTablet = remember(context) { DeviceUtils.isTablet(context) }

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val compactPhone = !isTablet && (maxWidth < 420.dp || maxHeight < 320.dp)
        val cardWidth = when {
            isTablet && maxWidth >= 1000.dp -> 280.dp
            compactPhone -> 148.dp
            isTablet -> 250.dp
            maxWidth >= 720.dp -> 200.dp
            else -> 170.dp
        }
        val horizontalPadding =
            if (maxWidth >= 1000.dp) {
                spec.spacing.large
            } else if (compactPhone) {
                spec.spacing.small
            } else {
                spec.spacing.xSmall / 2
            }

        LazyRow(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(spec.spacing.large),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) {
            items(particles, key = { it.id }) { particle ->
                ParticleCard(
                    particle = particle,
                    isSelected = particle.id == selectedParticleId,
                    cardWidth = cardWidth,
                    compact = compactPhone,
                    onClick = { onSelectedChange(particle.id) },
                )
            }
        }
    }
}

@Composable
private fun ParticleCard(
    particle: ParticlePreset,
    isSelected: Boolean,
    cardWidth: Dp,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val context = LocalContext.current
    val coverResId = remember(particle.coverResName, context.packageName) {
        particle.coverResName
            ?.let { name ->
                context.resources.getIdentifier(name, "drawable", context.packageName)
                    .takeIf { it != 0 }
            }
            ?: com.wujia.foundation.model.R.drawable.ic_rain_1
    }

    Box(
        modifier = modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .selectedBorderGlow(
                selected = isSelected,
                cornerRadius = spec.radii.thumbnail,
                selectedColor = spec.colors.goldSoft,
                unselectedColor = spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                borderWidth = spec.size.stroke,
            )
            .velarisClickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = coverResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.56f),
                            Color.Black.copy(alpha = 0.82f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) spec.spacing.medium else spec.spacing.large,
                    vertical = if (compact) spec.spacing.medium else spec.spacing.large,
                ),
        ) {
            Text(
                text = particle.title,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) spec.typography.label else spec.typography.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(spec.spacing.xSmall))

            Text(
                text = particle.description,
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = spec.typography.caption * 1.4,
            )

            if (particle.effect != ParticleEffect.NONE) {
                Spacer(Modifier.height(spec.spacing.xSmall))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spec.spacing.small),
                ) {
                    ParamLabel(stringResource(R.string.scene_edit_param_intensity), "${(particle.intensity * 100).toInt()}%")
                    ParamLabel(stringResource(R.string.scene_edit_param_wind), "${(particle.wind * 100).toInt()}%")
                }
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spec.spacing.medium)
                    .size(22.dp)
                    .clip(RoundedCornerShape(spec.radii.pill))
                    .background(spec.colors.goldSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = spec.colors.onGold,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ParticleInfoBar(
    title: String,
    effect: ParticleEffect?,
    effectLabel: String? = null,
    intensity: Float? = null,
    wind: Float? = null,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val effectColor = effect?.let { getEffectColor(it) } ?: spec.colors.textMuted

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .padding(horizontal = spec.spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 效果图标
        if (effect != null && effect != ParticleEffect.NONE) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(effectColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getEffectIcon(effect),
                    contentDescription = null,
                    tint = effectColor,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(spec.spacing.small))
        }

        // 标题
        Text(
            text = title,
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (effect != null && effect != ParticleEffect.NONE) {
            Spacer(Modifier.width(spec.spacing.small))

            Text(
                text = effectLabel.orEmpty(),
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.caption,
                maxLines = 1,
            )

            if (intensity != null && wind != null) {
                Spacer(Modifier.width(spec.spacing.small))

                Text(
                    text = stringResource(R.string.scene_edit_params_display, (intensity * 100).toInt(), (wind * 100).toInt()),
                    color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                    fontSize = spec.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ParamLabel(label: String, value: String) {
    val spec = VelarisTheme.spec

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label: ",
            fontSize = spec.typography.caption * 0.9f,
            color = spec.colors.textMuted.copy(alpha = spec.alpha.textMuted * 0.8f),
        )
        Text(
            text = value,
            fontSize = spec.typography.caption * 0.9f,
            fontWeight = FontWeight.Medium,
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
        )
    }
}

private fun getEffectIcon(effect: ParticleEffect): ImageVector = when (effect) {
    ParticleEffect.NONE -> Icons.Default.Eco
    ParticleEffect.RAIN -> Icons.Default.WaterDrop
    ParticleEffect.SNOW -> Icons.Default.Eco
    ParticleEffect.FIREFLIES -> Icons.Outlined.Nightlight
}

private fun getEffectColor(effect: ParticleEffect): Color = when (effect) {
    ParticleEffect.NONE -> VelarisColor.ParticleNone
    ParticleEffect.RAIN -> VelarisColor.ParticleRain
    ParticleEffect.SNOW -> VelarisColor.ParticleSnow
    ParticleEffect.FIREFLIES -> VelarisColor.ParticleFireflies
}

@LandscapePreviews
@Composable
private fun SceneEditParticleContentPreview() {
    val sampleParticles = listOf(
        ParticlePreset(
            id = ProjectsIds.Particle.LIGHT_RAIN,
            title = "细雨绵绵",
            description = "轻柔的雨滴，营造宁静氛围",
            category = "雨天",
            effect = ParticleEffect.RAIN,
            effectLabel = "雨",
            intensity = 0.3f,
            wind = 0.2f,
            quality = ParticleQuality.LOW,
            foregroundGlassEnabled = true,
            tags = listOf("轻柔", "自然"),
        ),
    )

    SceneEditParticleContent(
        categories = listOf("雨天", "雪天", "暴风雨", "平静"),
        selectedCategory = 0,
        particles = sampleParticles,
        selectedParticleId = ProjectsIds.Particle.MODERATE_RAIN,
        onCategorySelected = {},
        onParticleSelected = {},
    )
}
