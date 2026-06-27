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
import android.graphics.Paint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * 萤火虫粒子渲染器
 *
 * 暖色光点在场景中二维漂移，亮度呼吸式脉冲。
 * 与雨雪不同，萤火虫无重力下落，仅做水平+垂直随机游走。
 */
internal class SurfaceFireflyRenderer(
    private var config: ParticleConfig,
    private var fireflyConfig: FireflyConfig = fireflyConfigForQuality(config.quality),
) : SurfaceParticleRenderer {

    private val lock = Any()
    private var width = 0f
    private var height = 0f

    private val random = Random(0xF1A9C0DEL)

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = FIREFLY_COLOR
        style = Paint.Style.FILL
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = FIREFLY_COLOR
        style = Paint.Style.FILL
    }

    private var targetCount = 0
    private val fireflies = mutableListOf<Firefly>()

    override fun render(canvas: Canvas, deltaTimeMillis: Long) {
        synchronized(lock) {
            renderLocked(canvas, deltaTimeMillis)
        }
    }

    private fun renderLocked(canvas: Canvas, deltaTimeMillis: Long) {
        if (width <= 0f || height <= 0f) return

        ensureFireflies()

        val deltaSeconds = (deltaTimeMillis / 1000f).coerceAtMost(0.05f)
        update(deltaSeconds)

        val intensityValue = config.intensity.coerceIn(0f, 1f)
        if (intensityValue <= 0f) return

        fireflies.forEach { firefly ->
            // 呼吸式亮度脉冲
            val pulse = sin((firefly.lifeSeconds * firefly.pulseSpeed + firefly.phase) * TWO_PI)
            val brightness = (0.3f + 0.7f * ((pulse + 1f) / 2f)) * intensityValue

            val depthAlpha = (0.35f + firefly.depth * 0.65f)
            val alpha = (depthAlpha * brightness * 255f).roundToInt().coerceIn(0, 255)
            if (alpha <= 0) return@forEach

            // 外层光晕
            glowPaint.alpha = (alpha * 0.18f).roundToInt().coerceIn(0, 255)
            canvas.drawCircle(firefly.x, firefly.y, firefly.glowRadius, glowPaint)

            // 内层核心
            corePaint.alpha = alpha
            canvas.drawCircle(firefly.x, firefly.y, firefly.coreRadius, corePaint)
        }
    }

    override fun updateViewport(width: Int, height: Int) {
        synchronized(lock) {
            this.width = width.toFloat()
            this.height = height.toFloat()
            fireflies.clear()
            targetCount = 0
        }
    }

    override fun release() {
        synchronized(lock) {
            fireflies.clear()
            targetCount = 0
        }
    }

    fun updateConfig(config: ParticleConfig) {
        synchronized(lock) {
            val newFireflyConfig = fireflyConfigForQuality(config.quality)
            val shouldRebuild = this.config.quality != config.quality ||
                fireflyConfig != newFireflyConfig
            this.config = config
            fireflyConfig = newFireflyConfig
            if (shouldRebuild) {
                fireflies.clear()
                targetCount = 0
            }
        }
    }

    private fun ensureFireflies() {
        if (width <= 0f || height <= 0f) return

        val count = ((width * height / 1_000_000f) * fireflyConfig.firefliesPerMillionPixels)
            .roundToInt()
            .coerceIn(fireflyConfig.minFireflies, fireflyConfig.maxFireflies)

        if (count == targetCount && fireflies.isNotEmpty()) return

        targetCount = count
        fireflies.clear()
        repeat(count) {
            fireflies += createFirefly(initial = true)
        }
    }

    private fun update(deltaSeconds: Float) {
        if (width <= 0f || height <= 0f) return

        val windBias = config.wind * 30f

        fireflies.forEachIndexed { index, firefly ->
            firefly.lifeSeconds += deltaSeconds

            // 缓慢改变游走角度
            firefly.wanderAngle += firefly.wanderSpeed * deltaSeconds

            // 速度矢量：游走方向 + 风偏置
            val vx = cos(firefly.wanderAngle) * firefly.speed + windBias
            val vy = sin(firefly.wanderAngle) * firefly.speed

            firefly.x += vx * deltaSeconds
            firefly.y += vy * deltaSeconds

            // wrap 边界
            firefly.x = wrap(firefly.x, width)
            firefly.y = wrap(firefly.y, height)
        }
    }

    private fun createFirefly(initial: Boolean): Firefly {
        val depth = random.nextFloat().let { value ->
            if (value > 0.76f) {
                // 近景：更大更亮
                0.7f + random.nextFloat() * 0.3f
            } else {
                0.15f + random.nextFloat() * 0.55f
            }
        }
        val coreRadius = (0.8f + depth * 2.2f + random.nextFloat() * 0.8f)
        val glowRadius = coreRadius * (3.5f + random.nextFloat() * 1.5f)
        return Firefly(
            x = random.nextFloat() * width,
            y = random.nextFloat() * height,
            coreRadius = coreRadius,
            glowRadius = glowRadius,
            depth = depth,
            speed = random.range(8f, 32f) * (0.4f + depth),
            wanderAngle = random.nextFloat() * TWO_PI,
            wanderSpeed = random.range(0.3f, 1.2f),
            pulseSpeed = random.range(0.4f, 1.4f),
            phase = random.nextFloat(),
            lifeSeconds = random.nextFloat() * 20f,
        )
    }

    companion object {
        private const val TWO_PI = (PI * 2.0).toFloat()
        private const val FIREFLY_COLOR = 0xFFFFCC66.toInt()

        private fun Random.range(start: Float, end: Float): Float = start + nextFloat() * (end - start)

        private fun wrap(value: Float, maxValue: Float): Float {
            if (maxValue <= 0f) return value
            val wrapped = value % maxValue
            return if (wrapped < 0f) wrapped + maxValue else wrapped
        }
    }

    private data class Firefly(
        var x: Float,
        var y: Float,
        val coreRadius: Float,
        val glowRadius: Float,
        val depth: Float,
        val speed: Float,
        var wanderAngle: Float,
        val wanderSpeed: Float,
        val pulseSpeed: Float,
        val phase: Float,
        var lifeSeconds: Float,
    )
}
