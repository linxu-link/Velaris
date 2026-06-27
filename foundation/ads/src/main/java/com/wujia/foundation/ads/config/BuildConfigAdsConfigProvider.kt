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
package com.wujia.foundation.ads.config

import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsDebugGeography
import com.wujia.foundation.ads.AppOpenAdConfig
import com.wujia.foundation.ads.BuildConfig
import com.wujia.foundation.ads.GOOGLE_APP_OPEN_TEST_AD_UNIT_ID
import com.wujia.foundation.ads.GOOGLE_REWARDED_TEST_AD_UNIT_ID
import com.wujia.foundation.ads.RewardedAdConfig
import timber.log.Timber
import javax.inject.Inject

/**
 * 基于 BuildConfig 的广告配置提供者
 *
 * 从模块编译时生成的 [BuildConfig] 字段读取广告配置，
 * 包括是否启用广告、调试模式、测试设备 ID、调试地理位置和激励广告单元 ID。
 * 在非调试模式下使用测试广告单元 ID 时会打印警告日志。
 */
class BuildConfigAdsConfigProvider @Inject constructor() : AdsConfigProvider {
    override fun getConfig(): AdsConfig {
        if (!BuildConfig.ADS_DEBUG &&
            BuildConfig.ADS_APP_OPEN_AD_UNIT_ID == GOOGLE_APP_OPEN_TEST_AD_UNIT_ID
        ) {
            Timber.w("Production app open ad unit id is missing; using Google test app open ad unit id")
        }
        if (!BuildConfig.ADS_DEBUG &&
            BuildConfig.ADS_REWARDED_NEW_SCENE_SAVE_AD_UNIT_ID == GOOGLE_REWARDED_TEST_AD_UNIT_ID
        ) {
            Timber.w("Production rewarded ad unit id is missing; using Google test rewarded ad unit id")
        }
        return AdsConfig(
            enabled = BuildConfig.ADS_ENABLED,
            debug = BuildConfig.ADS_DEBUG,
            testDeviceHashedIds = BuildConfig.ADS_TEST_DEVICE_HASHED_IDS
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() },
            debugGeography = BuildConfig.ADS_DEBUG_GEOGRAPHY.toAdsDebugGeography(),
            appOpenAdConfig = AppOpenAdConfig(
                adUnitId = BuildConfig.ADS_APP_OPEN_AD_UNIT_ID,
                showTimeoutMillis = BuildConfig.ADS_APP_OPEN_SHOW_TIMEOUT_MILLIS,
                preloadTimeoutMillis = BuildConfig.ADS_APP_OPEN_PRELOAD_TIMEOUT_MILLIS,
            ),
            rewardedAdConfig = RewardedAdConfig(
                newSceneSaveAdUnitId = BuildConfig.ADS_REWARDED_NEW_SCENE_SAVE_AD_UNIT_ID,
            ),
        )
    }

    private fun String.toAdsDebugGeography(): AdsDebugGeography = runCatching { AdsDebugGeography.valueOf(uppercase()) }
        .getOrDefault(AdsDebugGeography.DISABLED)
}
