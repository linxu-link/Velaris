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
package com.wujia.feature.sceneedit.impl.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.wujia.feature.sceneedit.api.SceneEditNavKey
import com.wujia.feature.sceneedit.impl.ui.SceneEditScreen
import com.wujia.foundation.designsystem.theme.ProvideVelarisBlurState
import com.wujia.foundation.designsystem.theme.VelarisColor
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.rememberVelarisBlurState
import com.wujia.foundation.designsystem.theme.velarisBlurSource
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import com.wujia.foundation.navigation.Navigator
import com.wujia.foundation.ui.R

/**
 * sceneEdit 模块的导航入口注册函数。
 *
 * 在根宿主（scene impl）的 EntryProvider 中调用，用于注册 SceneEditNavKey 的处理。
 * 仅依赖 api 模块的 NavKey，实现 api/impl 拆分。
 */
fun EntryProviderScope<NavKey>.sceneEditEntry(
    navigator: Navigator,
) {
    entry<SceneEditNavKey> { key ->
        SceneEditPage(
            sceneId = key.sceneId,
            category = key.category,
            onBackClick = navigator::goBack,
            onSaved = navigator::goBack,
        )
    }
}

/**
 * 编辑页的外层容器。
 * 提供统一的深色渐变背景、毛玻璃返回按钮标题栏，以及 ProvideVelarisBlurState。
 * 内部承载真正的 SceneEditScreen 内容。
 */
@Composable
private fun SceneEditPage(
    sceneId: String?,
    category: com.wujia.foundation.model.scene.SceneCategory?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    val spec = VelarisTheme.spec
    val blurState = rememberVelarisBlurState()

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
                        text = stringResource(R.string.scene_edit_title),
                        color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                        fontSize = spec.typography.title,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Spacer(modifier = Modifier.height(spec.spacing.medium))

                SceneEditScreen(
                    sceneId = sceneId,
                    category = category,
                    onSaved = onSaved,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
