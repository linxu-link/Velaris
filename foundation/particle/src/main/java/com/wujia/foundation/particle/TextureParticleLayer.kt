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
package com.wujia.foundation.particle

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.atomic.AtomicReference

/**
 * 基于 TextureView 的粒子层
 *
 * 使用独立渲染线程，不阻塞主线程。
 * TextureView 支持透明度和变换，与 Compose 集成更好。
 *
 * @param modifier Modifier
 * @param config 粒子配置
 * @param active 是否激活（仅当前页面激活时才运行动画）
 */
@Composable
fun TextureParticleLayer(
    modifier: Modifier = Modifier,
    config: ParticleConfig = ParticleConfig(),
    active: Boolean = true,
) {
    if (config.effect == ParticleEffectType.None) return

    val activeState = rememberUpdatedState(active)
    val renderer = remember(config.effect) {
        when (config.effect) {
            ParticleEffectType.Rain -> SurfaceRainRenderer(config)
            ParticleEffectType.Snow -> SurfaceSnowRenderer(config)
            ParticleEffectType.Fireflies -> SurfaceFireflyRenderer(config)
            ParticleEffectType.None -> error("ParticleEffectType.None should return before creating a renderer")
        }
    }

    val renderThread = remember(renderer) { AtomicReference<ParticleRenderThread?>(null) }

    LaunchedEffect(renderer, config) {
        when (renderer) {
            is SurfaceRainRenderer -> renderer.updateConfig(config)
            is SurfaceSnowRenderer -> renderer.updateConfig(config)
            is SurfaceFireflyRenderer -> renderer.updateConfig(config)
        }
    }

    DisposableEffect(renderer) {
        onDispose {
            renderThread.getAndSet(null)?.stopRendering()
            renderer.release()
        }
    }

    key(renderer) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false

                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            renderer.updateViewport(width, height)
                            if (activeState.value) {
                                val thread = ParticleRenderThread(Surface(surface), renderer)
                                renderThread.getAndSet(thread)?.stopRendering()
                                thread.startRendering()
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                            renderer.updateViewport(width, height)
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            renderThread.getAndSet(null)?.stopRendering()
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
                }
            },
            update = { textureView ->
                if (!active) {
                    renderThread.getAndSet(null)?.stopRendering()
                } else if (textureView.isAvailable) {
                    val currentThread = renderThread.get()
                    if (currentThread == null || !currentThread.isAlive) {
                        renderThread.compareAndSet(currentThread, null)
                        textureView.surfaceTexture?.let { surfaceTexture ->
                            val thread = ParticleRenderThread(Surface(surfaceTexture), renderer)
                            renderThread.set(thread)
                            thread.startRendering()
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize(),
        )
    }
}
