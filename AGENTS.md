# AGENTS.md — Velaris 项目 AI Agent 指南

## 项目概述

Velaris 是一款横屏沉浸式场景应用。用户可以浏览场景分类、配置背景、视频、环境音、亮度/遮罩、定时器、天气粒子效果，并通过专用面板编辑和控制场景。

`AGENTS.md` 是 LLM 进入本项目的第一入口。先看这里的项目约束、模块边界和文档索引，再决定是否需要进入具体模块源码。

- **Application ID:** `com.wujia.velaris`
- **最低 SDK / 目标 SDK:** 由 convention plugin 统一管理
- **基础包名:** `com.wujia`

## 技术栈

| 技术 | 版本 |
|---|---|
| AGP | 9.2.0 |
| Kotlin | 2.3.0 |
| Compose BOM | 2025.09.01 |
| KSP | 2.3.4 |
| Hilt | 2.59 |
| Room | 2.8.3 |
| Navigation3 | 1.0.0 |
| Media3/ExoPlayer | 1.10.0 |
| Coroutines | 1.10.1 |
| kotlinx-serialization | 1.8.0 |
| Spotless + ktlint | 8.3.0 / 1.4.0 |

所有版本号统一定义在 `gradle/libs.versions.toml` 中，**禁止在模块 build.gradle.kts 中硬编码版本号**。

## 架构

### 分层结构 (Clean Architecture)

```
Presentation (Compose UI + ViewModel)
    ↓
Domain (Use Cases)
    ↓
Data (Repositories + LocalDataSources)
    ↓
Database (Room DAOs + Entities)
```

### 核心模式

- **MVVM:** 每个 feature 模块有一个 `@HiltViewModel`，暴露单个 `StateFlow<UiState>`，Compose Screen 仅消费状态和回调事件。
- **UDF (单向数据流):** ViewModel 暴露状态、接收事件；Compose 不直接修改状态。
- **Feature api/impl 拆分:** 每个 feature 有 `api` 模块（仅含 `NavKey`）和 `impl` 模块（ViewModel、Screen、导航入口）。跨 feature 导航只依赖 `api` 模块。
- **Navigation3:** 使用 `androidx.navigation3`，基于 `NavKey` 的类型安全路由，自定义多栈 `NavigationState`。

## 模块结构

### 模块分组

```
:app                              — 应用入口（Activity、Application、根导航）
:foundation:model                 — 领域模型 + Repository 接口（纯 Kotlin）
:foundation:domain                — Use Case 层
:foundation:data                  — Repository 实现 + 本地数据源
:foundation:database              — Room 数据库、DAO、Entity、Migration
:foundation:navigation            — Navigation3 状态管理 + Navigator
:foundation:designsystem          — 主题 token、自定义设计系统、可复用 Compose 组件
:foundation:ui                    — 业务级共享 Compose UI 组件
:foundation:player                — 音视频播放器（Media3/ExoPlayer）
:foundation:particle              — 天气粒子渲染（雨、雪）
:foundation:ads                   — Google 开屏广告、用户同意流程
:foundation:alarm                 — 闹钟提醒控制器（系统铃声 + AudioFocus，配合 alarmReminderEnabled）
:foundation:jetpack               — Jetpack 扩展工具（OffsetPagingSource 等）
:foundation:toolkit               — 工具类（context、coroutine、screen、permission、file 等）
:foundation:testing               — 共享测试 Fake 和测试 Rule
:sync:work                        — WorkManager 数据库种子数据/同步
:feature:{name}:api               — Feature NavKey 定义
:feature:{name}:impl              — Feature 实现（ViewModel、Screen、导航入口）
```

### 当前 Feature 模块

| Feature | API | Impl | 备注 |
|---|---|---|---|
| scene | `:feature:scene:api` | `:feature:scene:impl` | 根宿主，已集成 sceneEdit |
| sceneList | `:feature:sceneList:api` | `:feature:sceneList:impl` | Entry 已实现，尚未在 VelarisApp 注册 |
| sceneEdit | `:feature:sceneEdit:api` | `:feature:sceneEdit:impl` | 已通过 sceneEntry 内联注册 |
| sceneControl | `:feature:sceneControl:api` | `:feature:sceneControl:impl` | Entry 已实现，尚未在 VelarisApp 注册 |
| settings | `:feature:settings:api` | `:feature:settings:impl` | Entry 已实现，尚未在 VelarisApp 注册 |
| lock | `:feature:lock:api` | `:feature:lock:impl` | 独立 Activity（LockScreenActivity），通过 LockScreenLauncher 启动；当前在 settings.gradle.kts 中被注释禁用，未参与 Nav3 |

### 模块依赖规则

- Feature `impl` 模块可依赖其他 feature 的 `api` 模块（用于导航 key），**禁止**依赖其他 feature 的 `impl` 模块。
- **唯一例外:** `:feature:scene:impl` 作为根导航宿主，聚合所有其他 feature 的 impl。
- `foundation:data` 通过 `api` 依赖 `foundation:model`，通过 `impl` 依赖 `foundation:database`。
- `foundation:domain` 仅依赖 `foundation:model`（纯接口）。

## 包名规范

| 模块类型 | 包名模式 | 示例 |
|---|---|---|
| App | `com.wujia.velaris` | `com.wujia.velaris.MainActivity` |
| Foundation | `com.wujia.foundation.{module}` | `com.wujia.foundation.model.scene` |
| Feature API | `com.wujia.feature.{name}.api` | `com.wujia.feature.scene.api` |
| Feature Impl | `com.wujia.feature.{name}.impl` | `com.wujia.feature.scene.impl` |
| Sync | `com.wujia.velaris.sync` | `com.wujia.velaris.sync.workers` |

## 命名规范

### 类命名

| 元素 | 规范 | 示例 |
|---|---|---|
| NavKey | `{Feature}NavKey` | `SceneNavKey`, `SceneEditNavKey` |
| 导航入口 | `{feature}Entry` (扩展函数) | `sceneEntry()`, `sceneEditEntry()` |
| ViewModel | `{Feature}ViewModel` | `SceneViewModel`, `SceneEditViewModel` |
| UiState | `{Feature}UiState` | `SceneUiState`, `MainActivityUiState` |
| Screen | `{Feature}Screen` | `SceneScreen`, `SceneEditScreen` |
| Panel | `{Feature}Panel` | `SceneControlPanel`, `SettingsPanel` |
| Repository 接口 | `{Domain}Repository` | `SceneResourceRepository` |
| Repository 实现 | `Default{Domain}Repository` | `DefaultSceneResourceRepository` |
| Use Case | `{Verb}{Domain}UseCase` | `ObserveSceneResourcesUseCase` |
| LocalDataSource | `{Domain}LocalDataSource` | `SceneResourceLocalDataSource` |
| Entity | `{Domain}Entity` | `SceneEntity`, `SceneAudioEntity` |
| DAO | `{Domain}Dao` | `SceneDao`, `SceneAudioDao` |
| DI Module | `{Domain}DataModule` / `{Domain}Module` | `SceneDataModule`, `DatabaseModule` |
| 测试 Fake | `Fake{Interface}` | `FakeSceneResourceRepository` |
| 设计 Token | `Velaris{Category}` | `VelarisColor`, `VelarisTheme` |

### 函数命名

- ViewModel 事件处理: `on{Event}`（如 `onScenePageChange`、`onPlayingStateChanged`）
- 映射扩展: `asExternalModel()`、`asLocalInput()`、`asEditableScene()`
- Compose 入口: 小写 feature 名 + `Entry`（如 `sceneEntry`）

## 代码风格

- **格式化:** Spotless + ktlint 强制执行，提交前运行 `./gradlew spotlessApply`
- **许可证头:** Apache 2.0，由 Spotless 自动添加，模板在 `spotless/` 目录
- **Kotlin 代码风格:** `official`（`gradle.properties` 中配置）
- **文档语言:** 代码注释和 KDoc 使用**简体中文**
- **可见性:** ViewModel 类和 UiState 标记 `internal`；Database 类标记 `internal`；Repository 接口为 `public`
- **不可变性:** UiState 和设计 token 类使用 `@Stable` 或 `@Immutable` 注解
- **Compose 状态提升:** Screen 接收状态和事件回调，不直接持有可变状态
- **Flow 状态:** ViewModel 使用 `StateFlow` + `stateIn()`（通常 `WhileSubscribed(5_000)` 或 `Eager`）
- **密封接口:** 用于 UiState 和初始化结果（如 `MainActivityUiState { Loading, Ready }`）

## 构建系统

### Convention Plugin

项目使用自定义 convention plugin（`advance.*`），从 GitHub Packages 仓库获取：

```
仓库: https://maven.pkg.github.com/linxu-link/android-convention-plugin
认证: gpr.user / gpr.key (gradle.properties) 或 GITHUB_ACTOR / GITHUB_TOKEN (环境变量)
```

根 `build.gradle.kts` 配置：
```kotlin
advance {
    uiModulePath = ":foundation:ui"
    designsystemModulePath = ":foundation:designsystem"
    navigationModulePath = ":foundation:navigation"
}
```

### Build Type

- `debug`: applicationIdSuffix `.debug`
- `release`: applicationIdSuffix `.release`，R8 默认开启，使用 debug 签名配置

### 关键 Gradle Properties

| 属性 | 用途 |
|---|---|
| `gpr.user` / `gpr.key` | GitHub Packages 认证 |
| `minifyWithR8` | Release 混淆开关（默认 `true`） |
| `velaris.adsDebug` | 广告调试模式（仅 debug 构建，默认 `true`；release 强制 `false`） |

### 常用构建命令

```bash
# 构建 debug APK
./gradlew :app:assembleDebug

# 运行所有单元测试
./gradlew testDebugUnitTest

# 运行指定模块测试
./gradlew :foundation:data:testDebugUnitTest
./gradlew :feature:scene:impl:testDebugUnitTest

# 代码格式化
./gradlew spotlessApply

# 检查代码格式
./gradlew spotlessCheck
```

## 已有文档

优先阅读顺序：
1. `docs/PROJECT_OVERVIEW.md`
2. `docs/foundation/*.md`
3. 本文件其余约束说明

| 文件 | 内容 |
|---|---|
| `docs/PROJECT_OVERVIEW.md` | 项目全面概览、架构、启动链、导航设计、数据流 |
| `docs/foundation/*.md` | foundation 各模块职责、实现、边界（详见 docs/foundation/） |
| `docs/feature/README.md` | feature 模块总体设计、api/impl 拆分规则、通用模式、当前 feature 一览 |
| `docs/feature/scene.md` | scene（根宿主 + 主场景页）模块文档 |
| `docs/feature/sceneEdit.md` | sceneEdit 模块文档 |
| `docs/feature/sceneList.md` | sceneList 模块文档 |
| `docs/feature/sceneControl.md` | sceneControl 模块文档 |
| `docs/feature/settings.md` | settings 模块文档 |
| `docs/feature/lock.md` | lock 模块文档（特殊 Activity 模式） |
| `docs/plan/*.md` | 当前阶段的模块分析与实施计划 |

---

## 测试

### 框架与工具

| 工具 | 用途 |
|---|---|
| JUnit4 | 测试框架 |
| MainDispatcherRule | ViewModel 测试的协程调度器 Rule |
| Truth | 断言库 |
| Turbine | Flow 测试 |
| Robolectric | Android 上下文测试 |
| Roborazzi | 截图测试 |

### 测试规范

- 测试放在各模块的 `src/test/java/` 目录下
- ViewModel 测试使用 `MainDispatcherRule` + `runTest` + `advanceUntilIdle()`
- 共享 Fake 放在 `:foundation:testing` 模块中
- 测试文件命名: `{ClassUnderTest}Test.kt`

## 新增 Feature 指南

创建新 feature（以 `example` 为例）：

1. **创建 api 模块** `:feature:example:api`
   - 添加 `ExampleNavKey`（`data class` 或 `data object`，实现 `NavKey`，添加 `@Serializable`）
2. **创建 impl 模块** `:feature:example:impl`
   - `ExampleViewModel`：`@HiltViewModel`，暴露 `StateFlow<ExampleUiState>`
   - `ExampleScreen`：Compose Screen，消费状态、触发事件
   - `exampleEntry()`：`EntryProviderScope` 扩展函数，注册导航入口
   - `ExampleUiState`：`internal data class` 或 `sealed interface`
3. **注册导航**
   - 在 `:feature:scene:impl` 的导航 EntryProvider 中注册 `exampleEntry()`
4. **添加依赖**
   - 在使用方模块的 `build.gradle.kts` 中通过 `api(project(":feature:example:api"))` 依赖 api 模块
   - impl 模块通过 `implementation(project(":feature:example:impl"))` 添加

## 数据库

- **当前版本:** 14
- **迁移策略:** 11 个显式迁移（3→4 至 13→14） + destructive fallback
- **新增控制能力（v8+）：** guideCompleted、showCountdownClock、countdownClockPosition、alarmReminderEnabled、videoVolume、timerMode（Countdown/Clock）、clockAudioVolume
- **种子数据:** WorkManager 在启动时同步 + LocalDataSource 按需同步（已知重复风险）

## 关键设计决策

1. **Convention plugin** 外部化通用构建逻辑，保持模块 build.gradle.kts 简洁
2. **Navigation3** 实现类型安全的 `NavKey` 路由 + 自定义多栈导航状态
3. **Foundation 模块细粒度拆分**（model、domain、data、database 独立），强制依赖边界
4. **Feature api/impl 拆分** 防止 feature 间传递性依赖泄漏
5. **全 Compose UI** — 无 XML 布局（仅 Media3 PlayerView 的两个 surface/texture XML）
6. **Hilt** 作为唯一 DI 框架
7. **CompositionLocal** 传递设计 token（`LocalVelarisUiSpec`）和播放器配置（`LocalVelarisPlayerConfig`）

## foundation:toolkit API 参考

统一入口: `HiToolKit`（`com.wujia.foundation.toolkit.HiToolKit`），在 `Application.onCreate()` 中调用 `HiToolKit.init(context)` 初始化。

通过 `HiToolKit.xxx` 访问各工具类，或直接使用对应 `object` 单例。

### HiToolKit — 统一入口

| 属性 | 类型 | 说明 |
|---|---|---|
| `appContext` | `Context` | 缓存的 Application Context |
| `app` | `AppUtils` | 应用信息工具 |
| `res` | `ResUtils` | 资源访问工具 |
| `storage` | `SPUtils` | SharedPreferences 工具 |
| `density` | `DensityUtils` | dp/sp/px 单位转换 |
| `screen` | `ScreenUtils` | 屏幕信息工具 |
| `keyboard` | `KeyboardUtils` | 软键盘工具 |
| `toast` | `ToastUtils` | Toast 显示工具 |
| `brightness` | `BrightnessUtils` | 屏幕亮度工具 |
| `device` | `DeviceUtils` | 设备信息工具 |
| `clipboard` | `ClipboardUtils` | 剪贴板工具 |
| `time` | `TimeUtils` | 日期时间工具 |
| `file` | `FileUtils` | 文件操作工具 |
| `intent` | `IntentUtils` | Intent 构建工具 |
| `process` | `ProcessUtils` | 进程信息工具 |
| `coroutine` | `CoroutineKit` | 协程启动工具 |

### AppContext — Application Context 提供者

包名: `com.wujia.foundation.toolkit.app`

| 方法/属性 | 返回类型 | 说明 |
|---|---|---|
| `app` | `Application` | 获取缓存的 Application 实例（未初始化时通过反射获取） |
| `init(context)` | `Unit` | 手动初始化 |
| `isInitialized` | `Boolean` | 是否已初始化 |

### AppUtils — 应用信息

包名: `com.wujia.foundation.toolkit.app`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getAppVersionName(context)` | `String` | 版本名 |
| `getAppVersionCode(context)` | `Long` | 版本号 |
| `getPackageName(context)` | `String` | 包名 |
| `getAppName(context)` | `String` | 应用名称 |
| `getAppIcon(packageName?, context)` | `Drawable?` | 应用图标 |
| `isDebug(context)` | `Boolean` | 是否 debug 模式 |
| `isSystemApp(context)` | `Boolean` | 是否系统应用 |
| `isAppInstalled(packageName, context)` | `Boolean` | 应用是否已安装 |
| `getTargetSdkVersion(context)` | `Int` | 目标 SDK 版本 |
| `getMinSdkVersion(context)` | `Int` | 最低 SDK 版本 |
| `getAppPath(context)` | `String` | APK 路径 |
| `getDataDir(context)` | `String?` | 数据目录路径 |

### AppVersionInfo

包名: `com.wujia.foundation.toolkit`

| 属性 | 类型 | 说明 |
|---|---|---|
| `versionName` | `String` | 版本名 |
| `versionCode` | `Int` | 版本号 |

### CoroutineKit — 协程工具

包名: `com.wujia.foundation.toolkit.coroutine`

| 属性/方法 | 返回类型 | 说明 |
|---|---|---|
| `errorHandler` | `(Throwable) -> Unit` | 可替换的全局错误处理器 |
| `ioScope` | `CoroutineScope` | IO 调度器作用域 |
| `defaultScope` | `CoroutineScope` | Default 调度器作用域 |
| `mainScope` | `CoroutineScope` | Main 调度器作用域 |
| `launchIO(block)` | `Job` | 在 Application 作用域的 IO 调度器启动 |
| `launchDefault(block)` | `Job` | 在 Application 作用域的 Default 调度器启动 |
| `launchMain(block)` | `Job` | 在 Application 作用域的 Main 调度器启动 |
| `launch(dispatcher, block)` | `Job` | 在 Application 作用域的自定义调度器启动 |
| `launchIn(scope, dispatcher, block)` | `Job` | 在自定义作用域启动 |
| `cancelAll()` | `Unit` | 取消 Application 级别 SupervisorJob |

### Hilt Dispatcher 注入

包名: `com.wujia.foundation.toolkit.di`

| Qualifier 注解 | 提供 |
|---|---|
| `@IoDispatcher` | `Dispatchers.IO` |
| `@DefaultDispatcher` | `Dispatchers.Default` |
| `@MainDispatcher` | `Dispatchers.Main` |

`CoroutinesModule` 已安装在 `SingletonComponent`，ViewModel 中通过 `@Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher)` 注入。

### BrightnessUtils — 亮度工具

包名: `com.wujia.foundation.toolkit.display`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getSystemBrightness(context)` | `Int` | 系统亮度（0-255） |
| `setSystemBrightness(brightness, context)` | `Unit` | 设置系统亮度（需 WRITE_SETTINGS） |
| `isAutoBrightness(context)` | `Boolean` | 是否自动亮度 |
| `setAutoBrightness(enabled, context)` | `Unit` | 开关自动亮度 |
| `setWindowBrightness(window, brightness)` | `Unit` | 设置窗口亮度（0.0-1.0） |
| `getWindowBrightness(window)` | `Float` | 获取窗口亮度（0.0-1.0） |

### WindowBrightnessEffect — Compose 亮度副作用

包名: `com.wujia.foundation.toolkit.display`

```kotlin
@Composable
fun WindowBrightnessEffect(
    brightness: Float,        // 亮度值 0.0-1.0
    restoreOnDispose: Boolean = true  // dispose 时恢复原始亮度
)
```

### DensityUtils — 单位转换

包名: `com.wujia.foundation.toolkit.display`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `dp2px(dpValue, context)` | `Int` | dp → px（四舍五入） |
| `px2dp(pxValue, context)` | `Int` | px → dp |
| `sp2px(spValue, context)` | `Int` | sp → px |
| `px2sp(pxValue, context)` | `Int` | px → sp |
| `dp2pxF(dpValue, context)` | `Float` | dp → px（精确浮点） |
| `sp2pxF(spValue, context)` | `Float` | sp → px（精确浮点） |
| `pt2px(ptValue, context)` | `Float` | pt → px |
| `in2px(inValue, context)` | `Float` | 英寸 → px |
| `mm2px(mmValue, context)` | `Float` | 毫米 → px |

### KeyboardUtils — 软键盘

包名: `com.wujia.foundation.toolkit.display`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `showSoftInput(editText)` | `Unit` | 显示键盘 |
| `hideSoftInput(view)` | `Unit` | 隐藏键盘（View） |
| `hideSoftInput(activity)` | `Unit` | 隐藏键盘（Activity） |
| `toggleSoftInput(context)` | `Unit` | 切换键盘可见性 |
| `isActive(editText)` | `Boolean` | 键盘是否激活 |

### ScreenUtils — 屏幕信息

包名: `com.wujia.foundation.toolkit.display`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getScreenWidth(context)` | `Int` | 屏幕宽度 px |
| `getScreenHeight(context)` | `Int` | 屏幕高度 px |
| `getScreenWidthDp(context)` | `Int` | 屏幕宽度 dp |
| `getScreenHeightDp(context)` | `Int` | 屏幕高度 dp |
| `getScreenDensity(context)` | `Float` | 屏幕密度 |
| `getScreenDensityDpi(context)` | `Int` | 屏幕 DPI |
| `getStatusBarHeight(context)` | `Int` | 状态栏高度 px |
| `getNavigationBarHeight(context)` | `Int` | 导航栏高度 px |
| `hasNavigationBar(context)` | `Boolean` | 是否有导航栏 |
| `isLandscape(context)` | `Boolean` | 是否横屏 |
| `isPortrait(context)` | `Boolean` | 是否竖屏 |
| `getScreenSize(context)` | `Point` | 应用窗口尺寸 |
| `getRealScreenSize(context)` | `Point` | 真实屏幕尺寸（含系统装饰） |

### ToastUtils — Toast 显示

包名: `com.wujia.foundation.toolkit.display`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `showShort(text, context)` | `Unit` | 短时 Toast |
| `showShort(resId, context)` | `Unit` | 短时 Toast（资源 ID） |
| `showLong(text, context)` | `Unit` | 长时 Toast |
| `showLong(resId, context)` | `Unit` | 长时 Toast（资源 ID） |
| `cancel()` | `Unit` | 取消当前 Toast |

### ClipboardUtils — 剪贴板

包名: `com.wujia.foundation.toolkit.device`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `copyText(text, context)` | `Unit` | 复制文本 |
| `getText(context)` | `CharSequence?` | 读取文本 |
| `copyUri(uri, context)` | `Unit` | 复制 URI |
| `getUri(context)` | `Uri?` | 读取 URI |
| `copyIntent(intent, context)` | `Unit` | 复制 Intent |
| `getIntent(context)` | `Intent?` | 读取 Intent |
| `clear(context)` | `Unit` | 清空剪贴板 |
| `hasText(context)` | `Boolean` | 是否有文本 |

### DeviceUtils — 设备信息

包名: `com.wujia.foundation.toolkit.device`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getAndroidId(context)` | `String` | ANDROID_ID |
| `getSdkVersion()` | `Int` | SDK_INT |
| `getSdkVersionName()` | `String` | 系统版本名（如 "13"） |
| `getDeviceBrand()` | `String` | 设备品牌 |
| `getDeviceModel()` | `String` | 设备型号 |
| `getDeviceManufacturer()` | `String` | 制造商 |
| `getDeviceBoard()` | `String` | 主板 |
| `getDeviceHardware()` | `String` | 硬件 |
| `getDeviceFingerprint()` | `String` | 指纹 |
| `getDeviceDisplay()` | `String` | 显示 |
| `getBuildId()` | `String` | Build ID |
| `getAbis()` | `Array<String>` | 支持的 ABI |
| `isEmulator()` | `Boolean` | 是否模拟器 |
| `isTablet(context)` | `Boolean` | 是否平板 |
| `getSerialNumber()` | `String` | 设备序列号 |

### FileUtils — 文件操作

包名: `com.wujia.foundation.toolkit.file`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getFileByPath(filePath)` | `File?` | 根据路径获取 File |
| `isFileExists(filePath)` | `Boolean` | 文件是否存在 |
| `isDir(path)` | `Boolean` | 是否目录 |
| `isFile(path)` | `Boolean` | 是否文件 |
| `createDir(dirPath)` | `Boolean` | 创建目录（含父目录） |
| `createFile(filePath)` | `Boolean` | 创建文件（含父目录） |
| `deleteFile(filePath)` | `Boolean` | 删除文件/递归删除目录 |
| `deleteDir(dirPath)` | `Boolean` | 递归删除目录 |
| `getFileSize(filePath)` | `Long` | 文件大小（字节） |
| `getDirSize(dirPath)` | `Long` | 目录大小（字节） |
| `formatFileSize(size)` | `String` | 格式化文件大小（如 "1.50 MB"） |
| `readFileToString(filePath)` | `String` | 读取文件为字符串 |
| `writeStringToFile(filePath, content, append)` | `Boolean` | 写入字符串到文件 |
| `copyFile(srcPath, destPath)` | `Boolean` | 复制文件 |
| `moveFile(srcPath, destPath)` | `Boolean` | 移动文件 |
| `getInternalCacheDir(context)` | `File` | 内部缓存目录 |
| `getInternalFilesDir(context)` | `File` | 内部文件目录 |
| `getExternalCacheDir(context)` | `File?` | 外部缓存目录 |
| `getExternalFilesDir(type?, context)` | `File?` | 外部文件目录 |
| `isExternalStorageWritable()` | `Boolean` | 外部存储是否可写 |
| `isExternalStorageReadable()` | `Boolean` | 外部存储是否可读 |
| `closeIO(vararg closeables)` | `Unit` | 安全关闭 Closeable |

### IntentUtils — Intent 构建

包名: `com.wujia.foundation.toolkit.intent`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getLaunchAppIntent(packageName, context)` | `Intent?` | 启动应用 |
| `getInstallAppIntent(filePath, context)` | `Intent` | 安装 APK |
| `getUninstallAppIntent(packageName)` | `Intent` | 卸载应用 |
| `getDialIntent(phoneNumber)` | `Intent` | 打开拨号盘 |
| `getCallIntent(phoneNumber)` | `Intent` | 直接拨打 |
| `getSendSmsIntent(phoneNumber, content)` | `Intent` | 发送短信 |
| `getBrowserIntent(url)` | `Intent` | 打开浏览器 |
| `getEmailIntent(email, subject, text)` | `Intent` | 发送邮件 |
| `getShareTextIntent(text)` | `Intent` | 分享文本 |
| `getShareImageIntent(imageUri)` | `Intent` | 分享图片 |
| `getCaptureIntent(outputUri)` | `Intent` | 拍照 |
| `getPickImageIntent()` | `Intent` | 选择图片 |
| `getAppSettingsIntent(packageName)` | `Intent` | 应用详情设置 |
| `getWifiSettingsIntent()` | `Intent` | WiFi 设置 |
| `getBluetoothSettingsIntent()` | `Intent` | 蓝牙设置 |
| `getLocationSettingsIntent()` | `Intent` | 定位设置 |
| `getDisplaySettingsIntent()` | `Intent` | 显示设置 |
| `getSoundSettingsIntent()` | `Intent` | 声音设置 |
| `getSystemSettingsIntent()` | `Intent` | 系统设置 |
| `getMapIntent(lat, lng, label)` | `Intent` | 打开地图 |

### PermissionUtils — 权限工具

包名: `com.wujia.foundation.toolkit.permission`

**类型:**

| 类型 | 说明 |
|---|---|
| `PermissionGrantPolicy` | 枚举: `All`（全部授权）/ `Any`（任一授权） |
| `PermissionRequest` | 权限请求（`permissions: List<String>` + `grantPolicy`） |
| `VisualMediaPermissionType` | 枚举: `Image` / `Video`（版本感知的媒体权限） |

**方法:**

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `request(vararg permissions, grantPolicy)` | `PermissionRequest` | 创建权限请求 |
| `visualMediaRequest(type)` | `PermissionRequest` | 创建版本感知的媒体权限请求（处理 Android 14 部分访问） |
| `hasAccess(context, request)` | `Boolean` | 权限请求是否满足 |
| `hasPermission(context, permission)` | `Boolean` | 单个权限是否已授权 |
| `hasAllPermissions(context, permissions)` | `Boolean` | 所有权限是否已授权 |
| `hasAnyPermission(context, permissions)` | `Boolean` | 任一权限是否已授权 |
| `hasFullVisualMediaAccess(context, type)` | `Boolean` | 是否有完整媒体访问权限（非部分访问） |

### ProcessUtils — 进程信息

包名: `com.wujia.foundation.toolkit.process`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getProcessName(context)` | `String` | 当前进程名 |
| `isMainProcess(context)` | `Boolean` | 是否主进程 |
| `getCurrentPid()` | `Int` | 进程 ID |
| `getCurrentUid()` | `Int` | 用户 ID |
| `getCurrentTid()` | `Int` | 线程 ID |
| `getRunningAppProcesses(context)` | `List<RunningAppProcessInfo>` | 运行中的进程列表 |
| `isAppForeground(context)` | `Boolean` | 应用是否在前台 |
| `killBackgroundProcesses(packageName, context)` | `Unit` | 杀死后台进程 |

### ResUtils — 资源访问

包名: `com.wujia.foundation.toolkit.res`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getString(resId, context)` | `String` | 字符串资源 |
| `getString(resId, vararg formatArgs, context)` | `String` | 格式化字符串 |
| `getStringArray(resId, context)` | `Array<String>` | 字符串数组 |
| `getInt(resId, context)` | `Int` | 整数资源 |
| `getIntArray(resId, context)` | `IntArray` | 整数数组 |
| `getBoolean(resId, context)` | `Boolean` | 布尔资源 |
| `getColor(resId, context)` | `@ColorInt Int` | 颜色资源 |
| `getColorStateList(resId, context)` | `ColorStateList?` | 颜色状态列表 |
| `getDrawable(resId, context)` | `Drawable?` | Drawable 资源 |
| `getDimension(resId, context)` | `Float` | 尺寸资源（float） |
| `getDimensionPixelOffset(resId, context)` | `Int` | 尺寸资源（截断） |
| `getDimensionPixelSize(resId, context)` | `Int` | 尺寸资源（四舍五入） |
| `isNightMode(context)` | `Boolean` | 是否夜间模式 |
| `isRtl(context)` | `Boolean` | 是否 RTL 布局 |

### SPUtils — SharedPreferences

包名: `com.wujia.foundation.toolkit.storage`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `init(context)` | `Unit` | 初始化（HiToolKit.init 自动调用） |
| `getSp(name)` | `SharedPreferences` | 获取 SP 实例（默认 "default_sp"） |
| `put(key, value, spName)` | `Unit` | 写入值（null 删除 key） |
| `getString(key, defaultValue, spName)` | `String` | 读取 String |
| `getInt(key, defaultValue, spName)` | `Int` | 读取 Int |
| `getLong(key, defaultValue, spName)` | `Long` | 读取 Long |
| `getFloat(key, defaultValue, spName)` | `Float` | 读取 Float |
| `getBoolean(key, defaultValue, spName)` | `Boolean` | 读取 Boolean |
| `remove(key, spName)` | `Unit` | 删除 key |
| `clear(spName)` | `Unit` | 清空 SP |
| `contains(key, spName)` | `Boolean` | 是否包含 key |
| `getAll(spName)` | `Map<String, *>` | 获取所有键值对 |

### TimeUtils — 日期时间

包名: `com.wujia.foundation.toolkit.time`

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `now()` | `Long` | 当前时间戳 ms |
| `format(timestamp, pattern)` | `String` | 格式化时间戳 |
| `format(date, pattern)` | `String` | 格式化 Date |
| `parse(timeStr, pattern)` | `Date?` | 解析字符串为 Date |
| `parseToLong(timeStr, pattern)` | `Long` | 解析字符串为时间戳 |
| `getDateString(timestamp)` | `String` | "yyyy-MM-dd" |
| `getTimeString(timestamp)` | `String` | "HH:mm:ss" |
| `getDateTimeString(timestamp)` | `String` | "yyyy-MM-dd HH:mm:ss" |
| `getCalendar(timestamp)` | `Calendar` | 转为 Calendar |
| `getYear(timestamp)` | `Int` | 年 |
| `getMonth(timestamp)` | `Int` | 月（1-12） |
| `getDay(timestamp)` | `Int` | 日（1-31） |
| `getHour(timestamp)` | `Int` | 时（0-23） |
| `getMinute(timestamp)` | `Int` | 分（0-59） |
| `getSecond(timestamp)` | `Int` | 秒（0-59） |
| `getDayOfWeek(timestamp)` | `Int` | 星期（1=周一, 7=周日） |
| `getDayOfYear(timestamp)` | `Int` | 年中天数 |
| `getWeekOfYear(timestamp)` | `Int` | 年中周数 |
| `isToday(timestamp)` | `Boolean` | 是否今天 |
| `isSameDay(ts1, ts2)` | `Boolean` | 是否同一天 |
| `isLeapYear(year)` | `Boolean` | 是否闰年 |
| `getDaysInMonth(year, month)` | `Int` | 月天数 |
| `millisToFitTimeSpan(millis, precision)` | `String` | 毫秒转中文可读时长（如 "1天3小时2分钟"） |
