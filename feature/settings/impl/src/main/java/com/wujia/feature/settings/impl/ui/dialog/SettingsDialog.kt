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
package com.wujia.feature.settings.impl.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.VelarisDialogPanel

/**
 * 设置模块通用的对话框内容容器。
 * 只负责宽高约束、滚动与内边距，不再直接创建窗口级 Dialog。
 *
 * 所有具体设置对话框（主题、播放、隐私、关于）都应通过此包装器呈现，
 * 以保持视觉和交互一致性。
 */
@Composable
internal fun SettingsDialogContent(
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val spec = VelarisTheme.spec
    // 大屏设备使用更窄的比例，避免对话框过宽
    val dialogWidthFraction = if (configuration.screenWidthDp >= 900) 0.38f else 0.56f
    val dialogWidth = minOf(configuration.screenWidthDp.dp * dialogWidthFraction, 460.dp)
    val dialogMaxHeight = configuration.screenHeightDp.dp * 0.78f
    VelarisDialogPanel(
        modifier = Modifier
            .widthIn(max = dialogWidth)
            .heightIn(max = dialogMaxHeight),
        contentPadding = PaddingValues(
            horizontal = spec.spacing.large,
            vertical = spec.spacing.medium,
        ),
        scrollable = true,
    ) {
        content()
    }
}

@LandscapePreviews
@Composable
private fun SettingsDialogPreview() {
    SettingsDialogContent {}
}

/**
 * 设置对话框中通用的单选项行（带 RadioButton）。
 * 用于主题设置、播放设置等多个选项列表，减少重复代码。
 *
 * @param title 主标题
 * @param description 描述文字
 * @param selected 是否选中
 * @param onClick 点击回调（同时用于 RadioButton 和整行）
 */
@Composable
internal fun SettingsRadioOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spec.radii.thumbnail))
            .velarisClickable(onClick = onClick)
            .padding(vertical = spec.spacing.small, horizontal = spec.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = spec.colors.goldBright,
                unselectedColor = spec.colors.textSecondary.copy(alpha = 0.38f),
            ),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = spec.spacing.small, end = spec.spacing.medium),
        ) {
            Text(
                text = title,
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = spec.typography.body,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            )
            Text(
                text = description,
                color = spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary),
                fontSize = spec.typography.bodySmall,
            )
        }
    }
}
