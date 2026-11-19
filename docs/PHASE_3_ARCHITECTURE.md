# OpenField Phase 3 — Technical Architecture Specification

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Status:** Approved for Implementation  
**Target Releases:** v0.3.0 (Unified Personal Knowledge) → v1.0.0 (Stable OpenField)  
**Platform:** Native Android (Kotlin 2.4.21, Jetpack Compose, Material 3)  
**Database:** Room 2.7.2 + SQLite FTS4  
**Local AI Engine:** Google LiteRT-LM 0.19.1 + Gemma 4 E2B IT (`gemma-4-E2B-it.litertlm`)

---

## 1. Architectural Vision & Core Invariants

Phase 3 transforms OpenField from a collection of isolated offline AI workspaces into a **Unified Personal Intelligence Platform**.

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                                 JETPACK COMPOSE UI                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ Knowledge Center │  │ Document Q&A     │  │ Ask AI Chat      │  │ Create&Write  │ │
│  │ (Collections,All)│  │ (Cross-Source)   │  │ (Continuous)     │  │ (From Sources)│ │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘  └───────┬───────┘ │
├───────────┼─────────────────────┼─────────────────────┼────────────────────┼─────────┤
│           ▼                     ▼                     ▼                    ▼         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│  │                        COORDINATION & PRESENTATION LAYER                        │ │
│  │  KnowledgeCenterViewModel │ CrossSourceQaController │ EvidenceExplorerState     │ │
│  └──────────────────────────────────────┬──────────────────────────────────────────┘ │
├─────────────────────────────────────────┼────────────────────────────────────────────┤
│                                         ▼                                            │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│  │                             DOMAIN & RETRIEVAL LAYER                            │ │
│  │  ┌───────────────────────┐ ┌─────────────────────────┐ ┌──────────────────────┐ │ │
│  │  │KnowledgeSourceRegistry│ │   UnifiedSearchEngine   │ │ CrossSourceRetriever │ │ │
│  │  │(Common Source Contract│ │(FTS4 + SQLite + Packs)  │ │(Multi-Source Ranking)│ │ │
│  │  └───────────────────────┘ └─────────────────────────┘ └──────────┬───────────┘ │ │
│  │                                                                   │             │ │
│  │  ┌────────────────────────────────────────────────────────┐       │             │ │
│  │  │                 Evidence & Citation Validator          │◄──────┘             │ │
│  │  │          (Verifiable Source IDs, Pages & Proofs)       │                     │ │
│  │  └────────────────────────────────────────────────────────┘                     │ │
│  └──────────────────────────────────────┬──────────────────────────────────────────┘ │
├─────────────────────────────────────────┼────────────────────────────────────────────┤
│                                         ▼                                            │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│  │                            AI ORCHESTRATION LAYER                               │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                               AiOrchestrator                              │  │ │
│  │  │    (Single Process-Scoped Lock, Context Budgeter, Token Streaming Engine) │  │ │
│  │  └─────────────────────────────────────┬─────────────────────────────────────┘  │ │
│  │                                        ▼                                          │ │
│  │  ┌───────────────────────────────────────────────────────────────────────────┐  │ │
│  │  │                      LiteRtLmEngine (LocalAiEngine)                       │  │ │
│  │  │              Gemma 4 E2B IT (2.58 GB weights running on CPU)              │  │ │
│  │  └───────────────────────────────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────┬──────────────────────────────────────────┘ │
├─────────────────────────────────────────┼────────────────────────────────────────────┤
│                                         ▼                                            │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│  │                          DATA & PERSISTENCE LAYER (Room v5)                     │ │
│  │  documents │ chunks │ chunks_fts │ chat_sessions │ chat_messages │ drafts       │ │
│  │  knowledge_packs │ app_settings │ knowledge_collections │ collection_cross_ref │ │
│  └─────────────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### Non-Negotiable Invariants:
1. **Single Inference Engine:** Only one instance of `LiteRtLmEngine` exists in the application process, managed exclusively by `AiOrchestrator`. No feature may spawn private model sessions.
2. **100% On-Device & Zero Telemetry:** User documents, queries, voice transcripts, and retrieved chunks are never transmitted over the network.
3. **Evidence-Based Grounding:** In cross-source Q&A, answers must be strictly grounded on retrieved passages with real, verifiable citations. Missing evidence must trigger clear disclosure rather than hallucinated answers.
4. **Deterministic Utilities:** Calculations, conversions, and string parsers are executed with deterministic algorithms, never delegated to generative LLM guesses.

---

## 2. Unified Knowledge Architecture (v0.3.0)

### 2.1 The Common Knowledge Source Model (`KnowledgeSource`)

To break down content silos without duplicating underlying file data, all local resources are abstracted into a common polymorphic domain model:

```kotlin
enum class KnowledgeSourceType {
    DOCUMENT,        // Imported PDF or text file
    NOTE,            // User note or writing draft
    CONVERSATION,    // Saved multi-turn chat session
    KNOWLEDGE_PACK,  // Installed offline knowledge pack
    LEARNING_TOPIC   // Saved learning module or lesson
}

data class KnowledgeSource(
    val id: String,                         // e.g. "doc_333183248232", "chat_sess_01"
    val type: KnowledgeSourceType,          // Source classification
    val title: String,                      // Human-readable title
    val snippet: String,                    // Short preview description
    val itemCount: Int,                     // Pages, messages, words, or topics
    val formattedSize: String,              // Display size (e.g. "1.6 MB", "24 msgs")
    val updatedAt: Long,                    // Epoch millis for sorting
    val isIndexed: Boolean = true,          // Ready for local retrieval
    val isInRetrievalScope: Boolean = true  // User toggle for AI search inclusion
)
```

### 2.2 Knowledge Source Registry (`KnowledgeSourceRegistry`)

`KnowledgeSourceRegistry` aggregates and synchronizes knowledge sources across Room tables:
- **Documents:** Mapped from `DocumentEntity` in `documents`.
- **Conversations:** Mapped from `ChatSessionEntity` in `chat_sessions`.
- **Notes/Drafts:** Mapped from `WritingDraftEntity` in `writing_drafts`.
- **Knowledge Packs:** Mapped from `KnowledgePackEntity` where `isInstalled = true`.

It exposes reactive Kotlin `Flow<List<KnowledgeSource>>` streams filtered by type, search query, or collection.

---

## 3. Collections Subsystem

Users can organize sources into semantic collections (e.g., "University Studies", "App Architecture", "Auto Repair").

### 3.1 Domain Models
```kotlin
data class KnowledgeCollection(
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long,
    val sourceCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 3.2 Scoping & Retrieval Integration
When a collection is selected as the active retrieval scope:
1. The collection's member source IDs are retrieved from `collection_source_cross_ref`.
2. Cross-source retrieval restricts candidate chunks to those source IDs.
3. Excluded sources are strictly omitted from prompt injection.

---

## 4. Unified Local Search Engine (`UnifiedSearchEngine`)

`UnifiedSearchEngine` provides lightning-fast (< 50 ms) local keyword search across all supported sources without requiring model inference.

### 4.1 Search Execution Strategy
When a query is submitted:
1. **Sanitization:** Strips control characters, lowercases, and splits into search terms.
2. **Parallel Table Execution:**
   - **Documents:** Queries `document_chunks_fts` using `term1* OR term2*`.
   - **Chat History:** Queries `chat_messages` using index on `sessionId` + text matching.
   - **Writing Drafts:** Queries `writing_drafts` title and content.
   - **Knowledge Packs:** Searches installed pack titles, descriptions, and topic tags.
3. **Result Unification & Scoring:**
   - Calculates lexical BM25/term-frequency score.
   - Generates 120-character contextual snippet highlighting matching terms.
   - Returns sorted `List<UnifiedSearchResult>`.

```kotlin
data class UnifiedSearchResult(
    val sourceId: String,
    val sourceType: KnowledgeSourceType,
    val sourceTitle: String,
    val snippet: String,
    val pageNumber: Int? = null,
    val score: Float = 1.0f
)
```

---

## 5. Cross-Source Retrieval & Reasoning Architecture

Cross-Source Q&A is the centerpiece of v0.3.0, enabling questions such as:
> *"Compare the troubleshooting guidelines in my Devices pack with the error codes in my scanned manual."*

### 5.1 Retrieval Pipeline
```
User Query + Source Scope
       │
       ▼
CrossSourceRetriever.retrieveEvidence(query, scope)
       │
       ├─► Resolve Source IDs in Scope
       ├─► Parallel Retrieval (FTS4 Chunks + Drafts + Pack Content)
       ├─► Deduplicate & Diversity Re-ranking
       ├─► Context Budget Enforcer (Max 1,200 tokens across sources)
       │
       ▼
PromptBuilder.buildCrossSourceQaPrompt(query, evidenceList)
       │
       ▼
AiOrchestrator.orchestrate(request) ──► Gemma 4 E2B IT (CPU)
       │
       ▼
CitationValidator.validate(responseStream, evidenceList)
       │
       ▼
Rendered Answer with Interactive Citation Cards + Evidence Explorer
```

### 5.2 Evidence Passage Representation
```kotlin
data class KnowledgeEvidence(
    val evidenceId: String,
    val sourceId: String,
    val sourceTitle: String,
    val sourceType: KnowledgeSourceType,
    val pageNumber: Int? = null,
    val section: String? = null,
    val text: String,
    val relevanceScore: Float = 1.0f
)
```

### 5.3 Context Budget Allocation
Gemma 4 E2B IT operates within an optimal 2,048-token context window on mobile CPU:
- **System Directives & Rules:** ~250 tokens.
- **Retrieved Evidence:** Maximum 1,200 tokens (up to 4–6 passages, ~200–300 tokens each).
- **User Query & Scope Definition:** ~100 tokens.
- **Model Output Generation:** ~500 tokens.

If a query spans 5 sources, the retriever selects the top 1–2 most relevant chunks from each, ensuring cross-source diversity without overflowing the token budget.

### 5.4 Cross-Source Prompt Contract
The prompt strictly enforces evidence attribution:
```
Answer the question using ONLY the provided multi-source evidence.

RULES:
1. Ground your answer strictly on the facts in the provided evidence.
2. For EVERY claim, cite the source using: [Source: <Title>, Page <P>] or [Source: <Title>].
3. If sources present conflicting information, explicitly point out the discrepancy.
4. If the provided evidence is insufficient to answer the question, state:
   "The selected sources do not contain sufficient information to answer this question."
5. Never invent sources, page numbers, or external facts.

EVIDENCE:
---
[Source: AnyScanner_10_29_2026.pdf, Page 1]
Candidate: JAYAKUMAR C, Examination: SSLC Mar 2025...
---
[Source: Devices & Repairs, Section: Multimeter Tests]
Check DC voltage across battery terminals...

QUESTION:
<User Query>

GROUNDED ANSWER:
```

### 5.5 Citation Validation (`CitationValidator`)
Post-generation or inline stream verification:
- Extracts all citation tags `[Source: ..., Page ...]` using regex.
- Verifies that every cited source title and page number matches an item in `evidenceList`.
- Discards or flags hallucinated references in the UI with a warning badge.

---

## 6. Evidence Explorer Component Architecture

The **Evidence Explorer** provides radical transparency into local AI reasoning:
- Renders as an expandable bottom sheet or side panel on the Cross-Source Q&A screen.
- Lists all `KnowledgeEvidence` passages supplied to Gemma.
- Displays:
  - Source Title & Type badge (`[PDF]`, `[Note]`, `[Pack]`).
  - Page / Section reference.
  - Relevance score percentage.
  - Exact snippet text.
  - Quick Action: **View Source** (opens document at exact page).
  - Quick Action: **Exclude Source** (removes from scope and regenerates answer).

---

## 7. Unified Knowledge Center UI Architecture

Replaces the legacy tabbed library with a modern, high-density Knowledge Hub:

### UI Structure:
1. **Top Segmented Filter:**
   - `Overview` | `All Sources` | `Documents` | `Notes` | `Packs` | `Collections`
2. **Overview Screen:**
   - Metrics Grid: Total Indexed Sources, Document Count, Saved Notes, Chat Sessions, Installed Packs, Storage Used.
   - Quick Collections Shelf: Horizontal row of collection cards with source count and color badges.
   - Recent Ingestion Feed: Latest imported files or edited drafts.
3. **Collections Management:**
   - Create Collection modal (Title, Description, Color picker).
   - Collection Detail view with source reordering, removal, and "Ask Collection" shortcut.
4. **Multi-Source Selection Bar:**
   - Checkboxes on source cards allowing users to select 2+ sources and tap **"Ask Across Selected Sources"** to launch Cross-Source Q&A immediately.

---

## 8. Reliability, Concurrency & Lifecycle Guarantees

1. **Coroutine Dispatching:**
   - All database reads, file I/O, FTS searches, and OCR extractions execute on `Dispatchers.IO`.
   - LLM generation runs on a dedicated background thread pool.
   - Compose state updates occur exclusively on `Dispatchers.Main`.
2. **Cooperative Cancellation:**
   - Cancelling a cross-source query stops the active coroutine and immediately halts LiteRT-LM token generation.
3. **Memory Bounding:**
   - Bitmaps are recycled immediately.
   - FTS queries use strict `LIMIT` clauses (maximum 10 results).
   - Garbage collector overhead remains < 5% during cross-source retrieval.
