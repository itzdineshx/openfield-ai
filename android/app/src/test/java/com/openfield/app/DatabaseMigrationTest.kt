package com.openfield.app

import androidx.sqlite.db.SupportSQLiteDatabase
import com.openfield.app.data.local.OpenFieldDatabase
import com.openfield.app.data.local.entity.CollectionSourceCrossRefEntity
import com.openfield.app.data.local.entity.KnowledgeCollectionEntity
import com.openfield.app.data.local.entity.TaskItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class DatabaseMigrationTest {

    @Test
    fun migration_4_to_5_hasCorrectVersions() {
        assertEquals(4, OpenFieldDatabase.MIGRATION_4_5.startVersion)
        assertEquals(5, OpenFieldDatabase.MIGRATION_4_5.endVersion)
    }

    @Test
    fun migration_4_to_5_executesExpectedDdlStatements() {
        val executedSql = mutableListOf<String>()
        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        OpenFieldDatabase.MIGRATION_4_5.migrate(mockDb)

        val allSql = executedSql.joinToString("\n")
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `knowledge_collections`"))
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `collection_source_cross_ref`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_knowledge_collections_updatedAt`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_collectionId`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_sourceId`"))
    }

    @Test
    fun collectionEntity_instantiatesWithValidDefaults() {
        val collection = KnowledgeCollectionEntity(
            id = "col_test",
            name = "Test Collection",
            description = "Description of collection",
            colorHex = 0xFF1E88E5
        )
        assertEquals("col_test", collection.id)
        assertEquals("Test Collection", collection.name)
        assertTrue(collection.createdAt > 0)
        assertEquals(collection.createdAt, collection.updatedAt)
    }

    @Test
    fun collectionSourceCrossRefEntity_instantiatesWithValidTypes() {
        val ref = CollectionSourceCrossRefEntity(
            collectionId = "col_1",
            sourceId = "doc_1",
            sourceType = "DOCUMENT"
        )
        assertEquals("col_1", ref.collectionId)
        assertEquals("doc_1", ref.sourceId)
        assertEquals("DOCUMENT", ref.sourceType)
        assertTrue(ref.addedAt > 0)
    }

    @Test
    fun migration_5_to_6_hasCorrectVersions() {
        assertEquals(5, OpenFieldDatabase.MIGRATION_5_6.startVersion)
        assertEquals(6, OpenFieldDatabase.MIGRATION_5_6.endVersion)
    }

    @Test
    fun migration_5_to_6_executesExpectedDdlStatements() {
        val executedSql = mutableListOf<String>()
        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        OpenFieldDatabase.MIGRATION_5_6.migrate(mockDb)

        val allSql = executedSql.joinToString("\n")
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `task_sessions`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_task_sessions_updatedAt`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_task_sessions_status`"))
    }

    @Test
    fun migration_6_to_7_hasCorrectVersions() {
        assertEquals(6, OpenFieldDatabase.MIGRATION_6_7.startVersion)
        assertEquals(7, OpenFieldDatabase.MIGRATION_6_7.endVersion)
    }

    @Test
    fun migration_6_to_7_executesExpectedDdlStatements() {
        val executedSql = mutableListOf<String>()
        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        OpenFieldDatabase.MIGRATION_6_7.migrate(mockDb)

        val allSql = executedSql.joinToString("\n")
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `task_items`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_task_items_updatedAt`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_task_items_status`"))
        assertTrue(allSql.contains("CREATE INDEX IF NOT EXISTS `index_task_items_priority`"))
    }

    @Test
    fun taskItemEntity_instantiatesWithValidDefaults() {
        val task = TaskItemEntity(
            id = "task_1",
            title = "Study Physics",
            description = "Chapter 1 review",
            status = "PENDING",
            priority = "HIGH"
        )
        assertEquals("task_1", task.id)
        assertEquals("Study Physics", task.title)
        assertEquals("PENDING", task.status)
        assertEquals("HIGH", task.priority)
        assertTrue(task.createdAt > 0)
    }
}
