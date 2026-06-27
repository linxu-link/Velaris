# foundation:designsystem 模块文档

## 1. 模块概述

`foundation:designsystem` 是 Velaris 应用的基础设计系统模块，提供全局统一的视觉语言、主题规范和可复用 UI 组件。该模块定义了从色值、字号、间距到按钮、滑条、标签页、翻页器、面板等一整套 Compose 设计规范，是所有上层业务模块的视觉基础。

- **包名**: `com.wujia.foundation.designsystem`
- **类型**: Android Library (`advance.android.library` + `compose` 插件)
- **总文件数**: 20 个 Kotlin 源文件
- **总代码行数**: 约 2896 行
- **设计风格**: 深色主题 + 冷色强调 + 毛玻璃质感 (Glassmorphism)

---

## 2. 架构设计

### 2.1 双层主题架构

本模块采用 **Material3 + VelarisUiSpec** 双层主题架构，两层各司其职：

```
ProvideVelarisTheme          (入口 Composable)
    |
    +-- MaterialTheme        (框架层: M3 colorScheme / Typography，仅作为初始化占位)
         |
         +-- ProvideVelarisUiSpec   (业务层: VelarisUiSpec 通过 CompositionLocal 注入)
              |
              +-- 业务 Composable
```

- **Material3 层** (`MaterialTheme`): 作为 Jetpack Compose 框架初始化用途。`colorScheme` 采用与项目一致的静态冷色方案，`Typography` 仅定义了 `bodyLarge`。实际 UI 样式主要仍由自定义 token 控制。
- **VelarisUiSpec 层** (`ProvideVelarisUiSpec`): 通过 `CompositionLocal` (`LocalVelarisUiSpec`) 注入自定义设计 token。所有业务组件通过 `VelarisTheme.spec` 获取设计规范，控制颜色、圆角、间距、尺寸、字号、发光参数和渐变画刷。

这种设计的好处是：
1. 不依赖 M3 的 Material You 动态取色，确保品牌色一致性；
2. 自定义 token 结构清晰，可以整体替换主题；
3. M3 框架仍可用于其基础设施（如 `Scaffold`、`Surface`），不受影响。

### 2.2 毛玻璃体系 (Haze)

毛玻璃效果基于第三方库 [Haze](https://github.com/chrisbanes/haze) 实现，通过 `VelarisBlurState` + `CompositionLocal` 管理模糊状态：

```
VelarisBlurState (包装 HazeState)
    |
    +-- LocalVelarisBlurState  (CompositionLocal)
    |
    +-- Modifier.velarisBlurSource()   -- 标记模糊源（背景层）
    +-- Modifier.velarisGlassBlur()    -- 应用毛玻璃效果（前景层）
```

提供两种玻璃材质：
- `VelarisGlassMaterial.Thin` -- 薄型 (blurRadius = 4dp)，适用于列表项、工具栏
- `VelarisGlassMaterial.Regular` -- 常规型 (blurRadius = 8dp)，适用于弹窗、底部面板

### 2.3 包结构

```
com.wujia.foundation.designsystem/
  +-- theme/       主题层: 色值、字号、规格、模糊、弹窗视觉、主题入口
  +-- button/      按钮组件: 发光圆形按钮、圆角按钮、倒计时按钮
  +-- bar/         滑条与音频条: GlowSeekBar、AudioVisualizer
  +-- clock/       翻页时钟: FlipCountdownClock
  +-- icon/        图标组件: 呼吸发光图标
  +-- tab/         标签组件: 分段式标签页
  +-- pager/       翻页组件: 堆叠滚动翻页器、竖向翻页层
  +-- panel/       面板组件: 上滑面板
  +-- preview/     预览注解: 横屏预览注解
  +-- layout/      布局辅助: 横屏布局类型枚举
```

---

## 3. 核心类/接口

### 3.1 主题层 (theme/)

#### 3.1.1 VelarisUiSpec (170 行)

**文件**: `theme/VelarisUiSpec.kt`

主题规格的核心数据类，标记为 `@Immutable`。包含 8 个子数据类：

| 子类 | 职责 | 关键字段 |
|------|------|----------|
| `VelarisColors` | 颜色语义化 | `gold`, `goldSoft`, `goldBright`, `textPrimary`, `surface`, `stroke` 等 13 个颜色 token（名称沿用历史命名，实际已调整为冷色强调体系） |
| `VelarisAlpha` | 透明度分层 | `textPrimary(0.92f)`, `glowIdle(0.18f)`, `glowActive(0.28f)` 等 13 个透明度档位 |
| `VelarisRadii` | 圆角规范 | `panel(20dp)`, `card(28dp)`, `pill(50dp)`, `thumbnail(8dp)`, `badge(6dp)` |
| `VelarisSpacing` | 间距规范 | `xSmall(4dp)`, `small(8dp)`, `medium(12dp)`, `large(16dp)`, `xLarge(24dp)` 及边缘间距 |
| `VelarisSize` | 尺寸规范 | `stroke(1dp)`, `iconSmall(18dp)`, `iconMedium(24dp)`, `controlCompact(48dp)` 等 13 个尺寸 |
| `VelarisTypography` | 字号语义化 | `display`, `title`, `body`, `caption`, `tab` 等 12 个字号 token |
| `VelarisGlow` | 发光参数 | `circleRadiusScale(0.62f)`, `controlBlurRadius(10dp)`, `buttonBlurRadius(4dp)` |
| `VelarisBrushes` | 渐变画刷 | `selectedPill` (选中胶囊渐变), `sceneOverlay` (场景叠加), `glassSurface` (玻璃表面) |

**对外接口**:
- `DefaultVelarisUiSpec` -- 默认主题实例
- `LocalVelarisUiSpec` -- `staticCompositionLocalOf` 实例
- `VelarisTheme.spec` -- `@Composable @ReadOnlyComposable` 属性访问器
- `ProvideVelarisUiSpec(spec, content)` -- CompositionLocal 注入函数

#### 3.1.2 VelarisColor (194 行)

**文件**: `theme/VelarisColor.kt`

全局色值常量 `object`，按功能分类管理约 50 个色值常量：

| 分类 | 示例 |
|------|------|
| 强调色系 | `Gold(#6ED0D6)`, `GoldSoft(#94B9C6)`, `GoldBright(#F2F7FA)`, `OnGold(#0F1A23)` |
| 文字色 | `TextMuted(White)`, `IconMuted(#9DA8B5)` |
| 场景表面色 | `Surface(70%透明)`, `SurfaceSoft(40%)`, `SurfaceSubtle(32%)`, `ControlSurface(15%)` |
| 弹窗表面色 | `DialogSurface(94%)`, `DialogSurfaceSoft(90%)`, `DialogSurfaceSubtle(80%)`, `DialogControlSurface(85%)` |
| 毛玻璃色 | `GlassThin*` / `GlassRegular*` 系列 (Background / Tint / Fallback) |
| 渐变色 | `GradientPillStart/End`, `GradientGlassStart/End` |
| 粒子效果色 | `ParticleNone`, `ParticleRain`, `ParticleSnow`, `ParticleWhite` |
| 场景调色板 | `PaletteIce`, `PaletteForest`, `PaletteAmber`, `PaletteTwilight`, `PaletteSpa` |
| 背景渐变 | `BgGradientDarkBlue`, `BgGradientDarkSlate` |
| 预览渐变 | `PreviewGreenDark/Deep`, `MaterialSlateDark/NearBlack` 等 |
| M3 兼容色 | `M3Purple80/40`, `M3PurpleGrey80/40`, `M3Pink80/40`（保留为兼容字段，默认不参与业务视觉） |

#### 3.1.3 VelarisFontSize (133 行)

**文件**: `theme/VelarisFontSize.kt`

全局字号常量 `object`，约 31 个字号常量，按功能分类：

| 分类 | 常量 |
|------|------|
| 标题字号 | `Display(36sp)`, `ValueLarge(28sp)`, `SceneTitleLarge/Medium/Small`, `PreviewTitle(42sp)`, `Title(18sp)`, `Subtitle(18sp)` |
| 正文字号 | `SectionTitle(16sp)`, `ControlValue(17sp)`, `SeekPercent(20sp)`, `Body(15sp)` |
| 辅助字号 | `BodySmall(12sp)`, `Label(13sp)`, `Caption(11sp)`, `Micro(10sp)` |
| 横屏自适应 | `ControlTitleSmall/Medium/Large`, `ControlItemTitle*`, `ControlValue*`, `ControlSceneName*` |
| 场景自适应 | `SceneTitleCompactWidth(20sp)`, `SceneTitleCompactHeight(22sp)` |

#### 3.1.4 VelarisTheme (86 行)

**文件**: `theme/VelarisTheme.kt`

主题入口 Composable。逻辑流程：
1. 根据 `darkTheme` 选择静态 M3 `colorScheme`
2. 创建 `MaterialTheme`，配置统一色彩和 `M3Typography`
3. 在 `MaterialTheme` 内嵌套 `ProvideVelarisUiSpec`，注入 `DefaultVelarisUiSpec`

#### 3.1.5 VelarisBlur (99 行)

**文件**: `theme/VelarisBlur.kt`

毛玻璃封装层，核心类型和函数：

| 名称 | 类型 | 说明 |
|------|------|------|
| `VelarisBlurState` | class | 包装 `HazeState`，提供类型安全的模糊状态 |
| `LocalVelarisBlurState` | CompositionLocal | 可空的模糊状态，null 表示未启用模糊 |
| `rememberVelarisBlurState()` | @Composable | 创建并记住 `VelarisBlurState` |
| `ProvideVelarisBlurState(state, content)` | @Composable | 注入模糊状态 |
| `Modifier.velarisBlurSource(state)` | Extension | 标记模糊源 |
| `Modifier.velarisGlassBlur(material)` | Extension | 应用毛玻璃效果 |
| `VelarisGlassMaterial` | enum | `Thin` / `Regular` 两种材质 |

#### 3.1.6 VelarisDialogVisuals (39 行)

**文件**: `theme/VelarisDialogVisuals.kt`

弹窗专用视觉包裹函数 `ProvideVelarisDialogVisuals`：
- 禁用模糊 (`ProvideVelarisBlurState(null)`)
- 将 surface 颜色替换为弹窗专用色 (`DialogSurface` / `DialogSurfaceSoft` 等)
- 通过 `spec.copy()` 创建新的 `VelarisUiSpec`，不污染全局主题

---

### 3.2 按钮组件 (button/)

#### 3.2.1 GlowCircleIconButton (127 行)

**文件**: `button/IconButton.kt`

发光圆形图标按钮，用于播放/暂停等核心操作。

```
GlowCircleIconButton(selected, onSelectedChange, icon, size, contentDescription)
```

- 外层: 径向渐变发光圈（冷色，根据 `selected` 状态动画变化 alpha）
- 内层: 磨砂玻璃圆 + 冷色描边 + 图标
- 交互: `clickable` 切换 `selected` 状态，无涟漪效果
- 无障碍: `Role.Button` + `contentDescription`
- 动画: `animateFloatAsState` 控制发光 alpha，`tween(220ms)`

#### 3.2.2 RadiusIconButton (102 行)

**文件**: `button/RadiusIconButton.kt`

圆角矩形图标按钮，用于标签/收藏等操作。

```
RadiusIconButton(item: SceneTabItem, cornerRadius, contentPadding, selectedBrush)
```

- 展示 `SceneTabItem` 的文本和可选图标
- 使用 `selectedBrush` 渐变背景（默认为 `selectedPill` 渐变）
- 图标使用 `BreathingGlowIcon` 实现呼吸发光效果

#### 3.2.3 TimerCircleButton (178 行)

**文件**: `button/TimerCircleButton.kt`

带倒计时圆环的圆形按钮，复用 `GlowCircleIconButton` 的视觉结构。

```
TimerCircleButton(isRunning, progress, onToggleClick, icon, size, contentDescription)
```

- 外层: 发光 + 磨砂玻璃圆
- 进度圆环: Canvas 绘制背景轨道 + 进度弧（`StrokeCap.Round`）
- `progress: Float` 范围 0f~1f，`animateFloatAsState` 动画过渡
- 无障碍: `stateDescription` 显示 "剩余 XX%" 或 "已停止"

---

### 3.3 滑条与音频条 (bar/)

#### 3.3.1 GlowSeekBar (233 行)

**文件**: `bar/GlowSeekbar.kt`

带发光效果的滑动条。

```
GlowSeekBar(value, onValueChange, trackHeight, thumbRadius, glowRadius, ...)
```

- 纯 Canvas 绘制，无 Material Slider 组件
- 轨道: 圆角矩形，非活跃部分纯色，活跃部分水平渐变
- 流动高光: 无限循环 `infiniteRepeatable` 动画，在活跃轨道上流动
- 拇指: 实色圆 + 径向渐变发光圈
- 拖拽发光: 拖拽时 `glowScale` 放大至 1.25x，`glowAlpha` 增强
- 无障碍: `ProgressBarRangeInfo` + `setProgress` + `stateDescription`
- 手势: `detectDragGestures` 直接处理

#### 3.3.2 音频波形条（已移除）

历史上该位置曾提供 `AudioBars`、`AnimatedAudioBars`、`WaveformTrack` 和 `RealAudioBars` 等波形条组件。
当前仓库中这套组件已移除，音频实时可视化仅保留 `AudioVisualizer` 工具类供上层按需实现。

#### 3.3.3 AudioVisualizer (79 行)

**文件**: `bar/AudioVisualizer.kt`

Android `Visualizer` API 的封装类。

```
class AudioVisualizer(audioSessionId: Int, onLevelsChanged: (List<Float>) -> Unit)
```

- `start(barCount)` -- 启动波形捕获，将 byte 数据切分为 `barCount` 个归一化电平值 (0f~1f)。返回 `Boolean` 表示是否成功
- `stop()` -- 释放资源
- 使用 `Visualizer.getMaxCaptureRate() / 2` 采样率，仅监听波形数据 (非 FFT)
- 内置 `try-catch` 容错，启动失败时自动 stop

---

#### 3.3.4 FlipCountdownClock (约 615 行)

**文件**: `clock/FlipCountdownClock.kt`

日历式上下翻页的倒计时时钟组件。

```
FlipCountdownClock(time, units, cardWidth, cardHeight, digitGap, unitGap, separatorGap, cornerRadius, ...)
```

- 支持按需配置显示 `Hour` / `Minute` / `Second`
- 卡片为淡白色半透明表面，适合叠加在深色背景上
- 默认使用直角数字卡片，不做圆角处理
- 每个时间单位内部按数字位拆分，只有发生变化的数字位会触发翻牌
- 翻页方式采用 Split Flap 结构：旧数字上半片先翻，新数字下半片随后接上
- `FlipClockTime.fromTotalSeconds()` 提供总秒数到时分秒的转换工具

---

### 3.4 图标组件 (icon/)

#### 3.4.1 BreathingGlowIcon (88 行)

**文件**: `icon/BreathingGlowIcon.kt`

带呼吸发光动画的图标组件。

```
BreathingGlowIcon(imageVector, selected, contentDescription, selectedTint, unselectedTint, glowColor, iconSize, glowSize)
```

- 选中态: 径向渐变发光背景，alpha 在 `iconMinAlpha` ~ `iconMaxAlpha` 之间以 1800ms 周期呼吸闪烁 (`FastOutSlowInEasing`, `RepeatMode.Reverse`)
- 非选中态: 无发光背景，图标色使用 `unselectedTint`
- 颜色切换: `animateColorAsState` 平滑过渡

---

### 3.5 标签组件 (tab/)

#### 3.5.1 SceneSegmentedTabs (194 行)

**文件**: `tab/Tab.kt`

分段式标签页组件（类似 iOS UISegmentedControl）。

```
SceneSegmentedTabs(items, selectedIndex, onSelectedChange, height, cornerRadius, ...)
```

- 外层: 圆角胶囊容器 + 毛玻璃模糊 + 描边
- 选中指示器: 独立 `Box`，`animateDpAsState` 水平位移动画 (260ms)
- 每个标签项: `SceneSegmentedTabItem` 私有 composable，文字 + 可选 `BreathingGlowIcon`
- 文字/图标颜色: `animateColorAsState` 根据选中状态切换
- 布局: `BoxWithConstraints` + `Row { items.forEach { weight(1f) } }` 均分宽度

#### 3.5.2 SceneTabItem (16 行)

**文件**: `tab/SceneTabItem.kt`

标签项数据类。

```kotlin
data class SceneTabItem(
    val text: String,
    val icon: ImageVector? = null,
    val selectedIconTint: Color = DefaultVelarisUiSpec.colors.gold,
    val unselectedIconTint: Color = DefaultVelarisUiSpec.colors.iconMuted,
    val selectedTextColor: Color = DefaultVelarisUiSpec.colors.textPrimary,
    val unselectedTextColor: Color = DefaultVelarisUiSpec.colors.iconMuted,
)
```

---

### 3.6 翻页组件 (pager/)

#### 3.6.1 StackedScrollPager (462 行)

**文件**: `pager/StackedScrollPager.kt`

堆叠式水平翻页器，模块中最大的组件文件。

```
StackedScrollPager(currentPage, pageCount, onPageChange, enabled, animationDurationMillis, content)
```

**翻页效果分层** (从底到顶):
1. `StackedUnderPage` -- 目标页面，缩放 (0.98~1.0) + 透明度 (0.84~1.0) + 边缘渐变暗角
2. `TurningPage` -- 当前页面，自定义 `PagerRevealShape` 裁剪可见区域 + 3D 旋转 (`rotationY` 7 度) + 阴影 + 暗化
3. `RevealFeatherOverlay` -- 翻页接缝处的羽化高光层 (112dp 宽)

**手势处理** (`stackedPagerDrag`):
- `forEachGesture` + `awaitPointerEventScope` 手动管理拖拽
- 触摸斜率判断: 水平拖拽优先于垂直
- 边缘阻力: 首页不能前翻、末页不能后翻时施加 `EDGE_RESISTANCE = 0.32` 阻力
- 翻页判定: 拖拽超过 35% 宽度 或 fling 速度超过 900px/s
- 回弹/翻页动画: `Animatable.animateTo` + `tween(320ms)`

#### 3.6.2 VerticalPageTurnLayer (253 行)

**文件**: `pager/VerticalPageTurnLayer.kt`

竖向翻页效果层，用于上下翻页场景。

```
VerticalPageTurnLayer(active, direction, animationDurationMillis, onFinished, oldContent, newContent)
```

- `VerticalTurnDirection`: `TopToBottom` / `BottomToTop`
- 效果分层与 `StackedScrollPager` 类似，但方向从水平变为垂直:
  1. `VerticalUnderPage` -- 新内容，缩放 + 透明度 + 边缘渐变
  2. `VerticalTurningPage` -- 旧内容，`VerticalRevealShape` 裁剪 + `rotationX` 3D 旋转
  3. `VerticalRevealFeather` -- 翻页接缝羽化层
- 动画触发: `active` 为 true 时开始，完成后回调 `onFinished()`

---

### 3.7 面板组件 (panel/)

#### 3.7.1 SwipeUpPanel (188 行)

**文件**: `panel/SwipeUpPanel.kt`

底部上滑面板。

```
SwipeUpPanel(visible, onVisibleChange, panelHeight, threshold, panelColor, borderColor, content)
```

- 位置: `Alignment.BottomCenter`
- 动画: `Animatable` 控制 Y 偏移，`tween(260ms)` 动画进出
- 手势: `detectVerticalDragGestures` 处理拖拽，拖拽超过 `threshold(80%)` 自动收起
- 遮罩: 全屏半透明黑色遮罩，alpha 随面板进度变化 (`panelScrim`)
- 外观: 毛玻璃材质 (Regular) + 描边 + 玻璃表面渐变 + 顶部手柄条
- 手柄: 56dp x 5dp 圆角条，位于面板顶部居中

---

### 3.8 辅助工具

#### 3.8.1 LandscapePreviews (11 行)

**文件**: `preview/LandscapePreviews.kt`

横屏预览注解，组合多个 `@Preview` 注解，当前仅启用 `853x480 (16:9 Medium)` 配置。其余配置（640x360、1280x720、16:10 比例等）已注释待启用。

#### 3.8.2 LandscapeLayoutType (16 行)

**文件**: `layout/LandscapeLayoutType.kt`

横屏布局类型枚举及扩展函数：

```kotlin
enum class LandscapeLayoutType { Small, Medium, Large }

fun Dp.toLandscapeLayoutType(): LandscapeLayoutType = when {
    this < 720.dp  -> LandscapeLayoutType.Small
    this < 1000.dp -> LandscapeLayoutType.Medium
    else           -> LandscapeLayoutType.Large
}
```

---

## 4. 对外暴露的接口

以下为模块对外暴露的主要公共 API，供上层模块引用：

### 4.1 主题入口

| API | 类型 | 说明 |
|-----|------|------|
| `ProvideVelarisTheme` | @Composable | 应用主题入口，包裹静态 M3 + Velaris 双层主题 |
| `ProvideVelarisUiSpec` | @Composable | 注入自定义 UiSpec |
| `ProvideVelarisDialogVisuals` | @Composable | 弹窗专用视觉（禁用模糊、增强表面不透明度） |
| `VelarisTheme.spec` | @Composable 属性 | 获取当前 `VelarisUiSpec` |

### 4.2 主题 Token

| API | 说明 |
|-----|------|
| `VelarisUiSpec` | 主题规格数据类（含 8 个子类） |
| `VelarisColor` | 全局色值常量 object |
| `VelarisFontSize` | 全局字号常量 object |
| `DefaultVelarisUiSpec` | 默认主题实例 |
| `LocalVelarisUiSpec` | CompositionLocal |

### 4.3 毛玻璃

| API | 类型 | 说明 |
|-----|------|------|
| `VelarisBlurState` | class | 模糊状态 |
| `VelarisGlassMaterial` | enum | `Thin` / `Regular` |
| `rememberVelarisBlurState()` | @Composable | 创建模糊状态 |
| `ProvideVelarisBlurState` | @Composable | 注入模糊状态 |
| `Modifier.velarisBlurSource()` | Extension | 标记模糊源 |
| `Modifier.velarisGlassBlur()` | Extension | 应用模糊 |

### 4.4 UI 组件

| 组件 | 包 | 说明 |
|------|-----|------|
| `GlowCircleIconButton` | button | 发光圆形图标按钮 |
| `RadiusIconButton` | button | 圆角矩形图标按钮 |
| `TimerCircleButton` | button | 倒计时圆环按钮 |
| `GlowSeekBar` | bar | 发光滑动条 |
| `AudioBars` | bar | 静态音频条 |
| `AnimatedAudioBars` | bar | 动画音频条 |
| `WaveformTrack` | bar | 波形轨道容器 |
| `RealAudioBars` | bar | 真实音频可视化条 |
| `AudioVisualizer` | bar | Visualizer API 封装 |
| `BreathingGlowIcon` | icon | 呼吸发光图标 |
| `SceneSegmentedTabs` | tab | 分段式标签页 |
| `SceneTabItem` | tab | 标签项数据类 |
| `StackedScrollPager` | pager | 堆叠式水平翻页器 |
| `VerticalPageTurnLayer` | pager | 竖向翻页效果层 |
| `VerticalTurnDirection` | pager | 竖向翻页方向枚举 |
| `SwipeUpPanel` | panel | 底部上滑面板 |

### 4.5 布局辅助

| API | 说明 |
|-----|------|
| `LandscapeLayoutType` | 横屏布局尺寸枚举 (Small/Medium/Large) |
| `Dp.toLandscapeLayoutType()` | Dp 扩展函数，宽度转布局类型 |
| `LandscapePreviews` | 横屏预览注解 |

---

## 5. 依赖关系

### 5.1 Gradle 依赖

```kotlin
plugins {
    alias(libs.plugins.advance.android.library)
    alias(libs.plugins.advance.android.library.compose)
    alias(libs.plugins.advance.android.library.jacoco)
}

dependencies {
    // Jetpack Compose 基础
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Haze 毛玻璃库
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.blur.materials)
}
```

### 5.2 依赖图谱

```
foundation:designsystem
    |
    +-- Jetpack Compose (foundation, material3, icons-extended, tooling-preview)
    +-- Haze (haze, haze-blur, haze-blur-materials)
```

本模块是底层基础模块，不依赖其他项目内部模块。上层业务模块通过 `implementation(project(":foundation:designsystem"))` 引用本模块。

### 5.3 Android 平台依赖

| 类 | 用途 |
|----|------|
| `android.media.audiofx.Visualizer` | AudioVisualizer 中用于实时音频波形捕获 |
| `android.os.Build` | VelarisTheme 中检查 Android 12+ 动态取色支持 |

---

## 6. 当前缺陷与改进点

### 6.1 架构层面

1. **M3 colorScheme 仅作兼容层**: 业务 UI 应优先读取 `VelarisTheme.spec`。如果其他模块通过 `MaterialTheme.colorScheme` 获取颜色，应把它当作兼容层而不是视觉来源。

2. **`DefaultVelarisUiSpec` 硬编码默认值**: `SceneTabItem` 的默认颜色直接引用 `DefaultVelarisUiSpec.colors.*`，而非通过 `CompositionLocal` 获取。这意味着如果运行时主题被替换（如暗/亮模式切换），`SceneTabItem` 的默认值不会跟随变化。

3. **`ProvideVelarisDialogVisuals` 中模糊状态设为 null**: 弹窗内完全禁用了模糊，但没有提供降级策略说明。如果弹窗叠加在毛玻璃元素上，视觉效果可能不一致。

### 6.2 组件层面

4. **`StackedScrollPager` 使用已废弃 API**: `forEachGesture` 在较新版本的 Compose 中已被标记为 deprecated，应迁移到 `awaitEachGesture`。

5. **`GlowSeekBar` 缺少点击跳转功能**: 当前仅支持拖拽操作，不支持点击轨道直接跳转到对应位置。

6. ~~**`RealAudioBars` 的 Log 输出**~~：已解决。`AudioBar` 和 `AudioVisualizer` 中的所有 `android.util.Log` 调用已全部移除。

7. **`AudioVisualizer` 异常处理过于宽泛**: `catch (_: Throwable)` 捕获所有异常（包括 `OutOfMemoryError`），应缩小为 `catch (e: Exception)`。

8. **`GlowSeekBar` 中注释掉的代码**: 第 198~205 行有一段注释掉的高光绘制代码，应清理或恢复。

### 6.3 设计规范层面

9. **`VelarisFontSize` 常量过多**: 31 个字号常量中存在语义重叠（如 `Title` 与 `Subtitle` 均为 18sp，`ControlTitleSmall(14sp)` 与 `ControlItemTitleMedium(14sp)` 数值相同），建议合并同值项或用文档标注使用场景差异。

10. **部分色值缺少语义说明**: `VelarisColor` 中的 M3 兼容色（`M3Purple80` 等）和预览渐变色（`PreviewGreenDark` 等）应通过注释说明其使用限制或标记为 internal。

11. **`VelarisGlow` 中 `seekHighlightAlpha` 单位问题**: 该字段虽然类型是 `Float`，但注释中的语义是"透明度"，与 `VelarisAlpha` 中的同类字段存在分类模糊，建议统一放到 `VelarisAlpha` 中。

### 6.4 可访问性

12. **`RadiusIconButton` 缺少无障碍语义**: 未设置 `Role`、`contentDescription` 等语义属性。

13. **`BreathingGlowIcon` 缺少选中状态语义**: 未通过 `semantics { stateDescription }` 告知屏幕阅读器当前选中状态。

---

## 7. 代码统计

### 7.1 文件统计

| 包 | 文件数 | 行数 | 主要文件 |
|----|--------|------|----------|
| theme | 6 | ~621 | VelarisUiSpec (170), VelarisColor (194), VelarisFontSize (133), VelarisTheme (86), VelarisBlur (99), VelarisDialogVisuals (39) |
| button | 3 | ~407 | IconButton (127), RadiusIconButton (102), TimerCircleButton (178) |
| bar | 2 | ~312 | GlowSeekbar (233), AudioVisualizer (79) |
| clock | 1 | ~615 | FlipCountdownClock (约 615) |
| icon | 1 | 88 | BreathingGlowIcon (88) |
| tab | 2 | ~210 | Tab (194), SceneTabItem (16) |
| pager | 2 | ~715 | StackedScrollPager (462), VerticalPageTurnLayer (253) |
| panel | 1 | 188 | SwipeUpPanel (188) |
| preview | 1 | 11 | LandscapePreviews (11) |
| layout | 1 | 16 | LandscapeLayoutType (16) |
| **合计** | **21** | **~3515** | |

### 7.2 组件规模分布

| 行数范围 | 文件 |
|----------|------|
| > 400 行 | StackedScrollPager (462), FlipCountdownClock (约 615) |
| 200~400 行 | GlowSeekbar (233), VerticalPageTurnLayer (253) |
| 100~200 行 | VelarisUiSpec (170), VelarisColor (194), TimerCircleButton (178), SwipeUpPanel (188), Tab (194), IconButton (127), VelarisFontSize (133), RadiusIconButton (102) |
| < 100 行 | VelarisBlur (99), BreathingGlowIcon (88), AudioVisualizer (79), VelarisTheme (86), VelarisDialogVisuals (39), SceneTabItem (16), LandscapeLayoutType (16), LandscapePreviews (11) |

### 7.3 依赖外部库

| 库 | 用途 |
|----|------|
| Jetpack Compose Foundation | 手势、布局、动画基础设施 |
| Jetpack Compose Material3 | MaterialTheme 框架、Icon、Text |
| Jetpack Compose Material Icons Extended | 扩展图标集 |
| Jetpack Compose UI Tooling Preview | @Preview 支持 |
| Haze (haze / haze-blur / haze-blur-materials) | 毛玻璃效果 |
