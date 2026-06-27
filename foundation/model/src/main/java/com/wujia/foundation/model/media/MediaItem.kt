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
package com.wujia.foundation.model.media

data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val duration: Long,
    val bucketId: String?,
    val bucketName: String?,
) {
    val isVideo: Boolean
        get() = mimeType.startsWith("video/")

    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isAudio: Boolean
        get() = mimeType.startsWith("audio/")

    val formattedDuration: String
        get() {
            if (duration <= 0) return ""
            val seconds = (duration / 1000) % 60
            val minutes = (duration / (1000 * 60)) % 60
            val hours = duration / (1000 * 60 * 60)
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }
}

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    ALL,
}

enum class MediaSortOrder {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC,
}
