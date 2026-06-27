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

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Velaris 项目全局字号管理
 *
 * 所有字号常量在此集中定义，按功能分类组织。
 * 其他模块应引用此 object 中的常量，而非直接硬编码 .sp 值。
 */
object VelarisFontSize {

    // ── 标题字号 ───────────────────────────────────────────────

    /** 展示字号，用于倒计时等大数字显示 */
    val Display: TextUnit = 36.sp

    /** 大数值字号，用于计时器预设值 */
    val ValueLarge: TextUnit = 28.sp

    /** 翻页时钟数字字号，用于沉浸场景倒计时 */
    val FlipClockDigit: TextUnit = 52.sp

    /** 场景标题字号（大屏），用于大屏场景名称 */
    val SceneTitleLarge: TextUnit = 32.sp

    /** 场景标题字号（中屏），用于中屏场景名称 */
    val SceneTitleMedium: TextUnit = 26.sp

    /** 场景标题字号（小屏），用于小屏场景名称 */
    val SceneTitleSmall: TextUnit = 22.sp

    /** 预览卡片标题字号，用于翻页预览场景名 */
    val PreviewTitle: TextUnit = 42.sp

    /** 页面标题字号，用于页面/弹窗标题 */
    val Title: TextUnit = 18.sp

    /** 副标题字号，用于弹窗副标题 */
    val Subtitle: TextUnit = 18.sp

    // ── 正文字号 ───────────────────────────────────────────────

    /** 区域标题字号，用于面板分区标题 */
    val SectionTitle: TextUnit = 16.sp

    /** 控件数值字号，用于控制面板数值显示 */
    val ControlValue: TextUnit = 17.sp

    /** 滑条百分比字号，用于 Seekbar 旁的百分比文字 */
    val SeekPercent: TextUnit = 20.sp

    /** 正文字号，用于主体文本 */
    val Body: TextUnit = 15.sp

    // ── 辅助字号 ───────────────────────────────────────────────

    /** 小正文字号，用于次要描述文字 */
    val BodySmall: TextUnit = 12.sp

    /** 标签/Tab 字号，用于按钮文字和 Tab 标签 */
    val Label: TextUnit = 13.sp

    /** 说明文字字号，用于描述和注释 */
    val Caption: TextUnit = 11.sp

    /** 最小字号，用于最细小的辅助文字 */
    val Micro: TextUnit = 10.sp

    // ── 紧凑型控制面板字号（横屏自适应） ────────────────────────

    /** 紧凑面板标题字号（小屏） */
    val ControlTitleSmall: TextUnit = 14.sp

    /** 紧凑面板标题字号（中屏） */
    val ControlTitleMedium: TextUnit = 15.sp

    /** 紧凑面板标题字号（大屏） */
    val ControlTitleLarge: TextUnit = 16.sp

    /** 紧凑面板子项标题字号（小屏） */
    val ControlItemTitleSmall: TextUnit = 13.sp

    /** 紧凑面板子项标题字号（中屏） */
    val ControlItemTitleMedium: TextUnit = 14.sp

    /** 紧凑面板子项标题字号（大屏） */
    val ControlItemTitleLarge: TextUnit = 15.sp

    /** 紧凑面板数值字号（小屏） */
    val ControlValueSmall: TextUnit = 13.sp

    /** 紧凑面板数值字号（中屏） */
    val ControlValueMedium: TextUnit = 14.sp

    /** 紧凑面板数值字号（大屏） */
    val ControlValueLarge: TextUnit = 15.sp

    /** 紧凑面板场景名字号（小屏） */
    val ControlSceneNameSmall: TextUnit = 16.sp

    /** 紧凑面板场景名字号（中屏） */
    val ControlSceneNameMedium: TextUnit = 17.sp

    /** 紧凑面板场景名字号（大屏） */
    val ControlSceneNameLarge: TextUnit = 18.sp

    // ── 场景标题自适应字号 ─────────────────────────────────────

    /** 场景标题字号（紧凑宽度） */
    val SceneTitleCompactWidth: TextUnit = 20.sp

    /** 场景标题字号（紧凑高度） */
    val SceneTitleCompactHeight: TextUnit = 22.sp

    // ── Material3 占位字号 ─────────────────────────────────────

    /** Material3 bodyLarge 字号（仅用于 MaterialTheme 初始化） */
    val M3BodyLarge: TextUnit = 16.sp
}
