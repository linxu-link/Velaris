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

import android.graphics.Canvas
import android.view.Surface
import android.view.SurfaceHolder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 渲染目标抽象接口
 *
 * 统一渲染目标的访问方式，支持 Surface (TextureView)
 */
internal interface RenderTarget {
    fun lockCanvas(): Canvas?
    fun unlockCanvasAndPost(canvas: Canvas)
}

/**
 * SurfaceHolder 适配器（用于 SurfaceView，已弃用）
 *
 * @deprecated 项目已迁移到 TextureView，请使用 SurfaceTarget
 */
@Deprecated("项目已迁移到 TextureView，请使用 SurfaceTarget")
internal class SurfaceHolderTarget(private val holder: SurfaceHolder) : RenderTarget {
    override fun lockCanvas(): Canvas? = holder.lockCanvas()
    override fun unlockCanvasAndPost(canvas: Canvas) = holder.unlockCanvasAndPost(canvas)
}

/**
 * Surface 适配器（用于 TextureView）
 */
internal class SurfaceTarget(private val surface: Surface) : RenderTarget {
    override fun lockCanvas(): Canvas? = try {
        surface.lockCanvas(null)
    } catch (e: Exception) {
        null
    }

    override fun unlockCanvasAndPost(canvas: Canvas) {
        try {
            surface.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {
            // 忽略
        }
    }
}

/**
 * 粒子效果渲染线程
 *
 * 管理独立的渲染循环，避免阻塞主线程。
 * 使用 TextureView + Surface 实现高性能渲染。
 */
internal class ParticleRenderThread(
    private val renderTarget: RenderTarget,
    private val renderer: SurfaceParticleRenderer,
) : Thread("ParticleRenderThread") {

    /**
     * 便捷构造函数：从 SurfaceHolder 创建（已弃用）
     *
     * @deprecated 项目已迁移到 TextureView，请使用 Surface 构造函数
     */
    @Deprecated("项目已迁移到 TextureView，请使用 Surface 构造函数")
    constructor(surfaceHolder: SurfaceHolder, renderer: SurfaceParticleRenderer) :
        this(SurfaceHolderTarget(surfaceHolder), renderer)

    /**
     * 便捷构造函数：从 Surface 创建（用于 TextureView）
     */
    constructor(surface: Surface, renderer: SurfaceParticleRenderer) :
        this(SurfaceTarget(surface), renderer)

    private val running = AtomicBoolean(false)
    private var lastFrameTimeNanos = 0L

    fun startRendering() {
        running.set(true)
        start()
    }

    fun stopRendering() {
        running.set(false)
        try {
            join(1000) // 等待线程结束，最多 1 秒
        } catch (e: InterruptedException) {
            // 忽略中断
        }
    }

    override fun run() {
        lastFrameTimeNanos = System.nanoTime()

        while (running.get()) {
            val currentTimeNanos = System.nanoTime()
            val deltaTimeNanos = currentTimeNanos - lastFrameTimeNanos
            lastFrameTimeNanos = currentTimeNanos

            // 转换为毫秒
            val deltaTimeMillis = deltaTimeNanos / 1_000_000L

            var canvas: Canvas? = null
            try {
                canvas = renderTarget.lockCanvas()
                if (canvas != null) {
                    // 清除上一帧
                    canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

                    // 渲染当前帧
                    renderer.render(canvas, deltaTimeMillis)
                }
            } catch (e: Exception) {
                // Surface 可能已被销毁
                running.set(false)
            } finally {
                if (canvas != null) {
                    try {
                        renderTarget.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        // 忽略
                    }
                }
            }

            // 控制帧率（目标 60fps，每帧约 16ms）
            val frameTimeMillis = (System.nanoTime() - currentTimeNanos) / 1_000_000L
            val sleepTime = (16 - frameTimeMillis).coerceAtLeast(1)
            try {
                sleep(sleepTime)
            } catch (e: InterruptedException) {
                running.set(false)
            }
        }
    }
}
