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
 * 广告用户同意管理器接口
 *
 * 管理 Google User Messaging Platform (UMP) 的用户隐私同意流程。
 * 在 EEA 等需要用户同意的地区，首次使用广告前需要通过此接口收集用户同意。
 */
interface AdsConsentManager {
    /**
     * 收集用户隐私同意
     *
     * 如果需要同意，会向用户展示同意表单。
     * 返回当前的同意状态，包含是否可以请求广告等信息。
     */
    suspend fun gatherConsent(activity: Activity): AdsConsentState

    /** 当前是否可以请求广告 */
    fun canRequestAds(): Boolean

    /** 是否需要展示隐私选项入口 */
    fun isPrivacyOptionsRequired(): Boolean

    /** 展示隐私选项表单，返回更新后的同意状态 */
    suspend fun showPrivacyOptions(activity: Activity): AdsConsentState

    /** 仅在调试模式下重置同意状态 */
    fun resetForDebugOnly()
}

/**
 * 广告用户同意状态
 *
 * @property canRequestAds 是否允许请求和展示广告
 * @property privacyOptionsRequired 是否需要在应用中提供隐私选项入口
 * @property error 同意流程中的错误信息，无错误时为 null
 */
data class AdsConsentState(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean,
    val error: AdsError? = null,
)

/**
 * 广告错误信息
 *
 * @property code 错误码，来自底层 SDK 时为对应错误码
 * @property message 错误描述信息
 */
data class AdsError(val code: Int? = null, val message: String)
