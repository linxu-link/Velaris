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
package com.wujia.feature.sceneedit.impl.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * 编辑器本地媒体持久化入口。
 *
 * 系统选择器返回的 content:// 可能只具备临时读权限，编辑器保存前统一复制到应用私有目录，
 * 之后 UiState 和数据库只保存稳定的 file://，避免进程重启后素材不可读。
 */
internal class LocalSceneEditMediaStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun copyPickedMediaToInternalStorage(sourceUri: Uri, prefix: String): Uri = withContext(Dispatchers.IO) {
        // SAF provider 支持持久授权时尽量保留；Photo Picker 等不支持时忽略即可。
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                sourceUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }

        val input = context.contentResolver.openInputStream(sourceUri)
            ?: error("无法打开选择器 URI 的输入流: $sourceUri")

        input.use { ins ->
            val mediaDir = File(context.filesDir, "scene_local_media")
            if (!mediaDir.exists()) mediaDir.mkdirs()

            val ext = sourceUri.lastPathSegment?.substringAfterLast('.', "dat") ?: "dat"
            val destFile = File(mediaDir, "${prefix}_${UUID.randomUUID()}.$ext")

            FileOutputStream(destFile).use { outs ->
                ins.copyTo(outs)
            }

            Uri.fromFile(destFile)
        }
    }
}
