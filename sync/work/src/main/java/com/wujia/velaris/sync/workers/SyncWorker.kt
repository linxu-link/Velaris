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
package com.wujia.velaris.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.wujia.velaris.sync.DatabaseSeeder
import com.wujia.velaris.sync.initializers.SyncConstraints
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val databaseSeeder: DatabaseSeeder,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = traceAsync("Sync", 0) {
        databaseSeeder.seedIfEmpty()

        // TODO: 未来在此处添加远程数据同步逻辑
        // val syncedSuccessfully = awaitAll(
        //     async { sceneRepository.sync() },
        // ).all { it }
        // if (!syncedSuccessfully) return@traceAsync Result.retry()

        Result.success()
    }

    companion object {
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()
    }
}
