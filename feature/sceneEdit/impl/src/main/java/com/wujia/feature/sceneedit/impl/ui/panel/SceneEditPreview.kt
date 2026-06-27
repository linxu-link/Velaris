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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.feature.sceneedit.impl.ui.viewmodel.MaterialPreset
import com.wujia.feature.sceneedit.impl.ui.viewmodel.ParticlePreset
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SoundPreset
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.particle.ParticleEffect
import com.wujia.foundation.player.AudioMediaItem
import com.wujia.foundation.player.VelarisVideoPlayer
import com.wujia.foundation.player.rememberVelarisPlayerController
import com.wujia.foundation.player.rememberVideoFirstFrame
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.rememberContentBitmap

@LandscapePreviews
@Composable
private fun ScenePreviewEditPreviewContent() {
    SceneEditPreviewContent(
        title = "雾隐山居",
        description = "薄雾穿林，清风醒神",
        material = MaterialPreset(ProjectsIds.Scene.TRAIN_NIGHT, "雾隐山居"),
        sounds = listOf(
            SoundPreset(
                ProjectsIds.Noise.RAIN,
                "雨声",
                "雨声轻敲窗棂",
                "",
                "自然",
            ),
        ),
        onTitleChange = {},
        onDescriptionChange = {},
    )
}

@Composable
internal fun SceneEditPreviewContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    material: MaterialPreset?,
    sounds: List<SoundPreset>,
    particle: ParticlePreset? = null,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
) {
    val spec = VelarisTheme.spec
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spec.spacing.large),
        ) {
            SceneInfoPanel(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(),
                title = title,
                description = description,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
            )

            ScenePreviewPanel(
                title = title,
                material = material,
                sounds = sounds,
                particle = particle,
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
            )
        }

        Spacer(Modifier.height(spec.spacing.medium))
    }
}

@Composable
private fun SceneInfoPanel(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .padding(spec.spacing.small),
    ) {
        PanelTitle(
            icon = Icons.Outlined.EditNote,
            text = stringResource(R.string.scene_edit_scene_info),
        )

        Spacer(Modifier.height(spec.spacing.medium))

        FieldLabel(stringResource(R.string.scene_edit_title_label))
        EditableField(
            text = title,
            onTextChange = onTitleChange,
            modifier = Modifier.height(52.dp),
            placeholder = stringResource(R.string.scene_edit_title_placeholder),
            singleLine = true,
            textFieldAlignment = EditableFieldAlignment.Center,
        )

        Spacer(Modifier.height(spec.spacing.medium))

        FieldLabel(stringResource(R.string.scene_edit_description))

        EditableField(
            text = description,
            onTextChange = onDescriptionChange,
            modifier = Modifier.weight(1f),
            placeholder = stringResource(R.string.scene_edit_description_placeholder),
            textFieldAlignment = EditableFieldAlignment.Top,
        )
    }
}

@Composable
private fun ScenePreviewPanel(
    title: String,
    material: MaterialPreset?,
    sounds: List<SoundPreset>,
    particle: ParticlePreset? = null,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val soundKey = sounds.joinToString("|") { it.id }
    var isPlaying by remember(material?.id, soundKey) { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current
    val audioItems = if (!inspectionMode && isPlaying) {
        sounds.filter { it.uri.isNotBlank() }.map { sound ->
            AudioMediaItem(
                id = sound.id,
                uri = sound.uri,
                title = sound.title,
                loop = true,
            )
        }
    } else {
        emptyList()
    }
    val controller = if (inspectionMode) {
        null
    } else {
        rememberVelarisPlayerController(
            videoUri = material?.videoUri,
            audioItems = audioItems,
            playWhenReady = isPlaying,
            // 预览控制器在 dispose 时会自动完整释放资源（releaseVideoOnly 死分支已删除）。
        )
    }

    // 非播放预览状态下使用首帧静态图（类似主场景），避免 videoUri 绑定就 prepare 视频播放器产生音频日志。
    // 仅点击播放按钮后才走 playWhenReady -> play() -> ensure prepare + 渲染实时视频。
    val previewVideoFirstFrame = if (!inspectionMode && material?.videoUri != null && !isPlaying) {
        rememberVideoFirstFrame(material.videoUri)
    } else {
        null
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .padding(spec.spacing.small),
    ) {
        PanelTitle(
            icon = Icons.Outlined.Visibility,
            text = stringResource(R.string.scene_edit_preview),
        )

        Spacer(Modifier.height(spec.spacing.medium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(spec.radii.thumbnail))
                .background(
                    Brush.linearGradient(
                        listOf(VelarisColor.PreviewGreenDark, VelarisColor.PreviewGreenDeep),
                    ),
                )
                .border(
                    spec.size.stroke,
                    spec.colors.gold.copy(alpha = 0.55f),
                    RoundedCornerShape(spec.radii.thumbnail),
                ),
        ) {
            SceneMaterialPreviewLayer(
                material = material,
                modifier = Modifier.matchParentSize(),
            )

            if (previewVideoFirstFrame != null) {
                // 预览闲置：显示首帧图（不挂载播放器，不 prepare）
                Image(
                    bitmap = previewVideoFirstFrame,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            } else if (material?.videoUri != null && controller != null && isPlaying) {
                // 仅播放中挂载实时播放器（此时 play() 已触发 prepare）
                VelarisVideoPlayer(
                    controller = controller,
                    modifier = Modifier.matchParentSize(),
                )
            } else if (material?.videoUri != null) {
                ScenePreviewFallback(modifier = Modifier.matchParentSize())
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(spec.spacing.large)
                    .fillMaxWidth()
                    .height(50.dp)
                    .velarisClickable { isPlaying = !isPlaying }
                    .clip(RoundedCornerShape(spec.radii.thumbnail))
                    .background(Color.Black.copy(alpha = 0.42f))
                    .border(
                        spec.size.stroke,
                        spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                        RoundedCornerShape(spec.radii.thumbnail),
                    )
                    .padding(horizontal = spec.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(spec.size.stroke, spec.colors.gold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null,
                        tint = spec.colors.gold,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(Modifier.width(spec.spacing.large))

                Text(
                    title.ifBlank { stringResource(R.string.scene_edit_title_placeholder) },
                    color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                    fontSize = spec.typography.label,
                )

                Spacer(Modifier.width(spec.spacing.large + spec.spacing.xSmall))

                Text(
                    sounds.joinToString(" / ") { it.title }
                        .ifBlank { stringResource(R.string.scene_edit_no_sound_selected) },
                    color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                    fontSize = spec.typography.caption,
                )

                // 粒子效果信息
                if (particle != null && particle.effect != ParticleEffect.NONE) {
                    Spacer(Modifier.width(spec.spacing.small))
                    Text(
                        text = " | ${particle.title}",
                        color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                        fontSize = spec.typography.caption,
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneMaterialPreviewLayer(
    material: MaterialPreset?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = rememberContentBitmap(material?.backgroundUri)
    when {
        material?.videoUri != null -> Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        VelarisColor.MaterialSlateDark,
                        VelarisColor.MaterialNearBlack,
                    ),
                ),
            ),
        )

        bitmap != null -> Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )

        material?.backgroundResName != null -> {
            val resId = remember(material.backgroundResName) {
                context.resources.getIdentifier(
                    material.backgroundResName,
                    "drawable",
                    context.packageName,
                )
            }
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier,
                )
            } else {
                ScenePreviewFallback(modifier)
            }
        }

        else -> ScenePreviewFallback(modifier)
    }
}

@Composable
private fun ScenePreviewFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(VelarisColor.PreviewGreenDark, VelarisColor.PreviewGreenDeep),
            ),
        ),
    )
}

@Composable
private fun PanelTitle(icon: ImageVector, text: String) {
    val spec = VelarisTheme.spec
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = spec.colors.gold, modifier = Modifier.size(spec.size.iconSmall))
        Spacer(Modifier.width(spec.spacing.small))
        Text(
            text,
            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.body,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    val spec = VelarisTheme.spec
    Text(
        text,
        color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
        fontSize = spec.typography.bodySmall,
    )
}

private enum class EditableFieldAlignment {
    Center,
    Top,
}

@Composable
private fun EditableField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = false,
    textFieldAlignment: EditableFieldAlignment = EditableFieldAlignment.Center,
) {
    val spec = VelarisTheme.spec
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .background(Color.Black.copy(alpha = 0.12f))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            ),
    ) {
        val rowModifier = when (textFieldAlignment) {
            EditableFieldAlignment.Center -> Modifier.align(Alignment.CenterStart)
            EditableFieldAlignment.Top -> Modifier.align(Alignment.TopStart)
        }

        Row(
            modifier = rowModifier
                .fillMaxWidth()
                .padding(
                    start = spec.spacing.medium,
                    top = if (textFieldAlignment == EditableFieldAlignment.Top) spec.spacing.small else 0.dp,
                    end = spec.spacing.medium,
                    bottom = if (textFieldAlignment == EditableFieldAlignment.Top) spec.spacing.small else 0.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = singleLine,
                placeholder = if (placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            color = spec.colors.textPrimary.copy(alpha = spec.alpha.textSecondary),
                            fontSize = spec.typography.label,
                        )
                    }
                } else {
                    null
                },
                textStyle = TextStyle(
                    color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                    fontSize = spec.typography.label,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = spec.colors.gold,
                ),
                modifier = Modifier.weight(1f),
            )

            Icon(
                tint = spec.colors.gold,
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
