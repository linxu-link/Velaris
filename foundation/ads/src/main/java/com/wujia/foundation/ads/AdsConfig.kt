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

/**
 * 广告配置数据类
 *
 * 包含广告 SDK 的全局配置参数，如是否启用广告、调试模式、
 * 测试设备 ID、调试地理位置和激励广告子配置。
 */
data class AdsConfig(
    val enabled: Boolean = true,
    val debug: Boolean = false,
    val testDeviceHashedIds: List<String> = emptyList(),
    val debugGeography: AdsDebugGeography = AdsDebugGeography.DISABLED,
    val tagForChildDirectedTreatment: Boolean? = null,
    val tagForUnderAgeOfConsent: Boolean? = null,
    val appOpenAdConfig: AppOpenAdConfig = AppOpenAdConfig(),
    val rewardedAdConfig: RewardedAdConfig = RewardedAdConfig(),
)

/**
 * 广告调试地理位置枚举
 *
 * 用于在开发阶段模拟不同地区的用户同意行为，
 * 仅在 [AdsConfig.debug] 为 true 时生效。
 */
enum class AdsDebugGeography {
    DISABLED,
    EEA,
    NOT_EEA,
    REGULATED_US_STATE,
    OTHER,
}

/**
 * 开屏广告子配置
 *
 * @property adUnitId 冷启动展示的开屏广告单元 ID
 * @property showTimeoutMillis 冷启动展示时的最大等待时长，超时后直接放行启动流程
 * @property preloadTimeoutMillis 预加载广告时的最大等待时长
 */
data class AppOpenAdConfig(
    val adUnitId: String = GOOGLE_APP_OPEN_TEST_AD_UNIT_ID,
    val showTimeoutMillis: Long = 3_000L,
    val preloadTimeoutMillis: Long = 1_500L,
)

/**
 * 激励广告子配置
 *
 * @property newSceneSaveAdUnitId 保存新场景时展示的激励广告单元 ID
 * @property hourlyCooldownMillis 获得奖励后的冷却时间（毫秒），冷却期内不再展示广告
 * @property dailyRewardLimit 每日最大奖励次数，达到上限后当日不再展示广告
 */
data class RewardedAdConfig(
    val newSceneSaveAdUnitId: String = GOOGLE_REWARDED_TEST_AD_UNIT_ID,
    val hourlyCooldownMillis: Long = 60 * 60 * 1000L,
    val dailyRewardLimit: Int = 10,
)

internal const val GOOGLE_REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
internal const val GOOGLE_APP_OPEN_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
