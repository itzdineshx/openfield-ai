# OpenField Phase 3 — Database Design Specification

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Status:** Approved for Implementation  
**Target Milestone:** OpenField v0.3.0 (Database Schema Version 5)  
**Database Engine:** Room 2.7.2 over SQLite (Android 14 API 34)  
**FTS Engine:** SQLite FTS4 Virtual Tables

---

## 1. Database Architecture Overview

OpenField relies strictly on a local Room SQLite database (`openfield.db`) located in the application's private data sandbox (`/data/user/0/com.openfield.app/databases/openfield.db`).

### Guiding Principles:
1. **Zero Cloud Sync:** No external synchronization triggers or silent cloud replication.
2. **Schema Invariant & Non-Destructive Migrations:** User data must never be wiped or corrupted across version upgrades. `.fallbackToDestructiveMigration(dropAllTables = false)` is enforced.
3. **Normalized Metadata with Content Indirection:** Large file payloads (PDFs, images, pack archives) live on flash storage; Room stores structured metadata, extracted text chunks, and FTS4 inverted indices.
4. **Relational Integrity:** Foreign keys with `CASCADE` deletion ensure that deleting a collection, document, or session cleans up associated child rows, chunks, and virtual FTS records atomically.

---

## 2. Entity Inventory & Schema Definitions (Version 5)

The Room database contains 12 entities (10 retained from Version 4 + 2 new for Version 5).

### 2.1 Retained Entities (from Version 4)

#### 1. `chat_sessions` (`ChatSessionEntity`)
- `id: TEXT` (PK)
- `title: TEXT`
- `preview: TEXT`
- `lastUpdated: INTEGER` (Indexed)
- `messageCount: INTEGER`
- `isPinned: INTEGER` (Boolean)

#### 2. `chat_messages` (`ChatMessageEntity`)
- `id: TEXT` (PK)
- `sessionId: TEXT` (Indexed)
- `isUser: INTEGER` (Boolean)
- `text: TEXT`
- `timestamp: INTEGER` (Indexed)
- `stepsJson: TEXT?`
- `imageUri: TEXT?`
- `sourcePage: TEXT?`
- `isComplete: INTEGER` (Boolean)

#### 3. `documents` (`DocumentEntity`)
- `id: TEXT` (PK)
- `name: TEXT`
- `uriString: TEXT`
- `sizeBytes: INTEGER`
- `formattedSize: TEXT`
- `pageCount: INTEGER`
- `summary: TEXT`
- `isScanned: INTEGER` (Boolean)
- `createdAt: INTEGER`

#### 4. `document_chunks` (`DocumentChunkEntity`)
- `rowid: INTEGER` (Auto-increment PK)
- `documentId: TEXT` (Indexed)
- `pageNumber: INTEGER` (Indexed)
- `chunkIndex: INTEGER`
- `text: TEXT`

#### 5. `document_chunks_fts` (`DocumentChunkFtsEntity`)
- FTS4 virtual table configured with `@Fts4(contentEntity = DocumentChunkEntity::class)`.
- Full-text index over chunk `text`.

#### 6. `knowledge_packs` (`KnowledgePackEntity`)
- `id: TEXT` (PK)
- `title: TEXT`
- `description: TEXT`
- `size: TEXT`
- `isInstalled: INTEGER` (Boolean)
- `category: TEXT`
- `iconColorHex: INTEGER` (Long)
- `topicsJson: TEXT`

#### 7. `app_settings` (`SettingEntity`)
- `key: TEXT` (PK)
- `value: TEXT`

#### 8. `troubleshoot_sessions` (`TroubleshootSessionEntity`)
- `id: TEXT` (PK)
- `problem: TEXT`
- `category: TEXT`
- `currentStepNumber: INTEGER`
- `isResolved: INTEGER` (Boolean)
- `assessmentJson: TEXT`
- `historyJson: TEXT`
- `updatedAt: INTEGER` (Indexed)

#### 9. `learning_sessions` (`LearningSessionEntity`)
- `id: TEXT` (PK)
- `topic: TEXT`
- `level: TEXT`
- `lessonTitle: TEXT`
- `lessonJson: TEXT`
- `isQuizCompleted: INTEGER` (Boolean)
- `updatedAt: INTEGER` (Indexed)

#### 10. `writing_drafts` (`WritingDraftEntity`)
- `id: TEXT` (PK)
- `title: TEXT`
- `intent: TEXT`
- `tone: TEXT`
- `content: TEXT`
- `wordCount: INTEGER`
- `updatedAt: INTEGER` (Indexed)

---

### 2.2 New Entities for Version 5 (Collections Subsystem)

#### 11. `knowledge_collections` (`KnowledgeCollectionEntity`)
Stores user-defined thematic collections of sources.

```kotlin
@Entity(
    tableName = "knowledge_collections",
    indices = [Index(value = ["updatedAt"])]
)
data class KnowledgeCollectionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 12. `collection_source_cross_ref` (`CollectionSourceCrossRefEntity`)
Many-to-many relationship mapping knowledge sources to collections.

```kotlin
@Entity(
    tableName = "collection_source_cross_ref",
    primaryKeys = ["collectionId", "sourceId"],
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["sourceId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeCollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CollectionSourceCrossRefEntity(
    val collectionId: String,
    val sourceId: String,
    val sourceType: String,  // DOCUMENT, NOTE, CONVERSATION, KNOWLEDGE_PACK
    val addedAt: Long = System.currentTimeMillis()
)
```

---

## 3. Data Access Objects (DAOs)

### 3.1 `CollectionDao` (New Interface)
```kotlin
@Dao
interface CollectionDao {

    @Query("SELECT * FROM knowledge_collections ORDER BY updatedAt DESC")
    fun getCollectionsFlow(): Flow<List<KnowledgeCollectionEntity>>

    @Query("SELECT * FROM knowledge_collections ORDER BY updatedAt DESC")
    suspend fun getCollections(): List<KnowledgeCollectionEntity>

    @Query("SELECT * FROM knowledge_collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionById(id: String): KnowledgeCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: KnowledgeCollectionEntity)

    @Update
    suspend fun updateCollection(collection: KnowledgeCollectionEntity)

    @Query("DELETE FROM knowledge_collections WHERE id = :id")
    suspend fun deleteCollection(id: String)

    // Cross-reference queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSourceToCollection(ref: CollectionSourceCrossRefEntity)

    @Query("DELETE FROM collection_source_cross_ref WHERE collectionId = :collectionId AND sourceId = :sourceId")
    suspend fun removeSourceFromCollection(collectionId: String, sourceId: String)

    @Query("SELECT sourceId FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    suspend fun getSourceIdsForCollection(collectionId: String): List<String>

    @Query("SELECT collectionId FROM collection_source_cross_ref WHERE sourceId = :sourceId")
    suspend fun getCollectionIdsForSource(sourceId: String): List<String>

    @Query("SELECT COUNT(*) FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    fun getSourceCountForCollectionFlow(collectionId: String): Flow<Int>

    @Query("DELETE FROM collection_source_cross_ref WHERE sourceId = :sourceId")
    suspend fun removeSourceFromAllCollections(sourceId: String)
}
```

### 3.2 Enhanced Search & Retrieval Queries in `DocumentDao`
```kotlin
@Query("""
    SELECT c.* FROM document_chunks c 
    JOIN document_chunks_fts f ON c.rowid = f.rowid 
    WHERE document_chunks_fts MATCH :query AND c.documentId IN (:documentIds) 
    ORDER BY c.pageNumber ASC
    LIMIT :limit
""")
suspend fun searchChunksInSourceScope(documentIds: List<String>, query: String, limit: Int = 8): List<DocumentChunkEntity>
```

---

## 4. Migration DDL & Implementation (`MIGRATION_4_5`)

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create knowledge_collections table
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

        // 2. Create collection_source_cross_ref table
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
```

---

## 5. Migration Safety, Rollback & Validation

### Verification Checklist:
1. **Zero Data Loss:** `documents`, `document_chunks`, `chat_sessions`, `chat_messages`, `knowledge_packs`, `app_settings`, `troubleshoot_sessions`, `learning_sessions`, and `writing_drafts` tables are un-altered.
2. **Cascade Deletion:** When a collection is deleted from `knowledge_collections`, its entries in `collection_source_cross_ref` are automatically purged via SQLite foreign key cascading. The original documents or notes remain intact.
3. **Source Deletion Propagation:** When a document is deleted via `DocumentDao.deleteDocument(id)`, the repository triggers `collectionDao.removeSourceFromAllCollections(id)` to prevent orphaned cross-reference entries.
4. **Automated Migration Test:** `DatabaseMigrationTest.kt` verifies that a Version 4 database populated with mock documents and chat messages successfully upgrades to Version 5 with all data preserved and functional collection queries.
