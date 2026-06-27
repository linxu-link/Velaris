# feature:lock

`:feature:lock` 提供应用锁屏功能，采用**独立 Activity** 实现（不走 Navigation3 NavKey 体系）。

## 模块概述

- **api 路径**：`feature/lock/api`
- **impl 路径**：`feature/lock/impl`
- **包名**：`com.wujia.feature.lock.api` / `com.wujia.feature.lock.impl`
- **特殊性**：这是目前唯一**不使用 NavKey** 的 feature。

## 核心 API 与实现

### api
- `LockScreenLauncher.kt`
  ```kotlin
  object LockScreenLauncher {
      fun createIntent(context: Context): Intent { ... }
  }
  ```
  通过显式类名 `"com.wujia.feature.lock.impl.ui.LockScreenActivity"` 启动。

### impl
- `ui/LockScreenActivity.kt`（独立 Activity）
- `ui/LockScreenContent.kt`（Compose 内容）
- `ui/LockScreenViewModel.kt`（@HiltViewModel）

## 当前状态

- 模块源码完整存在于仓库。
- 在 `settings.gradle.kts` 中被完全注释：
  ```kotlin
  //include(":feature:lock:api")
  //include(":feature:lock:impl")
  ```
- 因此目前**不参与编译和打包**。

## 设计考量

- 采用独立 Activity 而非 Nav3 的原因可能是为了实现系统级锁屏效果（覆盖状态栏、独立任务栈、FLAG_ACTIVITY_NEW_TASK 等）。
- 未来如果要集成，建议评估是否改为 NavKey + 全屏对话框/覆盖层方案，以保持导航一致性。

## 建议

- 如果需要启用锁屏功能：
  1. 取消 settings.gradle.kts 中的注释。
  2. 在需要的地方调用 `LockScreenLauncher.createIntent(context)` 启动。
  3. 考虑是否补充一个 `LockNavKey`（如果希望未来统一到 Nav3）。

---

此模块为特殊情况，文档单独说明。
