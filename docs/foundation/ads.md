# `foundation:ads` 模块文档

## 模块概述

`foundation:ads` 是 Velaris 应用的广告基础模块，负责集成 Google Mobile Ads SDK 和 Google User Messaging Platform (UMP) SDK。该模块封装了以下核心能力：

- **UMP 用户隐私同意管理**：在 EEA 等需要用户同意的地区，首次展示广告前弹出同意表单，管理同意状态和隐私选项
- **广告 SDK 初始化**：协调同意收集与 SDK 启动的完整初始化流程，保证幂等性和并发安全
- **开屏广告 (App Open Ads)**：提供冷启动阶段的开屏广告加载和展示，支持预加载和超时机制
- **激励广告 (Rewarded Ads)**：提供激励广告的加载、展示完整流程，支持多种展示场景 (Placement)
- **频率控制门控 (Gatekeeper)**：基于冷却期和每日上限的激励广告展示频率限制，防止过度打扰用户
- **使用记录持久化**：通过 SharedPreferences 存储每日奖励次数和最后奖励时间戳

**包名**：`com.wujia.foundation.ads`

## 构建配置

`foundation:ads` 的 `BuildConfig` 主要由 `foundation/ads/build.gradle.kts` 生成。当前可配置项如下：

| 配置项 | 来源 | 默认值 | 说明 |
|---|---|---|---|
| `ADS_ENABLED` | `build.gradle.kts` 固定写入 | `true` | 全局广告总开关 |
| `ADS_DEBUG` | `velaris.adsDebug` | `true` | 是否启用广告调试模式 |
| `ADS_SKIP_CONSENT_FLOW_IN_DEBUG` | `local.properties` 的 `velaris.adsSkipConsentFlowInDebug` | `true` | debug 下是否跳过 UMP 同意流程 |
| `ADS_TEST_DEVICE_HASHED_IDS` | `local.properties` 的 `velaris.adsTestDeviceHashedIds` | 空字符串 | UMP / Google Ads 测试设备 hashed id，多个值时用逗号分隔 |
| `ADS_DEBUG_GEOGRAPHY` | `local.properties` 的 `velaris.adsDebugGeography` | `DISABLED` | UMP 调试地理位置，支持 `EEA` / `NOT_EEA` / `REGULATED_US_STATE` / `OTHER` |
| `ADS_REWARDED_NEW_SCENE_SAVE_AD_UNIT_ID` | `velaris.rewardedNewSceneSaveAdUnitId` | Google 测试激励广告单元 ID | 新建场景保存时使用的激励广告单元 ID |

建议的 `local.properties` 写法：

```properties
velaris.adsSkipConsentFlowInDebug=true
velaris.adsTestDeviceHashedIds=2D949B91CC02302446C7802D5DD507D4
velaris.adsDebugGeography=EEA
```

---

## 架构设计

### 整体分层

```
┌──────────────────────────────────────────────────────────┐
│  对外暴露的公共接口层 (Public Interfaces)                   │
│  AdsConfigProvider / AdsConsentManager / AdsInitializer   │
│  AppOpenAdManager / RewardedAdManager / RewardedAdGatekeeper │
│  RewardedAdUsageStore / AdsConfig / RewardedAdModels      │
├──────────────────────────────────────────────────────────┤
│  内部实现层 (Internal Implementations)                     │
│  config/       → BuildConfigAdsConfigProvider              │
│  consent/      → GoogleAdsConsentManager                   │
│                → GoogleUserMessagingPlatformWrapper         │
│  initialization/ → GoogleAdsInitializer                    │
│                  → GoogleAdsSdkWrapper                     │
│  appopen/      → GoogleAppOpenAdManager                    │
│                → GoogleAppOpenAdLoaderWrapper              │
│  rewarded/     → GoogleRewardedAdManager                   │
│                → GoogleRewardedAdLoaderWrapper              │
│                → SharedPreferencesRewardedAdUsageStore      │
│                → SystemRewardedAdClock                      │
│  (root)        → DefaultRewardedAdGatekeeper               │
├──────────────────────────────────────────────────────────┤
│  DI 绑定层                                                │
│  di/AdsModule (Hilt @Module)                              │
│  12 个 @Binds 接口→实现绑定                                 │
└──────────────────────────────────────────────────────────┘
```

### UMP 同意流程

```
用户打开需要广告的页面
         │
         ▼
  AdsInitializer.initialize(activity)
         │
         ▼
  ┌── AdsConfig.enabled? ──┐
  │ No                      │ Yes
  ▼                         ▼
  Disabled            applyRequestConfiguration()
                            │
                            ▼
                    consentManager.gatherConsent(activity)
                            │
                  ┌─────────┴──────────┐
                  ▼                    ▼
          consentInfoUpdate      loadAndShowConsentForm
          (请求同意信息更新)       (加载并展示同意表单)
                  │                    │
                  └─────────┬──────────┘
                            ▼
                   canRequestAds?
                  ┌────────┴────────┐
                  No                Yes
                  ▼                  ▼
         ConsentRequiredOr     googleAdsSdk.initialize()
         Unavailable                │
                                    ▼
                               Success (initialized = true)
```

**关键设计**：
- `GoogleAdsConsentManager` 通过 `GoogleUserMessagingPlatform` 接口隔离 UMP SDK 的直接依赖，便于测试替换
- UMP SDK 的回调式 API 通过 `suspendCancellableCoroutine` 桥接为 Kotlin 协程挂起函数
- 同意信息更新失败时继续尝试展示表单（容错设计）

### 初始化状态机

初始化结果由 `AdsInitializationResult` 密封接口表示，包含四种状态：

| 状态 | 含义 | 后续行为 |
|------|------|---------|
| `Success` | 初始化成功 | 可正常请求和展示广告 |
| `Disabled` | 广告已被配置禁用 | 跳过所有广告逻辑 |
| `ConsentRequiredOrUnavailable` | 用户同意未完成或被拒绝 | 不初始化 SDK，不展示广告 |
| `Failure` | 初始化过程发生错误 | 传递错误信息给调用方 |

**并发安全**：`GoogleAdsInitializer` 使用 `Mutex` 保护初始化流程，保证多次并发调用时只执行一次初始化（幂等）。

### 激励广告门控器 (Gatekeeper) 流程

```
Gatekeeper.showIfRequired(activity, placement)
         │
         ▼
    evaluate(placement)
         │
         ├── enabled == false → SkippedAdsUnavailable
         │
         ├── (now - lastReward) < hourlyCooldownMillis → SkippedRecentReward
         │
         ├── dailyRewardCount >= dailyRewardLimit → SkippedDailyLimitReached
         │
         └── → Required
                   │
                   ▼
           rewardedAdManager.show(activity, placement)
                   │
                   ├── RewardEarned → usageStore.recordRewardEarned(placement, now)
                   └── 其他结果 → 直接返回，不记录
```

**频率限制策略**：
- **冷却期 (Cooldown)**：默认 3600000ms（1 小时），上次获得奖励后在冷却期内不再展示广告
- **每日上限 (Daily Limit)**：默认 10 次/天，达到上限后当日不再展示

### SharedPreferences 使用记录存储

`SharedPreferencesRewardedAdUsageStore` 将激励广告使用数据持久化到名为 `velaris_rewarded_ads` 的 SharedPreferences 文件中。

**存储键命名规则**：`rewarded_{placement}_{field}`

| 键格式 | 类型 | 说明 |
|--------|------|------|
| `rewarded_{placement}_date` | String | 最后记录日期 (yyyy-MM-dd) |
| `rewarded_{placement}_count` | Int | 当日已获奖励次数 |
| `rewarded_{placement}_last_reward` | Long | 上次获奖励时间戳 (ms) |

**日期重置逻辑**：读取使用记录时，若存储的日期与当前本地日期不同，则自动将 `dailyRewardCount` 重置为 0。时钟通过 `RewardedAdClock` 接口抽象，便于测试注入固定时间。

### DI 绑定模块

`AdsModule` 使用 Hilt `@Binds` 将 12 个接口绑定到对应实现，均注册为 `SingletonComponent` 作用域的单例：

| # | 接口 | 实现 |
|---|------|------|
| 1 | `AdsConfigProvider` | `BuildConfigAdsConfigProvider` |
| 2 | `AdsConsentManager` | `GoogleAdsConsentManager` |
| 3 | `AdsInitializer` | `GoogleAdsInitializer` |
| 4 | `GoogleUserMessagingPlatform` | `GoogleUserMessagingPlatformWrapper` |
| 5 | `GoogleAdsSdk` | `GoogleAdsSdkWrapper` |
| 6 | `AppOpenAdManager` | `GoogleAppOpenAdManager` |
| 7 | `GoogleAppOpenAdLoader` | `GoogleAppOpenAdLoaderWrapper` |
| 8 | `RewardedAdManager` | `GoogleRewardedAdManager` |
| 9 | `RewardedAdGatekeeper` | `DefaultRewardedAdGatekeeper` |
| 10 | `RewardedAdUsageStore` | `SharedPreferencesRewardedAdUsageStore` |
| 11 | `RewardedAdClock` | `SystemRewardedAdClock` |
| 12 | `GoogleRewardedAdLoader` | `GoogleRewardedAdLoaderWrapper` |

---

## 核心类/接口

### 配置相关

#### `AdsConfig` (data class)

广告配置数据类，包含所有广告 SDK 的全局配置参数。

```kotlin
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
```

#### `AdsDebugGeography` (enum)

调试地理位置枚举，用于在开发阶段模拟不同地区的用户同意行为：

```kotlin
enum class AdsDebugGeography {
    DISABLED, EEA, NOT_EEA, REGULATED_US_STATE, OTHER
}
```

#### `RewardedAdConfig` (data class)

激励广告子配置，包含广告单元 ID 和频率限制参数：

```kotlin
data class RewardedAdConfig(
    val newSceneSaveAdUnitId: String = "ca-app-pub-3940256099942544/5224354917",
    val hourlyCooldownMillis: Long = 60 * 60 * 1000L,  // 默认 1 小时
    val dailyRewardLimit: Int = 10,                       // 默认每日 10 次
)
```

#### `AppOpenAdConfig` (data class)

开屏广告子配置，包含广告单元 ID 和超时设置：

```kotlin
data class AppOpenAdConfig(
    val adUnitId: String = GOOGLE_APP_OPEN_TEST_AD_UNIT_ID,
    val showTimeoutMillis: Long = 3_000L,  // 冷启动展示时的最大等待时长
    val preloadTimeoutMillis: Long = 1_500L,  // 预加载广告时的最大等待时长
)
```

#### `AdsConfigProvider` (接口)

广告配置提供者接口：

```kotlin
interface AdsConfigProvider {
    fun getConfig(): AdsConfig
}
```

#### `BuildConfigAdsConfigProvider` (实现类, internal)

从编译时生成的 `BuildConfig` 字段读取配置。在非调试模式下使用测试广告单元 ID 时会打印警告日志。

---

### 同意/UMP 相关

#### `AdsConsentManager` (接口)

用户隐私同意管理器接口，管理 UMP 同意流程：

```kotlin
interface AdsConsentManager {
    suspend fun gatherConsent(activity: Activity): AdsConsentState
    fun canRequestAds(): Boolean
    fun isPrivacyOptionsRequired(): Boolean
    suspend fun showPrivacyOptions(activity: Activity): AdsConsentState
    fun resetForDebugOnly()
}
```

#### `AdsConsentState` (data class)

同意状态快照：

```kotlin
data class AdsConsentState(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean,
    val error: AdsError? = null,
)
```

#### `AdsError` (data class)

广告错误信息，统一封装底层 SDK 的错误：

```kotlin
data class AdsError(
    val code: Int? = null,
    val message: String,
)
```

#### `GoogleAdsConsentManager` (实现类, @Singleton)

通过 `GoogleUserMessagingPlatform` 封装 UMP SDK。流程：
1. 调用 `requestConsentInfoUpdate` 更新同意信息
2. 调用 `loadAndShowConsentFormIfRequired` 展示同意表单
3. 返回包含 `canRequestAds`、`privacyOptionsRequired` 和错误信息的状态

#### `GoogleUserMessagingPlatform` (internal 接口)

隔离 UMP SDK 直接依赖的封装接口：

```kotlin
internal interface GoogleUserMessagingPlatform {
    fun canRequestAds(context: Context): Boolean
    fun isPrivacyOptionsRequired(context: Context): Boolean
    suspend fun requestConsentInfoUpdate(activity: Activity, config: AdsConfig): AdsError?
    suspend fun loadAndShowConsentFormIfRequired(activity: Activity): AdsError?
    suspend fun showPrivacyOptionsForm(activity: Activity): AdsError?
    fun reset(context: Context)
}
```

#### `GoogleUserMessagingPlatformWrapper` (internal 实现类)

将 UMP SDK 回调式 API 转换为协程挂起函数。支持调试地理位置模拟（EEA、NOT_EEA、REGULATED_US_STATE、OTHER）和测试设备 ID 配置。使用 `suspendCancellableCoroutine` + `resumeIfActive` 扩展保证取消安全。

---

### 广告初始化

#### `AdsInitializer` (接口)

```kotlin
interface AdsInitializer {
    suspend fun initialize(activity: Activity): AdsInitializationResult
}
```

#### `AdsInitializationResult` (sealed interface)

```kotlin
sealed interface AdsInitializationResult {
    data object Success : AdsInitializationResult
    data object Disabled : AdsInitializationResult
    data class ConsentRequiredOrUnavailable(val consentState: AdsConsentState) : AdsInitializationResult
    data class Failure(val error: AdsError) : AdsInitializationResult
}
```

#### `GoogleAdsInitializer` (实现类, @Singleton)

初始化流程：
1. 使用 `Mutex` 保护，保证幂等和并发安全
2. 检查 `initialized` 标志，已初始化则直接返回 `Success`
3. 检查 `AdsConfig.enabled`，禁用时返回 `Disabled`
4. 调用 `applyRequestConfiguration` 应用测试设备 ID 等配置
5. 调用 `consentManager.gatherConsent` 收集同意
6. 若 `canRequestAds` 为 false，返回 `ConsentRequiredOrUnavailable`
7. 在 IO 调度器上调用 `googleAdsSdk.initialize` 初始化 SDK
8. 设置 `initialized = true`，返回 `Success`

#### `GoogleAdsSdk` (internal 接口)

```kotlin
internal interface GoogleAdsSdk {
    suspend fun initialize(context: Context)
    fun applyRequestConfiguration(config: AdsConfig)
}
```

#### `GoogleAdsSdkWrapper` (internal 实现类)

将 `MobileAds.initialize` 的回调 API 转换为挂起函数。`applyRequestConfiguration` 设置测试设备 ID、儿童定向标签和未成年同意标签。

---

### 开屏广告管理

#### `AppOpenAdManager` (接口)

```kotlin
interface AppOpenAdManager {
    suspend fun preload(activity: Activity)
    suspend fun showIfAvailable(activity: Activity, onShown: () -> Unit = {}): AppOpenAdResult
}
```

#### `AppOpenAdResult` (sealed interface)

开屏广告展示结果，包含 4 种可能状态：

```kotlin
sealed interface AppOpenAdResult {
    data object Shown : AppOpenAdResult            // 广告已成功展示并关闭
    data object SkippedUnavailable : AppOpenAdResult  // 当前无法展示广告
    data object TimedOut : AppOpenAdResult          // 广告加载超时
    data class Failure(val error: AdsError) : AppOpenAdResult // 技术错误
}
```

#### `GoogleAppOpenAdManager` (internal 实现类, @Singleton)

开屏广告管理器实现，仅负责冷启动阶段的开屏广告加载和展示：

1. 检查广告是否启用，未启用返回 `SkippedUnavailable`
2. 检查 Activity 状态，不可用时返回 `SkippedUnavailable`
3. 调用 `adsInitializer.initialize(activity)` 确保 SDK 已初始化
4. 初始化失败则根据结果返回对应状态
5. 通过 `GoogleAppOpenAdLoader.load` 加载广告
6. 使用 `FullScreenContentCallback` 监听广告展示事件
7. 通过 `appOpenAd.show(activity)` 展示广告
8. 关闭时根据 `didShow` 标志返回 `Shown` 或 `SkippedUnavailable`

**关键设计**：
- 使用 `Mutex` 保护广告缓存和加载状态
- 支持预加载（`preload`）和展示（`showIfAvailable`）两种模式
- 超时机制：预加载和展示都有独立的超时配置
- 广告缓存：加载成功的广告会缓存，避免重复加载

#### `GoogleAppOpenAdLoader` (internal 接口)

```kotlin
internal interface GoogleAppOpenAdLoader {
    suspend fun load(activity: Activity, adUnitId: String): GoogleAppOpenAdLoadResult
}
```

#### `GoogleAppOpenAdLoadResult` (internal sealed interface)

```kotlin
internal sealed interface GoogleAppOpenAdLoadResult {
    data class Success(val ad: ManagedAppOpenAd) : GoogleAppOpenAdLoadResult
    data class Failure(val error: AdsError) : GoogleAppOpenAdLoadResult
}
```

#### `ManagedAppOpenAd` (internal 接口)

```kotlin
internal interface ManagedAppOpenAd {
    var fullScreenContentCallback: FullScreenContentCallback?
    fun show(activity: Activity)
}
```

#### `GoogleAppOpenAdLoaderWrapper` (internal 实现类)

将 `AppOpenAd.load` 回调式 API 转换为挂起函数。

---

### 激励广告管理

#### `RewardedAdManager` (接口)

```kotlin
interface RewardedAdManager {
    suspend fun show(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult
}
```

#### `RewardedAdPlacement` (enum)

激励广告展示场景枚举：

```kotlin
enum class RewardedAdPlacement {
    NEW_SCENE_SAVE,  // 保存新创建的场景时
}
```

#### `RewardedAdResult` (sealed interface)

激励广告展示结果，包含 7 种可能状态：

```kotlin
sealed interface RewardedAdResult {
    data object RewardEarned : RewardedAdResult            // 用户获得奖励
    data object SkippedRecentReward : RewardedAdResult      // 冷却期内跳过
    data object SkippedDailyLimitReached : RewardedAdResult // 每日上限跳过
    data object SkippedAdsUnavailable : RewardedAdResult    // 广告不可用跳过
    data object CancelledByUser : RewardedAdResult          // 用户取消
    data object ClosedWithoutReward : RewardedAdResult      // 关闭但未获得奖励
    data class Failure(val error: AdsError) : RewardedAdResult // 加载/展示错误
}
```

#### `GoogleRewardedAdManager` (internal 实现类, @Singleton)

展示流程：
1. 检查广告是否启用，未启用返回 `SkippedAdsUnavailable`
2. 根据 `placement` 获取对应的广告单元 ID
3. 调用 `adsInitializer.initialize(activity)` 确保 SDK 已初始化
4. 初始化失败则根据结果返回对应状态
5. 通过 `GoogleRewardedAdLoader.load` 加载广告
6. 使用 `FullScreenContentCallback` 监听广告展示事件
7. 通过 `rewardedAd.show(activity) { rewardEarned = true }` 回调标记奖励获取
8. 关闭时根据 `rewardEarned` 标志返回 `RewardEarned` 或 `ClosedWithoutReward`

#### `GoogleRewardedAdLoader` (internal 接口)

```kotlin
internal interface GoogleRewardedAdLoader {
    suspend fun load(activity: Activity, adUnitId: String): GoogleRewardedAdLoadResult
}
```

#### `GoogleRewardedAdLoadResult` (internal sealed interface)

```kotlin
internal sealed interface GoogleRewardedAdLoadResult {
    data class Success(val ad: RewardedAd) : GoogleRewardedAdLoadResult
    data class Failure(val error: AdsError) : GoogleRewardedAdLoadResult
}
```

#### `GoogleRewardedAdLoaderWrapper` (internal 实现类)

将 `RewardedAd.load` 回调式 API 转换为挂起函数。

---

### 激励广告门控器

#### `RewardedAdGatekeeper` (接口)

```kotlin
interface RewardedAdGatekeeper {
    suspend fun evaluate(placement: RewardedAdPlacement): RewardedAdGateState
    suspend fun showIfRequired(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult
}
```

#### `RewardedAdGateState` (sealed interface)

门控评估结果：

```kotlin
sealed interface RewardedAdGateState {
    data object Required : RewardedAdGateState              // 需要展示广告
    data object SkippedRecentReward : RewardedAdGateState    // 冷却期跳过
    data object SkippedDailyLimitReached : RewardedAdGateState // 每日上限跳过
    data object SkippedAdsUnavailable : RewardedAdGateState  // 广告不可用跳过
}
```

#### `DefaultRewardedAdGatekeeper` (internal 实现类, @Singleton)

评估逻辑：
1. 检查 `AdsConfig.enabled`
2. 从 `RewardedAdUsageStore` 读取使用记录
3. 检查冷却期：`now - lastRewardEarnedAtMillis < hourlyCooldownMillis`
4. 检查每日上限：`dailyRewardCount >= dailyRewardLimit`
5. 门控通过时委托 `RewardedAdManager.show` 展示广告
6. 仅当结果为 `RewardEarned` 时调用 `usageStore.recordRewardEarned` 记录

---

### 使用记录存储

#### `RewardedAdUsageStore` (接口)

```kotlin
interface RewardedAdUsageStore {
    suspend fun getUsage(placement: RewardedAdPlacement): RewardedAdUsage
    suspend fun recordRewardEarned(placement: RewardedAdPlacement, nowMillis: Long)
    suspend fun clearDebugOnly()
}
```

#### `RewardedAdUsage` (data class)

```kotlin
data class RewardedAdUsage(
    val localDate: String,          // 本地日期 yyyy-MM-dd
    val dailyRewardCount: Int,      // 当日已获奖励次数
    val lastRewardEarnedAtMillis: Long, // 上次获奖励时间戳 (ms)
)
```

#### `SharedPreferencesRewardedAdUsageStore` (internal 实现类, @Singleton)

- 使用 `SimpleDateFormat("yyyy-MM-dd")` 格式化日期
- IO 操作在 `@IoDispatcher` 调度器上执行
- 日期变更时自动重置每日计数

#### `RewardedAdClock` / `SystemRewardedAdClock` (internal fun interface / 实现类)

```kotlin
internal fun interface RewardedAdClock {
    fun nowMillis(): Long
}
```

抽象系统时间，测试时可注入固定时间。默认实现调用 `System.currentTimeMillis()`。

---

## 对外暴露的接口

### 公共接口 (public)

模块对外暴露以下公共类型，供其他模块通过 Hilt 注入使用：

| 接口 | 主要方法 | 说明 |
|------|---------|------|
| `AdsConfigProvider` | `getConfig(): AdsConfig` | 获取广告配置 |
| `AdsConsentManager` | `gatherConsent(activity)` / `canRequestAds()` / `isPrivacyOptionsRequired()` / `showPrivacyOptions(activity)` / `resetForDebugOnly()` | UMP 同意管理 |
| `AdsInitializer` | `initialize(activity): AdsInitializationResult` | 广告 SDK 初始化 |
| `AppOpenAdManager` | `preload(activity)` / `showIfAvailable(activity, onShown): AppOpenAdResult` | 开屏广告加载和展示 |
| `RewardedAdManager` | `show(activity, placement): RewardedAdResult` | 激励广告加载和展示 |
| `RewardedAdGatekeeper` | `evaluate(placement): RewardedAdGateState` / `showIfRequired(activity, placement): RewardedAdResult` | 频率控制门控 |
| `RewardedAdUsageStore` | `getUsage(placement)` / `recordRewardEarned(placement, nowMillis)` / `clearDebugOnly()` | 使用记录存储 |

### 公共数据类型 (public)

| 类型 | 说明 |
|------|------|
| `AdsConfig` | 广告配置数据类 |
| `AdsDebugGeography` | 调试地理位置枚举 |
| `AppOpenAdConfig` | 开屏广告子配置 |
| `RewardedAdConfig` | 激励广告子配置 |
| `AdsConsentState` | 同意状态快照 |
| `AdsError` | 广告错误信息 |
| `AdsInitializationResult` | 初始化结果密封接口 (4 种状态) |
| `AppOpenAdResult` | 开屏广告展示结果密封接口 (4 种状态) |
| `RewardedAdPlacement` | 激励广告展示场景枚举 |
| `RewardedAdResult` | 激励广告展示结果密封接口 (7 种状态) |
| `RewardedAdGateState` | 门控评估结果密封接口 (4 种状态) |
| `RewardedAdUsage` | 使用记录数据类 |

### 内部类型 (internal，不对外暴露)

| 类型 | 说明 |
|------|------|
| `GoogleUserMessagingPlatform` | UMP SDK 封装接口 |
| `GoogleUserMessagingPlatformWrapper` | UMP SDK 封装实现 |
| `GoogleAdsSdk` | Mobile Ads SDK 封装接口 |
| `GoogleAdsSdkWrapper` | Mobile Ads SDK 封装实现 |
| `GoogleAppOpenAdLoader` | 开屏广告加载器接口 |
| `GoogleAppOpenAdLoaderWrapper` | 开屏广告加载器实现 |
| `GoogleAppOpenAdLoadResult` | 开屏广告加载结果密封接口 |
| `ManagedAppOpenAd` | 开屏广告管理接口 |
| `GoogleRewardedAdLoader` | 激励广告加载器接口 |
| `GoogleRewardedAdLoaderWrapper` | 激励广告加载器实现 |
| `GoogleRewardedAdLoadResult` | 广告加载结果密封接口 |
| `RewardedAdClock` | 时钟抽象接口 |
| `SystemRewardedAdClock` | 系统时钟实现 |
| `DefaultRewardedAdGatekeeper` | 门控器默认实现 |

### 外部模块典型使用方式

```kotlin
// 通过 Hilt 注入
@Inject lateinit var gatekeeper: RewardedAdGatekeeper
@Inject lateinit var consentManager: AdsConsentManager
@Inject lateinit var initializer: AdsInitializer

// 在需要展示广告的地方调用
val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)
when (result) {
    is RewardedAdResult.RewardEarned -> { /* 用户获得奖励 */ }
    is RewardedAdResult.SkippedRecentReward -> { /* 冷却期内 */ }
    is RewardedAdResult.SkippedDailyLimitReached -> { /* 每日上限 */ }
    // ...
}
```

---

## 依赖关系

### 模块依赖 (foundation:ads 依赖的)

| 依赖 | 类型 | 用途 |
|------|------|------|
| `projects.foundation.toolkit` | 项目模块 | 提供 `@IoDispatcher` 等 DI 限定符 |
| `google.play.services.ads` | 外部库 | Google Mobile Ads SDK |
| `google.user.messaging.platform` | 外部库 | Google UMP SDK (隐私同意) |
| `javax.inject` | 外部库 | `@Inject`, `@Singleton` 注解 |
| `kotlinx.coroutines.android` | 外部库 | Kotlin 协程 Android 支持 |
| `kotlinx.coroutines.core` | 外部库 | `Mutex`, `suspendCancellableCoroutine` 等 |
| `timber` | 外部库 | 日志记录 |
| `androidx.core.ktx` | 外部库 | Android KTX 扩展 |

### 测试依赖

| 依赖 | 用途 |
|------|------|
| `kotlinx.coroutines.test` | `runTest`, `StandardTestDispatcher` |
| `androidx.test.core` | `ApplicationProvider` |
| `robolectric` | Android 环境模拟 (SharedPreferences 等) |

### 被以下模块依赖

| 模块 | build.gradle.kts 位置 |
|------|----------------------|
| `app` | `app/build.gradle.kts` |
| `feature:sceneEdit:impl` | `feature/sceneEdit/impl/build.gradle.kts` |
| `feature:settings:impl` | `feature/settings/impl/build.gradle.kts` |

---

## 测试覆盖

### 测试文件

| 文件 | 路径 | 测试数量 |
|------|------|---------|
| `GoogleAdsInitializerTest` | `src/test/.../initialization/GoogleAdsInitializerTest.kt` | 3 |
| `RewardedAdGatekeeperTest` | `src/test/.../RewardedAdGatekeeperTest.kt` | 6 |
| `GoogleAppOpenAdManagerTest` | `src/test/.../appopen/GoogleAppOpenAdManagerTest.kt` | 4 |
| **合计** | **3 个文件** | **13 个测试** |

### GoogleAdsInitializerTest (3 个测试)

| 测试方法 | 验证内容 |
|---------|---------|
| `initialize_whenDisabled_returnsDisabledWithoutGatheringConsent` | 配置禁用时直接返回 `Disabled`，不调用同意收集和 SDK 初始化 |
| `initialize_whenConsentCannotRequestAds_doesNotInitializeSdk` | 用户不同意时不初始化 SDK，返回 `ConsentRequiredOrUnavailable` |
| `initialize_whenConsentAllowsAds_initializesSdkOnce` | 同意后初始化 SDK，第二次调用直接返回成功（幂等性验证） |

### RewardedAdGatekeeperTest (6 个测试)

| 测试方法 | 验证内容 |
|---------|---------|
| `showIfRequired_skipsAdWithinHourlyCooldown` | 冷却期内跳过广告，不调用 `show` 和 `record` |
| `showIfRequired_skipsAdAfterDailyLimitReached` | 每日上限后跳过广告 |
| `showIfRequired_recordsRewardEarned` | 获得奖励后正确记录使用数据 |
| `showIfRequired_technicalFailureDoesNotRecordReward` | 加载/展示失败时不记录奖励 |
| `showIfRequired_closedWithoutRewardDoesNotRecordReward` | 用户关闭但未获得奖励时不记录 |
| `usageStore_resetsCountOnLocalDateChange` | 日期变更时每日计数自动重置为 0 |

### GoogleAppOpenAdManagerTest (4 个测试)

| 测试方法 | 验证内容 |
|---------|---------|
| `preload_whenDisabled_skipsPreload` | 广告禁用时跳过预加载 |
| `preload_whenTimeout_doesNotCacheAd` | 预加载超时时不缓存广告 |
| `showIfAvailable_whenDisabled_returnsSkippedUnavailable` | 广告禁用时返回 SkippedUnavailable |
| `showIfAvailable_whenLoadTimeout_returnsTimedOut` | 展示时加载超时返回 TimedOut |

### 测试基础设施

测试使用以下 Fake 实现隔离外部依赖：

- `FakeAdsConfigProvider` / `FakeRewardedAdsConfigProvider` — 返回固定配置
- `FakeAdsConsentManager` — 返回预设的同意状态，记录调用次数
- `FakeGoogleAdsSdk` — 记录 `initialize` 和 `applyRequestConfiguration` 调用次数
- `FakeRewardedAdClock` — 返回固定时间，支持修改
- `FakeRewardedAdUsageStore` — 返回预设使用记录，记录 `recordRewardEarned` 调用
- `FakeRewardedAdManager` — 返回预设广告结果，记录 `show` 调用次数
- `FakeGoogleAppOpenAdLoader` — 返回预设的开屏广告加载结果，记录 `load` 调用次数

---

## 当前缺陷/改进点

### 测试覆盖不足

1. **缺少 `GoogleAdsConsentManager` 单元测试**：同意管理器的 `gatherConsent`、`canRequestAds`、`showPrivacyOptions`、`resetForDebugOnly` 等方法无测试覆盖
2. **缺少 `GoogleRewardedAdManager` 单元测试**：激励广告管理器的 `show` 方法无测试，包括初始化失败、加载失败、用户取消等场景
3. **缺少 `SharedPreferencesRewardedAdUsageStore` 的边界测试**：仅测试了日期重置场景，缺少多次奖励记录、并发读写等边界情况测试
4. **缺少 `BuildConfigAdsConfigProvider` 测试**：配置解析逻辑（逗号分隔的测试设备 ID、地理位置字符串转换）未测试
5. **缺少 `GoogleUserMessagingPlatformWrapper` 测试**：回调到协程的桥接逻辑未测试
6. **缺少 `GoogleAppOpenAdManager` 完整测试**：开屏广告管理器的缓存机制、并发加载、超时处理等场景测试不完整

### 设计问题

7. **`RewardedAdGatekeeper` 的 `showIfRequired` 存在竞态条件风险**：`evaluate` 和 `showIfRequired` 之间没有原子性保证，并发调用可能绕过频率限制
8. **`SharedPreferencesRewardedAdUsageStore` 使用 `apply()` 异步写入**：极端情况下应用崩溃可能导致奖励记录丢失，虽然 `apply()` 在大多数场景下足够安全
9. **`RewardedAdClock` 接口可见性不一致**：作为 `internal fun interface` 定义在 `rewarded` 子包中，但被 `DefaultRewardedAdGatekeeper`（根包）引用，存在包级别可见性穿透
10. **缺少远程配置支持**：`AdsConfigProvider` 仅有 `BuildConfig` 实现，无法动态调整冷却期和每日上限等参数
11. **`GoogleAdsSdkWrapper` 缺少 `MobileAds.getVersionString()` 日志**：初始化成功时未记录 SDK 版本，不利于问题排查
12. **开屏广告缓存机制可能造成内存泄漏**：`GoogleAppOpenAdManager` 中 `cachedAd` 使用 `ManagedAppOpenAd` 接口，但实际持有 `AppOpenAd` 引用，需确保正确释放

### 错误处理

13. **`GoogleRewardedAdManager.loadAndShow` 中 `show` 的异常处理**：`runCatching` 捕获所有异常类型过于宽泛，可能掩盖编程错误
14. **`GoogleAdsConsentManager.gatherConsent` 中错误优先级**：当 `updateError` 和 `formError` 同时存在时返回 `formError`，但未记录两个错误的关联关系
15. **缺少重试机制**：广告加载失败后没有自动重试逻辑，网络波动场景下用户体验可能受影响
16. **开屏广告超时处理不完善**：预加载和展示超时后直接返回，但未提供降级方案或用户反馈

---

## 代码统计

| 指标 | 数值 |
|------|------|
| 总文件数 | 21 |
| 源码文件数 | 19 |
| 测试文件数 | 2 |
| 总行数 | 1615 |
| 源码总行数 | 1333 |
| 测试总行数 | 282 |
| 测试用例数 | 13 |
| 子包数 | 5 (`config`, `consent`, `initialization`, `rewarded`, `appopen`) + 根包 + `di` |
| DI 绑定数 | 12 |
| 公共接口数 | 7 |
| 内部接口数 | 7 |
| 数据类/密封接口数 | 12 |

### 源码文件明细

| 文件 | 行数 | 包路径 |
|------|------|--------|
| `GoogleAppOpenAdManager.kt` | 323 | `appopen` |
| `GoogleRewardedAdManager.kt` | 154 | `rewarded` |
| `GoogleUserMessagingPlatform.kt` | 113 | `consent` |
| `SharedPreferencesRewardedAdUsageStore.kt` | 103 | `rewarded` |
| `RewardedAdGatekeeper.kt` | 79 | (根包) |
| `AdsModule.kt` | 95 | `di` |
| `GoogleAdsInitializer.kt` | 69 | `initialization` |
| `RewardedAdModels.kt` | 65 | (根包) |
| `GoogleAdsConsentManager.kt` | 64 | `consent` |
| `GoogleAdsSdk.kt` | 55 | `initialization` |
| `AdsConsentManager.kt` | 47 | (根包) |
| `AdsConfig.kt` | 61 | (根包) |
| `BuildConfigAdsConfigProvider.kt` | 40 | `config` |
| `AdsInitializer.kt` | 38 | (根包) |
| `AppOpenAdManager.kt` | 34 | (根包) |
| `RewardedAdUsageStore.kt` | 15 | (根包) |
| `AdsConfigProvider.kt` | 10 | (根包) |
