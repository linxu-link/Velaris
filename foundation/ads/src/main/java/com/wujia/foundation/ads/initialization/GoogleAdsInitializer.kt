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

import android.app.Activity
import android.content.Context
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsConsentManager
import com.wujia.foundation.ads.AdsError
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.toolkit.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google 广告 SDK 初始化器实现
 *
 * 负责协调广告 SDK 的完整初始化流程：
 * 1. 检查广告配置是否启用
 * 2. 应用请求配置（测试设备 ID、儿童定向标签等）
 * 3. 通过 UMP SDK 收集用户隐私同意
 * 4. 在用户同意后初始化 Google Mobile Ads SDK
 *
 * 初始化是幂等的，多次调用会直接返回成功。
 * 使用 [Mutex] 保证并发安全。
 */
@Singleton
class GoogleAdsInitializer @Inject internal constructor(
    @param:ApplicationContext private val context: Context,
    private val configProvider: AdsConfigProvider,
    private val consentManager: AdsConsentManager,
    private val googleAdsSdk: GoogleAdsSdk,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AdsInitializer {
    private val initializeMutex = Mutex()
    private var initialized = false

    override suspend fun initialize(activity: Activity): AdsInitializationResult = initializeMutex.withLock {
        if (initialized) {
            return@withLock AdsInitializationResult.Success
        }

        val config = configProvider.getConfig()
        if (!config.enabled) {
            return@withLock AdsInitializationResult.Disabled
        }

        runCatching {
            googleAdsSdk.applyRequestConfiguration(config)
            val consentState = consentManager.gatherConsent(activity)

            if (!consentState.canRequestAds) {
                return@withLock AdsInitializationResult.ConsentRequiredOrUnavailable(
                    consentState = consentState,
                )
            }

            withContext(ioDispatcher) {
                googleAdsSdk.initialize(context)
            }
            initialized = true
            AdsInitializationResult.Success
        }.getOrElse { throwable ->
            Timber.w(throwable, "Google ads initialization failed")
            AdsInitializationResult.Failure(
                error = AdsError(message = throwable.message ?: "Google ads initialization failed"),
            )
        }
    }
}
