# CRUX Phase 3 — Test Strategy & Quality Assurance Plan

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Status:** Approved for Implementation  
**Target Milestone:** CRUX v0.3.0 → v1.0.0  
**Test Frameworks:** JUnit 4, Kotlin Coroutines Test, Roborazzi / AndroidX Test  
**Physical Hardware:** Lava LXX503 (Android 14 API 34, ARM64, 6 GB RAM)

---

## 1. Quality Philosophy & Verification Levels

In accordance with CRUX project rules (`AGENTS.md`), quality is never claimed without evidence. The test strategy enforces strict separation across four distinct verification levels:

1. **Level 1: Automated Unit Tests (`testDebugUnitTest`):**
   - Validates pure business logic, string transformations, regex parsing, prompt builders, schema validators, scoring functions, and deterministic utilities. Runs rapidly in JVM.
2. **Level 2: Database Migration & Integration Tests:**
   - Validates Room schema transitions, foreign key constraints, cascade deletions, and SQLite FTS4 virtual table queries using real SQLite instances.
3. **Level 3: Physical Device Verification (Lava LXX503):**
   - Validates end-to-end user workflows, Android SAF file permissions, LiteRT-LM C++ JNI bridge execution on mobile CPU, memory stability, thermal ceilings, and UI responsiveness.
4. **Level 4: Offline & Airplane-Mode Verification:**
   - Validates that all local features operate 100% offline without network connectivity after initial setup.

---

## 2. Test Suites for v0.3.0 (Unified Personal Knowledge)

### 2.1 Database & Migration Tests (`DatabaseMigrationTest.kt`)
- **Objective:** Ensure upgrading from Database Version 4 to Version 5 preserves all existing user content.
- **Test Scenarios:**
  - `migration_4_to_5_createsCollectionsAndCrossRefTables`: Verifies new tables exist with proper column types.
  - `migration_4_to_5_preservesExistingDocumentsAndChats`: Pre-populates a v4 database with documents and chat messages; executes `MIGRATION_4_5`; verifies all documents and messages remain readable.
  - `collectionCascadeDeletion_removesCrossRefs_preservesSourceRecords`: Deleting a collection deletes cross-references but leaves the underlying document intact.
  - `sourceRemoval_cleansCrossRefEntries`: Removing a document cleans all referencing rows in `collection_source_cross_ref`.

### 2.2 Knowledge Source Registry Tests (`KnowledgeSourceRegistryTest.kt`)
- **Objective:** Verify polymorphic aggregation of documents, drafts, chats, and packs.
- **Test Scenarios:**
  - `registry_aggregatesAllSourceTypes`: Verifies that documents, drafts, and installed packs are merged into `List<KnowledgeSource>`.
  - `registry_filtersByType`: Filtering by `DOCUMENT` returns only document sources.
  - `registry_calculatesFormattedSizeCorrectly`: Checks byte size formatting (`1.6 MB`, `24 msgs`, `120 words`).

### 2.3 Unified Search Engine Tests (`UnifiedSearchEngineTest.kt`)
- **Objective:** Verify fast, accurate search across multiple Room tables.
- **Test Scenarios:**
  - `search_matchesAcrossDocumentsAndPacks`: Query matching terms in both a document chunk and a pack topic returns both with proper source type tags.
  - `search_generatesHighlightedSnippet`: Verifies snippet contains matching keywords with surrounding context (~120 chars).
  - `search_handlesPunctuationAndStopWords`: Query with symbols and stop words is sanitized without throwing SQLite syntax exceptions.
  - `search_returnsEmptyListForZeroMatches`: Graceful empty state when no terms match.

### 2.4 Cross-Source Retrieval Tests (`CrossSourceRetrievalTest.kt`)
- **Objective:** Verify evidence extraction, deduplication, and context budgeting across multiple sources.
- **Test Scenarios:**
  - `retrieval_limitsChunksPerSource`: Bounded to max 2 chunks per source.
  - `retrieval_enforcesTokenBudget`: Context across 4 sources does not exceed 1,200 tokens.
  - `retrieval_preservesSourceIdAndPageNumber`: Every `KnowledgeEvidence` item maintains its original document ID, title, and page number.
  - `retrieval_respectsExcludedSources`: Sources marked `isInRetrievalScope = false` are never returned in evidence.

### 2.5 Cross-Source Prompt Tests (`CrossSourcePromptTest.kt`)
- **Objective:** Verify multi-source prompt generation and system directives.
- **Test Scenarios:**
  - `buildCrossSourceQaPrompt_formatsAttributionTags`: Formats each chunk with `[Source: <Title> (Page <P>)]`.
  - `buildCrossSourceQaPrompt_includesConflictRules`: Ensures prompt contains rules to identify conflicting information.
  - `buildCrossSourceQaPrompt_includesAbstentionDirective`: Ensures prompt contains instructions to abstain if evidence is insufficient.

### 2.6 Citation Validator Tests (`CitationValidatorTest.kt`)
- **Objective:** Verify post-generation citation checking.
- **Test Scenarios:**
  - `validateCitations_validReferences_markedVerified`: Output citing `[Source: DocA, Page 1]` matching evidence is validated.
  - `validateCitations_hallucinatedPage_markedInvalid`: Output citing `[Source: DocA, Page 99]` when only Page 1 was in evidence is flagged as unverified.
  - `validateCitations_hallucinatedSource_markedInvalid`: Output citing non-existent source is flagged.

---

## 3. Physical Device Verification Protocols (Lava LXX503)

| Protocol ID | Test Workflow | Expected Result | Device Evidence Required |
|---|---|---|---|
| **DEV-030-01** | **Database Upgrade & Data Integrity:**<br>1. Install v0.3.0 over existing v0.2.1 installation.<br>2. Launch app.<br>3. Open Knowledge Center. | Pre-existing documents (`AnyScanner_10_29_2026.pdf`) and chats remain present and readable. Room migration executes with zero exceptions. | Logcat filter on `OpenFieldDatabase` + screenshot of Document list. |
| **DEV-030-02** | **Collection Creation & Management:**<br>1. In Knowledge Center, tap "New Collection".<br>2. Name it "Certificates & Tech" with blue color tag.<br>3. Add `AnyScanner_10_29_2026.pdf` and `Devices & Repairs` pack.<br>4. Restart app. | Collection persists with source count = 2. Retained after process kill. | Screenshot of Collection Detail screen before and after process kill. |
| **DEV-030-03** | **Unified Local Search:**<br>1. In Knowledge Center search bar, enter `"certificate"`.<br>2. Enter `"multimeter"`. | Results return matching document and pack respectively with highlighted snippets in < 80 ms. | Screen recording / screenshot of search result view. |
| **DEV-030-04** | **Cross-Source Q&A:**<br>1. Select `AnyScanner_10_29_2026.pdf` and `Devices & Repairs` pack.<br>2. Tap "Ask Across 2 Sources".<br>3. Ask: *"Summarize the candidate details and multimeter maintenance instructions."* | Local Gemma 4 E2B IT generates structured comparative answer on CPU citing both `AnyScanner_10_29_2026.pdf (Page 1)` and `Devices & Repairs`. | Screenshot of streaming answer with clickable citation cards. |
| **DEV-030-05** | **Evidence Explorer Inspection:**<br>1. Tap the Evidence Explorer button on the generated answer.<br>2. Inspect retrieved excerpts. | Bottom sheet opens displaying the exact chunks, page numbers, and relevance scores. Tapping "View in Source" opens the source document. | Screenshot of Evidence Explorer bottom sheet. |

---

## 4. Performance & Resource Benchmarks

Tested on Lava LXX503 with Unisoc T616 Octa-core CPU:

| Metric | Target Budget | Verification Method |
|---|---|---|
| **Room v4 → v5 Migration Latency** | < 150 ms | `System.currentTimeMillis()` in `OpenFieldDatabase.kt` |
| **Unified Search Latency (All Tables)** | < 80 ms | Measured via `PerformanceTracker.recordSearchLatency()` |
| **Cross-Source Retrieval Latency** | < 250 ms | Measured via `PerformanceTracker.recordRetrievalLatency()` |
| **Time to First Token (TTFT)** | < 6,000 ms | LiteRT-LM CPU generation timestamp on mobile CPU |
| **Token Generation Speed** | > 3.0 tokens/s | Monitored live during cross-source generation |
| **Heap Memory Overhead** | < 40 MB | Android Profiler / `Runtime.getRuntime().totalMemory()` |
| **Thermal Ceiling** | < 42.0°C | Device battery thermal sensor reading |

---

## 5. Release Quality Gate (v0.3.0 Acceptance Criteria)

Before declaring v0.3.0 complete:
- [ ] 100% of automated unit tests pass (`.\gradlew.bat testDebugUnitTest`).
- [ ] Room migration v4 → v5 verified with pre-existing user data.
- [ ] Unified search verified across documents, drafts, and knowledge packs.
- [ ] Collections verified with creation, source assignment, and persistence.
- [ ] Cross-source Q&A verified on physical hardware with local Gemma 4 E2B IT.
- [ ] Citations verified against real source chunks; Evidence Explorer functional.
- [ ] Signed release APK built (`app-release.apk`) and SHA-256 independently verified.
- [ ] Clean Git working tree and release tag `v0.3.0` committed.
