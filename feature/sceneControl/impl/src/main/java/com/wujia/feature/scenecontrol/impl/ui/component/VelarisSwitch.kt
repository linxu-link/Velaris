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
package com.wujia.feature.scenecontrol.impl.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme

@Composable
/**
 * 场景控制专用的 Switch 组件。
 * 动画使用 animateDpAsState / animateColorAsState，
 * 实现平滑的 thumb 移动和 track 颜色过渡。
 * 样式与 Velaris 设计系统保持一致（金色激活态）。
 */
internal fun VelarisSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val spec = VelarisTheme.spec
    val switchPadding = spec.spacing.xSmall - spec.size.stroke
    val thumbSize = spec.size.iconLarge - spec.spacing.small + spec.size.stroke * 2f
    val checkedThumbOffset = spec.size.iconLarge - spec.spacing.xSmall - spec.size.stroke
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) checkedThumbOffset else switchPadding,
        label = "thumbOffset",
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            spec.colors.gold.copy(alpha = 0.82f)
        } else if (!enabled) {
            spec.colors.stroke.copy(alpha = 0.08f)
        } else {
            spec.colors.stroke.copy(alpha = 0.16f)
        },
        label = "trackColor",
    )

    Box(
        modifier = modifier
            .width(spec.size.panelHandleWidth + spec.spacing.xSmall)
            .height(spec.size.iconLarge)
            .clip(RoundedCornerShape(spec.radii.pill))
            .background(trackColor)
            .velarisClickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(switchPadding),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(spec.colors.textPrimary),
        )
    }
}
