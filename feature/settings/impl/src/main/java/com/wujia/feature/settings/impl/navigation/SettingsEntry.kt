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
package com.wujia.feature.settings.impl.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.wujia.feature.settings.api.SettingsNavKey
import com.wujia.feature.settings.impl.ui.SettingsScreen
import com.wujia.foundation.designsystem.theme.ProvideVelarisBlurState
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.rememberVelarisBlurState
import com.wujia.foundation.designsystem.theme.velarisBlurSource
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import com.wujia.foundation.navigation.Navigator
import com.wujia.foundation.ui.R

/**
 * 设置模块的导航入口注册函数。
 * 在根导航的 EntryProviderScope 中调用，用于支持独立设置页面（未来完整注册）。
 */
fun EntryProviderScope<NavKey>.settingsEntry(
    navigator: Navigator,
) {
    entry<SettingsNavKey> {
        SettingsPage(onBackClick = navigator::goBack)
    }
}

@Composable
private fun SettingsPage(
    onBackClick: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val blurState = rememberVelarisBlurState()
    val scrollState = rememberScrollState()

    // 提供模糊状态，使子内容可使用 velarisBlurSource / velarisGlassBlur 实现毛玻璃效果
    ProvideVelarisBlurState(blurState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .velarisBlurSource(blurState)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            VelarisColor.BgGradientDarkBlue,
                            VelarisColor.BgGradientDarkSlate,
                            Color.Black,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spec.spacing.medium, vertical = spec.spacing.medium),
            ) {
                // 自定义顶部栏：左侧返回按钮 + 居中标题
                Box(
                    modifier = Modifier.height(44.dp),
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .velarisGlassBlur(
                                shape = CircleShape,
                                blurRadius = spec.blur.button,
                            )
                            .background(spec.colors.controlSurface.copy(alpha = 0.20f))
                            .border(
                                spec.size.stroke,
                                spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                                CircleShape,
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = spec.colors.textPrimary.copy(alpha = spec.alpha.icon),
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_title),
                        color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                        fontSize = spec.typography.title,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Spacer(modifier = Modifier.height(spec.spacing.medium))

                SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    scrollState = scrollState,
                )
            }
        }
    }
}
