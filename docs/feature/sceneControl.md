# feature:sceneControl

`:feature:sceneControl` 实现场景实时控制面板，覆盖画面（亮度/暗度）、定时器（倒计时/时钟模式）、环境音与视频音量、粒子效果、闹钟提醒、倒计时钟位置等功能。

## 模块概述

- **api**：`feature/sceneControl/api`
- **impl**：`feature/sceneControl/impl`
- **包名**：`com.wujia.feature.scenecontrol.api` / `...impl`
- **核心职责**：
  - `SceneControlNavKey`（无参数）
  - 提供 `SceneControlPanel` + `SceneParticlePanel`
  - 实时更新控制设置并持久化
  - 管理定时器倒计时（支持后台绝对时间戳）
  - 控制闹钟提醒开关（与 foundation:alarm 配合）

## 架构

作为独立面板由主场景页唤起。

```
SceneControlNavKey
  → sceneControlEntry(navigator)
       → SceneControlScreen / SceneParticleScreen
            → SceneControlViewModel（维护倒计时 tick）
```

## 核心类与文件

### api
- `SceneControlNavKey.kt`：`data object SceneControlNavKey : NavKey`

### impl
- `navigation/SceneControlEntry.kt`
- `SceneControlPanel.kt`、`SceneParticlePanel.kt`
- `ui/viewmodel/SceneControlViewModel.kt`
  - 暴露 `SceneControlUiState`（包含 `timerRemainingMillis` 计算属性、`timerTick` 驱动刷新）
  - 处理 `ControlEvent.TimerExpired`
  - 调用 `UpdateSceneControlSettingsUseCase`
- `ui/component/`：`SceneTimePanel`、`SceneSoundPanel`、`SceneVisualPanel`、`VelarisSwitch`、`TimerOptionButton` 等
- `SceneControlScreen.kt`、`SceneParticleScreen.kt`

**UiState 关键字段**：
- `timerMode`、`isTimerRunning`、`timerRemainingMillis`
- `alarmReminderEnabled`、`showCountdownClock`、`countdownClockPosition`
- `clockAudioVolume`、`brightness`、`darkness`、`particleSettings`

## 依赖关系

依赖 foundation:domain 的控制设置更新 UseCase，以及 model 中的新枚举（SceneTimerMode、SceneCountdownClockPosition）。

## 当前状态

- 功能非常完整，支持倒计时/时钟切换、自定义时长、闹钟提醒、粒子实时调节。
- Entry 已就绪，当前通过 scene 内部 `activePanel` 机制展示。

## 特点

- 定时器使用绝对时间戳（`timerEndTimestampMillis`）实现后台计时不漂移。
- 单独的粒子面板（SceneParticlePanel）。
- 通过 `ControlEvent` Channel 与上层通信 TimerExpired 事件。

---

文档基于实际代码。
