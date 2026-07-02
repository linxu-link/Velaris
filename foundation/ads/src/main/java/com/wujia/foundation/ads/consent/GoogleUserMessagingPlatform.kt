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
package com.wujia.foundation.ads.consent

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsDebugGeography
import com.wujia.foundation.ads.AdsError
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Google User Messaging Platform (UMP) SDK 封装接口
 *
 * 隔离对 UMP SDK 的直接依赖，方便测试时替换。
 * 提供同意信息更新、同意表单展示和隐私选项管理等能力。
 */
internal interface GoogleUserMessagingPlatform {
    fun canRequestAds(context: Context): Boolean

    fun isPrivacyOptionsRequired(context: Context): Boolean

    suspend fun requestConsentInfoUpdate(activity: Activity, config: AdsConfig): AdsError?

    suspend fun loadAndShowConsentFormIfRequired(activity: Activity): AdsError?

    suspend fun showPrivacyOptionsForm(activity: Activity): AdsError?

    fun reset(context: Context)
}

/**
 * [GoogleUserMessagingPlatform] 的 Google UMP SDK 实现
 *
 * 将 UMP SDK 的回调式 API 转换为 Kotlin 协程挂起函数，
 * 支持调试地理位置模拟和测试设备 ID 配置。
 */
internal class GoogleUserMessagingPlatformWrapper @Inject constructor() : GoogleUserMessagingPlatform {
    override fun canRequestAds(context: Context): Boolean = consentInformation(context).canRequestAds()

    override fun isPrivacyOptionsRequired(context: Context): Boolean = consentInformation(context).privacyOptionsRequirementStatus ==
        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override suspend fun requestConsentInfoUpdate(activity: Activity, config: AdsConfig): AdsError? = suspendCancellableCoroutine { continuation ->
        consentInformation(activity).requestConsentInfoUpdate(
            activity,
            config.toConsentRequestParameters(activity),
            {
                continuation.resumeIfActive(null)
            },
            { error ->
                continuation.resumeIfActive(error.toAdsError())
            },
        )
    }

    override suspend fun loadAndShowConsentFormIfRequired(activity: Activity): AdsError? = suspendCancellableCoroutine { continuation ->
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
            continuation.resumeIfActive(error?.toAdsError())
        }
    }

    override suspend fun showPrivacyOptionsForm(activity: Activity): AdsError? = suspendCancellableCoroutine { continuation ->
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            continuation.resumeIfActive(error?.toAdsError())
        }
    }

    override fun reset(context: Context) {
        consentInformation(context).reset()
    }

    private fun consentInformation(context: Context): ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    private fun AdsConfig.toConsentRequestParameters(context: Context): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent == true)

        if (debug) {
            builder.setConsentDebugSettings(toConsentDebugSettings(context))
        }

        return builder.build()
    }

    private fun AdsConfig.toConsentDebugSettings(context: Context): ConsentDebugSettings {
        val builder = ConsentDebugSettings.Builder(context)

        when (debugGeography) {
            AdsDebugGeography.DISABLED -> Unit
            AdsDebugGeography.EEA -> builder.setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA,
            )
            AdsDebugGeography.NOT_EEA -> builder.setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA,
            )
            AdsDebugGeography.REGULATED_US_STATE -> builder.setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE,
            )
            AdsDebugGeography.OTHER -> builder.setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER,
            )
        }

        configTestDeviceIds(builder)
        return builder.build()
    }

    private fun AdsConfig.configTestDeviceIds(builder: ConsentDebugSettings.Builder) {
        testDeviceHashedIds.forEach { id ->
            builder.addTestDeviceHashedId(id)
        }
    }

    private fun <T> kotlinx.coroutines.CancellableContinuation<T>.resumeIfActive(value: T) {
        if (isActive) {
            resume(value)
        }
    }

    private fun com.google.android.ump.FormError.toAdsError(): AdsError = AdsError(code = errorCode, message = message)
}
