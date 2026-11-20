package com.openfield.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.dao.CollectionDao
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.dao.SavedAnswerDao
import com.openfield.app.data.local.dao.SettingsDao
import com.openfield.app.data.local.dao.TaskDao
import com.openfield.app.data.local.dao.TaskSessionDao
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.data.local.entity.ChatSessionEntity
import com.openfield.app.data.local.entity.CollectionSourceCrossRefEntity
import com.openfield.app.data.local.entity.DocumentChunkEntity
import com.openfield.app.data.local.entity.DocumentChunkFtsEntity
import com.openfield.app.data.local.entity.DocumentEntity
import com.openfield.app.data.local.entity.KnowledgeCollectionEntity
import com.openfield.app.data.local.entity.KnowledgePackArticleEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.local.entity.SavedAnswerEntity
import com.openfield.app.data.local.entity.SettingEntity
import com.openfield.app.data.local.entity.TaskItemEntity
import com.openfield.app.data.local.entity.TaskSessionEntity
import com.openfield.app.data.local.entity.TroubleshootSessionEntity
import com.openfield.app.data.local.entity.WritingDraftEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        DocumentEntity::class,
        DocumentChunkEntity::class,
        DocumentChunkFtsEntity::class,
        KnowledgePackEntity::class,
        KnowledgePackArticleEntity::class,
        SavedAnswerEntity::class,
        SettingEntity::class,
        TroubleshootSessionEntity::class,
        LearningSessionEntity::class,
        WritingDraftEntity::class,
        KnowledgeCollectionEntity::class,
        CollectionSourceCrossRefEntity::class,
        TaskSessionEntity::class,
        TaskItemEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class OpenFieldDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun documentDao(): DocumentDao
    abstract fun knowledgePackDao(): KnowledgePackDao
    abstract fun savedAnswerDao(): SavedAnswerDao
    abstract fun settingsDao(): SettingsDao
    abstract fun modeSessionDao(): ModeSessionDao
    abstract fun collectionDao(): CollectionDao
    abstract fun taskSessionDao(): TaskSessionDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: OpenFieldDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `troubleshoot_sessions` (
                        `id` TEXT NOT NULL,
                        `problem` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `currentStepNumber` INTEGER NOT NULL,
                        `isResolved` INTEGER NOT NULL,
                        `assessmentJson` TEXT NOT NULL,
                        `historyJson` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_troubleshoot_sessions_updatedAt` ON `troubleshoot_sessions` (`updatedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_sessions` (
                        `id` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `level` TEXT NOT NULL,
                        `lessonTitle` TEXT NOT NULL,
                        `lessonJson` TEXT NOT NULL,
                        `isQuizCompleted` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_learning_sessions_updatedAt` ON `learning_sessions` (`updatedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `writing_drafts` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `intent` TEXT NOT NULL,
                        `tone` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `wordCount` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_writing_drafts_updatedAt` ON `writing_drafts` (`updatedAt`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `knowledge_collections` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `colorHex` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_collections_updatedAt` ON `knowledge_collections` (`updatedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `collection_source_cross_ref` (
                        `collectionId` TEXT NOT NULL,
                        `sourceId` TEXT NOT NULL,
                        `sourceType` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`collectionId`, `sourceId`),
                        FOREIGN KEY(`collectionId`) REFERENCES `knowledge_collections`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_collectionId` ON `collection_source_cross_ref` (`collectionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_sourceId` ON `collection_source_cross_ref` (`sourceId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `task_sessions` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `sourceWorkspace` TEXT NOT NULL,
                        `targetWorkspace` TEXT NOT NULL,
                        `contextPayload` TEXT NOT NULL,
                        `tagsJson` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_sessions_updatedAt` ON `task_sessions` (`updatedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_sessions_status` ON `task_sessions` (`status`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `task_items` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `dueDate` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `subtasksJson` TEXT NOT NULL,
                        `linkedSourceIdsJson` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_items_updatedAt` ON `task_items` (`updatedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_items_status` ON `task_items` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_items_priority` ON `task_items` (`priority`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `knowledge_pack_articles` (
                        `id` TEXT NOT NULL,
                        `packId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `section` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `tagsJson` TEXT NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_pack_articles_packId` ON `knowledge_pack_articles` (`packId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_pack_articles_section` ON `knowledge_pack_articles` (`section`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `saved_answers` (
                        `id` TEXT NOT NULL,
                        `question` TEXT NOT NULL,
                        `answer` TEXT NOT NULL,
                        `sourceTitle` TEXT NOT NULL,
                        `sourceType` TEXT NOT NULL,
                        `savedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_answers_savedAt` ON `saved_answers` (`savedAt`)")
            }
        }

        fun getInstance(context: Context): OpenFieldDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpenFieldDatabase::class.java,
                    "openfield.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
