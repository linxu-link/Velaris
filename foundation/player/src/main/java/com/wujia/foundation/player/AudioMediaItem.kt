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

/**
 * 音频媒体项数据类，用于描述一个可播放的音频资源。
 *
 * @param id 音频的唯一标识符，不能为空
 * @param uri 音频资源的 URI，支持网络 URL 或本地资源路径
 * @param volume 播放音量，范围 0f ~ 1f，默认为 1f（最大音量）
 * @param title 音频的显示标题，默认为 id
 * @param loop 是否循环播放，默认为 false
 * @param stopAllOnEnd 播放结束时是否停止所有音频，默认为 false
 */
data class AudioMediaItem(
    val id: String,
    val uri: String,
    val volume: Float = DEFAULT_VOLUME,
    val title: String = id,
    val loop: Boolean = false,
    val stopAllOnEnd: Boolean = false,
    val noFocus: Boolean = false,
) {
    init {
        require(id.isNotBlank()) { "音频 id 不能为空。" }
        require(uri.isNotBlank()) { "音频 URI 不能为空。" }
    }
}

/**
 * 校验列表中的音频 id 是否唯一，如有重复则抛出异常。
 */
internal fun List<AudioMediaItem>.requireUniqueAudioIds() {
    val duplicateId = groupingBy { it.id }.eachCount().entries.firstOrNull { it.value > 1 }?.key
    require(duplicateId == null) { "音频 id 必须唯一: $duplicateId。" }
}

/** 默认音量值（最大音量） */
internal const val DEFAULT_VOLUME = 1f

/** 最小音量值 */
internal const val MIN_VOLUME = 0f

/** 最大音量值 */
internal const val MAX_VOLUME = 1f

/**
 * 将音量值限制在有效范围内 [MIN_VOLUME, MAX_VOLUME]。
 */
internal fun Float.coercePlayerVolume(): Float = coerceIn(MIN_VOLUME, MAX_VOLUME)
