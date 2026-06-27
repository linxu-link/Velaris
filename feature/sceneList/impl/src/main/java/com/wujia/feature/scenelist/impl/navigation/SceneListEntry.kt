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
package com.wujia.feature.scenelist.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.wujia.feature.sceneedit.api.SceneEditNavKey
import com.wujia.feature.scenelist.api.SceneListNavKey
import com.wujia.feature.scenelist.impl.ui.SceneListScreen
import com.wujia.foundation.navigation.Navigator

/**
 * 场景列表的 Navigation3 Entry 注册。
 * 通过 SceneListNavKey 传入可选的 category，实现按分类打开列表。
 * 提供 onAddScene 回调跳转到编辑（依赖 sceneEdit api）。
 */
fun EntryProviderScope<NavKey>.sceneListEntry(
    navigator: Navigator,
) {
    entry<SceneListNavKey> { key ->
        SceneListScreen(
            category = key.category,
            onBackClick = navigator::goBack,
            onAddScene = { category ->
                navigator.navigate(SceneEditNavKey(category = category))
            },
        )
    }
}
