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
package com.wujia.foundation.ads

import android.app.Activity

/**
 * 冷启动开屏广告管理器接口
 *
 * 负责在冷启动阶段尝试加载并展示 App Open Ad。
 * 当广告真正开始展示时会回调 [onShown]，用于让宿主页面释放系统启动屏。
 */
interface AppOpenAdManager {
    suspend fun preload(activity: Activity)

    suspend fun showIfAvailable(activity: Activity, onShown: () -> Unit = {}): AppOpenAdResult
}

sealed interface AppOpenAdResult {
    /** 广告已成功展示并关闭 */
    data object Shown : AppOpenAdResult

    /** 当前无法展示广告，例如广告未启用、未同意、广告位为空等 */
    data object SkippedUnavailable : AppOpenAdResult

    /** 广告加载超时，启动流程应继续放行 */
    data object TimedOut : AppOpenAdResult

    /** 广告流程发生技术错误 */
    data class Failure(val error: AdsError) : AppOpenAdResult
}
