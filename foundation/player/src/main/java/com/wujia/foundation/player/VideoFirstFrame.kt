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
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 提取并缓存视频首帧，用作轻量级预览图。
 *
 * 首次提取在主线程外运行，并按 URI 缓存在内存中。
 * 调用方仍需处理空值结果，因为编解码器、无效 URI 或远程资源
 * 可能无法同步提供帧数据。
 */
@Composable
fun rememberVideoFirstFrame(videoUri: String?): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    val config = LocalVelarisPlayerConfig.current
    VideoFirstFrameCache.configure(config.firstFrameCacheSizeKb)
    val firstFrame by produceState<ImageBitmap?>(
        initialValue = VideoFirstFrameCache.get(videoUri)?.asImageBitmap(),
        key1 = videoUri,
    ) {
        val uri = videoUri?.takeIf { it.isNotBlank() } ?: run {
            value = null
            return@produceState
        }

        VideoFirstFrameCache.get(uri)?.let { cached ->
            value = cached.asImageBitmap()
            return@produceState
        }

        value = withContext(Dispatchers.IO) {
            extractVideoFirstFrame(context, uri)
        }?.also { bitmap ->
            VideoFirstFrameCache.put(uri, bitmap)
        }?.asImageBitmap()
    }

    return firstFrame
}

private object VideoFirstFrameCache {
    @Volatile
    private var cache = createCache(12 * 1024)

    private fun createCache(maxSizeKb: Int) = object : LruCache<String, Bitmap>(maxSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = maxOf(1, value.byteCount / 1024)
    }

    fun configure(maxSizeKb: Int) {
        if (cache.maxSize() != maxSizeKb) {
            cache = createCache(maxSizeKb)
        }
    }

    fun get(uri: String?): Bitmap? = uri?.let(cache::get)

    fun put(uri: String, bitmap: Bitmap) {
        cache.put(uri, bitmap)
    }
}

private fun extractVideoFirstFrame(context: Context, videoUri: String): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri.toUri())
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: RuntimeException) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}
