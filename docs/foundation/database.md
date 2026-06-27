# foundation:database 模块文档

## 1. 模块概述

`foundation:database` 是 Velaris 应用的基础数据持久化层模块，基于 **Room** 构建，负责管理场景（Scene）及其关联音轨（Audio Track）的本地存储。该模块采用 NIA（Now in Android）风格的分层架构，通过 Hilt 依赖注入对外提供数据库实例和 DAO 对象。

**模块路径：** `foundation/database`
**包名：** `com.wujia.foundation.database`
**数据库名：** `velaris_database`
**当前版本：** 14

---

## 2. 架构设计

### 2.1 分层结构

```
foundation/database/
├── src/main/java/com/wujia/foundation/database/
│   ├── dao/                    # 数据访问对象层
│   │   ├── SceneDao.kt         # 场景 DAO（核心业务）
│   │   └── SceneAudioDao.kt    # 音轨 DAO
│   ├── di/                     # 依赖注入模块
│   │   ├── DatabaseModule.kt   # 提供 VelarisDatabase 单例
│   │   └── DaosModule.kt       # 提供 DAO 实例
│   ├── model/                  # 实体模型层
│   │   ├── SceneEntity.kt      # 场景实体
│   │   ├── SceneAudioEntity.kt # 音轨实体
│   │   └── SceneWithAudio.kt   # 场景+音轨关系模型
│   ├── util/                   # 工具类
│   │   └── InstantConverter.kt # 时间类型转换器
│   ├── VelarisDatabase.kt      # Room 数据库定义 + 迁移脚本
│   └── DatabaseMigrations.kt   # 迁移入口（占位）
└── build.gradle.kts
```

### 2.2 设计原则

- **NIA 风格**：DAO 不直接创建，从数据库实例统一拆分出来通过 DI 提供
- **单一数据库实例**：`VelarisDatabase` 作为 `@Singleton` 由 Hilt 管理
- **内部可见性**：`VelarisDatabase`、`DatabaseModule`、`DaosModule` 均为 `internal`，仅通过 DAO 对外暴露数据访问能力
- **响应式数据流**：核心查询方法同时提供 `suspend` 一次性查询和 `Flow` 持续观察两种模式

### 2.3 依赖注入链路

```
ApplicationContext
       │
       ▼
DatabaseModule.providesVelarisDatabase()
       │  (Singleton)
       ▼
VelarisDatabase
       │
       ├──▶ DaosModule.providesSceneDao()     ──▶ SceneDao
       └──▶ DaosModule.providesSceneAudioDao() ──▶ SceneAudioDao
```

---

## 3. 核心类/接口

### 3.1 数据实体

#### 3.1.1 SceneEntity

**文件：** `model/SceneEntity.kt`（约 67 行，25 个字段）

场景实体，对应数据库 `scenes` 表，是整个模块的核心数据模型。相比早期版本，新增了视频音量、定时模式、引导完成状态、倒计时/时钟显示、闹钟提醒、时钟音频音量、倒计时钟位置等控制字段，支持更丰富的场景控制与闹钟/时钟功能。

| 字段 | 类型 | 默认值（@ColumnInfo / Kotlin） | 说明 |
|------|------|--------------------------------|------|
| `id` | `String` | - | 主键，业务生成的字符串 ID |
| `title` | `String` | - | 场景标题 |
| `description` | `String` | - | 场景描述 |
| `category` | `String` | - | 场景分类 |
| `iconName` | `String` | - | 图标名称 |
| `accentColor` | `String` | - | 强调色（HEX 字符串） |
| `sortOrder` | `Int` | - | 排序权重 |
| `videoUri` | `String?` | `null` | 视频 URI（可空） |
| `videoVolume` | `Float` | `0` / `0f` | 视频音量（独立于环境音） |
| `backgroundResName` | `String?` | `null` | 背景资源名（可空） |
| `backgroundUri` | `String?` | `null` | 背景 URI（可空，媒体库图片） |
| `brightness` | `Float` | `0.5` (SceneControlDefaults) | 亮度值 |
| `darkness` | `Float` | `0.1` | 暗度值 |
| `timerMode` | `String` | `"Countdown"` | 定时模式：Countdown / Clock |
| `timerDurationMillis` | `Long` | `2700000`（45 分钟） | 定时器时长 |
| `fadeOutEnabled` | `Boolean` | `true` | 是否启用淡出 |
| `guideCompleted` | `Boolean` | `false` | 场景引导是否已完成 |
| `showCountdownClock` | `Boolean` | `true` | 是否显示倒计时/时钟 |
| `alarmReminderEnabled` | `Boolean` | `false` (domain) / column "0" | 是否启用闹钟提醒（到期播放系统闹钟铃声） |
| `countdownClockPosition` | `String` | `"Center"` | 倒计时钟屏幕位置（Center / TopStart / BottomStart / TopEnd / BottomEnd） |
| `clockAudioVolume` | `Float` | `0.5f` | 时钟/闹钟音频音量 |
| `particleEffect` | `String` | `"None"` | 粒子效果类型（None / Rain / Snow / Fireflies） |
| `particleIntensity` | `Float` | `0.72` | 粒子强度 |
| `particleWind` | `Float` | `0.2` | 风力值 |
| `particleQuality` | `String` | `"Medium"` | 粒子渲染质量（Low / Medium / High / Ultra） |
| `particleForegroundGlassEnabled` | `Boolean` | `true` | 是否启用前景毛玻璃 |
| `createdAt` | `Instant` | - | 创建时间 |
| `updatedAt` | `Instant` | - | 更新时间 |

**索引：**
- `idx_scenes_category_sortOrder`：`(category, sortOrder)` — 分类内排序查询
- `idx_scenes_sortOrder`：`(sortOrder)` — 全局排序查询

**说明：** 控制相关字段（brightness、darkness、timer*、guide*、show*、alarm*、clock*、particle* 等）主要由 `SceneControlSettings`（domain 模型）承载，DAO 提供 `updateControlSettings` 批量更新；`videoVolume` 有独立更新方法。部分 `@ColumnInfo(defaultValue)` 使用 `SceneControlDefaults` 常量保持与领域模型一致。

#### 3.1.2 SceneAudioEntity

**文件：** `model/SceneAudioEntity.kt`（32 行，8 个字段）

音轨实体，对应数据库 `scene_audio_tracks` 表，与 `SceneEntity` 通过外键关联。

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `audioId` | `Long` | `0`（自增） | 主键，自增 ID |
| `sceneId` | `String` | - | 外键，关联 scenes.id |
| `dataId` | `String` | - | 音频数据业务 ID |
| `title` | `String` | - | 音轨标题 |
| `uri` | `String` | - | 音频 URI |
| `volume` | `Float` | `1.0` | 音量（0.0-1.0） |
| `loop` | `Boolean` | `true` | 是否循环播放 |
| `sortOrder` | `Int` | `0` | 排序权重 |

**外键约束：**
- `sceneId` → `scenes.id`，级联删除（`CASCADE`）

**索引：**
- `idx_scene_audio_tracks_sceneId`：`(sceneId)`

#### 3.1.3 SceneWithAudio

**文件：** `model/SceneWithAudio.kt`（13 行）

Room `@Relation` 关系模型，将 `SceneEntity` 与其关联的 `SceneAudioEntity` 列表组合在一起。

```kotlin
data class SceneWithAudio(
    @Embedded val scene: SceneEntity,
    @Relation(parentColumn = "id", entityColumn = "sceneId")
    val audioTracks: List<SceneAudioEntity>,
)
```

---

### 3.2 数据访问对象（DAO）

#### 3.2.1 SceneDao

**文件：** `dao/SceneDao.kt`（227 行，20+ 方法）

场景的核心数据访问接口，提供完整的 CRUD 操作及高级业务方法（当前 ~25+ 方法）。

**查询方法（Flow 响应式）：**

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `observeAll()` | `Flow<List<SceneEntity>>` | 观察所有场景列表 |
| `observeAllWithAudio()` | `Flow<List<SceneWithAudio>>` | 观察所有场景及其音轨 |
| `observeById(id)` | `Flow<SceneEntity?>` | 观察单个场景 |
| `observeByCategory(category)` | `Flow<List<SceneEntity>>` | 按分类观察场景 |

**查询方法（一次性 suspend）：**

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getAll()` | `List<SceneEntity>` | 获取所有场景 |
| `getAllWithAudio()` | `List<SceneWithAudio>` | 获取所有场景及音轨 |
| `getById(id)` | `SceneEntity?` | 获取单个场景 |
| `getCount()` | `Int` | 获取场景总数 |
| `getMaxSortOrder()` | `Int` | 获取最大排序值 |
| `existsById(id)` | `Boolean` | 判断场景是否存在 |

**写入方法：**

| 方法 | 说明 |
|------|------|
| `upsert(scene)` | 新增或更新单个场景 |
| `upsertAll(scenes)` | 批量新增或更新场景 |
| `update(scene)` | 更新场景 |
| `updateSortOrder(id, sortOrder, updatedAt)` | 更新单个场景排序 |
| `updateSortOrders(orderedIds)` | 批量更新排序（事务内） |
| `updateSortOrdersByCategory(category, orderedIds)` | 按分类批量更新排序（事务内） |
| `updateControlSettings(...)` | 更新场景控制设置（亮度、暗度、timerMode、guideCompleted、showCountdownClock、alarmReminderEnabled、countdownClockPosition、clockAudioVolume、粒子效果等全量控制字段） |
| `updateVideoVolume(id, videoVolume, updatedAt)` | 单独更新视频音量 |
| `saveSceneWithAudio(scene, audioTracks)` | 事务保存场景及音轨 |

**删除方法：**

| 方法 | 说明 |
|------|------|
| `delete(scene)` | 删除场景实体 |
| `deleteById(id)` | 按 ID 删除场景 |
| `deleteAll()` | 删除所有场景 |
| `deleteAudioBySceneId(sceneId)` | 删除指定场景的所有音轨 |

**排序逻辑：**
所有列表查询均按 `sortOrder ASC, createdAt ASC, id ASC` 三级排序，保证稳定有序。

#### 3.2.2 SceneAudioDao

**文件：** `dao/SceneAudioDao.kt`（30 行，5 个方法）

音轨的数据访问接口。

| 方法 | 类型 | 说明 |
|------|------|------|
| `upsertAll(audioTracks)` | suspend | 批量新增或更新音轨 |
| `getBySceneId(sceneId)` | suspend | 获取场景的音轨列表 |
| `observeBySceneId(sceneId)` | Flow | 观察场景的音轨列表 |
| `deleteBySceneId(sceneId)` | suspend | 删除场景的所有音轨 |
| `updateVolume(sceneId, audioId, volume)` | suspend | 更新指定音轨音量 |

---

### 3.3 数据库定义

#### 3.3.1 VelarisDatabase

**文件：** `VelarisDatabase.kt`（约 110 行，含迁移脚本）

Room 数据库定义类，注册实体、类型转换器，并内联定义了全部显式迁移脚本（当前共 11 个）。

```kotlin
@Database(
    entities = [SceneEntity::class, SceneAudioEntity::class],
    version = 14,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
internal abstract class VelarisDatabase : RoomDatabase() {
    abstract fun sceneDao(): SceneDao
    abstract fun sceneAudioDao(): SceneAudioDao
}
```

**迁移脚本（全部定义在同一文件中）：**
- `MIGRATION_3_4` ~ `MIGRATION_13_14`（共 11 个）
- 最新：MIGRATION_13_14 新增 `clockAudioVolume` 列

**DatabaseModule 注册示例（di/DatabaseModule.kt）：**
```kotlin
.addMigrations(
    MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
    MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
    MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
)
.fallbackToDestructiveMigration()
```

#### 3.3.2 DatabaseMigrations

**文件：** `DatabaseMigrations.kt`（19 行）

迁移规范的集中入口占位对象，当前为空实现。按照 NIA 的设计模式，后续每次 schema 变更应将 `AutoMigrationSpec` 放置于此。

---

### 3.4 类型转换器

#### 3.4.1 InstantConverter

**文件：** `util/InstantConverter.kt`（19 行）

Room TypeConverter，实现 `kotlinx.datetime.Instant` 与 `Long`（epoch 毫秒）之间的双向转换。

```kotlin
@TypeConverter
fun longToInstant(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

@TypeConverter
fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilliseconds()
```

设计为 `internal` 可见性，仅模块内部使用。

---

### 3.5 依赖注入模块

#### 3.5.1 DatabaseModule

**文件：** `di/DatabaseModule.kt`（40 行）

提供 `VelarisDatabase` 单例，配置迁移链和 destructive fallback。

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesVelarisDatabase(@ApplicationContext context: Context): VelarisDatabase =
        Room.databaseBuilder(context, VelarisDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .build()
}
```

常量 `DATABASE_NAME = "velaris_database"` 定义在同一文件。

#### 3.5.2 DaosModule

**文件：** `di/DaosModule.kt`（29 行）

从 `VelarisDatabase` 实例拆分提供 `SceneDao` 和 `SceneAudioDao`。

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun providesSceneDao(database: VelarisDatabase): SceneDao = database.sceneDao()

    @Provides
    fun providesSceneAudioDao(database: VelarisDatabase): SceneAudioDao = database.sceneAudioDao()
}
```

---

## 4. 数据库表结构

### 4.1 scenes 表

| 列名 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | TEXT | PRIMARY KEY | - | 业务字符串 ID |
| title | TEXT | NOT NULL | - | 标题 |
| description | TEXT | NOT NULL | - | 描述 |
| category | TEXT | NOT NULL | - | 分类 |
| iconName | TEXT | NOT NULL | - | 图标名 |
| accentColor | TEXT | NOT NULL | - | 强调色 |
| sortOrder | INTEGER | NOT NULL | - | 排序 |
| videoUri | TEXT | NULLABLE | NULL | 视频 URI |
| backgroundResName | TEXT | NULLABLE | NULL | 背景资源名 |
| backgroundUri | TEXT | NULLABLE | NULL | 背景 URI |
| brightness | REAL | NOT NULL | 0.8 | 亮度 |
| darkness | REAL | NOT NULL | 0.1 | 暗度 |
| timerDurationMillis | INTEGER | NOT NULL | 2700000 | 定时时长(ms) |
| fadeOutEnabled | INTEGER | NOT NULL | 1 | 淡出开关 |
| particleEffect | TEXT | NOT NULL | 'None' | 粒子效果 |
| particleIntensity | REAL | NOT NULL | 0.72 | 粒子强度 |
| particleWind | REAL | NOT NULL | 0.2 | 风力 |
| particleQuality | TEXT | NOT NULL | 'Medium' | 粒子渲染质量 |
| particleForegroundGlassEnabled | INTEGER | NOT NULL | 1 | 前景毛玻璃开关 |
| createdAt | INTEGER | NOT NULL | - | 创建时间(epoch ms) |
| updatedAt | INTEGER | NOT NULL | - | 更新时间(epoch ms) |

**索引：**
- `index_scenes_category_sortOrder`：`(category, sortOrder)`
- `index_scenes_sortOrder`：`(sortOrder)`

### 4.2 scene_audio_tracks 表

| 列名 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| audioId | INTEGER | PRIMARY KEY AUTOINCREMENT | 0 | 自增主键 |
| sceneId | TEXT | NOT NULL, FK → scenes.id | - | 所属场景 ID |
| dataId | TEXT | NOT NULL | - | 音频业务 ID |
| title | TEXT | NOT NULL | - | 音轨标题 |
| uri | TEXT | NOT NULL | - | 音频 URI |
| volume | REAL | NOT NULL | 1.0 | 音量 |
| loop | INTEGER | NOT NULL | - | 是否循环 |
| sortOrder | INTEGER | NOT NULL | 0 | 排序 |

**外键：**
- `sceneId` → `scenes.id`，`ON DELETE CASCADE`

**索引：**
- `index_scene_audio_tracks_sceneId`：`(sceneId)`

### 4.3 ER 关系

```
┌─────────────────────────┐
│        scenes           │
│─────────────────────────│
│ PK  id: String          │
│     title               │
│     category            │
│     sortOrder           │
│     ... (21 fields)     │
└──────────┬──────────────┘
           │ 1:N (CASCADE DELETE)
           ▼
┌─────────────────────────┐
│   scene_audio_tracks    │
│─────────────────────────│
│ PK  audioId: Long (AI)  │
│ FK  sceneId → scenes.id │
│     dataId              │
│     title               │
│     uri                 │
│     volume              │
│     loop                │
│     sortOrder           │
└─────────────────────────┘
```

---

## 5. 迁移策略

### 5.1 迁移机制

当前采用 **显式迁移 + destructive fallback** 的混合策略：

1. **显式迁移**（`addMigrations`）：当前覆盖从 v3 至 v14 的 11 个版本间迁移（所有脚本内联定义于 VelarisDatabase.kt）
2. **Destructive Fallback**（`fallbackToDestructiveMigration`）：当遇到无法处理的迁移路径时，清空数据库重建

**注意**：所有 MIGRATION_* 对象目前仍定义在 `VelarisDatabase.kt` 中（随版本增长文件已膨胀），`DatabaseMigrations.kt` 仅保留占位对象。

### 5.2 迁移脚本详情

| 迁移 | 版本跨度 | 操作 |
|------|----------|------|
| `MIGRATION_3_4` | v3 → v4 | 新增 `backgroundUri` 列；将 `backgroundResName` 中的 `content://` URI 迁移至新列 |
| `MIGRATION_4_5` | v4 → v5 | 将 `darkness` 从 0.4 默认值修正为 0.1 |
| `MIGRATION_5_6` | v5 → v6 | 新增粒子效果 5 列（原 weather 相关）：`particleEffect`、`particleIntensity`、`particleWind`、`particleQuality`、`particleForegroundGlassEnabled` |
| `MIGRATION_6_7` | v6 → v7 | 移除已废弃的 `ambience` 列 |
| `MIGRATION_7_8` | v7 → v8 | 新增 `guideCompleted` 列（场景引导状态） |
| `MIGRATION_8_9` | v8 → v9 | 新增 `showCountdownClock` 列 |
| `MIGRATION_9_10` | v9 → v10 | 新增 `countdownClockPosition` 列（时钟屏幕位置） |
| `MIGRATION_10_11` | v10 → v11 | 新增 `alarmReminderEnabled` 列（闹钟提醒开关） |
| `MIGRATION_11_12` | v11 → v12 | 新增 `videoVolume` 列（视频独立音量） |
| `MIGRATION_12_13` | v12 → v13 | 新增 `timerMode` 列（Countdown / Clock） |
| `MIGRATION_13_14` | v13 → v14 | 新增 `clockAudioVolume` 列（时钟/闹钟音频音量，默认 0.5） |

### 5.3 Schema 导出

数据库配置 `exportSchema = true`，Schema JSON 文件存储于 `foundation/database/schemas/com.wujia.foundation.database.VelarisDatabase/`。当前主要保留近期版本（11/12/13/14.json），早期版本快照可能随清理而减少。Room 会在编译时验证迁移链。

### 5.4 DatabaseMigrations 占位

`DatabaseMigrations` 对象目前仅为占位（内部 `Placeholder` 类），按照 NIA 模式设计，预期后续 schema 变更时将 `AutoMigrationSpec` 集中放置于此。当前仍依赖手动显式 Migration。

---

## 6. 对外暴露的接口

模块通过 Hilt DI 对外暴露以下公共 API（非 `internal`）：

### 6.1 DAO 接口

- **`SceneDao`**：场景数据访问，提供 20+ 方法（详见 3.2.1）
- **`SceneAudioDao`**：音轨数据访问，提供 5 方法（详见 3.2.2）

### 6.2 数据模型

- **`SceneEntity`**：场景实体
- **`SceneAudioEntity`**：音轨实体
- **`SceneWithAudio`**：场景+音轨关系模型

### 6.3 内部实现（不对外暴露）

以下类均为 `internal`，外部模块不可直接访问：

- `VelarisDatabase`：数据库定义
- `DatabaseModule`：数据库 DI 模块
- `DaosModule`：DAO DI 模块
- `InstantConverter`：类型转换器
- `DatabaseMigrations`：迁移管理
- 所有迁移脚本（`MIGRATION_3_4` 等）

### 6.4 典型使用方式

其他模块通过 Hilt 注入 DAO 即可访问数据库：

```kotlin
@HiltViewModel
class SomeViewModel @Inject constructor(
    private val sceneDao: SceneDao,
    private val sceneAudioDao: SceneAudioDao,
) : ViewModel() {

    val scenes: Flow<List<SceneEntity>> = sceneDao.observeAll()

    fun getSceneAudio(sceneId: String): Flow<List<SceneAudioEntity>> =
        sceneAudioDao.observeBySceneId(sceneId)
}
```

---

## 7. 依赖关系

### 7.1 模块依赖

`build.gradle.kts` 配置：

```kotlin
plugins {
    alias(libs.plugins.advance.android.library)
    alias(libs.plugins.advance.android.library.jacoco)
    alias(libs.plugins.advance.android.room)
    alias(libs.plugins.advance.hilt)
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
```

### 7.2 外部依赖

| 依赖 | 用途 |
|------|------|
| **Room**（通过 advance 插件） | 数据库 ORM，包含 runtime、compiler、ktx |
| **Hilt**（通过 advance 插件） | 依赖注入 |
| **kotlinx-datetime** | `Instant` 时间类型 |
| **AndroidX**（隐式） | `Context`、`RoomDatabase` 等基础组件 |
| **Kotlin Coroutines**（隐式） | `suspend`、`Flow` 支持 |

### 7.3 被依赖关系

该模块为 `foundation` 层基础模块，预期被以下层依赖：
- `data` 层（Repository 实现）
- `domain` 层（如需直接访问 DAO）
- 其他 `foundation` 或 `feature` 模块（通过 DI 注入 DAO）

---

## 8. 当前缺陷与改进点

### 8.1 架构层面

1. **`fallbackToDestructiveMigration` 安全隐患**：当前配置了 destructive fallback，若显式迁移脚本遗漏，用户数据将被静默清除。建议：
   - 生产环境移除 `fallbackToDestructiveMigration()`
   - 或在 fallback 时加入数据备份逻辑

2. **迁移脚本位置不规范**：11 个迁移脚本直接定义在 `VelarisDatabase.kt` 中（文件已增长至 ~110 行），随后续版本增长会继续膨胀。建议将迁移集中迁移至独立的 `DatabaseMigrations.kt`（或按版本分文件），并考虑启用 AutoMigrationSpec。

3. **DatabaseMigrations 占位未启用**：`DatabaseMigrations` 对象内的 `Placeholder` 类无实际作用，AutoMigration 机制未实际启用。所有迁移仍为手动显式对象。

### 8.2 数据模型层面

4. **alarmReminderEnabled 默认值在 entity 与 domain 模型间存在差异**：Entity `@ColumnInfo(defaultValue = "0")` + kotlin 默认 `true`，而 `SceneControlSettings` 默认 `false`。上层 seed/创建逻辑需保持一致，避免首次持久化时行为不一致。

5. **时间字段未自动维护**：`createdAt` 和 `updatedAt` 由上层赋值，数据库层未配置自动填充机制（如 `@ColumnInfo(defaultValue)` 或 Room 的 `onConflict` 策略），容易出现遗忘更新 `updatedAt` 的情况。

6. **`accentColor` 使用字符串存储**：颜色值以 String 存储而非 Integer，不便于数据库层做颜色比较或排序，但考虑到业务场景的简单性，当前方案可接受。

7. **新控制字段（timerMode、clock*、alarm*、guide* 等）演进历史**：这些字段通过连续显式迁移添加，旧安装用户依赖迁移链完整性。建议在重大版本时考虑提供数据校验或 repair 逻辑。

### 8.3 DAO 层面

8. **`SceneDao` 过于庞大**：~254 行、25+ 方法承担了过多职责，包括场景 CRUD、排序管理、音轨操作（`saveSceneWithAudio`、`deleteAudioBySceneId`、`upsertAudioAll`）以及不断膨胀的 `updateControlSettings`（现在包含 15+ 参数）。音轨相关操作应完全委托给 `SceneAudioDao`；可考虑将控制设置更新拆分为更细粒度方法或使用部分更新策略。

9. **缺少批量删除方法**：`SceneDao` 没有 `deleteByIds(ids: List<String>)` 方法，批量删除需循环调用。

10. **`observeByCategory` 使用场景不明**：该方法在 `SceneDao` 中存在，但没有对应的分类内排序查询索引优化支持的证据。

### 8.4 测试层面

11. **缺少数据库测试**：当前模块无 `androidTest` 目录，缺少 DAO 单元测试和迁移测试。建议补充：
   - DAO 方法的 Room 测试（使用 `Room.inMemoryDatabaseBuilder`）
   - 迁移脚本的正确性验证（使用 `MigrationTestHelper`）

### 8.5 其他

12. **Schema JSON 历史管理**：`schemas/` 目录当前仅保留近期版本（v11+），早期版本快照可能已丢失或未重新导出。迁移测试和 Room 编译验证依赖 schema 历史，需确保 CI 中 schema 变更时正确提交新增 JSON。

13. **与 foundation:alarm 模块的配合**：`alarmReminderEnabled` 触发时由 `foundation:alarm.VelarisAlarmController` 负责系统铃声 + AudioFocus 管理，数据库仅存开关状态。文档与集成点需保持同步。

---

## 9. 代码统计

| 分类 | 文件数 | 代码行数（约） |
|------|--------|----------------|
| 实体模型（model） | 3 | 110 |
| 数据访问对象（dao） | 2 | 285 |
| 数据库定义 | 1 | 110 |
| 迁移管理 | 1 | 20 |
| 类型转换器（util） | 1 | 19 |
| 依赖注入（di） | 2 | 90 |
| 构建脚本 | 1 | 14 |
| **合计** | **11** | **约 650** |

### 9.1 各文件详情

| 文件 | 行数（约） | 职责 |
|------|------------|------|
| `SceneDao.kt` | 254 | 场景 DAO，25+ 方法（含扩展的 updateControlSettings + videoVolume） |
| `VelarisDatabase.kt` | 110 | 数据库定义 + 11 个迁移脚本 |
| `SceneEntity.kt` | 67 | 场景实体，25 字段（含 clock/alarm/timer/videoVolume/guide 等） |
| `DatabaseModule.kt` | 60 | 数据库 DI 模块（注册全部 11 个 Migration） |
| `SceneAudioEntity.kt` | 33 | 音轨实体，8 字段 |
| `SceneAudioDao.kt` | 31 | 音轨 DAO，5 方法 |
| `DaosModule.kt` | 30 | DAO DI 模块 |
| `DatabaseMigrations.kt` | 20 | 迁移入口占位（Placeholder） |
| `InstantConverter.kt` | 19 | 时间类型转换器 |
| `SceneWithAudio.kt` | 14 | 关系模型 |
| `build.gradle.kts` | 14 | 构建配置 |

### 9.2 数据库规模

| 指标 | 数量 |
|------|------|
| 实体（Entity） | 2 |
| DAO 接口 | 2 |
| 数据库版本 | 14 |
| 显式迁移脚本 | 11 |
| 索引 | 3 |
| 外键 | 1 |
| TypeConverter | 1 |
| Schema JSON 文件（近期） | 4（v11–v14，早期可能已清理） |

---

## 附录：构建插件说明

| 插件 | 用途 |
|------|------|
| `advance.android.library` | Android Library 基础配置 |
| `advance.android.library.jacoco` | 代码覆盖率支持 |
| `advance.android.room` | Room 编译器与 schema 导出配置 |
| `advance.hilt` | Hilt 依赖注入配置 |
