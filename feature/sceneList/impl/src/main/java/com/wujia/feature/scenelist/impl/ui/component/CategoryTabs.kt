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
package com.wujia.feature.scenelist.impl.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.scene.SceneCategory

/**
 * 场景分类 Tab 栏，视觉风格参考 SourceTabs：固定高度、分隔线、选中下划线、文字居中。
 *
 * @param selectedCategory 当前选中的分类
 * @param onCategoryChange 点击 tab 时的回调
 * @param modifier Modifier
 */
@Composable
internal fun CategoryTabs(
    selectedCategory: SceneCategory,
    onCategoryChange: (SceneCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = VelarisTheme.spec
    val categories = remember { SceneCategory.entries }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(spec.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
                    .velarisClickable { onCategoryChange(category) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = category.displayName,
                        color = if (isSelected) {
                            spec.colors.gold
                        } else {
                            spec.colors.textSecondary.copy(alpha = spec.alpha.textSecondary)
                        },
                        fontSize = spec.typography.body,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(spec.spacing.small))

                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(spec.radii.pill))
                            .background(
                                if (isSelected) spec.colors.gold else Color.Transparent,
                            ),
                    )
                }
            }

            if (index != categories.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(spec.colors.stroke.copy(alpha = spec.alpha.strokeStrong)),
                )
            }
        }
    }
}
