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
package com.wujia.foundation.ads.appopen

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsError
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.ads.AppOpenAdManager
import com.wujia.foundation.ads.AppOpenAdResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google App Open Ad 管理器实现。
 *
 * 仅负责冷启动阶段的开屏广告加载和展示。
 */
@Singleton
internal class GoogleAppOpenAdManager @Inject constructor(
    private val adsInitializer: AdsInitializer,
    private val configProvider: AdsConfigProvider,
    private val appOpenAdLoader: GoogleAppOpenAdLoader,
) : AppOpenAdManager {
    private val adCacheMutex = Mutex()
    private var cachedAd: ManagedAppOpenAd? = null
    private var inFlightLoad: CompletableDeferred<GoogleAppOpenAdLoadResult>? = null

    override suspend fun preload(activity: Activity) {
        val config = configProvider.getConfig()
        if (!config.enabled) {
            Timber.d("App open preload skipped: ads disabled")
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            Timber.d("App open preload skipped: activity unavailable")
            return
        }

        val adUnitId = config.appOpenAdConfig.adUnitId
        if (adUnitId.isBlank()) {
            Timber.d("App open preload skipped: ad unit id blank")
            return
        }

        Timber.d("App open preload start")

        when (adsInitializer.initialize(activity)) {
            AdsInitializationResult.Success -> {
                val loadedAd = withTimeoutOrNull(config.appOpenAdConfig.preloadTimeoutMillis) {
                    awaitLoadedAd(
                        activity = activity,
                        adUnitId = adUnitId,
                        allowImmediateLoad = true,
                    )
                } ?: run {
                    Timber.d(
                        "App open preload timeout: waited %d ms",
                        config.appOpenAdConfig.preloadTimeoutMillis,
                    )
                    return
                }
                if (loadedAd is GoogleAppOpenAdLoadResult.Failure) {
                    Timber.d("App open ad preload failed: %s", loadedAd.error.message)
                } else {
                    Timber.d("App open preload finished: ad ready")
                }
            }
            AdsInitializationResult.Disabled,
            is AdsInitializationResult.ConsentRequiredOrUnavailable,
            -> Timber.d("App open preload skipped: initializer unavailable")
            is AdsInitializationResult.Failure ->
                Timber.w("App open ad preload init failed")
        }
    }

    override suspend fun showIfAvailable(activity: Activity, onShown: () -> Unit): AppOpenAdResult {
        val config = configProvider.getConfig()
        if (!config.enabled) {
            Timber.d("App open show skipped: ads disabled")
            return AppOpenAdResult.SkippedUnavailable
        }
        if (activity.isFinishing || activity.isDestroyed) {
            Timber.d("App open show skipped: activity unavailable")
            return AppOpenAdResult.SkippedUnavailable
        }

        val adUnitId = config.appOpenAdConfig.adUnitId
        if (adUnitId.isBlank()) {
            Timber.d("App open show skipped: ad unit id blank")
            return AppOpenAdResult.SkippedUnavailable
        }

        Timber.d("App open show start")

        return when (val initializationResult = adsInitializer.initialize(activity)) {
            AdsInitializationResult.Success -> loadAndShow(activity, config, adUnitId, onShown)
            AdsInitializationResult.Disabled,
            is AdsInitializationResult.ConsentRequiredOrUnavailable,
            -> {
                Timber.d("App open show skipped: initializer unavailable")
                AppOpenAdResult.SkippedUnavailable
            }
            is AdsInitializationResult.Failure -> {
                Timber.w("App open show init failed: %s", initializationResult.error.message)
                AppOpenAdResult.Failure(initializationResult.error)
            }
        }
    }

    private suspend fun loadAndShow(
        activity: Activity,
        config: AdsConfig,
        adUnitId: String,
        onShown: () -> Unit,
    ): AppOpenAdResult {
        val loadedAd = withTimeoutOrNull(config.appOpenAdConfig.showTimeoutMillis) {
            awaitLoadedAd(
                activity = activity,
                adUnitId = adUnitId,
                allowImmediateLoad = true,
            )
        } ?: run {
            Timber.d(
                "App open show timeout: waited %d ms",
                config.appOpenAdConfig.showTimeoutMillis,
            )
            return AppOpenAdResult.TimedOut
        }

        val appOpenAd = when (loadedAd) {
            is GoogleAppOpenAdLoadResult.Success -> {
                adCacheMutex.withLock {
                    cachedAd = null
                }
                Timber.d("App open show proceed: ad obtained")
                loadedAd.ad
            }
            is GoogleAppOpenAdLoadResult.Failure -> {
                Timber.w("App open show load failed: %s", loadedAd.error.message)
                return AppOpenAdResult.Failure(loadedAd.error)
            }
        }

        return suspendCancellableCoroutine { continuation ->
            var didShow = false
            var completed = false

            fun complete(result: AppOpenAdResult) {
                if (!completed && continuation.isActive) {
                    completed = true
                    continuation.resume(result)
                }
            }

            appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    didShow = true
                    Timber.i("App open ad showed")
                    onShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    Timber.i("App open ad dismissed")
                    complete(if (didShow) AppOpenAdResult.Shown else AppOpenAdResult.SkippedUnavailable)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Timber.w("App open ad failed to show: %s", adError.message)
                    complete(AppOpenAdResult.Failure(adError.toAdsError()))
                }
            }

            runCatching {
                Timber.d("App open ad show invoke")
                appOpenAd.show(activity)
            }.onFailure { throwable ->
                Timber.w(throwable, "App open ad show failed")
                complete(
                    AppOpenAdResult.Failure(
                        AdsError(message = throwable.message ?: "App open ad show failed"),
                    ),
                )
            }

            continuation.invokeOnCancellation {
                appOpenAd.fullScreenContentCallback = null
            }
        }
    }

    private suspend fun awaitLoadedAd(
        activity: Activity,
        adUnitId: String,
        allowImmediateLoad: Boolean,
    ): GoogleAppOpenAdLoadResult? {
        var shouldLoad = false
        val deferred = adCacheMutex.withLock {
            cachedAd?.let {
                Timber.d("App open cache hit: prioritize cached ad for show")
                return GoogleAppOpenAdLoadResult.Success(it)
            }
            inFlightLoad?.let {
                Timber.d("App open reuse in-flight load")
                return@withLock it
            }
            if (!allowImmediateLoad) return null

            shouldLoad = true
            Timber.d("App open start new load")
            CompletableDeferred<GoogleAppOpenAdLoadResult>().also { inFlightLoad = it }
        }

        if (shouldLoad) {
            val result = runCatching {
                appOpenAdLoader.load(activity, adUnitId)
            }.getOrElse { throwable ->
                Timber.w(throwable, "App open ad load failed")
                GoogleAppOpenAdLoadResult.Failure(
                    AdsError(message = throwable.message ?: "App open ad load failed"),
                )
            }
            adCacheMutex.withLock {
                if (result is GoogleAppOpenAdLoadResult.Success) {
                    cachedAd = result.ad
                    Timber.d("App open load success: ad cached")
                }
                if (inFlightLoad === deferred) {
                    inFlightLoad = null
                }
            }
            if (!deferred.isCompleted) {
                deferred.complete(result)
            }
        }

        return deferred.await()
    }
}

internal interface GoogleAppOpenAdLoader {
    suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult
}

internal sealed interface GoogleAppOpenAdLoadResult {
    data class Success(val ad: ManagedAppOpenAd) : GoogleAppOpenAdLoadResult
    data class Failure(val error: AdsError) : GoogleAppOpenAdLoadResult
}

internal interface ManagedAppOpenAd {
    var fullScreenContentCallback: FullScreenContentCallback?

    fun show(activity: Activity)
}

internal class GoogleAppOpenAdLoaderWrapper @Inject constructor() : GoogleAppOpenAdLoader {
    override suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult =
        suspendCancellableCoroutine { continuation ->
            AppOpenAd.load(
                activity,
                adUnitId,
                AdRequest.Builder().build(),
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        if (continuation.isActive) {
                            continuation.resume(
                                GoogleAppOpenAdLoadResult.Success(
                                    RealManagedAppOpenAd(ad),
                                ),
                            )
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if (continuation.isActive) {
                            continuation.resume(GoogleAppOpenAdLoadResult.Failure(error.toAdsError()))
                        }
                    }
                },
            )
        }
}

private class RealManagedAppOpenAd(private val delegate: AppOpenAd) : ManagedAppOpenAd {
    override var fullScreenContentCallback: FullScreenContentCallback?
        get() = delegate.fullScreenContentCallback
        set(value) {
            delegate.fullScreenContentCallback = value
        }

    override fun show(activity: Activity) {
        delegate.show(activity)
    }
}

private fun AdError.toAdsError(): AdsError = AdsError(
    code = code,
    message = message,
)
