package com.openfield.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.dao.SettingsDao
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.data.local.entity.ChatSessionEntity
import com.openfield.app.data.local.entity.DocumentChunkEntity
import com.openfield.app.data.local.entity.DocumentChunkFtsEntity
import com.openfield.app.data.local.entity.DocumentEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.local.entity.SettingEntity
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
        SettingEntity::class,
        TroubleshootSessionEntity::class,
        LearningSessionEntity::class,
        WritingDraftEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class OpenFieldDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun documentDao(): DocumentDao
    abstract fun knowledgePackDao(): KnowledgePackDao
    abstract fun settingsDao(): SettingsDao
    abstract fun modeSessionDao(): ModeSessionDao

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

        fun getInstance(context: Context): OpenFieldDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpenFieldDatabase::class.java,
                    "openfield.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
