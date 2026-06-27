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

/**
 * Room 自动迁移的占位集中入口。
 *
 * 当前使用 destructive fallback，后续需要保留用户数据时再补充实际迁移 spec。
 * 后续每次 schema 变更都应把相关的 AutoMigrationSpec 放到这里，
 * 形成和 Now in Android 一致的单一迁移入口。
 */
internal object DatabaseMigrations {

    /**
     * 保留给未来的 schema 迁移扩展点。
     *
     * 示例：
     * `Schema2to3`、`Schema10to11`
     */
    internal class Placeholder
}
