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
package com.wujia.foundation.domain.media

import com.wujia.foundation.model.media.MediaItem
import com.wujia.foundation.model.media.MediaRepository
import com.wujia.foundation.model.media.MediaSortOrder
import javax.inject.Inject

/**
 * 获取本地音频媒体列表。
 *
 * 支持排序和分页参数，供媒体选择面板按需加载。
 */
class GetMediaAudiosUseCase @Inject constructor(private val mediaRepository: MediaRepository) {
    suspend operator fun invoke(
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = mediaRepository.getAudios(sortOrder, limit, offset)
}
