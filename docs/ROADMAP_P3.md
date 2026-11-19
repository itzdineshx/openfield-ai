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

- [ ] Audit current document-scoped retrieval.
- [ ] Audit FTS4 ranking and BM25 behavior.
- [ ] Define cross-source retrieval request contracts.
- [ ] Define source-scope configuration.
- [ ] Implement a unified retrieval coordinator.
- [ ] Support all-source retrieval.
- [ ] Support collection-scoped retrieval.
- [ ] Support manually selected sources.
- [ ] Support individual source exclusion.
- [ ] Resolve source IDs before retrieval.
- [ ] Validate source availability.
- [ ] Retrieve relevant passages.
- [ ] Rank results across different source types.
- [ ] Deduplicate retrieved passages.
- [ ] Preserve source identity.
- [ ] Preserve page references.
- [ ] Preserve section references.
- [ ] Preserve conversation message references where applicable.
- [ ] Implement context-budget management.
- [ ] Implement retrieval cancellation.
- [ ] Implement retrieval failure handling.
- [ ] Prevent excluded sources from entering the prompt.
- [ ] Ensure only retrieved evidence enters grounded answer context.

### Cross-source question answering

- [ ] Implement cross-source question input.
- [ ] Implement source-scope selector.
- [ ] Implement selected-source display.
- [ ] Implement collection-based question answering.
- [ ] Implement all-knowledge question answering.
- [ ] Integrate with shared AiOrchestrator.
- [ ] Reuse Gemma 4 E2B IT.
- [ ] Preserve streaming responses.
- [ ] Implement grounded answer instructions.
- [ ] Support multi-source comparisons.
- [ ] Support contradiction identification.
- [ ] Support source-based summaries.
- [ ] Support evidence-based extraction.
- [ ] Support follow-up questions within the selected scope.
- [ ] Implement insufficient-evidence responses.
- [ ] Prevent unsupported claims from being presented as facts.
- [ ] Ensure unrelated conversation context does not contaminate retrieval.

### Citation and evidence integrity

- [ ] Define a common citation data model.
- [ ] Preserve original source IDs.
- [ ] Preserve document page references.
- [ ] Preserve section references.
- [ ] Preserve source type.
- [ ] Preserve chunk or passage IDs.
- [ ] Validate citations against retrieved evidence.
- [ ] Reject fabricated source references.
- [ ] Reject invalid page numbers.
- [ ] Implement source navigation.
- [ ] Implement page navigation where supported.
- [ ] Implement citation previews.
- [ ] Implement source cards.
- [ ] Support multiple citations per answer.
- [ ] Handle sources without page information.
- [ ] Handle deleted or unavailable sources.
- [ ] Implement citation regression tests.

### Evidence Explorer

- [ ] Design Evidence Explorer interface.
- [ ] Display sources used in an answer.
- [ ] Display retrieved excerpts.
- [ ] Display page and section references.
- [ ] Display relevance metadata where meaningful.
- [ ] Allow users to open original sources.
- [ ] Allow users to inspect individual passages.
- [ ] Allow users to exclude a source.
- [ ] Allow users to repeat retrieval with updated scope.
- [ ] Ensure evidence inspection does not generate fake excerpts.

### Retrieval evaluation

- [ ] Create a fixed local cross-source evaluation dataset.
- [ ] Include questions with one relevant source.
- [ ] Include questions requiring multiple sources.
- [ ] Include questions with conflicting information.
- [ ] Include questions with insufficient evidence.
- [ ] Include irrelevant-source exclusion cases.
- [ ] Measure retrieval precision.
- [ ] Measure retrieval recall.
- [ ] Measure ranking quality.
- [ ] Measure citation accuracy.
- [ ] Measure unsupported-answer rate.
- [ ] Compare results against the v0.2.1 retrieval baseline.
- [ ] Document retrieval limitations.

**Physical-device verification:** Import multiple real documents, create a collection, ask questions across selected sources, inspect citations, navigate to the original evidence, and verify that excluded sources are not used.

---

# Gate 4 — Context-Aware Personal Assistant

**Objective:** Transform OpenField from isolated workspaces into a persistent task-oriented assistant that can continue user-approved work across different features.

### Task-session architecture

- [ ] Audit existing conversation and feature-session persistence.
- [ ] Define TaskSession data model.
- [ ] Define task-session lifecycle.
- [ ] Define session status.
- [ ] Define session context representation.
- [ ] Define source attachments.
- [ ] Define generated artifact relationships.
- [ ] Define session summary storage.
- [ ] Define session deletion behavior.
- [ ] Implement session repository.
- [ ] Implement session creation.
- [ ] Implement session renaming.
- [ ] Implement session listing.
- [ ] Implement session deletion.
- [ ] Implement session restoration.
- [ ] Implement session persistence after process termination.

### Shared context management

- [ ] Define a typed shared-context contract.
- [ ] Implement context source references.
- [ ] Implement context attachment.
- [ ] Implement context removal.
- [ ] Implement context inspection.
- [ ] Implement context clearing.
- [ ] Implement context selection for each workspace.
- [ ] Prevent automatic inclusion of unrelated sessions.
- [ ] Prevent hidden context reuse.
- [ ] Integrate document context with Ask AI.
- [ ] Integrate document context with Create & Write.
- [ ] Integrate learning context with quiz generation.
- [ ] Integrate knowledge context with writing workflows.
- [ ] Integrate image-analysis context with follow-up questions.
- [ ] Integrate troubleshooting context with session summaries.
- [ ] Preserve source provenance across workspaces.

### Session intelligence

- [ ] Implement session title generation.
- [ ] Implement session summary generation.
- [ ] Implement unfinished-task detection where supported.
- [ ] Implement relevant-source suggestions.
- [ ] Implement previous-work restoration.
- [ ] Implement session continuation.
- [ ] Implement related-artifact navigation.
- [ ] Implement session search.
- [ ] Implement session filters.
- [ ] Implement session history.
- [ ] Implement session summary updates.
- [ ] Ensure session summaries do not replace original messages.
- [ ] Ensure all AI-generated summaries remain local.

### Context controls and privacy

- [ ] Display active context.
- [ ] Display selected source count.
- [ ] Allow source removal.
- [ ] Allow context reset.
- [ ] Allow session deletion.
- [ ] Allow users to inspect stored session information.
- [ ] Define retention behavior.
- [ ] Ensure deleting a session follows documented data-retention rules.
- [ ] Ensure shared context is never silently transferred.
- [ ] Add session and context privacy tests.

### Workspace continuity

- [ ] Support Document Q&A to Create & Write.
- [ ] Support Learn & Explain to quiz generation.
- [ ] Support Knowledge Center to Ask AI.
- [ ] Support Ask AI to Create & Write.
- [ ] Support Troubleshooting to report generation.
- [ ] Support Image Analysis to follow-up questioning.
- [ ] Preserve workspace-specific behavior.
- [ ] Preserve original conversation history.
- [ ] Restore the correct session after app restart.

**Physical-device verification:** Start a document-research task, move between Q&A and writing, save the result, terminate the app, reopen it, and verify that the correct task context is restored without unrelated content.

---

# Gate 5 — Practical Offline Intelligence

**Objective:** Add reliable deterministic utilities that make OpenField useful even when generative AI is not required.

## Calculator and expression engine

- [ ] Design deterministic expression evaluator.
- [ ] Implement basic arithmetic.
- [ ] Implement operator precedence.
- [ ] Implement parentheses.
- [ ] Implement percentages.
- [ ] Implement ratios.
- [ ] Implement powers and roots.
- [ ] Implement scientific functions where approved.
- [ ] Implement expression validation.
- [ ] Handle division by zero.
- [ ] Handle invalid syntax.
- [ ] Handle precision and rounding.
- [ ] Prevent arbitrary code execution.
- [ ] Implement calculation history if useful.
- [ ] Implement result copy and reuse.
- [ ] Add extensive calculation unit tests.
- [ ] Ensure calculations do not depend on LLM output.

## Unit conversion

- [ ] Design unit and category definitions.
- [ ] Implement length conversion.
- [ ] Implement mass and weight conversion.
- [ ] Implement temperature conversion.
- [ ] Implement area conversion.
- [ ] Implement volume conversion.
- [ ] Implement speed conversion.
- [ ] Implement time conversion.
- [ ] Implement data-storage conversion.
- [ ] Implement energy conversion.
- [ ] Implement pressure conversion.
- [ ] Validate conversion factors.
- [ ] Handle invalid or incompatible units.
- [ ] Add conversion accuracy tests.
- [ ] Implement a clear conversion interface.

## Date and time utilities

- [ ] Implement date difference.
- [ ] Implement date addition and subtraction.
- [ ] Implement time-duration calculation.
- [ ] Implement deadline calculation.
- [ ] Implement calendar calculations.
- [ ] Implement time-zone conversion.
- [ ] Handle daylight-saving transitions where applicable.
- [ ] Handle locale-specific date formatting.
- [ ] Use Android-supported time APIs.
- [ ] Add date and time unit tests.

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

- [ ] Implement CSV file inspection.
- [ ] Implement CSV parsing.
- [ ] Implement table preview.
- [ ] Implement column selection.
- [ ] Implement sorting.
- [ ] Implement filtering.
- [ ] Implement basic column statistics.
- [ ] Implement JSON inspection.
- [ ] Implement JSON tree rendering.
- [ ] Implement structured document extraction where supported.
- [ ] Implement local table export.
- [ ] Handle malformed input safely.
- [ ] Handle large files with bounded memory.
- [ ] Add parser and data-integrity tests.

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

- [ ] Define Task entity.
- [ ] Define Subtask entity.
- [ ] Define TaskStatus.
- [ ] Define TaskPriority.
- [ ] Define task deadlines.
- [ ] Define task-source relationships.
- [ ] Define generated-artifact relationships.
- [ ] Define task-session relationships.
- [ ] Implement task repository.
- [ ] Implement task creation.
- [ ] Implement task editing.
- [ ] Implement task deletion.
- [ ] Implement task completion.
- [ ] Implement priority changes.
- [ ] Implement deadline management.
- [ ] Implement subtask management.
- [ ] Implement task search and filtering.
- [ ] Implement task persistence and restoration.

### Natural-language task planning

- [ ] Implement natural-language task intake.
- [ ] Define structured task-planning request.
- [ ] Define validated task-plan response.
- [ ] Implement missing-information detection.
- [ ] Ask clarifying questions when essential information is missing.
- [ ] Generate structured task plans.
- [ ] Generate editable subtasks.
- [ ] Generate suggested deadlines where appropriate.
- [ ] Generate task priorities.
- [ ] Link selected knowledge sources.
- [ ] Validate task-plan output.
- [ ] Reject malformed task structures.
- [ ] Allow users to edit plans before saving.
- [ ] Integrate with shared AiOrchestrator.
- [ ] Keep task planning fully local.

### Workflow templates

- [ ] Implement examination preparation workflow.
- [ ] Implement research-paper review workflow.
- [ ] Implement project-planning workflow.
- [ ] Implement technology-learning workflow.
- [ ] Implement troubleshooting workflow.
- [ ] Implement document-preparation workflow.
- [ ] Implement meeting-preparation workflow.
- [ ] Implement general personal-planning workflow.
- [ ] Allow users to customize templates.
- [ ] Allow users to save reusable workflows.
- [ ] Avoid rigid workflows that prevent free-form tasks.

### Task execution and progress

- [ ] Implement task dashboard.
- [ ] Implement upcoming tasks.
- [ ] Implement overdue tasks.
- [ ] Implement completed tasks.
- [ ] Implement progress tracking.
- [ ] Implement subtask progress.
- [ ] Implement linked-source navigation.
- [ ] Implement linked-document navigation.
- [ ] Implement task continuation.
- [ ] Implement task summaries.
- [ ] Implement task history.
- [ ] Preserve task state across app restarts.
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
- [ ] Evaluate query expansion.
- [ ] Evaluate metadata-aware ranking.
- [ ] Evaluate heading-aware ranking.
- [ ] Evaluate diversity-aware ranking.
- [ ] Improve duplicate suppression.
- [ ] Improve cross-source ranking.
- [ ] Improve retrieval confidence handling.
- [ ] Improve context-budget allocation.
- [ ] Preserve the existing FTS4 fallback.
- [ ] Document the selected retrieval architecture.
- [ ] Add regression tests.

### Personalization

- [ ] Audit existing personalization implementation.
- [ ] Implement preferred explanation depth.
- [ ] Implement preferred language.
- [ ] Implement preferred response length.
- [ ] Implement preferred writing tone.
- [ ] Implement preferred formatting style.
- [ ] Implement preferred measurement units.
- [ ] Implement user-defined instructions where approved.
- [ ] Store preferences locally.
- [ ] Implement preference inspection.
- [ ] Implement preference editing.
- [ ] Implement preference reset.
- [ ] Prevent user preferences from overriding safety constraints.
- [ ] Test preference selection across workspaces.

### Local memory and context quality

- [ ] Audit current local memory model.
- [ ] Define explicit memory categories.
- [ ] Define memory creation rules.
- [ ] Define memory relevance rules.
- [ ] Define memory retention behavior.
- [ ] Implement memory inspection.
- [ ] Implement memory editing.
- [ ] Implement memory deletion.
- [ ] Prevent unrelated memory from entering prompts.
- [ ] Prevent unapproved source context from entering prompts.
- [ ] Avoid silently storing sensitive information.
- [ ] Ensure memory deletion updates derived indexes.
- [ ] Ensure user control over personalization remains available.

### Long-context management

- [ ] Measure context growth in long conversations.
- [ ] Implement bounded context selection.
- [ ] Evaluate conversation summarization.
- [ ] Preserve original messages.
- [ ] Implement context pruning without data deletion.
- [ ] Prioritize relevant task context.
- [ ] Remove duplicate context.
- [ ] Implement session-summary refresh.
- [ ] Implement context restoration.
- [ ] Test long-running sessions.
- [ ] Measure prompt token reduction.
- [ ] Validate answer quality after optimization.

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

---

# Gate 8 — Integrated Productivity Workflows

**Objective:** Connect OpenField's knowledge, learning, writing, troubleshooting, and task capabilities into complete end-to-end workflows.

### Research workflow

- [ ] Import multiple research papers.
- [ ] Extract digital PDF text.
- [ ] Apply OCR to scanned documents.
- [ ] Index all supported documents.
- [ ] Organize papers into a collection.
- [ ] Search across selected papers.
- [ ] Ask cross-paper questions.
- [ ] Compare research methods.
- [ ] Extract findings and limitations.
- [ ] Inspect supporting evidence.
- [ ] Generate a structured research summary.
- [ ] Create an editable research report.
- [ ] Preserve citations.
- [ ] Save the report locally.
- [ ] Export the report.

### Learning workflow

- [ ] Select learning sources.
- [ ] Create a learning session.
- [ ] Generate a structured lesson.
- [ ] Explain selected concepts.
- [ ] Generate relevant examples.
- [ ] Create interactive quizzes.
- [ ] Record quiz performance.
- [ ] Identify incorrect answers.
- [ ] Generate targeted revision material.
- [ ] Create study tasks.
- [ ] Track learning progress.
- [ ] Restore the learning workflow after restart.

### Writing workflow

- [ ] Select knowledge sources.
- [ ] Retrieve supporting evidence.
- [ ] Generate a structured draft.
- [ ] Preserve source references.
- [ ] Edit the generated content.
- [ ] Apply rewriting and formatting.
- [ ] Save the draft.
- [ ] Link the draft to the task or session.
- [ ] Export and share with user approval.

### Troubleshooting workflow

- [ ] Capture problem description.
- [ ] Attach relevant images or documents.
- [ ] Retrieve relevant local knowledge.
- [ ] Generate a structured diagnostic plan.
- [ ] Present one primary step at a time.
- [ ] Record user observations.
- [ ] Adapt the next step.
- [ ] Record the final result.
- [ ] Generate a solution summary.
- [ ] Save the troubleshooting session.

### Knowledge workflow

- [ ] Import content.
- [ ] Index content.
- [ ] Organize collections.
- [ ] Search across local sources.
- [ ] Ask evidence-based questions.
- [ ] Inspect citations.
- [ ] Save useful findings.
- [ ] Create notes or documents from findings.
- [ ] Link generated content to original sources.

### Workflow integration and regression

- [ ] Ensure all workflows use shared repositories where appropriate.
- [ ] Ensure all AI operations use AiOrchestrator.
- [ ] Ensure context is explicitly passed between features.
- [ ] Ensure citations survive workflow transitions.
- [ ] Ensure generated artifacts persist.
- [ ] Ensure workflow progress persists.
- [ ] Ensure cancellation and recovery work.
- [ ] Prevent duplicate indexing and document generation.
- [ ] Test each workflow from beginning to end.
- [ ] Test workflow resumption after process termination.
- [ ] Test workflows in airplane mode.

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