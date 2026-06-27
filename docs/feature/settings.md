# feature:settings

`:feature:settings` 提供应用设置面板，包括主题切换、播放器性能配置、隐私与广告同意、关于信息等。

## 模块概述

- **api**：`feature/settings/api`
- **impl**：`feature/settings/impl`
- **包名**：`com.wujia.feature.settings.api` / `...impl`
- **核心职责**：
  - `SettingsNavKey`
  - 管理主题预设（VelarisThemePreset）和播放性能配置（Playback / PlayerConfig）
  - 集成 Google UMP 广告同意流程（通过 AdsConsentManager）
  - 显示应用版本、隐私政策、关于对话框
  - 提供多个设置对话框

## 核心类与文件

### api
- `SettingsNavKey.kt`：`data object SettingsNavKey : NavKey`

### impl
- `navigation/SettingsEntry.kt`
- `SettingsPanel.kt`
- `ui/SettingsScreen.kt`、`SettingsDialogOverlay.kt`
- `ui/SettingsViewModel.kt`
  - 依赖 `ThemeSettingsRepository`、`PlaybackSettingsRepository`、`AdsConsentManager`、`AppVersionInfo`
  - 暴露 `SettingsUiState`（selectedThemePreset、selectedProfile、多个 showXXXDialog 标志、consent 状态、版本号）
  - 提供 `playerConfig` 计算属性（根据 profile 映射到 VelarisPlayerConfig）
- `ui/dialog/`：
  - `ThemeSettingsDialog.kt`
  - `PlaybackSettingsDialog.kt`
  - `PrivacyDialog.kt`
  - `AboutDialog.kt`
  - `SettingsDialog.kt`

## 依赖关系

大量使用 foundation:player、foundation:ads、foundation:model:settings/theme 相关 Repository。

```kotlin
dependencies {
    api(projects.feature.settings.api)
    implementation(projects.foundation.player)
    implementation(projects.foundation.ads)
    implementation(projects.foundation.model)
    // ...
}
```

## 当前状态

- 设置功能齐全。
- Entry 已实现，但目前主要通过 scene 面板 overlay 触发（尚未在根 entryProvider 直接注册）。

## 特点

- 设置项变更立即通过 Repository 持久化并同步回 UiState。
- 广告同意状态实时反映在 UI 中（可请求广告、隐私选项等）。
- 播放性能配置直接影响 `VelarisPlayerConfig`（PowerSaver / Balanced / Quality）。

---

文档基于代码实际情况。
