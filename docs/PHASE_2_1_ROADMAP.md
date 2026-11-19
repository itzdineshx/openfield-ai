# OpenField Phase 2.1 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after implementation evidence, tests, and required physical-device verification exist.

**Target Version:** v0.2.1  
**Phase:** Phase 2.1 — Offline Intelligence, Reliability & Integration  
**Platform:** Native Android (Kotlin + Jetpack Compose + Material 3)  
**Baseline Release:** OpenField v0.2.0  
**Primary Test Device:** Lava LXX503 — Android 14, ARM64, Unisoc T616, 6GB RAM

**Phase 2.1 Goal:** Strengthen OpenField's offline-first capabilities by introducing reliable local OCR and speech recognition, completing real knowledge-pack catalog integration, improving inference efficiency, and hardening data integrity, privacy, and release reliability.

---

## Core Constraints

- Keep **Gemma 4 E2B IT** as the single approved generative AI model.
- Continue using Google LiteRT-LM 0.19.1 for on-device generative inference unless an approved architecture decision changes it.
- Do not introduce cloud AI inference or external LLM fallbacks.
- OCR and speech recognition must operate locally after their required assets are installed.
- Preserve all Phase 1 and Phase 2 functionality, database records, conversations, documents, drafts, sessions, and user preferences.
- Reuse the existing process-scoped `AiOrchestrator` and shared LiteRT-LM engine.
- Do not create additional Gemma model instances for individual workspaces.
- Network access is permitted only for explicitly user-initiated operations such as refreshing a knowledge-pack catalog or downloading a selected pack.
- Local AI functionality must not require an account, remote service, or internet connection after setup.
- Do not silently upload documents, images, audio, prompts, conversations, or personal memory.
- Do not introduce simulated downloads, fake progress, placeholder catalog entries, or fabricated benchmark results.
- Do not automatically download large models or assets without user consent.
- Preserve v0.2.0 as a stable rollback baseline.
- Every new dependency must undergo compatibility, maintenance, license, storage, and performance review.

---

## Gate 0 — Phase 2.1 Planning, Baseline Audit & Architecture

**Objective:** Establish a reproducible v0.2.0 baseline, identify incomplete Phase 2 capabilities, and define the architecture for Phase 2.1 before implementation.

### Baseline verification

- [ ] Review the approved Phase 1 PRD v1.1
- [ ] Review the completed Phase 1 implementation and roadmap
- [ ] Review the completed Phase 2 checklist and release documentation
- [ ] Verify the current v0.2.0 Git tag and release commit
- [ ] Record the exact Git commit hash
- [ ] Record the actual signed release APK size
- [ ] Calculate and record the actual SHA-256 checksum of the release APK
- [ ] Record the Android Gradle Plugin, Gradle, Kotlin, JDK, and dependency versions
- [ ] Run the complete existing test suite
- [ ] Record actual unit, integration, and UI test counts and results
- [ ] Resolve discrepancies between previously reported test counts
- [ ] Resolve discrepancies between previously reported APK sizes and checksums
- [ ] Install the v0.2.0 baseline on the physical test device
- [ ] Verify all existing AI workspaces before making changes
- [ ] Verify existing Room database records and migration state
- [ ] Verify existing document and knowledge-pack storage
- [ ] Verify existing model installation and initialization
- [ ] Record baseline cold model initialization time
- [ ] Record baseline time to first token (TTFT)
- [ ] Record baseline generation speed
- [ ] Record baseline memory, battery, and thermal behavior
- [ ] Create a reproducible baseline verification report

### Architecture and implementation audit

- [ ] Audit the existing PDF extraction pipeline
- [ ] Identify the current behavior for scanned and image-only PDFs
- [ ] Audit the existing voice input implementation
- [ ] Identify dependencies on Android `SpeechRecognizer` and manufacturer speech services
- [ ] Audit existing knowledge-pack catalog and download implementation
- [ ] Identify any simulated or incomplete catalog and download behavior
- [ ] Audit the FastAPI knowledge-pack API implementation
- [ ] Review existing prompt builders and inference context management
- [ ] Review `AiOrchestrator` and shared inference lifecycle
- [ ] Review Room database entities, migrations, and FTS4 indexes
- [ ] Identify existing reusable progress, cancellation, and error-handling components
- [ ] Identify storage and cleanup requirements for OCR and speech assets
- [ ] Define Phase 2.1 architecture decision records (ADRs)
- [ ] Define offline capability detection and feature availability rules
- [ ] Create Phase 2.1 GitHub milestone
- [ ] Create issues for every approved gate and feature
- [ ] Add Phase 2.1 labels, priorities, and dependencies
- [ ] Update `docs/ROADMAP_P2_1.md` with approved milestones

**Gate 0 Acceptance Criteria:**

- [ ] v0.2.0 baseline can be reproduced and installed
- [ ] Actual test counts, APK size, and checksum are recorded
- [ ] Existing Phase 2 behavior is documented
- [ ] Known implementation gaps are identified
- [ ] Architecture decisions are documented before implementation
- [ ] No existing user data is modified during baseline auditing

---

## Gate 1 — Fully Offline OCR for Documents

**Objective:** Enable OpenField to extract readable text from scanned and image-only PDFs without depending on online OCR services.

### OCR engine evaluation

- [ ] Evaluate suitable on-device OCR engines
- [ ] Evaluate Google ML Kit Text Recognition and its actual offline model availability
- [ ] Evaluate Tesseract or other suitable local OCR alternatives
- [ ] Verify ARM64 and Android compatibility
- [ ] Verify whether OCR requires Play Services or dynamic model downloads
- [ ] Verify supported languages and scripts
- [ ] Review model and library licensing
- [ ] Measure dependency and model storage requirements
- [ ] Measure CPU and memory requirements
- [ ] Benchmark OCR performance on the Lava LXX503
- [ ] Select the OCR engine based on offline reliability and measured performance
- [ ] Document the selected engine and trade-offs in an ADR

### OCR pipeline implementation

- [ ] Design a dedicated local OCR abstraction
- [ ] Integrate the selected OCR engine
- [ ] Separate OCR processing from the existing PDF text extraction pipeline
- [ ] Detect PDFs containing embedded text
- [ ] Detect scanned or image-only PDF pages
- [ ] Run OCR only on pages that require it
- [ ] Preserve the existing embedded-text extraction path
- [ ] Avoid replacing good embedded text with lower-quality OCR output
- [ ] Support mixed PDFs containing both text and scanned pages
- [ ] Implement bounded page rendering resolution and DPI
- [ ] Implement sequential or memory-bounded page processing
- [ ] Implement page-level processing progress
- [ ] Implement OCR cancellation
- [ ] Implement recoverable OCR error states
- [ ] Handle blank, rotated, low-quality, and unsupported pages
- [ ] Normalize extracted text for indexing
- [ ] Preserve page numbers and document provenance
- [ ] Record OCR engine and parser version
- [ ] Prevent duplicate chunks during repeated OCR indexing
- [ ] Support document re-indexing when OCR configuration changes

### Integration with local knowledge engine

- [ ] Integrate OCR output with existing document chunking
- [ ] Integrate OCR output with Room and SQLite FTS4
- [ ] Preserve page references for OCR-generated chunks
- [ ] Distinguish OCR-derived text from embedded PDF text where useful
- [ ] Include OCR-derived text in document Q&A
- [ ] Include OCR-derived text in document summaries
- [ ] Include OCR-derived text in multi-document retrieval
- [ ] Implement complete OCR chunk cleanup when a document is deleted
- [ ] Ensure OCR failure does not corrupt existing document content
- [ ] Ensure cancellation does not leave incomplete indexes

### OCR testing and device verification

- [ ] Test a normal text-based PDF
- [ ] Test a fully scanned PDF
- [ ] Test a mixed text and scanned PDF
- [ ] Test a multi-page scanned PDF
- [ ] Test a blank PDF page
- [ ] Test rotated and low-resolution scanned pages
- [ ] Test supported and unsupported languages
- [ ] Test OCR cancellation
- [ ] Test OCR failure and retry
- [ ] Test document re-indexing
- [ ] Test document deletion and complete index cleanup
- [ ] Test document Q&A using OCR-derived content
- [ ] Verify OCR works in airplane mode after asset installation
- [ ] Measure processing time per page
- [ ] Measure peak memory usage
- [ ] Measure storage overhead
- [ ] Measure battery and thermal impact
- [ ] Verify the complete OCR workflow on the Lava LXX503

**Gate 1 Acceptance Criteria:**

- [ ] Scanned and image-only PDFs can be indexed locally
- [ ] Mixed PDFs preserve both embedded and OCR-derived text
- [ ] Page-level source references remain available
- [ ] OCR works without internet after required assets are installed
- [ ] OCR cancellation, retry, re-indexing, and deletion work correctly
- [ ] OCR does not introduce duplicate or corrupted document indexes
- [ ] Physical-device performance is measured and documented

---

## Gate 2 — Guaranteed Local Speech Recognition

**Objective:** Replace device-dependent speech recognition behavior with a verified local speech-to-text implementation that works independently of the Android manufacturer's speech services.

### Speech engine evaluation

- [ ] Evaluate Vosk and suitable offline speech recognition alternatives
- [ ] Evaluate Whisper-derived mobile inference runtimes where appropriate
- [ ] Verify Android and ARM64 compatibility
- [ ] Verify supported languages and language models
- [ ] Verify that recognition does not require internet access
- [ ] Review model and runtime licenses
- [ ] Measure model download and storage requirements
- [ ] Measure CPU, RAM, and battery requirements
- [ ] Benchmark recognition latency on the Lava LXX503
- [ ] Evaluate recognition accuracy using a fixed test dataset
- [ ] Select a local speech recognition engine
- [ ] Document the selected engine and trade-offs in an ADR

### Local STT architecture

- [ ] Design a dedicated speech recognition interface
- [ ] Separate STT from Gemma generative inference
- [ ] Implement a local speech recognition adapter
- [ ] Keep Gemma 4 E2B IT as the only approved generative model
- [ ] Prevent speech recognition from creating additional Gemma instances
- [ ] Implement local language model and asset management
- [ ] Support explicit user-initiated model installation
- [ ] Display actual asset installation status
- [ ] Verify downloaded or imported asset checksums
- [ ] Track asset version and compatibility
- [ ] Handle insufficient storage
- [ ] Handle missing or corrupted speech assets
- [ ] Provide a clear unsupported-language state

### Recording and transcription workflow

- [ ] Request microphone permission only when the user initiates recording
- [ ] Implement explicit Start Recording action
- [ ] Implement Stop Recording action
- [ ] Implement Cancel Recording action
- [ ] Implement local speech-to-text processing
- [ ] Display transcription progress where appropriate
- [ ] Display the recognized transcript
- [ ] Allow users to edit the transcript before submission
- [ ] Allow users to discard a transcript
- [ ] Allow users to submit the transcript to Ask AI
- [ ] Support speech input in appropriate specialized workspaces
- [ ] Preserve normal text input as a fallback
- [ ] Implement audio focus and interruption handling
- [ ] Handle microphone permission denial
- [ ] Handle application backgrounding and process interruption
- [ ] Release microphone and audio buffers after use
- [ ] Ensure no audio recording continues after cancellation
- [ ] Prevent background or always-listening microphone behavior
- [ ] Avoid retaining raw audio unless explicitly required and user-controlled

### STT testing and device verification

- [ ] Test short and long speech input
- [ ] Test quiet and noisy environments
- [ ] Test pauses and interrupted speech
- [ ] Test supported language recognition
- [ ] Test microphone permission denial
- [ ] Test recording cancellation
- [ ] Test audio focus interruption
- [ ] Test application process recreation
- [ ] Test missing and corrupted speech assets
- [ ] Test text fallback when STT is unavailable
- [ ] Test recognition in airplane mode
- [ ] Verify recognition does not invoke external speech services
- [ ] Measure recognition latency
- [ ] Measure memory and battery usage
- [ ] Measure model storage requirements
- [ ] Verify the complete STT workflow on the Lava LXX503

**Gate 2 Acceptance Criteria:**

- [ ] Speech transcription works locally after the required assets are installed
- [ ] Recognition does not depend on the manufacturer's online speech provider
- [ ] Users explicitly control recording and submission
- [ ] Transcripts can be reviewed, edited, and discarded
- [ ] Text input remains available when speech recognition is unavailable
- [ ] No background or always-listening recording exists
- [ ] Offline behavior and resource costs are documented

---

## Gate 3 — Real Knowledge-Pack Catalog and Download Integration

**Objective:** Complete the knowledge-pack experience by connecting the Android application to the actual FastAPI catalog and download infrastructure, replacing any simulated catalog or download behavior.

### Backend API audit

- [ ] Audit the current FastAPI knowledge-pack implementation
- [ ] Verify `GET /v1/packs`
- [ ] Verify `POST /v1/downloads/{pack_version_id}/url`
- [ ] Verify pagination behavior
- [ ] Verify category and search filters
- [ ] Verify pack version metadata
- [ ] Verify manifest structure
- [ ] Verify pack file size metadata
- [ ] Verify SHA-256 checksum metadata
- [ ] Verify publisher and source information
- [ ] Verify license information
- [ ] Verify Android and application compatibility metadata
- [ ] Verify download URL expiration behavior
- [ ] Verify authentication requirements, if applicable
- [ ] Verify API error responses
- [ ] Verify rate limiting and retry behavior
- [ ] Verify HTTPS and permitted download hosts
- [ ] Document API contracts and expected responses

### Android catalog integration

- [ ] Define typed catalog request and response models
- [ ] Implement a real catalog API client
- [ ] Implement a catalog repository
- [ ] Connect the existing knowledge-pack UI to the real repository
- [ ] Remove simulated catalog responses
- [ ] Remove fake download progress
- [ ] Remove placeholder success states
- [ ] Implement explicit catalog refresh
- [ ] Implement search and filtering
- [ ] Implement pagination where supported
- [ ] Display actual pack title and description
- [ ] Display actual version and compatibility information
- [ ] Display actual file size
- [ ] Display publisher, source, and license information
- [ ] Display catalog loading state
- [ ] Display empty catalog state
- [ ] Display offline state
- [ ] Display API error state
- [ ] Cache the last successfully retrieved catalog
- [ ] Display the cache's last-updated timestamp
- [ ] Ensure a failed refresh does not erase the last valid catalog

### Secure pack download workflow

- [ ] Implement user-selected pack version downloads
- [ ] Require explicit confirmation before downloading
- [ ] Retrieve real download URLs from the backend
- [ ] Validate HTTPS and permitted download hosts
- [ ] Check available device storage before downloading
- [ ] Display actual download progress
- [ ] Support download cancellation
- [ ] Support retry after recoverable failure
- [ ] Support resumable downloads where supported
- [ ] Validate HTTP response status
- [ ] Validate downloaded file size
- [ ] Validate manifest contents
- [ ] Validate pack compatibility
- [ ] Verify SHA-256 checksum
- [ ] Reject checksum mismatches
- [ ] Reject malformed manifests
- [ ] Prevent path traversal and unsafe archive extraction
- [ ] Never execute downloaded pack content as code
- [ ] Download to temporary storage before installation
- [ ] Activate packs atomically after successful validation
- [ ] Preserve the previously installed pack when an update fails
- [ ] Prevent partially downloaded packs from appearing as installed
- [ ] Update Room metadata transactionally
- [ ] Update FTS4 indexes only after successful installation
- [ ] Implement pack removal and complete index cleanup
- [ ] Implement storage cleanup for abandoned temporary files

### Backend failure and offline behavior

- [ ] Test catalog refresh when the backend is unavailable
- [ ] Test expired download URLs
- [ ] Test interrupted downloads
- [ ] Test checksum mismatch
- [ ] Test insufficient storage
- [ ] Test malformed manifests
- [ ] Test pack installation failure
- [ ] Test pack update failure
- [ ] Test recovery after application process death
- [ ] Verify installed packs remain usable offline
- [ ] Verify Ask AI and other local workspaces work when the backend is unavailable
- [ ] Verify no documents, prompts, images, audio, or memory are transmitted during catalog operations
- [ ] Verify network activity occurs only for the requested operation

**Gate 3 Acceptance Criteria:**

- [ ] Catalog data comes from the actual backend
- [ ] Downloads use real URLs and real file data
- [ ] No simulated progress or fake installation success remains
- [ ] Manifest, compatibility, file size, and checksum validation work
- [ ] Pack installation and updates are atomic
- [ ] Previously installed packs remain safe when an update fails
- [ ] Installed packs remain usable without internet
- [ ] Backend outage does not affect local AI functionality

---

## Gate 4 — Prompt Efficiency and Inference Optimization

**Objective:** Improve response efficiency and resource usage without reducing answer quality, grounding, safety, or the distinct behavior of OpenField's AI workspaces.

### Evaluation baseline

- [ ] Create a fixed local prompt evaluation dataset
- [ ] Include normal Ask AI questions
- [ ] Include Learn & Explain tasks
- [ ] Include troubleshooting tasks
- [ ] Include image analysis tasks
- [ ] Include document Q&A tasks
- [ ] Include Create & Write tasks
- [ ] Include malformed structured output cases
- [ ] Include insufficient-evidence cases
- [ ] Record baseline prompt token estimates
- [ ] Record baseline TTFT
- [ ] Record baseline generation speed
- [ ] Record baseline output quality
- [ ] Record citation accuracy for document Q&A
- [ ] Record structured response validity for specialized modes

### Prompt architecture improvements

- [ ] Audit all system prompts and task-specific prompt builders
- [ ] Identify duplicated instructions
- [ ] Remove unnecessary repeated context
- [ ] Preserve separate instructions for each AI mode
- [ ] Keep Ask AI natural and conversational
- [ ] Avoid forcing JSON output in normal chat
- [ ] Use structured output contracts only where the UI requires them
- [ ] Simplify unnecessary formatting instructions
- [ ] Bound conversation history sent to inference
- [ ] Bound retrieved document context
- [ ] Remove duplicate retrieved passages
- [ ] Avoid including unrelated saved memory
- [ ] Prevent unnecessary repeated document metadata
- [ ] Preserve source references and citation information
- [ ] Preserve safety instructions and uncertainty handling
- [ ] Keep prompt construction centralized
- [ ] Add regression tests for prompt contracts

### Inference efficiency

- [ ] Measure prompt size before and after optimization
- [ ] Review context-window usage
- [ ] Review unnecessary generation tokens
- [ ] Optimize retrieval context selection
- [ ] Review structured output retry behavior
- [ ] Prevent uncontrolled retry loops
- [ ] Ensure cancellation works during generation
- [ ] Verify all modes continue using the shared inference engine
- [ ] Avoid additional model initialization
- [ ] Avoid loading unrelated assets into memory
- [ ] Evaluate improvements on the fixed test dataset
- [ ] Compare response quality against the baseline
- [ ] Compare TTFT against the baseline
- [ ] Compare generation speed against the baseline
- [ ] Compare citation and structured output accuracy
- [ ] Document trade-offs and measured results

**Gate 4 Acceptance Criteria:**

- [ ] Prompt changes are backed by repeatable evaluation
- [ ] Normal Ask AI remains conversational
- [ ] Specialized workspaces preserve their structured behavior
- [ ] No regression in factual quality, grounding, or safety
- [ ] Context and prompt size improvements are measured
- [ ] Performance improvements are reported using actual device measurements
- [ ] No unsupported performance claims are made

---

## Gate 5 — Reliability, Database Integrity & Lifecycle Hardening

**Objective:** Ensure the new OCR, STT, and catalog capabilities do not compromise existing user data, application stability, or the shared inference architecture.

### Database and storage integrity

- [ ] Review whether Room schema changes are necessary
- [ ] Avoid unnecessary database version increments
- [ ] Implement Room migrations only when required
- [ ] Test migrations from the v0.2.0 database
- [ ] Preserve existing conversations and messages
- [ ] Preserve document metadata and chunks
- [ ] Preserve knowledge-pack metadata
- [ ] Preserve learning sessions
- [ ] Preserve troubleshooting sessions
- [ ] Preserve writing drafts
- [ ] Preserve personalization preferences and local memory
- [ ] Verify OCR metadata and indexes do not break existing records
- [ ] Verify speech asset metadata does not break existing records
- [ ] Verify pack catalog updates do not corrupt installed pack metadata
- [ ] Test complete deletion of documents and derived indexes
- [ ] Test complete deletion of installed packs and derived indexes
- [ ] Test cleanup of temporary files and abandoned downloads

### Process lifecycle and recovery

- [ ] Test application process death during OCR
- [ ] Test application process death during STT asset installation
- [ ] Test application process death during pack download
- [ ] Test application process death during pack installation
- [ ] Test recovery after interrupted OCR
- [ ] Test recovery after interrupted downloads
- [ ] Test recovery after incomplete pack installation
- [ ] Test cancellation and retry across all new workflows
- [ ] Verify native resources are released after cancellation
- [ ] Verify audio resources are released after STT
- [ ] Verify image and PDF processing buffers are released
- [ ] Verify no duplicate background jobs are created
- [ ] Verify one process-scoped `AiOrchestrator` is used
- [ ] Verify one shared LiteRT-LM engine is used
- [ ] Verify concurrent workspace requests are handled safely
- [ ] Verify model initialization and shutdown behavior

### Regression testing

- [ ] Test Ask AI normal conversation
- [ ] Test Analyze Image
- [ ] Test Document Q&A
- [ ] Test Solve a Problem
- [ ] Test Learn & Explain
- [ ] Test Create & Write
- [ ] Test AI Tools
- [ ] Test My Library
- [ ] Test existing Phase 1 functionality
- [ ] Test existing Phase 2 functionality
- [ ] Test fresh installation
- [ ] Test upgrade from v0.2.0
- [ ] Test low-storage conditions
- [ ] Test low-memory conditions
- [ ] Test unsupported OCR and STT languages
- [ ] Test corrupted local assets
- [ ] Test unavailable backend
- [ ] Test airplane mode
- [ ] Run unit tests
- [ ] Run integration tests
- [ ] Run UI tests
- [ ] Run regression tests in CI
- [ ] Record actual test results and failures

**Gate 5 Acceptance Criteria:**

- [ ] Existing user data survives the v0.2.0 to v0.2.1 upgrade
- [ ] New workflows recover safely from interruption
- [ ] Cancellation releases resources correctly
- [ ] All existing workspaces pass regression testing
- [ ] No duplicate model instances are introduced
- [ ] No incomplete download or indexing operation is reported as successful

---

## Gate 6 — Privacy, Accessibility, Performance & Release Readiness

**Objective:** Verify that Phase 2.1 is safe, accessible, performant, and ready for a reproducible release.

### Privacy and security

- [ ] Review all new Android permissions
- [ ] Review microphone permission usage
- [ ] Review file and storage access
- [ ] Review OCR asset installation and storage
- [ ] Review STT asset installation and storage
- [ ] Review knowledge-pack download security
- [ ] Verify catalog requests contain no private document content
- [ ] Verify no prompts or conversations are transmitted
- [ ] Verify no images or audio are uploaded
- [ ] Verify no private memory or user preferences are transmitted
- [ ] Verify no cloud inference or hidden AI fallback exists
- [ ] Review temporary audio and image cleanup
- [ ] Review temporary download cleanup
- [ ] Review archive extraction safety
- [ ] Review URL validation and redirect behavior
- [ ] Review downloaded asset provenance
- [ ] Review dependency licenses
- [ ] Review logging for accidental private-content exposure
- [ ] Update privacy notice and FAQ

### Accessibility and usability

- [ ] Review screen reader labels for OCR controls
- [ ] Review screen reader labels for recording controls
- [ ] Review catalog and download progress announcements
- [ ] Verify font scaling across new screens
- [ ] Verify text contrast
- [ ] Verify touch target sizes
- [ ] Verify keyboard navigation
- [ ] Verify loading, empty, offline, and error states
- [ ] Provide clear explanations for unavailable capabilities
- [ ] Display storage requirements before large asset downloads
- [ ] Display supported language information
- [ ] Ensure cancellation and retry actions are discoverable
- [ ] Ensure new screens follow the existing professional OpenField design system

### Performance and device verification

- [ ] Measure OCR performance on the Lava LXX503
- [ ] Measure STT performance on the Lava LXX503
- [ ] Measure catalog refresh latency
- [ ] Measure pack download and installation performance
- [ ] Measure memory use during OCR
- [ ] Measure memory use during STT
- [ ] Measure memory use during pack installation
- [ ] Measure battery consumption during long OCR tasks
- [ ] Measure battery consumption during long STT tasks
- [ ] Measure sustained thermal behavior
- [ ] Measure inference performance after prompt optimization
- [ ] Test repeated multi-workspace usage
- [ ] Test extended application sessions
- [ ] Verify no persistent memory growth or resource leak
- [ ] Update supported-device matrix with measured results
- [ ] Document device limitations and known performance trade-offs

### Release preparation

- [ ] Update README and setup documentation
- [ ] Update architecture documentation
- [ ] Update database documentation if schema changes occur
- [ ] Update API integration documentation
- [ ] Document OCR setup and supported languages
- [ ] Document STT setup and supported languages
- [ ] Document knowledge-pack catalog behavior
- [ ] Document offline and online capability boundaries
- [ ] Update privacy notice and FAQ
- [ ] Update known limitations
- [ ] Update Phase 2.1 release notes
- [ ] Run the complete test suite
- [ ] Verify the signed release build
- [ ] Verify the release APK installs successfully
- [ ] Verify upgrade from v0.2.0
- [ ] Verify all existing data is preserved
- [ ] Verify OCR works offline after setup
- [ ] Verify STT works offline after setup
- [ ] Verify installed knowledge packs work offline
- [ ] Verify backend failure does not break local AI
- [ ] Verify all existing workspaces on physical hardware
- [ ] Generate the final release APK
- [ ] Calculate the final APK SHA-256 checksum
- [ ] Verify the checksum against the release artifact
- [ ] Archive the test report and device verification evidence
- [ ] Tag the release as v0.2.1
- [ ] Publish Phase 2.1 release notes and artifacts

**Gate 6 Acceptance Criteria:**

- [ ] Privacy and security review is complete
- [ ] Accessibility review is complete
- [ ] Performance measurements are documented
- [ ] Physical-device testing is complete
- [ ] Upgrade and offline tests pass
- [ ] Signed release APK and checksum are verified
- [ ] Documentation and release notes are complete

---

## Phase 2.1 Completion Criteria

Phase 2.1 can be marked complete only when:

- [ ] The v0.2.0 baseline has been audited and recorded
- [ ] Scanned and image-only PDFs can be processed using local OCR
- [ ] OCR-derived content is searchable through the existing local knowledge engine
- [ ] OCR page references remain available in document Q&A
- [ ] OCR cancellation, retry, re-indexing, and deletion work correctly
- [ ] Local speech recognition works independently of the Android manufacturer's online speech provider
- [ ] Speech transcription works offline after required assets are installed
- [ ] Users explicitly control microphone recording and transcript submission
- [ ] Text input remains available when speech recognition is unavailable
- [ ] The knowledge-pack catalog uses the real backend API
- [ ] Pack downloads use real files and actual download progress
- [ ] Manifest, compatibility, and checksum validation work
- [ ] Pack installation and updates are atomic
- [ ] Installed knowledge packs remain usable offline
- [ ] Backend failure does not affect local AI functionality
- [ ] Prompt and inference improvements are supported by measured evaluation results
- [ ] Ask AI remains a natural conversational chatbot
- [ ] All specialized AI workspaces preserve their intended behavior
- [ ] The shared inference architecture remains intact
- [ ] No cloud AI inference or silent external fallback is introduced
- [ ] Existing Phase 1 and Phase 2 functionality passes regression testing
- [ ] Existing user data survives the v0.2.0 to v0.2.1 upgrade
- [ ] Privacy, security, accessibility, and dependency reviews are complete
- [ ] Physical-device and airplane-mode verification is complete
- [ ] Documentation and release notes are updated
- [ ] The signed v0.2.1 release artifact and SHA-256 checksum are verified

---

## Out of Scope for Phase 2.1

The following features must not be introduced as part of this release unless separately approved:

- Cloud LLM inference
- Secondary generative AI models
- External AI API fallbacks
- Mandatory accounts or authentication for local functionality
- Cloud conversation synchronization
- Remote user memory storage
- Automatic upload of documents, images, or audio
- Always-listening or background microphone recording
- Community knowledge-pack marketplace, ratings, and social features
- GPS tracking, SOS, emergency mode, or trip planning
- Replacing Gemma 4 E2B IT without an approved architecture decision
- Replacing LiteRT-LM without an approved architecture decision
- Unverified OCR or STT engines
- Simulated catalog entries, downloads, or benchmark results

---

## Implementation Priority

Recommended implementation order:

1. **Gate 0 — Baseline Audit:** Establish the actual v0.2.0 state before modifying the application.
2. **Gate 1 — Offline OCR:** Complete scanned-document intelligence and integrate it with existing RAG.
3. **Gate 2 — Offline STT:** Make speech input independent of manufacturer-provided speech services.
4. **Gate 3 — Knowledge-Pack Integration:** Replace simulated catalog and download behavior with the real backend.
5. **Gate 4 — Inference Optimization:** Improve prompt efficiency using a fixed evaluation dataset.
6. **Gate 5 — Reliability:** Verify data integrity, process recovery, and regression behavior.
7. **Gate 6 — Release:** Complete security, accessibility, physical-device testing, and v0.2.1 release verification.

**Recommended first implementation target:** Complete Gate 0 and select the OCR engine before beginning Gate 1. Do not begin integrating OCR, STT, and catalog downloads simultaneously without first establishing their dependency, storage, and lifecycle requirements.

---

## Completion and Evidence Rules

**Completion rule:** A checkbox must not be marked complete based only on a screen being present, a successful build, or an AI coding agent claiming implementation.

Every completed item must have appropriate evidence:

- **Implementation evidence:** Relevant source files, architecture, and actual behavior.
- **Test evidence:** Automated test results or reproducible manual test steps.
- **Device evidence:** Physical-device verification wherever the item affects hardware, offline behavior, performance, permissions, or native resources.
- **Documentation evidence:** Updated technical documentation, known limitations, and release records where applicable.

**GitHub workflow:**

- Each gate should have its own GitHub issue or parent issue.
- Individual checklist items should be represented by actionable issues or sub-issues where useful.
- Link pull requests to the corresponding issues.
- Attach test results and physical-device evidence to the relevant issues.
- Close issues only after their acceptance criteria are satisfied.
- Keep GitHub Projects as the authoritative implementation status.
- Do not mark an entire gate complete while required child issues remain open.

**Final release rule:** OpenField v0.2.1 must remain a genuinely offline-first application. Network access for knowledge-pack discovery and downloads must remain explicit and optional. All core local AI experiences must continue to work without internet after their required models and assets are installed.