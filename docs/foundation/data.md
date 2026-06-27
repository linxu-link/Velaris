# `foundation:data` 模块文档

## 1. 模块概述

`foundation:data` 是 Velaris 应用数据层的核心模块，负责实现 `foundation:model` 中定义的 Repository 接口。该模块遵循 Clean Architecture 原则，作为 data 层连接领域模型（domain model）与底层数据源（Room 数据库、Android MediaStore、内存种子数据）。

**包名：** `com.wujia.foundation.data`

**核心职责：**
- 提供 8 个资源领域的 Repository 实现（scene、noise、media、background、video、particle、theme、playback）
- 管理种子数据（Seed Data）的初始化与加载
- 将 data 层内部模型（`Local*`）转换为对外暴露的领域模型
- 通过 Hilt DI 模块将实现绑定到接口

---

## 2. 架构设计

### 2.1 整体分层

```
foundation:model (接口定义)
       |
       v
foundation:data (本模块 - 接口实现)
       |
       +--- foundation:database (Room DAO / Entity)
       +--- foundation:toolkit (IoDispatcher 等工具)
       +--- foundation:ui (R.string / R.array 资源)
       +--- Android MediaStore API
```

### 2.2 统一数据流模式

除 media 外的 5 个子领域均采用完全一致的四层模式：

```
Seed Data (种子数据定义)
    |
    v
LocalDataSource (资源ID -> 内部模型, 加载/查询/观察)
    |
    v
DefaultRepository (内部模型 -> 领域模型, 对外暴露)
    |
    v
Hilt DI Module (@Binds @Singleton 接口绑定)
```

**media 子领域**采用不同模式，直接委托给 `MediaStoreDataSource` 查询系统媒体库：

```
MediaStore API (ContentResolver)
    |
    v
MediaStoreDataSource (Cursor -> MediaItem)
    |
    v
MediaRepositoryImpl (委托模式)
    |
    v
MediaDataModule
```

### 2.3 关键设计模式

| 模式 | 说明 |
|------|------|
| **Internal Model** | 每个子领域定义 `internal data class Local*Resource`，隔离 Room Entity 与领域模型 |
| **Seed Data** | 使用 `@StringRes` / `@RawRes` / `@DrawableRes` 注解的种子模板类，支持多语言和运行时资源解析 |
| **Repository Delegation** | Repository 层仅做模型转换，全部数据操作委托给 LocalDataSource |
| **Composition** | `MediaRepositoryImpl` 通过组合（而非继承）`MediaStoreDataSource` 完成查询 |
| **Mutex Seed Guard** | `SceneResourceLocalDataSource` 使用 `Mutex` 保证首次启动时种子数据只写入一次 |

### 2.4 场景子域的特殊架构（Room-backed）

场景（scene）是唯一使用 Room 持久化的子域，其数据流最为复杂：

```
LocalSeedScene (种子模板)
    |  seedDatabaseIfEmpty() 首次启动写入
    v
Room Database (SceneEntity + SceneAudioEntity)
    |  DAO 查询 (SceneWithAudio)
    v
SceneResourceLocalDataSource
    |  SceneWithAudio -> LocalSceneResource (内部模型)
    |  LocalSceneResource -> SceneResource (领域模型)
    v
DefaultSceneResourceRepository
```

场景还支持 CRUD 操作：创建/编辑（`saveSceneEdit`，含 videoVolume + 完整控制设置）、删除（`deleteSceneResource`，预设场景不可删）、排序（`reorderSceneResources`）、控制设置更新（timerMode、guideCompleted、showCountdownClock、alarmReminderEnabled、clock position/volume、粒子、视频音量等）、音量调整（环境音 + 视频音量）。控制设置通过 `SceneControlSettings`（含 SceneTimerMode / SceneCountdownClockPosition 枚举）在领域模型与本地模型间传递。

---

## 3. 核心类与接口

### 3.1 子领域：scene（场景）-- 最复杂的子域，Room 持久化

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `SceneSeedData.kt` | 93 | internal | 种子数据：`LocalSeedScene`、`LocalSeedAudio`、`defaultLocalScenes()`，包含 4 个预设场景（morning-mist、snow-night、after-rain、under-moon） |
| `LocalSceneResource.kt` | ~67 | internal | 内部模型：`LocalSceneResource`（含背景、视频+videoVolume、音频轨道、完整 controlSettings）、`LocalSceneAudioResource`、`LocalSceneEditInput`（含 videoVolume + controlSettings） |
| `SceneResourceLocalDataSource.kt` | 260 | public | 本地数据源：依赖 `SceneDao` + `SceneAudioDao`，提供观察/查询/CRUD/排序/设置更新等方法，首次启动通过 `seedDatabaseIfEmpty()` 写入种子数据 |
| `DefaultSceneResourceRepository.kt` | 182 | public | Repository 实现：实现 `SceneResourceRepository` 接口，负责 `LocalSceneResource <-> SceneResource` 双向转换，包含资源名称到 drawable 资源 ID 的解析 |

**场景预设数据一览：**

| ID | 分类 | 视频 | 音频轨道 |
|----|------|------|----------|
| `morning-mist` | FOCUS | video2 | rain + thunder |
| `snow-night` | FOCUS | video1 | fireplace + wind |
| `after-rain` | SLEEP | video3 | wind |
| `under-moon` | SLEEP | (无) | (无，使用静态背景) |

### 3.2 子领域：media（媒体库）-- MediaStore 查询

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `MediaItem.kt` | 5 | public | 类型别名：将 `foundation:model.media` 中的 `MediaItem`、`MediaType`、`MediaSortOrder` 重新导出 |
| `MediaStoreDataSource.kt` | 295 | public | MediaStore 数据源：通过 `ContentResolver` 查询设备图片和视频，支持排序/分页/类型过滤，兼容 Android Q+ Scoped Storage，使用 Bundle queryArgs（API 26+）优化查询性能 |
| `MediaRepositoryImpl.kt` | 41 | public | Repository 实现：实现 `MediaRepository` 接口，纯粹委托模式，无模型转换 |

**MediaStoreDataSource 核心能力：**
- `queryImages()` -- 查询设备图片
- `queryVideos()` -- 查询设备视频
- `queryAllMedia()` -- 合并查询并统一排序
- `queryMedia(MediaType, ...)` -- 按类型查询
- 内部使用 `Bundle(QUERY_ARG_*)` 实现高效的 SQL 级 LIMIT/排序（API 26+），低版本回退到传统 `sortOrder` 字符串

### 3.3 子领域：particle（粒子效果）-- 最完整的非 Room 子域

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `ParticleSeedData.kt` | 147 | internal | 种子数据：`LocalSeedParticle`、`defaultLocalParticles()`，包含 8 种粒子效果 |
| `LocalParticleResource.kt` | 25 | internal | 内部模型：含 effect、intensity、wind、quality、foregroundGlassEnabled 等渲染参数 |
| `ParticleResourceLocalDataSource.kt` | 63 | public | 本地数据源：内存常驻，提供观察/查询/按分类过滤方法 |
| `DefaultParticleResourceRepository.kt` | 50 | public | Repository 实现：`LocalParticleResource -> ParticleResource` 转换 |

**粒子效果预设数据一览：**

| ID | 分类 | 效果 | intensity | wind | quality |
|----|------|------|-----------|------|---------|
| `light-rain` | RAIN | RAIN | 0.4 | 0.1 | LOW |
| `moderate-rain` | RAIN | RAIN | 0.72 | 0.3 | MEDIUM |
| `heavy-rain` | STORM | RAIN | 0.95 | 0.7 | HIGH |
| `light-snow` | SNOW | SNOW | 0.35 | 0.05 | LOW |
| `moderate-snow` | SNOW | SNOW | 0.65 | 0.15 | MEDIUM |
| `blizzard` | STORM | SNOW | 0.9 | 0.6 | HIGH |
| `no-particle` | CALM | NONE | 0 | 0 | LOW |
| `fireflies` | CALM | FIREFLIES | 0.72 | 0.1 | MEDIUM |

### 3.4 子领域：noise（白噪音/环境音）

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `NoiseSeedData.kt` | 70 | internal | 种子数据：`LocalSeedNoise`、`defaultLocalNoises()`，6 种环境音 |
| `LocalNoiseResource.kt` | 20 | internal | 内部模型：含 rawResId、tags、defaultVolume、loop |
| `NoiseResourceLocalDataSource.kt` | 37 | public | 本地数据源：内存常驻，提供观察/查询/按分类过滤方法 |
| `DefaultNoiseResourceRepository.kt` | 45 | public | Repository 实现：将 rawResId 转换为 `android.resource://` URI |

**噪音预设数据一览：** rain（NATURE）、fireplace（HEALING）、wind（NATURE）、ocean（SLEEP）、piano（FOCUS）、thunder（SLEEP）

### 3.5 子领域：background（背景图片）

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `BackgroundSeedData.kt` | 40 | internal | 种子数据：`LocalSeedBackground`、`defaultLocalBackgrounds()`，4 种背景 |
| `LocalBackgroundResource.kt` | 14 | internal | 内部模型：含 drawableResId |
| `BackgroundResourceLocalDataSource.kt` | 31 | public | 本地数据源：内存常驻 |
| `DefaultBackgroundResourceRepository.kt` | 37 | public | Repository 实现：将 drawableResId 转换为 `android.resource://` URI |

**背景预设数据一览：** morning_mist、snow_night、after_rain、under_moon

### 3.6 子领域：video（视频资源）

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `VideoSeedData.kt` | 34 | internal | 种子数据：`LocalSeedVideo`、`defaultLocalVideos()`，3 个视频 |
| `LocalVideoResource.kt` | 14 | internal | 内部模型：含 rawResId |
| `VideoResourceLocalDataSource.kt` | 29 | public | 本地数据源：内存常驻 |
| `DefaultVideoResourceRepository.kt` | 37 | public | Repository 实现：将 rawResId 转换为 `android.resource://` URI |

**视频预设数据一览：** video1、video2、video3

### 3.7 DI 模块（`di` 包）

| 类名 | 行数 | 说明 |
|------|------|------|
| `SceneDataModule.kt` | 20 | 绑定 `DefaultSceneResourceRepository -> SceneResourceRepository` |
| `MediaDataModule.kt` | 20 | 绑定 `MediaRepositoryImpl -> MediaRepository` |
| `NoiseDataModule.kt` | 20 | 绑定 `DefaultNoiseResourceRepository -> NoiseResourceRepository` |
| `ParticleDataModule.kt` | 25 | 绑定 `DefaultParticleResourceRepository -> ParticleResourceRepository` |
| `BackgroundDataModule.kt` | 20 | 绑定 `DefaultBackgroundResourceRepository -> BackgroundResourceRepository` |
| `VideoDataModule.kt` | 20 | 绑定 `DefaultVideoResourceRepository -> VideoResourceRepository` |
| `ThemeDataModule.kt` | 19 | 绑定 `DefaultThemeSettingsRepository -> ThemeSettingsRepository` |
| `PlaybackDataModule.kt` | 19 | 绑定 `DefaultPlaybackSettingsRepository -> PlaybackSettingsRepository` |

所有 DI 模块均使用 `@InstallIn(SingletonComponent::class)` + `@Singleton`，实现为 `internal interface` 并通过 `@Binds` 完成接口绑定。

### 3.8 子领域：settings（设置）-- SharedPreferences 持久化

| 类名 | 行数 | 可见性 | 说明 |
|------|------|--------|------|
| `DefaultThemeSettingsRepository.kt` | 35 | public | 主题设置仓储实现：通过 SharedPreferences 持久化主题预设，支持 Flow 观察 |
| `DefaultPlaybackSettingsRepository.kt` | 35 | public | 播放设置仓储实现：通过 SharedPreferences 持久化播放器性能配置，支持 Flow 观察 |

**设置仓储特点：**
- 使用 `MutableStateFlow` 保持内存缓存，确保响应式更新
- 通过 `SPUtils` 工具类进行 SharedPreferences 读写
- 提供 `loadPersistedPreset()` 静态方法，支持在初始化时加载持久化数据
- 错误处理：读取失败时返回默认值（`VelarisThemePreset.Ocean` / `VelarisPlayerPerformanceProfile.Balanced`）

---

## 4. 对外暴露的接口

本模块对外暴露的公共 API 完全由 `foundation:model` 模块定义。本模块仅提供实现，通过 Hilt DI 消费方无需直接引用具体实现类。

### 4.1 Repository 接口（定义在 `foundation:model`，实现在本模块）

| 接口 | 实现类 | 主要方法 |
|------|--------|----------|
| `SceneResourceRepository` | `DefaultSceneResourceRepository` | `observeSceneResources()`, `getSceneResource(id)`, `getEditableScene(id)`, `saveSceneEdit(input)`, `deleteSceneResource(id)`, `updateSceneControlSettings(...)`, `updateSceneAudioVolume(...)`, `reorderSceneResources(...)` |
| `MediaRepository` | `MediaRepositoryImpl` | `getImages(sortOrder, limit)`, `getVideos(sortOrder, limit)`, `getAllMedia(sortOrder, limit)`, `getMediaByType(mediaType, sortOrder, limit)` |
| `NoiseResourceRepository` | `DefaultNoiseResourceRepository` | `observeNoiseResources()`, `getNoiseResources()`, `getNoiseResource(id)`, `getNoiseResourcesByCategory(category)` |
| `ParticleResourceRepository` | `DefaultParticleResourceRepository` | `observeParticleResources()`, `getParticleResources()`, `getParticleResource(id)`, `getParticleResourcesByCategory(category)` |
| `BackgroundResourceRepository` | `DefaultBackgroundResourceRepository` | `observeBackgroundResources()`, `getBackgroundResources()`, `getBackgroundResource(id)` |
| `VideoResourceRepository` | `DefaultVideoResourceRepository` | `observeVideoResources()`, `getVideoResources()`, `getVideoResource(id)` |
| `ThemeSettingsRepository` | `DefaultThemeSettingsRepository` | `observeThemePreset()`, `getThemePreset()`, `updateThemePreset(preset)` |
| `PlaybackSettingsRepository` | `DefaultPlaybackSettingsRepository` | `observePerformanceProfile()`, `getPerformanceProfile()`, `updatePerformanceProfile(profile)` |

### 4.2 领域模型（定义在 `foundation:model`，由本模块产出）

- `SceneResource` -- 场景资源（含背景、视频、音频轨道、控制设置）
- `MediaItem` -- 设备媒体文件（含 URI、尺寸、时长、相册信息等）
- `NoiseResource` -- 环境音资源（含 URI、分类、标签）
- `ParticleResource` -- 粒子效果资源（含渲染参数）
- `BackgroundResource` -- 背景图片资源
- `VideoResource` -- 视频资源
- `VelarisThemePreset` -- 全局主题预设枚举
- `VelarisPlayerPerformanceProfile` -- 播放器性能配置枚举
- `EditableScene` -- 场景编辑输入模型

---

## 5. 依赖关系

### 5.1 模块依赖（`build.gradle.kts`）

```kotlin
dependencies {
    api(projects.foundation.model)          // 对外暴露 model 层接口和领域模型
    implementation(projects.foundation.database)  // Room DAO、Entity（scene 子域使用）
    implementation(projects.foundation.toolkit)   // IoDispatcher 等工具
    implementation(projects.foundation.ui)        // R.string、R.array 资源引用
    implementation(projects.foundation.player)   // PlaybackSettingsRepository 接口

    implementation(libs.kotlinx.datetime)         // Clock.System.now() 时间戳
    implementation(libs.kotlinx.coroutines.android) // withContext、Flow
    implementation(libs.timber)                   // 日志（media、scene 使用）

    testImplementation(libs.kotlinx.coroutines.test)
}
```

**依赖方向图：**

```
foundation:data
    |-- api --> foundation:model (接口定义，传递给上层)
    |-- impl -> foundation:database (Room)
    |-- impl -> foundation:toolkit (IoDispatcher)
    |-- impl -> foundation:ui (字符串/数组资源)
    |-- impl -> foundation:player (PlaybackSettingsRepository)
    |-- impl -> kotlinx.datetime
    |-- impl -> kotlinx.coroutines
    |-- impl -> timber
```

注意：`foundation:model` 使用 `api` 依赖，意味着消费 `foundation:data` 的模块自动获得 `foundation:model` 的传递依赖。

### 5.2 外部库依赖

| 库 | 用途 |
|----|------|
| `kotlinx.datetime` | `Clock.System.now()` 获取当前时间戳，用于 Room Entity 的 `createdAt`/`updatedAt` 字段 |
| `kotlinx.coroutines` | `Flow`、`withContext(ioDispatcher)`、`Mutex`、`flowOf` 等协程原语 |
| `timber` | `MediaStoreDataSource` 和 `DefaultSceneResourceRepository` 中的日志记录 |
| `dagger.hilt` | DI 注解：`@Inject`、`@Module`、`@Binds`、`@Singleton`、`@ApplicationContext`、`@IoDispatcher` |

### 5.3 Android 框架依赖

| API | 使用位置 | 说明 |
|-----|----------|------|
| `ContentResolver` + `MediaStore` | `MediaStoreDataSource` | 设备媒体库查询 |
| `Context.resources.getIdentifier()` | `DefaultSceneResourceRepository` | 将资源名称解析为 drawable 资源 ID |
| `Context.getString()` / `resources.getStringArray()` | 各 `LocalDataSource` | 种子数据的运行时资源解析 |

---

## 6. 种子数据详解

### 6.1 种子数据设计模式

每个子域的种子数据均采用两级结构：

```
LocalSeed* (资源ID模板)  --  使用 @StringRes / @RawRes / @DrawableRes 注解
    |
    |  LocalDataSource.init { asLocalModel() }
    v
Local*Resource (内部模型)  --  包含运行时解析后的实际字符串和资源ID
```

这种设计的优势：
- **多语言支持**：`@StringRes` 标注的 titleResId/descriptionResId 在运行时由 Android 资源系统解析
- **编译期检查**：`@RawRes`、`@DrawableRes`、`@ArrayRes` 注解确保资源引用有效
- **延迟解析**：资源字符串在 LocalDataSource 初始化时解析，而非编译时硬编码

### 6.2 种子数据汇总表

| 子域 | 种子函数 | 条目数 | 数据类 | 持久化方式 |
|------|----------|--------|--------|------------|
| scene | `defaultLocalScenes()` | 4 | `LocalSeedScene` | Room（首次启动写入） |
| noise | `defaultLocalNoises()` | 6 | `LocalSeedNoise` | 内存常驻 |
| particle | `defaultLocalParticles()` | 8 | `LocalSeedParticle` | 内存常驻 |
| background | `defaultLocalBackgrounds()` | 4 | `LocalSeedBackground` | 内存常驻 |
| video | `defaultLocalVideos()` | 3 | `LocalSeedVideo` | 内存常驻 |

### 6.3 资源依赖

种子数据引用的资源分布在两个模块中：

- **`foundation:ui`**（`com.wujia.foundation.ui.R`）：字符串资源（`R.string.seed_*`）、数组资源（`R.array.seed_*`）
- **`foundation:model`**（`com.wujia.foundation.model.R`）：原始资源（`R.raw.rain_1`、`R.raw.video1` 等）、drawable 资源（`R.drawable.morning_mist` 等）

---

## 7. 测试覆盖

### 7.1 测试文件

| 文件 | 行数 | 测试类 | 测试数量 |
|------|------|--------|----------|
| `SceneSeedDataTest.kt` | 106 | `SceneSeedDataTest` | 2 |
| | | `NoiseResourceLocalDataSourceTest` | 2 |
| | | `ParticleResourceLocalDataSourceTest` | 5 |

**共 3 个测试类，9 个测试方法。**

### 7.2 测试内容

- **SceneSeedDataTest**：验证 `defaultLocalScenes()` 返回 4 个场景、ID 唯一、首个场景包含 rain 音频轨道
- **NoiseResourceLocalDataSourceTest**：验证 `defaultLocalNoises()` 返回 6 个噪音、ID 唯一、可按 FOCUS 分类过滤
- **ParticleResourceLocalDataSourceTest**：验证 `defaultLocalParticles()` 返回 8 个粒子效果、ID 唯一、包含 3 种雨效和 3 种雪效、可按 STORM 分类过滤、包含 no-particle 选项、包含萤火虫效果

---

## 8. 当前缺陷与改进点

### 8.1 架构层面

1. **种子数据测试仅覆盖验证层**：当前测试仅验证种子数据的数量和 ID 唯一性，未测试 `LocalDataSource` 和 `DefaultRepository` 的实际行为（模型转换、URI 生成、CRUD 操作）。建议补充集成测试，尤其是 scene 子域的 Room 读写流程。

2. **media 子域缺少 observe 能力**：其他 5 个子域均提供 `observe*Resources(): Flow<List<*>>` 方法，而 `MediaRepository` 仅提供 suspend 查询方法，无法响应设备媒体库变化。可以考虑使用 `ContentObserver` 实现 Flow 观察。

3. **非 scene 子域的 observe 实际是伪观察**：noise、particle、background、video 的 `observe*Resources()` 方法返回 `flowOf(localXxx)`，本质上是静态数据的单次发射，不会触发后续更新。虽然对于内存常驻数据来说功能上没问题，但语义上容易误导调用方。

### 8.2 代码质量

4. **scene 种子数据 `LocalSeedScene` 和 `LocalSeedAudio` 未标记 `internal`**：其他子域的种子数据类均使用 `internal` 修饰，而 scene 子域的 `LocalSeedScene` 和 `LocalSeedAudio` 缺少 `internal` 关键字，会泄漏到模块外部。

5. ~~**日志工具使用不统一**~~：已解决。`android.util.Log` 已从所有 data 模块文件中移除，统一使用 Timber。

6. **`toAndroidResourceUri()` 重复定义**：`DefaultNoiseResourceRepository`、`DefaultVideoResourceRepository`、`DefaultBackgroundResourceRepository` 和 `SceneResourceLocalDataSource` 各自定义了相同的 `Int.toAndroidResourceUri()` 扩展函数，建议提取到公共工具类或 `foundation:toolkit`。

7. **`DefaultNoiseResourceRepository` 中 `context` 参数可能冗余**：该类注入了 `@ApplicationContext context` 和 `localDataSource`，但 `context` 仅用于 `toAndroidResourceUri()` 转换。如果将 URI 转换下沉到 LocalDataSource 层，Repository 可以移除 context 依赖。

### 8.3 功能缺失

8. **缺少搜索/模糊匹配能力**：noise 和 particle 种子数据包含 `tags` 字段，但 Repository 层未暴露按标签搜索的接口。

9. **background 和 video 子域功能单薄**：仅有基础的 get/observe 方法，不支持分类过滤或排序，与 noise/particle 相比较为简陋。

10. **scene 预设场景硬编码保护**：`deleteSceneResource()` 通过 `presetSceneIds` 集合阻止删除预设场景，但该集合在 companion object 中静态初始化。如果未来种子数据动态化，这种保护机制需要调整。

### 8.4 性能

11. **`queryAllMedia()` 双查询 + 内存排序**：`MediaStoreDataSource.queryAllMedia()` 分别查询图片和视频后在内存合并排序，当设备媒体文件极多时可能有性能问题。可以考虑使用 `UNION` 查询或限制各自查询的条目数。

12. **scene 种子写入的 Mutex 锁粒度**：`seedDatabaseIfEmpty()` 使用全局 `Mutex`，每次调用 `observeSceneResources()` 都会先获取锁检查数据库计数。建议增加一个 `AtomicBoolean` 标记位快速跳过已初始化的情况。

---

## 9. 代码统计

### 9.1 文件数量

| 分类 | 文件数 |
|------|--------|
| 主源码（main） | 33 |
| 测试源码（test） | 1 |
| 构建脚本（build.gradle.kts） | 1 |
| **合计** | **35** |

### 9.2 行数统计

| 子域/分类 | 文件数 | 行数 | 占比 |
|-----------|--------|------|------|
| scene（场景） | 4 | 596 | 32.5% |
| media（媒体库） | 3 | 341 | 18.6% |
| particle（粒子效果） | 4 | 285 | 15.5% |
| noise（环境音） | 4 | 172 | 9.4% |
| background（背景） | 4 | 122 | 6.6% |
| video（视频） | 4 | 114 | 6.2% |
| settings（设置） | 2 | 70 | 3.8% |
| di（DI 模块） | 8 | 163 | 8.9% |
| **主源码合计** | **33** | **1843** | **100%** |
| 测试源码 | 1 | 106 | -- |

### 9.3 各子域文件明细

**scene（596 行）：**
- `SceneSeedData.kt` -- 93 行
- `LocalSceneResource.kt` -- 61 行
- `SceneResourceLocalDataSource.kt` -- 260 行（本模块最大文件）
- `DefaultSceneResourceRepository.kt` -- 182 行

**media（341 行）：**
- `MediaItem.kt` -- 5 行（类型别名）
- `MediaStoreDataSource.kt` -- 295 行（第二大文件）
- `MediaRepositoryImpl.kt` -- 41 行

**particle（285 行）：**
- `ParticleSeedData.kt` -- 147 行
- `LocalParticleResource.kt` -- 25 行
- `ParticleResourceLocalDataSource.kt` -- 63 行
- `DefaultParticleResourceRepository.kt` -- 50 行

**noise（172 行）：**
- `NoiseSeedData.kt` -- 70 行
- `LocalNoiseResource.kt` -- 20 行
- `NoiseResourceLocalDataSource.kt` -- 37 行
- `DefaultNoiseResourceRepository.kt` -- 45 行

**background（122 行）：**
- `BackgroundSeedData.kt` -- 40 行
- `LocalBackgroundResource.kt` -- 14 行
- `BackgroundResourceLocalDataSource.kt` -- 31 行
- `DefaultBackgroundResourceRepository.kt` -- 37 行

**video（114 行）：**
- `VideoSeedData.kt` -- 34 行
- `LocalVideoResource.kt` -- 14 行
- `VideoResourceLocalDataSource.kt` -- 29 行
- `DefaultVideoResourceRepository.kt` -- 37 行

**di（163 行）：**
- 8 个 DI Module 文件，每个 19-25 行

**settings（70 行）：**
- `DefaultThemeSettingsRepository.kt` -- 35 行
- `DefaultPlaybackSettingsRepository.kt` -- 35 行

### 9.4 复杂度热点

| 排名 | 文件 | 行数 | 复杂度来源 |
|------|------|------|------------|
| 1 | `SceneResourceLocalDataSource.kt` | 260 | Room CRUD、种子写入、Mutex 同步、多 Entity 映射 |
| 2 | `MediaStoreDataSource.kt` | 295 | ContentResolver 查询、Cursor 映射、API 版本兼容 |
| 3 | `DefaultSceneResourceRepository.kt` | 182 | 多层模型转换（LocalSceneResource <-> SceneResource <-> EditableScene <-> SceneEditInput） |
| 4 | `ParticleSeedData.kt` | 147 | 8 种粒子效果的参数配置 |

---

## 10. 包结构总览

```
com.wujia.foundation.data/
    di/
        BackgroundDataModule.kt
        MediaDataModule.kt
        NoiseDataModule.kt
        ParticleDataModule.kt
        SceneDataModule.kt
        VideoDataModule.kt
        ThemeDataModule.kt
        PlaybackDataModule.kt
    scene/
        SceneSeedData.kt
        LocalSceneResource.kt
        SceneResourceLocalDataSource.kt
        DefaultSceneResourceRepository.kt
    media/
        MediaItem.kt
        MediaStoreDataSource.kt
        MediaRepositoryImpl.kt
    particle/
        ParticleSeedData.kt
        LocalParticleResource.kt
        ParticleResourceLocalDataSource.kt
        DefaultParticleResourceRepository.kt
    noise/
        NoiseSeedData.kt
        LocalNoiseResource.kt
        NoiseResourceLocalDataSource.kt
        DefaultNoiseResourceRepository.kt
    background/
        BackgroundSeedData.kt
        LocalBackgroundResource.kt
        BackgroundResourceLocalDataSource.kt
        DefaultBackgroundResourceRepository.kt
    video/
        VideoSeedData.kt
        LocalVideoResource.kt
        VideoResourceLocalDataSource.kt
        DefaultVideoResourceRepository.kt
    settings/
        DefaultThemeSettingsRepository.kt
        DefaultPlaybackSettingsRepository.kt
```
