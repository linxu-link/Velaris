# foundation:model 模块文档

## 模块概述

`foundation:model` 是 Velaris 项目的纯 Kotlin 数据模型层模块，作为整个应用架构的最底层基石。该模块定义了所有业务领域模型（数据类、枚举）和仓储接口（Repository Interface），不包含任何实现逻辑，是领域驱动设计（DDD）中的"端口"定义层。

**核心定位：**
- 纯 Kotlin 数据类 + 接口定义，无业务逻辑实现
- 零外部依赖（仅依赖 `kotlinx.coroutines.core`）
- 被项目中几乎所有模块依赖，是数据流通的契约层
- 包含内置的 raw 资源文件（mp3/mp4），用于预制场景的种子数据

**模块路径：** `foundation/model/`

**包名：** `com.wujia.foundation.model`

---

## 架构设计

### 子域划分

模块按业务子域组织为 8 个子包，外加 2 个根级文件：

```
com.wujia.foundation.model/
├── SoundControlItem.kt              # 声音控制面板项（根级）
├── ProjectsIds.kt                   # 统一管理项目默认 seed 资源的固定 ID（根级）
├── scene/                           # 场景子域（最核心，233 行）
│   ├── SceneResource.kt             # 场景资源 + 音视频子资源 + 控制设置
│   ├── SceneResourceRepository.kt   # 场景仓储接口（7 个方法）
│   ├── SceneParticleSettings.kt     # 粒子效果设置模型
│   ├── SceneEditModels.kt           # 场景编辑相关模型
│   └── SceneCategory.kt            # 场景分类枚举
├── particle/                        # 粒子效果子域（95 行）
│   ├── ParticleResource.kt          # 粒子资源 + 枚举定义
│   └── ParticleResourceRepository.kt # 粒子仓储接口
├── media/                           # 媒体子域（74 行）
│   ├── MediaItem.kt                 # 设备媒体项 + 排序/类型枚举
│   └── MediaRepository.kt          # 媒体仓储接口
├── noise/                           # 噪声/音效子域（40 行）
│   ├── NoiseResource.kt             # 噪声资源 + 分类枚举
│   └── NoiseResourceRepository.kt   # 噪声仓储接口
├── background/                      # 背景子域（19 行）
│   ├── BackgroundResource.kt        # 背景资源
│   └── BackgroundResourceRepository.kt # 背景仓储接口
├── video/                           # 视频子域（19 行）
│   ├── VideoResource.kt             # 视频资源
│   └── VideoResourceRepository.kt   # 视频仓储接口
├── theme/                           # 主题子域（14 行）
│   └── VelarisThemePreset.kt        # 全局主题预设枚举
└── settings/                        # 设置子域（27 行）
    └── ThemeSettingsRepository.kt   # 主题设置仓储接口
```

### Repository 接口模式

所有子域均采用统一的 Repository Interface 模式：

1. **模型层（本模块）**：定义 `data class` + `interface XxxRepository`
2. **数据层（foundation:data）**：实现 Repository 接口，提供具体数据来源
3. **领域层（foundation:domain）**：通过 Repository 接口调用，不关心数据来源

这符合 DDD 中的"端口-适配器"架构（六边形架构），model 层定义端口，data 层提供适配器。

### 种子数据机制

通过 `@RawRes` 资源 ID 引用内置的 mp3/mp4 文件作为预制场景的种子数据。构建时通过自定义 Gradle Task `copyCategorizedRawResources` 将分类子目录中的资源扁平化到标准 `raw/` 目录。

---

## 核心类/接口详解

### 1. scene 子域

#### 1.1 SceneResource

**文件：** `scene/SceneResource.kt`
**行数：** 68 行（含配套类和常量）
**用途：** 场景领域模型，代表一个完整的沉浸式场景配置。

```kotlin
data class SceneResource(
    val id: String,                        // 场景唯一标识符
    val title: String,                     // 展示标题（如古诗词风格）
    val subtitle: String,                  // 副标题/描述
    val category: SceneCategory = SceneCategory.FOCUS,  // 场景分类
    val isPreset: Boolean = false,         // 是否为预制场景
    val backgroundResId: Int? = null,      // 背景图片资源 ID（@RawRes）
    val backgroundUri: String? = null,     // 设备媒体库图片 URI
    val video: SceneVideoResource? = null, // 视频资源（可为空）
    val audioTracks: List<SceneAudioResource> = emptyList(), // 音频轨道列表
    val controlSettings: SceneControlSettings = SceneControlSettings(), // 控制设置
)
```

**配套数据类：**

```kotlin
// 场景视频资源
data class SceneVideoResource(
    val uri: String,   // 视频 URI（android.resource:// 格式）
)

// 场景音频资源（可独立播放的音轨）
data class SceneAudioResource(
    val id: String,                                    // 音轨唯一标识符
    val title: String,                                 // 展示标题（如"风声"）
    val uri: String,                                   // 音频 URI
    val volume: Float = DEFAULT_SCENE_AUDIO_VOLUME,    // 音量 0f~1f
    val loop: Boolean = true,                          // 是否循环
)

// 场景控制设置（支持定时模式切换、倒计时/时钟 UI、闹钟提醒、视频音量、时钟音量等）
data class SceneControlSettings(
    val brightness: Float = SceneControlDefaults.BRIGHTNESS,              // 亮度 (0.5f)
    val darkness: Float = SceneControlDefaults.DARKNESS,                  // 暗度 (0.1f)
    val timerMode: SceneTimerMode = SceneTimerMode.Countdown,             // 定时模式（Countdown / Clock）
    val timerDurationMillis: Long = SceneControlDefaults.TIMER_DURATION_MILLIS, // 定时器 (45min)
    val fadeOutEnabled: Boolean = true,                                   // 智能淡出
    val guideCompleted: Boolean = false,                                  // 引导是否完成
    val showCountdownClock: Boolean = true,                               // 显示倒计时/时钟
    val alarmReminderEnabled: Boolean = false,                            // 闹钟提醒（到期播放系统铃声）
    val countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center, // 时钟位置
    val clockAudioVolume: Float = SceneControlDefaults.CLOCK_AUDIO_VOLUME, // 时钟/闹钟音量
    val particle: SceneParticleSettings = NO_PARTICLE,                    // 粒子效果设置
)

// 配套枚举
enum class SceneTimerMode { Countdown, Clock }
enum class SceneCountdownClockPosition { Center, TopStart, BottomStart, TopEnd, BottomEnd }
```

**SceneControlDefaults（统一默认值来源）：**

```kotlin
object SceneControlDefaults {
    const val BRIGHTNESS: Float = 0.5f
    const val BRIGHTNESS_DB: String = "0.5"
    const val DARKNESS: Float = 0.1f
    const val DARKNESS_DB: String = "0.1"
    const val TIMER_DURATION_MILLIS: Long = 45 * 60 * 1000L
    const val TIMER_DURATION_DB: String = "2700000"
    const val CLOCK_AUDIO_VOLUME: Float = 0.5f
}
```

**其他顶层常量：**

| 常量名 | 值 | 说明 |
|---|---|---|
| `DEFAULT_SCENE_AUDIO_VOLUME` | `0.6f` | 默认环境音音量 |
| `DEFAULT_SCENE_VIDEO_VOLUME` | `0f` | 默认视频音量（独立） |

#### 1.2 SceneResourceRepository

**文件：** `scene/SceneResourceRepository.kt`
**行数：** 68 行
**用途：** 场景资源仓储接口，是所有 Repository 中最复杂的，定义了场景的完整 CRUD 和控制操作。

```kotlin
interface SceneResourceRepository {
    // 观察所有场景资源的实时更新流
    fun observeSceneResources(): Flow<List<SceneResource>>

    // 根据 ID 获取单个场景资源
    suspend fun getSceneResource(id: String): SceneResource?

    // 获取可编辑的场景原始数据
    suspend fun getEditableScene(id: String): EditableScene?

    // 创建或更新场景编辑结果，返回保存后的场景 ID
    suspend fun saveSceneEdit(input: SceneEditInput): String

    // 删除非预制场景（预制场景受数据层保护）
    suspend fun deleteSceneResource(id: String)

    // 保存场景控制面板配置（画面、定时、淡出）
    suspend fun updateSceneControlSettings(sceneId: String, settings: SceneControlSettings)

    // 保存单个音轨的音量
    suspend fun updateSceneAudioVolume(sceneId: String, audioId: String, volume: Float)

    // 保存场景展示顺序（category 为 null 时更新全局顺序）
    suspend fun reorderSceneResources(category: SceneCategory?, orderedIds: List<String>)
}
```

**接口方法统计：** 8 个方法（1 个 Flow 观察 + 7 个 suspend 操作）

#### 1.3 SceneParticleSettings

**文件：** `scene/SceneParticleSettings.kt`
**行数：** 56 行
**用途：** 场景粒子效果的持久化配置模型。

```kotlin
enum class SceneParticleEffect { None, Snow, Rain, Fireflies }

enum class SceneParticleQuality { Low, Medium, High, Ultra }

data class SceneParticleSettings(
    val effect: SceneParticleEffect = SceneParticleEffect.None,   // 粒子效果类型
    val intensity: Float = DEFAULT_PARTICLE_INTENSITY,            // 强度 (0.0-1.0)
    val wind: Float = DEFAULT_PARTICLE_WIND,                      // 风力 (0.0-1.0)
    val quality: SceneParticleQuality = SceneParticleQuality.Medium, // 质量等级
    val foregroundGlassEnabled: Boolean = true,                   // 前景玻璃水痕（仅雨天）
) {
    init {
        require(intensity in 0f..1f) { "intensity must be in 0..1, got $intensity" }
        require(wind in 0f..1f) { "wind must be in 0..1, got $wind" }
    }
}
```

**常量：**
- `DEFAULT_PARTICLE_INTENSITY` 和 `DEFAULT_PARTICLE_WIND`：复用自 `ParticleResource.kt`
- `NO_PARTICLE`：无粒子效果的默认实例

#### 1.4 SceneEditModels

**文件：** `scene/SceneEditModels.kt`
**行数：** 33 行
**用途：** 场景编辑器使用的输入/编辑模型，与 `SceneResource` 分离以区分"展示模型"与"编辑模型"。

```kotlin
// 可编辑场景（从已有场景加载）
data class EditableScene(
    val id: String,
    val title: String,
    val description: String,
    val category: SceneCategory,
    val backgroundResName: String?,
    val backgroundUri: String?,
    val videoUri: String?,
    val audioTracks: List<SceneEditAudio>,
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

// 场景编辑输入（新建或更新时提交）
data class SceneEditInput(
    val id: String?,               // null 表示新建
    val title: String,
    val description: String,
    val category: SceneCategory,
    val backgroundResName: String?,
    val backgroundUri: String?,
    val videoUri: String?,
    val audioTracks: List<SceneEditAudio>,
    val controlSettings: SceneControlSettings = SceneControlSettings(),
)

// 编辑中的音频轨道
data class SceneEditAudio(
    val id: String,
    val title: String,
    val uri: String,
    val volume: Float = DEFAULT_SCENE_AUDIO_VOLUME,
    val loop: Boolean = true,
)
```

#### 1.5 SceneCategory

**文件：** `scene/SceneCategory.kt`
**行数：** 6 行
**用途：** 场景分类枚举，用于主场景页按模式筛选。

```kotlin
enum class SceneCategory(val displayName: String) {
    FOCUS("专注"),
    SLEEP("助眠"),
}
```

---

### 2. particle 子域

#### 2.1 ParticleResource

**文件：** `particle/ParticleResource.kt`
**行数：** 64 行
**用途：** 粒子效果资源模型，用于场景编辑器的粒子效果选择步骤。

```kotlin
data class ParticleResource(
    val id: String,
    val title: String,
    val description: String,
    val category: ParticleCategory,
    val effect: ParticleEffect,
    val intensity: Float,
    val wind: Float,
    val quality: ParticleQuality,
    val foregroundGlassEnabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val thumbnailResName: String? = null,
)
```

**配套枚举：**

```kotlin
// 粒子效果类型（与 foundation:particle.ParticleEffectType 对应）
enum class ParticleEffect(val displayName: String) {
    NONE("无"), RAIN("下雨"), SNOW("下雪"), FIREFLIES("萤火虫"),
}

// 粒子效果分类
enum class ParticleCategory(val displayName: String) {
    RAIN("雨天"), SNOW("雪天"), CALM("平静"), STORM("暴风雨"),
}

// 粒子效果质量（与 foundation:particle.RenderQuality 对应）
enum class ParticleQuality(val displayName: String) {
    LOW("低"), MEDIUM("中"), HIGH("高"),
}
```

**常量：**
- `DEFAULT_PARTICLE_INTENSITY = 0.72f`
- `DEFAULT_PARTICLE_WIND = 0.2f`

#### 2.2 ParticleResourceRepository

**文件：** `particle/ParticleResourceRepository.kt`
**行数：** 31 行
**用途：** 粒子效果资源仓储接口。

```kotlin
interface ParticleResourceRepository {
    fun observeParticleResources(): Flow<List<ParticleResource>>
    suspend fun getParticleResources(): List<ParticleResource>
    suspend fun getParticleResource(id: String): ParticleResource?
    suspend fun getParticleResourcesByCategory(category: ParticleCategory): List<ParticleResource>
}
```

---

### 3. media 子域

#### 3.1 MediaItem

**文件：** `media/MediaItem.kt`
**行数：** 50 行
**用途：** 设备媒体库中的媒体项模型，代表图片或视频文件。

```kotlin
data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val duration: Long,
    val bucketId: String?,
    val bucketName: String?,
) {
    val isVideo: Boolean       // mimeType 以 "video/" 开头
    val isImage: Boolean       // mimeType 以 "image/" 开头
    val formattedDuration: String  // 格式化时长 "H:MM:SS" 或 "M:SS"
}
```

**配套枚举：**

```kotlin
// 媒体类型过滤
enum class MediaType { IMAGE, VIDEO, ALL }

// 排序方式
enum class MediaSortOrder {
    DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC,
}
```

#### 3.2 MediaRepository

**文件：** `media/MediaRepository.kt`
**行数：** 24 行
**用途：** 设备媒体库访问仓储接口。注意：该接口无 Flow 观察方法（与其他 Repository 不同）。

```kotlin
interface MediaRepository {
    suspend fun getImages(sortOrder: MediaSortOrder = DATE_DESC, limit: Int? = null): List<MediaItem>
    suspend fun getVideos(sortOrder: MediaSortOrder = DATE_DESC, limit: Int? = null): List<MediaItem>
    suspend fun getAllMedia(sortOrder: MediaSortOrder = DATE_DESC, limit: Int? = null): List<MediaItem>
    suspend fun getMediaByType(mediaType: MediaType, sortOrder: MediaSortOrder = DATE_DESC, limit: Int? = null): List<MediaItem>
}
```

---

### 4. noise 子域

#### 4.1 NoiseResource

**文件：** `noise/NoiseResource.kt`
**行数：** 27 行
**用途：** 独立可选的音效素材资源，用于创建或编辑场景音轨。

```kotlin
data class NoiseResource(
    val id: String,
    val title: String,
    val description: String,
    val category: NoiseCategory,
    val uri: String,
    val tags: List<String> = emptyList(),
    val defaultVolume: Float = DEFAULT_NOISE_VOLUME,  // 1f
    val loop: Boolean = true,
)

enum class NoiseCategory(val displayName: String) {
    NATURE("自然"), HEALING("治愈"), FOCUS("专注"), SLEEP("助眠"),
}

const val DEFAULT_NOISE_VOLUME = 1f
```

#### 4.2 NoiseResourceRepository

**文件：** `noise/NoiseResourceRepository.kt`
**行数：** 13 行
**用途：** 噪声资源仓储接口。

```kotlin
interface NoiseResourceRepository {
    fun observeNoiseResources(): Flow<List<NoiseResource>>
    suspend fun getNoiseResources(): List<NoiseResource>
    suspend fun getNoiseResource(id: String): NoiseResource?
    suspend fun getNoiseResourcesByCategory(category: NoiseCategory): List<NoiseResource>
}
```

---

### 5. background 子域

#### 5.1 BackgroundResource

**文件：** `background/BackgroundResource.kt`
**行数：** 8 行
**用途：** 背景资源模型（结构最简）。

```kotlin
data class BackgroundResource(
    val id: String,
    val title: String,
    val description: String,
    val uri: String,
)
```

#### 5.2 BackgroundResourceRepository

**文件：** `background/BackgroundResourceRepository.kt`
**行数：** 11 行
**用途：** 背景资源仓储接口。

```kotlin
interface BackgroundResourceRepository {
    fun observeBackgroundResources(): Flow<List<BackgroundResource>>
    suspend fun getBackgroundResources(): List<BackgroundResource>
    suspend fun getBackgroundResource(id: String): BackgroundResource?
}
```

---

### 6. video 子域

#### 6.1 VideoResource

**文件：** `video/VideoResource.kt`
**行数：** 8 行
**用途：** 视频资源模型。

```kotlin
data class VideoResource(
    val id: String,
    val title: String,
    val description: String,
    val uri: String,
)
```

#### 6.2 VideoResourceRepository

**文件：** `video/VideoResourceRepository.kt`
**行数：** 11 行
**用途：** 视频资源仓储接口。

```kotlin
interface VideoResourceRepository {
    fun observeVideoResources(): Flow<List<VideoResource>>
    suspend fun getVideoResources(): List<VideoResource>
    suspend fun getVideoResource(id: String): VideoResource?
}
```

---

### 7. 根级文件

#### SoundControlItem

**文件：** `SoundControlItem.kt`
**行数：** 10 行
**用途：** 声音控制面板中的单个控制项（UI 展示模型）。

```kotlin
@Immutable
data class SoundControlItem(
    val title: String,          // 显示标题
    val icon: ImageVector,      // Compose 矢量图标
    val value: Float,           // 当前值
)
```

**注意：** 该类引用了 `androidx.compose.ui.graphics.vector.ImageVector`，是本模块中唯一与 Compose 有关联的类型。虽然模型层通常不应依赖 UI 框架，但 `ImageVector` 在此仅作为数据载体使用。

---

## 对外暴露的接口总览

### 数据类（Data Classes） - 13 个

| 类名 | 子域 | 行数 | 关键字段数 |
|---|---|---|---|
| `SceneResource` | scene | 28 | 10 |
| `SceneVideoResource` | scene | 3 | 1 |
| `SceneAudioResource` | scene | 7 | 5 |
| `SceneControlSettings` | scene | 11+ | 11（含 timerMode、guideCompleted、alarmReminderEnabled、clock* 等） |
| `SceneParticleSettings` | scene | 16 | 5 |
| `EditableScene` | scene | 12 | 8 |
| `SceneEditInput` | scene | 12 | 9 |
| `SceneEditAudio` | scene | 6 | 5 |
| `ParticleResource` | particle | 13 | 11 |
| `MediaItem` | media | 16 | 12 |
| `NoiseResource` | noise | 10 | 8 |
| `BackgroundResource` | background | 5 | 4 |
| `VideoResource` | video | 5 | 4 |
| `SoundControlItem` | root | 4 | 3 |

### 枚举类（Enums） - 10 个

| 枚举名 | 子域 | 值数量 | 用途 |
|---|---|---|---|
| `SceneCategory` | scene | 2 | 场景分类（专注/助眠） |
| `SceneParticleEffect` | scene | 4 | 粒子效果类型 |
| `SceneParticleQuality` | scene | 4 | 粒子质量等级 |
| `SceneTimerMode` | scene | 2 | 定时模式（Countdown / Clock） |
| `SceneCountdownClockPosition` | scene | 5 | 倒计时钟位置 |
| `ParticleEffect` | particle | 4 | 粒子效果类型 |
| `ParticleCategory` | particle | 4 | 粒子效果分类 |
| `ParticleQuality` | particle | 3 | 粒子效果质量 |
| `MediaType` | media | 3 | 媒体类型过滤 |
| `MediaSortOrder` | media | 6 | 排序方式 |
| `NoiseCategory` | noise | 4 | 噪声分类 |
| `VelarisThemePreset` | theme | 4 | 全局主题预设 |

### Repository 接口 - 7 个

| 接口名 | 子域 | 方法数 | 含 Flow 观察 |
|---|---|---|---|
| `SceneResourceRepository` | scene | 8 | 是 |
| `ParticleResourceRepository` | particle | 4 | 是 |
| `MediaRepository` | media | 4 | 否 |
| `NoiseResourceRepository` | noise | 4 | 是 |
| `BackgroundResourceRepository` | background | 3 | 是 |
| `VideoResourceRepository` | video | 3 | 是 |
| `ThemeSettingsRepository` | settings | 3 | 是 |

### 顶层常量 - 8 个

| 常量名 | 值 | 所在文件 |
|---|---|---|
| `DEFAULT_SCENE_AUDIO_VOLUME` | `1f` | SceneResource.kt |
| `DEFAULT_SCENE_BRIGHTNESS` | `0.8f` | SceneResource.kt |
| `DEFAULT_SCENE_DARKNESS` | `0.1f` | SceneResource.kt |
| `DEFAULT_SCENE_TIMER_DURATION_MILLIS` | `2700000L` (45min) | SceneResource.kt |
| `NO_PARTICLE` | `SceneParticleSettings(None)` | SceneParticleSettings.kt |
| `DEFAULT_PARTICLE_INTENSITY` | `0.72f` | ParticleResource.kt |
| `DEFAULT_PARTICLE_WIND` | `0.2f` | ParticleResource.kt |
| `DEFAULT_NOISE_VOLUME` | `1f` | NoiseResource.kt |

---

## 依赖关系

### 上游依赖（本模块依赖的库）

| 依赖 | 类型 | 说明 |
|---|---|---|
| `kotlinx.coroutines.core` | `api` | 提供 `Flow`、`suspend` 等协程原语 |
| `androidx.compose.runtime` | 隐式 | `SoundControlItem` 使用 `@Immutable` 注解 |
| `androidx.compose.ui` | 隐式 | `SoundControlItem` 使用 `ImageVector` |

**构建插件：** `advance.android.library`、`advance.android.library.compose`、`advance.android.library.jacoco`

### 下游依赖（依赖本模块的模块）

本模块被项目中 **12 个模块** 广泛依赖，是整个项目的共享契约层：

| 依赖方模块 | 依赖方式 | 说明 |
|---|---|---|
| `foundation:data` | `api` | 数据层实现 Repository 接口 |
| `foundation:domain` | `implementation` | 领域层通过 Repository 接口操作 |
| `foundation:testing` | `implementation` | 测试辅助模块 |
| `foundation:ui` | `implementation` | UI 基础组件层 |
| `feature:sceneEdit:api` | `api` | 场景编辑 Feature API |
| `feature:sceneList:api` | `api` | 场景列表 Feature API |
| `feature:scene:impl` | `implementation` | 场景 Feature 实现 |
| `feature:sceneEdit:impl` | `implementation` | 场景编辑 Feature 实现 |
| `feature:sceneControl:impl` | `implementation` | 场景控制 Feature 实现 |
| `feature:settings:impl` | `implementation` | 设置 Feature 实现 |
| `app` | `implementation` | 应用主模块 |

---

## 资源文件

### 资源目录结构

```
foundation/model/src/main/res/raw/
├── mp3/
│   ├── focus/
│   │   └── focus.mp3              # 专注音效
│   ├── heal/
│   │   └── fireplace_1.mp3        # 壁炉治愈音效
│   ├── nature/
│   │   ├── ocean_1.mp3            # 海浪音效
│   │   ├── rain_1.mp3             # 雨声音效
│   │   ├── thunder_1.mp3          # 雷声音效
│   │   └── wind_1.mp3             # 风声音效
│   ├── piano/
│   │   └── piano_1.mp3            # 钢琴音效
│   └── sleep/
│       └── sleep.mp3              # 助眠音效
└── mp4/
    ├── video1.mp4                 # 场景视频 1
    ├── video2.mp4                 # 场景视频 2
    └── video3.mp4                 # 场景视频 3
```

### 资源统计

| 类型 | 数量 | 分类目录数 |
|---|---|---|
| MP3 音频 | 8 个 | 5（focus, heal, nature, piano, sleep） |
| MP4 视频 | 3 个 | 1（mp4） |

### 构建时资源处理

`build.gradle.kts` 中定义了自定义 Gradle Task `copyCategorizedRawResources`（第 8-17 行），在构建前将分类子目录中的 mp3/mp4 文件扁平化到 `build/generated/res/categorizedRaw/raw/` 目录，确保 Android 资源系统能正确识别。

```kotlin
val copyCategorizedRawResources = tasks.register<Sync>("copyCategorizedRawResources") {
    from("src/main/res/raw") {
        include("**/*.mp3", "**/*.mp4")
        includeEmptyDirs = false
        eachFile {
            relativePath = RelativePath(true, "raw", name)
        }
    }
    into(generatedCategorizedRawResDir)
}
```

---

## 当前缺陷与改进点

### 已知问题

1. **`SoundControlItem` 引用 Compose 类型**
   - `ImageVector` 是 Compose UI 类型，出现在纯模型层中违反了分层原则
   - 建议：将 `SoundControlItem` 移至 `foundation:ui` 模块，或用 `Int`（drawable resource ID）替代 `ImageVector`

2. **`MediaRepository` 缺少 Flow 观察方法**
   - 其他 5 个 Repository 均提供 `observeXxx(): Flow<List<Xxx>>` 方法，但 `MediaRepository` 仅有 suspend 方法
   - 设备媒体库变化时无法自动通知 UI 层刷新

3. **Background 和 Video 子域模型过于简单**
   - `BackgroundResource` 和 `VideoResource` 仅有 4 个字段（id/title/description/uri），与其他资源模型相比缺少 `tags`、`category` 等分类能力
   - Repository 也缺少按分类查询的方法

4. **SceneParticleSettings 复用 ParticleResource 常量**
   - `SceneParticleSettings` 已改为直接引用 `DEFAULT_PARTICLE_INTENSITY` 和 `DEFAULT_PARTICLE_WIND`，消除了与旧 `DEFAULT_WEATHER_*` 常量的重复

5. **SceneParticleEffect 与 ParticleEffect 枚举值重复**
   - 两个枚举都定义了 None/Snow/Rain/Fireflies，且注释中提到"与 foundation:particle 对应"
   - 可考虑统一为单一枚举类型

6. **SceneEditModels 与 SceneResource 的字段差异**
   - `EditableScene` 使用 `description`，而 `SceneResource` 使用 `subtitle`
   - `EditableScene` 使用 `backgroundResName: String?`，而 `SceneResource` 使用 `backgroundResId: Int?`
   - 这种差异可能导致编辑-保存流程中的映射复杂性

### 架构改进建议

1. **引入资源统一抽象**：Background、Video、Noise、Particle 四个子域的 Repository 接口结构高度相似，可考虑引入泛型 `ResourceRepository<T>` 基接口
2. **补充 MediaRepository 的响应式能力**：添加 `observeMedia()` 方法以保持与其他 Repository 的一致性
3. **SceneCategory 扩展**：当前仅 FOCUS/SLEEP 两种分类，未来可按需添加更多场景模式
4. **版本化考虑**：作为被 12 个模块依赖的底层模块，接口变更影响面大，建议对 Repository 接口变更保持向后兼容
5. **ProjectsIds 常量管理**：新增的 `ProjectsIds` 对象管理所有种子资源的固定 ID，需确保与 data 层 seed 数据保持同步
6. **ThemeSettingsRepository 接口设计**：新增的主题设置仓储接口需要与 designsystem 模块的视觉映射协调

---

## 代码统计

### 文件汇总

| 子域 | 文件数 | 总行数 | 数据类 | 枚举 | 接口 |
|---|---|---|---|---|---|
| scene | 5 | 233 | 7 | 3 | 1 |
| particle | 2 | 95 | 1 | 3 | 1 |
| media | 2 | 74 | 1 | 2 | 1 |
| noise | 2 | 40 | 1 | 1 | 1 |
| background | 2 | 19 | 1 | 0 | 1 |
| video | 2 | 19 | 1 | 0 | 1 |
| theme | 1 | 14 | 0 | 1 | 0 |
| settings | 1 | 27 | 0 | 0 | 1 |
| root | 2 | 79 | 1 | 0 | 0 |
| **合计** | **19** | **600** | **13** | **10** | **7** |

### 按类型统计

- **Kotlin 源文件：** 19 个
- **总代码行数：** 600 行
- **数据类（data class）：** 13 个
- **枚举类（enum class）：** 10 个
- **接口（interface）：** 7 个
- **顶层常量（const val / val）：** 8 个
- **原始资源文件：** 11 个（8 mp3 + 3 mp4）
- **外部依赖：** 1 个（kotlinx.coroutines.core）

### 模块复杂度

- **最大文件：** `SceneResource.kt`（68 行）和 `SceneResourceRepository.kt`（68 行）
- **最小文件：** `SceneCategory.kt`（6 行）
- **最复杂接口：** `SceneResourceRepository`（8 个方法）
- **最简单接口：** `BackgroundResourceRepository` 和 `VideoResourceRepository`（各 3 个方法）
