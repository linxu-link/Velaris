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

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.wujia.foundation.model.media.MediaItem
import com.wujia.foundation.model.media.MediaSortOrder
import com.wujia.foundation.model.media.MediaType
import com.wujia.foundation.toolkit.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * 使用 MediaStore API 查询设备媒体（图片和视频）的数据源。
 *
 * MediaStore 是 Android 系统提供的媒体数据库，通过 ContentResolver 查询。
 * 支持 Android Q（API 29）的 Scoped Storage 特性。
 *
 * @param contentResolver 用于查询媒体库的 ContentResolver，通常来自 ApplicationContext
 * @param ioDispatcher 用于 IO 密集型操作的协程调度器
 */
class MediaStoreDataSource(
    private val contentResolver: ContentResolver,
    private val ioDispatcher: CoroutineDispatcher,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ) : this(context.contentResolver, ioDispatcher)

    companion object {
        // 图片集合 URI，根据系统版本选择合适的 VOLUME
        private val IMAGE_COLLECTION: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        // 视频集合 URI
        private val VIDEO_COLLECTION: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        private val AUDIO_COLLECTION: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        private const val MEDIA_STORE_TAG = "VelarisMediaStore"
    }

    /**
     * 从设备查询所有图片。
     * @param sortOrder 排序方式，默认为按添加日期降序
     * @param limit 返回数量上限，null 表示不限制
     */
    suspend fun queryImages(
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = withContext(ioDispatcher) {
        queryMedia(
            collection = IMAGE_COLLECTION,
            mimeTypePrefix = "image/",
            sortOrder = sortOrder,
            limit = limit,
            offset = offset,
        )
    }

    /**
     * 从设备查询所有视频。
     * @param sortOrder 排序方式，默认为按添加日期降序
     * @param limit 返回数量上限，null 表示不限制
     */
    suspend fun queryVideos(
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = withContext(ioDispatcher) {
        queryMedia(
            collection = VIDEO_COLLECTION,
            mimeTypePrefix = "video/",
            sortOrder = sortOrder,
            limit = limit,
            offset = offset,
        )
    }

    suspend fun queryAudios(
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = withContext(ioDispatcher) {
        queryMedia(
            collection = AUDIO_COLLECTION,
            mimeTypePrefix = "audio/",
            sortOrder = sortOrder,
            limit = limit,
            offset = offset,
        )
    }

    /**
     * 从设备查询所有媒体（图片和视频）。
     * 分别查询后合并，再按指定排序规则统一排序。
     * @param sortOrder 排序方式，默认为按添加日期降序
     * @param limit 返回数量上限，null 表示不限制
     */
    suspend fun queryAllMedia(
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = withContext(ioDispatcher) {
        val queryLimit = when {
            limit == null && offset == null -> null
            else -> (limit ?: 0) + (offset ?: 0)
        }
        val images = queryMedia(IMAGE_COLLECTION, "image/", sortOrder, queryLimit, null)
        val videos = queryMedia(VIDEO_COLLECTION, "video/", sortOrder, queryLimit, null)
        val audios = queryMedia(AUDIO_COLLECTION, "audio/", sortOrder, queryLimit, null)
        val combined = images + videos + audios
        sortMediaList(combined, sortOrder).let { sorted ->
            val sliced = if (offset != null && offset > 0) {
                sorted.drop(offset)
            } else {
                sorted
            }
            if (limit != null) sliced.take(limit) else sliced
        }
    }

    /**
     * 按媒体类型查询。
     * @param mediaType 媒体类型筛选
     * @param sortOrder 排序方式
     * @param limit 返回数量上限
     */
    suspend fun queryMedia(
        mediaType: MediaType,
        sortOrder: MediaSortOrder = MediaSortOrder.DATE_DESC,
        limit: Int? = null,
        offset: Int? = null,
    ): List<MediaItem> = withContext(ioDispatcher) {
        when (mediaType) {
            MediaType.IMAGE -> queryImages(sortOrder, limit, offset)
            MediaType.VIDEO -> queryVideos(sortOrder, limit, offset)
            MediaType.AUDIO -> queryAudios(sortOrder, limit, offset)
            MediaType.ALL -> queryAllMedia(sortOrder, limit, offset)
        }
    }

    /**
     * 通用的媒体查询方法，将 MediaStore Cursor 映射为 [MediaItem] 列表。
     *
     * 查询策略：
     * - 仅查询 size > 0 的文件（过滤损坏或无效条目）
     * - 投影列固定，减少不必要的字段读取
     * - 时长字段仅在查询视频时包含
     * - limit 通过 SQL LIMIT 子句实现，高效截断
     */
    private fun queryMedia(
        collection: Uri,
        mimeTypePrefix: String,
        sortOrder: MediaSortOrder,
        limit: Int?,
        offset: Int?,
    ): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        Timber.tag(MEDIA_STORE_TAG).d(
            "query start collection=%s mimeTypePrefix=%s sortOrder=%s limit=%s offset=%s sdk=%d",
            collection,
            mimeTypePrefix,
            sortOrder,
            limit,
            offset,
            Build.VERSION.SDK_INT,
        )

        // 投影列：只查询必要的字段
        val isVisualMedia = mimeTypePrefix == "image/" || mimeTypePrefix == "video/"
        val projection = buildList {
            add(MediaStore.MediaColumns._ID)
            add(MediaStore.MediaColumns.DISPLAY_NAME)
            add(MediaStore.MediaColumns.MIME_TYPE)
            add(MediaStore.MediaColumns.SIZE)
            add(MediaStore.MediaColumns.DATE_ADDED)
            add(MediaStore.MediaColumns.DATE_MODIFIED)
            if (isVisualMedia) {
                add(MediaStore.MediaColumns.WIDTH)
                add(MediaStore.MediaColumns.HEIGHT)
                add(MediaStore.MediaColumns.BUCKET_ID)
                add(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            }
            if (mimeTypePrefix == "video/" || mimeTypePrefix == "audio/") {
                add(MediaStore.MediaColumns.DURATION)
            }
        }.toTypedArray()

        val selection = "${MediaStore.MediaColumns.SIZE} > 0 AND ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?"
        val selectionArgs = arrayOf("$mimeTypePrefix%")

        // 排序列映射
        val sortColumn = when (sortOrder) {
            MediaSortOrder.DATE_DESC, MediaSortOrder.DATE_ASC -> MediaStore.MediaColumns.DATE_ADDED
            MediaSortOrder.NAME_ASC, MediaSortOrder.NAME_DESC -> MediaStore.MediaColumns.DISPLAY_NAME
            MediaSortOrder.SIZE_DESC, MediaSortOrder.SIZE_ASC -> MediaStore.MediaColumns.SIZE
        }

        // 排序方向
        val sortDirection = when (sortOrder) {
            MediaSortOrder.DATE_DESC, MediaSortOrder.SIZE_DESC -> "DESC"
            else -> "ASC"
        }
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(sortColumn))
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    if (sortDirection == "DESC") {
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    } else {
                        ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
                    },
                )
                if (limit != null && limit > 0) {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                }
                if (offset != null && offset > 0) {
                    putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                }
            }
            contentResolver.query(collection, projection, queryArgs, null)
        } else {
            val limitClause = when {
                limit != null && limit > 0 && offset != null && offset > 0 -> " LIMIT $limit OFFSET $offset"
                limit != null && limit > 0 -> " LIMIT $limit"
                offset != null && offset > 0 -> " OFFSET $offset"
                else -> ""
            }
            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "$sortColumn $sortDirection$limitClause",
            )
        }

        cursor?.use { cursor ->
            Timber.tag(MEDIA_STORE_TAG).d(
                "query cursor collection=%s mimeTypePrefix=%s cursorCount=%d",
                collection,
                mimeTypePrefix,
                cursor.count,
            )
            // 提前获取列索引，避免每行重复查找
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val widthColumn = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
            val heightColumn = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
            val bucketIdColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val durationColumn = if (mimeTypePrefix == "video/" || mimeTypePrefix == "audio/") {
                cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                // 构建 content:// URI
                val uri = ContentUris.withAppendedId(collection, id).toString()

                val duration = if (durationColumn >= 0) {
                    cursor.getLong(durationColumn)
                } else {
                    0L
                }

                mediaItems.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        displayName = cursor.getString(displayNameColumn) ?: "Unknown",
                        mimeType = cursor.getString(mimeTypeColumn) ?: "$mimeTypePrefix*",
                        size = cursor.getLong(sizeColumn),
                        dateAdded = cursor.getLong(dateAddedColumn),
                        dateModified = cursor.getLong(dateModifiedColumn),
                        width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0,
                        height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0,
                        duration = duration,
                        bucketId = if (bucketIdColumn >= 0) cursor.getString(bucketIdColumn) else null,
                        bucketName = if (bucketNameColumn >= 0) cursor.getString(bucketNameColumn) else null,
                    ),
                )
            }
        } ?: Timber.tag(MEDIA_STORE_TAG).w(
            "query cursor null collection=%s mimeTypePrefix=%s",
            collection,
            mimeTypePrefix,
        )

        val result = if (limit != null && limit > 0) {
            mediaItems.take(limit)
        } else {
            mediaItems
        }
        Timber.tag(MEDIA_STORE_TAG).d(
            "query result mimeTypePrefix=%s count=%d first=%s",
            mimeTypePrefix,
            result.size,
            result.take(5).map { "${it.id}:${it.mimeType}:${it.displayName}" },
        )
        return result
    }

    /**
     * 对混合列表（图片+视频）按排序规则排序。
     * 由于 MediaStore 不支持跨集合排序，需要在应用层合并后再排序。
     */
    private fun sortMediaList(list: List<MediaItem>, sortOrder: MediaSortOrder): List<MediaItem> = when (sortOrder) {
        MediaSortOrder.DATE_DESC -> list.sortedByDescending { it.dateAdded }
        MediaSortOrder.DATE_ASC -> list.sortedBy { it.dateAdded }
        MediaSortOrder.NAME_ASC -> list.sortedBy { it.displayName.lowercase() }
        MediaSortOrder.NAME_DESC -> list.sortedByDescending { it.displayName.lowercase() }
        MediaSortOrder.SIZE_DESC -> list.sortedByDescending { it.size }
        MediaSortOrder.SIZE_ASC -> list.sortedBy { it.size }
    }
}
