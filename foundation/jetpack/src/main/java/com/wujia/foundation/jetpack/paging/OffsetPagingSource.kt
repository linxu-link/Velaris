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
package com.wujia.foundation.jetpack.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 基于 offset/limit 的通用分页源。
 *
 * 适合 MediaStore、REST 列表等“按偏移量分页”的数据源。
 */
abstract class OffsetPagingSource<Value : Any>(private val pageSize: Int = DEFAULT_PAGE_SIZE) :
    PagingSource<Int, Value>() {
    final override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        val pageIndex = params.key ?: 0
        val loadSize = params.loadSize.coerceAtLeast(pageSize)
        val offset = pageIndex * pageSize
        return runCatching {
            loadPage(offset = offset, limit = loadSize)
        }.fold(
            onSuccess = { items ->
                val nextKey = if (items.size < loadSize) null else pageIndex + 1
                LoadResult.Page(
                    data = items,
                    prevKey = if (pageIndex == 0) null else pageIndex - 1,
                    nextKey = nextKey,
                )
            },
            onFailure = { throwable ->
                LoadResult.Error(throwable)
            },
        )
    }

    final override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    protected abstract suspend fun loadPage(offset: Int, limit: Int): List<Value>

    protected open fun defaultPagingConfig(): PagingConfig = defaultPagingConfig(pageSize)

    companion object {
        const val DEFAULT_PAGE_SIZE = 40
    }
}

fun defaultPagingConfig(pageSize: Int = OffsetPagingSource.DEFAULT_PAGE_SIZE): PagingConfig = PagingConfig(
    pageSize = pageSize,
    initialLoadSize = pageSize * 2,
    prefetchDistance = pageSize / 2,
    enablePlaceholders = false,
)
