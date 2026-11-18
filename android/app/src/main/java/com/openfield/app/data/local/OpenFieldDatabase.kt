package com.openfield.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.dao.SettingsDao
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.data.local.entity.ChatSessionEntity
import com.openfield.app.data.local.entity.DocumentChunkEntity
import com.openfield.app.data.local.entity.DocumentChunkFtsEntity
import com.openfield.app.data.local.entity.DocumentEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.local.entity.SettingEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        DocumentEntity::class,
        DocumentChunkEntity::class,
        DocumentChunkFtsEntity::class,
        KnowledgePackEntity::class,
        SettingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class OpenFieldDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun documentDao(): DocumentDao
    abstract fun knowledgePackDao(): KnowledgePackDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: OpenFieldDatabase? = null

        fun getInstance(context: Context): OpenFieldDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpenFieldDatabase::class.java,
                    "openfield.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
