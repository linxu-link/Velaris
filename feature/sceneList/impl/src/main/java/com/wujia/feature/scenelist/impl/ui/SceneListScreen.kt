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
package com.wujia.feature.scenelist.impl.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.wujia.feature.scenelist.impl.entity.SceneListItem
import com.wujia.feature.scenelist.impl.ui.component.AddSceneCard
import com.wujia.feature.scenelist.impl.ui.component.CategoryTabs
import com.wujia.feature.scenelist.impl.ui.component.SceneListCard
import com.wujia.feature.scenelist.impl.ui.component.verticalFade
import com.wujia.feature.scenelist.impl.ui.viewmodel.SceneListViewModel
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneCategory
import timber.log.Timber
import kotlin.math.abs
import com.wujia.foundation.ui.R as UiR

/**
 * 场景列表主 Screen。
 * 负责：
 * - 通过 LaunchedEffect 响应外部传入的 category 参数
 * - 连接 ViewModel（状态 + 事件）
 * - 渲染具体内容 SceneListScreenContent
 *
 * 通常由 SceneListPanel 或 sceneListEntry 包裹使用。
 */
@Composable
internal fun SceneListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: SceneCategory? = null,
    onAddScene: (SceneCategory?) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSceneClick: (String, SceneCategory?) -> Unit = { _, _ -> },
    viewModel: SceneListViewModel = hiltViewModel(),
) {
    LaunchedEffect(category) {
        viewModel.setCategory(category)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SceneListScreenContent(
        items = uiState.items,
        category = uiState.category ?: SceneCategory.FOCUS,
        onBackClick = onBackClick,
        onAddScene = onAddScene,
        onOpenSettings = onOpenSettings,
        onSceneClick = onSceneClick,
        onReorder = viewModel::onSceneReorder,
        onDelete = viewModel::onSceneDelete,
        onCategoryChange = viewModel::onCategoryChange,
        modifier = modifier,
    )
}

@Composable
internal fun SceneListScreenContent(
    items: List<SceneListItem>,
    onBackClick: () -> Unit,
    onAddScene: (SceneCategory?) -> Unit,
    onOpenSettings: () -> Unit,
    onSceneClick: (String, SceneCategory?) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onDelete: (String) -> Unit,
    onCategoryChange: (SceneCategory) -> Unit,
    modifier: Modifier = Modifier,
    category: SceneCategory = SceneCategory.FOCUS,
    windowSizeClass: WindowSizeClass = currentSceneListWindowSizeClass(),
) {
    val gridState = rememberLazyGridState()
    val dragState = remember { SceneListDragState() }
    val density = LocalDensity.current
    val autoScrollThresholdPx = with(density) { 64.dp.toPx() }
    val draggedElevationPx = with(density) { 20.dp.toPx() }
    var autoScrollDelta by remember { mutableFloatStateOf(0f) }
    var dragEventCount by remember { mutableIntStateOf(0) }
    var isManageMode by remember { mutableStateOf(false) }

    LaunchedEffect(items, dragState.draggingItemId) {
        if (!dragState.isDragging) {
            Timber.tag(SCENE_LIST_DRAG_TAG).d(
                "sync items count=%d ids=%s",
                items.size,
                items.joinToStringIds(),
            )
        }
        dragState.syncItems(items)
    }

    LaunchedEffect(dragState.isDragging, autoScrollDelta) {
        while (dragState.isDragging && autoScrollDelta != 0f) {
            val consumed = gridState.scrollBy(autoScrollDelta)
            if (consumed != 0f) {
                gridState.layoutInfo.findDragTargetIndex(dragState)?.let { targetIndex ->
                    dragState.movePreview(targetIndex)?.let { (fromIndex, toIndex) ->
                        Timber.tag(SCENE_LIST_DRAG_TAG).d(
                            "auto-scroll preview move id=%s from=%d to=%d consumed=%.1f center=%s visible=%s",
                            dragState.draggingItemId,
                            fromIndex,
                            toIndex,
                            consumed,
                            dragState.dragCenter().shortString(),
                            gridState.layoutInfo.visibleItemsInfo.debugVisibleItems(),
                        )
                    }
                }
            } else {
                val requestedDelta = autoScrollDelta
                autoScrollDelta = 0f
                Timber.tag(SCENE_LIST_DRAG_TAG).d(
                    "auto-scroll stopped no-consume requested=%.1f center=%s viewport=%d..%d",
                    requestedDelta,
                    dragState.dragCenter().shortString(),
                    gridState.layoutInfo.viewportStartOffset,
                    gridState.layoutInfo.viewportEndOffset,
                )
            }
            withFrameNanos { }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spec = VelarisTheme.spec
        val profile = sceneListLayoutProfile(
            windowSizeClass = windowSizeClass,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        )
        val dragOverscan = if (profile.edgePadding < 10.dp) profile.edgePadding else 10.dp
        val gridOuterHorizontalPadding = profile.edgePadding - dragOverscan
        val headerToGridSpacing = (profile.verticalSpacing - dragOverscan).coerceAtLeast(0.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            SceneListHeader(
                category = category,
                onBackClick = onBackClick,
                onOpenSettings = onOpenSettings,
                edgePadding = profile.edgePadding,
            )

            CategoryTabs(
                selectedCategory = category,
                onCategoryChange = onCategoryChange,
                modifier = Modifier.padding(horizontal = profile.edgePadding),
            )

            Spacer(modifier = Modifier.height(headerToGridSpacing))

            if (items.isEmpty() && !isManageMode) {
                // 空状态展示（高优先可读性改进）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(profile.edgePadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        // TODO: 建议添加 string 资源 scene_list_empty
                        text = "暂无场景",
                        color = spec.colors.textMuted,
                        fontSize = spec.typography.body,
                    )
                }
            } else {
                SceneListGrid(
                    items = items,
                    gridState = gridState,
                    dragState = dragState,
                    isManageMode = isManageMode,
                    autoScrollThresholdPx = autoScrollThresholdPx,
                    draggedElevationPx = draggedElevationPx,
                    autoScrollDelta = autoScrollDelta,
                    dragEventCount = dragEventCount,
                    profile = profile,
                    dragOverscan = dragOverscan,
                    outerHorizontalPadding = gridOuterHorizontalPadding,
                    category = category,
                    onReorder = onReorder,
                    onDelete = onDelete,
                    onAddScene = onAddScene,
                    onSceneClick = onSceneClick,
                    onManageModeChange = { isManageMode = it },
                    onAutoScrollDeltaChange = { autoScrollDelta = it },
                    onDragEventCountChange = { dragEventCount = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun currentSceneListWindowSizeClass(): WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

@Composable
private fun SceneListHeader(
    category: SceneCategory?,
    onBackClick: () -> Unit,
    onOpenSettings: () -> Unit,
    edgePadding: androidx.compose.ui.unit.Dp,
) {
    val spec = VelarisTheme.spec
    Row(
        modifier = Modifier.padding(horizontal = edgePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category?.let { stringResource(UiR.string.scene_category_title_format, it.displayName) }
                ?: stringResource(UiR.string.scene_list_title),
            color = spec.colors.goldSoft.copy(alpha = spec.alpha.textPrimary),
            fontSize = spec.typography.title,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(spec.spacing.large))
        Text(
            text = stringResource(UiR.string.scene_list_drag_hint),
            color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
            fontSize = spec.typography.body,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(UiR.string.common_settings),
                tint = spec.colors.gold.copy(alpha = spec.alpha.icon),
            )
        }
    }
}

@Composable
private fun SceneListGrid(
    items: List<SceneListItem>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    dragState: SceneListDragState,
    isManageMode: Boolean,
    autoScrollThresholdPx: Float,
    draggedElevationPx: Float,
    autoScrollDelta: Float,
    dragEventCount: Int,
    profile: SceneListLayoutProfile,
    dragOverscan: androidx.compose.ui.unit.Dp,
    outerHorizontalPadding: androidx.compose.ui.unit.Dp,
    category: SceneCategory?,
    onReorder: (Int, Int) -> Unit,
    onDelete: (String) -> Unit,
    onAddScene: (SceneCategory?) -> Unit,
    onSceneClick: (String, SceneCategory?) -> Unit,
    onManageModeChange: (Boolean) -> Unit,
    onAutoScrollDeltaChange: (Float) -> Unit,
    onDragEventCountChange: (Int) -> Unit,
) {
    val spec = VelarisTheme.spec
    Box(
        modifier = Modifier
            .padding(horizontal = outerHorizontalPadding)
            .fillMaxSize()
            .pointerInput(isManageMode) {
                detectTapGestures(
                    onTap = { offset ->
                        val tappedItem = gridState.layoutInfo.visibleItemsInfo
                            .any { item -> item.contains(offset) }
                        if (isManageMode && !tappedItem) {
                            onManageModeChange(false)
                        }
                    },
                )
            },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = profile.minCellWidth),
            state = gridState,
            contentPadding = PaddingValues(
                horizontal = dragOverscan,
                vertical = dragOverscan,
            ),
            modifier = Modifier
                .fillMaxSize()
                .verticalFade(
                    fadeHeight = spec.spacing.xLarge + spec.spacing.medium,
                    showTopFade = gridState.canScrollBackward,
                    showBottomFade = gridState.canScrollForward,
                ),
            verticalArrangement = Arrangement.spacedBy(profile.gridSpacing),
            horizontalArrangement = Arrangement.spacedBy(profile.gridSpacing),
        ) {
            val displayedItems = if (dragState.shouldUseVisualItems) {
                dragState.visualItems
            } else {
                items
            }
            itemsIndexed(
                items = displayedItems,
                key = { _, item -> item.id },
            ) { _, scene ->
                val isDragging = dragState.draggingItemId == scene.id
                val canDrag = items.size > 1
                val itemInfo = gridState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.key == scene.id }
                val draggedTranslation = if (isDragging && itemInfo != null) {
                    dragState.draggedTranslation(itemInfo)
                } else {
                    Offset.Zero
                }
                var cardModifier = Modifier
                    .fillMaxWidth()
                    .height(profile.cardHeight)

                if (canDrag) {
                    cardModifier = cardModifier.pointerInput(scene.id) {
                        var localDragEventCount = 0
                        var ignoreDrag = false
                        var pendingInitialOffset: IntOffset? = null
                        val thresholdPx = with(density) { MIN_DRAG_THRESHOLD_PX.dp.toPx() }
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                // 长按启动拖拽：进入管理模式，记录初始位置，准备视觉预览
                                val currentInfo = gridState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.key == scene.id }
                                    ?: return@detectDragGesturesAfterLongPress
                                onManageModeChange(true)
                                localDragEventCount = 0
                                onDragEventCountChange(0)
                                ignoreDrag = false
                                pendingInitialOffset = currentInfo.offset
                                Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                    "start id=%s initialIndex=%d itemOffset=%s itemSize=%s viewport=%d..%d visible=%s visual=%s",
                                    scene.id,
                                    items.indexOfFirst { it.id == scene.id },
                                    currentInfo.offset,
                                    currentInfo.size,
                                    gridState.layoutInfo.viewportStartOffset,
                                    gridState.layoutInfo.viewportEndOffset,
                                    gridState.layoutInfo.visibleItemsInfo.debugVisibleItems(),
                                    dragState.visualItems.joinToStringIds(),
                                )
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (ignoreDrag) return@detectDragGesturesAfterLongPress

                                if (dragState.isDragging) {
                                    // 阈值已通过，正常拖拽处理（更新 offset、预览移动、自动滚动）
                                } else if (pendingInitialOffset != null) {
                                    // 还在阈值判断阶段（MIN_DRAG_THRESHOLD_PX），避免误触
                                    val accumulated = dragState.dragOffset.x + dragAmount.x
                                    val accumulatedY = dragState.dragOffset.y + dragAmount.y
                                    if (abs(accumulated) < thresholdPx && abs(accumulatedY) < thresholdPx) {
                                        return@detectDragGesturesAfterLongPress
                                    }
                                    // 超过阈值，正式启动拖拽，进入管理模式
                                    val initial = pendingInitialOffset!!
                                    pendingInitialOffset = null
                                    dragState.startDrag(
                                        itemId = scene.id,
                                        items = items,
                                        itemOffset = initial,
                                        itemSize = gridState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { it.key == scene.id }
                                            ?.size ?: return@detectDragGesturesAfterLongPress,
                                    )
                                } else {
                                    ignoreDrag = true
                                    onAutoScrollDeltaChange(0f)
                                    onManageModeChange(false)
                                    dragState.cancelDrag()
                                    return@detectDragGesturesAfterLongPress
                                }

                                onManageModeChange(false)
                                localDragEventCount++
                                onDragEventCountChange(localDragEventCount)
                                dragState.dragBy(dragAmount)
                                val targetIndex =
                                    gridState.layoutInfo.findDragTargetIndex(dragState)
                                targetIndex?.let { index ->
                                    dragState.movePreview(index)
                                        ?.let { (fromIndex, toIndex) ->
                                            Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                                "preview move id=%s from=%d to=%d center=%s offset=%s visual=%s visible=%s",
                                                scene.id,
                                                fromIndex,
                                                toIndex,
                                                dragState.dragCenter().shortString(),
                                                dragState.dragOffset.shortString(),
                                                dragState.visualItems.joinToStringIds(),
                                                gridState.layoutInfo.visibleItemsInfo.debugVisibleItems(),
                                            )
                                        }
                                }
                                val nextAutoScrollDelta =
                                    gridState.layoutInfo.autoScrollDelta(
                                        dragCenterY = dragState.dragCenter().y,
                                        thresholdPx = autoScrollThresholdPx,
                                        canScrollBackward = gridState.canScrollBackward,
                                        canScrollForward = gridState.canScrollForward,
                                    ) ?: 0f
                                if (nextAutoScrollDelta != autoScrollDelta) {
                                    Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                        "auto-scroll delta %.1f -> %.1f center=%s viewport=%d..%d target=%s",
                                        autoScrollDelta,
                                        nextAutoScrollDelta,
                                        dragState.dragCenter().shortString(),
                                        gridState.layoutInfo.viewportStartOffset,
                                        gridState.layoutInfo.viewportEndOffset,
                                        targetIndex,
                                    )
                                }
                                onAutoScrollDeltaChange(nextAutoScrollDelta)
                                if (localDragEventCount % DRAG_LOG_SAMPLE_RATE == 0) {
                                    val currentInfo = gridState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == scene.id }
                                    Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                        "drag sample id=%s event=%d amount=%s offset=%s center=%s itemOffset=%s translation=%s target=%s visible=%s",
                                        scene.id,
                                        localDragEventCount,
                                        dragAmount.shortString(),
                                        dragState.dragOffset.shortString(),
                                        dragState.dragCenter().shortString(),
                                        currentInfo?.offset,
                                        currentInfo?.let(dragState::draggedTranslation)
                                            ?.shortString(),
                                        targetIndex,
                                        gridState.layoutInfo.visibleItemsInfo.debugVisibleItems(),
                                    )
                                }
                            },
                            onDragEnd = {
                                onAutoScrollDeltaChange(0f)
                                val result = dragState.endDrag()
                                Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                    "end id=%s result=%s visualBeforeClear=%s",
                                    scene.id,
                                    result,
                                    dragState.visualItems.joinToStringIds(),
                                )
                                result?.let { (fromIndex, toIndex) ->
                                    onReorder(fromIndex, toIndex)
                                }
                            },
                            onDragCancel = {
                                onAutoScrollDeltaChange(0f)
                                if (dragState.isDragging) {
                                    Timber.tag(SCENE_LIST_DRAG_TAG).d(
                                        "cancel id=%s offset=%s visual=%s",
                                        scene.id,
                                        dragState.dragOffset.shortString(),
                                        dragState.visualItems.joinToStringIds(),
                                    )
                                    dragState.cancelDrag()
                                }
                            },
                        )
                    }
                }

                if (!isManageMode) {
                    cardModifier = cardModifier.pointerInput(scene.id) {
                        detectTapGestures(
                            onTap = { onSceneClick(scene.id, scene.sceneCategory) },
                        )
                    }
                }

                cardModifier = if (isDragging) {
                    cardModifier
                        .zIndex(1f)
                        .graphicsLayer {
                            translationX = draggedTranslation.x
                            translationY = draggedTranslation.y
                            scaleX = 1.03f
                            scaleY = 1.03f
                            shadowElevation = draggedElevationPx
                        }
                } else {
                    cardModifier.animateItem()
                }

                SceneListCard(
                    item = scene,
                    isManageMode = isManageMode,
                    onDeleteClick = {
                        onDelete(scene.id)
                    },
                    modifier = cardModifier,
                )
            }

            item(key = "add-scene") {
                AddSceneCard(
                    onClick = { onAddScene(category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(profile.cardHeight),
                )
            }
        }
    }
}

@LandscapePreviews
@Composable
private fun SceneListScreenPreview() {
    SceneListScreenContent(
        items = sampleSceneListItems(),
        onBackClick = {},
        onAddScene = {},
        onOpenSettings = {},
        onSceneClick = { _, _ -> },
        onReorder = { _, _ -> },
        onDelete = {},
        onCategoryChange = {},
    )
}

private fun SceneListDragState.draggedTranslation(itemInfo: LazyGridItemInfo): Offset {
    val draggedTopLeft = Offset(
        x = draggedInitialItemOffset.x + dragOffset.x,
        y = draggedInitialItemOffset.y + dragOffset.y,
    )
    return draggedTopLeft - Offset(itemInfo.offset.x.toFloat(), itemInfo.offset.y.toFloat())
}

private fun SceneListDragState.dragCenter(): Offset = Offset(
    x = draggedInitialItemOffset.x + draggedItemSize.width / 2f + dragOffset.x,
    y = draggedInitialItemOffset.y + draggedItemSize.height / 2f + dragOffset.y,
)

private fun LazyGridLayoutInfo.findDragTargetIndex(
    dragState: SceneListDragState,
): Int? {
    val draggingItemId = dragState.draggingItemId ?: return null
    val dragCenter = dragState.dragCenter()
    return visibleItemsInfo
        .firstOrNull { item -> item.key != draggingItemId && item.contains(dragCenter) }
        ?.index
}

private fun LazyGridLayoutInfo.autoScrollDelta(
    dragCenterY: Float,
    thresholdPx: Float,
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
): Float? {
    val topEdge = viewportStartOffset + thresholdPx
    val bottomEdge = viewportEndOffset - thresholdPx
    val delta = when {
        dragCenterY < topEdge && canScrollBackward ->
            -MAX_AUTO_SCROLL_STEP_PX * ((topEdge - dragCenterY) / thresholdPx)

        dragCenterY > bottomEdge && canScrollForward ->
            MAX_AUTO_SCROLL_STEP_PX * ((dragCenterY - bottomEdge) / thresholdPx)

        else -> return null
    }
    return delta.coerceIn(-MAX_AUTO_SCROLL_STEP_PX, MAX_AUTO_SCROLL_STEP_PX)
}

private fun LazyGridItemInfo.contains(offset: Offset): Boolean = offset.x >= this.offset.x &&
    offset.x <= this.offset.x + size.width &&
    offset.y >= this.offset.y &&
    offset.y <= this.offset.y + size.height

private fun Offset.shortString(): String = "(%.1f,%.1f)".format(x, y)

private fun List<SceneListItem>.joinToStringIds(): String = joinToString(prefix = "[", postfix = "]") { it.id }

private fun List<LazyGridItemInfo>.debugVisibleItems(): String = joinToString(prefix = "[", postfix = "]") { item ->
    "${item.key}@${item.index}:${item.offset}"
}

private const val SCENE_LIST_DRAG_TAG = "SceneListDrag"
private const val DRAG_LOG_SAMPLE_RATE = 8
private const val MAX_AUTO_SCROLL_STEP_PX = 36f
private const val MIN_DRAG_THRESHOLD_PX = 4f
