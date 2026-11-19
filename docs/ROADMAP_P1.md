# OpenField Phase 1 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after evidence exists.

## Gate 0 — Project setup
- [x] Confirm Phase 1 PRD and scope freeze
- [x] Confirm license decision and third-party/model license review
- [x] Create repository and protect `main`
- [x] Add README, CONTRIBUTING, issue/PR templates
- [x] Add CI workflow and build badge
- [x] Create milestone labels and initial issues
- [x] Create docs folder and link approved specs

## Gate 1 — Android foundation
- [x] Android Studio/JDK/SDK setup documented
- [x] Kotlin Compose app builds on clean checkout
- [x] Navigation: Home, Chats, Tools, Library
- [x] Light theme and reusable UI components
- [x] Loading/empty/error/offline states
- [x] Lint and unit tests run in CI

## Gate 2 — Local AI feasibility (critical gate)
- [x] Select model artifact and verify license/source
- [x] Verify checksum, size, storage and memory requirements
- [x] Confirm LiteRT-LM Android API compatibility
- [x] Load and generate on target physical phone
- [x] Record cold start, latency, memory and sustained-session behavior
- [x] Airplane-mode generation works after setup
- [x] Model failure/unsupported-device states are understandable
- [x] Decide minimum supported device policy

## Gate 3 — Local chat
- [x] Room schema and migration baseline
- [x] Chat sessions/messages persist
- [x] Prompt submit and response rendering
- [x] Generation cancellation
- [x] Retry and model-unavailable handling
- [x] History list/detail/delete
- [x] App restart retains conversations
- [x] No prompt content in logs
- [x] Airplane-mode chat test passes

## Gate 4 — Image analysis
- [x] Runtime supports image input for selected artifact
- [x] Image picker and preview
- [x] Image preprocessing and size constraints
- [x] Capability/error states
- [x] Test on physical device
- [x] Clearly communicate uncertainty for identification/safety-related outputs

## Gate 5 — Documents
- [x] SAF import and app-private copy
- [x] PDF/text extraction
- [x] File/page/size limits
- [x] Chunking strategy
- [x] Local search/FTS verified on supported devices
- [x] Retrieval with source/page references
- [x] Document Q&A
- [x] Delete document + chunks + index cleanly
- [x] Scanned PDF/OCR limitation communicated

## Gate 6 — Guided AI and learning
- [x] Problem intake and clarification
- [x] Step-by-step flow
- [x] Capture user outcome and adapt next step
- [x] Stop/unsafe/uncertain handling
- [x] Learn & Explain
- [x] Follow-up context

## Gate 7 — AI utilities and Library
- [x] Summarize
- [x] Explain
- [x] Translate
- [x] Rewrite/improve
- [x] Extract key points/data
- [x] Create/write
- [x] Local pack manifest validation
- [x] Install/remove/import pack
- [x] Library search and status

## Gate 8 — Optional backend
- [x] FastAPI health endpoint
- [x] PostgreSQL configuration and migrations
- [x] Pack metadata schema and API contract
- [x] Pagination/filtering
- [x] Artifact storage and checksum
- [x] Download progress and atomic installation
- [x] Publisher/admin permissions
- [x] Backend unavailable does not break local features

## Gate 9 — Quality and release
- [x] Unit/integration/UI tests
- [x] Fresh install and upgrade/migration tests
- [x] Airplane mode and restart regression
- [x] Privacy/network review
- [x] Accessibility and font scaling
- [x] Performance, memory, battery and thermal checks
- [x] Dependency/model/pack license attribution
- [x] README, setup guide, privacy notice, FAQ, release notes
- [x] Known limitations and supported-device matrix
- [x] Signed release build and checksum
