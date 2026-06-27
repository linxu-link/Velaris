/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.foundation.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Velaris 项目全局色值管理
 *
 * 所有色值在此集中定义，按功能分类组织。
 * 其他模块应引用此 object 中的常量，而非直接硬编码色值。
 */
object VelarisColor {

    // ── 主强调色系 ──────────────────────────────────────────────

    /** 主强调色，用于按钮、选中态和高亮 */
    val Gold = Color(0xFF6ED0D6)

    /** 柔和强调色，用于次要文字、描边 */
    val GoldSoft = Color(0xFF94B9C6)

    /** 明亮强调色，用于主要文字 */
    val GoldBright = Color(0xFFF2F7FA)

    /** 强调色背景上的深色文字 */
    val OnGold = Color(0xFF0F1A23)

    // ── 文字色 ─────────────────────────────────────────────────

    /** 弱化文字（偏冷灰蓝，搭配透明度使用） */
    val TextMuted = Color(0xFFDEE7EF)

    /** 非活跃图标色（灰蓝色） */
    val IconMuted = Color(0xFF9AAAB6)

    // ── 表面色（场景内，低透明度） ──────────────────────────────

    /** 主表面色（70%透明度） */
    val Surface = Color(0xB3111720)

    /** 柔和表面色（40%透明度） */
    val SurfaceSoft = Color(0x66101822)

    /** 微妙表面色（32%透明度） */
    val SurfaceSubtle = Color(0x5212171F)

    /** 控件表面色（15%透明度） */
    val ControlSurface = Color(0x26303B45)

    // ── 表面色（弹窗用，更高不透明度） ──────────────────────────

    /** 弹窗主表面色（94%透明度） */
    val DialogSurface = Color(0xF00F1822)

    /** 弹窗柔和表面色（90%透明度） */
    val DialogSurfaceSoft = Color(0xE60F1721)

    /** 弹窗微妙表面色（80%透明度） */
    val DialogSurfaceSubtle = Color(0xCC101A25)

    /** 弹窗控件表面色（85%透明度） */
    val DialogControlSurface = Color(0xD90F1720)

    // ── 状态色 ─────────────────────────────────────────────────

    /** 非活跃轨道/滑块色 */
    val TrackInactive = Color(0xFF283742)

    // ── 毛玻璃材质色（薄型） ───────────────────────────────────

    /** 薄玻璃背景色（75%透明度） */
    val GlassThinBackground = Color(0xBF26323D)

    /** 薄玻璃叠色（10%白色，用于提亮实时模糊结果） */
    val GlassThinTint = Color(0x1A8CB2C2)

    /** 薄玻璃降级色（75%透明度，不支持模糊时的备选） */
    val GlassThinFallback = Color(0xBF26323D)

    // ── 毛玻璃材质色（常规型） ─────────────────────────────────

    /** 常规玻璃背景色（75%透明度） */
    val GlassRegularBackground = Color(0xBF26323D)

    /** 常规玻璃叠色（13%白色，用于提亮实时模糊结果） */
    val GlassRegularTint = Color(0x218CB2C2)

    /** 常规玻璃降级色（75%透明度，不支持模糊时的备选） */
    val GlassRegularFallback = Color(0xBF26323D)

    // ── 渐变色（选中胶囊） ─────────────────────────────────────

    /** 胶囊渐变起始色（冷青色，50%透明度） */
    val GradientPillStart = Color(0x8071D7E2)

    /** 胶囊渐变结束色（蓝灰色，30%透明度） */
    val GradientPillEnd = Color(0x804E6F84)

    // ── 渐变色（玻璃表面） ─────────────────────────────────────

    /** 玻璃表面渐变起始色（深蓝灰，20%透明度） */
    val GradientGlassStart = Color(0x3415222F)

    /** 玻璃表面渐变结束色（更深，14%透明度） */
    val GradientGlassEnd = Color(0x240D1420)

    // ── 粒子效果色 ─────────────────────────────────────────────

    /** 无粒子效果（灰色） */
    val ParticleNone = Color(0xFF6B7785)

    /** 雨滴粒子效果（蓝色） */
    val ParticleRain = Color(0xFF3B82F6)

    /** 雪花粒子效果（紫色） */
    val ParticleSnow = Color(0xFF8B5CF6)

    /** 萤火虫粒子效果（琥珀色） */
    val ParticleFireflies = Color(0xFFE9CF78)

    /** 粒子白色，用于雨滴和雪花渲染 */
    val ParticleWhite = Color.White

    // ── 场景列表调色板 ─────────────────────────────────────────

    /** 冰蓝色（雪景图标） */
    val PaletteIce = Color(0xFFA7C8D8)

    /** 森林绿（森林图标） */
    val PaletteForest = Color(0xFF8DBDAF)

    /** 暖琥珀色（咖啡图标） */
    val PaletteAmber = Color(0xFFD8BE97)

    /** 暮光紫（夜晚图标） */
    val PaletteTwilight = Color(0xFF8A9CD4)

    /** 水疗绿（SPA图标） */
    val PaletteSpa = Color(0xFF9AC8AF)

    // ── 页面背景渐变色 ─────────────────────────────────────────

    /** 背景渐变深蓝色 */
    val BgGradientDarkBlue = Color(0xFF07121B)

    /** 背景渐变深灰蓝色 */
    val BgGradientDarkSlate = Color(0xFF0D1620)

    // ── 场景编辑预览渐变色 ─────────────────────────────────────

    /** 预览背景深绿灰色 */
    val PreviewGreenDark = Color(0xFF233239)

    /** 预览背景极深绿色 */
    val PreviewGreenDeep = Color(0xFF0B1317)

    /** 视频材质深灰绿色 */
    val MaterialSlateDark = Color(0xFF152229)

    /** 视频材质近黑色 */
    val MaterialNearBlack = Color(0xFF081116)

    /** 噪音卡片顶部深青色 */
    val NoiseTealDark = Color(0xFF1E3138)

    /** 噪音卡片底部极深青色 */
    val NoiseTealDeep = Color(0xFF081216)

    /** 媒体缩略图深灰绿色 */
    val ThumbnailGrayDark = Color(0xFF1D2A32)

    /** 媒体缩略图近黑色 */
    val ThumbnailNearBlack = Color(0xFF091116)

    // ── Material3 兼容色（仅保留字段，默认不作为业务视觉来源） ──

    val M3Purple80 = Color(0xFFD0BCFF)
    val M3PurpleGrey80 = Color(0xFFCCC2DC)
    val M3Pink80 = Color(0xFFEFB8C8)
    val M3Purple40 = Color(0xFF6650a4)
    val M3PurpleGrey40 = Color(0xFF625b71)
    val M3Pink40 = Color(0xFF7D5260)
}
