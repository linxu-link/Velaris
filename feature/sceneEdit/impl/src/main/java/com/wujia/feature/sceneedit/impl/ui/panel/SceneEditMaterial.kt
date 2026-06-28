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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wujia.feature.sceneedit.impl.ui.viewmodel.MaterialPreset
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.selectedBorderGlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val SCENE_EDIT_MATERIAL_TAG = "SceneEditMaterial"

@LandscapePreviews
@Composable
private fun SceneEditMaterialPreview() {
    SceneEditMaterialContent(
        materials = listOf(
            MaterialPreset(id = ProjectsIds.Scene.TRAIN_NIGHT, title = "雾隐山居"),
            MaterialPreset(id = ProjectsIds.Scene.SNOW_NIGHT, title = "风雪夜归人"),
        ),
        selectedMaterialId = ProjectsIds.Scene.TRAIN_NIGHT,
        selectedMaterial = null,
        selectedSourceTab = 0,
        onSourceTabSelected = {},
        onMaterialSelected = { _ -> },
        onPickLocalImage = {},
        onPickLocalVideo = {},
    )
}

@Composable
internal fun SceneEditMaterialContent(
    materials: List<MaterialPreset>,
    selectedMaterialId: String?,
    selectedMaterial: MaterialPreset? = null,
    selectedSourceTab: Int,
    onSourceTabSelected: (Int) -> Unit,
    onMaterialSelected: (MaterialPreset) -> Unit,
    onPickLocalImage: () -> Unit,
    onPickLocalVideo: () -> Unit,
) {
    val spec = VelarisTheme.spec
    LaunchedEffect(
        selectedSourceTab,
        materials.size,
    ) {
        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d(
            "ui tab=%d materialCount=%d selectedId=%s",
            selectedSourceTab,
            materials.size,
            selectedMaterialId,
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(spec.radii.thumbnail))
                .border(
                    spec.size.stroke,
                    spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                    RoundedCornerShape(spec.radii.thumbnail),
                )
                .background(spec.colors.surfaceSubtle.copy(alpha = 0.32f))
                .padding(spec.spacing.medium),
        ) {
            SourceTabs(
                tabs = listOf(stringResource(R.string.scene_edit_preset_material), stringResource(R.string.scene_edit_local_image), stringResource(R.string.scene_edit_local_video)),
                selectedIndex = selectedSourceTab,
                onSelectedChange = onSourceTabSelected,
            )

            Spacer(Modifier.height(spec.spacing.medium))

            when {
                selectedSourceTab == 0 -> {
                    MaterialGrid(
                        materials = materials,
                        selectedMaterialId = selectedMaterialId,
                        onItemSelected = onMaterialSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }

                // 对于本地图片/视频，使用系统 Photo Picker，而非请求广域权限并加载整个媒体库。
                // 这是合规推荐的做法。选完后这里会显示已选的本地素材（而不是一直显示提示），
                // 这样用户返回后能立刻看到选中的图片/视频。
                selectedSourceTab == 1 -> {
                    val localImage = if (selectedMaterial != null && selectedMaterial.typeLabel == "image" && selectedMaterial.backgroundUri != null) {
                        selectedMaterial
                    } else {
                        null
                    }
                    val localList = if (localImage != null) listOf(localImage) else emptyList()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        if (localList.isNotEmpty()) {
                            MaterialGrid(
                                materials = localList,
                                selectedMaterialId = selectedMaterialId,
                                onItemSelected = onMaterialSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                        }
                        LocalPickPrompt(
                            label = if (localList.isNotEmpty()) "更换本地图片" else stringResource(R.string.scene_edit_pick_local_image),
                            onPick = onPickLocalImage,
                            modifier = if (localList.isNotEmpty()) {
                                Modifier.fillMaxWidth().height(64.dp)
                            } else {
                                Modifier.fillMaxWidth().weight(1f)
                            },
                        )
                    }
                }

                selectedSourceTab == 2 -> {
                    val localVideo = if (selectedMaterial != null && selectedMaterial.typeLabel == "video" && selectedMaterial.videoUri != null) {
                        selectedMaterial
                    } else {
                        null
                    }
                    val localList = if (localVideo != null) listOf(localVideo) else emptyList()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        if (localList.isNotEmpty()) {
                            MaterialGrid(
                                materials = localList,
                                selectedMaterialId = selectedMaterialId,
                                onItemSelected = onMaterialSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                        }
                        LocalPickPrompt(
                            label = if (localList.isNotEmpty()) "更换本地视频" else stringResource(R.string.scene_edit_pick_local_video),
                            onPick = onPickLocalVideo,
                            modifier = if (localList.isNotEmpty()) {
                                Modifier.fillMaxWidth().height(64.dp)
                            } else {
                                Modifier.fillMaxWidth().weight(1f)
                            },
                        )
                    }
                }

                else -> MaterialPlaceholder(
                    text = stringResource(R.string.scene_edit_need_permission),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MaterialPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
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
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.body,
        )
    }
}

@Composable
private fun LocalPickPrompt(
    label: String,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.5f))
            .border(
                spec.size.stroke,
                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                RoundedCornerShape(spec.radii.thumbnail),
            )
            .velarisClickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = spec.colors.textPrimary,
                fontSize = spec.typography.body,
            )
            Spacer(Modifier.height(spec.spacing.small))
            Text(
                text = "点击从设备选择",
                color = spec.colors.textSecondary,
                fontSize = spec.typography.caption,
            )
        }
    }
}

@Composable
fun SourceTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, text ->
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .velarisClickable { onSelectedChange(index) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = text,
                        color = if (index == selectedIndex) {
                            spec.colors.gold
                        } else {
                            spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
                        },
                        fontSize = spec.typography.body,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(spec.spacing.small))

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(spec.radii.pill))
                            .background(
                                if (index == selectedIndex) {
                                    spec.colors.gold
                                } else {
                                    Color.Transparent
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun MaterialItemCard(
    material: MaterialPreset,
    selected: Boolean,
    onClick: () -> Unit,
    onThumbnailLoadFailed: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
) {
    val spec = VelarisTheme.spec
    val context = LocalContext.current
    val coverResId = remember(material.coverResName, context.packageName) {
        material.coverResName
            ?.takeIf { it.isNotBlank() }
            ?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(spec.radii.thumbnail + spec.spacing.xSmall))
            .background(spec.colors.surfaceSubtle.copy(alpha = 0.42f))
            .selectedBorderGlow(
                selected = selected,
                cornerRadius = spec.radii.thumbnail + spec.spacing.xSmall,
                selectedColor = spec.colors.goldSoft,
                unselectedColor = spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong),
                borderWidth = spec.size.stroke,
            )
            .velarisClickable { onClick() },
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
        ) {
            if (coverResId != null) {
                Image(
                    painter = painterResource(id = coverResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            } else {
                MediaThumbnail(
                    uri = material.thumbnailUri ?: material.backgroundUri ?: material.videoUri,
                    onLoadFailed = onThumbnailLoadFailed,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
        }

        if (material.typeLabel.isNotBlank()) {
            MaterialTypeBadge(
                text = material.typeLabel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(spec.spacing.small),
            )
        }

        if (material.durationText.isNotBlank()) {
            MaterialTypeBadge(
                text = material.durationText,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spec.spacing.small),
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spec.spacing.small)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(spec.colors.gold),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = spec.colors.onGold,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun MaterialTypeBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spec.radii.badge))
            .background(Color.Black.copy(alpha = 0.54f))
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                shape = RoundedCornerShape(spec.radii.badge),
            )
            .padding(horizontal = spec.spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = spec.colors.textPrimary,
            fontSize = spec.typography.caption,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MediaThumbnail(
    uri: String?,
    onLoadFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(uri) {
        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail LaunchedEffect start, uri=$uri")

        if (uri == null) {
            bitmap = null
            Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail uri==null, skip")
            return@LaunchedEffect
        }

        bitmap = runCatching {
            withContext(Dispatchers.IO) {
                val parsed = uri.toUri()
                Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail parsed=$parsed, scheme=${parsed.scheme}")

                if (parsed.scheme == "android.resource") {
                    Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail -> loadScaledBitmap (resource)")
                    context.contentResolver.loadScaledBitmap(parsed, 360, 240)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail attempting loadThumbnail (Q+)")
                    val thumb = runCatching {
                        context.contentResolver.loadThumbnail(parsed, Size(360, 240), null)
                    }.getOrNull()
                    Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail loadThumbnail result: ${if (thumb != null) "SUCCESS ${thumb.width}x${thumb.height}" else "null"}")

                    if (thumb != null) {
                        thumb
                    } else if (parsed.scheme == "file") {
                        // Local video from picker (file://) — loadThumbnail often returns null for raw file videos.
                        // Use MediaMetadataRetriever to extract a frame. This is the proper way for video files.
                        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail trying MediaMetadataRetriever for local video file")
                        runCatching {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(context, parsed)
                            val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            retriever.release()
                            if (frame != null) {
                                // Scale down to target size (similar to other paths)
                                val targetW = 360
                                val targetH = 240
                                val scaled = if (frame.width > targetW || frame.height > targetH) {
                                    Bitmap.createScaledBitmap(frame, targetW, targetH, true).also {
                                        if (it != frame) frame.recycle()
                                    }
                                } else {
                                    frame
                                }
                                Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail retriever got frame ${scaled.width}x${scaled.height}")
                                scaled
                            } else {
                                Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail retriever returned null frame")
                                null
                            }
                        }.getOrNull() ?: runCatching {
                            Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail final fallback to loadScaledBitmap for file")
                            context.contentResolver.loadScaledBitmap(parsed, 360, 240)
                        }.getOrNull()
                    } else {
                        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail fallback to loadScaledBitmap")
                        context.contentResolver.loadScaledBitmap(parsed, 360, 240)
                    }
                } else {
                    Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail -> loadScaledBitmap (pre-Q)")
                    context.contentResolver.loadScaledBitmap(parsed, 360, 240)
                }
            }
        }.getOrNull()

        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail final bitmap: ${if (bitmap != null) "SUCCESS ${bitmap!!.width}x${bitmap!!.height}" else "null -> placeholder"}")

        if (bitmap == null) {
            Timber.tag(SCENE_EDIT_MATERIAL_TAG).d("MediaThumbnail calling onLoadFailed()")
            onLoadFailed()
        }
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        VelarisColor.ThumbnailGrayDark,
                        VelarisColor.ThumbnailNearBlack,
                    ),
                ),
            ),
        )
    }
}

private fun ContentResolver.loadScaledBitmap(
    uri: Uri,
    targetWidth: Int,
    targetHeight: Int,
): Bitmap? {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = bounds.calculateInSampleSize(targetWidth, targetHeight)
    }
    return openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    }
}

private fun BitmapFactory.Options.calculateInSampleSize(
    targetWidth: Int,
    targetHeight: Int,
): Int {
    var sampleSize = 1
    var halfWidth = outWidth / 2
    var halfHeight = outHeight / 2
    while (halfWidth / sampleSize >= targetWidth && halfHeight / sampleSize >= targetHeight) {
        sampleSize *= 2
    }
    return sampleSize
}

@Composable
internal fun MaterialGrid(
    materials: List<MaterialPreset>,
    selectedMaterialId: String?,
    onItemSelected: (MaterialPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    LaunchedEffect(materials.size, selectedMaterialId) {
        Timber.tag(SCENE_EDIT_MATERIAL_TAG).d(
            "grid visibleCount=%d selectedId=%s firstIds=%s",
            materials.size,
            selectedMaterialId,
            materials.take(5).map { it.id },
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val minCellWidth = when {
            maxWidth < 560.dp -> 124.dp
            maxWidth < 840.dp -> 140.dp
            else -> 156.dp
        }
        val itemHeight = when {
            maxHeight < 220.dp -> 88.dp
            maxHeight < 300.dp -> 104.dp
            else -> 120.dp
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellWidth),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spec.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spec.spacing.medium),
        ) {
            items(materials, key = { it.id }) { material ->
                MaterialItemCard(
                    material = material,
                    selected = material.id == selectedMaterialId,
                    onClick = { onItemSelected(material) },
                    onThumbnailLoadFailed = {},
                    height = itemHeight,
                )
            }
        }
    }
}
