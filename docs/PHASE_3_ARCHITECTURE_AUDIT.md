# OpenField Phase 3 — Architecture & Baseline Audit

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Auditor:** Principal Android Architect & AI Systems Engineer  
**Baseline Release:** OpenField v0.2.1 (Phase 2.1 Release, Tag: `v0.2.1`)  
**Target Milestone:** Phase 3 (v0.3.0 Unified Personal Knowledge → v1.0.0 Stable OpenField)  
**Target Hardware:** Lava LXX503 (Android 14, ARM64, 6 GB RAM, Unisoc T616)

---

## 1. Executive Baseline & Audit Summary

OpenField v0.2.1 is currently deployed, passing 100% of automated tests (110 tests across 17 suites), and verified on physical hardware. The application provides local generative AI capabilities powered strictly by Google LiteRT-LM (0.19.1) running Gemma 4 E2B IT (`gemma-4-E2B-it.litertlm`, 2.58 GB weights) on-device via CPU.

### Core Baseline Findings:
1. **Single Shared Model Architecture:** A single process-scoped `AiOrchestrator` manages `LiteRtLmEngine` (implementing `LocalAiEngine`). All features share this single instance, preventing duplicate model loads and Out-Of-Memory (OOM) termination on 6 GB RAM devices.
2. **Dedicated Workspace Decoupling:** Chat, Image Analysis, Document Q&A, Guided Problem Solving, Learn & Explain, Create & Write, AI Tools, and My Library have independent UI components, interaction state machines, specialized prompts, and validated output schemas.
3. **Database Version:** Room SQLite is currently at **Version 4**, persisting chat sessions, messages, documents, chunks, FTS4 indexes, knowledge packs, settings, troubleshooting sessions, learning lessons, and writing drafts.
4. **Current Knowledge Silos:** In v0.2.1, information exists in isolated silos:
   - Imported documents and scanned PDFs are indexed in `document_chunks_fts`.
   - Conversations are stored in `chat_sessions` and `chat_messages` (queried via SQL `LIKE`, not FTS).
   - Writing drafts are isolated in `writing_drafts`.
   - Knowledge packs reside in `knowledge_packs` (queried in-memory).
   - Learning lessons reside in `learning_sessions`.
5. **The Phase 3 Mandate:** Phase 3 transitions OpenField from a set of isolated tools into a **Unified Personal Intelligence Platform**, allowing users to search, organize into collections, ask cross-source questions with grounded citations, maintain continuous task sessions, and execute deterministic workflows offline.

---

## 2. Existing Package & Module Structure

The Android project is situated in `android/` with application ID `com.openfield.app`:

```
android/app/src/main/java/com/openfield/app/
├── core/
│   ├── components/       # Common Material 3 Compose widgets (Cards, Buttons, TopBars, Markdown, Dialogs)
│   ├── model/            # Shared presentation models (ChatMessage, DocumentItem, KnowledgePack, etc.)
│   ├── theme/            # Color palettes, typography, shapes (OpenField Blue, Green, Navy, CardBorder)
│   └── util/             # PerformanceTracker, TextCleaner, Permission helpers
├── data/
│   ├── local/
│   │   ├── dao/          # ChatDao, DocumentDao, ModeSessionDao, MiscDaos (KnowledgePackDao, SettingsDao)
│   │   ├── entity/       # ChatSessionEntity, ChatMessageEntity, DocumentEntity, DocumentChunkEntity,
│   │   │                 # DocumentChunkFtsEntity, KnowledgePackEntity, SettingEntity,
│   │   │                 # TroubleshootSessionEntity, LearningSessionEntity, WritingDraftEntity
│   │   └── OpenFieldDatabase.kt # Room database instance (v4) with Migration(3, 4)
│   ├── remote/           # CatalogApiClient (REST client for optional FastAPI pack catalog)
│   └── repository/       # OpenFieldRepository (monolithic facade wiring all controllers & use cases)
├── feature/
│   ├── ai/
│   │   ├── controller/   # ChatController, ImageAnalysisController, DocumentQaController,
│   │   │                 # TroubleshootingController, LearningController, WritingController
│   │   ├── mode/         # AiOrchestrator, PromptBuilder, ModeConfiguration, StructuredOutputParser
│   │   └── model/        # LocalAiEngine, LiteRtLmEngine, ModelRepository, ModelStorageManager
│   ├── chat/             # ChatScreen, MessageBubble, ChatTopBar
│   ├── create/           # CreateWriteScreen, IntentChips, EditorCanvas
│   ├── document/         # DocumentQAScreen, DocumentDetailScreen, PdfRenderer, LocalOcrEngine
│   │   ├── ocr/          # LocalOcrEngine (MLKitOcrEngine using bundled text-recognition)
│   │   └── rag/          # PdfTextExtractor, DocumentChunker, DocumentRetriever, RetrievalBenchmark
│   ├── home/             # HomeScreen, QuickActionGrid, ModelReadinessBanner
│   ├── image/            # ImageAnalysisScreen, CameraPicker, VisionTensorPreprocessor
│   ├── learn/            # LearnScreen, LevelSelector, QuizCard, BatteryConceptDiagram
│   ├── library/          # LibraryScreen, KnowledgePackDetailScreen, KnowledgePackManager
│   ├── problem/          # ProblemSolverScreen, DiagnosticStepCard, HazardAlert
│   ├── settings/         # SettingsScreen, AboutScreen, ProfileMenuSheet
│   ├── splash/           # SplashScreen, OnboardingScreen
│   ├── tools/            # ToolsScreen, ToolDetailScreen
│   └── voice/            # VoiceInputManager, SpeechToTextEngine
└── navigation/           # AppNavigation, AppRoutes, BottomNavigationBar
```

---

## 3. Database Schema & Migration Audit

### 3.1 Room Database Configuration
- **Database Class:** `com.openfield.app.data.local.OpenFieldDatabase`
- **Current Version:** `4`
- **File Name:** `openfield.db`
- **Entities Registered (10):**
  1. `ChatSessionEntity` (`chat_sessions`)
  2. `ChatMessageEntity` (`chat_messages`)
  3. `DocumentEntity` (`documents`)
  4. `DocumentChunkEntity` (`document_chunks`)
  5. `DocumentChunkFtsEntity` (`document_chunks_fts` - SQLite FTS4 virtual table)
  6. `KnowledgePackEntity` (`knowledge_packs`)
  7. `SettingEntity` (`app_settings`)
  8. `TroubleshootSessionEntity` (`troubleshoot_sessions`)
  9. `LearningSessionEntity` (`learning_sessions`)
  10. `WritingDraftEntity` (`writing_drafts`)

### 3.2 Migration History
- **Version 1 → 2:** Initial table schemas for documents and chats.
- **Version 2 → 3:** Added `document_chunks_fts` virtual table.
- **Version 3 → 4:** Added `troubleshoot_sessions`, `learning_sessions`, and `writing_drafts` with index on `updatedAt`.
- **Destructive Fallback:** Configured with `.fallbackToDestructiveMigration(dropAllTables = false)` to prevent accidental wipe of user data.

### 3.3 Detailed Entity Schema Table

| Table Name | Primary Key | Key Columns & Types | Indices |
|---|---|---|---|
| `chat_sessions` | `id: TEXT` | `title: TEXT`, `preview: TEXT`, `lastUpdated: INTEGER`, `messageCount: INTEGER`, `isPinned: INTEGER` | `index_chat_sessions_lastUpdated` |
| `chat_messages` | `id: TEXT` | `sessionId: TEXT`, `isUser: INTEGER`, `text: TEXT`, `timestamp: INTEGER`, `stepsJson: TEXT?`, `imageUri: TEXT?`, `sourcePage: TEXT?`, `isComplete: INTEGER` | `index_chat_messages_sessionId`, `index_chat_messages_timestamp` |
| `documents` | `id: TEXT` | `name: TEXT`, `uriString: TEXT`, `sizeBytes: INTEGER`, `formattedSize: TEXT`, `pageCount: INTEGER`, `summary: TEXT`, `isScanned: INTEGER`, `createdAt: INTEGER` | None (PK indexed) |
| `document_chunks` | `rowid: INTEGER` (auto) | `documentId: TEXT`, `pageNumber: INTEGER`, `chunkIndex: INTEGER`, `text: TEXT` | `index_document_chunks_documentId`, `index_document_chunks_pageNumber` |
| `document_chunks_fts` | `rowid: INTEGER` (contentEntity) | `text: TEXT` (Virtual FTS4 table over `document_chunks`) | SQLite FTS4 inverted index |
| `knowledge_packs` | `id: TEXT` | `title: TEXT`, `description: TEXT`, `size: TEXT`, `isInstalled: INTEGER`, `category: TEXT`, `iconColorHex: INTEGER`, `topicsJson: TEXT` | None (PK indexed) |
| `app_settings` | `key: TEXT` | `value: TEXT` | None (PK indexed) |
| `troubleshoot_sessions`| `id: TEXT` | `problem: TEXT`, `category: TEXT`, `currentStepNumber: INTEGER`, `isResolved: INTEGER`, `assessmentJson: TEXT`, `historyJson: TEXT`, `updatedAt: INTEGER` | `index_troubleshoot_sessions_updatedAt` |
| `learning_sessions` | `id: TEXT` | `topic: TEXT`, `level: TEXT`, `lessonTitle: TEXT`, `lessonJson: TEXT`, `isQuizCompleted: INTEGER`, `updatedAt: INTEGER` | `index_learning_sessions_updatedAt` |
| `writing_drafts` | `id: TEXT` | `title: TEXT`, `intent: TEXT`, `tone: TEXT`, `content: TEXT`, `wordCount: INTEGER`, `updatedAt: INTEGER` | `index_writing_drafts_updatedAt` |

---

## 4. Ingestion, OCR & Indexing Pipeline Audit

### 4.1 Ingestion Flow
1. **SAF Import:** User selects PDF or text file via `ActivityResultContracts.OpenDocument()`.
2. **Permission Persistence:** `contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)`.
3. **Extraction (`PdfTextExtractor.kt`):**
   - Renders page using Android `PdfRenderer`.
   - Attempts embedded text extraction using lightweight Android PDF streams.
   - If extracted text < 40 characters (scanned/rasterized page), invokes `LocalOcrEngine` (`MLKitOcrEngine`).
   - Bitmaps are explicitly recycled immediately to prevent native memory exhaustion.
4. **Segmentation (`DocumentChunker.kt`):**
   - Segments extracted text into overlapping chunks of ~500 characters with 50-character overlap.
   - Preserves `documentId`, `pageNumber`, and `chunkIndex`.
5. **Persistence & FTS4:**
   - Chunks are inserted into `document_chunks`.
   - Room's `@Fts4(contentEntity = DocumentChunkEntity::class)` virtual table automatically indexes chunk text.

### 4.2 Retrieval Flow (`DocumentRetriever.kt`)
- **Query Sanitization:** Removes punctuation, lowercases, filters stop words.
- **FTS Search:** Queries `document_chunks_fts` using `term1* OR term2*`.
- **Lexical Fallback:** If FTS syntax fails or returns no results, falls back to in-memory word matching against all chunks in the document.
- **Limitation Identified:** `RetrievedChunk` does not retain `documentId` or document title. When calling `retrieveRelevantChunksAcrossDocuments`, chunk provenance to specific documents is lost prior to prompt formatting!

---

## 5. AI Orchestration & Model Lifecycle Audit

### 5.1 Model & Runtime
- **Model:** `gemma-4-E2B-it.litertlm` (~2.58 GB binary artifact on flash storage).
- **Runtime:** Google LiteRT-LM 0.19.1 (`com.google.ai.edge.litertlm:litertlm-android:0.19.1`).
- **Engine:** `LiteRtLmEngine.kt` implements `LocalAiEngine`:
  - `initialize()` verifies file presence and allocates engine on background thread.
  - `generateStream(prompt, options)` invokes LiteRT-LM C++ JNI bridge, emitting streaming tokens via Kotlin coroutine `Flow<String>`.
  - Supports cooperative cancellation: cancelling the coroutine calls LiteRT-LM interruption.

### 5.2 Orchestrator (`AiOrchestrator.kt`)
- Serves as the central gateway for all 7 modes: Ask AI, Image Analysis, Document Q&A, Problem Solving, Learn & Explain, Writing Studio, and AI Tools.
- Enforces single active generation across the entire application using mutex locking.
- Records Time to First Token (TTFT) and token throughput via `PerformanceTracker`.

### 5.3 Prompt Engineering (`PromptBuilder.kt`)
- Streamlined in Phase 2.1 to reduce template character count by ~18%.
- Provides mode-specific templates:
  - `buildChatPrompt`: multi-turn conversational context.
  - `buildDocumentQaPrompt`: grounded extraction with page citations.
  - `buildDocumentComparisonPrompt`: two-document comparative contrast.
  - `buildTroubleshootStepPrompt`: diagnostic step schema.
  - `buildLearnPrompt`: lesson outline and quiz schema.
  - `buildWritingPrompt`: drafting, rewriting, summarizing templates.
- **Missing for Phase 3:** No general `buildCrossSourceQaPrompt` capable of ingesting arbitrary multi-source evidence (documents, notes, chats, packs) with source attribution and conflict resolution.

---

## 6. PRD v1.1 Traceability Matrix for Phase 3

| PRD Section / Req ID | PRD Requirement Description | Current v0.2.1 Status | Phase 3 Target Release |
|---|---|---|---|
| **DOC-005 / DOC-006** | Answer questions grounded on documents with verifiable page citations. | Single-document only | **v0.3.0:** Cross-source grounding across multiple documents, notes & packs. |
| **LIB-001 / LIB-009** | Organize knowledge into documents and packs; preserve chunk provenance. | Segregated tabs, no collections | **v0.3.0:** Unified Knowledge Center, Collections, and Evidence Explorer. |
| **SRCH-001 (Proposed)** | Unified search across documents, conversations, notes, and knowledge packs. | Document-only FTS4; Chat uses SQL LIKE | **v0.3.0:** Unified search engine with multi-source filters and snippet ranking. |
| **TASK-001 (Proposed)** | Maintain ongoing task context across different workspaces. | Isolated per workspace | **v0.4.0:** Task sessions & shared context system. |
| **UTIL-001 – UTIL-006** | Deterministic calculations, conversions, date/time, QR reader, structured data. | Only LLM prompt tools exist | **v0.5.0:** Deterministic local calculation engine and utilities. |
| **AUTO-001 (Proposed)** | Natural language task planning with structured editable workflows. | Not implemented | **v0.6.0:** Personal task planning & workflow templates. |
| **INTEL-001 – INTEL-004**| Hybrid retrieval, personalization, evaluation datasets, and prompt budgeting. | Lexical FTS4 baseline | **v0.7.0:** Advanced retrieval & local evaluation framework. |
| **PROD-001 (Proposed)** | End-to-end integrated multi-stage workflows (Research, Learn, Write). | Individual manual navigation | **v0.8.0:** Integrated productivity workflows. |

---

## 7. Reusable vs. Missing Components for v0.3.0

### Reusable Components (Keep & Extend):
- `OpenFieldDatabase` & Room architecture: Schema version 4 can be cleanly migrated to version 5.
- `document_chunks_fts` SQLite FTS4 virtual table: Highly efficient, zero new native dependencies needed.
- `PdfTextExtractor` & `LocalOcrEngine`: Fully functional offline OCR and PDF extraction.
- `AiOrchestrator` & `LiteRtLmEngine`: Single shared CPU runtime proven stable.
- Material 3 Design Tokens & UI Components (`Cards.kt`, `Buttons.kt`, `TopBars.kt`, `MarkdownContent.kt`).

### Missing Components to Build for v0.3.0:
1. **`KnowledgeSource` Domain Model & Registry:** Unified interface representing Documents, Notes, Conversations, and Knowledge Packs under a single type hierarchy.
2. **Collections Infrastructure:**
   - Room Entity `KnowledgeCollectionEntity` (`knowledge_collections`).
   - Cross-reference table `CollectionSourceCrossRefEntity` (`collection_source_cross_ref`).
   - Collection DAO methods for creating, renaming, deleting, and assigning sources.
3. **Unified Search Engine (`UnifiedSearchEngine.kt`):**
   - Unified search coordinator executing across documents (FTS4), chat messages (`chat_messages`), writing drafts (`writing_drafts`), and knowledge packs (`knowledge_packs`).
   - Unified ranking and snippet highlighter.
4. **Cross-Source Retrieval Coordinator (`CrossSourceRetriever.kt`):**
   - Retrieves evidence across multiple selected sources or collections.
   - Retains `sourceId`, `sourceTitle`, `sourceType`, `pageNumber`, and `chunkIndex` on every chunk.
5. **Cross-Source Prompt Template (`PromptBuilder.buildCrossSourceQaPrompt`):**
   - Formats evidence from diverse sources with source tags `[Source: Title (Page X)]`.
   - Enforces attribution, comparative reasoning, and conflict identification.
6. **Evidence Explorer UI Component (`EvidenceExplorer.kt`):**
   - Bottom sheet / expandable panel displaying the exact excerpts used by Gemma, relevance scores, and direct navigation links.
7. **Unified Knowledge Center Screen (`KnowledgeCenterScreen.kt`):**
   - Elevates `LibraryScreen.kt` into a full Knowledge Center featuring Overview stats, Collections, All Sources, and Multi-source selection.

---

## 8. Database Migration Plan (Version 4 → 5)

To support Collections and multi-source organization without modifying existing document or chat tables, the database will be migrated from Version 4 to Version 5 using a non-destructive migration:

```sql
-- 1. Knowledge Collections Table
CREATE TABLE IF NOT EXISTS `knowledge_collections` (
    `id` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT NOT NULL,
    `colorHex` INTEGER NOT NULL,
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);
CREATE INDEX IF NOT EXISTS `index_knowledge_collections_updatedAt` ON `knowledge_collections` (`updatedAt`);

-- 2. Collection Source Cross-Reference Table
CREATE TABLE IF NOT EXISTS `collection_source_cross_ref` (
    `collectionId` TEXT NOT NULL,
    `sourceId` TEXT NOT NULL,
    `sourceType` TEXT NOT NULL,
    `addedAt` INTEGER NOT NULL,
    PRIMARY KEY(`collectionId`, `sourceId`),
    FOREIGN KEY(`collectionId`) REFERENCES `knowledge_collections`(`id`) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_sourceId` ON `collection_source_cross_ref` (`sourceId`);
CREATE INDEX IF NOT EXISTS `index_collection_source_cross_ref_collectionId` ON `collection_source_cross_ref` (`collectionId`);
```

### Data Preservation Guarantee:
- Zero tables dropped or altered.
- All existing chat conversations, messages, imported PDFs, OCR chunks, knowledge packs, drafts, troubleshooting sessions, and learning sessions remain 100% intact.
- Migration test suite will verify database upgrade from v4 to v5 with pre-existing data.

---

## 9. Performance & Resource Risk Assessment

| Risk Area | Potential Impact | Mitigation Strategy |
|---|---|---|
| **Multi-Source Context Overhead** | Exceeding Gemma 4 context budget (2048 tokens), causing truncation or high TTFT. | Implement hard context budget in `CrossSourceRetriever`: top-2 chunks per source, max 6 chunks total (~1,200 tokens), leaving ~800 tokens for system rules and generation. |
| **Search Latency Across Multiple Tables** | Querying 4 tables sequentially on UI thread causing frame drops. | Execute parallel coroutine queries on `Dispatchers.IO` using `async`/`awaitAll`; synthesize and sort results in memory within < 80 ms. |
| **Memory Allocation During Search** | Loading full text of all drafts and messages. | Return bounded projection (id, title, snippet) rather than full blob contents. |
| **Citation Fabrication** | Gemma generating plausible but non-existent page numbers or sources. | Implement strict post-generation citation validation in `CitationValidator.kt`: regex extract all `[Source: ...]` tags and match against provided evidence IDs. |

---

## 10. Gate 0 Audit Sign-Off

- **Baseline Verified:** v0.2.1 stable on Lava LXX503 (`app-release.apk`, SHA-256: `7f2df3bae1ed1cbec7084add57a60ddc70510fc03b03b760a43614dce5a39979`).
- **Audit Outcome:** Codebase is structurally clean, modular, and ready for Phase 3.
- **Immediate Recommendation:** Proceed to author technical design specifications (`PHASE_3_ARCHITECTURE.md`, `PHASE_3_DATABASE_DESIGN.md`, `PHASE_3_IMPLEMENTATION_PLAN.md`, `PHASE_3_TEST_STRATEGY.md`) before implementing v0.3.0 features.
