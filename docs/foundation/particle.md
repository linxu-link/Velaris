# foundation:particle 模块文档

## 模块概述

`foundation:particle` 是 Velaris 应用的粒子效果基础模块，负责渲染沉浸式场景中的视觉粒子效果。当前支持三种效果类型：**下雨（Rain）**、**下雪（Snow）** 和 **萤火虫（Fireflies）**。

该模块采用 **TextureView + 独立渲染线程** 的单一渲染路径，通过 `TextureView` + `Surface` 在专用渲染线程中绘制，不阻塞主线程。

模块通过统一入口 `ParticleLayer` Composable 对外交互，调用方只需传入 `ParticleConfig` 即可切换不同效果类型和质量等级。

**包名**: `com.wujia.foundation.particle`
**命名空间**: `com.wujia.foundation.particle`
**被依赖方**: `feature:scene:impl`

---

## 架构设计

### 整体架构图

```
                    ParticleLayer (统一入口)
                           |
                   TextureParticleLayer
                           |
                +----------+----------+
                |          |          |
        SurfaceRain   SurfaceSnow   SurfaceFirefly
        Renderer      Renderer      Renderer
                |          |          |
                +----+-----+----+----+
                     |          |
             ParticleRenderThread
                     |
                 TextureView
```

### 渲染路径

模块仅保留 TextureView + 独立线程的渲染路径：

| 特性 | 说明 |
|------|------|
| 渲染载体 | `TextureView` + `Surface` |
| 线程模型 | 独立 `ParticleRenderThread` |
| 帧调度 | 自主 sleep 控制 ~60fps |
| Compose 集成 | 通过 `AndroidView` 桥接 |
| 适用场景 | 所有粒子效果，不阻塞主线程 |

### ParticleLayer 抽象

`ParticleLayer` 是对外唯一暴露的 Composable 入口。它根据 `ParticleConfig.effect` 类型自动选择对应的 Surface 渲染器：

```kotlin
@Composable
fun ParticleLayer(
    modifier: Modifier = Modifier,
    config: ParticleConfig = ParticleConfig(),
    active: Boolean = true,
)
```

分发逻辑：
- `ParticleEffectType.None` -> 不渲染任何内容（提前返回）
- `ParticleEffectType.Rain` -> `TextureParticleLayer` (内部使用 `SurfaceRainRenderer`)
- `ParticleEffectType.Snow` -> `TextureParticleLayer` (内部使用 `SurfaceSnowRenderer`)
- `ParticleEffectType.Fireflies` -> `TextureParticleLayer` (内部使用 `SurfaceFireflyRenderer`)

### ParticleConfig 配置体系

配置体系分为三层：

1. **ParticleConfig** -- 顶层配置，定义效果类型、强度、风力、质量等级、前景玻璃水痕开关
2. **SnowConfig / RainConfig / FireflyConfig** -- 各效果的粒子细节配置，定义粒子密度、数量范围等
3. **RenderQuality 枚举** -- 质量等级（Low / Medium / High / Ultra），通过 `snowConfigForQuality()` / `rainConfigForQuality()` / `fireflyConfigForQuality()` 工厂函数自动映射为具体粒子配置

质量等级与粒子数量映射：

| 质量等级 | 雪花密度（每百万像素） | 雨滴密度（每百万像素） | 萤火虫密度（每百万像素） |
|---------|---------------------|---------------------|----------------------|
| Low     | 140 / 50-200        | 200 / 80-300        | 8 / 6-25             |
| Medium  | 260 / 90-380        | 360 / 120-520       | 15 / 12-50           |
| High    | 380 / 140-520       | 500 / 180-700       | 24 / 18-80           |
| Ultra   | 760 / 280-1040      | 1000 / 360-1400     | 40 / 30-140          |

> 表格中格式为：密度 / 最小-最大数量

---

## 核心类/接口

### ParticleConfig（配置数据类）

**文件**: `ParticleConfig.kt`（200 行）

粒子配置的核心数据类，标记为 `@Immutable` 以支持 Compose 高效重组。包含 `require` 校验以确保参数合法。

```kotlin
@Immutable
data class ParticleConfig(
    val effect: ParticleEffectType = ParticleEffectType.None,  // 粒子效果类型
    val intensity: Float = 0.72f,                                // 强度 (0.0 - 1.0)
    val wind: Float = 0.2f,                                      // 风力 (0.0 - 1.0)
    val quality: RenderQuality = RenderQuality.Medium,           // 质量等级
    val foregroundGlassEnabled: Boolean = true,                  // 前景玻璃水痕（仅雨天）
)

@Immutable
data class SnowConfig(
    val flakesPerMillionPixels: Int = 260,  // 每百万像素雪花数
    val minFlakes: Int = 90,                // 最少雪花数
    val maxFlakes: Int = 380,               // 最多雪花数
    val nearFlakeRatio: Float = 0.14f,      // 近景雪花比例 (0.0 - 0.5)
)

@Immutable
data class RainConfig(
    val dropsPerMillionPixels: Int = 360,   // 每百万像素雨滴数
    val minDrops: Int = 120,                // 最少雨滴数
    val maxDrops: Int = 520,                // 最多雨滴数
    val splashRatio: Float = 0.12f,         // 溅射比例 (0.0 - 0.5)
    val screenStreakCount: Int = 18,        // 屏幕水痕数量
)

@Immutable
data class FireflyConfig(
    val firefliesPerMillionPixels: Int = 15,  // 每百万像素萤火虫数
    val minFireflies: Int = 12,               // 最少萤火虫数
    val maxFireflies: Int = 50,               // 最多萤火虫数
)
```

相关枚举：

```kotlin
enum class ParticleEffectType { None, Snow, Rain, Fireflies }
enum class RenderQuality { Low, Medium, High, Ultra }
```

工厂函数：

```kotlin
fun snowConfigForQuality(quality: RenderQuality): SnowConfig
fun rainConfigForQuality(quality: RenderQuality): RainConfig
fun fireflyConfigForQuality(quality: RenderQuality): FireflyConfig
```

### ParticleLayer（统一入口）

**文件**: `ParticleLayer.kt`（56 行）

对外暴露的唯一 Composable 入口。当 `effect` 为 `None` 时直接返回，否则委托给 `TextureParticleLayer`。

```kotlin
@Composable
fun ParticleLayer(
    modifier: Modifier = Modifier,
    config: ParticleConfig = ParticleConfig(),
    active: Boolean = true,
)
```

**参数说明**：
- `modifier` -- Compose Modifier，可用于设置尺寸、位置等
- `config` -- 粒子配置，决定效果类型和视觉参数
- `active` -- 是否激活。非激活时停止动画更新以节省性能（用于非当前页面场景）

### TextureParticleLayer（TextureView 包装层）

**文件**: `TextureParticleLayer.kt`（124 行）

基于 TextureView 的粒子层 Composable，桥接 Compose 和 Android View 系统。根据 `ParticleEffectType` 创建对应的 Surface 渲染器：

- `ParticleEffectType.Rain` -> `SurfaceRainRenderer`
- `ParticleEffectType.Snow` -> `SurfaceSnowRenderer`
- `ParticleEffectType.Fireflies` -> `SurfaceFireflyRenderer`

```kotlin
@Composable
fun TextureParticleLayer(
    modifier: Modifier = Modifier,
    config: ParticleConfig = ParticleConfig(),
    active: Boolean = true,
)
```

**生命周期管理**：
- `onSurfaceTextureAvailable` -- 创建 `ParticleRenderThread` 并启动渲染
- `onSurfaceTextureSizeChanged` -- 更新渲染器视口尺寸（`renderer.updateViewport`）
- `onSurfaceTextureDestroyed` -- 停止渲染线程
- `DisposableEffect.onDispose` -- 停止线程并释放渲染器资源

**配置更新**：`LaunchedEffect(config)` 监听配置变化，调用 `renderer.updateConfig(config)` 将新配置传递给渲染器。

**线程管理**：使用 `AtomicReference<ParticleRenderThread?>` 确保线程引用的线程安全性，支持 `active` 状态切换时的暂停/恢复。

### ParticleRenderThread（渲染线程管理）

**文件**: `ParticleRenderThread.kt`（152 行）

独立渲染线程实现，继承自 `Thread`。

```kotlin
internal class ParticleRenderThread(
    private val renderTarget: RenderTarget,
    private val renderer: SurfaceParticleRenderer,
) : Thread("ParticleRenderThread")
```

**公开方法**：
- `startRendering()` -- 设置运行标志并启动线程
- `stopRendering()` -- 设置停止标志并 join 等待（最多 1 秒）

**渲染循环逻辑**：
1. 获取 `System.nanoTime()` 计算 deltaTime
2. `renderTarget.lockCanvas()` 获取 Canvas
3. `canvas.drawColor(TRANSPARENT, CLEAR)` 清除上一帧
4. `renderer.render(canvas, deltaTimeMillis)` 执行渲染
5. `renderTarget.unlockCanvasAndPost(canvas)` 提交帧
6. `sleep(sleepTime)` 帧率控制（目标 ~60fps，每帧约 16ms）

**RenderTarget 抽象**：

```kotlin
internal interface RenderTarget {
    fun lockCanvas(): Canvas?
    fun unlockCanvasAndPost(canvas: Canvas)
}
```

提供两个实现：
- `SurfaceTarget(surface: Surface)` -- 当前使用的实现（用于 TextureView）
- `SurfaceHolderTarget(holder: SurfaceHolder)` -- 已弃用（用于 SurfaceView）

### SurfaceParticleRenderer（渲染器接口）

**文件**: `SurfaceParticleRenderer.kt`（46 行）

Surface 渲染器的基础接口，所有效果渲染器均实现此接口。

```kotlin
internal interface SurfaceParticleRenderer {
    fun render(canvas: Canvas, deltaTimeMillis: Long)  // 渲染一帧
    fun updateViewport(width: Int, height: Int)          // 更新视口大小
    fun release()                                         // 释放资源
}
```

### SurfaceRainRenderer（雨天渲染器）

**文件**: `SurfaceRainRenderer.kt`（313 行）

Surface 版雨天渲染器，实现 `SurfaceParticleRenderer`。

**粒子类型**：
- `RainDrop` -- 雨滴粒子，包含位置、速度、深度、透明度、溅射等属性
- `SplashParticle` -- 落地溅射粒子
- `ScreenWaterStreak` -- 屏幕前景水痕，模拟玻璃上的水滴流动效果

**渲染特效**：
- 薄雾背景层（alpha = 1.8% * intensity）
- 带风偏移的斜线雨滴
- 深度分层（depth 0.16~1.0 影响大小、速度、透明度）
- 落地溅射效果（底部区域触发）
- 前景玻璃水痕（`foregroundGlassEnabled` 控制开关）

**配置更新**：
```kotlin
fun updateConfig(config: ParticleConfig)  // 更新配置，如果关键参数变化则重建粒子场
```

**线程安全**：所有公开方法内部使用 `synchronized(lock)` 保护共享状态。

**配置变更检测**：当 `wind`、`quality`、`foregroundGlassEnabled` 或 `rainConfig` 发生变化时清空并重建粒子场；仅 `intensity` 变化时不重建。

### SurfaceSnowRenderer（雪天渲染器）

**文件**: `SurfaceSnowRenderer.kt`（236 行）

Surface 版雪天渲染器，实现 `SurfaceParticleRenderer`。

**粒子类型**：
- `SnowFlake` -- 雪花粒子，包含位置、半径、下落速度、风速、颤动（flutter）、漂移（drift）、阵风偏移（gustOffset）等属性

**渲染特效**：
- 极淡白色背景层（alpha = 1.4% * intensity）
- 带颤动（flutter）和慢速漂移（slowSway）的自然飘落轨迹
- 近景雪花（depth > 0.74）附加：
  - 运动拖尾线（trailPaint）
  - 模糊光晕圈（1.8x 半径、14% alpha）
- 深度分层影响大小、速度、透明度

**配置更新**：
```kotlin
fun updateConfig(config: ParticleConfig)  // 更新配置
```

**线程安全与配置变更检测**：与 `SurfaceRainRenderer` 一致，使用 `synchronized(lock)` 保护。

### SurfaceFireflyRenderer（萤火虫渲染器）

**文件**: `SurfaceFireflyRenderer.kt`（213 行）

Surface 版萤火虫渲染器，实现 `SurfaceParticleRenderer`。模拟夏夜萤火虫的漫游闪烁效果。

**运动模型**：
- 2D 漫游运动（`wanderAngle` + `wanderSpeed`），无重力影响
- 速度在水平和垂直两个分量上独立随机游走
- 超出屏幕边界时执行环绕（wrap-around），而非回收重建

**亮度模型**：
- 呼吸式亮度脉冲：`brightness = (0.3 + 0.7 * sin(lifeSeconds * pulseSpeed + phase)) * intensity`
- 每个萤火虫具有独立的 `pulseSpeed` 和 `phase`，形成自然错落的闪烁节奏

**渲染特效**：
- 双层渲染：外层光晕（3.5~5 倍核心半径的辉光）+ 内层实心核心
- 暖色调：`FIREFLY_COLOR = 0xFFFFCC66`
- 深度分层（depth 0.15~1.0）影响大小、速度、透明度
- 风力仅作用于水平轴方向

**配置更新**：
```kotlin
fun updateConfig(config: ParticleConfig)  // 更新配置
```

**线程安全**：所有公开方法内部使用 `synchronized(lock)` 保护共享状态。

**配置变更检测**：当 `quality` 发生变化时重建粒子场；仅 `intensity` / `wind` 变化时不重建。

### ParticleLayerExample（使用示例）

**文件**: `ParticleLayerExample.kt`（105 行）

提供 4 个使用示例 Composable：

| 示例函数 | 说明 |
|---------|------|
| `ParticleLayerUsageExample()` | 基础下雨效果 |
| `SnowEffectExample()` | 高质量雪天效果 |
| `LowQualityRainExample()` | 低质量模式（省电），禁用水痕 |
| `DynamicParticleExample()` | 动态参数切换粒子效果 |

---

## 对外暴露的接口

### 公开 Composable 函数

```kotlin
// 统一入口（唯一推荐入口）
@Composable fun ParticleLayer(modifier: Modifier, config: ParticleConfig, active: Boolean)

// TextureView 包装层（可直接使用，但推荐通过 ParticleLayer 调用）
@Composable fun TextureParticleLayer(modifier: Modifier, config: ParticleConfig, active: Boolean)

// 使用示例
@Composable fun ParticleLayerUsageExample()
@Composable fun SnowEffectExample()
@Composable fun LowQualityRainExample()
@Composable fun DynamicParticleExample()
```

### 公开数据类与枚举

```kotlin
// 粒子效果类型
enum class ParticleEffectType { None, Snow, Rain, Fireflies }

// 渲染质量等级
enum class RenderQuality { Low, Medium, High, Ultra }

// 主配置
data class ParticleConfig(effect, intensity, wind, quality, foregroundGlassEnabled)

// 雪花配置
data class SnowConfig(flakesPerMillionPixels, minFlakes, maxFlakes, nearFlakeRatio)

// 雨滴配置
data class RainConfig(dropsPerMillionPixels, minDrops, maxDrops, splashRatio, screenStreakCount)

// 萤火虫配置
data class FireflyConfig(firefliesPerMillionPixels, minFireflies, maxFireflies)
```

### 公开工厂函数

```kotlin
fun snowConfigForQuality(quality: RenderQuality): SnowConfig
fun rainConfigForQuality(quality: RenderQuality): RainConfig
fun fireflyConfigForQuality(quality: RenderQuality): FireflyConfig
```

### 内部（internal）接口和类

以下类型标记为 `internal`，不对模块外暴露：

- `interface SurfaceParticleRenderer` -- Surface 渲染器接口
- `interface RenderTarget` -- 渲染目标抽象
- `class ParticleRenderThread` -- 渲染线程
- `class SurfaceRainRenderer` -- 雨天渲染器
- `class SurfaceSnowRenderer` -- 雪天渲染器
- `class SurfaceFireflyRenderer` -- 萤火虫渲染器
- `class SurfaceTarget` -- Surface 适配器
- `class SurfaceHolderTarget`（已弃用）-- SurfaceHolder 适配器

---

## 依赖关系

### 本模块依赖

来源：`foundation/particle/build.gradle.kts`

```kotlin
dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
```

**总结**：本模块仅依赖 AndroidX Compose 基础库，无其他内部模块依赖，是一个纯基础层模块。

### 依赖本模块的模块

| 模块 | 依赖方式 |
|------|---------|
| `feature:scene:impl` | `implementation(projects.foundation.particle)` |

---

## 性能考量

### 线程管理

- **独立渲染线程**：所有粒子效果均在独立的 `ParticleRenderThread` 中渲染，不阻塞 Compose 主线程。
- **active 状态**：非当前页面时（`active=false`）停止渲染线程，避免不可见页面消耗渲染资源。通过 `AtomicBoolean` 控制线程生命周期，`AtomicReference` 管理线程实例引用。

### 帧率控制

渲染线程使用粗粒度 sleep 控制帧率：
```kotlin
val sleepTime = (16 - frameTimeMillis).coerceAtLeast(1)
sleep(sleepTime)
```
注意：该实现使用 `sleep()` 而非 `Choreographer` 或 vsync 信号，可能导致帧时间不稳定。

### 内存分配模式

- **粒子数组预分配**：使用 `mutableListOf` + `repeat` 一次性创建所有粒子，避免运行时频繁分配。
- **密度自适应**：粒子数量根据 `屏幕像素数 / 1,000,000 * 密度系数` 计算，钳制在 `[min, max]` 范围内。
- **Paint 对象复用**：所有 Paint 对象在类初始化时创建一次，后续通过修改 alpha/strokeWidth 等属性复用。

### 线程安全

所有 Surface 渲染器（`SurfaceRainRenderer`、`SurfaceSnowRenderer`、`SurfaceFireflyRenderer`）的公开方法使用 `synchronized(lock)` 保护共享可变状态（width、height、粒子列表），确保渲染线程与配置更新（来自 Compose 主线程）之间的线程安全。

### 配置变更检测

各渲染器的 `updateConfig()` 方法实现了变更检测机制：
- **雨天**：`wind`、`quality`、`foregroundGlassEnabled` 或 `rainConfig` 变化时重建粒子场；仅 `intensity` 变化时不重建。
- **雪天**：与雨天类似，关键参数变化时重建。
- **萤火虫**：`quality` 变化时重建粒子场；仅 `intensity` / `wind` 变化时不重建。

这种设计避免了不必要的粒子场重建，减少配置动态调整时的闪烁。

---

## 当前缺陷/改进点

### 性能问题

1. **sleep 帧率控制粗糙**：`ParticleRenderThread` 使用 `Thread.sleep()` 控制帧率，精度受系统调度影响，可能导致帧时间波动。建议改用 `Choreographer` 或 vsync 信号。

2. **粒子遍历效率**：`forEachIndexed` 遍历时对越界粒子做 `list[index] = newParticle` 替换操作（雨天和雪天），在大量粒子场景下可能产生可观的开销。可考虑使用数组替代 List 以减少装箱开销。

3. **每帧全量重绘**：无论粒子是否可见，每帧绘制所有粒子。未做可见性裁剪（culling）。

### 设计问题

4. **SurfaceHolderTarget 已弃用但仍保留**：`ParticleRenderThread.kt` 中的 `SurfaceHolderTarget` 和相关构造函数标记为 `@Deprecated`，但代码仍保留在项目中，增加了不必要的复杂度。

5. **颜色硬编码**：粒子颜色全部硬编码（雨天/雪天为白色 `0xFFFFFFFF`，萤火虫为暖黄 `0xFFFFCC66`），无法根据场景背景自适应。建议支持通过配置传入颜色或色调。

6. **缺少粒子缓存/池化**：粒子回收时直接创建新对象（雨天和雪天的边界回收策略），频繁创建/销毁小对象可能增加 GC 压力。建议使用对象池模式。

### 缺失效果

7. **不支持雾、冰雹、沙尘暴等天气效果**：`ParticleEffectType` 枚举仅包含 `None`、`Snow`、`Rain`、`Fireflies` 四种类型。
8. **雨天无闪电效果**
9. **雪天无积雪效果**
10. **无粒子间碰撞或与场景元素的交互**

---

## 代码统计

### 文件清单

| 文件名 | 行数 | 可见性 | 职责 |
|--------|------|--------|------|
| `ParticleConfig.kt` | 200 | public | 配置数据类、枚举、工厂函数 |
| `ParticleLayer.kt` | 56 | public | 统一入口 Composable |
| `TextureParticleLayer.kt` | 124 | public | TextureView 包装层 Composable |
| `ParticleRenderThread.kt` | 152 | internal | 渲染线程管理 |
| `SurfaceParticleRenderer.kt` | 46 | internal | Surface 渲染器接口 |
| `SurfaceRainRenderer.kt` | 313 | internal | 雨天渲染器 |
| `SurfaceSnowRenderer.kt` | 236 | internal | 雪天渲染器 |
| `SurfaceFireflyRenderer.kt` | 213 | internal | 萤火虫渲染器 |
| `ParticleLayerExample.kt` | 105 | public | 使用示例 |
| `build.gradle.kts` | -- | -- | 构建配置 |

### 统计摘要

| 指标 | 值 |
|------|-----|
| Kotlin 源文件数 | 9 |
| 总代码行数 | 1,445 |
| 公开 Composable 函数 | 5 个（含 4 个示例） |
| 公开数据类 | 4 个（ParticleConfig、SnowConfig、RainConfig、FireflyConfig） |
| 公开枚举 | 2 个（ParticleEffectType、RenderQuality） |
| 公开工厂函数 | 3 个 |
| 内部类/接口 | 8 个 |
| 粒子效果类型 | 3 种（Rain、Snow、Fireflies） |
| 渲染路径 | 1 条（TextureView + Surface） |
| 质量等级 | 4 级（Low / Medium / High / Ultra） |
| 依赖的外部模块 | 0 个（仅 Compose 基础库） |
| 被依赖次数 | 1 个（feature:scene:impl） |
