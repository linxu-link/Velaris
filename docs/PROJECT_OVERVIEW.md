# Velaris 项目说明

本文档基于当前仓库代码生成，作为理解 Velaris 运行结构、模块边界和主要数据流的入口。

## 项目定位

Velaris 是一个横屏沉浸式场景应用。核心体验围绕“场景”展开：用户进入应用后浏览不同分类的场景，场景可包含背景、视频、环境音、亮度/遮罩、定时、天气粒子等配置，并可进入编辑、场景列表、控制面板和设置等功能面板。

当前工程使用 Kotlin、Jetpack Compose、Hilt、Room、WorkManager、Navigation3、Media3/播放器相关封装、Google Mobile Ads 以及自定义 `advance.*` Gradle convention plugin。

## 模块结构

顶层模块由 `settings.gradle.kts` 注册：

- `:app`：应用入口，负责 `Application`、`Activity`、主题、导航根节点、广告初始化和启动屏状态。
- `:foundation:model`：领域模型和 Repository 接口，例如 `SceneResourceRepository`。
- `:foundation:domain`：Use Case 层，封装面向 UI 的业务入口，例如 `ObserveSceneResourcesUseCase`、`SaveSceneEditUseCase`。
- `:foundation:data`：Repository 实现和本地数据源，将 Room 实体转换为领域模型。
- `:foundation:database`：Room 数据库、DAO、Entity、Migration。
- `:foundation:navigation`：Navigation3 的状态持有与 `Navigator` 封装。
- `:foundation:designsystem`、`:foundation:ui`：通用 Compose 组件、主题规格和业务 UI 组件。
- `:foundation:player`：音频、视频播放控制和播放器池。
- `:foundation:particle`：雨雪等天气粒子渲染。
- `:foundation:ads`：广告配置、同意流程、开屏广告管理。
- `:foundation:toolkit`：应用上下文、协程、屏幕、权限、文件等工具能力。
- `:foundation:testing`：测试 fake 和测试规则。
- `:sync:work`：启动同步与数据库 seed，基于 WorkManager。
- `:feature:*:api`：功能模块对外导航 key 和必要 API。
- `:feature:*:impl`：功能模块实现，包含 ViewModel、Compose UI、导航 entry。

功能模块当前包括（详见 `docs/feature/` 目录下的独立文档）：

- `scene`：主场景页（根路由 + 面板聚合宿主）
- `sceneList`：场景列表与分类拖拽排序
- `sceneEdit`：场景编辑器（材质/声音/粒子/控制）
- `sceneControl`：实时控制面板（定时、音量、粒子、闹钟提醒等）
- `settings`：设置面板（主题、播放性能、广告同意、关于）
- `lock`：锁屏（独立 Activity，当前 build 中禁用）

优先阅读 `docs/feature/README.md` 了解 feature 模块的 api/impl 拆分规则和通用模式。

## 启动链路

应用入口在 `app/src/main/AndroidManifest.xml`：

- `VelarisApplication` 是 `android:name` 指定的应用类。
- `MainActivity` 是 launcher activity，固定横屏，并使用 `Theme.Velaris`。
- Manifest 声明了媒体读取权限和 Google Mobile Ads application id 占位符。

启动过程如下：

1. `VelarisApplication.onCreate()` 执行全局初始化：
   - debug 包种植 `Timber.DebugTree`。
   - 初始化 `HiToolKit`。
   - debug 包启用 StrictMode thread policy。
   - 调用 `Sync.initialize(this)`。
2. `Sync.initialize()` 使用 WorkManager 以 `ExistingWorkPolicy.KEEP` 入队唯一任务 `SyncWork_Database`。
3. `SyncWorker` 通过 Hilt 注入 `DatabaseSeeder`，执行 `databaseSeeder.seedIfEmpty()`。
4. `MainActivity.onCreate()` 安装 SplashScreen、启用 edge-to-edge、隐藏系统栏、设置 Compose 内容并异步初始化广告。
5. `MainActivityViewModel` 订阅 `ObserveSceneResourcesUseCase()`：
   - 场景列表为空时保持 `Loading`。
   - 场景列表非空后延迟 150ms 进入 `Ready`。
   - SplashScreen 的 `setKeepOnScreenCondition` 根据该状态决定是否继续显示。
6. `VelarisApp()` 创建 `NavigationState`，以 `SceneNavKey` 作为根页面，并通过 `NavDisplay` 渲染当前导航栈。

需要注意：场景数据是否能及时 seed 成功会直接影响启动屏是否退出。当前数据层和 WorkManager 都包含 seed 能力，但 `MainActivityViewModel` 只看 `observeSceneResources()` 的结果，若异常路径最终发出空列表，启动屏会持续保持。

## 导航设计

导航基础设施位于 `:foundation:navigation`：

- `rememberNavigationState(startKey, topLevelKeys)` 创建顶层栈和每个顶层页面的子栈。
- `NavigationState.currentTopLevelKey` 表示当前顶层 key。
- `NavigationState.currentSubStack` 表示当前顶层页面对应的子栈。
- `Navigator.navigate(key)` 根据 key 类型决定清空当前子栈、切换顶层页面或向当前子栈 push 页面。
- `Navigator.goBack()` 从子栈返回；如果已经在起始路由，会抛出错误。

`VelarisApp()` 当前只注册了一个顶层 key：`SceneNavKey`。`sceneEntry(navigator)` 注册主场景页，并把编辑页入口委托给 `sceneEditEntry(navigator)`。

功能模块采用 api/impl 分离：

- api 模块定义可序列化的 `NavKey`，供其他模块依赖。
- impl 模块提供 `EntryProviderScope<NavKey>` 扩展函数，把 key 映射到具体 Compose 页面。

这种结构让跨功能跳转依赖 api，而页面实现仍留在 impl 中。

## 场景数据流

场景数据的主路径是：

`Room Entity -> LocalSceneResource -> SceneResource -> UseCase -> ViewModel -> Compose UI`

关键组件：

- `VelarisDatabase`：Room database，当前 version 为 7，包含 `SceneEntity` 和 `SceneAudioEntity`。
- `SceneDao` / `SceneAudioDao`：读写场景和音轨。
- `SceneResourceLocalDataSource`：本地事实来源，负责 seed、保存、删除、排序、更新控制参数和音量。
- `DefaultSceneResourceRepository`：实现 `SceneResourceRepository`，把 local model 映射为领域模型，并解析 drawable 资源名。
- `ObserveSceneResourcesUseCase`：UI 层观察场景列表的入口。
- `SceneViewModel`：组合场景列表和交互状态，输出 `SceneUiState`。

`SceneViewModel` 会按 `selectedCategory` 过滤全部场景，并维护当前场景页、当前场景 ID、播放状态和面板显示状态。音量保存会调用 `UpdateSceneAudioVolumeUseCase`，最终落到 Room。

## 数据初始化

当前有两处 seed 路径：

- `sync/work` 中的 `DatabaseSeederImpl.seedIfEmpty()`：由 WorkManager 启动任务触发。
- `foundation/data` 中的 `SceneResourceLocalDataSource.seedDatabaseIfEmpty()`：在观察或读取场景前兜底触发。

两者都使用 `defaultLocalScenes()` 生成预置场景，并将视频、音频 raw resource 转成 `android.resource://<package>/<resId>` URI。

这能提高首次启动成功率，但也意味着 seed 逻辑存在重复。后续维护时应尽量保持两处字段写入一致，或收敛到同一个共享 seed 写入器，避免新增字段时一处遗漏。

## 构建与配置

构建逻辑当前依赖远端 convention plugin：

- `settings.gradle.kts` 在 `pluginManagement.repositories` 中配置了 GitHub Packages：`https://maven.pkg.github.com/linxu-link/android-convention-plugin`。
- `gradle/libs.versions.toml` 中 `convention-plugin = "0.1.7"`。
- 根 `build.gradle.kts` 应用 `advance.root`，并配置 foundation UI、designsystem、navigation 模块路径。
- `app/build.gradle.kts` 应用 `advance.android.application`、Compose、Hilt、Jacoco、OSS licenses、baseline profile、Roborazzi 等插件。

常用命令：

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :foundation:data:testDebugUnitTest
./gradlew :feature:scene:impl:testDebugUnitTest
```

如果本地无法解析远端 convention plugin，需要确认 GitHub Packages 凭据：

- Gradle property：`gpr.user`、`gpr.key`
- 或环境变量：`GITHUB_ACTOR`、`GITHUB_TOKEN`

## foundation:toolkit 接口

`:foundation:toolkit` 是应用通用工具模块，主要通过 `HiToolKit` 暴露能力。应用启动时建议在 `Application.onCreate()` 调用：

```kotlin
HiToolKit.init(application)
```

`HiToolKit.init(context)` 会初始化 `AppContext` 和 `SPUtils`。大多数工具方法都带有默认 `context = AppContext.app` 参数；如果未初始化，`AppContext.app` 会尝试反射获取当前 `Application`，失败时抛出 `IllegalStateException`。

### 统一入口

- `HiToolKit.init(context)`：初始化 toolkit。
- `HiToolKit.appContext`：当前 `Application` context。
- `HiToolKit.app`：应用信息工具 `AppUtils`。
- `HiToolKit.res`：资源工具 `ResUtils`。
- `HiToolKit.storage`：SharedPreferences 工具 `SPUtils`。
- `HiToolKit.density`：单位换算工具 `DensityUtils`。
- `HiToolKit.screen`：屏幕信息工具 `ScreenUtils`。
- `HiToolKit.keyboard`：软键盘工具 `KeyboardUtils`。
- `HiToolKit.toast`：Toast 工具 `ToastUtils`。
- `HiToolKit.brightness`：亮度工具 `BrightnessUtils`。
- `HiToolKit.device`：设备信息工具 `DeviceUtils`。
- `HiToolKit.clipboard`：剪贴板工具 `ClipboardUtils`。
- `HiToolKit.time`：时间日期工具 `TimeUtils`。
- `HiToolKit.file`：文件工具 `FileUtils`。
- `HiToolKit.intent`：系统 Intent 构建工具 `IntentUtils`。
- `HiToolKit.process`：进程工具 `ProcessUtils`。
- `HiToolKit.coroutine`：协程启动工具 `CoroutineKit`。

### 应用与上下文

- `AppVersionInfo(versionName, versionCode)`：应用版本信息数据结构，由 `app` 模块通过 Hilt 提供给设置等页面。
- `AppContext.app`：获取缓存或反射得到的 `Application`。
- `AppContext.init(context)`：手动注入应用 context。
- `AppContext.isInitialized`：是否已手动初始化。
- `AppUtils.getAppVersionName(context)` / `getAppVersionCode(context)`：读取版本名和版本号。
- `AppUtils.getPackageName(context)` / `getAppName(context)` / `getAppIcon(packageName, context)`：读取包名、应用名、图标。
- `AppUtils.isDebug(context)` / `isSystemApp(context)` / `isAppInstalled(packageName, context)`：应用状态判断。
- `AppUtils.getTargetSdkVersion(context)` / `getMinSdkVersion(context)`：SDK 配置读取。
- `AppUtils.getAppPath(context)` / `getDataDir(context)`：APK 路径和应用数据目录。

### 资源与存储

- `ResUtils.getString(resId, context)` / `getString(resId, vararg formatArgs, context)`：字符串资源读取。
- `ResUtils.getStringArray(resId, context)` / `getInt(resId, context)` / `getIntArray(resId, context)` / `getBoolean(resId, context)`：数组、整数、布尔资源读取。
- `ResUtils.getColor(resId, context)` / `getColorStateList(resId, context)` / `getDrawable(resId, context)`：颜色、状态颜色、Drawable 读取。
- `ResUtils.getDimension(resId, context)` / `getDimensionPixelOffset(resId, context)` / `getDimensionPixelSize(resId, context)`：尺寸资源读取。
- `ResUtils.isNightMode(context)` / `isRtl(context)`：夜间模式和 RTL 判断。
- `SPUtils.init(context)`：初始化 SharedPreferences 工具，通常由 `HiToolKit.init()` 调用。
- `SPUtils.getSp(name)`：获取指定 SP 文件，默认文件名为 `default_sp`。
- `SPUtils.put(key, value, spName)`：写入 `String`、`Int`、`Long`、`Float`、`Boolean`；`value = null` 时删除 key。
- `SPUtils.getString/getInt/getLong/getFloat/getBoolean(key, defaultValue, spName)`：读取基础类型。
- `SPUtils.remove(key, spName)` / `clear(spName)` / `contains(key, spName)` / `getAll(spName)`：删除、清空、包含判断和全量读取。

### 显示、输入与亮度

- `DensityUtils.dp2px/px2dp/sp2px/px2sp(value, context)`：dp、sp 与 px 的整型转换。
- `DensityUtils.dp2pxF/sp2pxF/pt2px/in2px/mm2px(value, context)`：基于 `TypedValue` 的浮点转换。
- `ScreenUtils.getScreenWidth/getScreenHeight(context)`：真实屏幕宽高，包含系统装饰区域。
- `ScreenUtils.getScreenWidthDp/getScreenHeightDp(context)`：屏幕 dp 尺寸。
- `ScreenUtils.getScreenDensity/getScreenDensityDpi(context)`：density 和 dpi。
- `ScreenUtils.getStatusBarHeight/getNavigationBarHeight(context)`：系统栏高度。
- `ScreenUtils.hasNavigationBar(context)` / `isLandscape(context)` / `isPortrait(context)`：导航栏和方向判断。
- `ScreenUtils.getScreenSize(context)`：应用窗口区域尺寸，Android R 及以上使用 `WindowMetrics`。
- `ScreenUtils.getRealScreenSize(context)`：真实屏幕尺寸。
- `KeyboardUtils.showSoftInput(editText)` / `hideSoftInput(view)` / `hideSoftInput(activity)` / `toggleSoftInput(context)` / `isActive(editText)`：软键盘控制。
- `ToastUtils.showShort(text|resId, context)` / `showLong(text|resId, context)` / `cancel()`：Toast 显示与取消，显示前会取消上一个 Toast。
- `BrightnessUtils.getSystemBrightness(context)` / `setSystemBrightness(brightness, context)`：系统亮度读写，写入需要 `WRITE_SETTINGS` 能力。
- `BrightnessUtils.isAutoBrightness(context)` / `setAutoBrightness(enabled, context)`：自动亮度模式读写。
- `BrightnessUtils.setWindowBrightness(window, brightness)` / `getWindowBrightness(window)`：当前窗口亮度控制，不修改系统设置。
- `WindowBrightnessEffect(brightness, restoreOnDispose)`：Compose 副作用，应用并可在 dispose 时恢复当前 Activity Window 的亮度。

### 设备、剪贴板与进程

- `DeviceUtils.getAndroidId(context)`：读取 `Settings.Secure.ANDROID_ID`。
- `DeviceUtils.getSdkVersion()` / `getSdkVersionName()`：系统 SDK 版本。
- `DeviceUtils.getDeviceBrand/getDeviceModel/getDeviceManufacturer/getDeviceBoard/getDeviceHardware/getDeviceFingerprint/getDeviceDisplay/getBuildId()`：设备构建信息。
- `DeviceUtils.getAbis()`：设备支持的 CPU ABI 列表。
- `DeviceUtils.isEmulator()` / `isTablet(context)`：模拟器和平板判断。
- `DeviceUtils.getSerialNumber()`：读取序列号；Android O 及以上可能需要 phone state 权限，失败时回退。
- `ClipboardUtils.copyText/getText(context)`：文本剪贴板读写。
- `ClipboardUtils.copyUri/getUri(context)`：URI 剪贴板读写。
- `ClipboardUtils.copyIntent/getIntent(context)`：Intent 剪贴板读写。
- `ClipboardUtils.clear(context)` / `hasText(context)`：清空和文本判断。
- `ProcessUtils.getProcessName(context)` / `isMainProcess(context)`：当前进程名和主进程判断。
- `ProcessUtils.getCurrentPid()` / `getCurrentUid()` / `getCurrentTid()`：当前进程、用户、线程 ID。
- `ProcessUtils.getRunningAppProcesses(context)` / `isAppForeground(context)`：运行进程列表和前台判断。
- `ProcessUtils.killBackgroundProcesses(packageName, context)`：请求杀死指定包名后台进程。

### 文件与 Intent

- `FileUtils.getFileByPath(path)` / `isFileExists(path)` / `isDir(path)` / `isFile(path)`：路径和类型判断。
- `FileUtils.createDir(path)` / `createFile(path)`：创建目录或文件。
- `FileUtils.deleteFile(path)` / `deleteDir(path)`：删除文件或递归删除目录。
- `FileUtils.getFileSize(path)` / `getDirSize(path)` / `formatFileSize(size)`：大小计算和格式化。
- `FileUtils.readFileToString(path)` / `writeStringToFile(path, content, append)`：UTF-8 文本读写。
- `FileUtils.copyFile(src, dest)` / `moveFile(src, dest)`：复制和移动。
- `FileUtils.getInternalCacheDir(context)` / `getInternalFilesDir(context)` / `getExternalCacheDir(context)` / `getExternalFilesDir(type, context)`：常用应用目录。
- `FileUtils.isExternalStorageWritable()` / `isExternalStorageReadable()`：外部存储状态。
- `FileUtils.closeIO(vararg closeables)`：安全关闭 `Closeable`。
- `IntentUtils.getLaunchAppIntent(packageName, context)`：构建打开应用主界面的 Intent。
- `IntentUtils.getInstallAppIntent(filePath, context)` / `getUninstallAppIntent(packageName)`：安装和卸载 Intent。Android N 及以上安装路径依赖 `${applicationId}.fileprovider`。
- `IntentUtils.getDialIntent(phoneNumber)` / `getCallIntent(phoneNumber)` / `getSendSmsIntent(phoneNumber, content)`：拨号、直拨、短信 Intent；直拨需要 `CALL_PHONE`。
- `IntentUtils.getBrowserIntent(url)` / `getEmailIntent(email, subject, text)`：浏览器和邮件 Intent。
- `IntentUtils.getShareTextIntent(text)` / `getShareImageIntent(imageUri)`：分享文本或图片。
- `IntentUtils.getCaptureIntent(outputUri)` / `getPickImageIntent()`：拍照和选择图片。
- `IntentUtils.getAppSettingsIntent(packageName)` / `getWifiSettingsIntent()` / `getBluetoothSettingsIntent()` / `getLocationSettingsIntent()` / `getDisplaySettingsIntent()` / `getSoundSettingsIntent()` / `getSystemSettingsIntent()`：系统设置页 Intent。
- `IntentUtils.getMapIntent(lat, lng, label)`：地图定位 Intent。

### 权限、时间与协程

- `PermissionUtils.request(vararg permissions, grantPolicy)`：构建权限请求描述。
- `PermissionUtils.visualMediaRequest(type)`：按 Android 版本构建图片或视频读取权限请求；Android 14 及以上包含 `READ_MEDIA_VISUAL_USER_SELECTED`，策略为 `Any`。
- `PermissionUtils.hasAccess(context, request)` / `hasPermission(context, permission)` / `hasAllPermissions(context, permissions)` / `hasAnyPermission(context, permissions)`：权限检查。
- `PermissionUtils.hasFullVisualMediaAccess(context, type)`：判断是否拥有完整图片或视频访问权限。
- `PermissionGrantPolicy.All` / `Any`：权限请求的全量或任一授权策略。
- `PermissionRequest(permissions, grantPolicy)`：权限请求数据结构。
- `VisualMediaPermissionType.Image` / `Video`：图片或视频媒体类型。
- `TimeUtils.now()`：当前毫秒时间戳。
- `TimeUtils.format(timestamp|date, pattern)` / `parse(timeStr, pattern)` / `parseToLong(timeStr, pattern)`：时间格式化和解析。
- `TimeUtils.getDateString/getTimeString/getDateTimeString(timestamp)`：常用时间字符串。
- `TimeUtils.getCalendar/getYear/getMonth/getDay/getHour/getMinute/getSecond(timestamp)`：日期字段读取。
- `TimeUtils.getDayOfWeek/getDayOfYear/getWeekOfYear(timestamp)`：星期、年内天数、年内周数。
- `TimeUtils.isToday(timestamp)` / `isSameDay(timestamp1, timestamp2)` / `isLeapYear(year)` / `getDaysInMonth(year, month)`：日期判断。
- `TimeUtils.millisToFitTimeSpan(millis, precision)`：友好时间跨度，例如 `1小时1分钟1秒`。
- `CoroutineKit.errorHandler`：全局协程异常处理回调（默认为空实现），可替换为 Timber 等日志系统。
- `CoroutineKit.ioScope/defaultScope/mainScope`：基于 Application `SupervisorJob` 的 scope。
- `CoroutineKit.launchIO/launchDefault/launchMain(block)` / `launch(dispatcher, block)`：在 Application scope 启动任务并返回 `Job`。
- `CoroutineKit.launchIn(scope, dispatcher, block)`：在外部 scope 中启动任务，并复用统一异常处理器。
- `CoroutineKit.cancelAll()`：取消 Application scope 下所有任务；取消后该内部 `SupervisorJob` 不会自动重建。
- Hilt dispatcher：`@IoDispatcher`、`@DefaultDispatcher`、`@MainDispatcher` 分别绑定 `Dispatchers.IO`、`Dispatchers.Default`、`Dispatchers.Main`。

## 运行时风险点

- 启动屏依赖场景列表非空：`ObserveSceneResourcesUseCase` 或 repository 异常时会 emit 空列表，可能导致 SplashScreen 一直停留。
- seed 逻辑重复：`DatabaseSeederImpl` 与 `SceneResourceLocalDataSource` 都能写入预置数据，字段演进时需要同步维护。
- `Navigator.goBack()` 在起始路由会抛错：根页面返回策略需要由宿主层谨慎处理。
- `Room.fallbackToDestructiveMigration()` 已启用：缺失 migration 时会破坏用户本地数据，应只在明确接受数据丢失的阶段保留。
- 支持 productFlavors（demo / prod）用于区分广告配置等；buildTypes（debug / release）提供 `.debug` / `.release` suffix 和独立的 signingConfig。
- release 默认回退使用 debug signingConfig（通过 gradle property 可配置真实 release keystore）。发布前需配置 velaris.release.* 属性并确认签名策略。
- 媒体权限覆盖 Android 13/14 路径：涉及用户相册资源时，需要确认运行时授权和 `READ_MEDIA_VISUAL_USER_SELECTED` 的受限访问行为。

## 新功能接入建议

新增功能页时，优先保持现有模式：

1. 在 `feature/<name>/api` 定义 `NavKey`。
2. 在 `feature/<name>/impl` 实现页面、ViewModel 和 `EntryProviderScope<NavKey>` 扩展。
3. 由需要跳转的上层 impl 依赖该功能的 api；只有聚合页面或宿主模块依赖 impl。
4. 业务数据先进入 `foundation:model` 定义接口和模型，再由 `foundation:data` 实现，`foundation:domain` 暴露 Use Case。
5. UI 状态在 ViewModel 中组合为单一 `UiState`，Compose 页面只消费状态和事件回调。

这样可以维持当前的模块边界：功能导航 API 稳定、实现可替换，数据访问不直接泄漏到 UI。
