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
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsConsentManager
import com.wujia.foundation.ads.AdsConsentState
import com.wujia.foundation.ads.AdsError
import com.wujia.foundation.ads.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google UMP 用户同意管理器实现
 *
 * 通过 [GoogleUserMessagingPlatform] 封装 Google User Messaging Platform (UMP) SDK，
 * 负责向用户展示隐私同意表单、查询同意状态和管理隐私选项。
 * 在 EEA 等需要用户同意的地区，首次使用广告前会弹出同意表单。
 */
@Singleton
class GoogleAdsConsentManager @Inject internal constructor(
    @param:ApplicationContext private val context: Context,
    private val configProvider: AdsConfigProvider,
    private val userMessagingPlatform: GoogleUserMessagingPlatform,
) : AdsConsentManager {
    override suspend fun gatherConsent(activity: Activity): AdsConsentState {
        val config = configProvider.getConfig()
        if (!config.enabled) {
            return currentState(activity)
        }
        if (config.debug && BuildConfig.ADS_SKIP_CONSENT_FLOW_IN_DEBUG) {
            Timber.i("Google ads debug mode: skip UMP consent flow")
            return debugState()
        }

        val updateError = userMessagingPlatform.requestConsentInfoUpdate(activity, config)
        if (updateError != null) {
            Timber.w("Google ads consent info update failed: %s", updateError.message)
        }

        val formError = userMessagingPlatform.loadAndShowConsentFormIfRequired(activity)
        if (formError != null) {
            Timber.w("Google ads consent form failed: %s", formError.message)
        }

        return currentState(activity, error = formError ?: updateError)
    }

    override fun canRequestAds(): Boolean {
        if (configProvider.getConfig().debug && BuildConfig.ADS_SKIP_CONSENT_FLOW_IN_DEBUG) {
            return true
        }
        return userMessagingPlatform.canRequestAds(context)
    }

    override fun isPrivacyOptionsRequired(): Boolean {
        if (configProvider.getConfig().debug && BuildConfig.ADS_SKIP_CONSENT_FLOW_IN_DEBUG) {
            return false
        }
        return userMessagingPlatform.isPrivacyOptionsRequired(context)
    }

    override suspend fun showPrivacyOptions(activity: Activity): AdsConsentState {
        if (configProvider.getConfig().debug && BuildConfig.ADS_SKIP_CONSENT_FLOW_IN_DEBUG) {
            return debugState()
        }
        val error = userMessagingPlatform.showPrivacyOptionsForm(activity)
        return currentState(activity, error)
    }

    override fun resetForDebugOnly() {
        if (configProvider.getConfig().debug && BuildConfig.ADS_SKIP_CONSENT_FLOW_IN_DEBUG) {
            userMessagingPlatform.reset(context)
        }
    }

    private fun currentState(activity: Activity, error: AdsError? = null): AdsConsentState = AdsConsentState(
        canRequestAds = userMessagingPlatform.canRequestAds(activity),
        privacyOptionsRequired = userMessagingPlatform.isPrivacyOptionsRequired(activity),
        error = error,
    )

    private fun debugState(): AdsConsentState = AdsConsentState(
        canRequestAds = true,
        privacyOptionsRequired = false,
    )
}
