# `foundation:ui` 模块文档

> **模块路径**: `foundation/ui`
> **Android Namespace**: `com.wujia.foundation.ui`
> **Kotlin Package**: `com.wujia.foundation.ui`
> **Gradle Path**: `:foundation:ui`
> **最后更新**: 2026-06-01

---

## 1. 模块概述

### 1.1 模块定位

`foundation:ui` 是 Velaris 项目的**基础 UI 组件层**，位于 `foundation:designsystem`（设计系统）与各 `feature`（业务功能）模块之间，充当**桥梁**角色。它将 `designsystem` 提供的设计令牌（Design Tokens）、主题系统和原子组件，组合为更高层级的**可复用业务复合组件**，供 feature 模块直接调用。

应用名称为"**氛围时钟**"，口号为"在喧嚣中，寻一盏静光"，面向**中文用户**，主要服务于**横屏沉浸式场景音画**体验。

### 1.2 职责

| 职责 | 说明 |
|------|------|
| 业务卡片组件 | 封装 LogoCard、PlayerCard、TimeCard 等带毛玻璃背景的卡片 UI |
| 控制面板 | 提供 SeekbarControlPanel / SeekbarControlRow 等滑条混音控制面板 |
| 步骤流程 | StepHeader（步骤标题栏）、StepIndicator（步骤进度指示器） |
| 引导蒙层 | 提供 GuideOverlay / guideTarget 等通用引导高亮能力 |
| 媒体辅助 | rememberContentBitmap（异步 Bitmap 加载）、WaveformBar（音频波形可视化） |
| 装饰效果 | selectedBorderGlow（选中发光边框 Modifier 扩展） |
| 资源托管 | 存放大量业务字符串（strings.xml，193 条 string + 13 条 string-array）及 9 个自然主题矢量图标 |

### 1.3 命名空间

- **Android Namespace**: `com.wujia.foundation.ui`（定义于 `build.gradle.kts` 第 8 行）
- **Kotlin Package**: `com.wujia.foundation.ui`（所有源文件统一包名）
- **Build Plugins**:
  - `advance.android.library` -- Android 库基础配置
  - `advance.android.library.compose` -- Compose 编译支持
  - `advance.android.library.jacoco` -- 代码覆盖率

---

## 2. 架构设计

### 2.1 分层关系

```
┌───────────────────────────────────────────────────────────┐
│                    feature 模块层                          │
│  sceneEdit/impl   sceneControl/impl   scene/impl          │
│  sceneList/impl                                             │
│  app                                                        │
└────────────────────────┬──────────────────────────────────┘
                         │  implementation 依赖
┌────────────────────────▼──────────────────────────────────┐
│                foundation:ui  (本模块)                      │
│                                                            │
│  LogoCard  PlayerCard  TimeCard  SeekbarControlPanel       │
│  StepHeader  StepIndicator  GuideOverlay                   │
│  rememberContentBitmap  selectedBorderGlow                 │
│  ButtonControlPanel (空壳)                                 │
└──────┬──────────────────┬──────────────────┬──────────────┘
       │                  │                  │
┌──────▼──────────┐ ┌─────▼─────────┐ ┌─────▼──────────────┐
│ designsystem    │ │   toolkit     │ │     model          │
│ VelarisTheme    │ │ 工具函数库    │ │ SoundControlItem   │
│ 原子组件        │ │               │ │ 数据模型           │
└─────────────────┘ └───────────────┘ └────────────────────┘

foundation:data ──implementation──> foundation:ui (反向依赖，见缺陷 6.2)
```

### 2.2 Composable 组件模式

所有 UI 组件均遵循标准 Jetpack Compose 函数式组件规范：

- 使用 `@Composable` 注解声明，接受 `Modifier` 作为首参或命名参数，支持外部样式覆盖。
- 通过 `VelarisTheme.spec` 获取全局设计令牌，驱动所有尺寸、颜色、间距、透明度、圆角等视觉参数，**避免硬编码**。
- **默认参数模式**：关键 UI 文案、尺寸等均有合理默认值，调用方可按需覆盖。
- Preview 函数使用 `@Preview` 或 `@LandscapePreviews`（来自 designsystem 的自定义注解）进行 Android Studio 预览。

### 2.3 Modifier 扩展模式

模块通过 `Modifier` 扩展函数封装可复用的绘制逻辑：

- `selectedBorderGlow` 使用 `drawWithContent` 自定义绘制管线，在原有内容之上叠加选中态边框和发光效果。
- 利用 Android 原生 `BlurMaskFilter` 实现高斯模糊光晕，通过 `drawIntoCanvas` + `nativeCanvas` 桥接到原生 Canvas。
- 所有视觉参数（圆角、颜色、边框宽度、模糊半径）均通过函数参数暴露。

### 2.4 设计令牌驱动（VelarisTheme.spec）

本模块**不自行定义任何视觉常量**，全部视觉表现由 `VelarisTheme.spec` 统一下发。以下是 spec 中被引用的关键令牌类别：

| 令牌类别 | 引用字段示例 | 使用场景 |
|---------|-------------|---------|
| `spec.colors` | `surface`, `surfaceSoft`, `controlSurface`, `stroke`, `gold`, `goldBright`, `goldSoft`, `onGold`, `textPrimary`, `textSecondary`, `textMuted` | 全部组件的文字、背景、边框颜色 |
| `spec.typography` | `title`, `subtitle`, `sectionTitle`, `body`, `label`, `micro`, `controlValue` | 字号控制 |
| `spec.spacing` | `xSmall`, `small`, `medium`, `large`, `xLarge`, `edgeSmall` | 内外间距 |
| `spec.radii` | `panel`, `card` | 圆角半径 |
| `spec.size` | `stroke`, `iconMedium`, `controlSmall`, `stepIndicator`, `stepLineWidth` | 图标、控件尺寸 |
| `spec.alpha` | `textPrimary`, `textSecondary`, `textMuted`, `stroke`, `strokeMedium`, `strokeStrong`, `icon` | 各类元素透明度 |

### 2.5 与 designsystem 的关系

本模块强依赖 `foundation:designsystem`，使用其提供的：

| designsystem 导出 | 本模块使用场景 |
|---|---|
| `VelarisTheme.spec` | 所有组件的主题规范 |
| `VelarisGlassMaterial` / `velarisGlassBlur()` | 毛玻璃背景效果（PlayerCard、TimeCard、ButtonControlPanel） |
| `GlowCircleIconButton` | PlayerCard 中的播放/暂停按钮 |
| `TimerCircleButton` | TimeCard 中的定时器按钮 |
| `GlowSeekBar` | SeekbarControlPanel 中的滑块控件 |
| `AnimatedAudioBars` / `RealAudioBars` | WaveformBar 中的音频波形可视化 |
| `LandscapePreviews` | 横屏预览注解 |

---

## 3. 代码能力

### 3.1 位图异步加载（rememberContentBitmap）

- 通过 `LaunchedEffect(uri)` + `Dispatchers.IO` 实现协程驱动的异步图片解码。
- **两轮解码策略**：第一轮仅解码尺寸（`inJustDecodeBounds = true`），第二轮根据 `maxDimensionPx`（默认 2048px）计算 `inSampleSize` 后解码实际 Bitmap。
- URI 为 null 或空时直接返回 null，避免无效 IO。
- 内部 `calculateInSampleSize` 以 2 的幂次递增计算采样率。

### 3.2 LogoCard

- 应用品牌标识卡片，展示圆形图标 + 应用名称 + 标语。
- 圆形图标内显示 `app_tagline` 最后一个字符作为 Logo 占位。
- 支持可选点击事件（`onClick`），使用无涟漪 `clickable`（`indication = null`）。
- 通过 `showText` 参数控制是否显示文字部分。

### 3.3 PlayerCard

- 播放控制卡片，内嵌 `GlowCircleIconButton` 实现播放/暂停切换。
- 左侧圆形按钮根据 `isPlaying` 状态切换 `PlayArrow` / `Pause` 图标。
- 右侧文字区域点击后触发 `onTextClick`，显示混音标题 + `Tune` 图标（旋转 90 度）。
- 使用毛玻璃背景（`velarisGlassBlur`）和 `controlSurface` 颜色。

### 3.4 TimeCard

- 定时器卡片，内嵌 `TimerCircleButton` 显示环形进度。
- 展示标题（默认"定时关闭"）和时间文本（如 "45:00"）。
- 支持自定义图标（`ImageVector`），默认使用 `Icons.Outlined.Timer`。
- 注释中保留了被禁用的边框代码（第 59-63 行）。

### 3.5 StepHeader

- 步骤编辑页顶部标题栏，水平排列：主标题 + 步骤标题 + 可选"下一步"按钮。
- 主标题使用 `goldSoft` 配色，步骤标题使用 `textMuted` 配色。
- "下一步"按钮通过 `nextText` 参数控制显示，为 null 时隐藏；使用 `weight(1f)` 将其推到右侧。

### 3.6 StepIndicator

- 步骤进度指示器，**硬编码 4 步流程**：选择素材 -> 选择声音 -> 粒子效果 -> 预览保存。
- 每步由 `StepItem`（圆形数字标记 + 标题文本）组成，当前步骤高亮为金色（`spec.colors.gold`）。
- 步与步之间以 `StepLine`（水平线段）连接，使用 `strokeStrong` 透明度。

### 3.7 SeekbarControlPanel

- 声音混音控制面板，使用 `LazyColumn` 渲染 `SoundControlItem` 列表。
- 每行由 `SeekbarControlRow` 渲染：圆形图标 + 标题 + `GlowSeekBar` 滑条 + 百分比数值。
- **智能滚动渐隐**：通过 `derivedStateOf` 监听 `LazyListState`，在可继续上滑/下滑时动态显示顶部/底部渐变遮罩。
- 标题、字号均可通过参数覆盖，使用 `TextUnit.takeOrElse` 解析默认值。

### 3.8 SeekbarControlRow

- 单行声音控制项，由 `SeekbarControlPanel` 内部调用，也可独立使用。
- 图标背景为半透明圆形（`stroke.copy(alpha = 0.08f)`），图标着色为 `goldSoft.copy(alpha = 0.82f)`。
- 百分比数值通过 `(item.value * 100).roundToInt()` 计算，右对齐显示。
- 标题宽度约束为 `widthIn(min = 48.dp, max = 96.dp)`。

### 3.9 SelectedBorderGlow（Modifier 扩展）

- `Modifier.selectedBorderGlow(selected, cornerRadius, selectedColor, unselectedColor, ...)`.
- **选中态**：先用 `BlurMaskFilter`（`Blur.NORMAL`）绘制模糊发光描边（alpha=0.58），再绘制清晰边框（alpha=0.78）。
- **未选中态**：仅绘制未选中颜色的实线边框。
- 绘制时自动处理 inset 以确保描边不超出边界。

### 3.10 WaveformBar

- 音频波形可视化组件，展示一行描述文字（"风声 . 雪落声 . 壁炉声"）+ 波形条。
- 当 `audioSessionId > 0` 时使用 `RealAudioBars` 绑定 Visualizer 实现实时波形。
- 否则回退到 `AnimatedAudioBars` 展示装饰性动画波形。
- 声明了 `RECORD_AUDIO` 权限（AndroidManifest.xml）。

### 3.11 GuideOverlay / guideTarget

- `rememberGuideTargetState()` 创建目标状态对象，供目标元素和引导层共享。
- `Modifier.guideTarget(state)` 通过 `onGloballyPositioned` 记录目标元素在根布局中的位置。
- `GuideOverlay(...)` 负责绘制全屏遮罩、挖空高亮、说明卡片和弹簧箭头线，支持：
  - 自定义标题和描述文案
  - 自动 / 上下左右四种说明卡片摆放方式
  - 可选的附加 `content` 区域，用于扩展按钮或额外提示
- 当前高亮形状为圆角矩形，适合大多数按钮、卡片和面板入口。

### 3.12 ButtonControlPanel

- 当前为**空壳组件**：仅渲染一个带毛玻璃背景和边框的 `Box` 容器，内部没有任何子组件。
- 接口已定义：接受 `List<SoundControlItem>` 和 `onValueChange` 回调，但未实际使用。
- Preview 函数同样为空。

---

## 4. 对外暴露的接口

### 4.1 Public Composable 函数

| 函数名 | 文件路径 | 行号 | 签名摘要 |
|--------|---------|------|---------|
| `rememberContentBitmap` | `src/main/java/com/wujia/foundation/ui/RememberContentBitmap.kt` | **L19** | `fun rememberContentBitmap(uri: String?, maxDimensionPx: Int = 2048): Bitmap?` |
| `ButtonControlPanel` | `src/main/java/com/wujia/foundation/ui/ButtonControlPanel.kt` | **L19** | `fun ButtonControlPanel(items: List<SoundControlItem>, onValueChange: (Int, Float) -> Unit, modifier: Modifier, title: String)` |
| `LogoCard` | `src/main/java/com/wujia/foundation/ui/LogoCard.kt` | **L40** | `fun LogoCard(modifier: Modifier, minHeight: Dp, iconSize: Dp, showText: Boolean, onClick: (() -> Unit)?)` |
| `PlayerCard` | `src/main/java/com/wujia/foundation/ui/PlayerCard.kt` | **L45** | `fun PlayerCard(modifier: Modifier, isPlaying: Boolean, onIconClick: () -> Unit, onTextClick: () -> Unit, title: String, minWidth: Dp, minHeight: Dp, controlSize: Dp)` |
| `TimeCard` | `src/main/java/com/wujia/foundation/ui/TimeCard.kt` | **L41** | `fun TimeCard(modifier: Modifier, isRunning: Boolean, progress: Float, timeText: String, onToggleClick: () -> Unit, title: String, icon: ImageVector, minWidth: Dp, minHeight: Dp, controlSize: Dp)` |
| `SeekbarControlPanel` | `src/main/java/com/wujia/foundation/ui/SeekbarControlPanel.kt` | **L63** | `fun SeekbarControlPanel(items: List<SoundControlItem>, onValueChange: (Int, Float) -> Unit, modifier: Modifier, title: String, minWidth: Dp, minHeight: Dp, titleFontSize: TextUnit, itemTitleFontSize: TextUnit, valueFontSize: TextUnit)` |
| `SeekbarControlRow` | `src/main/java/com/wujia/foundation/ui/SeekbarControlPanel.kt` | **L196** | `fun SeekbarControlRow(item: SoundControlItem, onValueChange: (Float) -> Unit, modifier: Modifier, titleFontSize: TextUnit, valueFontSize: TextUnit)` |
| `StepHeader` | `src/main/java/com/wujia/foundation/ui/StepHeader.kt` | **L25** | `fun StepHeader(modifier: Modifier, title: String, stepTitle: String, nextText: String?, onNextClick: () -> Unit)` |
| `StepIndicator` | `src/main/java/com/wujia/foundation/ui/StepIndicator.kt` | **L33** | `fun StepIndicator(currentStep: Int)` |
| `WaveformBar` | `src/main/java/com/wujia/foundation/ui/WaveformBar.kt` | **L33** | `fun WaveformBar(modifier: Modifier, audioSessionId: Int?, barHeight: Dp, waveformWidth: Dp)` |
| `GuideOverlay` | `src/main/java/com/wujia/foundation/ui/guide/GuideOverlay.kt` | 新增 | `fun GuideOverlay(targetState: GuideTargetState, description: String, ...)` |
| `rememberGuideTargetState` | `src/main/java/com/wujia/foundation/ui/guide/GuideTarget.kt` | 新增 | `fun rememberGuideTargetState(): GuideTargetState` |

### 4.2 Public Modifier 扩展函数

| 函数名 | 文件路径 | 行号 | 签名 |
|--------|---------|------|------|
| `selectedBorderGlow` | `src/main/java/com/wujia/foundation/ui/SelectedBorderGlow.kt` | **L16** | `fun Modifier.selectedBorderGlow(selected: Boolean, cornerRadius: Dp, selectedColor: Color, unselectedColor: Color, borderWidth: Dp = 1.dp, glowStrokeWidth: Dp = 5.dp, glowBlurRadius: Dp = 7.dp): Modifier` |
| `guideTarget` | `src/main/java/com/wujia/foundation/ui/guide/GuideTarget.kt` | 新增 | `fun Modifier.guideTarget(state: GuideTargetState): Modifier` |

### 4.3 R.string 资源

strings.xml 位于 `src/main/res/values/strings.xml`（310 行），共 **193 条** `<string>` 条目，按功能分类如下：

| 分类 | 资源名前缀 | 数量 | 示例 |
|------|-----------|------|------|
| 应用信息 | `app_` | 2 | `app_name`（氛围时钟）、`app_tagline`（在喧嚣中，寻一盏静光） |
| 通用操作 | `common_` | 11 | `common_cancel`、`common_confirm`、`common_play`、`common_pause`、`common_save`、`common_next` 等 |
| 天气相关 | `weather_` | 10 | `weather_rain`、`weather_snow`、`weather_effect`、`weather_intensity`、`weather_wind` 等 |
| 播放器相关 | `player_` | 5 | `player_mix`、`player_sound`、`player_wind`、`player_rain`、`player_wind_snow_fireplace` |
| 定时器相关 | `timer_` | 12 | `timer_close`、`timer_15min`、`timer_25min`、`timer_45min`、`timer_custom`、`timer_label` 等 |
| 场景列表 | `scene_list_` | 4 | `scene_list_title`、`scene_list_add_scene`、`scene_list_delete_scene`、`scene_list_drag_hint` |
| 场景编辑 | `scene_edit_` | 38 | `scene_edit_title`、`scene_edit_step1`~`step4`、`scene_edit_preview`、`scene_edit_save_new` 等 |
| 场景控制 | `scene_control_` | 4 | `scene_control_settings`、`scene_control_brightness`、`scene_control_dark`、`scene_control_sound` |
| 设置页面 | `settings_` | 6 | `settings_title`、`settings_playback`、`settings_privacy`、`settings_about` 等 |
| 关于对话框 | `about_` | 4 | `about_tagline`、`about_version`、`about_author`、`about_license` |
| 播放设置 | `playback_` | 6 | `playback_power_saver`、`playback_balanced`、`playback_quality` 及其 `_desc` |
| 隐私对话框 | `privacy_` | 4 | `privacy_no_collect`、`privacy_google_ads`、`privacy_consent_management`、`privacy_manage_options` |
| 声音分类 | `sound_` | 5 | `sound_nature`、`sound_healing`、`sound_focus`、`sound_sleep`、`sound_reading` |
| 步骤指示 | `step_` | 2 | `step_add_scene`、`step1_title` |
| Tab 标签 | `tab_` | 2 | `tab_focus_scene`、`tab_sleep_scene` |
| 收藏 | `favorite` | 2 | `favorite_scene`、`favorite` |
| 提示/同步 | `hint_` / `sync_` | 2 | `hint_swipe_up`、`sync_data` |
| 种子数据-场景 | `seed_scene_` | 8 | `seed_scene_morning_mist_title`、`seed_scene_snow_night_subtitle` 等 |
| 种子数据-音频 | `seed_audio_` | 4 | `seed_audio_rain_title`、`seed_audio_thunder_title`、`seed_audio_fireplace_title`、`seed_audio_wind_title` |
| 种子数据-噪音 | `seed_noise_` | 12 | `seed_noise_rain_title`、`seed_noise_rain_description`、`seed_noise_fireplace_title` 等 |
| 种子数据-视频 | `seed_video_` | 6 | `seed_video_video1_title`~`video3_title` 及 `_description` |
| 种子数据-背景 | `seed_background_` | 8 | `seed_background_morning_mist_title`、`seed_background_snow_night_description` 等 |
| 种子数据-粒子 | `seed_particle_` | 16 | `seed_particle_light_rain_title`、`seed_particle_blizzard_description`、`seed_particle_no_particle_title` 等 |

### 4.4 R.drawable 资源

共 **9 个**矢量图标（`res/drawable/`），均为 200dp x 200dp（viewport 1024x1024）的自然白噪音主题图标：

| 资源名 | 描述 | 填充色 |
|--------|------|--------|
| `ic_bird` | 鸟鸣 | `#FF000000` |
| `ic_fireplace` | 壁炉 | `#4F4F4F` |
| `ic_ocean` | 海浪 | `#565654` |
| `ic_rain` | 雨滴 | `#FF000000` |
| `ic_snow` | 雪花 | `#565654` |
| `ic_stream` | 流水 | `#FF000000` |
| `ic_thunder` | 闪电 | `#272636` |
| `ic_train` | 火车 | `#FF000000` |
| `ic_wind` | 风 | `#FF000000` |

### 4.5 R.array 资源

共 **13 条** `<string-array>` 条目，用于种子数据的标签分类（每条 3 个 item）：

| 资源名 | 用途 | 标签内容 |
|--------|------|---------|
| `seed_noise_rain_tags` | 雨声 | 雨声、自然、阅读 |
| `seed_noise_fireplace_tags` | 炉火 | 炉火、治愈、放松 |
| `seed_noise_wind_tags` | 风声 | 风声、自然、清醒 |
| `seed_noise_ocean_tags` | 海浪 | 海浪、助眠、放松 |
| `seed_noise_piano_tags` | 钢琴 | 钢琴、专注、阅读 |
| `seed_noise_thunder_tags` | 雷声 | 雷声、助眠、雨夜 |
| `seed_particle_light_rain_tags` | 细雨粒子 | 细雨、雨天、阅读 |
| `seed_particle_moderate_rain_tags` | 中雨粒子 | 中雨、雨天、沉浸 |
| `seed_particle_heavy_rain_tags` | 暴雨粒子 | 暴雨、雷雨、氛围 |
| `seed_particle_light_snow_tags` | 小雪粒子 | 小雪、雪天、宁静 |
| `seed_particle_moderate_snow_tags` | 中雪粒子 | 中雪、雪天、冬日 |
| `seed_particle_blizzard_tags` | 暴风雪粒子 | 暴风雪、极端、暴风雨 |
| `seed_particle_no_particle_tags` | 无效果粒子 | 无效果、纯净、简洁 |

### 4.6 权限声明

AndroidManifest.xml 声明了以下权限（会自动合并到 app 模块）：

- `android.permission.RECORD_AUDIO` -- 用于 WaveformBar 绑定 Visualizer 实现实时音频波形。

---

## 5. 依赖关系

### 5.1 上游依赖（本模块依赖）

```kotlin
// foundation/ui/build.gradle.kts
dependencies {
    // 项目内部模块
    implementation(projects.foundation.designsystem)  // 主题系统、原子组件、毛玻璃效果
    implementation(projects.foundation.toolkit)        // 工具函数库
    implementation(projects.foundation.model)          // SoundControlItem 数据模型

    // Jetpack Compose
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
```

### 5.2 下游依赖（依赖本模块的模块）

| 模块 | Gradle 路径 | 依赖方式 | 构建文件位置 |
|------|------------|---------|-------------|
| `app` | `:app` | `implementation(projects.foundation.ui)` | `app/build.gradle.kts:70` |
| `feature:sceneEdit:impl` | `:feature:sceneEdit:impl` | `implementation(projects.foundation.ui)` | `feature/sceneEdit/impl/build.gradle.kts:15` |
| `feature:sceneList:impl` | `:feature:sceneList:impl` | `implementation(projects.foundation.ui)` | `feature/sceneList/impl/build.gradle.kts:16` |
| `feature:sceneControl:impl` | `:feature:sceneControl:impl` | `implementation(projects.foundation.ui)` | `feature/sceneControl/impl/build.gradle.kts:14` |
| `feature:scene:impl` | `:feature:scene:impl` | `implementation(projects.foundation.ui)` | `feature/scene/impl/build.gradle.kts:20` |
| `foundation:data` | `:foundation:data` | `implementation(projects.foundation.ui)` | `foundation/data/build.gradle.kts:16` |

---

## 6. 当前缺陷和改进建议

### 6.1 ButtonControlPanel 空壳实现

**问题**：`ButtonControlPanel.kt`（第 37-40 行）的 `Box` 容器内没有任何子组件，`items` 和 `onValueChange` 参数已声明但从未使用。Preview 函数 `ButtonControlPanelPreview` 同样为空。

**影响**：该组件目前无法正常工作，调用方只能看到一个透明毛玻璃容器。

**建议**：实现内部按钮网格布局（类似 SeekbarControlPanel 的按钮式版本），或标记为 `@Deprecated` 并在 KDoc 中说明待实现。

### 6.2 strings.xml 职责膨胀（data -> ui 反向依赖）

**问题**：`strings.xml` 包含 193 条 string 和 13 条 string-array（共 310 行），其中大量条目属于**种子数据**（`seed_*` 共 54 条）和**业务功能文案**（`scene_edit_*` 38 条、`settings_*` 6 条、`privacy_*` 4 条、`playback_*` 6 条等），远超 `foundation:ui` 作为 UI 组件层的职责范围。

**影响**：
- `foundation:data` 模块依赖 `foundation:ui`（仅为访问 `R.string.seed_*` 种子数据资源），形成 **data -> ui 的反向依赖**，违反分层架构中"下层不应依赖上层"的原则。
- 无法对 UI 组件和业务文案独立进行变更管理。
- `foundation:ui` 的编译时间受大量无关资源影响。

**建议**：
1. 将种子数据字符串（`seed_*`）迁移至 `foundation:data` 模块自己的 `strings.xml`，消除反向依赖。
2. 将业务功能文案（`scene_edit_*`、`scene_list_*`、`settings_*`、`playback_*`、`privacy_*`、`about_*`、`scene_control_*`、`tab_*`、`favorite_*`、`hint_*`、`sync_*`、`seed_*`）迁移至对应 feature 模块或共享的 `common:resources` 模块。
3. 仅保留组件内部引用的字符串在 `foundation:ui`（约 20 条：`app_name`、`app_tagline`、`player_mix`、`player_sound`、`timer_close`、`timer_label`、`step_add_scene`、`scene_edit_step1`~`step4`、`player_wind_snow_fireplace`、`common_play`、`common_pause` 等）。

### 6.3 ~~遗留调试日志~~

已解决。`WaveformBar.kt` 的 `Log.d` 和 `RememberContentBitmap.kt` 的 `Log.w` 已全部移除，项目中不再使用 `android.util.Log`。

### 6.4 Bitmap 缺少生命周期管理

**问题**：`rememberContentBitmap` 返回的 `Bitmap` 对象没有绑定 Compose 的生命周期。当 Composable 离开组合树后，`Bitmap` 不会被主动回收（`recycle()`），可能导致内存泄漏，尤其在加载大图场景下。

**建议**：
1. 使用 `DisposableEffect` 在 Composable 离开组合时调用 `bitmap?.recycle()`。
2. 或者使用 Coil / Glide 等图片加载框架的 Compose 集成，自动管理生命周期和内存缓存。
3. 考虑使用 `ImageBitmap` 替代原生 `Bitmap` 以更好地与 Compose 图形系统集成。

### 6.5 Alpha 值硬编码

**问题**：`SeekbarControlPanel.kt` 和 `SelectedBorderGlow.kt` 中存在多处硬编码的 alpha 值，未使用 `VelarisTheme.spec.alpha` 中的令牌：

| 文件 | 行号 | 硬编码值 | 用途 | 建议 |
|------|------|---------|------|------|
| `SeekbarControlPanel.kt` | L216 | `0.08f` | 图标圆形背景透明度 | 新增 `spec.alpha.iconBackground` |
| `SeekbarControlPanel.kt` | L222 | `0.82f` | 图标着色透明度 | 新增 `spec.alpha.iconBright` |
| `SeekbarControlPanel.kt` | L231 | `0.78f` | 标题文字透明度 | 复用 `spec.alpha.textSecondary` |
| `SeekbarControlPanel.kt` | L254 | `0.6f` | 百分比文字透明度 | 新增 `spec.alpha.textTertiary` |
| `SelectedBorderGlow.kt` | L35 | `0.58f` | 光晕描边透明度 | 作为参数暴露或新增令牌 |
| `SelectedBorderGlow.kt` | L54 | `0.78f` | 选中边框透明度 | 作为参数暴露或新增令牌 |

**建议**：将这些值提取到 `VelarisTheme.spec.alpha` 中统一管理，或至少作为组件参数暴露，避免主题变更时遗漏修改。

### 6.6 StepIndicator 硬编码 4 步

**问题**：`StepIndicator` 组件（`StepIndicator.kt` 第 41-54 行）硬编码了 4 个步骤（选择素材、选择声音、粒子效果、预览保存），步骤数量、标题和资源引用均不可由调用方配置。

**影响**：如果业务流程发生变化（增加/减少步骤），必须修改 `foundation:ui` 模块源码，违反开闭原则。

**建议**：将步骤列表参数化：

```kotlin
data class StepConfig(val number: Int, val title: String)

@Composable
fun StepIndicator(
    currentStep: Int,
    steps: List<StepConfig>,
    modifier: Modifier = Modifier
)
```

### 6.7 其他问题

| 问题 | 位置 | 说明 | 建议 |
|------|------|------|------|
| Preview 命名冲突 | `LogoCard.kt:31` / `PlayerCard.kt:37` | 两个文件的 Preview 函数都命名为 `Preview_DefaultPlayerCard` | 修正 `LogoCard.kt` 中的命名为 `Preview_LogoCard` |
| TimeCard 注释代码 | `TimeCard.kt:59-63` | 被注释掉的 border 代码 | 清理或恢复，避免代码腐化 |
| AndroidManifest 权限 | `AndroidManifest.xml:4` | `RECORD_AUDIO` 在 library manifest 中声明，自动合并到所有使用方 | 确认是否所有下游模块都需要此权限，考虑移至 WaveformBar 使用方 |
| 单元测试缺失 | `ExampleUnitTest.kt` | 仅有自动生成的占位测试（`2 + 2 = 4`） | 补充 `calculateInSampleSize` 等纯逻辑的单元测试 |
| LazyColumn key 唯一性 | `SeekbarControlPanel.kt:143` | `key = { _, item -> item.title }` 使用 title 作为 key | 若存在同名 item 将导致崩溃，建议使用唯一 ID |
| SelectedBorderGlow API | `SelectedBorderGlow.kt` | 使用 `android.graphics.Paint` + `BlurMaskFilter` 原生 API | 考虑使用 Compose `graphicsLayer` + `renderEffect` 替代 |

---

## 7. 代码统计

### 7.1 文件统计

| 类别 | 文件数 | 行数 |
|------|--------|------|
| Kotlin 源文件（main） | 10 | 1,011 |
| Kotlin 测试文件（test） | 1 | 16 |
| AndroidManifest.xml | 1 | 5 |
| strings.xml | 1 | 310 |
| Drawable XML（矢量图标） | 9 | 219 |
| **合计** | **22** | **1,561** |

### 7.2 Kotlin 源文件明细（共 1,011 行）

| 文件 | 行数 | 主要内容 |
|------|------|---------|
| `SeekbarControlPanel.kt` | 286 | `SeekbarControlPanel` + `SeekbarControlRow` + Preview |
| `StepIndicator.kt` | 111 | `StepIndicator` + `StepItem`(private) + `StepLine`(private) |
| `LogoCard.kt` | 106 | `LogoCard` + Preview |
| `TimeCard.kt` | 103 | `TimeCard` + Preview |
| `PlayerCard.kt` | 102 | `PlayerCard` + Preview |
| `WaveformBar.kt` | 70 | `WaveformBar` |
| `RememberContentBitmap.kt` | 66 | `rememberContentBitmap` + `calculateInSampleSize`(private) |
| `StepHeader.kt` | 62 | `StepHeader` + Preview |
| `SelectedBorderGlow.kt` | 58 | `Modifier.selectedBorderGlow` 扩展 |
| `ButtonControlPanel.kt` | 47 | `ButtonControlPanel`（空壳）+ Preview（空） |

### 7.3 组件统计

| 类型 | 数量 | 列表 |
|------|------|------|
| Public Composable 函数 | 10 | `rememberContentBitmap`, `ButtonControlPanel`, `LogoCard`, `PlayerCard`, `TimeCard`, `SeekbarControlPanel`, `SeekbarControlRow`, `StepHeader`, `StepIndicator`, `WaveformBar` |
| Public Modifier 扩展 | 1 | `selectedBorderGlow` |
| Private Composable 函数 | 3 | `StepItem`, `StepLine`, `calculateInSampleSize` |
| Preview 函数 | 5 | `ButtonControlPanelPreview`, `Preview_DefaultPlayerCard`(x2), `StepHeaderPreview`, `SeekbarControlPanelPreview` |

### 7.4 资源统计

| 资源类型 | 条目数 |
|---------|--------|
| R.string（`<string>`） | 193 |
| R.array（`<string-array>`，共 39 个 item） | 13 |
| R.drawable（矢量图标） | 9 |
| 权限声明（`RECORD_AUDIO`） | 1 |
