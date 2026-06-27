# feature:sceneEdit

`:feature:sceneEdit` 提供场景编辑功能，支持从头创建新场景或编辑已有场景的材质（背景/视频）、环境音、粒子效果和控制参数。

## 模块概述

- **api 路径**：`feature/sceneEdit/api`
- **impl 路径**：`feature/sceneEdit/impl`
- **包名**：`com.wujia.feature.sceneedit.api` / `com.wujia.feature.sceneedit.impl`
- **核心职责**：
  - 定义带参数的 `SceneEditNavKey`（支持指定 sceneId 或 category）
  - 实现多步骤编辑流程（材质 → 声音 → 粒子 → 预览/控制）
  - 与 domain UseCase（GetEditableSceneUseCase、SaveSceneEditUseCase 等）协作
  - 提供媒体权限、资源选择等辅助对话框

## 架构

编辑页通过 `SceneEditNavKey` 导航进入，通常由 `scene` 或 `sceneList` 触发。

```
SceneEditNavKey(sceneId, category)
  → sceneEditEntry(navigator)
       → SceneEditPage（带返回按钮的壳层）
            → SceneEditScreen（核心编辑 UI + 多子面板）
```

编辑完成后调用 `onSaved = navigator::goBack` 返回上一级。

## 核心类与文件

### api
- `SceneEditNavKey.kt`
  ```kotlin
  @kotlinx.serialization.Serializable
  data class SceneEditNavKey(
      val sceneId: String? = null,
      val category: SceneCategory? = null,
  ) : NavKey
  ```

### impl 主要文件

**导航**
- `navigation/SceneEditEntry.kt`
  - 提供 `sceneEditEntry(navigator)`
  - 包装了带毛玻璃和返回按钮的 `SceneEditPage` 壳层
  - 内部嵌入 `SceneEditScreen`

**ViewModel**
- `ui/SceneEditViewModel.kt`（@HiltViewModel internal）
  - 依赖大量 Get*UseCase（媒体、背景、视频、噪声、粒子等）和 `SaveSceneEditUseCase`
  - 暴露 `uiState: StateFlow<SceneEditUiState>`
  - 内部维护多状态（`MaterialSelectionState`、`SoundSelectionState`、`ParticleSelectionState`）
  - 支持步骤切换（`SceneEditStep`：Material / Sound / Particle 等）

**UiState**
```kotlin
internal data class SceneEditUiState(
    val currentStep: SceneEditStep = SceneEditStep.Material,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val title: String,
    val description: String,
    val materialState: MaterialSelectionState,
    val soundState: SoundSelectionState,
    val particleState: ParticleSelectionState,
    val controlSettings: SceneControlSettings,
    ...
)
```

**主 UI**
- `ui/SceneEditScreen.kt`
- `ui/panel/`：
  - `SceneEditMaterial.kt`、`SceneEditNoise.kt`、`SceneEditParticle.kt`、`SceneEditPreview.kt`
- `ui/dialog/`：`MediaPermissionDialog.kt`、`SettingsDialog.kt`

## 依赖关系

**典型 impl 依赖**（只依赖 api + foundation）：
```kotlin
dependencies {
    api(projects.feature.sceneEdit.api)
    implementation(projects.feature.scene.api) // 仅 api，用于可能的导航

    implementation(projects.foundation.domain)
    implementation(projects.foundation.model)
    implementation(projects.foundation.ui)
    implementation(projects.foundation.designsystem)
    // ...
}
```

## 当前状态

- 功能完整，支持新建和编辑流程。
- 已通过 `sceneEntry` 内联注册，可从主场景页直接进入。
- 编辑保存后自动返回并刷新场景列表/当前场景。

## 特点与注意事项

- 编辑流程采用状态机（currentStep）驱动子面板切换。
- 媒体资源选择同时支持预置资源和用户设备媒体（需运行时权限）。
- 大量状态被拆分为独立的 `*SelectionState`，便于各子面板独立管理。
- `SceneEditUiState` 内部保留了向后兼容的委托属性（`selectedMaterial` 等）。

---

文档基于实际源码生成。
