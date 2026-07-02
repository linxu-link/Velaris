# foundation/testing 模块文档

## 1. 模块概述

### 1.1 模块定位与职责

`foundation/testing` 是 Velaris 项目的**共享测试基础设施模块**。该模块以 Android Library（AAR）的形式发布，专门为其他模块的单元测试提供可复用的测试替身（Test Doubles）和测试工具。

该模块遵循以下设计原则：

- **集中管理**：将各模块通用的 Fake 实现和测试规则集中在一个模块中，避免各 feature/foundation 模块重复编写相同代码。
- **API 传递依赖**：通过 `api` 依赖传递 `kotlinx.coroutines.test` 和 `junit`，消费方无需自行声明这两个测试核心库。
- **仅服务于测试**：虽然该模块自身以 `main` source set 发布（非 `testImplementation` 形式的源码集），但其所有类均设计为仅在测试场景中使用。

### 1.2 命名空间

| 属性     | 值                          |
|----------|-----------------------------|
| 包名     | `com.wujia.foundation.testing` |
| Gradle 路径 | `:foundation:testing`      |
| Android Plugin | `advance.android.library`  |

---

## 2. 架构设计

### 2.1 共享测试基础设施设计

本模块采用"测试基础设施即库"的模式，将 Fake 实现和 JUnit Rule 打包为可被多个模块 `testImplementation` 引用的 Android Library。

```
foundation/testing (AAR)
  |-- FakeSceneResourceRepository   (Fake Repository)
  |-- MainDispatcherRule            (JUnit Rule)
       |
       |-- api: kotlinx.coroutines.test
       |-- api: junit
       |-- implementation: foundation.model
```

这种设计确保了：
- 各 feature 模块在测试中对 `SceneResourceRepository` 的模拟行为一致。
- 协程调度器的测试替换逻辑不会在每个测试文件中重复。
- 当 `SceneResourceRepository` 接口签名变更时，只需更新一处 Fake 实现。

### 2.2 Fake Repository 模式

模块中的 `FakeSceneResourceRepository` 是对 Repository Pattern 的测试替身实现。它遵循以下模式：

1. **构造注入预设数据**：通过构造参数 `initialScenes`、`editableScene`、`savedId`、`saveFailure` 提供可配置的初始状态。
2. **MutableStateFlow 驱动**：内部使用 `MutableStateFlow` 存储场景列表，支持通过 `updateScenes()` 方法在测试过程中动态修改数据并触发 Flow 重新发射。
3. **行为记录**：通过 `savedInput`、`deletedIds`、`reorderCategory`、`reorderedIds` 等属性记录方法调用的入参，供测试断言使用。
4. **异常模拟**：通过 `saveFailure` 参数可让 `saveSceneEdit()` 抛出指定异常，用于测试错误处理路径。

### 2.3 Test Rule 设计

`MainDispatcherRule` 是一个 JUnit 4 的 `TestWatcher`，用于在测试生命周期中替换 `Dispatchers.Main`：

- **starting**：测试开始前调用 `Dispatchers.setMain(testDispatcher)` 将主线程调度器替换为测试调度器。
- **finished**：测试结束后调用 `Dispatchers.resetMain()` 恢复原始调度器。

默认使用 `StandardTestDispatcher()`，也允许调用方注入自定义的 `TestDispatcher`（如 `UnconfinedTestDispatcher`）以控制协程执行策略。

---

## 3. 代码能力

该模块提供的功能列表如下：

| 能力                       | 类/组件                   | 说明                                                         |
|----------------------------|---------------------------|--------------------------------------------------------------|
| 场景资源 Repository 测试替身 | `FakeSceneResourceRepository` | 实现 `SceneResourceRepository` 全部 8 个方法，支持数据可观察、行为记录、异常模拟 |
| 动态更新场景数据            | `FakeSceneResourceRepository.updateScenes()` | 运行时更新 Flow 发射的数据，模拟数据变更场景       |
| 保存操作输入记录            | `FakeSceneResourceRepository.savedInput`     | 记录最近一次 `saveSceneEdit` 的入参                     |
| 删除操作记录                | `FakeSceneResourceRepository.deletedIds`     | 记录所有被调用 `deleteSceneResource` 的 ID              |
| 排序操作记录                | `FakeSceneResourceRepository.reorderCategory` / `reorderedIds` | 记录最近一次排序操作的分类和 ID 列表 |
| 保存失败模拟                | `FakeSceneResourceRepository(saveFailure = ...)` | 通过构造参数注入异常，使保存操作抛出指定异常     |
| 主线程调度器替换            | `MainDispatcherRule`         | 自动在测试生命周期内替换 `Dispatchers.Main`               |
| 协程测试依赖传递            | `api(kotlinx.coroutines.test)` | 消费方无需自行声明 `kotlinx.coroutines.test`          |
| JUnit 依赖传递             | `api(junit)`                | 消费方无需自行声明 `junit`                                |

---

## 4. 对外暴露的接口

### 4.1 FakeSceneResourceRepository

**文件路径**：`foundation/testing/src/main/java/com/wujia/foundation/testing/FakeSceneResourceRepository.kt`（第 21-84 行）

**实现接口**：`com.wujia.foundation.model.scene.SceneResourceRepository`

#### 构造参数

| 参数名            | 类型                | 默认值                | 说明                                     |
|-------------------|---------------------|-----------------------|------------------------------------------|
| `initialScenes`   | `List<SceneResource>` | `emptyList()`         | 用于 `observeSceneResources()` 和 `getSceneResource()` 的初始数据 |
| `editableScene`   | `EditableScene?`     | `null`                | `getEditableScene()` 返回的可编辑场景     |
| `savedId`         | `String`             | `"saved-scene"`       | `saveSceneEdit()` 成功时返回的 ID         |
| `saveFailure`     | `Throwable?`         | `null`                | 若非 null，`saveSceneEdit()` 会抛出此异常 |

#### 可配置行为（公开属性）

| 属性名            | 类型                  | 可写性   | 说明                                         |
|-------------------|-----------------------|----------|----------------------------------------------|
| `savedInput`      | `SceneEditInput?`     | 只读（private set） | 记录 `saveSceneEdit` 最近一次接收的输入       |
| `deletedIds`      | `MutableList<String>` | 可读写   | 记录 `deleteSceneResource` 被调用时的所有 ID  |
| `reorderCategory` | `SceneCategory?`      | 只读（private set） | 记录 `reorderSceneResources` 最近一次的 category |
| `reorderedIds`    | `List<String>`        | 只读（private set） | 记录 `reorderSceneResources` 最近一次的 orderedIds |

#### 公开方法

| 方法签名                                                                    | 说明                         |
|-----------------------------------------------------------------------------|------------------------------|
| `fun updateScenes(newScenes: List<SceneResource>)`                         | 运行时更新内部场景列表（触发 Flow 重新发射） |
| `override fun observeSceneResources(): Flow<List<SceneResource>>`          | 返回内部 `MutableStateFlow`   |
| `override suspend fun getSceneResource(id: String): SceneResource?`        | 从内部列表按 ID 查找          |
| `override suspend fun getEditableScene(id: String): EditableScene?`        | 返回构造时注入的 `editableScene` |
| `override suspend fun saveSceneEdit(input: SceneEditInput): String`        | 记录输入并返回 `savedId`，或抛出 `saveFailure` |
| `override suspend fun deleteSceneResource(id: String)`                     | 将 ID 追加到 `deletedIds`     |
| `override suspend fun updateSceneControlSettings(sceneId, settings)`       | 空实现（no-op）              |
| `override suspend fun updateSceneAudioVolume(sceneId, audioId, volume)`    | 空实现（no-op）              |
| `override suspend fun reorderSceneResources(category, orderedIds)`         | 记录 category 和 orderedIds   |

---

### 4.2 MainDispatcherRule

**文件路径**：`foundation/testing/src/main/java/com/wujia/foundation/testing/MainDispatcherRule.kt`（第 13-23 行）

**继承关系**：`TestWatcher`（JUnit 4）

#### 构造参数

| 参数名            | 类型             | 默认值                     | 说明                         |
|-------------------|------------------|----------------------------|------------------------------|
| `testDispatcher`  | `TestDispatcher` | `StandardTestDispatcher()` | 替换 `Dispatchers.Main` 使用的测试调度器 |

#### 生命周期钩子

| 方法名       | 触发时机     | 行为                            |
|--------------|-------------|---------------------------------|
| `starting()` | 测试开始前   | `Dispatchers.setMain(testDispatcher)` |
| `finished()` | 测试结束后   | `Dispatchers.resetMain()`       |

#### 使用示例

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun myTest() = runTest {
        // Dispatchers.Main 已被替换为 StandardTestDispatcher
        val viewModel = MyViewModel()
        // ...
    }
}
```

---

### 4.3 各模块中定义的本地 Fake 类

以下 Fake 类定义在各自模块的 `test` source set 中，属于 `private` 可见性，仅供所在测试文件内部使用，不属于 `foundation/testing` 模块的公开 API。

#### 4.3.1 feature/sceneControl/impl

| 类名                    | 文件路径                                                                                                                               | 行号     | 实现接口             | 说明                                     |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------|----------|----------------------|------------------------------------------|
| `FakeSceneControlClock` | `feature/sceneControl/impl/src/test/java/com/wujia/feature/scenecontrol/impl/ui/SceneControlViewModelTest.kt` | 第 358 行 | `SceneControlClock`  | 可配置当前时间戳的时钟替身                |

```kotlin
private class FakeSceneControlClock(
    var currentTimeMillis: Long = 0L,
) : SceneControlClock() {
    override fun currentTimeMillis(): Long = currentTimeMillis
}
```

#### 4.3.2 feature/sceneEdit/impl

文件路径：`feature/sceneEdit/impl/src/test/java/com/wujia/feature/sceneedit/impl/ui/SceneEditViewModelTest.kt`

| 类名                           | 行号     | 实现接口                    | 说明                                                        |
|--------------------------------|----------|-----------------------------|-------------------------------------------------------------|
| `FakeMediaRepository`          | 第 363 行 | `MediaRepository`           | 返回空列表的媒体资源替身                                     |
| `FakeBackgroundResourceRepository` | 第 386 行 | `BackgroundResourceRepository` | 内置 2 个预设背景资源（"雾隐山居"、"月下静湖"）               |
| `FakeVideoResourceRepository`  | 第 410 行 | `VideoResourceRepository`   | 内置 1 个预设视频资源（"风雪夜归人"）                         |
| `FakeNoiseResourceRepository`  | 第 428 行 | `NoiseResourceRepository`   | 内置 6 个预设噪音资源，覆盖 NATURE、HEALING、SLEEP、FOCUS 四个分类 |

```kotlin
private class FakeMediaRepository : MediaRepository {
    override suspend fun getImages(sortOrder: MediaSortOrder, limit: Int?): List<MediaItem> = emptyList()
    override suspend fun getVideos(sortOrder: MediaSortOrder, limit: Int?): List<MediaItem> = emptyList()
    override suspend fun getAllMedia(sortOrder: MediaSortOrder, limit: Int?): List<MediaItem> = emptyList()
    override suspend fun getMediaByType(mediaType: MediaType, sortOrder: MediaSortOrder, limit: Int?): List<MediaItem> = emptyList()
}
```

#### 4.3.3 foundation/ads - GoogleAdsInitializerTest

文件路径：`foundation/ads/src/test/java/com/wujia/foundation/ads/initialization/GoogleAdsInitializerTest.kt`

| 类名                   | 行号     | 实现接口           | 说明                                                   |
|------------------------|----------|--------------------|--------------------------------------------------------|
| `FakeAdsConfigProvider`  | 第 95 行  | `AdsConfigProvider`  | 返回构造时注入的 `AdsConfig`                            |
| `FakeAdsConsentManager`  | 第 101 行 | `AdsConsentManager`  | 可配置 `canRequestAds` 布尔值，记录 `gatherConsent` 调用次数 |
| `FakeGoogleAdsSdk`       | 第 127 行 | `GoogleAdsSdk`       | 记录 `initialize` 和 `applyRequestConfiguration` 调用次数 |

```kotlin
private class FakeGoogleAdsSdk : GoogleAdsSdk {
    var initializeCalls = 0
    var applyRequestConfigurationCalls = 0
    override suspend fun initialize(context: android.content.Context) { initializeCalls += 1 }
    override fun applyRequestConfiguration(config: AdsConfig) { applyRequestConfigurationCalls += 1 }
}
```

---

## 5. 依赖关系

### 5.1 依赖的模块

| 依赖方式       | 依赖目标                    | 说明                                      |
|----------------|-----------------------------|-------------------------------------------|
| `implementation` | `projects.foundation.model` | 用于引用 `SceneResourceRepository`、`SceneResource`、`EditableScene` 等模型类型 |
| `api`            | `libs.kotlinx.coroutines.test` | 传递 `TestDispatcher`、`runTest` 等协程测试 API |
| `api`            | `libs.junit`                  | 传递 JUnit 4 的 `TestWatcher`、`@Rule` 等 API   |

### 5.2 被依赖的模块

以下模块通过 `testImplementation(projects.foundation.testing)` 依赖本模块：

| 模块                          | Gradle 路径                  | 使用的组件                                  |
|-------------------------------|------------------------------|---------------------------------------------|
| `feature/scene/impl`          | `:feature:scene:impl`        | `FakeSceneResourceRepository`、`MainDispatcherRule` |
| `feature/sceneList/impl`      | `:feature:sceneList:impl`    | `FakeSceneResourceRepository`、`MainDispatcherRule` |
| `feature/sceneControl/impl`   | `:feature:sceneControl:impl` | `FakeSceneResourceRepository`、`MainDispatcherRule` |
| `feature/sceneEdit/impl`      | `:feature:sceneEdit:impl`    | `FakeSceneResourceRepository`、`MainDispatcherRule` |
| `foundation/domain`           | `:foundation:domain`         | `FakeSceneResourceRepository`（未使用 `MainDispatcherRule`） |

### 5.3 未使用本模块但拥有本地 Fake 的模块

| 模块              | Gradle 路径              | 原因                                                                 |
|-------------------|--------------------------|----------------------------------------------------------------------|
| `foundation/ads`  | `:foundation:ads`        | 自行声明 `kotlinx.coroutines.test` 和 `robolectric` 依赖，未使用 `MainDispatcherRule`，采用直接传入 `StandardTestDispatcher` 的方式管理协程调度 |

### 5.4 依赖关系图

```
                    foundation.model
                   /       |        \
          (implementation) |         \
                  /        |          \
   foundation.testing      |           \
    /    |    |    \        |            \
   /     |    |     \       |             \
  v      v    v      v     v              v
scene  sceneList  sceneControl  sceneEdit  domain  (均 testImplementation)
 impl     impl       impl        impl
```

```
foundation.ads (不依赖 foundation.testing，本地维护自己的 Fake 类)
```

---

## 6. 当前缺陷和改进建议

### 6.1 Fake 覆盖不足

**现状**：`foundation/testing` 模块目前仅提供 `FakeSceneResourceRepository` 一个 Fake 实现，但项目中实际存在多个 Repository 接口，它们的 Fake 实现分散在各 feature 模块的测试文件中。

**影响**：
- `FakeMediaRepository`、`FakeBackgroundResourceRepository`、`FakeVideoResourceRepository`、`FakeNoiseResourceRepository` 这 4 个 Fake 类定义在 `feature/sceneEdit/impl` 的测试文件中（第 363-447 行），均为 `private` 可见性，无法被其他需要相同模拟行为的模块复用。
- 如果未来 `feature/scene/impl` 或 `foundation/domain` 需要测试涉及背景资源、噪音资源的逻辑，只能重新编写 Fake。

**建议**：
1. 将 `FakeMediaRepository`、`FakeBackgroundResourceRepository`、`FakeVideoResourceRepository`、`FakeNoiseResourceRepository` 提升到 `foundation/testing` 模块，与 `FakeSceneResourceRepository` 保持一致的公开可见性和可配置行为。
2. 统一各 Fake 的行为记录模式（如使用构造注入预设数据、通过公开属性记录调用参数等）。

### 6.2 foundation/ads 模块未接入共享测试基础设施

**现状**：`foundation/ads` 模块的测试自行管理 `StandardTestDispatcher`，自行编写了 `FakeAdsConfigProvider`、`FakeAdsConsentManager`、`FakeGoogleAdsSdk` 等 Fake 类，且不使用 `MainDispatcherRule`。

**影响**：
- 协程调度器的管理方式不统一：其他模块通过 `MainDispatcherRule` 统一管理，而 ads 模块直接在 `runTest(testDispatcher)` 中传入 dispatcher。
- ads 模块的 Fake 类全部为 `private`，当前仅服务模块内测试。

**建议**：
1. 评估 ads 模块是否可以引入 `MainDispatcherRule`，统一协程调度器管理模式。
2. 若 ads 模块的 Fake 类在更多测试场景中被使用，考虑将通用部分提取到 `foundation/testing` 或 ads 模块自身的 `testFixtures` source set。

### 6.3 缺少测试对测试基础设施自身的验证

**现状**：`foundation/testing` 模块没有自身的单元测试。`FakeSceneResourceRepository` 的行为正确性完全依赖消费方测试的间接验证。

**建议**：
1. 为 `FakeSceneResourceRepository` 添加少量自测用例，确保其作为测试基础设施的契约稳定性。
2. 验证 `MainDispatcherRule` 在 `starting()`/`finished()` 生命周期中是否正确设置和重置调度器。

### 6.4 依赖声明方式可优化

**现状**：本模块以 `implementation` 依赖 `foundation.model`，但由于本模块的公开 API（`FakeSceneResourceRepository`）直接暴露了 `SceneResource`、`EditableScene`、`SceneEditInput`、`SceneControlSettings`、`SceneCategory` 等类型，按 Gradle 最佳实践应使用 `api` 声明。

**建议**：将 `foundation.model` 的依赖方式从 `implementation` 改为 `api`，确保消费方能透明访问这些模型类型而无需自行声明对 `foundation.model` 的依赖。

### 6.5 FakeSceneResourceRepository 的空实现缺乏可观测性

**现状**：`updateSceneControlSettings()` 和 `updateSceneAudioVolume()` 为纯空实现（`Unit`），调用方无法断言这些方法是否被调用以及传入了什么参数。

**建议**：参照 `saveSceneEdit` 和 `deleteSceneResource` 的模式，为这两个方法添加行为记录属性（如 `lastControlSettings`、`lastAudioVolume`），提升测试断言能力。

### 6.6 缺少 FakeSceneResourceFactory 或 Builder

**现状**：消费方每次使用 `FakeSceneResourceRepository` 时需要自行构造 `SceneResource` 对象，涉及较多样板代码。

**建议**：提供一个 `SceneResourceFactory` 或 Builder 工具类，支持快速创建具有默认合理值的 `SceneResource`，减少测试代码中的样板代码量。

---

## 7. 代码统计

### 7.1 模块自身

| 指标       | 值   |
|------------|------|
| 源文件数   | 2    |
| 总代码行数 | 107  |

| 文件                          | 行数 |
|-------------------------------|------|
| `MainDispatcherRule.kt`       | 23   |
| `FakeSceneResourceRepository.kt` | 84   |

### 7.2 消费方测试文件统计

使用 `foundation/testing` 模块的测试文件：

| 文件                                                                                         | 行数 |
|----------------------------------------------------------------------------------------------|------|
| `foundation/domain/src/test/.../SceneResourceUseCaseTest.kt`                                | 45   |
| `feature/scene/impl/src/test/.../SceneViewModelTest.kt`                                     | 135  |
| `feature/sceneList/impl/src/test/.../SceneListViewModelTest.kt`                             | 104  |
| `feature/sceneControl/impl/src/test/.../SceneControlViewModelTest.kt`                       | 362  |
| `feature/sceneEdit/impl/src/test/.../SceneEditViewModelTest.kt`                             | 447  |
| **小计**                                                                                     | **1093** |

### 7.3 项目中所有 Fake 类统计

| 位置          | Fake 类数量 | 文件数 |
|---------------|-------------|--------|
| `foundation/testing`（共享）   | 1  | 1  |
| `feature/sceneEdit/impl`（本地） | 4  | 1  |
| `feature/sceneControl/impl`（本地） | 1  | 1  |
| `foundation/ads`（本地）        | 7  | 2  |
| **合计**                        | **13** | **5** |

其中，共享模块中的 Fake 占比约 7.7%（1/13），表明仍有大量测试替身未被提取为共享资源。
