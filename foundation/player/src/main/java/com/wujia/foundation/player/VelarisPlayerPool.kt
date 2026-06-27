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
package com.wujia.foundation.player

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class VelarisPlayerPool(context: Context, private val config: VelarisPlayerConfig = VelarisPlayerConfig.Balanced) {
    private val appContext = context.applicationContext
    private val controllers = ConcurrentHashMap<Int, VelarisPlayerController>()

    fun get(page: Int): VelarisPlayerController =
        controllers.getOrPut(page) { VelarisPlayerController(appContext, config) }

    fun releaseOutsideRange(currentPage: Int, retainedPageDistance: Int = config.retainedPageDistance) {
        val pagesToRelease = controllers.keys
            .filter { page -> abs(currentPage - page) > retainedPageDistance }

        pagesToRelease.forEach { page ->
            controllers.remove(page)?.release()
        }
    }

    fun pauseAllVideos() {
        controllers.values.forEach { controller ->
            controller.pauseVideo()
        }
    }

    fun pauseVideosExcept(page: Int) {
        controllers.forEach { (controllerPage, controller) ->
            if (controllerPage != page) {
                controller.pauseVideo()
            }
        }
    }

    /**
     * 对除指定 page 之外的所有已创建控制器执行完整暂停（视频 + 音频 + 释放音频焦点）。
     * 用于页面切换时确保“上一页/其他保留页”的音频真正停止，避免相邻页（retainedPageDistance=1）音频泄漏。
     * 保留页的控制器实例仍留在池中（媒体已 prepare），只是处于暂停状态，便于快速返回时恢复。
     */
    fun pauseExcept(page: Int) {
        controllers.forEach { (controllerPage, controller) ->
            if (controllerPage != page) {
                controller.pause()
            }
        }
    }

    /**
     * 对池中所有控制器执行完整暂停（视频 + 音频）。
     * 与 pauseAllVideos 不同，此方法会同时停止音频。
     */
    fun pauseAll() {
        controllers.values.forEach { controller ->
            controller.pause()
        }
    }

    fun releaseAll() {
        controllers.values.forEach { controller ->
            controller.release()
        }
        controllers.clear()
    }
}

@Composable
fun rememberVelarisPlayerPool(): VelarisPlayerPool {
    val context = LocalContext.current
    val config = LocalVelarisPlayerConfig.current
    val pool = remember(config) { VelarisPlayerPool(context, config) }
    DisposableEffect(pool) {
        onDispose { pool.releaseAll() }
    }
    return pool
}
