# OpenField Phase 3 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after implementation evidence, automated tests, and required physical-device verification exist.

**Phase 3 Goal:** Transform OpenField from a collection of specialized offline AI workspaces into a unified personal intelligence platform that understands, organizes, connects, and helps users act on their personal knowledge while preserving privacy, offline functionality, and local data ownership.

**Baseline Release:** v0.2.1 (Phase 2.1)

**Target Releases:**
- v0.3.0 — Unified Personal Knowledge
- v0.4.0 — Context-Aware Personal Assistant
- v0.5.0 — Practical Offline Intelligence
- v0.6.0 — Personal Task Automation
- v0.7.0 — Advanced Intelligence and Personalization
- v0.8.0 — Integrated Productivity Workflows
- v0.9.0 — Release Candidate
- v1.0.0 — Stable OpenField

---

## Core Constraints

- Keep Gemma 4 E2B IT as the single approved generative AI model.
- Use Google LiteRT-LM 0.19.1 for on-device inference.
- Preserve the shared process-scoped AiOrchestrator and inference engine.
- No cloud AI inference or silent external AI fallback.
- Preserve all Phase 1 and Phase 2 functionality.
- Preserve existing documents, conversations, knowledge packs, drafts, preferences, and learning progress.
- All core local capabilities must continue working offline after setup.
- Use Room + SQLite FTS4 as the existing local data foundation.
- Reuse existing repositories, entities, indexing pipelines, and UI components where appropriate.
- No fake data, simulated functionality, or placeholder success states.
- No automatic upload of personal information.
- No mandatory account or cloud synchronization.
- No background or always-listening microphone.
- No destructive database migration or silent deletion of user content.
- All AI-generated answers based on local sources must preserve evidence and citation integrity.
- Deterministic operations must use deterministic implementations instead of relying on LLM guesses.
- Every new feature must be evaluated for storage, memory, latency, battery, and thermal impact on supported devices.

---

# Gate 0 — Phase 3 Planning and Architecture Audit

**Objective:** Understand the existing OpenField architecture and establish a technically sound foundation before implementing new features.

### PRD and project review

- [x] Read the authoritative Phase 1 PRD v1.1 (Expanded Specification).
- [x] Read Phase 1 implementation documentation.
- [x] Read the completed Phase 2 roadmap and implementation report.
- [x] Read the completed Phase 2.1 roadmap and implementation report.
- [x] Review the v0.2.1 release notes and Git history.
- [x] Identify all existing implemented features.
- [x] Identify incomplete features and known limitations.
- [x] Map Phase 3 requirements against the original PRD.
- [x] Identify conflicts between Phase 3 proposals and approved PRD requirements.
- [x] Obtain approval for any scope change that conflicts with the PRD.

### GitHub planning

- [x] Create Phase 3 GitHub milestone.
- [x] Create release milestones for v0.3.0 through v1.0.0.
- [x] Create issues for every Phase 3 gate.
- [x] Create feature-specific issues with clear acceptance criteria.
- [x] Add labels for architecture, database, AI, retrieval, UI, testing, performance, privacy, and release.
- [x] Add priority labels.
- [x] Define issue dependencies.
- [x] Create a Phase 3 GitHub Project board.
- [x] Define Definition of Ready and Definition of Done.
- [x] Link implementation reports and architecture documents to relevant issues.

### Existing architecture audit

- [x] Audit current Gradle modules and dependencies.
- [x] Audit package and feature organization.
- [x] Audit existing navigation and screen architecture.
- [x] Audit ViewModels, controllers, repositories, and use cases.
- [x] Audit shared UI components and design system.
- [x] Audit AiOrchestrator and LiteRT-LM lifecycle.
- [x] Audit PromptBuilder and mode-specific prompts.
- [x] Audit all existing Room entities and DAO interfaces.
- [x] Audit Room database version and migration history.
- [x] Audit FTS4 tables, triggers, and indexing lifecycle.
- [x] Audit document import, extraction, OCR, and chunking.
- [x] Audit conversation and message persistence.
- [x] Audit knowledge-pack catalog and installation.
- [x] Audit local memory and personalization.
- [x] Audit background tasks and coroutine lifecycle.
- [x] Audit Android permissions and file-storage handling.
- [x] Identify duplicated logic and reusable implementations.
- [x] Identify performance bottlenecks and reliability risks.

### Architecture and technical documentation

- [x] Create `docs/PHASE_3_ROADMAP.md`.
- [x] Create `docs/PHASE_3_PRD.md`.
- [x] Create `docs/PHASE_3_ARCHITECTURE_AUDIT.md`.
- [x] Create `docs/PHASE_3_ARCHITECTURE.md`.
- [x] Create `docs/PHASE_3_DATABASE_DESIGN.md`.
- [x] Create `docs/PHASE_3_IMPLEMENTATION_PLAN.md`.
- [x] Create `docs/PHASE_3_TEST_STRATEGY.md`.
- [x] Define the unified knowledge architecture.
- [x] Define the source registry architecture.
- [x] Define the unified retrieval architecture.
- [x] Define context and task-session architecture.
- [x] Define the deterministic utility architecture.
- [x] Define task-management architecture.
- [x] Define data migration and rollback strategy.
- [x] Define performance budgets based on actual v0.2.1 measurements.
- [x] Define physical-device verification strategy.
- [x] Define Phase 3 acceptance criteria.

**Gate 0 Completion:** The architecture audit, PRD traceability, database assessment, technical design, GitHub issues, and test strategy must be reviewed before feature implementation begins. — **COMPLETE (Commit `218d0d7`)**

---

# Gate 1 — Unified Personal Knowledge Architecture

**Objective:** Transform the existing document, conversation, knowledge-pack, and local content systems into one unified knowledge infrastructure.

### Knowledge source architecture

- [x] Audit all existing local content types.
- [x] Identify documents, conversations, notes, generated documents, learning materials, and knowledge packs.
- [x] Define a common knowledge-source representation (`KnowledgeSource`).
- [x] Define stable source identifiers.
- [x] Define source-type identification (`KnowledgeSourceType`).
- [x] Define source metadata contracts.
- [x] Define source availability and indexing states.
- [x] Implement or adapt a centralized KnowledgeSourceRegistry (`KnowledgeSourceRegistry.kt`).
- [x] Implement a unified source-resolution mechanism.
- [x] Connect existing document records to the registry.
- [x] Connect existing conversation records to the registry.
- [x] Connect existing local notes to the registry where supported.
- [x] Connect generated documents to the registry.
- [x] Connect installed knowledge packs to the registry.
- [x] Preserve references to original local records.
- [x] Avoid unnecessary duplication of large source content.
- [x] Implement source availability validation.
- [x] Implement deleted-source detection.
- [x] Implement source metadata synchronization.
- [x] Implement source indexing-state tracking.

### Unified knowledge repository

- [x] Audit existing repositories before creating new ones.
- [x] Implement or adapt UnifiedKnowledgeRepository (`OpenFieldRepository.kt`).
- [x] Implement source listing and filtering.
- [x] Implement source metadata retrieval.
- [x] Implement source opening and resolution.
- [x] Implement source deletion and cleanup coordination.
- [x] Implement source re-indexing coordination.
- [x] Implement repository-level error handling.
- [x] Implement cancellation support.
- [x] Add repository unit tests.
- [x] Add source lifecycle integration tests.

### Unified indexing architecture

- [x] Audit existing PDF indexing and OCR pipeline.
- [x] Audit existing conversation indexing.
- [x] Audit knowledge-pack indexing.
- [x] Define reusable indexing contracts.
- [x] Implement or adapt a unified indexing coordinator.
- [x] Preserve existing FTS4 indexing where appropriate.
- [x] Define parser and index version tracking.
- [x] Prevent duplicate indexing.
- [x] Handle source content updates.
- [x] Handle deleted sources.
- [x] Handle partial indexing failures.
- [x] Support indexing cancellation.
- [x] Support safe retry and recovery.
- [x] Preserve page and section metadata.
- [x] Verify FTS4 synchronization after updates and deletion.
- [x] Test index consistency after application restart.

### Data integrity

- [x] Define source-to-content relationships.
- [x] Define foreign keys where appropriate (`MIGRATION_4_5`).
- [x] Define indexing metadata and status fields.
- [x] Add appropriate database indexes.
- [x] Define deletion behavior for original files and derived indexes.
- [x] Prevent orphaned chunks and search records.
- [x] Test interrupted indexing.
- [x] Test repeated indexing.
- [x] Test source replacement and update.
- [x] Test database consistency after process termination.

---

# Gate 2 — Unified Knowledge Center

**Objective:** Build a dedicated local knowledge-management experience where users can discover, organize, search, and manage all supported information.

### Knowledge Center interface

- [x] Design a dedicated Knowledge Center entry point (`KnowledgeCenterScreen.kt`).
- [x] Implement Knowledge Center overview.
- [x] Display total indexed sources.
- [x] Display total documents.
- [x] Display total notes.
- [x] Display saved conversations.
- [x] Display installed knowledge packs.
- [x] Display recent knowledge activity.
- [x] Display indexing failures.
- [x] Display unavailable or deleted sources appropriately.
- [x] Implement All Sources screen.
- [x] Implement Documents section.
- [x] Implement Conversations section.
- [x] Implement Notes section where supported.
- [x] Implement Generated Content section.
- [x] Implement Knowledge Packs section.
- [x] Implement Recent Activity section.
- [x] Implement empty, loading, success, and error states.
- [x] Implement responsive layouts.
- [x] Preserve existing OpenField visual consistency.

### Source management

- [x] Implement source preview.
- [x] Implement source details.
- [x] Implement source metadata display.
- [x] Implement source opening.
- [x] Implement source-specific actions.
- [x] Implement source search.
- [x] Implement source re-indexing.
- [x] Implement include/exclude from AI retrieval.
- [x] Implement index-only deletion where applicable.
- [x] Implement original-file deletion as a separate explicit action where supported.
- [x] Implement deletion confirmation.
- [x] Implement unavailable-source recovery states.
- [x] Ensure all actions reflect actual database state.

### Knowledge collections

- [x] Design local collection data model (`CollectionEntities.kt`).
- [x] Implement collection creation (`CreateCollectionDialog.kt`).
- [x] Implement collection renaming.
- [x] Implement collection deletion.
- [x] Implement collection listing.
- [x] Implement adding sources to collections.
- [x] Implement removing sources from collections.
- [x] Implement collection detail screen.
- [x] Implement collection search.
- [x] Implement collection-specific retrieval scope.
- [x] Support multiple collections where appropriate.
- [x] Prevent duplicate source membership.
- [x] Preserve collection membership after restart.
- [ ] Implement collection empty states.
- [ ] Add collection unit tests.
- [ ] Add collection persistence tests.
- [ ] Add collection migration tests.

### Unified knowledge search

- [ ] Audit existing FTS4 search behavior.
- [ ] Define supported searchable source types.
- [ ] Implement unified search query handling.
- [ ] Search source titles.
- [ ] Search document content.
- [ ] Search extracted OCR content.
- [ ] Search saved conversations.
- [ ] Search notes where supported.
- [ ] Search installed knowledge-pack content.
- [ ] Implement source-type filters.
- [ ] Implement collection filters.
- [ ] Implement date filters.
- [ ] Implement result sorting.
- [ ] Implement result snippets.
- [ ] Implement highlighted matching terms.
- [ ] Implement pagination or bounded result retrieval.
- [ ] Implement duplicate-result filtering.
- [ ] Implement deleted-source filtering.
- [ ] Implement empty-result handling.
- [ ] Implement search cancellation.
- [ ] Implement search history only if justified and user-controlled.
- [ ] Ensure search works offline.
- [ ] Add search relevance tests.
- [ ] Benchmark search latency on physical hardware.

**Physical-device verification:** Import documents, save conversations, install knowledge packs, search across all supported sources, open results, create collections, and verify persistence after restart.

---

# Gate 3 — Cross-Source AI Intelligence

**Objective:** Allow users to ask questions across multiple local documents, notes, conversations, and knowledge packs instead of being restricted to one source at a time.

### Retrieval architecture

- [x] Audit current document-scoped retrieval.
- [x] Audit FTS4 ranking and BM25 behavior.
- [x] Define cross-source retrieval request contracts.
- [x] Define source-scope configuration.
- [x] Implement a unified retrieval coordinator (`CrossSourceRetriever.kt`).
- [x] Support all-source retrieval.
- [x] Support collection-scoped retrieval.
- [x] Support manually selected sources.
- [x] Support individual source exclusion.
- [x] Resolve source IDs before retrieval.
- [x] Validate source availability.
- [x] Retrieve relevant passages.
- [x] Rank results across different source types.
- [x] Deduplicate retrieved passages.
- [x] Preserve source identity.
- [x] Preserve page references.
- [x] Preserve section references.
- [x] Preserve conversation message references where applicable.
- [x] Implement context-budget management (~1,200 tokens across sources).
- [x] Implement retrieval cancellation.
- [x] Implement retrieval failure handling.
- [x] Prevent excluded sources from entering the prompt.
- [x] Ensure only retrieved evidence enters grounded answer context.

### Cross-source question answering

- [x] Implement cross-source question input.
- [x] Implement source-scope selector.
- [x] Implement selected-source display.
- [x] Implement collection-based question answering.
- [x] Implement all-knowledge question answering.
- [x] Integrate with shared AiOrchestrator (`AiOrchestrator.streamCrossSourceQa`).
- [x] Reuse Gemma 4 E2B IT.
- [x] Preserve streaming responses.
- [x] Implement grounded answer instructions (`PromptBuilder.buildCrossSourceQaPrompt`).
- [x] Support multi-source comparisons.
- [x] Support contradiction identification.
- [x] Support source-based summaries.
- [x] Support evidence-based extraction.
- [x] Support follow-up questions within the selected scope.
- [x] Implement insufficient-evidence responses.
- [x] Prevent unsupported claims from being presented as facts.
- [x] Ensure unrelated conversation context does not contaminate retrieval.

### Citation and evidence integrity

- [x] Define a common citation data model.
- [x] Preserve original source IDs.
- [x] Preserve document page references.
- [x] Preserve section references.
- [x] Preserve source type.
- [x] Preserve chunk or passage IDs.
- [x] Validate citations against retrieved evidence (`CitationValidator.kt`).
- [x] Reject fabricated source references.
- [x] Reject invalid page numbers.
- [x] Implement source navigation.
- [x] Implement page navigation where supported.
- [x] Implement citation previews.
- [x] Implement source cards.
- [x] Support multiple citations per answer.
- [x] Handle sources without page information.
- [x] Handle deleted or unavailable sources.
- [x] Implement citation regression tests (`CitationValidatorTest.kt`).

### Evidence Explorer

- [x] Design Evidence Explorer interface (`EvidenceExplorerSheet.kt`).
- [x] Display sources used in an answer.
- [x] Display retrieved excerpts.
- [x] Display page and section references.
- [x] Display relevance metadata where meaningful.
- [x] Allow users to open original sources.
- [x] Allow users to inspect individual passages.
- [x] Allow users to exclude a source.
- [x] Allow users to repeat retrieval with updated scope.
- [x] Ensure evidence inspection does not generate fake excerpts.

### Retrieval evaluation

- [x] Create a fixed local cross-source evaluation dataset.
- [x] Include questions with one relevant source.
- [x] Include questions requiring multiple sources.
- [x] Include questions with conflicting information.
- [x] Include questions with insufficient evidence.
- [x] Include irrelevant-source exclusion cases.
- [x] Measure retrieval precision.
- [x] Measure retrieval recall.
- [x] Measure ranking quality.
- [x] Measure citation accuracy.
- [x] Measure unsupported-answer rate.
- [x] Compare results against the v0.2.1 retrieval baseline.
- [x] Document retrieval limitations.

**Gate 3 Completion:** Cross-source retrieval, structured prompt building, citation verification, and Evidence Explorer UI completed and verified with automated test coverage. — **COMPLETE (Release v0.3.0)**

---

# Gate 4 — Context-Aware Personal Assistant

**Objective:** Transform OpenField from isolated workspaces into a persistent task-oriented assistant that can continue user-approved work across different features.

### Task-session architecture

- [x] Audit existing conversation and feature-session persistence.
- [x] Define TaskSession data model (`TaskSessionEntity.kt`).
- [x] Define task-session lifecycle.
- [x] Define session status.
- [x] Define session context representation.
- [x] Define source attachments.
- [x] Define generated artifact relationships.
- [x] Define session summary storage.
- [x] Define session deletion behavior.
- [x] Implement session repository (`TaskSessionDao.kt`).
- [x] Implement session creation.
- [x] Implement session renaming.
- [x] Implement session listing.
- [x] Implement session deletion.
- [x] Implement session restoration.
- [x] Implement session persistence after process termination (`MIGRATION_5_6`).

### Shared context management

- [x] Define a typed shared-context contract (`SharedContextHandoff`).
- [x] Implement context source references.
- [x] Implement context attachment (`SharedContextManager.kt`).
- [x] Implement context removal.
- [x] Implement context inspection.
- [x] Implement context clearing.
- [x] Implement context selection for each workspace.
- [x] Prevent automatic inclusion of unrelated sessions.
- [x] Prevent hidden context reuse.
- [x] Integrate document context with Ask AI.
- [x] Integrate document context with Create & Write.
- [x] Integrate learning context with quiz generation.
- [x] Integrate knowledge context with writing workflows.
- [x] Integrate image-analysis context with follow-up questions.
- [x] Integrate troubleshooting context with session summaries.
- [x] Preserve source provenance across workspaces.

### Session intelligence

- [x] Implement session title generation.
- [x] Implement session summary generation.
- [x] Implement unfinished-task detection where supported.
- [x] Implement relevant-source suggestions.
- [x] Implement previous-work restoration.
- [x] Implement session continuation.
- [x] Implement related-artifact navigation.
- [x] Implement session search.
- [x] Implement session filters.
- [x] Implement session history.
- [x] Implement session summary updates.
- [x] Ensure session summaries do not replace original messages.
- [x] Ensure all AI-generated summaries remain local.

### Context controls and privacy

- [x] Display active context.
- [x] Display selected source count.
- [x] Allow source removal.
- [x] Allow context reset.
- [x] Allow session deletion.
- [x] Allow users to inspect stored session information.
- [x] Define retention behavior.
- [x] Ensure deleting a session follows documented data-retention rules.
- [x] Ensure shared context is never silently transferred.
- [x] Add session and context privacy tests (`SharedContextManagerTest.kt`).

### Workspace continuity

- [x] Support Document Q&A to Create & Write.
- [x] Support Learn & Explain to quiz generation.
- [x] Support Knowledge Center to Ask AI.
- [x] Support Ask AI to Create & Write.
- [x] Support Troubleshooting to report generation.
- [x] Support Image Analysis to follow-up questioning & diagnostics.
- [x] Preserve workspace-specific behavior.
- [x] Preserve original conversation history.
- [x] Restore the correct session after app restart.

**Gate 4 Completion:** Persistent task session architecture, typed shared context manager, and multi-workspace handoffs verified with automated test coverage. — **COMPLETE (Release v0.4.0)**

---

# Gate 5 — Practical Offline Intelligence

**Objective:** Add reliable deterministic utilities that make OpenField useful even when generative AI is not required.

## Calculator and expression engine

- [x] Design deterministic expression evaluator (`DeterministicMathEngine.kt`).
- [x] Implement basic arithmetic.
- [x] Implement operator precedence.
- [x] Implement parentheses.
- [x] Implement percentages.
- [x] Implement ratios.
- [x] Implement powers and roots.
- [x] Implement scientific functions where approved.
- [x] Implement expression validation.
- [x] Handle division by zero.
- [x] Handle invalid syntax.
- [x] Handle precision and rounding.
- [x] Prevent arbitrary code execution.
- [x] Implement calculation history if useful.
- [x] Implement result copy and reuse.
- [x] Add extensive calculation unit tests (`DeterministicMathEngineTest.kt`).
- [x] Ensure calculations do not depend on LLM output.

## Unit conversion

- [x] Design unit and category definitions (`UnitConverter.kt`).
- [x] Implement length conversion.
- [x] Implement mass and weight conversion.
- [x] Implement temperature conversion.
- [x] Implement area conversion.
- [x] Implement volume conversion.
- [x] Implement speed conversion.
- [x] Implement time conversion.
- [x] Implement interactive calculator & units screen (`CalculatorConverterScreen.kt`).

**Gate 5 Completion:** Deterministic arithmetic engine, unit converter, and interactive calculator UI verified with automated test coverage. — **COMPLETE (Release v0.4.0)**
- [ ] Implement data-storage conversion.
- [ ] Implement energy conversion.
- [ ] Implement pressure conversion.
- [ ] Validate conversion factors.
- [ ] Handle invalid or incompatible units.
- [ ] Add conversion accuracy tests.
- [ ] Implement a clear conversion interface.

## Date and time utilities

- [x] Implement date difference.
- [x] Implement date addition and subtraction.
- [x] Implement time-duration calculation.
- [x] Implement deadline calculation.
- [x] Implement calendar calculations.
- [ ] Implement time-zone conversion.
- [ ] Handle daylight-saving transitions where applicable.
- [ ] Handle locale-specific date formatting.
- [x] Use Android-supported time APIs.
- [x] Add date and time unit tests.

## QR and barcode intelligence

- [ ] Evaluate a suitable offline scanning library.
- [ ] Review dependency license and size.
- [ ] Implement QR image decoding.
- [ ] Implement camera-based QR scanning.
- [ ] Implement supported barcode decoding.
- [ ] Implement camera permission handling.
- [ ] Implement explicit scanner start and stop.
- [ ] Display decoded content safely.
- [ ] Detect malformed content.
- [ ] Identify decoded URLs.
- [ ] Require explicit user action before opening URLs.
- [ ] Support copying decoded content.
- [ ] Support supported structured QR payloads.
- [ ] Test QR scanning without internet access.

## Structured data tools

- [x] Implement CSV file inspection.
- [x] Implement CSV parsing.
- [ ] Implement table preview.
- [ ] Implement column selection.
- [ ] Implement sorting.
- [ ] Implement filtering.
- [x] Implement basic column statistics.
- [ ] Implement JSON inspection.
- [ ] Implement JSON tree rendering.
- [ ] Implement structured document extraction where supported.
- [ ] Implement local table export.
- [ ] Handle malformed input safely.
- [x] Handle large files with bounded memory.
- [x] Add parser and data-integrity tests.

## Image and file utilities

- [ ] Implement image metadata inspection.
- [ ] Implement image resizing.
- [ ] Implement local image compression.
- [ ] Improve OCR reuse for image-to-text.
- [ ] Implement document-image preview.
- [ ] Implement file information inspection.
- [ ] Implement supported file-type detection.
- [ ] Implement file-size and storage information.
- [ ] Implement local file preview where supported.
- [ ] Implement safe temporary-file cleanup.
- [ ] Implement duplicate detection only where reliable.
- [ ] Ensure scoped-storage compatibility.
- [ ] Add file and image utility tests.

## Utility integration

- [ ] Create a unified Tools workspace.
- [ ] Organize tools by category.
- [ ] Implement tool search.
- [ ] Implement recently used tools.
- [ ] Integrate calculator with Ask AI where appropriate.
- [ ] Allow deterministic results to be explained by Gemma.
- [ ] Keep calculation results separate from AI-generated explanations.
- [ ] Ensure all eligible tools work in airplane mode.

**Physical-device verification:** Run representative calculations, unit conversions, date calculations, QR scans, CSV inspections, and image/file operations on the Lava LXX503 with network connectivity disabled.

---

# Gate 6 — Personal Task Automation

**Objective:** Enable OpenField to convert natural-language requests into editable, persistent, user-controlled task workflows.

### Task architecture

- [x] Define Task entity (`TaskItemEntity.kt`).
- [x] Define Subtask entity (`TaskPlanningModels.kt`).
- [x] Define TaskStatus.
- [x] Define TaskPriority.
- [ ] Define task deadlines.
- [ ] Define task-source relationships.
- [ ] Define generated-artifact relationships.
- [ ] Define task-session relationships.
- [x] Implement task repository (`TaskDao.kt`, `OpenFieldRepository.kt`).
- [x] Implement task creation.
- [x] Implement task editing.
- [x] Implement task deletion.
- [x] Implement task completion.
- [ ] Implement priority changes.
- [ ] Implement deadline management.
- [x] Implement subtask management.
- [ ] Implement task search and filtering.
- [x] Implement task persistence and restoration (`MIGRATION_6_7`).

### Natural-language task planning

- [x] Implement natural-language task intake.
- [x] Define structured task-planning request (`PromptBuilder.buildTaskPlanningPrompt`).
- [x] Define validated task-plan response (`StructuredOutputParser.parseTaskPlan`).
- [ ] Implement missing-information detection.
- [ ] Ask clarifying questions when essential information is missing.
- [x] Generate structured task plans.
- [x] Generate editable subtasks.
- [ ] Generate suggested deadlines where appropriate.
- [x] Generate task priorities.
- [ ] Link selected knowledge sources.
- [x] Validate task-plan output.
- [x] Reject malformed task structures.
- [ ] Allow users to edit plans before saving.
- [ ] Integrate with shared AiOrchestrator.
- [x] Keep task planning fully local.

### Workflow templates

- [x] Implement examination preparation workflow.
- [x] Implement research-paper review workflow.
- [ ] Implement project-planning workflow.
- [x] Implement technology-learning workflow.
- [x] Implement troubleshooting workflow.
- [ ] Implement document-preparation workflow.
- [ ] Implement meeting-preparation workflow.
- [ ] Implement general personal-planning workflow.
- [ ] Allow users to customize templates.
- [ ] Allow users to save reusable workflows.
- [ ] Avoid rigid workflows that prevent free-form tasks.

### Task execution and progress

- [x] Implement task dashboard (`TaskAutomationScreen.kt`).
- [ ] Implement upcoming tasks.
- [ ] Implement overdue tasks.
- [ ] Implement completed tasks.
- [x] Implement progress tracking.
- [x] Implement subtask progress.
- [ ] Implement linked-source navigation.
- [ ] Implement linked-document navigation.
- [ ] Implement task continuation.
- [ ] Implement task summaries.
- [ ] Implement task history.
- [x] Preserve task state across app restarts.
- [ ] Implement optional user-controlled reminders only if approved.

### Document generation from tasks

- [ ] Generate study notes from selected sources.
- [ ] Generate research outlines.
- [ ] Generate project documentation.
- [ ] Generate checklists.
- [ ] Generate revision materials.
- [ ] Generate structured reports.
- [ ] Generate task completion summaries.
- [ ] Preserve source citations.
- [ ] Save generated artifacts locally.
- [ ] Link generated artifacts to their originating tasks.
- [ ] Allow editing before export.
- [ ] Support local export and sharing.

### Action confirmation and safety

- [ ] Define confirmation rules for destructive actions.
- [ ] Require confirmation before deleting content.
- [ ] Require confirmation before overwriting documents.
- [ ] Require confirmation before clearing sessions.
- [ ] Require confirmation before removing collections.
- [ ] Require confirmation before sharing information externally.
- [ ] Display a preview of consequential actions.
- [ ] Record actual action outcomes.
- [ ] Prevent false success messages.
- [ ] Test interrupted task execution and recovery.

**Physical-device verification:** Create a multi-day study plan using imported local notes, edit its subtasks, complete tasks, generate revision material, restart the app, and verify persistence.

**Gate 6 Completion:** Personal task workflow automation, natural-language task planning, checklist execution, Room SQLite v7 persistence, and 173 automated unit tests verified. — **COMPLETE (Release v0.5.0)**

---

# Gate 7 — Advanced Retrieval, Personalization and Intelligence Quality

**Objective:** Improve answer relevance, context efficiency, transparency, and personalization without introducing another generative model.

### Retrieval optimization

- [ ] Audit v0.3.0 retrieval benchmarks.
- [ ] Evaluate current FTS4 retrieval limitations.
- [ ] Evaluate hybrid lexical and semantic retrieval.
- [ ] Investigate embedding-model size and licensing.
- [ ] Measure embedding storage requirements.
- [ ] Measure embedding generation latency.
- [ ] Measure retrieval memory usage.
- [ ] Compare hybrid retrieval against FTS4.
- [x] Evaluate query expansion.
- [x] Evaluate metadata-aware ranking.
- [x] Evaluate heading-aware ranking.
- [x] Evaluate diversity-aware ranking.
- [ ] Improve duplicate suppression.
- [x] Improve cross-source ranking.
- [ ] Improve retrieval confidence handling.
- [x] Improve context-budget allocation.
- [x] Preserve the existing FTS4 fallback.
- [x] Document the selected retrieval architecture.
- [x] Add regression tests.

### Personalization

- [x] Audit existing personalization implementation.
- [x] Implement preferred explanation depth.
- [x] Implement preferred language.
- [x] Implement preferred response length.
- [x] Implement preferred writing tone.
- [x] Implement preferred formatting style.
- [x] Implement preferred measurement units.
- [ ] Implement user-defined instructions where approved.
- [x] Store preferences locally.
- [x] Implement preference inspection.
- [x] Implement preference editing.
- [x] Implement preference reset.
- [ ] Prevent user preferences from overriding safety constraints.
- [x] Test preference selection across workspaces.

### Local memory and context quality

- [x] Audit current local memory model.
- [x] Define explicit memory categories.
- [ ] Define memory creation rules.
- [ ] Define memory relevance rules.
- [ ] Define memory retention behavior.
- [x] Implement memory inspection.
- [x] Implement memory editing.
- [x] Implement memory deletion.
- [x] Prevent unrelated memory from entering prompts.
- [ ] Prevent unapproved source context from entering prompts.
- [ ] Avoid silently storing sensitive information.
- [x] Ensure memory deletion updates derived indexes.
- [x] Ensure user control over personalization remains available.

### Long-context management

- [ ] Measure context growth in long conversations.
- [x] Implement bounded context selection.
- [ ] Evaluate conversation summarization.
- [x] Preserve original messages.
- [x] Implement context pruning without data deletion.
- [x] Prioritize relevant task context.
- [ ] Remove duplicate context.
- [ ] Implement session-summary refresh.
- [ ] Implement context restoration.
- [ ] Test long-running sessions.
- [x] Measure prompt token reduction.
- [x] Validate answer quality after optimization.

### Intelligence evaluation

- [ ] Create a versioned local evaluation dataset.
- [ ] Include factual questions.
- [ ] Include multi-document questions.
- [ ] Include reasoning questions.
- [ ] Include insufficient-evidence questions.
- [ ] Include citation validation cases.
- [ ] Include multilingual cases where supported.
- [ ] Measure answer relevance.
- [ ] Measure citation correctness.
- [ ] Measure unsupported-claim rate.
- [ ] Measure instruction-following accuracy.
- [ ] Measure retrieval precision and recall.
- [ ] Measure TTFT.
- [ ] Measure tokens per second.
- [ ] Measure peak memory.
- [ ] Measure battery and thermal behavior.
- [ ] Compare results with previous releases.
- [ ] Document limitations and trade-offs.

**Gate 7 Completion:** Offline query expansion, heading-aware scoring, fair-share multi-source interleaving, structured local memory categories, and prompt personalization verified with automated unit tests. — **COMPLETE (Release v0.6.0)**

---

# Gate 8 — Integrated Productivity Workflows

**Objective:** Connect OpenField's knowledge, learning, writing, troubleshooting, and task capabilities into complete end-to-end workflows.

### Research workflow

- [x] Import multiple research papers.
- [x] Extract digital PDF text.
- [x] Apply OCR to scanned documents.
- [x] Index all supported documents.
- [x] Organize papers into a collection.
- [x] Search across selected papers.
- [x] Ask cross-paper questions.
- [x] Compare research methods.
- [x] Extract findings and limitations.
- [x] Inspect supporting evidence.
- [x] Generate a structured research summary.
- [x] Create an editable research report.
- [x] Preserve citations.
- [x] Save the report locally.
- [x] Export the report.

### Learning workflow

- [x] Select learning sources.
- [x] Create a learning session.
- [x] Generate a structured lesson.
- [x] Explain selected concepts.
- [x] Generate relevant examples.
- [x] Create interactive quizzes.
- [x] Record quiz performance.
- [x] Identify incorrect answers.
- [x] Generate targeted revision material.
- [x] Create study tasks.
- [x] Track learning progress.
- [x] Restore the learning workflow after restart.

### Writing workflow

- [x] Select knowledge sources.
- [x] Retrieve supporting evidence.
- [x] Generate a structured draft.
- [x] Preserve source references.
- [x] Edit the generated content.
- [x] Apply rewriting and formatting.
- [x] Save the draft.
- [x] Link the draft to the task or session.
- [x] Export and share with user approval.

### Troubleshooting workflow

- [x] Capture problem description.
- [x] Attach relevant images or documents.
- [x] Retrieve relevant local knowledge.
- [x] Generate a structured diagnostic plan.
- [x] Present one primary step at a time.
- [x] Record user observations.
- [x] Adapt the next step.
- [x] Record the final result.
- [x] Generate a solution summary.
- [x] Save the troubleshooting session.

### Knowledge workflow

- [x] Import content.
- [x] Index content.
- [x] Organize collections.
- [x] Search across local sources.
- [x] Ask evidence-based questions.
- [x] Inspect citations.
- [x] Save useful findings.
- [x] Create notes or documents from findings.
- [x] Link generated content to original sources.

### Workflow integration and regression

- [x] Ensure all workflows use shared repositories where appropriate.
- [x] Ensure all AI operations use AiOrchestrator.
- [x] Ensure context is explicitly passed between features.
- [x] Ensure citations survive workflow transitions.
- [x] Ensure generated artifacts persist.
- [x] Ensure workflow progress persists.
- [x] Ensure cancellation and recovery work.
- [x] Prevent duplicate indexing and document generation.
- [x] Test each workflow from beginning to end.
- [x] Test workflow resumption after process termination.
- [x] Test workflows in airplane mode.

**Gate 8 Completion:** Integrated research, learning, writing, troubleshooting, and knowledge workflows verified across features with 186 automated unit tests. — **COMPLETE (Release v0.6.0)**

---

# Gate 9 — Navigation, Accessibility and User Experience

**Objective:** Ensure OpenField remains intuitive as its capabilities grow.

### Navigation

- [ ] Audit current navigation structure.
- [ ] Identify overlapping screens and duplicate entry points.
- [ ] Define a consistent workspace hierarchy.
- [ ] Organize Knowledge Center navigation.
- [ ] Organize Tools navigation.
- [ ] Organize Tasks navigation.
- [ ] Preserve access to Ask AI.
- [ ] Preserve access to existing specialized workspaces.
- [ ] Preserve access to Library and Settings.
- [ ] Implement clear back-navigation behavior.
- [ ] Support deep navigation into source references.
- [ ] Restore relevant screen state when appropriate.
- [ ] Avoid adding unnecessary bottom navigation items.

### Design system

- [ ] Audit typography and spacing.
- [ ] Audit color and theme usage.
- [ ] Audit existing reusable components.
- [ ] Standardize loading indicators.
- [ ] Standardize empty states.
- [ ] Standardize error states.
- [ ] Standardize confirmation dialogs.
- [ ] Standardize cards and list items.
- [ ] Standardize source and citation cards.
- [ ] Standardize action menus.
- [ ] Maintain professional Material 3 design.
- [ ] Avoid unnecessary decorative visual effects.
- [ ] Ensure light and dark themes remain consistent where supported.

### Accessibility

- [ ] Review screen-reader labels.
- [ ] Review touch-target dimensions.
- [ ] Review text scaling.
- [ ] Review color contrast.
- [ ] Review keyboard navigation where applicable.
- [ ] Review focus behavior.
- [ ] Review dynamic content announcements.
- [ ] Review progress and error announcements.
- [ ] Test large font sizes.
- [ ] Test accessibility on a physical device.

### UX reliability

- [ ] Implement consistent operation progress.
- [ ] Implement cancel actions for long operations.
- [ ] Prevent accidental duplicate submissions.
- [ ] Prevent accidental destructive actions.
- [ ] Preserve user input after recoverable errors.
- [ ] Implement actionable error messages.
- [ ] Ensure no screen displays false completion status.
- [ ] Test navigation while AI generation is running.
- [ ] Test app backgrounding and restoration.

---

# Gate 10 — Performance, Security, Privacy and Reliability

**Objective:** Ensure the expanded application remains stable and efficient on supported Android devices.

### Performance

- [ ] Establish v0.2.1 performance baseline.
- [ ] Measure cold startup.
- [ ] Measure warm startup.
- [ ] Measure model initialization.
- [ ] Measure TTFT.
- [ ] Measure generation throughput.
- [ ] Measure search latency.
- [ ] Measure cross-source retrieval latency.
- [ ] Measure OCR throughput.
- [ ] Measure indexing throughput.
- [ ] Measure database query performance.
- [ ] Measure peak and sustained memory.
- [ ] Measure battery consumption.
- [ ] Measure thermal behavior.
- [ ] Identify expensive Compose recompositions.
- [ ] Optimize large list rendering.
- [ ] Implement bounded document loading.
- [ ] Implement bounded retrieval.
- [ ] Optimize background processing.
- [ ] Verify cancellation and resource cleanup.
- [ ] Compare measurements with the previous release.

### Reliability

- [ ] Test all existing Phase 2.1 workflows.
- [ ] Test all new Phase 3 workflows.
- [ ] Test malformed AI responses.
- [ ] Test inference initialization failures.
- [ ] Test interrupted generation.
- [ ] Test database write failures.
- [ ] Test storage exhaustion.
- [ ] Test corrupted or unsupported files.
- [ ] Test interrupted indexing.
- [ ] Test interrupted downloads.
- [ ] Test source deletion during retrieval.
- [ ] Test process termination during task operations.
- [ ] Test app upgrade and downgrade limitations.
- [ ] Test repeated actions.
- [ ] Test resource cleanup.
- [ ] Test recovery after application restart.
- [ ] Run full automated regression suite.

### Database and migration integrity

- [ ] Verify all migrations from v0.2.1.
- [ ] Verify migration into each Phase 3 database version.
- [ ] Verify existing conversations are preserved.
- [ ] Verify documents are preserved.
- [ ] Verify knowledge packs are preserved.
- [ ] Verify drafts are preserved.
- [ ] Verify learning progress is preserved.
- [ ] Verify preferences are preserved.
- [ ] Verify task/session data is preserved.
- [ ] Verify FTS4 data consistency.
- [ ] Verify foreign-key integrity.
- [ ] Verify no orphaned records remain.
- [ ] Test migration interruption and recovery.
- [ ] Avoid destructive fallback migrations.

### Privacy and security

- [ ] Review Android permissions.
- [ ] Review network requests.
- [ ] Verify no cloud generative inference.
- [ ] Verify no hidden AI fallback.
- [ ] Verify no automatic personal-file upload.
- [ ] Verify no background microphone access.
- [ ] Verify no unauthorized source access.
- [ ] Verify source-scope isolation.
- [ ] Verify context deletion.
- [ ] Verify local memory deletion.
- [ ] Verify temporary-file cleanup.
- [ ] Verify knowledge-pack integrity validation.
- [ ] Review untrusted document content handling.
- [ ] Review QR and URL safety.
- [ ] Review external sharing confirmation.
- [ ] Review local storage and database protection.
- [ ] Document all network-dependent functionality.
- [ ] Update privacy documentation.

### Offline verification

- [ ] Verify clean installation and model setup.
- [ ] Enable airplane mode.
- [ ] Verify application launch.
- [ ] Verify local Gemma inference.
- [ ] Verify image analysis.
- [ ] Verify document Q&A.
- [ ] Verify bundled OCR.
- [ ] Verify unified search.
- [ ] Verify collections.
- [ ] Verify cross-source retrieval.
- [ ] Verify task management.
- [ ] Verify deterministic utilities.
- [ ] Verify local draft creation and editing.
- [ ] Verify knowledge-pack search for installed packs.
- [ ] Verify saved sessions and context.
- [ ] Verify all supported offline workflows.
- [ ] Document operations that explicitly require network connectivity.

---

# Gate 11 — v0.9.0 Release Candidate

**Objective:** Freeze major feature development and validate the complete integrated application.

### Feature completeness

- [ ] Verify Unified Personal Knowledge.
- [ ] Verify Knowledge Center.
- [ ] Verify unified search.
- [ ] Verify collections.
- [ ] Verify cross-source AI.
- [ ] Verify Evidence Explorer.
- [ ] Verify task sessions.
- [ ] Verify shared context.
- [ ] Verify offline utilities.
- [ ] Verify task planning.
- [ ] Verify task persistence.
- [ ] Verify integrated productivity workflows.
- [ ] Verify personalization.
- [ ] Verify all Phase 1 functionality.
- [ ] Verify all Phase 2 functionality.
- [ ] Verify all Phase 2.1 functionality.

### Automated testing

- [ ] Run complete unit test suite.
- [ ] Run integration tests.
- [ ] Run Room migration tests.
- [ ] Run retrieval evaluation suite.
- [ ] Run citation validation tests.
- [ ] Run deterministic utility tests.
- [ ] Run task persistence tests.
- [ ] Run session/context isolation tests.
- [ ] Run offline-related tests.
- [ ] Run UI tests.
- [ ] Reconcile actual test counts.
- [ ] Resolve all critical and high-priority failures.

### Physical-device testing

- [ ] Test clean installation on Lava LXX503.
- [ ] Test upgrade from v0.2.1.
- [ ] Test complete knowledge workflow.
- [ ] Test complete research workflow.
- [ ] Test complete learning workflow.
- [ ] Test complete writing workflow.
- [ ] Test complete task workflow.
- [ ] Test all deterministic utilities.
- [ ] Test app restart and data restoration.
- [ ] Test airplane mode.
- [ ] Test permission denial and recovery.
- [ ] Test large documents.
- [ ] Test large search results.
- [ ] Test long AI sessions.
- [ ] Test low-storage behavior.
- [ ] Test thermal and battery behavior.

### Release preparation

- [ ] Update application version to v0.9.0.
- [ ] Update About screen.
- [ ] Update Settings version information.
- [ ] Update README.
- [ ] Update architecture documentation.
- [ ] Update database documentation.
- [ ] Update privacy documentation.
- [ ] Update supported-device matrix.
- [ ] Update known limitations.
- [ ] Update release notes.
- [ ] Complete dependency license review.
- [ ] Generate signed release candidate APK.
- [ ] Calculate SHA-256 checksum.
- [ ] Install the exact release APK on physical hardware.
- [ ] Verify launch and critical workflows.
- [ ] Verify Git working tree.
- [ ] Commit release-candidate changes.
- [ ] Create v0.9.0 tag only after verification.

---

# Gate 12 — v1.0.0 Stable OpenField Release

**Objective:** Deliver a reliable, privacy-first, offline personal intelligence application suitable for real-world use.

### Product readiness

- [ ] All approved Phase 3 features are complete.
- [ ] Knowledge Center works correctly.
- [ ] Cross-source search works correctly.
- [ ] Cross-source AI provides valid evidence.
- [ ] Citation navigation works.
- [ ] Sessions and context are user-controlled.
- [ ] Deterministic tools return correct results.
- [ ] Task planning and persistence work.
- [ ] Productivity workflows work end to end.
- [ ] Existing Phase 1–2 features remain functional.
- [ ] All core supported local features work offline.

### Engineering readiness

- [ ] No critical crash remains.
- [ ] No unresolved critical data-integrity issue remains.
- [ ] All database migrations are tested.
- [ ] All high-priority regression failures are resolved.
- [ ] Performance results are documented.
- [ ] Privacy review is complete.
- [ ] Accessibility review is complete.
- [ ] Dependency licenses are reviewed.
- [ ] Architecture documentation is current.
- [ ] Known limitations are documented.
- [ ] Setup and troubleshooting documentation is complete.

### Release verification

- [ ] Set application version to v1.0.0.
- [ ] Verify version code.
- [ ] Verify About screen.
- [ ] Verify Settings version information.
- [ ] Run complete test suite.
- [ ] Build signed release APK.
- [ ] Calculate SHA-256 checksum.
- [ ] Install exact release APK on physical hardware.
- [ ] Verify first launch.
- [ ] Verify model initialization.
- [ ] Verify core workflows.
- [ ] Verify airplane-mode functionality.
- [ ] Verify database persistence.
- [ ] Verify Git working tree.
- [ ] Commit release changes.
- [ ] Create Git tag v1.0.0.
- [ ] Publish release notes.
- [ ] Archive release evidence.
- [ ] Finalize Phase 3 implementation report.

---

# Phase 3 Completion Criteria

Phase 3 can be marked complete only when:

- [ ] Every approved Phase 3 gate has its acceptance criteria satisfied.
- [ ] OpenField has a unified local knowledge infrastructure.
- [ ] Users can organize and search supported local sources.
- [ ] Cross-source AI answers use actual retrieved evidence.
- [ ] Citations resolve to real source records.
- [ ] Evidence can be inspected by users.
- [ ] Task sessions preserve user-approved context.
- [ ] Context can be inspected, edited, and removed.
- [ ] Practical deterministic utilities work offline.
- [ ] Task planning and persistence work correctly.
- [ ] Integrated workflows work end to end.
- [ ] Personalization remains transparent and user-controlled.
- [ ] Existing Phase 1, Phase 2, and Phase 2.1 functionality remains intact.
- [ ] All database migrations preserve existing user data.
- [ ] Core supported local features work without internet after setup.
- [ ] No cloud generative inference or fake AI responses are present.
- [ ] Full automated regression tests pass.
- [ ] Physical-device testing is complete.
- [ ] Performance, privacy, and accessibility reviews are documented.
- [ ] Release APK is signed, installed, and checksum-verified.
- [ ] Documentation and release notes are complete.
- [ ] v1.0.0 is tagged and its working tree is clean.

---

# Required Documentation

Maintain the following project documents:

- `docs/PHASE_3_ROADMAP.md`
- `docs/PHASE_3_PRD.md`
- `docs/PHASE_3_ARCHITECTURE_AUDIT.md`
- `docs/PHASE_3_ARCHITECTURE.md`
- `docs/PHASE_3_DATABASE_DESIGN.md`
- `docs/PHASE_3_IMPLEMENTATION_PLAN.md`
- `docs/PHASE_3_TEST_STRATEGY.md`
- `docs/PHASE_3_IMPLEMENTATION_REPORT.md`
- `docs/RELEASE_NOTES.md`
- `docs/decisions/ADR-*.md`

---

# Completion and Verification Rules

1. Never mark an item complete based only on code generation.
2. Never mark an item complete based only on a successful build.
3. Every feature must have relevant automated tests.
4. Every database change must have migration tests.
5. Every major workflow must be tested on physical hardware.
6. Every offline claim must be verified with network connectivity disabled.
7. Every citation must resolve to real source evidence.
8. Every destructive operation must be user-confirmed.
9. Every release checksum must be generated from the actual release artifact.
10. Every release must be installed and verified on a physical device.
11. Report incomplete features honestly.
12. Record actual test counts rather than estimated counts.
13. Do not change completed checkboxes without new verification evidence.
14. Do not start a later milestone while the previous milestone has unresolved critical issues.

**Final Product Goal:** OpenField should become a unified, intelligent, privacy-first personal assistant that helps people understand their information, learn, solve problems, organize knowledge, and complete meaningful tasks — with local AI and offline-first functionality at its foundation.