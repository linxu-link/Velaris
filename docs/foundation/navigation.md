# foundation:navigation 模块文档

> **模块路径：** `foundation/navigation/`
> **Gradle 路径：** `:foundation:navigation`
> **命名空间：** `com.wujia.foundation.navigation`

---

## 1. 模块概述

`foundation:navigation` 是 Velaris 项目的**导航基础设施模块**，基于 AndroidX Navigation3（`androidx.navigation3.runtime`）构建，提供类型安全的路由机制和自定义多栈状态管理。

该模块的核心设计理念是**双层栈导航（Dual-Stack Navigation）**：

- **一级导航栈（TopLevelStack）**：存放顶层分区键（类似底部 Tab），记录分区切换顺序。
- **子栈（SubStacks）**：每个一级键拥有独立的子返回栈，存放该分区内的页面堆叠。

这种架构适用于多分区场景——每个分区内有独立的页面栈，分区之间互不干扰，同时支持跨分区的历史回退。

模块为横向沉浸式场景应用提供导航骨架，所有导航操作均通过类型安全的 `NavKey` 子类触发，支持配置变更（Configuration Change）和进程死亡（Process Death）后的状态自动恢复。

---

## 2. 架构设计

### 2.1 整体架构

```
┌──────────────────────────────────────────────────────────────────┐
│                          VelarisApp                              │
│                                                                  │
│  val state     = rememberNavigationState(startKey, topLevelKeys) │
│  val navigator = Navigator(state)                                │
│  val entries   = state.toEntries(entryProvider)                  │
│                                                                  │
│  NavDisplay(entries, onBack = navigator::goBack)                  │
└──────────────────────────────────────────────────────────────────┘
                               │
                               │ toEntries() 展平
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│  NavEntry 列表（按一级栈顺序排列，每个一级键下展开其子栈所有Entry） │
│  装饰器：SaveableStateHolder + ViewModelStore                     │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 NavKey 类型安全路由

所有路由目标均由实现 `NavKey` 接口的密封类定义。每个 `NavKey` 对应一个唯一的屏幕，通过 `@kotlinx.serialization.Serializable` 注解支持序列化，确保 Navigation3 的状态保存能力。

设计规范：

- 无参数路由使用 `data object`（单例），有参数路由使用 `data class`
- 参数通过构造函数传入，天然类型安全，无需字符串解析
- 定义在各 feature 模块的 `api` 子模块中，确保跨模块可见性

```kotlin
// 无参数路由 - data object
@kotlinx.serialization.Serializable
data object SceneNavKey : NavKey

// 带参数路由 - data class，参数均有默认值
@kotlinx.serialization.Serializable
data class SceneEditNavKey(
    val sceneId: String? = null,
    val category: SceneCategory? = null,
) : NavKey
```

### 2.3 NavigationState 多栈管理

`NavigationState` 维护两个核心数据结构：

```
topLevelStack: [ SceneNavKey ]                           ← 一级导航栈
                       │
subStacks:      SceneNavKey → [ SceneNavKey,            ← 子栈（栈底为一级键本身）
                                 SceneEditNavKey(id=1) ]
```

**层级关系：**

| 层级 | 数据结构 | 说明 |
|------|---------|------|
| 一级栈 | `topLevelStack: NavBackStack<NavKey>` | 存放一级导航键，决定当前显示哪个分区 |
| 子栈 | `subStacks: Map<NavKey, NavBackStack<NavKey>>` | 每个一级键对应一个独立子栈，记录该分区内的页面堆叠 |

当前活跃页面由 `currentSubStack.last()` 决定（即当前分区子栈的栈顶）。

**状态持久化：** 一级栈和子栈均通过 `rememberNavBackStack()` 创建，底层使用 Navigation3 的 `SaveableStateHolder` 机制，自动在配置变更和进程死亡后恢复。

**关键派生状态：**
- `currentTopLevelKey`：当前一级栈顶键，通过 `derivedStateOf` 自动响应栈变化
- `currentKey`：当前子栈顶键，代表实际显示的页面

### 2.4 Navigator 策略模式

`Navigator` 是导航操作的唯一入口，封装所有栈操作逻辑。根据目标键的类型自动选择不同的导航策略：

```
navigate(key)
  │
  ├─ key == currentTopLevelKey → clearSubStack()      // 回到当前分区首页
  │
  ├─ key ∈ topLevelKeys        → goToTopLevel(key)    // 切换一级分区
  │
  └─ 其他                       → goToKey(key)         // 在当前子栈内前进
```

---

## 3. 核心类/接口

### 3.1 NavigationState 类

**文件：** `foundation/navigation/src/main/java/com/wujia/foundation/navigation/NavigationState.kt`

#### `rememberNavigationState()` 工厂函数（第 22-36 行）

```kotlin
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `startKey` | `NavKey` | 应用起始路由键，用户从该键按返回将尝试退出应用 |
| `topLevelKeys` | `Set<NavKey>` | 所有一级导航键的集合，每个键将自动创建独立子栈 |

内部使用 `rememberNavBackStack(startKey)` 创建一级栈，遍历 `topLevelKeys` 为每个键创建子栈。

#### NavigationState 类定义（第 45-62 行）

```kotlin
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
)
```

**属性一览：**

| 属性 | 类型 | 可见性 | 行号 | 说明 |
|------|------|--------|------|------|
| `startKey` | `NavKey` | public | 46 | 应用起始导航键 |
| `topLevelStack` | `NavBackStack<NavKey>` | public | 47 | 一级导航返回栈 |
| `subStacks` | `Map<NavKey, NavBackStack<NavKey>>` | public | 48 | 一级键到子栈的映射 |
| `currentTopLevelKey` | `NavKey`（derivedStateOf） | public | 50 | 当前一级栈顶键 |
| `topLevelKeys` | `Set<NavKey>`（getter） | public | 52-53 | 所有一级键集合（`subStacks.keys`） |
| `currentSubStack` | `NavBackStack<NavKey>` | @VisibleForTesting | 56-58 | 当前活跃分区的子栈 |
| `currentKey` | `NavKey`（derivedStateOf） | @VisibleForTesting | 61 | 当前子栈顶键，即实际显示页面 |

#### `toEntries()` 扩展函数（第 68-86 行）

```kotlin
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `entryProvider` | `(NavKey) -> NavEntry<NavKey>` | NavKey 到 NavEntry 的工厂函数 |

**返回值：** 展平后的 `SnapshotStateList<NavEntry<NavKey>>`，顺序按一级栈排列，每个一级键下展开其子栈的所有 Entry。

**内置装饰器（对每个子栈独立应用）：**
- `rememberSaveableStateHolderNavEntryDecorator()` — 确保每个 Entry 的 SaveableState 独立保存和恢复
- `rememberViewModelStoreNavEntryDecorator()` — 确保每个 Entry 拥有独立的 ViewModelStore，ViewModel 生命周期与 Entry 绑定

---

### 3.2 Navigator 类

**文件：** `foundation/navigation/src/main/java/com/wujia/foundation/navigation/Navigator.kt`

#### 构造函数（第 10 行）

```kotlin
class Navigator(val state: NavigationState)
```

#### 公开方法

| 方法 | 签名 | 行号 | 说明 |
|------|------|------|------|
| `navigate` | `fun navigate(key: NavKey)` | 17-23 | 核心导航方法。三种情况：(1) `key == currentTopLevelKey` → 清空子栈（回到分区首页）；(2) `key ∈ topLevelKeys` → 切换到目标一级分区；(3) 其他 → 压入当前子栈（去重提升至栈顶） |
| `replaceCurrentSubStackWith` | `fun replaceCurrentSubStackWith(key: NavKey)` | 28-43 | 替换当前子栈：清空后放入 `[currentTopLevelKey, key]`。若 key 是一级键则切换分区并清空目标子栈 |
| `goBack` | `fun goBack()` | 48-57 | 返回操作：当前为 startKey 时抛异常；当前在子栈底部时弹出一级栈；否则弹出当前子栈栈顶 |

#### 私有方法

| 方法 | 行号 | 说明 |
|------|------|------|
| `goToKey(key: NavKey)` | 62-68 | 压入当前子栈，若已存在则先移除再添加（move-to-top 语义） |
| `goToTopLevel(key: NavKey)` | 73-84 | 压入一级栈；startKey 时先清空一级栈确保其始终在栈底；其他键先移除再添加 |
| `clearSubStack()` | 89-93 | 清除当前子栈中除第一个元素（根键）以外的所有键，保留 size <= 1 时无操作 |

#### `navigate(key)` 策略详解

| key 的类型 | 行为 | 对应方法 |
|-----------|------|---------|
| 等于当前一级键（`currentTopLevelKey`） | 清空当前子栈中除根键以外的所有键 | `clearSubStack()` |
| 属于某个一级键（`in topLevelKeys`） | 切换到该一级栈，将其推至一级栈顶部 | `goToTopLevel(key)` |
| 其他（非一级键） | 在当前子栈末尾添加 key，若已存在则先移除再添加 | `goToKey(key)` |

#### `goBack()` 策略详解

| 当前状态 | 行为 |
|---------|------|
| 当前是起始键（`currentKey == startKey`） | 抛出异常 `"不能从起始路由返回"` |
| 当前是子栈底部（`currentKey == currentTopLevelKey`） | 从一级栈弹出最后一个键（`topLevelStack.removeLastOrNull()`） |
| 其他 | 从当前子栈弹出最后一个键（`currentSubStack.removeLastOrNull()`） |

---

## 4. 对外暴露的接口

### 4.1 foundation:navigation 模块公开 API

| API | 类型 | 文件 | 行号 |
|-----|------|------|------|
| `rememberNavigationState()` | @Composable 函数 | NavigationState.kt | 22-36 |
| `NavigationState` | 类 | NavigationState.kt | 45-62 |
| `NavigationState.toEntries()` | 扩展函数 | NavigationState.kt | 68-86 |
| `Navigator` | 类 | Navigator.kt | 10-94 |
| `Navigator.navigate()` | 方法 | Navigator.kt | 17-23 |
| `Navigator.replaceCurrentSubStackWith()` | 方法 | Navigator.kt | 28-43 |
| `Navigator.goBack()` | 方法 | Navigator.kt | 48-57 |

### 4.2 NavKey 类型定义

以下 NavKey 类型定义在各 feature 模块的 `api` 子模块中（不在 `foundation:navigation` 内部），但与导航模块紧密关联：

| NavKey 类型 | 定义文件 | Kotlin 类型 | 参数 |
|------------|---------|------------|------|
| `SceneNavKey` | `feature/scene/api/src/main/java/com/wujia/feature/scene/api/SceneNavKey.kt` | `data object` | 无 |
| `SceneListNavKey` | `feature/sceneList/api/src/main/java/com/wujia/feature/scenelist/api/SceneListNavKey.kt` | `data class` | `category: SceneCategory? = null` |
| `SceneEditNavKey` | `feature/sceneEdit/api/src/main/java/com/wujia/feature/sceneedit/api/SceneEditNavKey.kt` | `data class` | `sceneId: String? = null`, `category: SceneCategory? = null` |
| `SceneControlNavKey` | `feature/sceneControl/api/src/main/java/com/wujia/feature/scenecontrol/api/SceneControlNavKey.kt` | `data object` | 无 |
| `SettingsNavKey` | `feature/settings/api/src/main/java/com/wujia/feature/settings/api/SettingsNavKey.kt` | `data object` | 无 |

### 4.3 Entry 注册函数（各 feature impl 模块）

各 feature 模块的 `impl` 子模块通过 `EntryProviderScope<NavKey>` 的扩展函数注册路由入口：

| 函数 | 所属模块 | 文件 | 注册的 NavKey |
|------|---------|------|-------------|
| `EntryProviderScope<NavKey>.sceneEntry(navigator)` | feature:scene:impl | `feature/scene/impl/.../SceneEntry.kt` | `SceneNavKey`，同时内联注册 `sceneEditEntry` |
| `EntryProviderScope<NavKey>.sceneListEntry(navigator)` | feature:sceneList:impl | `feature/sceneList/impl/.../SceneListEntry.kt` | `SceneListNavKey` |
| `EntryProviderScope<NavKey>.sceneEditEntry(navigator)` | feature:sceneEdit:impl | `feature/sceneEdit/impl/.../SceneEditEntry.kt` | `SceneEditNavKey` |
| `EntryProviderScope<NavKey>.sceneControlEntry(navigator)` | feature:sceneControl:impl | `feature/sceneControl/impl/.../SceneControlEntry.kt` | `SceneControlNavKey` |
| `EntryProviderScope<NavKey>.settingsEntry(navigator)` | feature:settings:impl | `feature/settings/impl/.../SettingsEntry.kt` | `SettingsNavKey` |

**当前集成状态：** `VelarisApp.kt` 中的 `entryProvider` 仅注册了 `sceneEntry(navigator)`，而 `sceneEntry` 内部嵌套调用了 `sceneEditEntry(navigator)`。其他 feature（sceneList、sceneControl、settings）的 entry 注册未出现在当前主入口中，说明这些功能尚未完成主入口集成，或处于开发中。

**Lock 功能**：`feature:lock` 采用独立 Activity（非 NavKey），通过 `LockScreenLauncher` 启动 `LockScreenActivity`。目前在 `settings.gradle.kts` 中被注释，未编译进应用。

---

## 5. 依赖关系

### 5.1 本模块的依赖（build.gradle.kts）

**文件：** `foundation/navigation/build.gradle.kts`

**Gradle 插件：**
- `advance.android.library` — 项目自定义 Android Library 配置插件
- `advance.hilt` / `hilt` — Hilt 依赖注入
- `kotlin.serialization` — Kotlin 序列化支持
- `compose` — Jetpack Compose 编译支持

**依赖库：**

| 依赖 | 类型 | 说明 |
|------|------|------|
| `androidx.navigation3.runtime` | `api` | Navigation3 运行时：`NavKey`、`NavBackStack`、`NavEntry`、`rememberNavBackStack`、`rememberDecoratedNavEntries` |
| `androidx.savedstate.compose` | `implementation` | Compose SavedState 支持 |
| `androidx.lifecycle.viewModel.navigation3` | `implementation` | Navigation3 的 ViewModel 集成（`rememberViewModelStoreNavEntryDecorator`） |

### 5.2 依赖本模块的模块（上游消费方）

| 模块 | 依赖方式 | 用途 |
|------|---------|------|
| `:app` | `implementation` | 创建 `NavigationState` 和 `Navigator`，组装 entryProvider，渲染 `NavDisplay` |
| `:feature:scene:impl` | `implementation` | 注册 `sceneEntry` 路由，调用 `Navigator.navigate()` |
| `:feature:sceneList:impl` | `implementation` | 注册 `sceneListEntry` 路由，调用 `Navigator.navigate()` 和 `Navigator.goBack()` |
| `:feature:sceneEdit:impl` | `implementation` | 注册 `sceneEditEntry` 路由，调用 `Navigator.goBack()` |
| `:feature:settings:impl` | `implementation` | 注册 `settingsEntry` 路由，调用 `Navigator.goBack()` |

**注意：** `:feature:sceneControl:impl` 的 `SceneControlEntry.kt` 导入了 `Navigator`，但其 `build.gradle.kts` 中未显式声明对 `foundation:navigation` 的依赖，推测通过传递依赖或上层编译获得。

### 5.3 依赖关系图

```
        ┌───────────────────────────┐
        │  androidx.navigation3     │
        │  .runtime (api)           │
        └─────────────┬─────────────┘
                      │
        ┌─────────────▼─────────────┐
        │                           │
        │   foundation:navigation   │
        │                           │
        └─────────────┬─────────────┘
                      │
   ┌──────┬───────────┼───────────┬──────────┐
   │      │           │           │          │
┌──▼──┐ ┌─▼──────┐ ┌──▼───────┐ ┌▼────────┐ ┌▼────────┐
│ :app│ │ scene  │ │sceneEdit │ │sceneList│ │settings │
│     │ │ :impl  │ │  :impl   │ │  :impl  │ │  :impl  │
└─────┘ └────────┘ └──────────┘ └─────────┘ └─────────┘
```

---

## 6. 当前缺陷/改进点

### 6.1 设计层面

**D1. 顶层 Entry 注册不完整**

`VelarisApp.kt` 中 `entryProvider` 仅注册了 `sceneEntry`，其他 feature 模块（sceneList、sceneControl、settings）的 entry 注册未出现在主入口。这意味着这些路由可能尚未生效，或存在路由图不完整的问题。

**D2. 多栈架构未充分利用**

`VelarisApp.kt` 中 `topLevelKeys` 仅包含 `SceneNavKey` 一个键，实际上只有单分区，未真正使用多栈切换能力。`SceneListNavKey`、`SettingsNavKey` 等均作为子导航键使用，与 `SceneNavKey` 共享同一子栈，这意味着它们的页面堆叠在同一栈中，无法独立管理。

**D3. Navigator.state 直接暴露**

`Navigator` 的构造参数 `state` 声明为 `public val`（第 10 行），外部代码可直接操作底层栈内容（如 `navigator.state.currentSubStack.add(key)`），绕过 Navigator 的封装逻辑。建议改为 `private` 或 `internal`。

### 6.2 功能层面

**F1. goBack() 在 startKey 时直接抛异常**

`Navigator.kt` 第 50 行：当 `currentKey == startKey` 时调用 `error()` 直接崩溃。上层需自行判断是否处于起始页面来避免调用 `goBack()`，增加心智负担且容易遗漏。

建议：返回 `Boolean` 表示是否消费了返回事件，或提供 `canGoBack(): Boolean` 方法供上层判断。

**F2. 缺少深度链接（Deep Link）支持**

当前所有 NavKey 均通过内存对象进行导航，缺少 URI/Intent 形式的路由解析能力。

**F3. 缺少导航动画配置入口**

`toEntries()` 未暴露过渡动画参数，所有页面切换使用 Navigation3 默认行为，无法为不同路由配置不同动画。

**F4. 缺少导航拦截/守卫机制**

没有提供 `beforeNavigate` 类型的拦截器，无法实现权限检查、未保存内容确认等场景。

### 6.3 代码层面

**C1. goToKey 去重行为隐含副作用**

`goToKey()` 中先 `remove(key)` 再 `add(key)` 的 move-to-top 语义可能导致意外的栈结构变化。例如，将栈中层的页面提升到栈顶，中间页面保留在栈中。在某些场景下这可能导致用户回退时出现非预期的中间页面。

**C2. replaceCurrentSubStackWith 的边界条件**

`Navigator.kt` 第 31-33 行：当 key 是另一个一级键时，先 `goToTopLevel(key)` 切换了 `currentTopLevelKey`，再调用 `clearSubStack()` 清除的是**新切换到的分区子栈**而非原分区子栈。这可能与方法名暗示的语义（"替换当前子栈"）不一致。

**C3. currentSubStack 缺少空安全保护**

`NavigationState.kt` 第 57-58 行：若 `topLevelStack` 中的键不在 `subStacks` 映射中，直接抛异常。虽然正常流程不会出现此情况，但缺少防御性编程。

**C4. 缺少单元测试**

`currentSubStack` 和 `currentKey` 标注了 `@VisibleForTesting`，但仓库中未发现对应的测试文件。Navigator 的三种导航策略逻辑纯属栈操作，非常适合用 JVM 单元测试覆盖。

---

## 7. 代码统计

### 7.1 文件清单

| 文件 | 绝对路径 | 行数 | 说明 |
|------|---------|------|------|
| `NavigationState.kt` | `foundation/navigation/src/main/java/com/wujia/foundation/navigation/NavigationState.kt` | 86 | 导航状态：`rememberNavigationState`、`NavigationState` 类、`toEntries` 扩展 |
| `Navigator.kt` | `foundation/navigation/src/main/java/com/wujia/foundation/navigation/Navigator.kt` | 94 | 导航器：`navigate`、`goBack`、`replaceCurrentSubStackWith` |
| `build.gradle.kts` | `foundation/navigation/build.gradle.kts` | 16 | Gradle 构建配置 |

### 7.2 统计摘要

| 指标 | 数值 |
|------|------|
| Kotlin 源文件数 | 2 |
| Kotlin 源码总行数 | 180 |
| 含构建脚本总行数 | 196 |
| 公开类数 | 2（`NavigationState`、`Navigator`） |
| 公开 @Composable 函数 | 1（`rememberNavigationState`） |
| 公开扩展函数 | 1（`toEntries`） |
| Navigator 公开方法数 | 3（`navigate`、`replaceCurrentSubStackWith`、`goBack`） |
| Navigator 私有方法数 | 3（`goToKey`、`goToTopLevel`、`clearSubStack`） |
| 项目中 NavKey 类型总数 | 5（`SceneNavKey`、`SceneListNavKey`、`SceneEditNavKey`、`SceneControlNavKey`、`SettingsNavKey`） |
| 外部依赖数 | 3（1 api + 2 implementation） |
| 上游消费模块数 | 5（`:app` + 4 个 feature `:impl`） |

### 7.3 复杂度评估

本模块属于**低复杂度**基础设施模块：

- 仅 2 个 Kotlin 文件，约 180 行源码
- 无自定义 View、无资源文件、无测试代码
- 逻辑集中在 Navigator 的三种导航策略和 NavigationState 的双层栈管理
- 依赖链清晰，不引入循环依赖

---

## 附录 A：完整代码流程示例

以"从场景页导航到场景编辑页，编辑完后返回"为例：

```
1. 初始状态
   topLevelStack: [SceneNavKey]
   subStacks: {
     SceneNavKey → [SceneNavKey]
   }
   currentTopLevelKey = SceneNavKey
   currentKey = SceneNavKey

2. 调用 navigator.navigate(SceneEditNavKey(sceneId="1"))
   → SceneEditNavKey 不是顶级键，走 goToKey()
   → 当前子栈：remove(SceneEditNavKey) 无效果，add(SceneEditNavKey)
   topLevelStack: [SceneNavKey]
   subStacks: {
     SceneNavKey → [SceneNavKey, SceneEditNavKey(sceneId="1")]
   }
   currentKey = SceneEditNavKey(sceneId="1")

3. 调用 navigator.goBack()
   → currentKey = SceneEditNavKey，不是顶级键，走 else 分支
   → currentSubStack.removeLastOrNull() 弹出 SceneEditNavKey
   topLevelStack: [SceneNavKey]
   subStacks: {
     SceneNavKey → [SceneNavKey]
   }
   currentKey = SceneNavKey  ← 回到场景页面
```

## 附录 B：关键源码行号索引

| 内容 | 文件 | 行号 |
|------|------|------|
| `rememberNavigationState()` 工厂函数 | NavigationState.kt | 22-36 |
| `NavigationState` 类定义 | NavigationState.kt | 45-62 |
| `currentTopLevelKey` 属性 | NavigationState.kt | 50 |
| `currentSubStack` 属性 | NavigationState.kt | 56-58 |
| `currentKey` 属性 | NavigationState.kt | 61 |
| `toEntries()` 扩展函数 | NavigationState.kt | 68-86 |
| `Navigator` 类定义 | Navigator.kt | 10 |
| `navigate()` 方法 | Navigator.kt | 17-23 |
| `replaceCurrentSubStackWith()` 方法 | Navigator.kt | 28-43 |
| `goBack()` 方法 | Navigator.kt | 48-57 |
| `goToKey()` 私有方法 | Navigator.kt | 62-68 |
| `goToTopLevel()` 私有方法 | Navigator.kt | 73-84 |
| `clearSubStack()` 私有方法 | Navigator.kt | 89-93 |
