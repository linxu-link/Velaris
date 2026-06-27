# feature:scene

`:feature:scene` 是 Velaris 应用的核心场景体验模块，包含主场景展示页面（根路由）和作为其他 feature 面板/编辑页的**聚合宿主**。

## 模块概述

- **api 路径**：`feature/scene/api`
- **impl 路径**：`feature/scene/impl`
- **包名**：`com.wujia.feature.scene.api` / `com.wujia.feature.scene.impl`
- **核心职责**：
  - 提供 `SceneNavKey` 作为应用启动后的根页面
  - 实现主场景浏览（横屏沉浸式背景/视频/音频/粒子叠加）
  - 管理面板 overlay（控制面板、粒子面板、列表、编辑、设置）
  - 协调播放器（PlayerMediaCoordinator）和粒子渲染
  - 在 `sceneEntry` 中聚合注册其他 feature 的导航入口
- **特殊地位**：唯一允许依赖其他 feature `impl` 模块的 feature（AGENTS.md 唯一例外）。

## 架构与导航角色

```
VelarisApp (app)
  └── rememberNavigationState(startKey = SceneNavKey, topLevelKeys = {SceneNavKey})
       └── sceneEntry(navigator)          ← 由 scene:impl 提供
            ├── SceneScreen
            └── sceneEditEntry(navigator) ← 内联注册
```

当前 `topLevelKeys` 仅包含 `SceneNavKey`，所有其他页面（列表、控制、编辑、设置）均作为其子栈存在。

## 核心类与文件

### api
| 文件 | 说明 |
|------|------|
| `SceneNavKey.kt` | `data object SceneNavKey : NavKey`（@Serializable） |

### impl 主要文件

**导航入口**
- `navigation/SceneEntry.kt`
  - `fun EntryProviderScope<NavKey>.sceneEntry(navigator)`
  - 注册 `SceneNavKey` → `SceneScreen`
  - 同时调用 `sceneEditEntry(navigator)` 完成编辑页聚合

**ViewModel**
- `SceneViewModel.kt`（@HiltViewModel internal）
  - 依赖 `ObserveSceneResourcesUseCase` 等多个 UseCase
  - 暴露 `uiState: StateFlow<SceneUiState>`
  - 内部维护 `ScenePanelState`（NONE / CONTROL / PARTICLE / EDIT / LIST / SETTINGS）
  - 处理页面切换、音量更新、控制设置保存、面板开关等事件

**UiState**（@Stable）
```kotlin
internal data class SceneUiState(
    val scenes: List<SceneResource>,
    val allScenes: List<SceneResource>,
    val currentSceneId: String?,
    val selectedCategory: SceneCategory,
    val activePanel: ScenePanelState,
    ...
) {
    val currentScene: SceneResource?
    val showControlPanel / showParticlePanel / ...
    val hasCompletedGuide: Boolean
}
```

**主屏幕**
- `SceneScreen.kt`
- `ui/SceneLayoutParams.kt`、`ChromeAutoHideState.kt`
- 大量 overlay 组件：
  - `overlay/ScenePanelOverlay.kt`
  - `dialog/*Dialog.kt`（AlarmReminderDialog、CustomTimerDialog、PlayerSoundDialog 等）
  - `component/SceneTitleLayer.kt`、`SystemClockDisplay.kt` 等

**协调器**
- `PlayerMediaCoordinator.kt`：音视频播放与场景资源联动
- `ParticleMapping.kt`：粒子效果与 SceneParticleSettings 的映射

## 依赖关系（impl 模块）

**build.gradle.kts 关键依赖**（scene:impl）：
```kotlin
dependencies {
    api(projects.feature.scene.api)

    // 唯一允许直接依赖其他 feature impl 的地方
    implementation(projects.feature.sceneControl.impl)
    implementation(projects.feature.sceneEdit.impl)
    implementation(projects.feature.sceneList.impl)
    implementation(projects.feature.settings.impl)

    implementation(projects.foundation.navigation)
    implementation(projects.foundation.player)
    implementation(projects.foundation.particle)
    implementation(projects.foundation.alarm)
    // ...
}
```

其他 feature 的 impl 模块**禁止**依赖 scene:impl，只能通过 api 进行导航交互。

## 当前状态与集成

- 已完整实现并作为应用根页面运行。
- 内部已聚合 `sceneEditEntry`。
- 通过 `activePanel` 状态驱动 `ScenePanelOverlay`，实现对 sceneList、sceneControl、settings 等面板的调用（即使这些面板的 entry 尚未在顶层注册）。
- 包含完整的播放、粒子、闹钟提醒、引导状态等业务协调逻辑。

## 代码统计（约）

- api：1 个文件（极简 NavKey）
- impl：约 16+ 个 Kotlin 文件（不含生成），含大量 UI 组件和 overlay 逻辑
- 特点：scene 是目前代码量最大的 feature 模块，承担了“容器 + 协调器 + 主 UI”三重职责。

## 注意事项

1. 作为聚合宿主，新增 feature 时必须在此模块的 `sceneEntry`（或未来多 topLevel 结构）中注册对应 entry。
2. 面板状态（ScenePanelState）目前是内部枚举，与其他 feature 面板的实际导航 key 存在一定耦合。
3. 播放器和粒子协调逻辑集中在 scene 模块，未来可考虑进一步下沉到专门的 coordinator 模块。
4. 目前仍为单 topLevel 设计（只有 SceneNavKey）。

---

文档基于 2026-06 实际代码生成，持续与源码保持同步。
