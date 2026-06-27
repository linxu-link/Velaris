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
package com.wujia.foundation.data.video

/**
 * 视频资源的 data 层内部模型。
 *
 * rawResId 由 Repository 层转换为 android.resource URI，
 * 上层只消费 URI，不直接接触 Android 资源 ID。
 */
internal data class LocalVideoResource(
    val id: String,
    val title: String,
    val description: String,
    val rawResId: Int,
    val thumbnailResName: String? = null,
)
