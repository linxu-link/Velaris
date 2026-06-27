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
package com.wujia.foundation.ads.rewarded

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsError
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.ads.RewardedAdManager
import com.wujia.foundation.ads.RewardedAdPlacement
import com.wujia.foundation.ads.RewardedAdResult
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google 激励广告管理器实现
 *
 * 封装激励广告的加载和展示流程。在展示广告前会先确保 SDK 已初始化，
 * 然后通过 [GoogleRewardedAdLoader] 加载广告并展示给用户。
 * 使用协程桥接 Google 广告 SDK 的回调式 API。
 */
@Singleton
internal class GoogleRewardedAdManager @Inject constructor(
    private val adsInitializer: AdsInitializer,
    private val configProvider: AdsConfigProvider,
    private val rewardedAdLoader: GoogleRewardedAdLoader,
) : RewardedAdManager {
    override suspend fun show(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult {
        val config = configProvider.getConfig()
        if (!config.enabled) return RewardedAdResult.SkippedAdsUnavailable

        val adUnitId = config.adUnitIdFor(placement)
        if (adUnitId.isBlank()) return RewardedAdResult.SkippedAdsUnavailable

        return when (val initializationResult = adsInitializer.initialize(activity)) {
            AdsInitializationResult.Success -> loadAndShow(activity, adUnitId)
            AdsInitializationResult.Disabled,
            is AdsInitializationResult.ConsentRequiredOrUnavailable,
            -> RewardedAdResult.SkippedAdsUnavailable
            is AdsInitializationResult.Failure -> RewardedAdResult.Failure(initializationResult.error)
        }
    }

    private suspend fun loadAndShow(activity: Activity, adUnitId: String): RewardedAdResult {
        val rewardedAd = when (val loadResult = rewardedAdLoader.load(activity, adUnitId)) {
            is GoogleRewardedAdLoadResult.Success -> loadResult.ad
            is GoogleRewardedAdLoadResult.Failure -> return RewardedAdResult.Failure(loadResult.error)
        }

        return suspendCancellableCoroutine { continuation ->
            var rewardEarned = false
            var completed = false

            fun complete(result: RewardedAdResult) {
                if (!completed && continuation.isActive) {
                    completed = true
                    continuation.resume(result)
                }
            }

            rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    complete(
                        if (rewardEarned) {
                            RewardedAdResult.RewardEarned
                        } else {
                            RewardedAdResult.ClosedWithoutReward
                        },
                    )
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    complete(RewardedAdResult.Failure(adError.toAdsError()))
                }
            }

            runCatching {
                rewardedAd.show(activity) {
                    rewardEarned = true
                }
            }.onFailure { throwable ->
                Timber.w(throwable, "Rewarded ad show failed")
                complete(
                    RewardedAdResult.Failure(
                        AdsError(message = throwable.message ?: "Rewarded ad show failed"),
                    ),
                )
            }

            continuation.invokeOnCancellation {
                rewardedAd.fullScreenContentCallback = null
            }
        }
    }

    private fun AdsConfig.adUnitIdFor(placement: RewardedAdPlacement): String = when (placement) {
        RewardedAdPlacement.NEW_SCENE_SAVE -> rewardedAdConfig.newSceneSaveAdUnitId
    }
}

/**
 * 激励广告加载器接口
 *
 * 封装从 Google AdMob 加载激励广告的操作，便于测试时替换。
 */
internal interface GoogleRewardedAdLoader {
    suspend fun load(activity: Activity, adUnitId: String): GoogleRewardedAdLoadResult
}

/** 激励广告加载结果 */
internal sealed interface GoogleRewardedAdLoadResult {
    data class Success(val ad: RewardedAd) : GoogleRewardedAdLoadResult
    data class Failure(val error: AdsError) : GoogleRewardedAdLoadResult
}

/**
 * [GoogleRewardedAdLoader] 的 Google AdMob SDK 实现
 *
 * 使用 [RewardedAd.load] 加载激励广告，将回调转换为挂起函数返回结果。
 */
internal class GoogleRewardedAdLoaderWrapper @Inject constructor() : GoogleRewardedAdLoader {
    override suspend fun load(activity: Activity, adUnitId: String): GoogleRewardedAdLoadResult =
        suspendCancellableCoroutine { continuation ->
            RewardedAd.load(
                activity,
                adUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        if (continuation.isActive) {
                            continuation.resume(GoogleRewardedAdLoadResult.Success(ad))
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if (continuation.isActive) {
                            continuation.resume(GoogleRewardedAdLoadResult.Failure(error.toAdsError()))
                        }
                    }
                },
            )
        }
}

private fun AdError.toAdsError(): AdsError = AdsError(
    code = code,
    message = message,
)
