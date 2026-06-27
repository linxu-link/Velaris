# foundation:player 模块文档

## 模块概述

`foundation:player` 是 Velaris 应用的媒体播放基础模块，采用**多播放器 in-process 架构**，为横屏沉浸式场景应用提供统一的媒体播放能力（2026-06 重构后为满足 Google Play 合规而移除前台 Service）。

- **视频通道**：基于 ExoPlayer + TextureView/SurfaceView 实现视频渲染，支持网络和本地视频资源
- **音频通道**：VelarisPlayerController 内部直接管理多个 ExoPlayer 实例，支持同时播放多个音频流（如场景背景音乐 + 环境音效 + 时钟音频）。所有音频逻辑都在应用进程内，无远程 Service。

两个通道在播放层面相互独立（可单独暂停视频而保持音频继续播放），但在音频焦点管理层面统一协调——当系统音频焦点丢失时，视频和音频均会做出相应响应。所有播放器资源生命周期由调用方（通常通过 rememberVelarisPlayerController）管理。

模块命名空间：`com.wujia.foundation.player`

---

## 架构设计

### 当前架构总览（重构后）

```
┌─────────────────────────────────────────────────────────────────┐
│                     VelarisPlayerController                     │
│                    （统一外观 / Facade 模式）                      │
│                                                                 │
│  ┌──────────────────────┐    ┌──────────────────────────────┐  │
│  │   视频播放器（本地）    │    │   音频播放器组（in-process） │  │
│  │   ExoPlayer 实例      │    │   多个 ExoPlayer 实例（LinkedMap）│
│  │   TextureView 渲染    │    │   支持同时多轨 + 时钟音频    │  │
│  └──────────────────────┘    └──────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │               AudioFocusController                       │  │
│  │   请求/释放音频焦点 → 回调驱动 videoPlayer + audioPlayers  │  │
│  │                  暂停/恢复/duck + 音量调整                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

**2026-06 重构说明**：为满足 Google Play 合规（移除前台媒体服务），已将原音频 Service 逻辑完全内联到 `VelarisPlayerController` 中。所有音频播放器现在都是应用进程内的普通 ExoPlayer 实例，由 Controller 直接管理生命周期（无跨进程 Intent、无 MediaSessionService、无前台通知要求）。

### VelarisPlayerController 作为统一外观

`VelarisPlayerController` 是模块对外暴露的唯一入口类（Facade 模式），它直接封装了：

1. **视频 ExoPlayer 实例**：直接持有并在当前线程操作
2. **音频播放器组**：内部使用 `LinkedHashMap` 管理多个 ExoPlayer（支持 `setPlaybackAudioItems`、`upsertAudioItem`、`removeAudioItem` 等），包括主音频和时钟音频（`clock-audio` 特殊处理，noFocus）。
3. **音频焦点控制器**：统一管理音频焦点的请求、释放及响应策略，同时影响视频和音频播放器。

调用方无需关心底层是视频还是音频，只需通过 Controller 的公共 API 进行播放控制。资源释放通过 `release()` / `releaseVideoOnly()`（历史兼容，现已简化）或 `DisposableEffect` 自动完成。

### 音频焦点处理策略

`AudioFocusController` 封装了 Android 音频焦点 API，通过回调同时驱动视频播放器和音频播放器组的行为：

| 焦点事件 | 行为 | 说明 |
|---|---|---|
| `AUDIOFOCUS_GAIN` | 恢复音量/播放 | 其他应用释放焦点 |
| `AUDIOFOCUS_LOSS` | 永久暂停所有播放器 | 其他应用开始播放（如音乐播放器） |
| `AUDIOFOCUS_LOSS_TRANSIENT` | 暂停播放，标记需恢复 | 临时中断（如来电） |
| `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` | 降低音量至 20% | 允许降音量继续播放（如导航语音） |

- 兼容 Android O（API 26）及以下版本
- AudioAttributes 使用 `USAGE_MEDIA` + `CONTENT_TYPE_MOVIE`
- 音频结束监听（`onPlaybackStateChanged == STATE_ENDED`）会更新 `endedAudioIds`，并在满足条件时自动 `stopAllAudio()`（已修复混合循环/非循环语义回归）

### 播放器池模式

`VelarisPlayerPool` 用于管理多页面场景下的播放器实例：

- **按页码缓存**：使用 `ConcurrentHashMap<Int, VelarisPlayerController>` 以页码为 key 存储播放器实例
- **距离淘汰策略**：`releaseOutsideRange(currentPage, retainedPageDistance)` 释放超出保留距离的页面播放器
- **选择性暂停**：`pauseVideosExcept(page)` 可暂停除当前页外所有视频
- 通过 `rememberVelarisPlayerPool()` 在 Compose 中自动管理生命周期

---

## 核心类/接口

### VelarisPlayerController（359 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VelarisPlayerController.kt`

统一的播放器控制器，协调视频播放器与音频播放服务。构造函数为 `internal`，应通过 `rememberVelarisPlayerController()` Composable 创建。

#### 构造

```kotlin
class VelarisPlayerController internal constructor(
    context: Context,
    private val config: VelarisPlayerConfig = VelarisPlayerConfig.Balanced,
)
```

#### Compose 可观察状态

| 属性 | 类型 | 说明 |
|---|---|---|
| `isVideoFirstFrameRendered` | `Boolean`（mutableStateOf） | 视频是否已渲染首帧 |
| `isVideoPlaying` | `Boolean`（mutableStateOf） | 视频是否正在播放 |

#### 公共方法

```kotlin
// 音频会话
val audioSessionId: Int  // 当前音频 session ID，可用于 Visualizer 等效果

// 视频控制
fun setVideoUri(uri: String?)  // 设置视频 URI，null/空白则清除

// 音频控制（通过 Service Intent 通信）
fun setAudioItems(items: List<AudioMediaItem>)  // 设置音频列表（id 必须唯一）

// 播放控制
fun play()        // 请求焦点 → 播放视频 → 启动音频 Service（如有音频）
fun pause()       // 暂停视频 → 暂停音频 → 释放焦点
fun pauseVideo()  // 仅暂停视频，不影响音频和焦点（用于滑动切换场景）
fun stopAudio()   // 停止所有音频播放

// 音量控制
fun setVideoVolume(volume: Float)                    // 设置视频音量 [0f, 1f]
fun setAudioVolume(audioId: String, volume: Float)   // 设置指定音频音量
fun setAudioMasterVolume(volume: Float)              // 设置所有音频主音量

// 生命周期
fun release()  // 释放所有资源（停止 Service、释放焦点、释放 Player）
```

#### 内部状态管理

- `currentVideoUri: String?` — 当前视频 URI，用于去重
- `currentAudioItems: List<AudioMediaItem>` — 当前音频列表缓存
- `currentAudioVolumes: MutableMap<String, Float>` — 各音频音量缓存
- `currentAudioMasterVolume: Float` — 主音量缓存
- `requestedVideoVolume: Float` — 用户请求的视频音量（非 duck 状态下的音量）
- `resumeVideoOnFocusGain: Boolean` — 临时失去焦点后是否需要恢复播放

#### 视频播放器配置

```kotlin
internal val videoPlayer: ExoPlayer = ExoPlayer.Builder(
    appContext,
    DefaultRenderersFactory(appContext)
        .setEnableDecoderFallback(config.enableDecoderFallback),
)
    .setAudioAttributes(/* USAGE_MEDIA + CONTENT_TYPE_MOVIE */, handleAudioFocus = false)
    .setHandleAudioBecomingNoisy(true)
    .build()
    .apply {
        repeatMode = Player.REPEAT_MODE_ALL  // 视频默认循环播放
    }
```

---

### VelarisAudioPlaybackService（488 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VelarisAudioPlaybackService.kt`

音频播放前台服务，继承 `MediaSessionService`，支持同时播放多个音频流。

#### 架构特点

- **多播放器架构**：每个 `AudioMediaItem` 对应一个独立的 `ExoPlayer` 实例，存储在 `LinkedHashMap<String, ExoPlayer>` 中
- **sessionPlayer 同步**：第一个音频项使用 `sessionPlayer`（与 MediaSession 绑定），其余使用独立 Player；通过 `syncingPrimaryPlayer` 标志防止 sessionPlayer 的回调递归触发同步
- **播放结束策略**：
  - `stopAllOnEnd = true` 的音频播放完毕后，停止所有音频并结束服务
  - 当所有非循环音频播放完毕后，自动结束服务

#### 生命周期

```
onCreate()
  ├── 创建 sessionPlayer + 监听器（同步所有 Player）
  ├── 创建 MediaSession
  └── promoteToForeground() → 创建通知渠道 + startForeground()

onStartCommand(intent)
  └── 根据 intent.action 分发命令:
      ├── ACTION_SET_AUDIO_ITEMS → setAudioItems()
      ├── ACTION_PLAY → playAll()
      ├── ACTION_PAUSE → pauseAll()
      ├── ACTION_STOP → stopAll() + stopSelf()
      ├── ACTION_SET_VOLUME → setAudioVolume()
      └── ACTION_SET_MASTER_VOLUME → setMasterVolume()

onGetSession() → 返回 mediaSession

onDestroy()
  ├── 重置 audioSessionId
  ├── 释放 MediaSession
  ├── 移除所有监听器
  ├── 释放 secondary Players
  ├── 释放 sessionPlayer
  └── 清空所有集合
```

#### 通知

- 渠道 ID：`velaris_audio_playback`
- 渠道名称：`场景音频播放`
- 优先级：`IMPORTANCE_LOW`
- 内容：`Velaris / 正在播放场景音频`
- 小图标：`android.R.drawable.ic_media_play`

#### Intent Action 与 Extra 常量

| Action | Extra Key | 说明 |
|---|---|---|
| `ACTION_SET_AUDIO_ITEMS` | `EXTRA_AUDIO_IDS`, `EXTRA_AUDIO_URIS`, `EXTRA_AUDIO_VOLUMES`, `EXTRA_AUDIO_TITLES`, `EXTRA_AUDIO_LOOPS`, `EXTRA_AUDIO_STOP_ALL_ON_ENDS` | 设置音频列表，数据通过并行数组传递 |
| `ACTION_PLAY` | — | 播放所有 |
| `ACTION_PAUSE` | — | 暂停所有 |
| `ACTION_STOP` | — | 停止所有并结束服务 |
| `ACTION_SET_VOLUME` | `EXTRA_AUDIO_ID`, `EXTRA_VOLUME` | 设置单个音频音量 |
| `ACTION_SET_MASTER_VOLUME` | `EXTRA_VOLUME` | 设置全局主音量 |

#### 静态属性

```kotlin
val audioSessionId: Int  // Compose 可观察状态，当前 sessionPlayer 的 audioSessionId
```

通过 `mutableIntStateOf` 实现，Compose UI 读取时可自动追踪变化触发重组。

#### Intent 序列化工具

```kotlin
internal fun List<AudioMediaItem>.toCommandExtras(): Bundle
```

将 `AudioMediaItem` 列表转换为 Bundle（并行数组格式），用于 Intent extras 传递。

---

### VelarisVideoPlayer（138 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VelarisVideoPlayer.kt`

Jetpack Compose 视频播放器组件及相关工厂函数。

#### Composable 函数

```kotlin
// 创建不自动管理媒体源的 Controller
@Composable
fun rememberVelarisPlayerController(): VelarisPlayerController

// 创建 Compose 感知的 Controller（自动管理视频/音频/播放状态）
@Composable
fun rememberVelarisPlayerController(
    videoUri: String? = null,
    audioItems: List<AudioMediaItem> = emptyList(),
    playWhenReady: Boolean = false,
): VelarisPlayerController

// 视频渲染组件
@Composable
fun VelarisVideoPlayer(
    controller: VelarisPlayerController,
    modifier: Modifier = Modifier,
)
```

#### `rememberVelarisPlayerController`（带参数版本）行为

1. `remember(config)` — 创建 Controller 实例，config 变化时重建
2. `LaunchedEffect(controller, videoUri)` — 监听视频 URI 变化并更新
3. `LaunchedEffect(controller, audioItems)` — 监听音频列表变化并更新
4. `LaunchedEffect(controller, playWhenReady)` — 监听播放状态变化
5. `DisposableEffect(controller)` — 离开 Composition 时调用 `release()`

#### `VelarisVideoPlayer` 渲染实现

- 使用 `AndroidView` 将原生 `PlayerView`（Media3 UI）嵌入 Compose
- Surface 类型由 `LocalVelarisPlayerConfig.useTextureView` 决定：
  - `true` → `foundation_player_velaris_video_player_texture.xml`（TextureView）
  - `false` → `foundation_player_velaris_video_player_surface.xml`（SurfaceView）
- 使用 `key(config.useTextureView)` 强制重建 AndroidView 以切换 Surface 类型
- 配置：`resizeMode = RESIZE_MODE_ZOOM`、透明 shutter 背景、透明背景、禁用内置控制器

---

### AudioFocusController（120 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/AudioFocusController.kt`

音频焦点控制器，负责管理音频焦点请求与释放。可见性为 `internal`。

```kotlin
internal class AudioFocusController(
    context: Context,
    private val onFocusGained: () -> Unit = {},
    private val onFocusLost: () -> Unit = {},
    private val onFocusLostTransient: () -> Unit = onFocusLost,
    private val onDuck: () -> Unit = {},
)
```

#### 公共方法

```kotlin
fun request(): Boolean  // 请求音频焦点，返回是否成功获取
fun abandon()           // 释放音频焦点（幂等，未持有焦点时直接返回）
```

#### 焦点请求配置

- AudioAttributes: `USAGE_MEDIA` + `CONTENT_TYPE_MOVIE`
- API >= 26: 使用 `AudioFocusRequest.Builder` 构建请求对象（提前构建，避免重复创建）
- API < 26: 使用已废弃的 `requestAudioFocus(listener, STREAM_MUSIC, AUDIOFOCUS_GAIN)` API

---

### VideoFirstFrame（93 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VideoFirstFrame.kt`

视频首帧提取与缓存，用作轻量级预览图。

#### Composable 函数

```kotlin
@Composable
fun rememberVideoFirstFrame(videoUri: String?): ImageBitmap?
```

行为流程：
1. 从 `LocalVelarisPlayerConfig` 读取缓存大小配置
2. 检查内存缓存（`VideoFirstFrameCache`，基于 `LruCache`）
3. 缓存未命中时，在 `Dispatchers.IO` 线程通过 `MediaMetadataRetriever` 提取首帧
4. 提取后存入缓存并返回 `ImageBitmap`

#### VideoFirstFrameCache

- 基于 `android.util.LruCache<String, Bitmap>` 实现
- 默认缓存大小：12 * 1024 KB（约 12MB），可通过 `VelarisPlayerConfig.firstFrameCacheSizeKb` 配置
- `configure()` 方法在缓存大小变化时重建缓存
- 使用 `@Volatile` 保证线程可见性

#### extractVideoFirstFrame

```kotlin
private fun extractVideoFirstFrame(context: Context, videoUri: String): Bitmap?
```

- 使用 `MediaMetadataRetriever.getFrameAtTime(0, OPTION_CLOSEST_SYNC)` 提取第一帧
- 捕获 `IllegalArgumentException`、`SecurityException`、`RuntimeException`，返回 null
- `finally` 块中调用 `retriever.release()`

---

### VelarisPlayerConfig（104 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VelarisPlayerConfig.kt`

播放器性能配置，通过 `CompositionLocal` 在 Compose 树中传递。

#### 配置参数

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `profile` | `VelarisPlayerPerformanceProfile` | `Balanced` | 性能档位标识 |
| `useTextureView` | `Boolean` | `true` | `true` = TextureView（兼容性好，开销较高），`false` = SurfaceView（省电） |
| `enableDecoderFallback` | `Boolean` | `true` | 首选解码器失败时是否尝试备选 |
| `retainedPageDistance` | `Int` | `1` | 播放器池保留的相邻页面数，`0` 仅保留当前页 |
| `maxSimultaneousAudioTracks` | `Int` | `Int.MAX_VALUE` | 同时播放的最大音频轨道数 |
| `firstFrameCacheSizeKb` | `Int` | `12 * 1024`（12MB） | 视频首帧缓存大小（KB） |

#### 预设档位

| 档位 | useTextureView | enableDecoderFallback | retainedPageDistance | maxSimultaneousAudioTracks | firstFrameCacheSizeKb |
|---|---|---|---|---|---|
| `PowerSaver` | `false` | `false` | `0` | `2` | `4 * 1024`（4MB） |
| `Balanced` | `true` | `true` | `1` | `Int.MAX_VALUE` | `12 * 1024`（12MB） |
| `Quality` | `true` | `true` | `2` | `Int.MAX_VALUE` | `16 * 1024`（16MB） |

#### CompositionLocal 传递

```kotlin
val LocalVelarisPlayerConfig = compositionLocalOf { VelarisPlayerConfig.Balanced }

@Composable
fun ProvideVelarisPlayerConfig(
    config: VelarisPlayerConfig,
    content: @Composable () -> Unit,
)
```

---

### VelarisPlayerPool（64 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/VelarisPlayerPool.kt`

播放器实例池，用于多页面场景（如横向翻页浏览场景）下管理多个播放器实例。

```kotlin
class VelarisPlayerPool(
    context: Context,
    private val config: VelarisPlayerConfig = VelarisPlayerConfig.Balanced,
)
```

#### 公共方法

```kotlin
fun get(page: Int): VelarisPlayerController
    // 获取或创建指定页码的播放器，内部使用 ConcurrentHashMap 缓存

fun releaseOutsideRange(currentPage: Int, retainedPageDistance: Int = config.retainedPageDistance)
    // 释放超出保留距离范围的页面播放器

fun pauseAllVideos()
    // 暂停所有页面的视频播放

fun pauseVideosExcept(page: Int)
    // 暂停除指定页面外的所有视频播放

fun releaseAll()
    // 释放所有播放器实例并清空缓存
```

#### Compose 集成

```kotlin
@Composable
fun rememberVelarisPlayerPool(): VelarisPlayerPool
// config 变化时重建 Pool，离开 Composition 时自动 releaseAll()
```

---

### AudioMediaItem（45 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/AudioMediaItem.kt`

音频媒体项数据类，描述一个可播放的音频资源。

```kotlin
data class AudioMediaItem(
    val id: String,               // 唯一标识符（不能为空）
    val uri: String,              // 音频 URI（网络 URL 或本地路径，不能为空）
    val volume: Float = 1f,       // 音量 [0f, 1f]
    val title: String = id,       // 显示标题
    val loop: Boolean = false,    // 是否循环播放
    val stopAllOnEnd: Boolean = false,  // 播放结束时是否停止所有音频
)
```

#### 内部工具

```kotlin
// 校验列表中音频 id 的唯一性
internal fun List<AudioMediaItem>.requireUniqueAudioIds()

// 音量范围常量
internal const val DEFAULT_VOLUME = 1f
internal const val MIN_VOLUME = 0f
internal const val MAX_VOLUME = 1f

// 音量范围限制扩展函数
internal fun Float.coercePlayerVolume(): Float
```

---

### PlaybackSettingsRepository（27 行）

**文件路径**：`foundation/player/src/main/java/com/wujia/foundation/player/PlaybackSettingsRepository.kt`

播放配置仓库接口，负责播放性能档位的持久化与观察。由 `foundation:data` 模块的 `DefaultPlaybackSettingsRepository` 实现。

```kotlin
interface PlaybackSettingsRepository {
    fun observePerformanceProfile(): Flow<VelarisPlayerPerformanceProfile>
    fun getPerformanceProfile(): VelarisPlayerPerformanceProfile
    suspend fun updatePerformanceProfile(profile: VelarisPlayerPerformanceProfile)
}
```

**主要方法：**
- `observePerformanceProfile()`：观察当前播放性能档位的实时更新流
- `getPerformanceProfile()`：获取当前持久化的播放性能档位
- `updatePerformanceProfile(profile)`：更新播放性能档位并持久化

---

## 对外暴露的接口

### 公共类与函数

| 类型 | 名称 | 说明 |
|---|---|---|
| `class` | `VelarisPlayerController` | 统一播放器控制器（构造函数 internal） |
| `data class` | `VelarisPlayerConfig` | 播放器性能配置 |
| `data class` | `AudioMediaItem` | 音频媒体项数据类 |
| `class` | `VelarisPlayerPool` | 播放器实例池 |
| `interface` | `PlaybackSettingsRepository` | 播放设置仓储接口（持久化性能档位） |
| `enum class` | `VelarisPlayerPerformanceProfile` | 性能档位枚举（PowerSaver / Balanced / Quality） |
| `@Composable` | `rememberVelarisPlayerController()` | 创建不自动管理媒体源的 Controller |
| `@Composable` | `rememberVelarisPlayerController(videoUri, audioItems, playWhenReady)` | 创建 Compose 感知的 Controller |
| `@Composable` | `VelarisVideoPlayer(controller, modifier)` | Compose 视频播放器组件 |
| `@Composable` | `rememberVideoFirstFrame(videoUri)` | 视频首帧提取（返回 `ImageBitmap?`） |
| `@Composable` | `rememberVelarisPlayerPool()` | 创建播放器实例池 |
| `@Composable` | `ProvideVelarisPlayerConfig(config, content)` | 在 Compose 子树中提供播放器配置 |
| `val` | `LocalVelarisPlayerConfig` | CompositionLocal，默认值 `VelarisPlayerConfig.Balanced` |

### AndroidManifest 声明

**权限**：

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Service 声明**：

```xml
<service
    android:name=".VelarisAudioPlaybackService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

### 布局资源

| 资源文件 | Surface 类型 | 说明 |
|---|---|---|
| `foundation_player_velaris_video_player_texture.xml` | `texture_view` | TextureView 模式 |
| `foundation_player_velaris_video_player_surface.xml` | `surface_view` | SurfaceView 模式 |

两个布局均为 `match_parent` 的 `PlayerView`，`resize_mode="zoom"`，`use_controller="false"`。

---

## 依赖关系

### 本模块依赖

**build.gradle.kts 声明**：

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
}
```

| 依赖 | 用途 |
|---|---|
| `androidx.core.ktx` | AndroidX Core KTX 扩展（ContextCompat 等） |
| `androidx.compose.ui` | Jetpack Compose UI（AndroidView、CompositionLocal、状态管理） |
| `media3.exoplayer` | ExoPlayer 核心播放器引擎 |
| `media3.session` | MediaSession / MediaSessionService（后台音频服务、系统媒体控制） |
| `media3.ui` | PlayerView（视频渲染 UI 组件） |

**Gradle 插件**：
- `advance.android.library` — Android Library 配置
- `advance.android.library.compose` — Compose 支持
- `advance.android.library.jacoco` — 代码覆盖率
- `advance.hilt` — Hilt 依赖注入

### 依赖本模块的模块

| 模块 | 声明方式 |
|---|---|
| `feature:scene:impl` | `implementation(projects.foundation.player)` |
| `feature:sceneEdit:impl` | `implementation(projects.foundation.player)` |
| `feature:settings:impl` | `implementation(projects.foundation.player)` |

---

## 当前缺陷/改进点

### 错误处理

1. **音频焦点请求失败静默忽略**：`play()` 中如果 `audioFocusController.request()` 返回 `false`，直接 return 不播放，但不通知 UI 层焦点获取失败的原因
2. **Intent 通信无错误回传**：Controller 向 Service 发送 Intent 后无法获知执行结果，缺少成功/失败回调机制
3. **Service 启动异常未处理**：`startAudioService()` 未捕获 `IllegalStateException`（如后台启动限制），可能在特定设备上崩溃
4. **`extractVideoFirstFrame` 异常静默吞没**：所有异常返回 null，无日志记录，调试困难

### 生命周期问题

5. **Service 生命周期与 Controller 不完全同步**：Controller 调用 `release()` 时通过 `stopService()` 停止 Service，但如果 Service 正在前台播放，系统可能延迟销毁
6. **（已修复）历史上 `releaseVideoOnly()` + `releaseAudioOnDispose=false` 分支**：该死分支及危险参数已在 2026-06 重构中删除。现在 `rememberVelarisPlayerController` 只有完整释放路径，所有调用方 dispose 时都会彻底清理所属的音频播放器。相关文档和调用点注释已同步更新。
7. **Compose 状态与 Service 状态不同步**：`isVideoPlaying` 仅跟踪 Controller 侧的状态，如果 Service 侧异常（如系统杀死 Service），Controller 不会感知
8. **`audioSessionId` 在 Service 销毁后仍可读取**：虽然 `onDestroy()` 会重置为 `C.INDEX_UNSET`，但存在时序窗口

### 多播放器同步

9. **`syncingPrimaryPlayer` 标志非线程安全**：使用普通 `Boolean` 而非 `@Volatile` 或原子变量，存在潜在的多线程可见性问题
10. **secondary Player 与 sessionPlayer 的同步延迟**：`playAll()` 中逐个设置 `playWhenReady`，多个 Player 之间可能存在微小的播放不同步

### 配置与灵活性

11. **通知样式硬编码**：通知的标题（"Velaris"）、文本（"正在播放场景音频"）和图标均为硬编码，无法自定义
12. **视频音频属性不一致**：视频 ExoPlayer 使用 `CONTENT_TYPE_MOVIE`，Service 中音频 Player 使用 `CONTENT_TYPE_MUSIC`，语义不统一
13. **无进度查询 API**：缺少获取视频/音频播放进度的公共方法
14. **无播放速度控制**：不支持倍速播放

### 内存与性能

15. **`VideoFirstFrameCache` 线程安全性**：使用 `@Volatile` 仅保证引用可见性，`configure()` 方法在多线程调用时可能创建多次缓存实例
16. **AudioMediaItem 列表全量替换**：`setAudioItems()` 每次调用都会释放所有 secondary Player 并重新创建，无增量更新
17. **`VelarisPlayerPool` 并发问题**：`releaseOutsideRange()` 遍历 keys 后再逐个 remove，ConcurrentHashMap 的迭代器为弱一致性，理论上可能遗漏

### 其他

18. **缺少单元测试**：整个模块无测试文件
19. **Hilt 依赖未使用**：`build.gradle.kts` 引入了 `advance.hilt` 插件，但源码中未使用 `@Inject`、`@HiltViewModel` 等注解
20. **缺少播放错误监听**：ExoPlayer 的 `Player.Listener.onError()` 未处理，播放错误不会通知 UI 层

---

## 代码统计

| 指标 | 数值 |
|---|---|
| Kotlin 源文件数 | 9 |
| XML 布局文件数 | 2 |
| AndroidManifest.xml | 1 |
| build.gradle.kts | 1 |
| Kotlin 代码总行数 | 1,438 |
| XML 文件总行数 | 17 |
| **代码总行数（含 XML）** | **1,466** |

### 各文件行数

| 文件 | 行数 | 职责 |
|---|---|---|
| `VelarisAudioPlaybackService.kt` | 488 | 音频前台服务（MediaSessionService） |
| `VelarisPlayerController.kt` | 359 | 统一播放器控制器（Facade） |
| `VelarisVideoPlayer.kt` | 138 | Compose 视频播放器组件 + Controller 工厂 |
| `AudioFocusController.kt` | 120 | 音频焦点管理 |
| `VelarisPlayerConfig.kt` | 104 | 性能配置 + CompositionLocal |
| `VideoFirstFrame.kt` | 93 | 视频首帧提取与缓存 |
| `VelarisPlayerPool.kt` | 64 | 播放器实例池 |
| `AudioMediaItem.kt` | 45 | 音频媒体项数据类 |
| `PlaybackSettingsRepository.kt` | 27 | 播放设置仓储接口 |

### 关键指标

- **公共 API 暴露**：VelarisPlayerController（10 个公共方法 + 2 个公共属性）、VelarisPlayerPool（5 个公共方法）、PlaybackSettingsRepository（3 个方法）
- **Compose 组件**：6 个 `@Composable` 函数
- **Intent Action 数量**：6 种（SET_AUDIO_ITEMS, PLAY, PAUSE, STOP, SET_VOLUME, SET_MASTER_VOLUME）
- **性能档位**：3 种预设（PowerSaver, Balanced, Quality）
- **音频焦点回调**：4 种状态处理
- **声明权限数**：4 个
