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
package com.wujia.foundation.ads.di

import com.wujia.foundation.ads.AdsConfigProvider
import com.wujia.foundation.ads.AdsConsentManager
import com.wujia.foundation.ads.AdsInitializer
import com.wujia.foundation.ads.AppOpenAdManager
import com.wujia.foundation.ads.appopen.GoogleAppOpenAdLoader
import com.wujia.foundation.ads.appopen.GoogleAppOpenAdLoaderWrapper
import com.wujia.foundation.ads.appopen.GoogleAppOpenAdManager
import com.wujia.foundation.ads.config.BuildConfigAdsConfigProvider
import com.wujia.foundation.ads.consent.GoogleAdsConsentManager
import com.wujia.foundation.ads.consent.GoogleUserMessagingPlatform
import com.wujia.foundation.ads.consent.GoogleUserMessagingPlatformWrapper
import com.wujia.foundation.ads.initialization.GoogleAdsInitializer
import com.wujia.foundation.ads.initialization.GoogleAdsSdk
import com.wujia.foundation.ads.initialization.GoogleAdsSdkWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 广告模块 Hilt DI 绑定模块
 *
 * 将广告模块中的所有接口与其实现类绑定到 [SingletonComponent]。
 * 子包职责：
 * - [com.wujia.foundation.ads.config]：广告配置读取
 * - [com.wujia.foundation.ads.consent]：用户隐私同意管理
 * - [com.wujia.foundation.ads.initialization]：广告 SDK 初始化
 * - [com.wujia.foundation.ads.appopen]：开屏广告加载和展示
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface AdsModule {
    @Binds
    @Singleton
    fun bindsAdsConfigProvider(provider: BuildConfigAdsConfigProvider): AdsConfigProvider

    @Binds
    @Singleton
    fun bindsAdsConsentManager(manager: GoogleAdsConsentManager): AdsConsentManager

    @Binds
    @Singleton
    fun bindsAdsInitializer(initializer: GoogleAdsInitializer): AdsInitializer

    @Binds
    @Singleton
    fun bindsGoogleUserMessagingPlatform(platform: GoogleUserMessagingPlatformWrapper): GoogleUserMessagingPlatform

    @Binds
    @Singleton
    fun bindsGoogleAdsSdk(sdk: GoogleAdsSdkWrapper): GoogleAdsSdk

    @Binds
    @Singleton
    fun bindsAppOpenAdManager(manager: GoogleAppOpenAdManager): AppOpenAdManager

    @Binds
    @Singleton
    fun bindsGoogleAppOpenAdLoader(loader: GoogleAppOpenAdLoaderWrapper): GoogleAppOpenAdLoader
}
