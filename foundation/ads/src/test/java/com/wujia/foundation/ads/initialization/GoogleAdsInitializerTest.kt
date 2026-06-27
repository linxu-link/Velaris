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
import androidx.test.core.app.ApplicationProvider
import com.wujia.foundation.ads.AdsConfig
import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsConsentManager
import com.wujia.foundation.ads.AdsConsentState
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.toolkit.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class GoogleAdsInitializerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val activity: Activity = Activity()

    @Test
    fun initialize_whenDisabled_returnsDisabledWithoutGatheringConsent() = runTest(testDispatcher) {
        val consentManager = FakeAdsConsentManager(canRequestAds = true)
        val googleAdsSdk = FakeGoogleAdsSdk()
        val initializer = testInitializer(
            config = AdsConfig(enabled = false),
            consentManager = consentManager,
            googleAdsSdk = googleAdsSdk,
        )

        val result = initializer.initialize(activity)

        assertIs<AdsInitializationResult.Disabled>(result)
        assertEquals(0, consentManager.gatherConsentCalls)
        assertEquals(0, googleAdsSdk.initializeCalls)
    }

    @Test
    fun initialize_whenConsentCannotRequestAds_doesNotInitializeSdk() = runTest(testDispatcher) {
        val consentManager = FakeAdsConsentManager(canRequestAds = false)
        val googleAdsSdk = FakeGoogleAdsSdk()
        val initializer = testInitializer(
            config = AdsConfig(enabled = true),
            consentManager = consentManager,
            googleAdsSdk = googleAdsSdk,
        )

        val result = initializer.initialize(activity)

        assertIs<AdsInitializationResult.ConsentRequiredOrUnavailable>(result)
        assertEquals(1, consentManager.gatherConsentCalls)
        assertEquals(1, googleAdsSdk.applyRequestConfigurationCalls)
        assertEquals(0, googleAdsSdk.initializeCalls)
    }

    @Test
    fun initialize_whenConsentAllowsAds_initializesSdkOnce() = runTest(testDispatcher) {
        val consentManager = FakeAdsConsentManager(canRequestAds = true)
        val googleAdsSdk = FakeGoogleAdsSdk()
        val initializer = testInitializer(
            config = AdsConfig(enabled = true),
            consentManager = consentManager,
            googleAdsSdk = googleAdsSdk,
        )

        val firstResult = initializer.initialize(activity)
        val secondResult = initializer.initialize(activity)

        assertIs<AdsInitializationResult.Success>(firstResult)
        assertIs<AdsInitializationResult.Success>(secondResult)
        assertEquals(1, consentManager.gatherConsentCalls)
        assertEquals(1, googleAdsSdk.applyRequestConfigurationCalls)
        assertEquals(1, googleAdsSdk.initializeCalls)
    }

    private fun testInitializer(
        config: AdsConfig,
        consentManager: AdsConsentManager,
        googleAdsSdk: GoogleAdsSdk,
        @IoDispatcher dispatcher: CoroutineDispatcher = testDispatcher,
    ): GoogleAdsInitializer = GoogleAdsInitializer(
        context = ApplicationProvider.getApplicationContext(),
        configProvider = FakeAdsConfigProvider(config),
        consentManager = consentManager,
        googleAdsSdk = googleAdsSdk,
        ioDispatcher = dispatcher,
    )
}

private class FakeAdsConfigProvider(private val config: AdsConfig) : AdsConfigProvider {
    override fun getConfig(): AdsConfig = config
}

private class FakeAdsConsentManager(private val canRequestAds: Boolean) : AdsConsentManager {
    var gatherConsentCalls = 0

    override suspend fun gatherConsent(activity: Activity): AdsConsentState {
        gatherConsentCalls += 1
        return AdsConsentState(
            canRequestAds = canRequestAds,
            privacyOptionsRequired = false,
        )
    }

    override fun canRequestAds(): Boolean = canRequestAds

    override fun isPrivacyOptionsRequired(): Boolean = false

    override suspend fun showPrivacyOptions(activity: Activity): AdsConsentState = AdsConsentState(
        canRequestAds = canRequestAds,
        privacyOptionsRequired = false,
    )

    override fun resetForDebugOnly() = Unit
}

private class FakeGoogleAdsSdk : GoogleAdsSdk {
    var initializeCalls = 0
    var applyRequestConfigurationCalls = 0

    override suspend fun initialize(context: android.content.Context) {
        initializeCalls += 1
    }

    override fun applyRequestConfiguration(config: AdsConfig) {
        applyRequestConfigurationCalls += 1
    }
}
