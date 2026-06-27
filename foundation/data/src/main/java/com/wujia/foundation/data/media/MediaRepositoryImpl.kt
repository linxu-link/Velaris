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
package com.wujia.foundation.data.media

import com.wujia.foundation.model.media.MediaItem
import com.wujia.foundation.model.media.MediaRepository
import com.wujia.foundation.model.media.MediaSortOrder
import com.wujia.foundation.model.media.MediaType
import javax.inject.Inject

/**
 * [MediaRepository] 的实现类。
 *
 * 委托给 [MediaStoreDataSource] 执行实际的 MediaStore 查询。
 * 这是经典的对象组合（Composition）模式，避免继承 MediaStore API 的复杂性。
 *
 * @param mediaStoreDataSource MediaStore 数据源实例
 */
class MediaRepositoryImpl @Inject constructor(private val mediaStoreDataSource: MediaStoreDataSource) :
    MediaRepository {

    override suspend fun getImages(sortOrder: MediaSortOrder, limit: Int?, offset: Int?): List<MediaItem> =
        mediaStoreDataSource.queryImages(sortOrder, limit, offset)

    override suspend fun getVideos(sortOrder: MediaSortOrder, limit: Int?, offset: Int?): List<MediaItem> =
        mediaStoreDataSource.queryVideos(sortOrder, limit, offset)

    override suspend fun getAudios(sortOrder: MediaSortOrder, limit: Int?, offset: Int?): List<MediaItem> =
        mediaStoreDataSource.queryAudios(sortOrder, limit, offset)

    override suspend fun getAllMedia(sortOrder: MediaSortOrder, limit: Int?, offset: Int?): List<MediaItem> =
        mediaStoreDataSource.queryAllMedia(sortOrder, limit, offset)

    override suspend fun getMediaByType(
        mediaType: MediaType,
        sortOrder: MediaSortOrder,
        limit: Int?,
        offset: Int?,
    ): List<MediaItem> = mediaStoreDataSource.queryMedia(mediaType, sortOrder, limit, offset)
}
