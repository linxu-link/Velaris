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
package com.wujia.velaris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wujia.foundation.ads.AdsInitializationResult
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.ads.AppOpenAdManager
import com.wujia.foundation.ads.AppOpenAdResult
import com.wujia.foundation.designsystem.theme.ProvideVelarisTheme
import com.wujia.foundation.model.settings.ThemeSettingsRepository
import com.wujia.velaris.ui.VelarisApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsInitializer: AdsInitializer

    @Inject lateinit var appOpenAdManager: AppOpenAdManager

    @Inject lateinit var themeSettingsRepository: ThemeSettingsRepository

    private val viewModel: MainActivityViewModel by viewModels()
    private var shouldAttemptColdStartAppOpenAd = false
    private var didStartColdStartAppOpenAd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        shouldAttemptColdStartAppOpenAd = savedInstanceState == null
        Timber.d(
            "MainActivity onCreate: shouldAttemptColdStartAppOpenAd=%s, savedInstanceState=%s",
            shouldAttemptColdStartAppOpenAd,
            savedInstanceState != null,
        )
        enableEdgeToEdge()
        hideSystemUi()
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.shouldKeepSplashScreen()
        }
        setContent {
            val themePreset by themeSettingsRepository.observeThemePreset()
                .collectAsStateWithLifecycle(themeSettingsRepository.getThemePreset())
            ProvideVelarisTheme(themePreset = themePreset) {
                VelarisApp()
            }
        }
        if (shouldAttemptColdStartAppOpenAd) {
            Timber.d("MainActivity cold start: mark app open pending and preload")
            viewModel.onColdStartAppOpenPending()
            preloadColdStartAppOpenAd()
        } else {
            Timber.d("MainActivity warm start: skip cold start app open")
            viewModel.onAppOpenAdNotRequested()
        }
        initializeAds()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Timber.d(
            "MainActivity onPostResume: shouldAttemptColdStartAppOpenAd=%s, didStartColdStartAppOpenAd=%s",
            shouldAttemptColdStartAppOpenAd,
            didStartColdStartAppOpenAd,
        )
        if (shouldAttemptColdStartAppOpenAd && !didStartColdStartAppOpenAd) {
            didStartColdStartAppOpenAd = true
            Timber.d("MainActivity onPostResume: trigger cold start app open show")
            showColdStartAppOpenAd()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun initializeAds() {
        lifecycleScope.launch {
            when (val result = adsInitializer.initialize(this@MainActivity)) {
                AdsInitializationResult.Success -> Timber.d("Google ads initialized")
                AdsInitializationResult.Disabled -> Timber.d("Google ads disabled")
                is AdsInitializationResult.ConsentRequiredOrUnavailable -> {
                    Timber.d(
                        "Google ads consent unavailable: canRequestAds=%s",
                        result.consentState.canRequestAds,
                    )
                }
                is AdsInitializationResult.Failure -> {
                    Timber.w("Google ads initialization failed: %s", result.error.message)
                }
            }
        }
    }

    private fun showColdStartAppOpenAd() {
        lifecycleScope.launch {
            Timber.d("MainActivity showColdStartAppOpenAd start")
            when (
                val result = appOpenAdManager.showIfAvailable(this@MainActivity) {
                    viewModel.onAppOpenAdShown()
                }
            ) {
                AppOpenAdResult.Shown -> {
                    Timber.i("MainActivity app open result: shown")
                    viewModel.onAppOpenAdFinished()
                    preloadColdStartAppOpenAd()
                }
                AppOpenAdResult.SkippedUnavailable -> {
                    Timber.d("MainActivity app open result: unavailable")
                    viewModel.onAppOpenAdFinished()
                    preloadColdStartAppOpenAd()
                }
                AppOpenAdResult.TimedOut -> {
                    Timber.d("MainActivity app open result: timed out")
                    viewModel.onAppOpenAdFinished()
                    preloadColdStartAppOpenAd()
                }
                is AppOpenAdResult.Failure -> {
                    Timber.w("MainActivity app open result: failed: %s", result.error.message)
                    viewModel.onAppOpenAdFinished()
                    preloadColdStartAppOpenAd()
                }
            }
        }
    }

    private fun preloadColdStartAppOpenAd() {
        lifecycleScope.launch {
            Timber.d("MainActivity preloadColdStartAppOpenAd start")
            appOpenAdManager.preload(this@MainActivity)
        }
    }
}
