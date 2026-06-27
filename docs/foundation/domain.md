# `foundation:domain` 模块文档

> 最后更新：2026-06-01

---

## 1. 模块概述

### 1.1 模块定位

`foundation:domain` 是 Velaris 项目 **Clean Architecture** 分层中的 **领域层（Domain Layer）** 模块。它处于 `foundation:model`（数据模型/仓库接口）之上、`feature:*`（UI/表现层）之下，负责将业务逻辑封装为独立的、可复用的 Use Case（用例）。

### 1.2 职责

- **封装业务逻辑**：每个 Use Case 对应一个原子化的业务操作，外部调用者无需了解数据来源（本地数据库、网络、缓存等）。
- **隔离层依赖**：表现层（ViewModel）通过 Use Case 访问数据，而不直接依赖 `Repository` 实现，从而实现依赖倒置。
- **提供响应式数据流**：部分 Use Case 返回 `kotlinx.coroutines.flow.Flow`，支持数据的持续观察。

### 1.3 命名空间

| 属性       | 值                            |
|------------|-------------------------------|
| 模块路径   | `foundation:domain`           |
| 包名       | `com.wujia.foundation.domain` |
| 构建插件   | `advance.android.library`、`advance.android.library.compose`、`advance.android.library.jacoco` |

---

## 2. 架构设计

### 2.1 整体架构

本模块严格遵循 **Clean Architecture** 的领域层设计原则：

```
[feature:*:impl]  (表现层 / ViewModel)
        |
        v
[foundation:domain]  (领域层 / UseCase)  <-- 本模块
        |
        v
[foundation:model]   (模型层 / Repository 接口 + 数据模型)
```

### 2.2 设计模式

#### 2.2.1 UseCase 模式

每个业务操作被封装为一个独立的类（Use Case），遵循以下约定：

- **单一职责**：每个 Use Case 类只负责一个业务操作。
- **构造函数注入**：通过 `@Inject constructor` 注入 Repository 依赖，配合 Hilt DI 框架使用。
- **`operator fun invoke`**：通过 Kotlin 的操作符重载，使 Use Case 可以像函数一样被直接调用。

```kotlin
// 典型 UseCase 结构
class GetEditableSceneUseCase @Inject constructor(
    private val sceneResourceRepository: SceneResourceRepository,
) {
    suspend operator fun invoke(id: String): EditableScene? =
        sceneResourceRepository.getEditableScene(id)
}

// 调用方使用方式
val scene = getEditableSceneUseCase(id = "scene-id")
```

#### 2.2.2 响应式数据流（Flow）

需要持续观察数据变化的 Use Case 返回 `Flow<T>`（非挂起函数），例如：

```kotlin
class ObserveSceneResourcesUseCase @Inject constructor(
    private val sceneResourceRepository: SceneResourceRepository,
) {
    operator fun invoke() =
        sceneResourceRepository.observeSceneResources()
            .catch {
                emit(emptyList())
            }
}
```

#### 2.2.3 错误处理策略

- **`ObserveSceneResourcesUseCase`**：在 Flow 中通过 `.catch` 捕获异常，降级为空列表，保证 UI 层不会因数据源异常而崩溃。
- **`ReorderSceneResourcesUseCase`**：在执行前进行前置校验（检查 `orderedIds` 是否有重复），不满足条件时静默返回。
- 其余 Use Case 未显式处理异常，异常将向上传播给调用方。

### 2.3 关键设计决策

1. **薄 UseCase 层**：本模块的大多数 Use Case 是对 Repository 方法的直接转发，未添加额外业务逻辑。这是有意为之——当业务逻辑简单时，保持薄包装可以避免过度工程化；当未来需要添加校验、缓存、组合等逻辑时，已有扩展点。
2. **同一 Repository 的多个 UseCase**：场景（scene）域拥有 8 个独立 UseCase，每个对应 Repository 上的一个方法，保持了操作粒度的清晰。
3. **返回值差异**：部分 UseCase 挂起函数返回 `Unit`（写操作），部分返回数据（读操作），保持语义一致性。

---

## 3. 代码能力

| 能力域     | 功能                  | 数据类型                  | 响应方式    | 说明                             |
|------------|-----------------------|---------------------------|-------------|----------------------------------|
| 场景(编辑) | 获取可编辑场景        | `EditableScene?`          | `suspend`   | 根据 ID 获取场景编辑数据         |
| 场景(编辑) | 保存场景编辑          | `String` (返回 ID)        | `suspend`   | 保存场景编辑并返回场景 ID        |
| 场景(资源) | 观察场景资源列表      | `Flow<List<SceneResource>>` | `Flow`    | 持续观察所有场景资源变化         |
| 场景(资源) | 获取单个场景资源      | `SceneResource?`          | `suspend`   | 根据 ID 获取指定场景资源         |
| 场景(资源) | 删除场景资源          | `Unit`                    | `suspend`   | 根据 ID 删除场景资源             |
| 场景(资源) | 重排序场景资源        | `Unit`                    | `suspend`   | 对指定分类下的场景资源重新排序   |
| 场景(设置) | 更新场景控制设置      | `Unit`                    | `suspend`   | 更新场景的控制面板设置           |
| 场景(音频) | 更新场景音频音量      | `Unit`                    | `suspend`   | 更新场景中指定音频的音量         |
| 场景(视频) | 更新场景视频音量      | `Unit`                    | `suspend`   | 更新场景视频的音量               |
| 媒体(图片) | 获取媒体图片列表      | `List<MediaItem>`         | `suspend`   | 支持排序方式和数量限制           |
| 媒体(视频) | 获取媒体视频列表      | `List<MediaItem>`         | `suspend`   | 支持排序方式和数量限制           |
| 媒体(音频) | 获取媒体音频列表      | `List<MediaItem>`         | `suspend`   | 支持排序方式、数量限制和偏移量   |
| 背景       | 观察背景资源列表      | `Flow<List<BackgroundResource>>` | `Flow` | 持续观察背景资源变化        |
| 视频(资源) | 观察视频资源列表      | `Flow<List<VideoResource>>` | `Flow`  | 持续观察视频资源变化           |
| 噪音       | 观察噪音资源列表      | `Flow<List<NoiseResource>>` | `Flow`  | 持续观察噪音资源变化           |
| 粒子       | 观察粒子资源列表      | `Flow<List<ParticleResource>>` | `Flow` | 持续观察粒子资源变化         |
| 粒子       | 获取所有粒子资源      | `List<ParticleResource>`  | `suspend`   | 一次性获取全部粒子资源           |
| 粒子       | 根据 ID 获取粒子资源  | `ParticleResource?`       | `suspend`   | 根据 ID 获取指定粒子资源         |
| 粒子       | 根据分类获取粒子资源  | `List<ParticleResource>`  | `suspend`   | 按分类筛选粒子资源               |

---

## 4. 对外暴露的接口

### 4.1 场景（scene）包

#### `GetEditableSceneUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/GetEditableSceneUseCase.kt`
- **行号**：第 7-12 行

| 方法签名                                             | 行号 | 说明             |
|------------------------------------------------------|------|------------------|
| `suspend operator fun invoke(id: String): EditableScene?` | 10-11 | 根据 ID 获取可编辑场景 |

---

#### `SaveSceneEditUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/SaveSceneEditUseCase.kt`
- **行号**：第 7-12 行

| 方法签名                                           | 行号 | 说明                 |
|----------------------------------------------------|------|----------------------|
| `suspend operator fun invoke(input: SceneEditInput): String` | 10-11 | 保存场景编辑，返回场景 ID |

---

#### `ObserveSceneResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/ObserveSceneResourcesUseCase.kt`
- **行号**：第 20-29 行

| 方法签名                           | 行号 | 说明                              |
|------------------------------------|------|-----------------------------------|
| `operator fun invoke(): Flow<List<SceneResource>>` | 23-28 | 观察所有场景资源，含 catch 降级逻辑 |

---

#### `GetSceneResourceUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/GetSceneResourceUseCase.kt`
- **行号**：第 14-19 行

| 方法签名                                           | 行号 | 说明               |
|----------------------------------------------------|------|--------------------|
| `suspend operator fun invoke(id: String): SceneResource?` | 17-18 | 根据 ID 获取场景资源 |

---

#### `DeleteSceneResourceUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/DeleteSceneResourceUseCase.kt`
- **行号**：第 6-12 行

| 方法签名                        | 行号 | 说明               |
|---------------------------------|------|--------------------|
| `suspend operator fun invoke(id: String)` | 9-11 | 根据 ID 删除场景资源 |

---

#### `ReorderSceneResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/ReorderSceneResourcesUseCase.kt`
- **行号**：第 7-20 行

| 方法签名                                                         | 行号 | 说明                                       |
|------------------------------------------------------------------|------|--------------------------------------------|
| `suspend operator fun invoke(category: SceneCategory?, orderedIds: List<String>)` | 10-18 | 重排序场景资源，含重复 ID 校验 |

---

#### `UpdateSceneControlSettingsUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/UpdateSceneControlSettingsUseCase.kt`
- **行号**：第 7-14 行

| 方法签名                                                                       | 行号 | 说明                   |
|--------------------------------------------------------------------------------|------|------------------------|
| `suspend operator fun invoke(sceneId: String, settings: SceneControlSettings)` | 10-13 | 更新场景控制面板设置   |

---

#### `UpdateSceneAudioVolumeUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/UpdateSceneAudioVolumeUseCase.kt`
- **行号**：第 6-14 行

| 方法签名                                                                                | 行号 | 说明                   |
|-----------------------------------------------------------------------------------------|------|------------------------|
| `suspend operator fun invoke(sceneId: String, audioId: String, volume: Float)` | 9-13 | 更新场景指定音频的音量 |

---

#### `UpdateSceneVideoVolumeUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/scene/UpdateSceneVideoVolumeUseCase.kt`
- **行号**：第 6-13 行

| 方法签名                                                                                | 行号 | 说明                   |
|-----------------------------------------------------------------------------------------|------|------------------------|
| `suspend operator fun invoke(sceneId: String, volume: Float)` | 9-12 | 更新场景视频的音量 |

---

### 4.2 媒体（media）包

#### `GetMediaImagesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/media/GetMediaImagesUseCase.kt`
- **行号**：第 8-16 行

| 方法签名                                                                                      | 行号 | 说明             |
|-----------------------------------------------------------------------------------------------|------|------------------|
| `suspend operator fun invoke(sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC, limit: Int? = null): List<MediaItem>` | 11-15 | 获取媒体图片列表，支持排序与限制数量 |

---

#### `GetMediaVideosUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/media/GetMediaVideosUseCase.kt`
- **行号**：第 8-16 行

| 方法签名                                                                                      | 行号 | 说明             |
|-----------------------------------------------------------------------------------------------|------|------------------|
| `suspend operator fun invoke(sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC, limit: Int? = null): List<MediaItem>` | 11-15 | 获取媒体视频列表，支持排序与限制数量 |

---

#### `GetMediaAudiosUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/media/GetMediaAudiosUseCase.kt`
- **行号**：第 8-17 行

| 方法签名                                                                                      | 行号 | 说明             |
|-----------------------------------------------------------------------------------------------|------|------------------|
| `suspend operator fun invoke(sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC, limit: Int? = null, offset: Int? = null): List<MediaItem>` | 11-16 | 获取媒体音频列表，支持排序、限制数量和偏移量 |

---

### 4.3 背景（background）包

#### `GetBackgroundResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/background/GetBackgroundResourcesUseCase.kt`
- **行号**：第 8-13 行

| 方法签名                                            | 行号 | 说明                   |
|-----------------------------------------------------|------|------------------------|
| `operator fun invoke(): Flow<List<BackgroundResource>>` | 11-12 | 观察所有背景资源       |

---

### 4.4 视频资源（video）包

#### `GetVideoResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/video/GetVideoResourcesUseCase.kt`
- **行号**：第 8-13 行

| 方法签名                                          | 行号 | 说明                   |
|---------------------------------------------------|------|------------------------|
| `operator fun invoke(): Flow<List<VideoResource>>` | 11-12 | 观察所有视频资源       |

---

### 4.5 噪音（noise）包

#### `GetNoiseResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/noise/GetNoiseResourcesUseCase.kt`
- **行号**：第 8-13 行

| 方法签名                                          | 行号 | 说明                   |
|---------------------------------------------------|------|------------------------|
| `operator fun invoke(): Flow<List<NoiseResource>>` | 11-12 | 观察所有噪音资源       |

---

### 4.6 粒子（particle）包

#### `GetParticleResourcesUseCase`
- **文件**：`foundation/domain/src/main/java/com/wujia/foundation/domain/particle/GetParticleResourcesUseCase.kt`
- **行号**：第 14-40 行

| 方法签名                                                                         | 行号 | 说明               |
|----------------------------------------------------------------------------------|------|--------------------|
| `operator fun invoke(): Flow<List<ParticleResource>>`                            | 20-21 | 观察所有粒子资源   |
| `suspend fun getAll(): List<ParticleResource>`                                   | 26-27 | 一次性获取全部粒子资源 |
| `suspend fun getById(id: String): ParticleResource?`                             | 32-33 | 根据 ID 获取粒子资源 |
| `suspend fun getByCategory(category: ParticleCategory): List<ParticleResource>`  | 38-39 | 根据分类获取粒子资源 |

> 注：`GetParticleResourcesUseCase` 是本模块中唯一提供多个显式公开方法的 UseCase，其余 UseCase 均仅通过 `operator fun invoke` 暴露单一入口。

---

## 5. 依赖关系

### 5.1 本模块依赖

| 依赖项                    | 类型             | 说明                                     |
|---------------------------|------------------|------------------------------------------|
| `projects.foundation.model` | `implementation` | 提供 Repository 接口和数据模型定义       |
| `libs.javax.inject`       | `implementation` | 提供 `@Inject` 注解，用于依赖注入        |
| `libs.kotlinx.coroutines.core` | `api`       | 提供 `Flow`、`suspend` 等协程基础设施；以 `api` 形式暴露给下游消费方 |

**测试依赖：**

| 依赖项                    | 类型               | 说明                                   |
|---------------------------|--------------------|----------------------------------------|
| `projects.foundation.testing` | `testImplementation` | 提供 `FakeSceneResourceRepository` 等测试替身 |

### 5.2 被谁依赖（下游消费方）

| 消费方模块                  | 依赖类型       | 用途                           |
|-----------------------------|----------------|--------------------------------|
| `feature:scene:impl`        | `implementation` | 场景主页面（列表/详情）的业务逻辑 |
| `feature:sceneList:impl`    | `implementation` | 场景列表功能的业务逻辑          |
| `feature:sceneEdit:impl`    | `implementation` | 场景编辑功能的业务逻辑          |
| `feature:sceneControl:impl` | `implementation` | 场景控制面板（音量/设置）的业务逻辑 |
| `app`                       | `implementation` | 应用主模块，可能用于 Hilt 组件组装 |

### 5.3 依赖关系图

```
                       ┌───────────────────────────────────────────────────┐
                       │               foundation:domain                  │
                       │        (UseCase / 领域层 / 本模块)               │
                       └──────────┬────────────────────┬──────────────────┘
                                  │                    │
                    implementation│                    │api
                                  v                    v
                       ┌─────────────────┐   ┌──────────────────────┐
                       │foundation:model │   │kotlinx.coroutines    │
                       │(Repository+模型)│   │      .core           │
                       └─────────────────┘   └──────────────────────┘

              ┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  ┌─────┐
              │scene:impl│  │sceneList:impl│  │sceneEdit:impl│  │sceneControl:impl│  │ app │
              └────┬─────┘  └──────┬───────┘  └──────┬───────┘  └───────┬─────────┘  └──┬──┘
                   │               │                 │                  │                │
                   └───────────────┴─────────────────┴──────────────────┘────────────────┘
                                           │
                                           v
                               ┌──────────────────────┐
                               │   foundation:domain   │
                               └──────────────────────┘
```

---

## 6. 当前缺陷和改进建议

### 6.1 代码异味

#### (1) 大部分 UseCase 仅为透传，缺乏业务逻辑价值

除 `ReorderSceneResourcesUseCase`（含重复校验）和 `ObserveSceneResourcesUseCase`（含异常降级）外，其余 UseCase 均为对 Repository 方法的直接转发，没有附加任何业务逻辑。这使得 UseCase 层的存在意义仅为"依赖隔离"，增加了维护成本但收益有限。

**建议**：如果短期内没有在 UseCase 层增加业务逻辑的计划，可以考虑在各 `feature` 模块中直接注入 Repository 接口（已通过 `foundation:model` 模块暴露），省去 domain 模块的中间层开销。或者，明确记录哪些 UseCase 未来预期会增加复杂逻辑，作为保留此层的依据。

#### (2) `GetParticleResourcesUseCase` 风格不一致

该 UseCase 暴露了 4 个公开方法（`invoke`、`getAll`、`getById`、`getByCategory`），而其余所有 UseCase 均严格遵循单一 `operator fun invoke` 入口。这种不一致性破坏了 API 的可预测性。

**建议**：将 `getById`、`getByCategory` 拆分为独立的 UseCase 类（如 `GetParticleResourceByIdUseCase`、`GetParticleResourcesByCategoryUseCase`），与整体风格保持一致；或者为其他资源域也提供类似的多查询能力（如果确实有此需求）。

#### (3) 异常处理策略不一致

- `ObserveSceneResourcesUseCase` 对 Flow 异常进行了 catch 并降级为空列表。
- `GetBackgroundResourcesUseCase`、`GetVideoResourcesUseCase`、`GetNoiseResourcesUseCase`、`GetParticleResourcesUseCase` 同样返回 Flow，但未添加 catch 逻辑。
- 所有 `suspend` UseCase 均未处理异常。

**建议**：统一异常处理策略。要么所有 Flow UseCase 均添加 catch 降级逻辑，要么均不添加（由上层统一处理），避免部分有、部分没有的混淆情况。

#### (4) 缺少 KDoc 注释

14 个 UseCase 源文件中，仅有 3 个包含 KDoc 注释：
- `ObserveSceneResourcesUseCase`（完整注释）
- `GetSceneResourceUseCase`（完整注释）
- `GetParticleResourcesUseCase`（完整注释）

其余 11 个文件缺少类级别的文档注释，不利于新开发者理解用例意图。

#### (5) ~~`android.util.Log` 直接使用~~

已解决。`ObserveSceneResourcesUseCase` 中的 `android.util.Log` 已移除，领域层不再依赖 Android 日志框架。

### 6.2 架构问题

#### (1) 测试覆盖不足

当前仅有 1 个测试文件（`SceneResourceUseCaseTest.kt`，3 个测试用例），覆盖了 `ObserveSceneResourcesUseCase` 和 `GetSceneResourceUseCase`。其余 12 个 UseCase 完全没有单元测试。

**建议**：为所有 UseCase 补充单元测试，特别是：
- `ReorderSceneResourcesUseCase` 的重复 ID 校验逻辑
- `ObserveSceneResourcesUseCase` 的 Flow catch 降级逻辑
- `GetParticleResourcesUseCase` 的 4 个方法各自的边界情况

#### (2) 测试替身仅覆盖场景域

`foundation:testing` 模块目前仅提供了 `FakeSceneResourceRepository`，缺少 `FakeMediaRepository`、`FakeBackgroundResourceRepository` 等测试替身，导致其他域的 UseCase 无法编写单元测试。

**建议**：在 `foundation:testing` 模块中为所有 Repository 接口补充 Fake 实现。

#### (3) 无返回值的写操作缺少结果反馈

`DeleteSceneResourceUseCase`、`UpdateSceneControlSettingsUseCase`、`UpdateSceneAudioVolumeUseCase`、`ReorderSceneResourcesUseCase` 的 `invoke` 方法返回 `Unit`。调用方无法得知操作是否成功（除非捕获异常）。

**建议**：考虑引入 `Result<T>` 包装返回值，或定义领域层的业务异常类型，让调用方能够区分"操作成功"与"操作失败但未崩溃"的情况。

---

## 7. 代码统计

### 7.1 文件概览

| 统计项             | 数值 |
|--------------------|------|
| 源文件总数（含 Manifest） | 18   |
| Kotlin 源文件（main）    | 16   |
| Kotlin 测试文件          | 1    |
| AndroidManifest.xml      | 1    |
| 代码总行数               | 318  |

### 7.2 按包分布

| 包名                     | 文件数 | 行数 | UseCase 数量 |
|--------------------------|--------|------|--------------|
| `scene`                  | 9      | 155  | 9            |
| `media`                  | 3      | 49   | 3            |
| `background`             | 1      | 13   | 1            |
| `video`                  | 1      | 13   | 1            |
| `noise`                  | 1      | 13   | 1            |
| `particle`               | 1      | 40   | 1            |
| 测试 (`domain` 包)       | 1      | 45   | --           |
| **合计**                 | **17** | **318** | **16**   |

### 7.3 各文件行数明细

| 文件                                         | 行数 |
|----------------------------------------------|------|
| `scene/GetEditableSceneUseCase.kt`           | 12   |
| `scene/SaveSceneEditUseCase.kt`              | 12   |
| `scene/ReorderSceneResourcesUseCase.kt`      | 20   |
| `scene/DeleteSceneResourceUseCase.kt`        | 12   |
| `scene/ObserveSceneResourcesUseCase.kt`      | 29   |
| `scene/GetSceneResourceUseCase.kt`           | 19   |
| `scene/UpdateSceneControlSettingsUseCase.kt` | 14   |
| `scene/UpdateSceneAudioVolumeUseCase.kt`     | 14   |
| `scene/UpdateSceneVideoVolumeUseCase.kt`     | 13   |
| `media/GetMediaImagesUseCase.kt`             | 16   |
| `media/GetMediaVideosUseCase.kt`             | 16   |
| `media/GetMediaAudiosUseCase.kt`             | 17   |
| `background/GetBackgroundResourcesUseCase.kt` | 13  |
| `video/GetVideoResourcesUseCase.kt`          | 13   |
| `noise/GetNoiseResourcesUseCase.kt`          | 13   |
| `particle/GetParticleResourcesUseCase.kt`    | 40   |
| 测试 `SceneResourceUseCaseTest.kt`           | 45   |
| `AndroidManifest.xml`                        | 2    |

### 7.4 关键指标

| 指标                              | 数值 |
|-----------------------------------|------|
| UseCase 类总数                    | 16   |
| 平均每 UseCase 行数（main）       | ~17.5 行 |
| 最大单文件行数                    | 45 行（测试文件） |
| 最小单文件行数                    | 12 行（多个文件） |
| 测试覆盖的 UseCase 数量          | 2 / 16 (12.5%) |
| 测试用例总数                      | 3    |
| 使用 KDoc 注释的 UseCase 数量    | 3 / 16 (18.8%) |
