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
 * SurfaceView 雪渲染器
 *
 * 在独立线程上执行 Canvas 绘制，不阻塞主线程。
 * 从 SnowParticleLayer.kt 移植，移除 Compose 依赖。
 */
internal class SurfaceSnowRenderer(
    private var config: ParticleConfig,
    private var snowConfig: SnowConfig = snowConfigForQuality(config.quality),
) : SurfaceParticleRenderer {

    private val lock = Any()
    private var width = 0f
    private var height = 0f

    private val random = Random(0x5A17C0DEL)
    private val flakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.FILL
    }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val backgroundPaint = Paint().apply {
        color = WHITE
        style = Paint.Style.FILL
    }

    private var targetCount = 0
    private val flakes = mutableListOf<SnowFlake>()

    override fun render(canvas: Canvas, deltaTimeMillis: Long) {
        synchronized(lock) {
            renderLocked(canvas, deltaTimeMillis)
        }
    }

    private fun renderLocked(canvas: Canvas, deltaTimeMillis: Long) {
        if (width <= 0f || height <= 0f) return

        ensureFlakes()

        val deltaSeconds = (deltaTimeMillis / 1000f).coerceAtMost(0.05f)
        update(deltaSeconds)

        val intensityValue = config.intensity.coerceIn(0f, 1f)
        if (intensityValue <= 0f) return

        // 背景薄雾
        backgroundPaint.alpha = (255f * 0.014f * intensityValue).roundToInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, width, height, backgroundPaint)

        // 雪花
        flakes.forEach { flake ->
            val flutter = sin((flake.lifeSeconds * flake.flutterSpeed + flake.phase) * TWO_PI)
            val slowSway = sin((flake.lifeSeconds * 0.21f + flake.phase * 0.37f) * TWO_PI)
            val x = wrap(
                value = flake.x + flutter * flake.flutterAmplitude + slowSway * flake.driftAmplitude,
                maxValue = width,
            )
            val y = wrap(flake.y, height)
            val alpha = ((0.2f + flake.depth * 0.58f) * flake.opacity * intensityValue * 255f)
                .roundToInt()
                .coerceIn(0, 230)

            // 近景雪花拖尾
            if (flake.isNear) {
                trailPaint.alpha = (alpha * 0.3f).roundToInt().coerceIn(0, 255)
                trailPaint.strokeWidth = max(1f, flake.radius * 0.42f)
                canvas.drawLine(
                    x - flake.windSpeed * 0.035f,
                    y - flake.fallSpeed * 0.045f,
                    x,
                    y,
                    trailPaint,
                )
                // 近景光晕
                flakePaint.alpha = (alpha * 0.14f).roundToInt().coerceIn(0, 255)
                canvas.drawCircle(x, y, flake.radius * 1.8f, flakePaint)
            }

            // 雪花主体
            flakePaint.alpha = alpha
            canvas.drawCircle(x, y, flake.radius, flakePaint)
        }
    }

    override fun updateViewport(width: Int, height: Int) {
        synchronized(lock) {
            this.width = width.toFloat()
            this.height = height.toFloat()
            flakes.clear()
            targetCount = 0
        }
    }

    override fun release() {
        synchronized(lock) {
            flakes.clear()
            targetCount = 0
        }
    }

    fun updateConfig(config: ParticleConfig) {
        synchronized(lock) {
            val newSnowConfig = snowConfigForQuality(config.quality)
            val shouldRebuild = this.config.wind != config.wind ||
                this.config.quality != config.quality ||
                snowConfig != newSnowConfig
            this.config = config
            snowConfig = newSnowConfig
            if (shouldRebuild) {
                flakes.clear()
                targetCount = 0
            }
        }
    }

    private fun ensureFlakes() {
        if (width <= 0f || height <= 0f) return

        val count = ((width * height / 1_000_000f) * snowConfig.flakesPerMillionPixels)
            .roundToInt()
            .coerceIn(snowConfig.minFlakes, snowConfig.maxFlakes)

        if (count == targetCount && flakes.isNotEmpty()) return

        targetCount = count
        flakes.clear()
        repeat(count) {
            flakes += createFlake(initial = true)
        }
    }

    private fun update(deltaSeconds: Float) {
        if (width <= 0f || height <= 0f) return

        flakes.forEachIndexed { index, flake ->
            flake.lifeSeconds += deltaSeconds
            flake.y += flake.fallSpeed * deltaSeconds
            flake.x += (flake.windSpeed + flake.gustOffset) * deltaSeconds

            val offscreenPadding = flake.radius * 8f
            if (flake.y > height + offscreenPadding ||
                flake.x < -offscreenPadding ||
                flake.x > width + offscreenPadding
            ) {
                flakes[index] = createFlake(initial = false)
            }
        }
    }

    private fun createFlake(initial: Boolean): SnowFlake {
        val depth = random.nextFloat().let { value ->
            if (value > 1f - snowConfig.nearFlakeRatio.coerceIn(0f, 0.5f)) {
                0.72f + random.nextFloat() * 0.28f
            } else {
                0.12f + random.nextFloat() * 0.68f
            }
        }
        val sizeMultiplier = depth * depth
        val radius = 0.65f + sizeMultiplier * 3.6f + random.nextFloat() * 1.2f
        val fallSpeed = (34f + depth * 230f) * random.range(0.74f, 1.22f)
        val windSpeed = (config.wind * 150f + random.range(-18f, 18f)) * (0.25f + depth)
        return SnowFlake(
            x = random.nextFloat() * width,
            y = if (initial) random.nextFloat() * height else -radius * random.range(2f, 10f),
            radius = radius,
            fallSpeed = fallSpeed,
            windSpeed = windSpeed,
            flutterAmplitude = random.range(6f, 22f) * (0.45f + depth),
            flutterSpeed = random.range(0.07f, 0.34f),
            driftAmplitude = random.range(10f, 48f) * depth,
            gustOffset = random.range(-10f, 24f) * (0.2f + depth),
            opacity = random.range(0.45f, 0.95f),
            depth = depth,
            phase = random.nextFloat(),
            lifeSeconds = random.nextFloat() * 24f,
            isNear = depth > 0.74f,
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

    private data class SnowFlake(
        var x: Float,
        var y: Float,
        val radius: Float,
        val fallSpeed: Float,
        val windSpeed: Float,
        val flutterAmplitude: Float,
        val flutterSpeed: Float,
        val driftAmplitude: Float,
        val gustOffset: Float,
        val opacity: Float,
        val depth: Float,
        val phase: Float,
        var lifeSeconds: Float,
        val isNear: Boolean,
    )
}
