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
package com.wujia.foundation.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wujia.foundation.database.dao.SceneAudioDao
import com.wujia.foundation.database.dao.SceneDao
import com.wujia.foundation.database.model.SceneAudioEntity
import com.wujia.foundation.database.model.SceneEntity
import com.wujia.foundation.database.util.InstantConverter

/**
 * 应用数据库。
 */
@Database(
    entities = [SceneEntity::class, SceneAudioEntity::class],
    version = 14,
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
)
internal abstract class VelarisDatabase : RoomDatabase() {

    abstract fun sceneDao(): SceneDao

    abstract fun sceneAudioDao(): SceneAudioDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN backgroundUri TEXT DEFAULT NULL")
        db.execSQL(
            "UPDATE scenes SET backgroundUri = backgroundResName " +
                "WHERE backgroundResName LIKE 'content://%'",
        )
        db.execSQL(
            "UPDATE scenes SET backgroundResName = NULL " +
                "WHERE backgroundResName LIKE 'content://%'",
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE scenes SET darkness = 0.1 WHERE darkness = 0.4")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN weatherEffect TEXT NOT NULL DEFAULT 'None'")
        db.execSQL("ALTER TABLE scenes ADD COLUMN weatherIntensity REAL NOT NULL DEFAULT 0.72")
        db.execSQL("ALTER TABLE scenes ADD COLUMN weatherWind REAL NOT NULL DEFAULT 0.2")
        db.execSQL("ALTER TABLE scenes ADD COLUMN weatherQuality TEXT NOT NULL DEFAULT 'Medium'")
        db.execSQL("ALTER TABLE scenes ADD COLUMN weatherForegroundGlassEnabled INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes DROP COLUMN ambience")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN guideCompleted INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN showCountdownClock INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN countdownClockPosition TEXT NOT NULL DEFAULT 'Center'")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN alarmReminderEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN videoVolume REAL NOT NULL DEFAULT 0")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN timerMode TEXT NOT NULL DEFAULT 'Countdown'")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scenes ADD COLUMN clockAudioVolume REAL NOT NULL DEFAULT 0.5")
    }
}
