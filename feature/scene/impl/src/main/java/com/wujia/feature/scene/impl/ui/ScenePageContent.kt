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
package com.wujia.feature.scene.impl.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.wujia.feature.scene.impl.ui.component.SceneTitleLayer
import com.wujia.foundation.model.scene.SceneResource
import com.wujia.foundation.particle.ParticleEffectType
import com.wujia.foundation.particle.ParticleLayer
import com.wujia.foundation.player.VelarisPlayerController
import com.wujia.foundation.player.VelarisVideoPlayer
import com.wujia.foundation.player.rememberVideoFirstFrame
import com.wujia.foundation.ui.rememberContentBitmap

@Composable
internal fun ScenePageContent(
    scene: SceneResource,
    titleFontSize: TextUnit,
    showSubtitle: Boolean,
    edgePadding: Dp,
    modifier: Modifier,
    isCurrentPage: Boolean,
    isScenePlaying: Boolean,
    playerController: VelarisPlayerController?,
    renderVideo: Boolean = true,
    chromeVisible: Boolean = true,
) {
    val hasLiveFirstFrame = playerController?.hasVideoEverRenderedFirstFrame == true

    val shouldAttachLiveVideoPlayer =
        renderVideo &&
            isCurrentPage &&
            scene.video != null &&
            playerController != null &&
            (isScenePlaying || hasLiveFirstFrame)

    val shouldShowStaticVideoFirstFrame = when {
        scene.video == null -> false
        !isCurrentPage -> true
        !renderVideo -> true
        playerController == null -> true
        !hasLiveFirstFrame -> true
        else -> false
    }

    val videoFirstFrame = if (shouldShowStaticVideoFirstFrame) {
        rememberVideoFirstFrame(scene.video?.uri)
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        // Layer 1: 背景
        if (videoFirstFrame != null) {
            Image(
                bitmap = videoFirstFrame,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (scene.backgroundUri != null) {
            val backgroundUri = scene.backgroundUri.orEmpty()
            val backgroundBitmap = rememberContentBitmap(backgroundUri)
            backgroundBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        } else {
            scene.backgroundResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // Layer 2: 视频播放器（实时层）
        if (shouldAttachLiveVideoPlayer) {
            VelarisVideoPlayer(
                controller = playerController,
                modifier = Modifier
                    .fillMaxSize(),
            )
        }

        // Layer 3: 暗色遮罩
        SceneDarknessOverlay(darkness = scene.controlSettings.darkness)

        // Layer 4: 粒子
        val particleConfig = scene.controlSettings.particle.toParticleConfig()
        val renderParticle =
            particleConfig.effect != ParticleEffectType.None && isCurrentPage && isScenePlaying
        if (renderParticle) {
            ParticleLayer(
                modifier = Modifier.fillMaxSize(),
                config = particleConfig,
                active = true,
            )
        }

        // Layer 5: 标题
        SceneTitleLayer(
            title = scene.title,
            subtitle = scene.subtitle,
            titleFontSize = titleFontSize,
            edgePadding = edgePadding,
            showSubtitle = scene.subtitle.isNotBlank(),
            visible = chromeVisible,
        )
    }
}

@Composable
private fun SceneDarknessOverlay(darkness: Float, modifier: Modifier = Modifier) {
    val edgeAlpha = darkness.coerceIn(0f, 1f)
    if (edgeAlpha <= 0f) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val color = Color.Black.copy(alpha = edgeAlpha)
                onDrawBehind {
                    drawRect(color = color)
                }
            },
    )
}
