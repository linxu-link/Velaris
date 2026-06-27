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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.wujia.feature.scenelist.impl.entity.SceneListItem
import com.wujia.feature.scenelist.impl.ui.component.move

/**
 * 场景列表拖拽状态持有者。
 * 负责管理拖拽过程中的视觉状态（visualItems）、偏移、已提交顺序等。
 *
 * 设计要点：
 * - optimistic 更新：拖拽时立即修改 visualItems 供 UI 渲染
 * - submittedOrderIds 用于判断上游数据是否已追上本地变更
 * - 与 VM 的 optimisticOrder 配合实现“拖拽成功则清空，失败则回滚”
 */
internal class SceneListDragState {
    var draggingItemId by mutableStateOf<String?>(null)
        private set

    var draggedInitialIndex by mutableStateOf(-1)
        private set

    var visualItems by mutableStateOf<List<SceneListItem>>(emptyList())
        private set

    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    var draggedInitialItemOffset by mutableStateOf(IntOffset.Zero)
        private set

    var draggedItemSize by mutableStateOf(IntSize.Zero)
        private set

    private var submittedOrderIds by mutableStateOf<List<String>?>(null)

    val isDragging: Boolean get() = draggingItemId != null

    val shouldUseVisualItems: Boolean get() = isDragging || submittedOrderIds != null

    /**
     * 与上游 items 同步。
     * 拖拽中时忽略，避免打断视觉状态。
     * 通过比较 submittedOrderIds 判断是否需要清空乐观状态。
     */
    fun syncItems(items: List<SceneListItem>) {
        if (isDragging) return

        val submittedIds = submittedOrderIds
        if (submittedIds == null) {
            visualItems = items
            return
        }

        val incomingIds = items.map { it.id }
        when {
            incomingIds == submittedIds -> {
                submittedOrderIds = null
                visualItems = items
            }

            incomingIds.toSet() == submittedIds.toSet() -> {
                // 本地提交的顺序与上游集合相同但顺序不同时，忽略陈旧发射（等待 reorder 确认）
            }

            else -> {
                submittedOrderIds = null
                visualItems = items
            }
        }
    }

    fun startDrag(
        itemId: String,
        items: List<SceneListItem>,
        itemOffset: IntOffset,
        itemSize: IntSize,
    ) {
        draggingItemId = itemId
        draggedInitialIndex = items.indexOfFirst { it.id == itemId }
        visualItems = items
        dragOffset = Offset.Zero
        draggedInitialItemOffset = itemOffset
        draggedItemSize = itemSize
    }

    fun dragBy(delta: Offset) {
        dragOffset += delta
    }

    fun movePreview(toIndex: Int): Pair<Int, Int>? {
        val itemId = draggingItemId ?: return null
        val fromIndex = visualItems.indexOfFirst { it.id == itemId }
        if (fromIndex !in visualItems.indices || toIndex !in visualItems.indices || fromIndex == toIndex) {
            return null
        }
        visualItems = visualItems.toMutableList().apply {
            move(fromIndex, toIndex)
        }
        return fromIndex to toIndex
    }

    fun endDrag(): Pair<Int, Int>? {
        val itemId = draggingItemId ?: return null
        val fromIndex = draggedInitialIndex
        val toIndex = visualItems.indexOfFirst { it.id == itemId }
        val result = if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
            submittedOrderIds = visualItems.map { it.id }
            fromIndex to toIndex
        } else {
            null
        }
        clear()
        return result
    }

    fun cancelDrag() {
        clear()
    }

    private fun clear() {
        draggingItemId = null
        draggedInitialIndex = -1
        dragOffset = Offset.Zero
        draggedInitialItemOffset = IntOffset.Zero
        draggedItemSize = IntSize.Zero
    }
}
