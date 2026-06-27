# feature:sceneList

`:feature:sceneList` 提供场景列表浏览、按分类（专注/助眠）筛选、以及拖拽排序功能。

## 模块概述

- **api**：`feature/sceneList/api`
- **impl**：`feature/sceneList/impl`
- **包名**：`com.wujia.feature.scenelist.api` / `com.wujia.feature.scenelist.impl`
- **核心职责**：
  - `SceneListNavKey(category)` 支持按分类打开列表
  - 实现卡片式场景列表 + CategoryTabs
  - 支持长按拖拽重排序（分类内或全局）
  - 提供“添加场景”入口（跳转编辑）
  - 删除非预设场景

## 架构

通过 `SceneListNavKey` 打开，通常作为 scene 主页的 overlay 面板使用。

```
SceneListNavKey(category)
  → sceneListEntry(navigator)
       → SceneListScreen
            ├── CategoryTabs
            └── SceneListCards（可拖拽）
```

## 核心类与文件

### api
- `SceneListNavKey.kt`
  ```kotlin
  data class SceneListNavKey(
      val category: SceneCategory? = null,
  ) : NavKey
  ```

### impl
- `navigation/SceneListEntry.kt`：注册入口，传递 category 并提供 onBack / onAddScene 回调
- `ui/SceneListViewModel.kt`（@HiltViewModel）
  - 依赖 `ObserveSceneResourcesUseCase`、`ReorderSceneResourcesUseCase`、`DeleteSceneResourceUseCase`
  - 暴露 `SceneListUiState(items: List<SceneListItem>, category, isLoading)`
- `entity/SceneListItem.kt`：列表展示用的轻量模型
- `ui/SceneListScreen.kt` + `ui/component/`：
  - `CategoryTabs.kt`
  - `SceneListCards.kt`
  - `SceneListDragState.kt`（拖拽状态管理）
- `SceneListLayoutProfile.kt`

## 依赖关系

```kotlin
dependencies {
    api(projects.feature.sceneList.api)
    implementation(projects.feature.sceneEdit.api) // 只依赖 api 用于新建
    implementation(projects.foundation.domain)
    implementation(projects.foundation.navigation)
    implementation(projects.foundation.designsystem)
    implementation(projects.foundation.ui)
    // coil 用于封面等
}
```

## 当前状态

- 核心列表 + 拖拽排序 + 分类 tabs 已实现。
- Entry 按照规范编写完成，但目前尚未在 `VelarisApp` 的顶层 `entryProvider` 中注册（通过 scene 内部面板 overlay 间接使用）。

## 特点

- 拖拽使用自定义 `move` 扩展和 `SceneListDragState` 管理临时顺序。
- 支持分类内排序（避免污染其他分类）。
- 列表项包含删除按钮（仅非预设场景）。

---

文档基于代码实际情况编写。
