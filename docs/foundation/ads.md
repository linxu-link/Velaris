# `foundation:ads` 模块文档

## 模块概述

`foundation:ads` 是 Velaris 应用的广告基础模块。本项目当前只使用 Google 开屏广告，不包含激励广告、插屏广告或横幅广告能力。

模块封装以下能力：

- **UMP 用户隐私同意管理**：在需要用户同意的地区，首次请求广告前处理同意状态和隐私选项
- **广告 SDK 初始化**：协调同意收集与 Google Mobile Ads SDK 初始化，保证幂等性和并发安全
- **开屏广告 (App Open Ads)**：提供冷启动阶段的开屏广告加载和展示，支持预加载和超时机制

**包名**：`com.wujia.foundation.ads`

## 构建配置

`foundation:ads` 的 `BuildConfig` 由 `foundation/ads/build.gradle.kts` 生成。当前可配置项如下：

| 配置项 | 来源 | 默认值 | 说明 |
|---|---|---|---|
| `ADS_ENABLED` | `build.gradle.kts` 固定写入 | `true` | 全局广告总开关 |
| `ADS_DEBUG` | `velaris.adsDebug`（仅 debug 构建生效） | debug: `true`，release: `false` | 是否启用广告调试模式 |
| `ADS_SKIP_CONSENT_FLOW_IN_DEBUG` | `local.properties` 的 `velaris.adsSkipConsentFlowInDebug`（仅 debug 构建） | debug: `true`，release: `false` | debug 下是否跳过 UMP 同意流程 |
| `ADS_TEST_DEVICE_HASHED_IDS` | `local.properties` 的 `velaris.adsTestDeviceHashedIds`（仅 debug 构建） | debug: 读取本地配置，release: 空字符串 | UMP / Google Ads 测试设备 hashed id，多个值时用逗号分隔 |
| `ADS_DEBUG_GEOGRAPHY` | `local.properties` 的 `velaris.adsDebugGeography`（仅 debug 构建） | debug: 读取本地配置，release: `DISABLED` | UMP 调试地理位置，支持 `EEA` / `NOT_EEA` / `REGULATED_US_STATE` / `OTHER` |
| `ADS_APP_OPEN_AD_UNIT_ID` | Manifest metadata `com.wujia.velaris.ads.APP_OPEN_AD_UNIT_ID` | Google 开屏测试广告单元 ID | 冷启动展示的开屏广告单元 ID |
| `ADS_APP_OPEN_SHOW_TIMEOUT_MILLIS` | `velaris.appOpenShowTimeoutMillis` | `3000` | 冷启动等待开屏广告展示的最大时长 |
| `ADS_APP_OPEN_PRELOAD_TIMEOUT_MILLIS` | `velaris.appOpenPreloadTimeoutMillis` | `5000` | 预加载开屏广告的最大等待时长 |

建议的 `local.properties` 写法：

```properties
velaris.adsSkipConsentFlowInDebug=true
velaris.adsTestDeviceHashedIds=2D949B91CC02302446C7802D5DD507D4
velaris.adsDebugGeography=EEA
```

正式开屏广告位由 `app` 模块通过 Manifest placeholder 写入 metadata：

```xml
<meta-data
    android:name="com.wujia.velaris.ads.APP_OPEN_AD_UNIT_ID"
    android:value="${appOpenAdUnitId}" />
```

## 架构设计

### 分层

```
Public API
  AdsConfigProvider
  AdsConsentManager
  AdsInitializer
  AppOpenAdManager
  AdsConfig / AppOpenAdConfig

Internal implementation
  config/         BuildConfigAdsConfigProvider
  consent/        GoogleAdsConsentManager
                  GoogleUserMessagingPlatformWrapper
  initialization/ GoogleAdsInitializer
                  GoogleAdsSdkWrapper
  appopen/        GoogleAppOpenAdManager
                  GoogleAppOpenAdLoaderWrapper
  di/             AdsModule
```

### 初始化流程

```
AdsInitializer.initialize(activity)
        |
        v
AdsConfig.enabled?
        |
        +-- false -> AdsInitializationResult.Disabled
        |
        v
GoogleAdsSdk.applyRequestConfiguration(config)
        |
        v
AdsConsentManager.gatherConsent(activity)
        |
        v
canRequestAds?
        |
        +-- false -> AdsInitializationResult.ConsentRequiredOrUnavailable
        |
        v
GoogleAdsSdk.initialize(context)
        |
        v
AdsInitializationResult.Success
```

`GoogleAdsInitializer` 使用 `Mutex` 保护初始化流程，保证并发调用时只初始化一次。

### 开屏广告流程

```
AppOpenAdManager.showIfAvailable(activity)
        |
        v
AdsInitializer.initialize(activity)
        |
        v
load cached ad or AppOpenAd.load(...)
        |
        v
show / timed out / failed / skipped
```

`GoogleAppOpenAdManager` 会在展示前完成广告初始化，并使用超时控制避免启动流程被广告加载长期阻塞。

## DI 绑定

`AdsModule` 注册以下绑定：

| 接口 | 实现 |
|---|---|
| `AdsConfigProvider` | `BuildConfigAdsConfigProvider` |
| `AdsConsentManager` | `GoogleAdsConsentManager` |
| `AdsInitializer` | `GoogleAdsInitializer` |
| `GoogleUserMessagingPlatform` | `GoogleUserMessagingPlatformWrapper` |
| `GoogleAdsSdk` | `GoogleAdsSdkWrapper` |
| `AppOpenAdManager` | `GoogleAppOpenAdManager` |
| `GoogleAppOpenAdLoader` | `GoogleAppOpenAdLoaderWrapper` |

## 测试

当前测试覆盖：

| 测试文件 | 覆盖内容 |
|---|---|
| `GoogleAdsInitializerTest` | 广告禁用、同意不可请求、初始化幂等 |
| `GoogleAppOpenAdManagerTest` | 广告禁用、初始化失败、加载超时、预加载缓存复用 |

## 边界说明

- 本模块不提供激励广告能力。
- 本模块不保存广告奖励、冷却期或每日次数等业务状态。
- feature 模块不应直接依赖 Google Mobile Ads SDK；广告展示入口应通过 `AppOpenAdManager` 或应用壳层编排。
