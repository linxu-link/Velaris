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
import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsError
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.ads.AppOpenAdConfig
import com.wujia.foundation.ads.AppOpenAdResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class GoogleAppOpenAdManagerTest {
    private val dispatcher = StandardTestDispatcher()
    private val activity: Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun showIfAvailable_whenDisabled_skipsImmediately() = runTest(dispatcher) {
        val loader = FakeGoogleAppOpenAdLoader()
        val manager = manager(
            config = AdsConfig(enabled = false),
            appOpenAdLoader = loader,
        )

        val result = manager.showIfAvailable(activity)

        assertEquals(AppOpenAdResult.SkippedUnavailable, result)
        assertEquals(0, loader.loadCalls)
    }

    @Test
    fun showIfAvailable_whenInitializerFails_returnsFailure() = runTest(dispatcher) {
        val manager = manager(
            initializer = FakeAdsInitializer(
                result = AdsInitializationResult.Failure(AdsError(message = "init failed")),
            ),
        )

        val result = manager.showIfAvailable(activity)

        assertIs<AppOpenAdResult.Failure>(result)
        assertEquals("init failed", result.error.message)
    }

    @Test
    fun showIfAvailable_whenLoadTimesOut_returnsTimedOut() = runTest(dispatcher) {
        val manager = manager(
            config = AdsConfig(
                appOpenAdConfig = AppOpenAdConfig(
                    adUnitId = "test",
                    showTimeoutMillis = 100,
                    preloadTimeoutMillis = 100,
                ),
            ),
            appOpenAdLoader = HangingGoogleAppOpenAdLoader(),
        )

        val result = manager.showIfAvailable(activity)

        assertEquals(AppOpenAdResult.TimedOut, result)
    }

    @Test
    fun preload_thenShow_usesCachedAdWithoutLoadingAgain() = runTest(dispatcher) {
        val ad = FakeManagedAppOpenAd()
        val loader = SuccessGoogleAppOpenAdLoader(ad)
        val manager = manager(appOpenAdLoader = loader)

        manager.preload(activity)
        val result = manager.showIfAvailable(activity)

        assertEquals(AppOpenAdResult.Shown, result)
        assertEquals(1, loader.loadCalls)
        assertEquals(1, ad.showCalls)
    }

    private fun manager(
        config: AdsConfig = AdsConfig(
            appOpenAdConfig = AppOpenAdConfig(
                adUnitId = "test",
                showTimeoutMillis = 100,
                preloadTimeoutMillis = 100,
            ),
        ),
        initializer: AdsInitializer = FakeAdsInitializer(),
        appOpenAdLoader: GoogleAppOpenAdLoader = FakeGoogleAppOpenAdLoader(),
    ): GoogleAppOpenAdManager = GoogleAppOpenAdManager(
        adsInitializer = initializer,
        configProvider = FakeAdsConfigProvider(config),
        appOpenAdLoader = appOpenAdLoader,
    )
}

private class FakeAdsConfigProvider(private val config: AdsConfig) : AdsConfigProvider {
    override fun getConfig(): AdsConfig = config
}

private class FakeAdsInitializer(private val result: AdsInitializationResult = AdsInitializationResult.Success) :
    AdsInitializer {
    override suspend fun initialize(activity: Activity): AdsInitializationResult = result
}

private class FakeGoogleAppOpenAdLoader : GoogleAppOpenAdLoader {
    var loadCalls = 0

    override suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult {
        loadCalls += 1
        return GoogleAppOpenAdLoadResult.Failure(AdsError(message = "unused"))
    }
}

private class HangingGoogleAppOpenAdLoader : GoogleAppOpenAdLoader {
    override suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult =
        suspendCancellableCoroutine { }
}

private class SuccessGoogleAppOpenAdLoader(private val ad: FakeManagedAppOpenAd) : GoogleAppOpenAdLoader {
    var loadCalls = 0

    override suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult {
        loadCalls += 1
        return GoogleAppOpenAdLoadResult.Success(ad)
    }
}

private class FakeManagedAppOpenAd : ManagedAppOpenAd {
    override var fullScreenContentCallback: com.google.android.gms.ads.FullScreenContentCallback? = null
    var showCalls = 0

    override fun show(activity: Activity) {
        showCalls += 1
        fullScreenContentCallback?.onAdShowedFullScreenContent()
        fullScreenContentCallback?.onAdDismissedFullScreenContent()
    }
}
