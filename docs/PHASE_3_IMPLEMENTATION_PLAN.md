# OpenField Phase 3 — Implementation Plan (v0.3.0 Unified Personal Knowledge)

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Status:** Approved for Implementation  
**Target Milestone:** OpenField v0.3.0  
**Baseline:** v0.2.1  
**Estimated Complexity:** High (Multi-source retrieval, Room v5 migration, UI unification)

---

## 1. Overview & Phased Strategy

Phase 3 is divided into 7 release milestones:
- **v0.3.0:** Unified Personal Knowledge (Connecting local knowledge sources, search, collections, cross-source Q&A).
- **v0.4.0:** Context-Aware Personal Assistant (Continuous task sessions & shared workspace context).
- **v0.5.0:** Practical Offline Intelligence (Deterministic calculator, unit converter, date/time, QR scanner, data utilities).
- **v0.6.0:** Personal Task Automation (Natural-language task planning, editable workflows, document generation).
- **v0.7.0:** Advanced Intelligence & Personalization (Hybrid retrieval, local evaluation dataset, user preferences).
- **v0.8.0:** Productivity & Integration (Full end-to-end multi-stage workflows).
- **v0.9.0:** Release Candidate (Comprehensive stabilization, airplane-mode audit, performance benchmarks).
- **v1.0.0:** Stable OpenField Release.

**Immediate Objective:** Implement and verify **v0.3.0 Unified Personal Knowledge**.

---

## 2. v0.3.0 Milestone Breakdown & Task List

### Milestone 3.1: Data Layer & Room Migration (v4 → v5)
- [ ] Create `KnowledgeCollectionEntity.kt` in `data/local/entity/`.
- [ ] Create `CollectionSourceCrossRefEntity.kt` in `data/local/entity/`.
- [ ] Create `CollectionDao.kt` in `data/local/dao/`.
- [ ] Add `CollectionDao` to `OpenFieldDatabase.kt`.
- [ ] Register new entities in `OpenFieldDatabase.kt` and bump version to `5`.
- [ ] Implement `OpenFieldDatabase.MIGRATION_4_5`.
- [ ] Add Room migration unit test in `DatabaseMigrationTest.kt` verifying data preservation.

### Milestone 3.2: Domain Models & Source Registry
- [ ] Create `KnowledgeSource.kt` in `core/model/`:
  - `KnowledgeSourceType` enum (`DOCUMENT`, `NOTE`, `CONVERSATION`, `KNOWLEDGE_PACK`, `LEARNING_TOPIC`).
  - `KnowledgeSource` model with ID, title, type, snippet, size, and retrieval scope flag.
  - `KnowledgeEvidence` model for cross-source retrieval snippets.
- [ ] Implement `KnowledgeSourceRegistry.kt` in `data/repository/`:
  - Aggregates sources from `DocumentDao`, `ChatDao`, `WritingDraftDao`, and `KnowledgePackDao`.
  - Exposes `getSourcesFlow()`, `getSourcesByType()`, and `getSourceById()`.
- [ ] Wire `collectionDao` into `OpenFieldRepository.kt`.

### Milestone 3.3: Unified Search Engine
- [ ] Implement `UnifiedSearchEngine.kt` in `feature/document/rag/`:
  - Coordinates parallel query execution across `document_chunks_fts`, `chat_messages`, `writing_drafts`, and `knowledge_packs`.
  - Generates highlighted 120-character preview snippets around matching keywords.
  - Returns ranked `List<UnifiedSearchResult>`.
- [ ] Add unit tests in `UnifiedSearchEngineTest.kt`.

### Milestone 3.4: Cross-Source Retrieval & Prompt Engineering
- [ ] Implement `CrossSourceRetriever.kt` in `feature/document/rag/`:
  - Ingests user query and target source IDs (or active collection ID).
  - Retrieves top-k chunks per source, scores relevance, deduplicates, and caps context budget to 1,200 tokens.
  - Maps results to `List<KnowledgeEvidence>`.
- [ ] Enhance `PromptBuilder.kt`:
  - Implement `buildCrossSourceQaPrompt(query, evidenceList, collectionName)`.
  - Enforce strict source attribution tags `[Source: <Title> (Page <P>)]`.
  - Enforce conflict disclosure and insufficient-evidence abstention.
- [ ] Add unit tests in `CrossSourceRetrieverTest.kt` and `CrossSourcePromptTest.kt`.

### Milestone 3.5: Citation Verification & Evidence Explorer
- [ ] Implement `CitationValidator.kt` in `feature/document/rag/`:
  - Regex parser extracting all `[Source: ...]` citations from model output.
  - Verifies that every cited title and page corresponds to an actual `KnowledgeEvidence` item.
  - Identifies fabricated citations.
- [ ] Implement `EvidenceExplorerSheet.kt` in `feature/document/`:
  - Expandable Compose bottom sheet showing the exact evidence passages fed to Gemma.
  - Interactive "View in Source" button navigating to the exact page/record.
  - "Exclude from Scope" button.

### Milestone 3.6: Unified Knowledge Center UI
- [ ] Create `KnowledgeCenterScreen.kt` in `feature/library/` (superseding and modernizing `LibraryScreen.kt`):
  - Top Segmented Navigation: `Overview` | `All Sources` | `Documents` | `Notes` | `Packs` | `Collections`.
  - **Overview Tab:** Live statistics cards (Total Sources, Documents, Notes, Conversations, Packs, Storage used) + Recent Ingestion shelf.
  - **Collections Tab:** Grid of collections with color tags, source counts, create collection button, and collection detail view.
  - **Multi-Source Selection Bar:** Floating action pill when 2+ sources are selected: *"Ask Across X Sources"*.
- [ ] Implement `CreateCollectionDialog.kt`.

### Milestone 3.7: Cross-Source Q&A Integration
- [ ] Enhance `DocumentQaController.kt` (or create `CrossSourceQaController.kt`) to support multi-source sessions.
- [ ] Wire multi-source selection from Knowledge Center to launch Q&A session scoped to selected sources or collection.
- [ ] Display interactive citation chips in answer bubbles linking to Evidence Explorer.
- [ ] Update `AppNavigation.kt` routes to include `KNOWLEDGE_CENTER` and cross-source parameters.

### Milestone 3.8: Stabilization, Release Build & Physical Device Verification
- [ ] Run full automated test suite (`.\gradlew.bat testDebugUnitTest`).
- [ ] Bump version code to `3` and version name to `"0.3.0"`.
- [ ] Build signed release APK (`app-release.apk`).
- [ ] Verify SHA-256 checksum.
- [ ] Install APK onto physical Lava LXX503 device via ADB.
- [ ] Test live:
  - Create a Collection ("Test Collection") and add `AnyScanner_10_29_2026.pdf` + `Devices & Repairs` pack.
  - Execute a cross-source question: *"Compare the certificate details with device repair topics."*
  - Verify citations and open Evidence Explorer on device.
- [ ] Tag git release `v0.3.0`.

---

## 3. Affected File Matrix

| File Path | Action | Description |
|---|---|---|
| `android/app/build.gradle.kts` | Modify | Update versionCode to 3, versionName to "0.3.0" |
| `android/app/src/main/java/com/openfield/app/data/local/entity/CollectionEntities.kt` | Create | `KnowledgeCollectionEntity`, `CollectionSourceCrossRefEntity` |
| `android/app/src/main/java/com/openfield/app/data/local/dao/CollectionDao.kt` | Create | DAO for collections and cross-reference queries |
| `android/app/src/main/java/com/openfield/app/data/local/OpenFieldDatabase.kt` | Modify | Add entities, `collectionDao()`, version = 5, `MIGRATION_4_5` |
| `android/app/src/main/java/com/openfield/app/core/model/KnowledgeModels.kt` | Create | `KnowledgeSource`, `KnowledgeEvidence`, `UnifiedSearchResult` |
| `android/app/src/main/java/com/openfield/app/data/repository/KnowledgeSourceRegistry.kt`| Create | Centralized source aggregator and resolver |
| `android/app/src/main/java/com/openfield/app/feature/document/rag/UnifiedSearchEngine.kt`| Create | Cross-table FTS and keyword search engine |
| `android/app/src/main/java/com/openfield/app/feature/document/rag/CrossSourceRetriever.kt`| Create | Multi-source context retrieval and budgeting |
| `android/app/src/main/java/com/openfield/app/feature/document/rag/CitationValidator.kt` | Create | Post-generation citation integrity verifier |
| `android/app/src/main/java/com/openfield/app/feature/ai/mode/PromptBuilder.kt` | Modify | Add `buildCrossSourceQaPrompt` |
| `android/app/src/main/java/com/openfield/app/data/repository/OpenFieldRepository.kt` | Modify | Wire `CollectionDao`, `KnowledgeSourceRegistry`, cross-source Q&A |
| `android/app/src/main/java/com/openfield/app/feature/document/EvidenceExplorer.kt` | Create | Expandable evidence inspection UI |
| `android/app/src/main/java/com/openfield/app/feature/library/KnowledgeCenterScreen.kt` | Create | Upgraded unified Knowledge Center with collections & stats |
| `android/app/src/main/java/com/openfield/app/navigation/AppNavigation.kt` | Modify | Update library routes to Knowledge Center |
| `android/app/src/test/java/com/openfield/app/DatabaseMigrationTest.kt` | Create | Test Room v4 to v5 migration |
| `android/app/src/test/java/com/openfield/app/CrossSourceRetrievalTest.kt` | Create | Test multi-source retrieval & citation validation |
| `android/app/src/test/java/com/openfield/app/UnifiedSearchEngineTest.kt` | Create | Test multi-table search ranking & snippet generation |

---

## 4. Definition of Done (DoD) for v0.3.0

1. **Room v5 Migration:** Unit tests confirm upgrade from v4 to v5 preserves all existing documents, chunks, chats, and drafts.
2. **Unified Search:** Searches documents, drafts, and packs, returning highlighted snippets in < 80 ms on device.
3. **Collections:** Users can create collections, assign sources, and persist them across app restarts.
4. **Cross-Source Q&A:** Gemma 4 E2B IT answers questions referencing multiple sources with verified citations.
5. **Evidence Explorer:** Excerpts used by Gemma are inspectable in the UI with direct links to sources.
6. **Automated Tests:** 100% unit tests pass with zero regressions.
7. **Physical Device Verification:** Tested live on Lava LXX503; release APK installed and verified.
