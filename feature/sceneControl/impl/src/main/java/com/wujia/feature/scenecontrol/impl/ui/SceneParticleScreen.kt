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
package com.wujia.feature.scenecontrol.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.wujia.feature.scenecontrol.impl.ui.component.ScenePanelShell
import com.wujia.feature.scenecontrol.impl.ui.component.VelarisSwitch
import com.wujia.feature.scenecontrol.impl.ui.component.controlItemTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinHeight
import com.wujia.feature.scenecontrol.impl.ui.component.controlPanelMinWidth
import com.wujia.feature.scenecontrol.impl.ui.component.controlTitleFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.controlValueFontSize
import com.wujia.feature.scenecontrol.impl.ui.component.particlePanelMaxHeight
import com.wujia.foundation.designsystem.preview.LandscapePreviews
import com.wujia.foundation.designsystem.tab.SceneSegmentedTabs
import com.wujia.foundation.designsystem.tab.SceneTabItem
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.designsystem.theme.velarisGlassBlur
import com.wujia.foundation.model.scene.NO_PARTICLE
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.model.soundcontrol.SoundControlItem
import com.wujia.foundation.ui.R
import com.wujia.foundation.ui.SeekbarControlPanel

@Composable
/**
 * 粒子效果屏幕。
 * 通过 ScenePanelShell 统一获取响应式布局参数
 * （minWidth / 字体大小 / maxHeight），
 * 避免在 EffectPanel 和 IntensityPanel 中重复解包 layoutType。
 */
internal fun SceneParticleScreen(modifier: Modifier = Modifier, sceneName: String = "", particleSettings: SceneParticleSettings = NO_PARTICLE, onEffectChange: (SceneParticleEffect) -> Unit = {}, onIntensityChange: (Float) -> Unit = {}, onWindChange: (Float) -> Unit = {}, onQualityChange: (SceneParticleQuality) -> Unit = {}, onForegroundGlassChange: (Boolean) -> Unit = {}, isCustomScene: Boolean = false, onEditSceneClick: () -> Unit = {}) {
    ScenePanelShell(
        sceneName = sceneName,
        isCustomScene = isCustomScene,
        onEditSceneClick = onEditSceneClick,
        modifier = modifier,
    ) { layoutType ->
        val minWidth = layoutType.controlPanelMinWidth
        val minHeight = layoutType.controlPanelMinHeight
        val maxHeight = layoutType.particlePanelMaxHeight
        val titleFontSize = layoutType.controlTitleFontSize
        val itemTitleFontSize = layoutType.controlItemTitleFontSize
        val valueFontSize = layoutType.controlValueFontSize

        SceneParticleEffectPanel(
            particleSettings = particleSettings,
            onEffectChange = onEffectChange,
            onQualityChange = onQualityChange,
            onForegroundGlassChange = onForegroundGlassChange,
            minWidth = minWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            titleFontSize = titleFontSize,
            itemTitleFontSize = itemTitleFontSize,
            modifier = Modifier.weight(1f),
        )

        SceneParticleIntensityPanel(
            particleSettings = particleSettings,
            onIntensityChange = onIntensityChange,
            onWindChange = onWindChange,
            minWidth = minWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            titleFontSize = titleFontSize,
            itemTitleFontSize = itemTitleFontSize,
            valueFontSize = valueFontSize,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SceneParticleEffectPanel(
    particleSettings: SceneParticleSettings,
    onEffectChange: (SceneParticleEffect) -> Unit,
    onQualityChange: (SceneParticleQuality) -> Unit,
    onForegroundGlassChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 280.dp,
    minHeight: Dp = 180.dp,
    maxHeight: Dp = 220.dp,
    titleFontSize: TextUnit = TextUnit.Unspecified,
    itemTitleFontSize: TextUnit = TextUnit.Unspecified,
) {
    val spec = VelarisTheme.spec
    val panelShape = RoundedCornerShape(spec.radii.panel)
    val resolvedTitleFontSize = titleFontSize.takeOrElse { spec.typography.subtitle }
    val resolvedItemTitleFontSize = itemTitleFontSize.takeOrElse { spec.typography.subtitle }

    val effects = listOf(
        SceneParticleEffect.None,
        SceneParticleEffect.Rain,
        SceneParticleEffect.Snow,
        SceneParticleEffect.Fireflies,
    )
    val effectItems = listOf(
        SceneTabItem(text = stringResource(R.string.particle_none), icon = Icons.Outlined.CloudOff),
        SceneTabItem(
            text = stringResource(R.string.particle_rain),
            icon = Icons.Outlined.WaterDrop,
        ),
        SceneTabItem(text = stringResource(R.string.particle_snow), icon = Icons.Outlined.Grain),
        SceneTabItem(
            text = stringResource(R.string.particle_fireflies),
            icon = Icons.Outlined.Nightlight,
        ),
    )

    val qualities = listOf(
        SceneParticleQuality.Low,
        SceneParticleQuality.Medium,
        SceneParticleQuality.High,
        SceneParticleQuality.Ultra,
    )
    val qualityItems = listOf(
        SceneTabItem(
            text = stringResource(R.string.particle_quality_low),
            icon = Icons.Outlined.Tune,
        ),
        SceneTabItem(
            text = stringResource(R.string.particle_quality_medium),
            icon = Icons.Outlined.Tune,
        ),
        SceneTabItem(
            text = stringResource(R.string.particle_quality_high),
            icon = Icons.Outlined.Tune,
        ),
        SceneTabItem(
            text = stringResource(R.string.particle_quality_ultra),
            icon = Icons.Outlined.Tune,
        ),
    )

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .height(maxHeight)
            .clip(panelShape)
            .velarisGlassBlur(
                shape = panelShape,
                blurRadius = spec.blur.panel,
            )
            .background(spec.colors.surfaceSoft)
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                shape = panelShape,
            )
            .padding(vertical = spec.spacing.small, horizontal = spec.spacing.large),
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(spec.spacing.xSmall))
            Text(
                text = stringResource(R.string.particle_property),
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = resolvedTitleFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(spec.spacing.medium))

        // 粒子类型选择
        SceneSegmentedTabs(
            items = effectItems,
            selectedIndex = effects.indexOf(particleSettings.effect).coerceAtLeast(0),
            onSelectedChange = { index -> onEffectChange(effects[index]) },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 38.dp,
        )

        // 质量选择（粒子效果不为 None 时显示）
        if (particleSettings.effect != SceneParticleEffect.None) {
            Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spec.size.stroke)
                    .background(spec.colors.stroke.copy(alpha = spec.alpha.stroke)),
            )

            Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

            Text(
                text = stringResource(R.string.particle_quality),
                color = spec.colors.textSecondary.copy(alpha = 0.78f),
                fontSize = resolvedItemTitleFontSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = spec.spacing.xSmall),
            )

            Spacer(modifier = Modifier.height(spec.spacing.small))

            SceneSegmentedTabs(
                items = qualityItems,
                selectedIndex = qualities.indexOf(particleSettings.quality).coerceAtLeast(0),
                onSelectedChange = { index -> onQualityChange(qualities[index]) },
                modifier = Modifier.fillMaxWidth(),
                minHeight = 36.dp,
            )
        }

        // 前景水痕开关（仅雨天）
        if (particleSettings.effect == SceneParticleEffect.Rain) {
            Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spec.size.stroke)
                    .background(spec.colors.stroke.copy(alpha = spec.alpha.stroke)),
            )

            Spacer(Modifier.height(spec.spacing.medium - spec.spacing.xSmall / 2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.particle_foreground_streak),
                        color = spec.colors.textSecondary.copy(alpha = 0.78f),
                        fontSize = resolvedItemTitleFontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(spec.spacing.xSmall / 2))
                    Text(
                        text = stringResource(R.string.particle_rain_on_glass),
                        color = spec.colors.textMuted.copy(alpha = 0.6f),
                        fontSize = resolvedItemTitleFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                VelarisSwitch(
                    checked = particleSettings.foregroundGlassEnabled,
                    onCheckedChange = onForegroundGlassChange,
                )
            }
        }
    }
}

@Composable
private fun SceneParticleIntensityPanel(
    particleSettings: SceneParticleSettings,
    onIntensityChange: (Float) -> Unit,
    onWindChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 280.dp,
    minHeight: Dp = 180.dp,
    maxHeight: Dp = 220.dp,
    titleFontSize: TextUnit = TextUnit.Unspecified,
    itemTitleFontSize: TextUnit = TextUnit.Unspecified,
    valueFontSize: TextUnit = TextUnit.Unspecified,
) {
    val spec = VelarisTheme.spec
    val panelShape = RoundedCornerShape(spec.radii.panel)
    val intensityLabel = stringResource(R.string.particle_intensity)
    val windLabel = stringResource(R.string.particle_wind)

    val items = remember(particleSettings.intensity, particleSettings.wind) {
        listOf(
            SoundControlItem(intensityLabel, Icons.Outlined.Tune, particleSettings.intensity),
            SoundControlItem(windLabel, Icons.Outlined.WaterDrop, particleSettings.wind),
        )
    }

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .height(maxHeight)
            .clip(panelShape)
            .velarisGlassBlur(
                shape = panelShape,
                blurRadius = spec.blur.panel,
            )
            .background(spec.colors.surfaceSoft)
            .border(
                width = spec.size.stroke,
                color = spec.colors.stroke.copy(alpha = spec.alpha.stroke),
                shape = panelShape,
            )
            .padding(vertical = spec.spacing.small, horizontal = spec.spacing.large),
    ) {
        SeekbarControlPanel(
            items = items,
            title = stringResource(R.string.particle_effect),
            onValueChange = { index, value ->
                when (index) {
                    0 -> onIntensityChange(value)
                    1 -> onWindChange(value)
                }
            },
            minWidth = minWidth,
            minHeight = minHeight,
            titleFontSize = titleFontSize,
            itemTitleFontSize = itemTitleFontSize,
            valueFontSize = valueFontSize,
            modifier = modifier.heightIn(min = minHeight, max = maxHeight),
        )
    }
}

@LandscapePreviews
@Composable
private fun SceneParticleScreenPreview() {
    SceneParticleScreen(
        particleSettings = NO_PARTICLE.copy(effect = SceneParticleEffect.Rain),
    )
}
