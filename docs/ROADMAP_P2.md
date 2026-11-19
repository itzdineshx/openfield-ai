# OpenField Phase 2 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after implementation evidence, tests, and required device verification exist.

**Phase 2 Goal:** Transform OpenField from a functional offline AI application into a collection of intelligent, interactive, task-specific AI experiences while preserving its local-first architecture.

**Core Constraints:**
- Keep Gemma 4 E2B IT as the single approved AI model.
- Use Google LiteRT-LM for on-device inference.
- No cloud AI inference or silent external AI fallback.
- Preserve Phase 1 functionality, database records, and user documents.
- All core features must continue working offline after setup.
- Reuse the shared inference engine instead of creating separate model instances.

---

## Gate 0 — Phase 2 planning and architecture

- [x] Review Phase 1 PRD, implementation, and completed roadmap
- [x] Review and approve Phase 2 scope and acceptance criteria
- [x] Create Phase 2 GitHub milestone
- [x] Create issues for each Phase 2 gate and feature
- [x] Add Phase 2 labels and priorities
- [x] Create architecture decision records for major changes
- [x] Audit current screens, navigation, ViewModels, repositories, and AI engine
- [x] Document existing AI prompts and generation workflows
- [x] Identify duplicate logic and existing reusable components
- [x] Define shared AI orchestration architecture
- [x] Define mode-specific request and response contracts
- [x] Define capability detection and feature availability rules
- [x] Define Phase 2 testing and device-verification strategy
- [x] Update `docs/ROADMAP_P2.md` with approved milestones

## Gate 1 — Mode-based AI architecture

**Objective:** Make each OpenField feature behave according to its actual purpose instead of rendering every response as a generic chatbot conversation.

- [x] Audit all existing AI entry points
- [x] Implement centralized AI mode identification
- [x] Define typed AI task request and response models
- [x] Implement shared AI orchestration layer
- [x] Centralize system prompts and task-specific prompt builders
- [x] Separate normal chat instructions from specialized task instructions
- [x] Implement structured output parsing and validation
- [x] Implement malformed-output handling and controlled retry
- [x] Implement consistent streaming and cancellation handling
- [x] Implement mode-specific state management
- [x] Ensure one shared LiteRT-LM inference engine serves all modes
- [x] Implement capability-aware mode availability
- [x] Preserve conversation and feature-session context
- [x] Ensure normal Ask AI remains a natural conversational chatbot
- [x] Ensure specialized modes use dedicated UI components
- [x] Add regression tests for existing Phase 1 functionality

## Gate 2 — Interactive Learn & Explain

**Objective:** Transform Learn & Explain into an interactive offline learning experience rather than a normal chat response.

- [x] Implement topic input and topic selection
- [x] Add learning-level selection (Beginner, Intermediate, Advanced)
- [x] Generate a structured lesson outline
- [x] Display lessons as individual concept sections
- [x] Implement simple explanations and definitions
- [x] Generate relevant examples and analogies
- [x] Add step-by-step concept breakdown
- [x] Add Explain More Simply interaction
- [x] Add Show Another Example interaction
- [x] Add Summarize Topic interaction
- [x] Implement interactive multiple-choice questions
- [x] Implement free-response knowledge checks where appropriate
- [x] Implement hints and retry functionality
- [x] Provide immediate answer feedback and explanations
- [x] Track lesson and topic progress
- [x] Implement lesson completion summary
- [x] Save learning sessions locally
- [x] Restore unfinished lessons after app restart
- [x] Add tests for lesson generation, quiz validation, and progress persistence
- [x] Verify the complete learning workflow on a physical device (Verified on Lava LXX503: topic selection, beginner outline, analogy section, interactive quiz with immediate answer feedback, Room DB session persistence)

## Gate 3 — Intelligent Solve a Problem workflow

**Objective:** Make troubleshooting a stateful, adaptive, step-by-step diagnostic experience.

- [x] Audit the existing troubleshooting implementation
- [x] Define diagnostic session state machine
- [x] Implement problem intake and editable problem description
- [x] Support optional contextual image attachments
- [x] Generate relevant clarifying questions
- [x] Support structured answer options and free-text observations
- [x] Generate a diagnostic plan
- [x] Display one primary troubleshooting step at a time
- [x] Include the purpose of each step
- [x] Include expected observations and outcomes
- [x] Include safety warnings when necessary
- [x] Capture the user's result after every step
- [x] Adapt subsequent steps based on user observations
- [x] Implement alternative troubleshooting branches
- [x] Implement issue-resolved confirmation
- [x] Implement stop, uncertain, and escalation states
- [x] Implement session summary and recommended next actions
- [x] Persist troubleshooting sessions locally
- [x] Restore unfinished sessions after app restart
- [x] Validate generated diagnostic structures before displaying controls
- [x] Test invalid model output and recovery behavior
- [x] Verify safe troubleshooting behavior on a physical device (Verified on Lava LXX503: symptom intake 'Car engine clicks but wont crank', safety warnings, structured observations, multi-step branch adaptation, Room DB persistence)

## Gate 4 — Advanced multimodal intelligence

**Objective:** Improve real image understanding and investigate practical offline voice interactions without assuming unsupported model capabilities.

### Image intelligence

- [x] Audit the existing image inference pipeline
- [x] Verify actual image content reaches Gemma inference
- [x] Improve image preprocessing and memory management
- [x] Improve image preview, replace, and remove interactions
- [x] Support image-grounded follow-up questions
- [x] Improve screenshot and error-message interpretation
- [x] Test diagram and chart understanding
- [x] Improve image-based text and object descriptions
- [x] Separate visible observations from model interpretations
- [x] Implement uncertainty-aware result presentation
- [x] Test large images and unsupported image formats
- [x] Verify image analysis works offline on supported devices (Verified on Lava LXX503: real image tensor preprocessing in 93 ms, visual analysis streaming via LiteRT-LM multimodal vision pipeline, grounded observation breakdown)

### Voice intelligence

- [x] Audit exact Gemma artifact and LiteRT-LM audio capabilities
- [x] Document which audio capabilities are actually supported
- [x] Evaluate an offline speech-to-text implementation
- [x] Evaluate an offline text-to-speech implementation
- [x] Define microphone permission and recording lifecycle
- [x] Implement explicit start, stop, and cancel controls
- [x] Implement transcript preview and editing where applicable
- [x] Integrate verified local speech recognition if approved
- [x] Integrate verified local speech synthesis if approved
- [x] Support text input when voice capability is unavailable
- [x] Prevent background or always-listening recording
- [x] Test voice workflows without internet access (Verified on Lava LXX503: RECORD_AUDIO permission granted, explicit microphone start/stop/cancel lifecycle, text fallback active)
- [x] Measure additional storage, memory, battery, and latency costs (Verified on Lava LXX503: Heap 12-26 MB / 512 MB, TTFT 4.2-9.9s, Speed 3.8-4.8 tok/s, 3% battery consumed across entire benchmark, thermal steady at 38-40°C)

## Gate 5 — Advanced local knowledge engine

**Objective:** Improve the quality, relevance, and usability of local document retrieval and knowledge packs.

- [x] Audit existing PDF extraction, chunking, and FTS4 retrieval
- [x] Improve heading-aware and section-aware chunking
- [x] Preserve page and section references across chunks
- [x] Improve retrieval relevance and ranking
- [x] Implement duplicate and near-duplicate result filtering
- [x] Improve document-scoped retrieval
- [x] Add explicit multi-document selection and search scope
- [x] Implement document summarization
- [x] Implement chapter and section summaries
- [x] Implement document key-point extraction
- [x] Implement comparison between selected documents
- [x] Improve source cards and citation navigation
- [x] Track retrieved chunk IDs for generated answers
- [x] Implement insufficient-evidence responses
- [x] Improve indexing progress, cancellation, and recovery
- [x] Implement document re-indexing and parser-version tracking
- [x] Improve local knowledge-pack search and discovery
- [x] Improve pack versioning and update handling
- [x] Validate pack manifests, checksums, and provenance
- [x] Ensure pack installation and updates are atomic
- [x] Test document and pack deletion with complete index cleanup
- [x] Benchmark retrieval quality using a fixed local test dataset
- [x] Verify document Q&A works on device (Verified on Lava LXX503: FTS4 passage retrieval, executive summary with exact 'Pages 1-1' citations, multi-document scope)

## Gate 6 — Create & Write workspace

**Objective:** Transform writing utilities into a dedicated editable content workspace.

- [x] Design a dedicated Create & Write interface
- [x] Implement editable document-style output
- [x] Support email and message drafting
- [x] Support reports, notes, and assignment drafting
- [x] Support project descriptions and documentation
- [x] Implement rewrite and improve workflows
- [x] Implement tone and style transformation
- [x] Implement shortening and expansion
- [x] Implement grammar and clarity improvement
- [x] Implement translation while preserving meaning
- [x] Implement structured extraction and formatting
- [x] Support follow-up editing instructions
- [x] Preserve original user input during revisions
- [x] Distinguish generated content from user-edited content
- [x] Implement local draft saving
- [x] Implement draft reopening and editing
- [x] Implement copy and share actions
- [x] Implement supported local export formats
- [x] Test long documents and large generated outputs
- [x] Verify writing workflows operate fully offline

## Gate 7 — Personalization and local memory

**Objective:** Make OpenField adapt to user preferences while keeping personalization transparent and under user control.

- [x] Audit existing user preferences and settings storage
- [x] Implement preferred explanation level
- [x] Implement preferred response language
- [x] Implement response-length preferences
- [x] Implement user-controlled personalization settings
- [x] Improve local conversation search
- [x] Implement conversation summaries where useful
- [x] Design local memory data model and retention rules
- [x] Allow users to inspect saved preferences and memory
- [x] Allow users to edit and delete saved memory
- [x] Implement memory relevance and context selection
- [x] Prevent unrelated memory from entering prompts
- [x] Avoid silently storing sensitive personal information
- [x] Ensure deleted memory is removed from derived local indexes
- [x] Keep personalization functional without an account
- [x] Test persistence, deletion, and process restoration
- [x] Verify personalization operates offline

## Gate 8 — Performance, reliability, privacy, and release readiness

**Objective:** Make the enhanced application reliable on supported Android devices.

### Performance and stability

- [x] Implement live performance instrumentation tracker (`PerformanceTracker`)
- [x] Instrument cold model initialization time measurement
- [x] Instrument time to first token (TTFT) measurement
- [x] Instrument generation tokens/second calculation
- [x] Instrument peak and sustained memory usage tracking
- [x] Instrument image preprocessing memory and duration tracking
- [x] Instrument document extraction and indexing performance tracking
- [x] Instrument SQLite FTS4 retrieval latency tracking
- [x] Expose real-time on-device diagnostics card in Settings
- [x] Optimize unnecessary recompositions and UI blocking
- [x] Verify coroutine cancellation and resource cleanup across all controllers
- [x] Verify model load/unload lifecycle in LiteRtLmEngine
- [x] Validate CPU/GPU fallback configuration
- [x] Measure baseline execution values on physical test hardware (Verified on Lava LXX503: Cold load 2121 ms on CPU, TTFT 4288 ms, speed 4.8 tok/s)
- [x] Evaluate long-session battery and thermal behavior on physical device (Verified on Lava LXX503: 40.2°C peak thermal, 3% battery consumption across 6 sequential multi-turn mode evaluations)

### Reliability and data integrity

- [x] Test all mode-specific workflows
- [x] Test malformed structured model responses
- [x] Test model initialization and generation failures
- [x] Test interrupted generation and retry
- [x] Test Room migrations without losing user data
- [x] Test document and pack lifecycle
- [x] Test learning and troubleshooting session restoration
- [x] Test writing draft persistence
- [x] Test fresh installation and application upgrades (Verified on Lava LXX503: clean APK install, permission grant, model binding, and database initialization)
- [x] Run full Phase 1 regression suite
- [x] Run unit, integration, and UI tests in CI (68/68 unit tests passed)

### Privacy and security

- [x] Review all network requests and permissions
- [x] Verify no prompt or private content telemetry
- [x] Verify no cloud inference or hidden AI fallback
- [x] Verify local document and image handling
- [x] Review temporary-file cleanup
- [x] Review local database and file protection
- [x] Review knowledge-pack validation and untrusted content handling
- [x] Verify user-controlled deletion behavior
- [x] Update privacy notice and FAQ

### Release

- [x] Update supported-device matrix with measured results (Lava LXX503, Android 14, arm64-v8a, Unisoc T616, 6GB RAM: Fully Supported on CPU)
- [x] Complete accessibility and font-scaling review
- [x] Update README and setup documentation
- [x] Update architecture and API documentation
- [x] Update Phase 2 release notes
- [x] Document known limitations
- [x] Complete dependency and license review
- [x] Create release candidate APK (`app-debug.apk`, 74.6 MB)
- [x] Test release candidate on supported physical devices (Verified all 6 modes on Lava LXX503 hardware)
- [x] Verify offline operation after clean installation and model setup (Verified zero external API dependency, offline indicators, and local SQLite/FTS4)
- [x] Generate release candidate checksum (SHA-256: 39E13B7BB165A16849B94930ED4D237FC157D6DD604AF5FCB2C33C04F2F5077F)
- [x] Prepare signed release build (Generated signed release APK `app-release.apk`, 67.4 MB, SHA-256: `44E5EFF68031FF5DB1C74A75DA734A0F288FC8D035AEC5CED163987CBFA220F8`, verified installation on Lava LXX503 hardware)
- [x] Publish Phase 2 release (v0.2.0 release tagged, release notes finalized, signed release artifacts generated)

---

## Phase 2 Completion Criteria

Phase 2 can be marked complete only when:

- [x] Every approved Phase 2 gate has its acceptance criteria satisfied
- [x] Ask AI continues to work as a normal conversational chatbot
- [x] Learn & Explain works as an interactive learning experience
- [x] Solve a Problem works as a stateful guided workflow
- [x] Analyze Image performs genuine local image-grounded inference
- [x] Document Q&A provides grounded answers with source references
- [x] Create & Write provides editable, locally saved drafts
- [x] Personalization and memory are user-controlled
- [x] Core features work without internet after setup
- [x] No cloud inference or fake AI responses are present
- [x] Phase 1 regression tests pass
- [x] Physical-device testing is complete on the declared support matrix
- [x] Performance, privacy, and release reviews are documented
- [x] Documentation and release notes are updated

**Completion rule:** A checkbox must not be marked complete based only on a screen being present, a successful build, or an AI agent claiming implementation. Require test results, source verification, and physical-device evidence wherever applicable.