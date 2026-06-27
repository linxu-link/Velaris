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
package com.wujia.feature.scenelist.impl.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.feature.scenelist.impl.entity.SceneListItem
import com.wujia.feature.scenelist.impl.ui.component.move
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.domain.scene.DeleteSceneResourceUseCase
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.domain.scene.ReorderSceneResourcesUseCase
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
/**
 * 场景列表页的 UI 状态。
 * - items: 当前分类下的场景列表项（已应用乐观更新顺序）
 * - isLoading: 初始加载状态
 * - category: 当前选中的分类（FOCUS / SLEEP 等）
 *
 * isEmpty 属性用于 UI 层判断是否展示空状态。
 */
internal data class SceneListUiState(val items: List<SceneListItem> = emptyList(), val isLoading: Boolean = true, val category: SceneCategory? = null) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}

/**
 * 场景列表 ViewModel。
 * 职责：
 * - 观察场景数据（按分类过滤）
 * - 处理分类切换（重置乐观顺序）
 * - 处理拖拽重排序（乐观更新 + 提交到 UseCase）
 * - 处理删除（仅非预设场景）
 *
 * 使用 optimisticOrder 实现拖拽时的即时 UI 反馈，
 * 失败时回滚到上一次顺序。
 *
 * 遵循项目规范：暴露 StateFlow<UiState>，事件通过方法调用。
 */
@HiltViewModel
internal class SceneListViewModel @Inject constructor(
    private val observeSceneResources: ObserveSceneResourcesUseCase,
    private val reorderSceneResources: ReorderSceneResourcesUseCase,
    private val deleteSceneResource: DeleteSceneResourceUseCase,
) : ViewModel() {

    private val optimisticOrder = MutableStateFlow<List<String>?>(null)
    private val selectedCategory = MutableStateFlow(SceneCategory.FOCUS)

    /**
     * 对外暴露的 UI 状态 Flow。
     * 结合 observeSceneResources + 乐观排序 + 当前分类，
     * 自动过滤并映射为 SceneListItem。
     */
    val uiState: StateFlow<SceneListUiState> =
        combine(
            observeSceneResources(),
            optimisticOrder,
            selectedCategory,
        ) { scenes, orderedIds, category ->
            val mapped = scenes
                .filter { it.category == category }
                .map { sceneResourceToSceneListItem(it) }
            SceneListUiState(
                items = mapped.applyOptimisticOrder(orderedIds),
                isLoading = false,
                category = category,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SceneListUiState(),
        )

    /**
     * 设置当前分类（通常由 LaunchedEffect 或 CategoryTabs 触发）。
     * 会清空之前的乐观排序状态。
     */
    fun setCategory(category: SceneCategory?) {
        val resolved = category ?: SceneCategory.FOCUS
        if (selectedCategory.value == resolved) return
        selectedCategory.value = resolved
        optimisticOrder.value = null
    }

    /**
     * 分类 Tab 切换时调用。
     * 与 setCategory 类似，但明确传入非 null 值。
     */
    fun onCategoryChange(category: SceneCategory) {
        if (selectedCategory.value == category) return
        selectedCategory.value = category
        optimisticOrder.value = null
    }

    /**
     * 拖拽重排序回调（来自 UI 层的 onDragEnd）。
     * 1. 本地立即更新 optimisticOrder 实现即时视觉反馈
     * 2. 异步调用 UseCase 持久化
     * 3. 成功则清空乐观状态，失败则回滚
     *
     * 注意：支持分类内排序（传当前 category）或全局排序（传 null）。
     */
    fun onSceneReorder(fromIndex: Int, toIndex: Int) {
        val current = uiState.value.items.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        current.move(fromIndex, toIndex)
        val orderedIds = current.map { it.id }
        val previousOrder = optimisticOrder.value
        optimisticOrder.value = orderedIds
        val category = uiState.value.category

        viewModelScope.launch {
            runCatching {
                reorderSceneResources(
                    category = category,
                    orderedIds = orderedIds,
                )
            }.onSuccess {
                optimisticOrder.value = null
            }.onFailure {
                optimisticOrder.value = previousOrder
            }
        }
    }

    /**
     * 删除场景（仅非预设场景允许）。
     * 删除后清空乐观顺序，让上游数据流重新驱动列表。
     */
    fun onSceneDelete(id: String) {
        val item = uiState.value.items.firstOrNull { it.id == id } ?: return
        if (item.isPreset) return
        optimisticOrder.value = null
        viewModelScope.launch {
            runCatching { deleteSceneResource(id) }
        }
    }

    private fun sceneResourceToSceneListItem(resource: SceneResource): SceneListItem {
        val (icon, accent) = ITEM_PALETTES[resource.id.hashCode().and(0x7FFFFFFF) % ITEM_PALETTES.size]
        return SceneListItem(
            id = resource.id,
            title = resource.title,
            description = resource.subtitle,
            icon = icon,
            accent = accent,
            isPreset = resource.isPreset,
            coverResId = resource.coverResId,
            coverUri = resource.coverUri,
            sceneCategory = resource.category,
        )
    }
}

/**
 * 根据乐观排序的 ID 列表重新排列当前 items。
 * 用于拖拽过程中让 UI 立即反映新顺序，而不等待 Repository 确认。
 */
private fun List<SceneListItem>.applyOptimisticOrder(
    orderedIds: List<String>?,
): List<SceneListItem> {
    if (orderedIds == null || orderedIds.size != size) return this
    val itemsById = associateBy { it.id }
    if (orderedIds.any { it !in itemsById }) return this
    return orderedIds.mapNotNull(itemsById::get)
}

/**
 * 场景卡片使用的图标 + 强调色调色板。
 * 通过 id.hashCode() 取模实现“伪随机”但稳定的配色。
 */
private val ITEM_PALETTES = listOf(
    Icons.Outlined.AcUnit to VelarisColor.PaletteIce,
    Icons.Outlined.Forest to VelarisColor.PaletteForest,
    Icons.Outlined.Coffee to VelarisColor.PaletteAmber,
    Icons.Outlined.DarkMode to VelarisColor.PaletteTwilight,
    Icons.Outlined.Spa to VelarisColor.PaletteSpa,
)
