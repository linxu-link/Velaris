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
package com.wujia.foundation.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun rememberContentBitmap(uri: String?, maxDimensionPx: Int = 2048): Bitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = if (uri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    val parsedUri = Uri.parse(uri)
                    // First pass: decode bounds only
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(parsedUri)?.use { boundsStream ->
                        BitmapFactory.decodeStream(boundsStream, null, options)
                    }

                    // Compute sample size
                    options.inSampleSize = calculateInSampleSize(
                        width = options.outWidth,
                        height = options.outHeight,
                        maxDimension = maxDimensionPx,
                    )
                    options.inJustDecodeBounds = false

                    // Second pass: decode with sampling
                    context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                }
            }.getOrNull()
        }
    }
    return bitmap
}

private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var inSampleSize = 1
    if (width > maxDimension || height > maxDimension) {
        while (width / inSampleSize > maxDimension || height / inSampleSize > maxDimension) {
            inSampleSize *= 2
        }
    }
    return max(inSampleSize, 1)
}
