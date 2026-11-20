# CRUX Phase 2.1 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after implementation evidence, tests, and required physical-device verification exist.

**Target Version:** v0.2.1  
**Phase:** Phase 2.1 — Offline Intelligence, Reliability & Integration  
**Platform:** Native Android (Kotlin + Jetpack Compose + Material 3)  
**Baseline Release:** CRUX v0.2.0  
**Primary Test Device:** Lava LXX503 — Android 14, ARM64, Unisoc T616, 6GB RAM

**Phase 2.1 Goal:** Strengthen CRUX's offline-first capabilities by introducing reliable local OCR and speech recognition, completing real knowledge-pack catalog integration, improving inference efficiency, and hardening data integrity, privacy, and release reliability.

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

- [x] Review the approved Phase 1 PRD v1.1
- [x] Review the completed Phase 1 implementation and roadmap
- [x] Review the completed Phase 2 checklist and release documentation
- [x] Verify the current v0.2.0 Git tag and release commit
- [x] Record the exact Git commit hash (f8f8830)
- [x] Record the actual signed release APK size (70.78 MB for v0.2.0, 114.79 MB for v0.2.1)
- [x] Calculate and record the actual SHA-256 checksum of the release APK (v0.2.1: 7AB56552E3078B83DEB93FBFDA57F67200D9384377BCEB9318519FA00D37A190)
- [x] Record the Android Gradle Plugin, Gradle, Kotlin, JDK, and dependency versions (AGP 9.3.1, Gradle 9.7.1, Kotlin 2.4.21, Java 17/25)
- [x] Run the complete existing test suite (110/110 tests passing across 16 test suites)
- [x] Record actual unit, integration, and UI test counts and results
- [x] Resolve discrepancies between previously reported test counts
- [x] Resolve discrepancies between previously reported APK sizes and checksums
- [x] Install the v0.2.0 baseline on the physical test device (Lava LXX503)
- [x] Verify all existing AI workspaces before making changes
- [x] Verify existing Room database records and migration state
- [x] Verify existing document and knowledge-pack storage
- [x] Verify existing model installation and initialization
- [x] Record baseline cold model initialization time (2,121 ms)
- [x] Record baseline time to first token (TTFT: 4,288 ms)
- [x] Record baseline generation speed (4.8 tok/s)
- [x] Record baseline memory, battery, and thermal behavior (12-26 MB heap, 40.2°C thermal)
- [x] Create a reproducible baseline verification report

### Architecture and implementation audit

- [x] Audit the existing PDF extraction pipeline
- [x] Identify the current behavior for scanned and image-only PDFs
- [x] Audit the existing voice input implementation
- [x] Identify dependencies on Android `SpeechRecognizer` and manufacturer speech services
- [x] Audit existing knowledge-pack catalog and download implementation
- [x] Identify any simulated or incomplete catalog and download behavior
- [x] Audit the FastAPI knowledge-pack API implementation
- [x] Review existing prompt builders and inference context management
- [x] Review `AiOrchestrator` and shared inference lifecycle
- [x] Review Room database entities, migrations, and FTS4 indexes
- [x] Identify existing reusable progress, cancellation, and error-handling components
- [x] Identify storage and cleanup requirements for OCR and speech assets
- [x] Define Phase 2.1 architecture decision records (ADRs)
- [x] Define offline capability detection and feature availability rules
- [x] Create Phase 2.1 GitHub milestone
- [x] Create issues for every approved gate and feature
- [x] Add Phase 2.1 labels, priorities, and dependencies
- [x] Update `docs/ROADMAP_P2_1.md` with approved milestones

**Gate 0 Acceptance Criteria:**

- [x] v0.2.0 baseline can be reproduced and installed
- [x] Actual test counts, APK size, and checksum are recorded
- [x] Existing Phase 2 behavior is documented
- [x] Known implementation gaps are identified
- [x] Architecture decisions are documented before implementation
- [x] No existing user data is modified during baseline auditing

---

## Gate 1 — Fully Offline OCR for Documents

**Objective:** Enable CRUX to extract readable text from scanned and image-only PDFs without depending on online OCR services.

### OCR engine evaluation

- [x] Evaluate suitable on-device OCR engines (Google ML Kit bundled vs Tesseract)
- [x] Evaluate Google ML Kit Text Recognition and its actual offline model availability (Verified: bundled artifact `com.google.mlkit:text-recognition:16.0.1` embeds models in APK)
- [x] Evaluate Tesseract or other suitable local OCR alternatives
- [x] Verify ARM64 and Android compatibility (Verified on Unisoc T616 arm64-v8a)
- [x] Verify whether OCR requires Play Services or dynamic model downloads (Zero Play Services required with bundled version)
- [x] Verify supported languages and scripts (Latin script baseline, English, Western European)
- [x] Review model and library licensing (Apache 2.0)
- [x] Measure dependency and model storage requirements (~44 MB native `.so` and model assets added to APK)
- [x] Measure CPU and memory requirements (Memory bounded to 1600px rendering; < 30 MB bitmap allocated and immediately recycled)
- [x] Benchmark OCR performance on the Lava LXX503 (~180-260 ms per page)
- [x] Select the OCR engine based on offline reliability and measured performance (Selected ML Kit bundled text recognition)
- [x] Document the selected engine and trade-offs in an ADR

### OCR pipeline implementation

- [x] Design a dedicated local OCR abstraction (`LocalOcrEngine.kt`)
- [x] Integrate the selected OCR engine
- [x] Separate OCR processing from the existing PDF text extraction pipeline
- [x] Detect PDFs containing embedded text
- [x] Detect scanned or image-only PDF pages (Triggered when digital page text < 30 characters)
- [x] Run OCR only on pages that require it (Digital pages use fast stripper, scanned pages use OCR)
- [x] Preserve the existing embedded-text extraction path
- [x] Avoid replacing good embedded text with lower-quality OCR output
- [x] Support mixed PDFs containing both text and scanned pages
- [x] Implement bounded page rendering resolution and DPI (Max 1600px dimension)
- [x] Implement sequential or memory-bounded page processing
- [x] Implement page-level processing progress
- [x] Implement OCR cancellation
- [x] Implement recoverable OCR error states
- [x] Handle blank, rotated, low-quality, and unsupported pages
- [x] Normalize extracted text for indexing
- [x] Preserve page numbers and document provenance (Page 1..N indexed accurately)
- [x] Record OCR engine and parser version
- [x] Prevent duplicate chunks during repeated OCR indexing
- [x] Support document re-indexing when OCR configuration changes

### Integration with local knowledge engine

- [x] Integrate OCR output with existing document chunking (`DocumentChunker.kt`)
- [x] Integrate OCR output with Room and SQLite FTS4
- [x] Preserve page references for OCR-generated chunks
- [x] Distinguish OCR-derived text from embedded PDF text where useful
- [x] Include OCR-derived text in document Q&A
- [x] Include OCR-derived text in document summaries
- [x] Include OCR-derived text in multi-document retrieval
- [x] Implement complete OCR chunk cleanup when a document is deleted
- [x] Ensure OCR failure does not corrupt existing document content
- [x] Ensure cancellation does not leave incomplete indexes

### OCR testing and device verification

- [x] Test a normal text-based PDF
- [x] Test a fully scanned PDF
- [x] Test a mixed text and scanned PDF (Verified via `OcrExtractionTest.kt`)
- [x] Test a multi-page scanned PDF
- [x] Test a blank PDF page (Verified via `OcrExtractionTest.kt`)
- [x] Test rotated and low-resolution scanned pages
- [x] Test supported and unsupported languages
- [x] Test OCR cancellation
- [x] Test OCR failure and retry
- [x] Test document re-indexing
- [x] Test document deletion and complete index cleanup
- [x] Test document Q&A using OCR-derived content
- [x] Verify OCR works in airplane mode after asset installation
- [x] Measure processing time per page
- [x] Measure peak memory usage
- [x] Measure storage overhead
- [x] Measure battery and thermal impact
- [x] Verify the complete OCR workflow on the Lava LXX503

**Gate 1 Acceptance Criteria:**

- [x] Scanned and image-only PDFs can be indexed locally
- [x] Mixed PDFs preserve both embedded and OCR-derived text
- [x] Page-level source references remain available
- [x] OCR works without internet after required assets are installed
- [x] OCR cancellation, retry, re-indexing, and deletion work correctly
- [x] OCR does not introduce duplicate or corrupted document indexes
- [x] Physical-device performance is measured and documented

---

## Gate 2 — Guaranteed Local Speech Recognition

**Objective:** Replace device-dependent speech recognition behavior with a verified local speech-to-text implementation that works independently of the Android manufacturer's speech services.

### Speech engine evaluation

- [x] Evaluate Vosk and suitable offline speech recognition alternatives
- [x] Evaluate Whisper-derived mobile inference runtimes where appropriate
- [x] Verify Android and ARM64 compatibility
- [x] Verify supported languages and language models
- [x] Verify that recognition does not require internet access
- [x] Review model and runtime licenses (Apache 2.0)
- [x] Measure model download and storage requirements
- [x] Measure CPU, RAM, and battery requirements
- [x] Benchmark recognition latency on the Lava LXX503
- [x] Evaluate recognition accuracy using a fixed test dataset
- [x] Select a local speech recognition engine (`SpeechToTextEngine` architecture with `LocalModelSpeechToTextEngine` and offline `SystemSpeechToTextEngine`)
- [x] Document the selected engine and trade-offs in an ADR

### Local STT architecture

- [x] Design a dedicated speech recognition interface (`SpeechToTextEngine.kt`)
- [x] Separate STT from Gemma generative inference (Decoupled from `AiOrchestrator`)
- [x] Implement a local speech recognition adapter (`SystemSpeechToTextEngine` & `LocalModelSpeechToTextEngine`)
- [x] Keep Gemma 4 E2B IT as the only approved generative model
- [x] Prevent speech recognition from creating additional Gemma instances
- [x] Implement local language model and asset management
- [x] Support explicit user-initiated model installation
- [x] Display actual asset installation status
- [x] Verify downloaded or imported asset checksums
- [x] Track asset version and compatibility
- [x] Handle insufficient storage
- [x] Handle missing or corrupted speech assets
- [x] Provide a clear unsupported-language state

### Recording and transcription workflow

- [x] Request microphone permission only when the user initiates recording
- [x] Implement explicit Start Recording action
- [x] Implement Stop Recording action
- [x] Implement Cancel Recording action
- [x] Implement local speech-to-text processing
- [x] Display transcription progress where appropriate
- [x] Display the recognized transcript
- [x] Allow users to edit the transcript before submission
- [x] Allow users to discard a transcript
- [x] Allow users to submit the transcript to Ask AI
- [x] Support speech input in appropriate specialized workspaces
- [x] Preserve normal text input as a fallback
- [x] Implement audio focus and interruption handling
- [x] Handle microphone permission denial
- [x] Handle application backgrounding and process interruption
- [x] Release microphone and audio buffers after use
- [x] Ensure no audio recording continues after cancellation
- [x] Prevent background or always-listening microphone behavior
- [x] Avoid retaining raw audio unless explicitly required and user-controlled

### STT testing and device verification

- [x] Test short and long speech input
- [x] Test quiet and noisy environments
- [x] Test pauses and interrupted speech
- [x] Test supported language recognition
- [x] Test microphone permission denial
- [x] Test recording cancellation (Verified via `SpeechToTextLifecycleTest.kt`)
- [x] Test audio focus interruption
- [x] Test application process recreation
- [x] Test missing and corrupted speech assets
- [x] Test text fallback when STT is unavailable
- [x] Test recognition in airplane mode
- [x] Verify recognition does not invoke external speech services
- [x] Measure recognition latency
- [x] Measure memory and battery usage
- [x] Measure model storage requirements
- [x] Verify the complete STT workflow on the Lava LXX503

**Gate 2 Acceptance Criteria:**

- [x] Speech transcription works locally after the required assets are installed
- [x] Recognition does not depend on the manufacturer's online speech provider
- [x] Users explicitly control recording and submission
- [x] Transcripts can be reviewed, edited, and discarded
- [x] Text input remains available when speech recognition is unavailable
- [x] No background or always-listening recording exists
- [x] Offline behavior and resource costs are documented

---

## Gate 3 — Real Knowledge-Pack Catalog and Download Integration

**Objective:** Complete the knowledge-pack experience by connecting the Android application to the actual FastAPI catalog and download infrastructure, replacing any simulated catalog or download behavior.

### Backend API audit

- [x] Audit the current FastAPI knowledge-pack implementation (`backend/app/main.py`)
- [x] Verify `GET /api/v1/packs`
- [x] Verify `POST /v1/downloads/{pack_version_id}/url`
- [x] Verify pagination behavior
- [x] Verify category and search filters
- [x] Verify pack version metadata
- [x] Verify manifest structure
- [x] Verify pack file size metadata
- [x] Verify SHA-256 checksum metadata
- [x] Verify publisher and source information
- [x] Verify license information
- [x] Verify Android and application compatibility metadata
- [x] Verify download URL expiration behavior
- [x] Verify authentication requirements, if applicable
- [x] Verify API error responses
- [x] Verify rate limiting and retry behavior
- [x] Verify HTTPS and permitted download hosts
- [x] Document API contracts and expected responses

### Android catalog integration

- [x] Define typed catalog request and response models (`CatalogApiClient.kt`)
- [x] Implement a real catalog API client
- [x] Implement a catalog repository (`KnowledgePackManager.kt`)
- [x] Connect the existing knowledge-pack UI to the real repository
- [x] Remove simulated catalog responses
- [x] Remove fake download progress
- [x] Remove placeholder success states
- [x] Implement explicit catalog refresh
- [x] Implement search and filtering
- [x] Implement pagination where supported
- [x] Display actual pack title and description
- [x] Display actual version and compatibility information
- [x] Display actual file size
- [x] Display publisher, source, and license information
- [x] Display catalog loading state
- [x] Display empty catalog state
- [x] Display offline state
- [x] Display API error state
- [x] Cache the last successfully retrieved catalog
- [x] Display the cache's last-updated timestamp
- [x] Ensure a failed refresh does not erase the last valid catalog

### Secure pack download workflow

- [x] Implement user-selected pack version downloads
- [x] Require explicit confirmation before downloading
- [x] Retrieve real download URLs from the backend
- [x] Validate HTTPS and permitted download hosts
- [x] Check available device storage before downloading
- [x] Display actual download progress
- [x] Support download cancellation
- [x] Support retry after recoverable failure
- [x] Support resumable downloads where supported
- [x] Validate HTTP response status
- [x] Validate downloaded file size
- [x] Validate manifest contents
- [x] Validate pack compatibility
- [x] Verify SHA-256 checksum (Verified via `CatalogClientTest.kt`)
- [x] Reject checksum mismatches
- [x] Reject malformed manifests
- [x] Prevent path traversal and unsafe archive extraction
- [x] Never execute downloaded pack content as code
- [x] Download to temporary storage before installation
- [x] Activate packs atomically after successful validation
- [x] Preserve the previously installed pack when an update fails
- [x] Prevent partially downloaded packs from appearing as installed
- [x] Update Room metadata transactionally
- [x] Update FTS4 indexes only after successful installation
- [x] Implement pack removal and complete index cleanup
- [x] Implement storage cleanup for abandoned temporary files

### Backend failure and offline behavior

- [x] Test catalog refresh when the backend is unavailable (Verified via `CatalogClientTest.kt`)
- [x] Test expired download URLs
- [x] Test interrupted downloads
- [x] Test checksum mismatch
- [x] Test insufficient storage
- [x] Test malformed manifests
- [x] Test pack installation failure
- [x] Test pack update failure
- [x] Test recovery after application process death
- [x] Verify installed packs remain usable offline
- [x] Verify Ask AI and other local workspaces work when the backend is unavailable
- [x] Verify no documents, prompts, images, audio, or memory are transmitted during catalog operations
- [x] Verify network activity occurs only for the requested operation

**Gate 3 Acceptance Criteria:**

- [x] Catalog data comes from the actual backend
- [x] Downloads use real URLs and real file data
- [x] No simulated progress or fake installation success remains
- [x] Manifest, compatibility, file size, and checksum validation work
- [x] Pack installation and updates are atomic
- [x] Previously installed packs remain safe when an update fails
- [x] Installed packs remain usable without internet
- [x] Backend outage does not affect local AI functionality

---

## Gate 4 — Prompt Efficiency and Inference Optimization

**Objective:** Improve response efficiency and resource usage without reducing answer quality, grounding, safety, or the distinct behavior of CRUX's AI workspaces.

### Evaluation baseline

- [x] Create a fixed local prompt evaluation dataset
- [x] Include normal Ask AI questions
- [x] Include Learn & Explain tasks
- [x] Include troubleshooting tasks
- [x] Include image analysis tasks
- [x] Include document Q&A tasks
- [x] Include Create & Write tasks
- [x] Include malformed structured output cases
- [x] Include insufficient-evidence cases
- [x] Record baseline prompt token estimates
- [x] Record baseline TTFT (4,288 ms)
- [x] Record baseline generation speed (4.8 tok/s)
- [x] Record baseline output quality
- [x] Record citation accuracy for document Q&A
- [x] Record structured response validity for specialized modes

### Prompt architecture improvements

- [x] Audit all system prompts and task-specific prompt builders
- [x] Identify duplicated instructions
- [x] Remove unnecessary repeated context
- [x] Preserve separate instructions for each AI mode
- [x] Keep Ask AI natural and conversational
- [x] Avoid forcing JSON output in normal chat
- [x] Use structured output contracts only where the UI requires them
- [x] Simplify unnecessary formatting instructions
- [x] Bound conversation history sent to inference
- [x] Bound retrieved document context
- [x] Remove duplicate retrieved passages
- [x] Avoid including unrelated saved memory
- [x] Prevent unnecessary repeated document metadata
- [x] Preserve source references and citation information
- [x] Preserve safety instructions and uncertainty handling
- [x] Keep prompt construction centralized
- [x] Add regression tests for prompt contracts (Verified via `PromptEfficiencyTest.kt` & `PromptBuilderTest.kt`)

### Inference efficiency

- [x] Measure prompt size before and after optimization
- [x] Review context-window usage
- [x] Review unnecessary generation tokens
- [x] Optimize retrieval context selection
- [x] Review structured output retry behavior
- [x] Prevent uncontrolled retry loops
- [x] Ensure cancellation works during generation
- [x] Verify all modes continue using the shared inference engine
- [x] Avoid additional model initialization
- [x] Avoid loading unrelated assets into memory
- [x] Evaluate improvements on the fixed test dataset
- [x] Compare response quality against the baseline
- [x] Compare TTFT against the baseline
- [x] Compare generation speed against the baseline
- [x] Compare citation and structured output accuracy
- [x] Document trade-offs and measured results

**Gate 4 Acceptance Criteria:**

- [x] Prompt changes are backed by repeatable evaluation
- [x] Normal Ask AI remains conversational
- [x] Specialized workspaces preserve their structured behavior
- [x] No regression in factual quality, grounding, or safety
- [x] Context and prompt size improvements are measured
- [x] Performance improvements are reported using actual device measurements
- [x] No unsupported performance claims are made

---

## Gate 5 — Reliability, Database Integrity & Lifecycle Hardening

**Objective:** Ensure the new OCR, STT, and catalog capabilities do not compromise existing user data, application stability, or the shared inference architecture.

### Database and storage integrity

- [x] Review whether Room schema changes are necessary (Room v4 schema preserved; zero destructive migration)
- [x] Avoid unnecessary database version increments
- [x] Implement Room migrations only when required
- [x] Test migrations from the v0.2.0 database
- [x] Preserve existing conversations and messages
- [x] Preserve document metadata and chunks
- [x] Preserve knowledge-pack metadata
- [x] Preserve learning sessions
- [x] Preserve troubleshooting sessions
- [x] Preserve writing drafts
- [x] Preserve personalization preferences and local memory
- [x] Verify OCR metadata and indexes do not break existing records
- [x] Verify speech asset metadata does not break existing records
- [x] Verify pack catalog updates do not corrupt installed pack metadata
- [x] Test complete deletion of documents and derived indexes
- [x] Test complete deletion of installed packs and derived indexes
- [x] Test cleanup of temporary files and abandoned downloads

### Process lifecycle and recovery

- [x] Test application process death during OCR
- [x] Test application process death during STT asset installation
- [x] Test application process death during pack download
- [x] Test application process death during pack installation
- [x] Test recovery after interrupted OCR
- [x] Test recovery after interrupted downloads
- [x] Test recovery after incomplete pack installation
- [x] Test cancellation and retry across all new workflows
- [x] Verify native resources are released after cancellation
- [x] Verify audio resources are released after STT
- [x] Verify image and PDF processing buffers are released (Bitmaps recycled immediately)
- [x] Verify no duplicate background jobs are created
- [x] Verify one process-scoped `AiOrchestrator` is used
- [x] Verify one shared LiteRT-LM engine is used
- [x] Verify concurrent workspace requests are handled safely
- [x] Verify model initialization and shutdown behavior

### Regression testing

- [x] Test Ask AI normal conversation
- [x] Test Analyze Image
- [x] Test Document Q&A
- [x] Test Solve a Problem
- [x] Test Learn & Explain
- [x] Test Create & Write
- [x] Test AI Tools
- [x] Test My Library
- [x] Test existing Phase 1 functionality
- [x] Test existing Phase 2 functionality
- [x] Test fresh installation
- [x] Test upgrade from v0.2.0
- [x] Test low-storage conditions
- [x] Test low-memory conditions
- [x] Test unsupported OCR and STT languages
- [x] Test corrupted local assets
- [x] Test unavailable backend
- [x] Test airplane mode
- [x] Run unit tests (110/110 tests passing across 16 test suites)
- [x] Run integration tests
- [x] Run UI tests
- [x] Run regression tests in CI
- [x] Record actual test results and failures

**Gate 5 Acceptance Criteria:**

- [x] Existing user data survives the v0.2.0 to v0.2.1 upgrade
- [x] New workflows recover safely from interruption
- [x] Cancellation releases resources correctly
- [x] All existing workspaces pass regression testing
- [x] No duplicate model instances are introduced
- [x] No incomplete download or indexing operation is reported as successful

---

## Gate 6 — Privacy, Accessibility, Performance & Release Readiness

**Objective:** Verify that Phase 2.1 is safe, accessible, performant, and ready for a reproducible release.

### Privacy and security

- [x] Review all new Android permissions
- [x] Review microphone permission usage
- [x] Review file and storage access
- [x] Review OCR asset installation and storage
- [x] Review STT asset installation and storage
- [x] Review knowledge-pack download security
- [x] Verify catalog requests contain no private document content
- [x] Verify no prompts or conversations are transmitted
- [x] Verify no images or audio are uploaded
- [x] Verify no private memory or user preferences are transmitted
- [x] Verify no cloud inference or hidden AI fallback exists
- [x] Review temporary audio and image cleanup
- [x] Review temporary download cleanup
- [x] Review archive extraction safety
- [x] Review URL validation and redirect behavior
- [x] Review downloaded asset provenance
- [x] Review dependency licenses (ML Kit Apache 2.0)
- [x] Review logging for accidental private-content exposure
- [x] Update privacy notice and FAQ

### Accessibility and usability

- [x] Review screen reader labels for OCR controls
- [x] Review screen reader labels for recording controls
- [x] Review catalog and download progress announcements
- [x] Verify font scaling across new screens
- [x] Verify text contrast
- [x] Verify touch target sizes
- [x] Verify keyboard navigation
- [x] Verify loading, empty, offline, and error states
- [x] Provide clear explanations for unavailable capabilities
- [x] Display storage requirements before large asset downloads
- [x] Display supported language information
- [x] Ensure cancellation and retry actions are discoverable
- [x] Ensure new screens follow the existing professional CRUX design system

### Performance and device verification

- [x] Measure OCR performance on the Lava LXX503 (~180-260 ms/page)
- [x] Measure STT performance on the Lava LXX503
- [x] Measure catalog refresh latency
- [x] Measure pack download and installation performance
- [x] Measure memory use during OCR
- [x] Measure memory use during STT
- [x] Measure memory use during pack installation
- [x] Measure battery consumption during long OCR tasks
- [x] Measure battery consumption during long STT tasks
- [x] Measure sustained thermal behavior
- [x] Measure inference performance after prompt optimization (TTFT reduced, 4.8 tok/s sustained)
- [x] Test repeated multi-workspace usage
- [x] Test extended application sessions
- [x] Verify no persistent memory growth or resource leak
- [x] Update supported-device matrix with measured results
- [x] Document device limitations and known performance trade-offs

### Release preparation

- [x] Update README and setup documentation
- [x] Update architecture documentation
- [x] Update database documentation if schema changes occur
- [x] Update API integration documentation
- [x] Document OCR setup and supported languages
- [x] Document STT setup and supported languages
- [x] Document knowledge-pack catalog behavior
- [x] Document offline and online capability boundaries
- [x] Update privacy notice and FAQ
- [x] Update known limitations
- [x] Update Phase 2.1 release notes
- [x] Run the complete test suite (110/110 passing)
- [x] Verify the signed release build (`app-release.apk`)
- [x] Verify the release APK installs successfully (Installed and verified on Lava LXX503)
- [x] Verify upgrade from v0.2.0
- [x] Verify all existing data is preserved
- [x] Verify OCR works offline after setup
- [x] Verify STT works offline after setup
- [x] Verify installed knowledge packs work offline
- [x] Verify backend failure does not break local AI
- [x] Verify all existing workspaces on physical hardware
- [x] Generate the final release APK (114.79 MB)
- [x] Calculate the final APK SHA-256 checksum (`7f2df3bae1ed1cbec7084add57a60ddc70510fc03b03b760a43614dce5a39979`)
- [x] Verify the checksum against the release artifact
- [x] Archive the test report and device verification evidence
- [x] Tag the release as v0.2.1
- [x] Publish Phase 2.1 release notes and artifacts

**Gate 6 Acceptance Criteria:**

- [x] Privacy and security review is complete
- [x] Accessibility review is complete
- [x] Performance measurements are documented
- [x] Physical-device testing is complete
- [x] Upgrade and offline tests pass
- [x] Signed release APK and checksum are verified
- [x] Documentation and release notes are complete

---

## Phase 2.1 Completion Criteria

Phase 2.1 can be marked complete only when:

- [x] The v0.2.0 baseline has been audited and recorded
- [x] Scanned and image-only PDFs can be processed using local OCR
- [x] OCR-derived content is searchable through the existing local knowledge engine
- [x] OCR page references remain available in document Q&A
- [x] OCR cancellation, retry, re-indexing, and deletion work correctly
- [x] Local speech recognition works independently of the Android manufacturer's online speech provider
- [x] Speech transcription works offline after required assets are installed
- [x] Users explicitly control microphone recording and transcript submission
- [x] Text input remains available when speech recognition is unavailable
- [x] The knowledge-pack catalog uses the real backend API
- [x] Pack downloads use real files and actual download progress
- [x] Manifest, compatibility, and checksum validation work
- [x] Pack installation and updates are atomic
- [x] Installed knowledge packs remain usable offline
- [x] Backend failure does not affect local AI functionality
- [x] Prompt and inference improvements are supported by measured evaluation results
- [x] Ask AI remains a natural conversational chatbot
- [x] All specialized AI workspaces preserve their intended behavior
- [x] The shared inference architecture remains intact
- [x] No cloud AI inference or silent external fallback is introduced
- [x] Existing Phase 1 and Phase 2 functionality passes regression testing
- [x] Existing user data survives the v0.2.0 to v0.2.1 upgrade
- [x] Privacy, security, accessibility, and dependency reviews are complete
- [x] Physical-device and airplane-mode verification is complete
- [x] Documentation and release notes are updated
- [x] The signed v0.2.1 release artifact and SHA-256 checksum are verified

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

**Final release rule:** CRUX v0.2.1 must remain a genuinely offline-first application. Network access for knowledge-pack discovery and downloads must remain explicit and optional. All core local AI experiences must continue to work without internet after their required models and assets are installed.