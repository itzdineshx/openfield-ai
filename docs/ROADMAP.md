# OpenField Phase 1 Developer Checklist

Use GitHub Issues/Projects as the source of truth. Check items only after evidence exists.

## Gate 0 — Project setup
- [ ] Confirm Phase 1 PRD and scope freeze
- [ ] Confirm license decision and third-party/model license review
- [ ] Create repository and protect `main`
- [ ] Add README, CONTRIBUTING, issue/PR templates
- [ ] Add CI workflow and build badge
- [ ] Create milestone labels and initial issues
- [ ] Create docs folder and link approved specs

## Gate 1 — Android foundation
- [ ] Android Studio/JDK/SDK setup documented
- [ ] Kotlin Compose app builds on clean checkout
- [ ] Navigation: Home, Chats, Tools, Library
- [ ] Light theme and reusable UI components
- [ ] Loading/empty/error/offline states
- [ ] Lint and unit tests run in CI

## Gate 2 — Local AI feasibility (critical gate)
- [ ] Select model artifact and verify license/source
- [ ] Verify checksum, size, storage and memory requirements
- [ ] Confirm LiteRT-LM Android API compatibility
- [ ] Load and generate on target physical phone
- [ ] Record cold start, latency, memory and sustained-session behavior
- [ ] Airplane-mode generation works after setup
- [ ] Model failure/unsupported-device states are understandable
- [ ] Decide minimum supported device policy

## Gate 3 — Local chat
- [ ] Room schema and migration baseline
- [ ] Chat sessions/messages persist
- [ ] Prompt submit and response rendering
- [ ] Generation cancellation
- [ ] Retry and model-unavailable handling
- [ ] History list/detail/delete
- [ ] App restart retains conversations
- [ ] No prompt content in logs
- [ ] Airplane-mode chat test passes

## Gate 4 — Image analysis
- [ ] Runtime supports image input for selected artifact
- [ ] Image picker and preview
- [ ] Image preprocessing and size constraints
- [ ] Capability/error states
- [ ] Test on physical device
- [ ] Clearly communicate uncertainty for identification/safety-related outputs

## Gate 5 — Documents
- [ ] SAF import and app-private copy
- [ ] PDF/text extraction
- [ ] File/page/size limits
- [ ] Chunking strategy
- [ ] Local search/FTS verified on supported devices
- [ ] Retrieval with source/page references
- [ ] Document Q&A
- [ ] Delete document + chunks + index cleanly
- [ ] Scanned PDF/OCR limitation communicated

## Gate 6 — Guided AI and learning
- [ ] Problem intake and clarification
- [ ] Step-by-step flow
- [ ] Capture user outcome and adapt next step
- [ ] Stop/unsafe/uncertain handling
- [ ] Learn & Explain
- [ ] Follow-up context

## Gate 7 — AI utilities and Library
- [ ] Summarize
- [ ] Explain
- [ ] Translate
- [ ] Rewrite/improve
- [ ] Extract key points/data
- [ ] Create/write
- [ ] Local pack manifest validation
- [ ] Install/remove/import pack
- [ ] Library search and status

## Gate 8 — Optional backend
- [ ] FastAPI health endpoint
- [ ] PostgreSQL configuration and migrations
- [ ] Pack metadata schema and API contract
- [ ] Pagination/filtering
- [ ] Artifact storage and checksum
- [ ] Download progress and atomic installation
- [ ] Publisher/admin permissions
- [ ] Backend unavailable does not break local features

## Gate 9 — Quality and release
- [ ] Unit/integration/UI tests
- [ ] Fresh install and upgrade/migration tests
- [ ] Airplane mode and restart regression
- [ ] Privacy/network review
- [ ] Accessibility and font scaling
- [ ] Performance, memory, battery and thermal checks
- [ ] Dependency/model/pack license attribution
- [ ] README, setup guide, privacy notice, FAQ, release notes
- [ ] Known limitations and supported-device matrix
- [ ] Signed release build and checksum
