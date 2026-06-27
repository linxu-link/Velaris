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

/**
 * SurfaceView 粒子渲染器接口
 *
 * 在独立渲染线程上调用，实现 Canvas 绘制。
 */
internal interface SurfaceParticleRenderer {
    /**
     * 渲染一帧
     *
     * @param canvas Surface 的 Canvas
     * @param deltaTimeMillis 上一帧到当前帧的时间间隔（毫秒）
     */
    fun render(canvas: Canvas, deltaTimeMillis: Long)

    /**
     * 更新视口大小
     *
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    fun updateViewport(width: Int, height: Int)

    /**
     * 释放资源
     */
    fun release()
}
