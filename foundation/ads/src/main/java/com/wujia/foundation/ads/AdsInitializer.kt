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
 * 广告 SDK 初始化器接口
 *
 * 负责在 Activity 可用时完成广告 SDK 的初始化流程，
 * 包括隐私同意收集和 SDK 启动。返回 [AdsInitializationResult] 表示初始化结果。
 */
interface AdsInitializer {
    suspend fun initialize(activity: Activity): AdsInitializationResult
}

/**
 * 广告初始化结果密封接口
 *
 * 表示 [AdsInitializer.initialize] 的四种可能结果。
 */
sealed interface AdsInitializationResult {
    /** 初始化成功，可以正常请求和展示广告 */
    data object Success : AdsInitializationResult

    /** 广告功能已被配置禁用 */
    data object Disabled : AdsInitializationResult

    /**
     * 用户同意尚未完成或被拒绝
     *
     * @property consentState 当前同意状态的快照
     */
    data class ConsentRequiredOrUnavailable(val consentState: AdsConsentState) : AdsInitializationResult

    /**
     * 初始化过程中发生错误
     *
     * @property error 错误详情
     */
    data class Failure(val error: AdsError) : AdsInitializationResult
}
