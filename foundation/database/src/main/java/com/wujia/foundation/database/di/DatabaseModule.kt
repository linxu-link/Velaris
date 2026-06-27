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
package com.wujia.foundation.database.di

import android.content.Context
import androidx.room.Room
import com.wujia.foundation.database.MIGRATION_10_11
import com.wujia.foundation.database.MIGRATION_11_12
import com.wujia.foundation.database.MIGRATION_12_13
import com.wujia.foundation.database.MIGRATION_13_14
import com.wujia.foundation.database.MIGRATION_3_4
import com.wujia.foundation.database.MIGRATION_4_5
import com.wujia.foundation.database.MIGRATION_5_6
import com.wujia.foundation.database.MIGRATION_6_7
import com.wujia.foundation.database.MIGRATION_7_8
import com.wujia.foundation.database.MIGRATION_8_9
import com.wujia.foundation.database.MIGRATION_9_10
import com.wujia.foundation.database.VelarisDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 提供数据库实例的 Hilt 模块。
 *
 * 这是 Now in Android 风格的标准入口：先有数据库，再从数据库拆 DAO。
 */

const val DATABASE_NAME = "velaris_database"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesVelarisDatabase(@ApplicationContext context: Context): VelarisDatabase = Room.databaseBuilder(
        context,
        VelarisDatabase::class.java,
        DATABASE_NAME,
    ).addMigrations(
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
    )
        .fallbackToDestructiveMigration()
        .build()
}
