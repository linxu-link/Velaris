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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SoundPreset
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.toolkit.device.DeviceUtils
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.selectedBorderGlow

@Composable
internal fun SceneEditNoiseContent(
    modifier: Modifier = Modifier,
    categories: List<String>,
    sounds: List<SoundPreset>,
    selectedCategory: Int,
    selectedSoundIds: List<String>,
    selectedSounds: List<SoundPreset>,
    isPreviewPlaying: Boolean,
    onCategoryChange: (Int) -> Unit,
    onSoundChange: (String) -> Unit,
    onSoundPresetChange: (SoundPreset) -> Unit,
    onPreviewToggle: () -> Unit,
    onPickLocalAudio: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val selectedTitle = selectedSounds.joinToString(" / ") { it.title }

    Column(modifier = modifier.fillMaxSize()) {
        SoundCategoryAndList(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategoryChange = onCategoryChange,
            sounds = sounds,
            selectedSoundIds = selectedSoundIds,
            onSoundChange = onSoundChange,
            onSoundPresetChange = onSoundPresetChange,
            onPickLocalAudio = onPickLocalAudio,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.height(spec.spacing.medium))

        Row {
            SoundPlayingBar(
                title = selectedTitle,
                isPlaying = isPreviewPlaying,
                onClick = onPreviewToggle,
            )
        }
    }
}

@Composable
private fun SoundCategoryAndList(
    categories: List<String>,
    selectedCategory: Int,
    onCategoryChange: (Int) -> Unit,
    sounds: List<SoundPreset>,
    selectedSoundIds: List<String>,
    onSoundChange: (String) -> Unit,
    onSoundPresetChange: (SoundPreset) -> Unit,
    onPickLocalAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val isLocalCategory = categories.getOrNull(selectedCategory) == NoiseCategory.LOCAL.displayName
    Row(modifier = modifier) {
        SoundCategoryList(
            categories = categories,
            selectedIndex = selectedCategory,
            onSelectedChange = onCategoryChange,
            modifier = Modifier.width(130.dp),
        )

        Spacer(Modifier.width(spec.spacing.large))

        SoundCardList(
            sounds = sounds,
            isLocalCategory = isLocalCategory,
            selectedSoundIds = selectedSoundIds,
            onSelectedChange = onSoundChange,
            onLocalSelectedChange = onSoundPresetChange,
            onPickLocalAudio = onPickLocalAudio,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SoundCategoryList(
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
                        0 -> Icons.Outlined.Eco
                        1 -> Icons.Outlined.FavoriteBorder
                        2 -> Icons.Outlined.RadioButtonChecked
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
private fun SoundCardList(
    sounds: List<SoundPreset>,
    isLocalCategory: Boolean,
    selectedSoundIds: List<String>,
    onSelectedChange: (String) -> Unit,
    onLocalSelectedChange: (SoundPreset) -> Unit,
    onPickLocalAudio: () -> Unit,
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
            if (isLocalCategory) {
                // 本地音频分类：显示已通过 picker 添加的本地音频项，使用与其它音频列表完全一致的 SoundPresetCard 样式。
                // 同时在末尾提供“添加”卡片（如果未达上限），点击触发 onPickLocalAudio。
                itemsIndexed(sounds, key = { _, item -> item.id }) { _, item ->
                    SoundPresetCard(
                        preset = item,
                        selected = item.id in selectedSoundIds,
                        cardWidth = cardWidth,
                        compact = compactPhone,
                        onClick = { onLocalSelectedChange(item) },
                    )
                }
                if (sounds.size < 3) {
                    item {
                        AddLocalAudioCard(
                            cardWidth = cardWidth,
                            compact = compactPhone,
                            onClick = onPickLocalAudio,
                        )
                    }
                }
            } else {
                itemsIndexed(sounds, key = { _, item -> item.id }) { _, item ->
                    SoundPresetCard(
                        preset = item,
                        selected = item.id in selectedSoundIds,
                        cardWidth = cardWidth,
                        compact = compactPhone,
                        onClick = { onSelectedChange(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddLocalAudioCard(
    cardWidth: Dp,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    Box(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .selectedBorderGlow(
                selected = false,
                cornerRadius = spec.radii.thumbnail,
                selectedColor = spec.colors.goldSoft,
                unselectedColor = spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong),
                borderWidth = spec.size.stroke,
            )
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .velarisClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        // 模拟 SoundPresetCard 的结构：全尺寸内容区 + 渐变叠加 + 底部文字
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // 中心大图标，模拟封面位置
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = spec.colors.textPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(if (compact) 32.dp else 48.dp),
            )
        }

        // 渐变叠加，保持和其他卡片视觉一致
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.25f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun SoundPresetCard(
    preset: SoundPreset,
    selected: Boolean,
    cardWidth: Dp,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val context = LocalContext.current
    val coverResId = remember(preset.coverResName, context.packageName) {
        preset.coverResName
            ?.let { name ->
                context.resources.getIdentifier(name, "drawable", context.packageName)
                    .takeIf { it != 0 }
            }
            ?: com.wujia.foundation.model.R.drawable.ic_rain_1
    }
    Box(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .selectedBorderGlow(
                selected = selected,
                cornerRadius = spec.radii.thumbnail,
                selectedColor = spec.colors.goldSoft,
                unselectedColor = spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong),
                borderWidth = spec.size.stroke,
            )
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .velarisClickable { onClick() },
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
                text = preset.title,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = if (compact) spec.typography.label else spec.typography.sectionTitle,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(spec.spacing.xSmall))

            Text(
                text = preset.description,
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.caption,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spec.spacing.medium)
                    .size(22.dp)
                    .clip(CircleShape)
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
private fun SoundPlayingBar(
    title: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .velarisClickable { onClick() }
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
            .padding(horizontal = spec.spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.VolumeUp,
            contentDescription = null,
            tint = spec.colors.gold,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(spec.spacing.medium))

        Text(
            text = when {
                title.isBlank() -> stringResource(R.string.scene_edit_select_sound_prompt)
                isPlaying -> stringResource(R.string.scene_edit_previewing_title, title)
                else -> stringResource(R.string.scene_edit_click_to_preview_title, title)
            },
            color = spec.colors.gold,
            fontSize = spec.typography.label,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.width(spec.spacing.xLarge))
    }
}

@LandscapePreviews
@Composable
private fun SceneEditNoiseScreenPreview() {
    SceneEditNoiseContent(
        categories = listOf("自然", "治愈", "专注", NoiseCategory.LOCAL.displayName),
        sounds = listOf(
            SoundPreset(
                ProjectsIds.Noise.RAIN,
                "雨声",
                "雨声轻敲窗棂",
                "",
                "自然",
            ),
        ),
        selectedCategory = 0,
        selectedSoundIds = listOf(ProjectsIds.Noise.RAIN),
        selectedSounds = listOf(
            SoundPreset(
                ProjectsIds.Noise.RAIN,
                "雨声",
                "雨声轻敲窗棂",
                "",
                "自然",
            ),
        ),
        isPreviewPlaying = true,
        onCategoryChange = {},
        onSoundChange = {},
        onSoundPresetChange = {},
        onPreviewToggle = {},
        onPickLocalAudio = {},
    )
}
