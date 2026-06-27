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
package com.wujia.foundation.ads.initialization

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.wujia.foundation.ads.AdsConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Google Mobile Ads SDK 封装接口
 *
 * 隔离对 [MobileAds] 的直接依赖，方便测试时替换。
 */
internal interface GoogleAdsSdk {
    suspend fun initialize(context: Context)

    fun applyRequestConfiguration(config: AdsConfig)
}

/**
 * [GoogleAdsSdk] 的 Google Mobile Ads SDK 实现
 *
 * 将 [MobileAds.initialize] 的回调式 API 转换为挂起函数，
 * 并通过 [MobileAds.setRequestConfiguration] 应用请求配置。
 */
internal class GoogleAdsSdkWrapper @Inject constructor() : GoogleAdsSdk {
    override suspend fun initialize(context: Context) {
        suspendCancellableCoroutine { continuation ->
            MobileAds.initialize(context) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    override fun applyRequestConfiguration(config: AdsConfig) {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTestDeviceIds(config.testDeviceHashedIds)
            .setTagForChildDirectedTreatment(config.tagForChildDirectedTreatment.toChildTag())
            .setTagForUnderAgeOfConsent(config.tagForUnderAgeOfConsent.toUnderAgeTag())
            .build()

        MobileAds.setRequestConfiguration(requestConfiguration)
    }

    private fun Boolean?.toChildTag(): Int = when (this) {
        true -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
        false -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
        null -> RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
    }

    private fun Boolean?.toUnderAgeTag(): Int = when (this) {
        true -> RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
        false -> RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
        null -> RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED
    }
}
