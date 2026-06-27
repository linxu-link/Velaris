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
package com.wujia.foundation.navigation

import androidx.navigation3.runtime.NavKey

/**
 * 通过更新导航状态来处理导航事件（前进和后退）。
 *
 * @param state - 将响应导航事件而更新的导航状态。
 */
class Navigator(val state: NavigationState) {

    /**
     * 导航到指定的导航键
     *
     * @param key - 要导航到的导航键。
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * 用指定 key 替换当前一级导航下的子栈。
     */
    fun replaceCurrentSubStackWith(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> {
                goToTopLevel(key)
                clearSubStack()
            }
            else -> {
                state.currentSubStack.apply {
                    clear()
                    add(state.currentTopLevelKey)
                    add(key)
                }
            }
        }
    }

    /**
     * 返回上一个导航键。
     */
    fun goBack() {
        when (state.currentKey) {
            state.startKey -> error("不能从起始路由返回")
            state.currentTopLevelKey -> {
                // 当前处于子栈的底部，返回上一个一级导航栈。
                state.topLevelStack.removeLastOrNull()
            }
            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    /**
     * 导航到非一级导航键。
     */
    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // 如果已在栈中则移除，以便添加到末尾。
            remove(key)
            add(key)
        }
    }

    /**
     * 导航到一级导航栈。
     */
    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                // 这是起始键。清空栈使其作为唯一的键。
                clear()
            } else {
                // 如果已在栈中则移除，以便添加到末尾。
                remove(key)
            }
            add(key)
        }
    }

    /**
     * 清除当前子栈中除根键以外的所有键。
     */
    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
