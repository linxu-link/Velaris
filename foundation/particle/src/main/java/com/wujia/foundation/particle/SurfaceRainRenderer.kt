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
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * SurfaceView 雨渲染器
 *
 * 在独立线程上执行 Canvas 绘制，不阻塞主线程。
 * 从 RainParticleLayer.kt 移植，移除 Compose 依赖。
 */
internal class SurfaceRainRenderer(
    private var config: ParticleConfig,
    private var rainConfig: RainConfig = rainConfigForQuality(config.quality),
) : SurfaceParticleRenderer {

    private val lock = Any()
    private var width = 0f
    private var height = 0f

    private val random = Random(0x6A11FA17L)
    private val rainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val mistPaint = Paint().apply {
        color = WHITE
        style = Paint.Style.FILL
    }
    private val splashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val beadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.FILL
    }

    private var targetCount = 0
    private val drops = mutableListOf<RainDrop>()
    private val streaks = mutableListOf<ScreenWaterStreak>()

    override fun render(canvas: Canvas, deltaTimeMillis: Long) {
        synchronized(lock) {
            renderLocked(canvas, deltaTimeMillis)
        }
    }

    private fun renderLocked(canvas: Canvas, deltaTimeMillis: Long) {
        if (width <= 0f || height <= 0f) return

        ensureDrops()

        val deltaSeconds = (deltaTimeMillis / 1000f).coerceAtMost(0.05f)
        update(deltaSeconds)

        val intensityValue = config.intensity.coerceIn(0f, 1f)
        if (intensityValue <= 0f) return

        // 薄雾
        mistPaint.alpha = (255f * 0.018f * intensityValue).roundToInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, width, height, mistPaint)

        // 雨滴
        drops.forEach { drop ->
            val depth = drop.depth
            val alpha = ((0.16f + depth * 0.58f) * drop.opacity * intensityValue * 255f)
                .roundToInt()
                .coerceIn(0, 210)
            val x = wrap(drop.x, width)
            val y = wrap(drop.y, height)
            val windOffset = drop.windSpeed * drop.length / drop.fallSpeed
            val startX = x - windOffset
            val startY = y - drop.length

            rainPaint.alpha = alpha
            rainPaint.strokeWidth = drop.strokeWidth
            canvas.drawLine(startX, startY, x, y, rainPaint)

            if (drop.hasSplash && y > height * 0.78f) {
                val splashProgress = ((drop.lifeSeconds * drop.splashSpeed + drop.phase) % 1f)
                val splashAlpha = (alpha * (1f - splashProgress) * 0.42f).roundToInt().coerceIn(0, 130)
                if (splashAlpha > 8) {
                    val splashWidth = drop.splashWidth * (0.45f + splashProgress)
                    val splashY = y + drop.strokeWidth * 1.5f
                    splashPaint.alpha = splashAlpha
                    splashPaint.strokeWidth = max(0.7f, drop.strokeWidth * 0.56f)
                    canvas.drawLine(
                        x - splashWidth,
                        splashY,
                        x - splashWidth * 0.28f,
                        splashY - splashWidth * 0.18f,
                        splashPaint,
                    )
                    canvas.drawLine(
                        x + splashWidth * 0.28f,
                        splashY - splashWidth * 0.18f,
                        x + splashWidth,
                        splashY,
                        splashPaint,
                    )
                }
            }
        }

        // 屏幕水痕
        streaks.forEach { streak ->
            val x = wrap(streak.x, width)
            val y = streak.y
            val alpha = (streak.opacity * intensityValue * 255f).roundToInt().coerceIn(0, 82)
            val topAlpha = (alpha * 0.32f).roundToInt().coerceIn(0, 255)
            val endX = x + streak.slant
            val startY = y - streak.length

            streakPaint.strokeWidth = streak.width
            streakPaint.alpha = topAlpha
            canvas.drawLine(x - streak.slant * 0.45f, startY, x, y - streak.length * 0.58f, streakPaint)
            streakPaint.alpha = alpha
            canvas.drawLine(x, y - streak.length * 0.58f, endX, y, streakPaint)

            beadPaint.alpha = (alpha * 0.78f).roundToInt().coerceIn(0, 255)
            canvas.drawCircle(endX, y, streak.width * 1.25f, beadPaint)
            if (streak.secondaryBead > 0f) {
                beadPaint.alpha = (alpha * 0.36f).roundToInt().coerceIn(0, 255)
                canvas.drawCircle(
                    x - streak.slant * 0.2f,
                    y - streak.length * streak.secondaryBead,
                    streak.width * 0.72f,
                    beadPaint,
                )
            }
        }
    }

    override fun updateViewport(width: Int, height: Int) {
        synchronized(lock) {
            this.width = width.toFloat()
            this.height = height.toFloat()
            drops.clear()
            streaks.clear()
            targetCount = 0
        }
    }

    override fun release() {
        synchronized(lock) {
            drops.clear()
            streaks.clear()
            targetCount = 0
        }
    }

    fun updateConfig(config: ParticleConfig) {
        synchronized(lock) {
            val newRainConfig = rainConfigForQuality(config.quality)
            val shouldRebuild = this.config.wind != config.wind ||
                this.config.quality != config.quality ||
                this.config.foregroundGlassEnabled != config.foregroundGlassEnabled ||
                rainConfig != newRainConfig
            this.config = config
            rainConfig = newRainConfig
            if (shouldRebuild) {
                drops.clear()
                streaks.clear()
                targetCount = 0
            }
        }
    }

    private fun ensureDrops() {
        if (width <= 0f || height <= 0f) return

        val count = ((width * height / 1_000_000f) * rainConfig.dropsPerMillionPixels)
            .roundToInt()
            .coerceIn(rainConfig.minDrops, rainConfig.maxDrops)

        if (count == targetCount && drops.isNotEmpty()) return

        targetCount = count
        drops.clear()
        repeat(count) {
            drops += createDrop(initial = true)
        }
        streaks.clear()
        if (config.foregroundGlassEnabled) {
            repeat(rainConfig.screenStreakCount.coerceIn(0, 48)) {
                streaks += createStreak(initial = true)
            }
        }
    }

    private fun update(deltaSeconds: Float) {
        if (width <= 0f || height <= 0f) return

        drops.forEachIndexed { index, drop ->
            drop.lifeSeconds += deltaSeconds
            drop.x += drop.windSpeed * deltaSeconds
            drop.y += drop.fallSpeed * deltaSeconds
            if (drop.y > height + drop.length || drop.x < -drop.length || drop.x > width + drop.length) {
                drops[index] = createDrop(initial = false)
            }
        }

        streaks.forEachIndexed { index, streak ->
            streak.lifeSeconds += deltaSeconds
            streak.y += streak.speed * deltaSeconds
            streak.x +=
                sin((streak.lifeSeconds * streak.wobbleSpeed + streak.phase) * TWO_PI) * streak.wobble * deltaSeconds
            if (streak.y - streak.length > height) {
                streaks[index] = createStreak(initial = false)
            }
        }
    }

    private fun createDrop(initial: Boolean): RainDrop {
        val depth = random.range(0.16f, 1f)
        val fallSpeed = (740f + depth * 1260f) * random.range(0.78f, 1.2f)
        val windSpeed = (config.wind * 520f + random.range(-36f, 38f)) * (0.35f + depth)
        val length = (18f + depth * 46f) * random.range(0.78f, 1.24f)
        return RainDrop(
            x = random.nextFloat() * width,
            y = if (initial) random.nextFloat() * height else -length * random.range(1f, 7f),
            length = length,
            fallSpeed = fallSpeed,
            windSpeed = windSpeed,
            strokeWidth = 0.65f + depth * 1.85f,
            opacity = random.range(0.42f, 0.92f),
            depth = depth,
            phase = random.nextFloat(),
            lifeSeconds = random.nextFloat() * 8f,
            hasSplash = random.nextFloat() < rainConfig.splashRatio.coerceIn(0f, 0.5f),
            splashWidth = random.range(3f, 11f) * (0.6f + depth),
            splashSpeed = random.range(1.8f, 3.6f),
        )
    }

    private fun createStreak(initial: Boolean): ScreenWaterStreak {
        val length = random.range(height * 0.08f, height * 0.32f)
        val streakWidth = random.range(1.2f, 3.8f)
        return ScreenWaterStreak(
            x = random.nextFloat() * width,
            y = if (initial) random.nextFloat() * height else -length * random.range(0.4f, 1.8f),
            length = length,
            width = streakWidth,
            speed = random.range(18f, 82f),
            slant = random.range(-8f, 16f) + config.wind * 22f,
            opacity = random.range(0.08f, 0.22f),
            phase = random.nextFloat(),
            wobble = random.range(0.8f, 3.2f),
            wobbleSpeed = random.range(0.05f, 0.16f),
            secondaryBead = if (random.nextFloat() < 0.58f) random.range(0.22f, 0.78f) else 0f,
            lifeSeconds = random.nextFloat() * 12f,
        )
    }

    companion object {
        private const val TWO_PI = (PI * 2.0).toFloat()
        private const val WHITE = 0xFFFFFFFF.toInt()

        private fun Random.range(start: Float, end: Float): Float = start + nextFloat() * (end - start)

        private fun wrap(value: Float, maxValue: Float): Float {
            if (maxValue <= 0f) return value
            val wrapped = value % maxValue
            return if (wrapped < 0f) wrapped + maxValue else wrapped
        }
    }

    private data class RainDrop(
        var x: Float,
        var y: Float,
        val length: Float,
        val fallSpeed: Float,
        val windSpeed: Float,
        val strokeWidth: Float,
        val opacity: Float,
        val depth: Float,
        val phase: Float,
        var lifeSeconds: Float,
        val hasSplash: Boolean,
        val splashWidth: Float,
        val splashSpeed: Float,
    )

    private data class ScreenWaterStreak(
        var x: Float,
        var y: Float,
        val length: Float,
        val width: Float,
        val speed: Float,
        val slant: Float,
        val opacity: Float,
        val phase: Float,
        val wobble: Float,
        val wobbleSpeed: Float,
        val secondaryBead: Float,
        var lifeSeconds: Float,
    )
}
