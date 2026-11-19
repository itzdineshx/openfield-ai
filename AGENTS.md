# OpenField Project Constitution & AI Agent Context

> **Purpose:** This file is the primary operating manual for any AI coding agent working on OpenField. Read it before inspecting, planning, editing, testing, or reviewing code.
>
> **Authority:** The current product source of truth is `OpenField_Phase1_PRD_50_Pages.pdf` (Phase 1 PRD v1.1, Expanded Specification, 15 November 2026), together with approved project decisions and the current repository implementation. `docs/ROADMAP_P2.md` defines the proposed Phase 2 direction. This file consolidates those sources into practical agent instructions; it does not silently override them.

---

## 0. Mandatory Instructions — Read First

1. **Inspect before changing.** Read the relevant source files, existing architecture, tests, Gradle configuration, and documentation before proposing or editing implementation.
2. **Preserve working behavior.** Do not rewrite functioning features, replace stable libraries, migrate architecture, or change the model/runtime merely because another approach is fashionable.
3. **Keep Gemma 4 E2B IT.** The approved and currently working local model is `gemma-4-E2B-it.litertlm` through Google LiteRT-LM. Do not substitute Gemma 2, another Gemma variant, an API model, or a cloud provider.
4. **No cloud inference.** All core AI generation must use the on-device model. Never silently send prompts, images, documents, audio, chat history, or personal data to a remote AI service.
5. **No fake AI.** Never ship hardcoded sample answers, simulated model output, canned success responses, placeholder analysis, or fallback text that pretends to be model output. A fallback may explain an error, but must be clearly identified as a system message.
6. **One shared inference engine; multiple experiences.** Features should reuse the existing local AI engine and orchestration boundary. They must not each create a separate model instance or process.
7. **Modes must actually differ.** Ask AI is normal conversational chat. Learn & Explain is an interactive tutor. Solve a Problem is a stateful diagnostic workflow. Analyze Image is image-grounded. Ask Document is retrieval-grounded. Create & Write is an editable writing workspace. Do not render every mode as the same generic text transcript.
8. **Respect the PRD scope.** Phase 1 explicitly excludes trip planning, navigation, emergency response, survival assistance, social networking, and mandatory cloud processing. Do not introduce these without explicit product-owner approval and a documented scope revision.
9. **Local data is the default.** Room, app-private files, local model files, local document indexes, and installed packs are the system of record for core features.
10. **Never claim unverified completion.** A screen existing is not a feature. A successful compile is not a runtime test. A unit test is not physical-device verification. Report each verification level separately.
11. **Small, reviewable changes.** Implement one coherent milestone at a time. Avoid broad refactors and huge multi-feature coding-agent tasks.
12. **Explain uncertainty.** If the PRD, repository, model artifact, runtime API, or device capability does not establish a fact, inspect or test it. Do not invent implementation details.

If an instruction in a task conflicts with this file, preserve user data and safety first, then follow the latest explicit product-owner decision. Record material changes in the relevant documentation/ADR.

---

## 1. Product Identity

### 1.1 What OpenField is

OpenField is an **offline-first Android AI companion** for everyday questions, understanding, learning, document knowledge, image interpretation, and practical problem solving.

OpenField is not merely a chatbot with an offline toggle. It is a unified local AI interface that combines:
- Natural conversational chat.
- Image understanding.
- Personal document import and grounded Q&A.
- Guided, stateful troubleshooting/problem solving.
- Learning and explanation.
- Focused text utilities and writing workflows.
- A local knowledge library containing personal documents and installable knowledge packs.
- Local model, privacy, storage, and offline-readiness management.

The product promise is useful assistance when internet access is unavailable or undesirable, with user data kept on the device by default.

### 1.2 Vision

Make capable, understandable, privacy-conscious AI assistance available wherever a person has an Android device, including places where connectivity is unreliable or absent.

### 1.3 Product principles

| Principle | Required consequence |
|---|---|
| Offline-first | Core supported flows must work after model/resources are installed, including airplane mode. |
| Local by default | No silent upload of user prompts, conversations, images, documents, or audio. |
| Single coherent UX | Shared navigation, composer, message components, status patterns, and design tokens. |
| Evidence-aware | Document/pack answers retain source chunk and provenance metadata. |
| Human control | User confirms sensitive or irreversible actions; app never autonomously repairs devices. |
| Graceful degradation | Unsupported capability is explained; text chat remains available. |
| Modular architecture | UI depends on feature/domain contracts, not LiteRT-LM or backend implementation details. |
| Honest claims | Distinguish supported, implemented, tested, and device-verified capabilities. |

### 1.4 Non-goals and boundaries

OpenField does **not** promise:
- Cloud-model parity or perfect factual accuracy.
- Professional medical, legal, financial, or engineering diagnosis.
- Universal image/audio support on every phone.
- Autonomous device control or remote repair.
- That a retrieved document is authoritative or factually correct.
- That model multimodality automatically supplies microphone capture, speech recognition, or speech synthesis.

Phase 1 excluded:
- Trip planning, maps/navigation, location tracking.
- Emergency response and survival assistance.
- Social networking/community profiles.
- Mandatory account registration or cloud sync.
- Mandatory cloud inference or Firebase dependency.

A new scope area requires explicit approval, rationale, privacy/offline analysis, implementation cost, and acceptance tests.

---

## 2. Source of Truth and Project Status

### 2.1 Document hierarchy

Use this precedence:
1. Latest explicit product-owner decision in the project.
2. Current approved PRD revision: `OpenField_Phase1_PRD_50_Pages.pdf`, v1.1, 15-Nov-2026.
3. Approved Phase 2 plan: `docs/ROADMAP_P2.md`.
4. Current source code and tests, which establish what is actually implemented.
5. Older notes, mockups, and generated summaries.

The PRD distinguishes product commitments from engineering proposals. An approved requirement is not proof of implementation. If source code conflicts with the PRD, do not silently assume either is correct: report the discrepancy and recommend a minimal resolution.

### 2.2 Phase 1 baseline — reported complete, maintain regression protection

The project owner has reported the Phase 1 roadmap gates as complete and has physically tested the core local chat flow. Current known baseline:

- Native Android application using Kotlin and Jetpack Compose.
- Material 3 light theme and single-activity navigation.
- Twelve designed screens aligned with the approved visual direction.
- Local Gemma 4 E2B IT inference through LiteRT-LM.
- Streaming normal chat and cooperative cancellation.
- Room SQLite conversation persistence.
- PDF/text import through Android Storage Access Framework.
- PDF extraction and page-aware chunking.
- SQLite FTS4 local document retrieval.
- Document Q&A/RAG with source references.
- Image selection/preview and multimodal processing path.
- Guided troubleshooting and Learn & Explain foundations.
- AI utility tools and local knowledge library/catalog foundations.
- Optional FastAPI pack-catalog service; local app must work when backend is absent.
- Privacy-first defaults and no prompt telemetry/logging.
- JUnit tests and Android CI workflow.
- Project documentation including privacy, FAQ, supported devices, release notes, license, and roadmap.

**Important verification qualification:** The above is the reported Phase 1 completion baseline. Verify current code and tests before relying on a specific feature. The current model/runtime integration was tested on a physical Android device and a chat response identifying itself as Gemma 4 was observed. Do not extrapolate that one test into proof of every image, voice, document, pack, or low-memory scenario.

### 2.3 Phase 2 direction

Phase 2 is about making OpenField feel like a set of distinct, useful experiences rather than a generic chatbot with shortcut buttons.

Roadmap milestones in `docs/ROADMAP_P2.md`:
- **2.1 Mode-Based AI Architecture**
- **2.2 Interactive Learn & Explain**
- **2.3 Intelligent Problem Solver**
- **2.4 Advanced Multimodal Intelligence**
- **2.5 Advanced Local Knowledge Engine**
- **2.6 Create & Write Workspace**
- **2.7 Personalization and Local Memory**
- **2.8 Performance, Reliability, and Release Readiness**

Suggested releases:
- v0.2.0 Experience Foundation.
- v0.3.0 Multimodal Intelligence.
- v0.4.0 Personal Productivity.
- v0.5.0 Release Readiness.

These are targets, not completed features. Update the roadmap only with evidence.

---

## 3. Platform, Stack, and Runtime Baseline

### 3.1 Known technology choices

| Area | Current project direction / known baseline |
|---|---|
| Platform | Android only |
| Language | Kotlin |
| UI | Jetpack Compose |
| Design system | Material 3, approved light theme |
| Local persistence | Room over SQLite |
| Document retrieval | SQLite FTS4 |
| Local AI | Gemma 4 E2B IT |
| Inference runtime | Google LiteRT-LM |
| Model artifact | `gemma-4-E2B-it.litertlm` |
| Document parsing | PDFBox Android / existing extraction implementation |
| File access | Android Storage Access Framework |
| Async work | Kotlin coroutines; WorkManager where suitable |
| Optional backend | FastAPI, PostgreSQL, Supabase Auth/Storage as catalog/sync infrastructure only |
| Package/application ID | `com.openfield.app` |
| Project path (developer workstation) | `E:\personal\OpenField\android` |

Do not assume every item above is used in every feature. Inspect Gradle and source before adding or changing dependencies.

### 3.2 Current local development environment (known snapshot)

Previously validated build configuration:
- Gradle wrapper: 9.7.1.
- Android Gradle Plugin: 9.3.1.
- Kotlin: 2.4.21.
- KSP: 2.3.14.
- Room: 2.7.2.
- LiteRT-LM: 0.19.1.
- kotlinx-coroutines: 1.11.0.
- Java compile target: 17; local Android Studio JBR was Java 25.

Versions can change. Always inspect `gradle/libs.versions.toml`, root/app Gradle files, wrapper properties, and dependency insight before recommending a version update. Do not blindly downgrade or upgrade.

### 3.3 Known model/device facts

- Model filename: `gemma-4-E2B-it.litertlm`.
- Approximate artifact size: 2.588 GB (2,588,147,712 bytes in one tested installation).
- App-private model path observed on device: `/data/user/0/com.openfield.app/files/models/gemma-4-E2B-it.litertlm`.
- Test device used: LAVA LXX503, Android 14, ARM64, 6 GB RAM.
- Another device profile in project history: 6 GB RAM Android phone.
- Minimum support claims must follow `docs/SUPPORTED_DEVICES.md` and actual testing, not assumptions.

The LiteRT-LM engine has been observed initializing successfully on CPU, loading model signatures, and running the local model. A prior crash in the LiteRT-LM completion callback was addressed by aligning Kotlin coroutines to 1.11.0 and removing exclusions from the LiteRT-LM dependency. A subsequent APK installed successfully and the user confirmed chat worked. Do not reintroduce the old exclusions or alter dependencies without evidence.

### 3.4 Build and install commands

From the Android project root in PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Install with the full ADB path if `adb` is not on PATH:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r ".\app\build\outputs\apk\debug\app-debug.apk"
```

Capture crash logs:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" logcat -b crash -d -v threadtime
```

If PowerShell says `adb` is not recognized, this is a shell PATH issue, not necessarily an Android build failure. Use the full executable path above. Do not tell the developer to reinstall Android Studio as the first response.

---

## 4. Product Information Architecture and Screens

### 4.1 Top-level navigation

Persistent bottom navigation:
1. **Home**
2. **Chats**
3. **Tools**
4. **Library**

Settings are reachable from the appropriate profile/overflow/settings entry. Preserve the established navigation model unless an approved UX change says otherwise.

### 4.2 Screen inventory and responsibilities

The approved visual reference is a light, AI-first Android experience. Recreate UI using native Compose components; never use a screenshot/mockup as the actual interactive screen.

| Screen / area | Responsibility |
|---|---|
| Splash | Initialize app and show concise loading state; no mandatory sign-in or long animation. |
| Onboarding: Ask Anything | Explain everyday questions and offline AI. |
| Onboarding: Understand & Solve | Introduce image understanding, troubleshooting, and learning. |
| Onboarding: Your Knowledge | Introduce local documents, packs, and privacy. |
| Model Setup | Device/storage check, download, integrity verification, initialization, smoke test, retry. |
| Home | Model/offline readiness, dominant prompt composer, core task shortcuts, recent activity. |
| AI Chat | Natural conversation, attachments, streaming, follow-ups, stop/retry/copy/save/delete. |
| Analyze Image | Image selection/capture, preview, question, analysis, observations, uncertainty, follow-ups. |
| Document Q&A | File card, extraction/index state, scoped questions, grounded answers, source cards. |
| Solve a Problem | Structured problem statement, clarifying questions, step cards, observations, branching, stop/finish. |
| Learn & Explain | Topic and level selection, lesson content, examples, practice, feedback, progress. |
| AI Tools | Task-specific utilities and dedicated workflows. |
| My Library | Separate personal documents and knowledge packs; status, search, install/remove. |
| Settings | Model, offline, privacy, storage, language/theme, supported preferences, About. |

### 4.3 Home screen rules

- The prompt composer is the primary focus.
- Show factual model readiness: missing, downloading, loading, ready, unsupported, failed.
- Show offline status in plain language; do not imply that every feature is available merely because the device is offline.
- Core shortcuts: Analyze Image, Ask Document, Solve a Problem, Learn & Explain.
- Recent activity is secondary.
- Do not turn Home into a promotional feed.
- Do not show deferred trip/map/emergency/survival features.
- Preserve typed prompt if the model is not ready and guide the user to setup.
- Feature availability must reflect actual capability descriptors.

### 4.4 Navigation and state restoration

- Top-level navigation remains persistent.
- Detail screens provide a clear back action.
- Restore drafts/task context where safe.
- Do not put large file/image payloads in navigation arguments; pass stable IDs/URIs or saved references.
- Do not discard user work silently.
- After process recreation, restore persisted data and re-evaluate runtime/model readiness.
- A killed generation must not be presented as completed.

---

## 5. Mode Contracts — Distinct Experiences Are Mandatory

All modes reuse the same local inference engine, but each has its own task contract, state, UI, prompt policy, validation, and persistence.

### 5.1 Ask AI — normal chatbot

**Purpose:** General-purpose natural conversation.

Expected behavior:
- Plain chat composer and message list.
- Natural conversational system instruction.
- Ordered conversation context.
- Streaming output.
- Follow-up continuity.
- Stop generation.
- Retry/regenerate with clear attempt history.
- Copy/select text.
- Local persistence and conversation search/history.
- Clear pending, streaming, complete, cancelled, and failed states.

Do not force normal chat into JSON, quiz cards, a wizard, or a rigid response template. It should remain the only mode whose primary interaction is a conventional chat transcript.

### 5.2 Analyze Image — image-grounded workspace

**Purpose:** Answer a user question about a selected image.

Flow:
1. Select image using Android picker or supported camera flow.
2. Display preview; allow replace/remove.
3. Ask what the user wants to know or provide a suggested task.
4. Validate URI, file access, MIME type, orientation, dimensions, and size.
5. Prepare image using the actual runtime-supported preprocessing path.
6. Confirm installed model/runtime image-input capability.
7. Send actual image content plus prompt to Gemma.
8. Render result with observations, interpretations, uncertainty, and follow-up.

Rules:
- Never pretend to analyze an image if only its filename or URI was supplied.
- Do not fabricate confidence percentages.
- Distinguish visible observations from inference.
- Qualify ambiguous identifications.
- Never certify edibility, toxicity, medical diagnosis, or physical safety from an image.
- Clear temporary image buffers when not saved.
- Keep image processing local by default.
- If vision is unavailable, explain and keep text chat usable.

### 5.3 Ask Document — grounded document workspace

**Purpose:** Answer from a selected personal document or explicitly selected document scope.

Flow:
1. Import using Storage Access Framework.
2. Validate MIME type, size, encryption, and supported format.
3. Extract text asynchronously.
4. Preserve page/section boundaries when possible.
5. Normalize and chunk with metadata.
6. Index locally using FTS.
7. Retrieve relevant chunks for each question.
8. Pass bounded passages plus source metadata to Gemma.
9. Render answer and tappable source/page references.
10. Support follow-up within the selected scope.

Rules:
- A document-scoped question must not search all personal files unless the user explicitly changes scope.
- Never invent a page, quote, source, or retrieved fact.
- Track chunk IDs supplied to the model.
- If evidence is insufficient, say so. Offer a general answer separately only when the user requests it.
- Scanned PDFs without extractable text remain unsupported until OCR is deliberately implemented and tested.
- Encrypted, malformed, empty, or oversized files need clear error states.
- Indexing must be incremental, cancellable, and recoverable.
- Deleting a document removes its derived chunks/index entries, subject to documented lifecycle rules.
- Re-indexing must version or invalidate stale chunks.

### 5.4 Solve a Problem — guided diagnostic workflow

**Purpose:** Turn an open-ended issue into a sequence of safe questions, steps, and observations.

Recommended state machine:
- `Draft`
- `Clarifying`
- `ReadyToStart`
- `StepActive`
- `AwaitingObservation`
- `Completed`
- `Stopped`
- `Escalated`

Flow:
1. Capture editable problem statement and optional context/image.
2. Ask only material clarifying questions.
3. Summarize understanding and propose a safe plan.
4. Present one primary step at a time.
5. Include action, reason, expected observation, and caution.
6. Collect user observation (structured options and/or free text).
7. Use observation to choose next step or revise hypothesis.
8. Ask whether the issue is resolved.
9. Finish with summary, unresolved items, and safe next action.

Rules:
- The app is an information workflow, not a remote repair agent.
- Never execute commands or modify device settings autonomously.
- Do not tell users to disable security protections or run unknown commands blindly.
- Require explicit confirmation for destructive or risky actions.
- Include a stop/escalate path when risk or uncertainty increases.
- Do not state a likely cause as confirmed diagnosis.
- Persist session state, answers, steps, timestamps, and terminal status.
- Validate generated step schemas before enabling controls.
- If structured output fails, allow one repair attempt; otherwise show a safe explanatory fallback without fake controls.

### 5.5 Learn & Explain — interactive tutor

**Purpose:** Help the user understand a topic through an adaptive lesson, not just a long chat answer.

Recommended flow:
`Topic → Level → Lesson outline → Concept → Example → Knowledge check → Feedback → Next concept → Revision`

Core interaction:
- Topic input or selected topic.
- Level selection: beginner, intermediate, advanced (or concise/detailed variants if existing UX requires).
- Short lesson outline.
- Concept cards/sections with definitions and explanations.
- Examples and analogies.
- “Explain more simply”, “Show another example”, “Give practice”, “Summarize” actions.
- Interactive knowledge checks, preferably multiple-choice plus optional free response.
- Immediate feedback and explanation for correct/incorrect answers.
- Hints and retry.
- Lesson progress and completion summary.
- Resume unfinished lessons and review completed lessons.

Rules:
- Do not present the whole lesson as a generic assistant bubble.
- Do not claim mastery from one answer.
- Persist lesson ID, topic, level, outline, sections, quiz questions, attempts, feedback, and progress locally.
- Generate content using Gemma, then validate structured fields.
- If the model returns malformed content, repair once or gracefully fall back to readable explanation without pretending an interactive quiz exists.

### 5.6 Create & Write — editable writing workspace

**Purpose:** Produce and revise content in a document-like editor.

Supported tasks:
- Draft emails, messages, reports, assignments, notes, project descriptions.
- Summarize, rewrite, expand, shorten, translate, change tone, improve clarity/grammar, extract structured points.
- Preserve factual meaning unless user explicitly asks to alter it.
- Make generated content editable.
- Copy, save, reopen, revise, and export supported local formats.

Rules:
- Do not make chat bubbles the primary writing surface.
- Clearly distinguish generated draft from user edits.
- Preserve original input for revision.
- Save drafts locally.
- Do not claim a file was exported unless it was actually created.

### 5.7 AI Tools — focused transformations

The Phase 1 tool contracts include:
- **Summarize:** Condense while preserving important facts and caveats.
- **Explain:** Explain a phrase/topic at the selected level; examples optional.
- **Translate:** Preserve names, numbers, units, and technical tokens; disclose uncertainty for idioms.
- **Rewrite:** Change tone/length/clarity without changing intended meaning.
- **Extract:** Return key points/fields with source references when document-grounded.
- **Generate/Create:** Produce clearly labelled draft content under user constraints.

Each tool requires:
- A visible selected task/mode.
- Validated input.
- Dedicated task instruction/template.
- Loading, success, error, retry, and copy states.
- Output-specific rendering.
- Local execution and persistence as appropriate.

Do not use one generic prompt for every tool.

### 5.8 Chats

- Search local conversation titles and user message text where supported.
- Rename, open, and delete conversations.
- Preserve chronological ordering and generation status.
- A failed or cancelled attempt must not look complete.
- Deleting a conversation cleans its associated messages and conversation-owned attachment references according to retention policy.

### 5.9 My Library

Keep two resource types clearly separate:
1. **Personal documents** selected/imported by the user.
2. **Knowledge packs** installed as versioned curated resources.

Pack lifecycle:
`Discover → Inspect details/license/source → Download → Verify checksum → Validate manifest/compatibility → Install atomically → Index → Search/use → Update/remove`

Pack rules:
- Show publisher, source, version, license, size, and compatibility where metadata exists.
- Validate manifest schema and safe paths.
- Reject path traversal, executable content, malformed manifests, missing provenance/license metadata, incompatible versions, and unreasonable file sizes.
- Never execute scripts/code from packs.
- Failed installs must not become searchable.
- Updates are staged; do not destroy the working version until the new version validates.
- Keep chunk provenance including pack ID/version.
- Removing one version must not remove another installed version's chunks.
- Retain historical chat text if its source pack is later removed, but mark source availability accurately.

---

## 6. AI Model Capabilities and Limits

### 6.1 Model policy

The current single-model baseline is **Gemma 4 E2B IT** using the `.litertlm` artifact and LiteRT-LM runtime. It is the only approved inference model for the current project baseline.

Do not:
- Replace it with a cloud API.
- Add a second LLM to perform a feature invisibly.
- Add a remote fallback when local generation fails.
- Claim model support for a modality merely because a model family supports it in some other runtime/artifact.
- Change model artifact format, source, or runtime without a technical validation and explicit approval.

### 6.2 What the model can be used for (subject to actual artifact/runtime path)

Text generation can power:
- General Q&A and conversation.
- Explanations and tutoring.
- Summarization and extraction from supplied context.
- Translation and rewriting.
- Structured lesson/quiz/diagnostic generation.
- Reasoning over bounded retrieved passages.
- Drafting and creative generation.

Image understanding can be used only when:
- The exact installed artifact supports vision.
- LiteRT-LM API path accepts image content.
- Android preprocessing creates the required representation.
- The image is actually passed into inference.
- The full flow is verified on target hardware.

Audio/voice must be treated as separate capabilities:
- Microphone permission and capture.
- Audio preprocessing.
- Model audio input, if supported by this exact artifact/runtime.
- Offline speech-to-text, if provided by a verified local speech adapter.
- Text-to-speech, if provided by a verified local TTS adapter.
- Playback, interruption, transcript review, and lifecycle behavior.

Do not infer that Gemma's audio capability automatically supplies a full voice assistant.

### 6.3 Capability registry

Use one source of truth for feature availability. The app should distinguish:
- Model artifact capability.
- Runtime API capability.
- Device hardware capability.
- App adapter implementation.
- User permission.
- Installed resources.
- Network-only optional actions.

A capability should be enabled only when all required layers are present. Avoid scattered hardcoded booleans across screens. Re-evaluate readiness after process restart and model load/unload.

### 6.4 Prompting and generation

- Keep system/task prompt construction outside UI composables.
- Keep prompts versioned or centrally managed.
- Separate normal-chat instructions from feature-specific instructions.
- Bound context length and output size for the target device.
- Include only relevant conversation history and permitted local context.
- Avoid sending hidden reasoning traces to UI or database.
- Support cancellation and resource cleanup.
- Validate generated structured data before rendering interactive UI.
- Never assume model output is valid JSON just because JSON was requested.
- Parse defensively; reject missing/invalid required fields.
- If supported, perform at most one constrained repair attempt; then degrade gracefully.
- Keep model errors distinct from empty successful responses.

### 6.5 Output schemas

Use typed domain data structures for interactive modes, for example:
- `LessonOutline`, `LessonSection`, `KnowledgeCheck`, `AnswerFeedback`.
- `DiagnosticQuestion`, `DiagnosticStep`, `ExpectedObservation`, `SafetyCaution`, `WorkflowTransition`.
- `DocumentAnswer`, `SourceReference`, `RetrievedChunk`.
- `WritingDraft`, `RevisionInstruction`, `UtilityResult`.

Names are conceptual examples, not instructions to duplicate existing classes. Inspect the codebase and reuse or adapt current models before adding types.

---

## 7. Architecture and Dependency Rules

### 7.1 Preferred logical layers

The PRD describes these responsibilities:

1. **Presentation:** Compose screens, reusable components, navigation, ViewModels/state holders.
2. **Domain:** Use cases, feature contracts, policies, typed task requests/results.
3. **Data:** Repositories, Room DAOs, file sources, settings, local resource lifecycle.
4. **AI orchestration:** Prompt construction, conversation context, retrieval coordination, mode routing, parsing/validation.
5. **Inference adapter:** LiteRT-LM model loading, tokenization, generation, streaming, cancellation, release.
6. **Local capabilities:** Image processing, document parsing, speech adapters.
7. **Optional backend:** Catalog/pack metadata/artifact distribution and future explicitly opt-in sync only.

Do not force this into many Gradle modules without a concrete need. Package boundaries are sufficient until build isolation or ownership justifies module splitting.

### 7.2 Dependency direction

- UI depends on domain contracts.
- Domain should not depend on Android UI or remote service implementation.
- Repositories/adapters implement domain contracts.
- AI feature controllers call a shared orchestration service.
- Orchestration calls a local inference abstraction.
- The LiteRT-LM SDK stays behind the inference adapter.
- Backend connectivity must not be required for local chat, local RAG, or installed packs.

Avoid:
- Direct LiteRT-LM calls from composables.
- Prompt strings scattered across screens.
- Direct Room DAO calls from UI.
- Duplicate model engines per mode.
- Circular package dependencies.
- Large navigation arguments carrying file bytes or image bitmaps.

### 7.3 Mode-based orchestration

A mode request should carry the minimum required data:
- Mode/task identifier.
- User input.
- Conversation/session identifier.
- Optional attachment references.
- Optional selected document/pack scope.
- Relevant history/context.
- Generation options within model constraints.

Orchestration should:
1. Validate request.
2. Resolve capabilities.
3. Load permitted context.
4. Retrieve local evidence if needed.
5. Build mode-specific prompt.
6. Invoke the shared model engine.
7. Stream or collect response.
8. Parse/validate if structured output is required.
9. Persist final/cancelled/failed state.
10. Return typed UI state.

Do not add a separate inference pipeline for every feature.

### 7.4 Error model

Use clear, typed errors where appropriate:
- Model missing/not ready.
- Unsupported capability.
- Permission denied.
- Invalid input/file.
- Insufficient storage.
- Retrieval has no evidence.
- Context too large.
- Out of memory.
- Generation cancelled.
- Runtime failure.
- Corrupt model/pack.
- Network unavailable for optional online action.

User-facing messages should be plain language. Technical details and stack traces belong in debug logs, not visible answer text. Never log private prompt/document contents.

---

## 8. Local Data, Room, Files, and RAG

### 8.1 Local source of truth

The Android client owns:
- Conversations and messages.
- User-selected documents and metadata.
- Extracted text/chunks/index metadata.
- Guided workflow sessions.
- Learning sessions/progress.
- Writing drafts.
- App settings/preferences.
- Model installation metadata.
- Installed knowledge pack metadata/content/indexes.

Keep large model, document, image, and pack binaries in managed files/directories rather than storing large blobs in Room. Room stores metadata and references.

### 8.2 Database evolution

Before changing entities:
1. Inspect all current Room entities, DAOs, database class, converters, and migrations.
2. Understand foreign keys, indices, cascade behavior, and existing data.
3. Add a forward migration; do not delete user data or reset the database.
4. Add migration tests where possible.
5. Keep status enums/versioned payloads backward compatible.
6. Verify deletion semantics and derived-index cleanup.

Never use destructive migration in production to avoid writing a migration.

### 8.3 Conversation/message lifecycle

Message states should distinguish at least:
- Pending.
- Streaming.
- Complete.
- Cancelled/interrupted.
- Failed.

Rules:
- Commit user messages safely before generation.
- Persist final assistant response and relevant metadata.
- Cancelled partial output must be visibly partial/cancelled.
- Process death must not restore an incomplete response as completed.
- Retry creates a distinct attempt or version; do not silently overwrite previous output.
- Keep stable ordering using sequence/time.
- Do not persist hidden reasoning or temporary model internals.
- Delete conversation-owned references according to documented retention policy.

### 8.4 Document ingestion and FTS RAG

Existing Phase 1 baseline uses PDF/text extraction, bounded chunking with overlap, page metadata, Room/SQLite, and FTS4.

Improve incrementally:
- Preserve document ID, filename, parser version, page/section, chunk index, and provenance.
- Use deterministic chunking and stable metadata.
- Avoid losing page references at chunk boundaries.
- Normalize query text carefully.
- Retrieve only within the selected document scope unless user broadens it.
- Rank relevant passages; deduplicate near-identical chunks.
- Bound the number and size of passages supplied to the model.
- Preserve exact chunk IDs in the answer record.
- Expose source cards/snippets/page references from the retrieved metadata.
- Include an insufficient-evidence route.
- Do not treat FTS results as proof of truth; they are provenance pointers.

Potential Phase 2 improvements (only after baseline tests):
- Heading-aware chunking.
- Query expansion/rewrite.
- Hybrid retrieval if a real local embedding path is justified and supported.
- Multi-document search with explicit scope.
- Document/chapter summaries.
- Cross-document comparison.
- Better relevance evaluation corpus.

Do not add an embedding model or remote vector database without explicit approval and a measured need.

### 8.5 Document limitations

Unless separately implemented and verified:
- Scanned PDFs with no text layer are unsupported (OCR not implied).
- Encrypted PDFs need a clear unsupported/password path.
- Do not claim support for every Office file format.
- Do not fabricate page numbers when extraction cannot establish them.
- A citation points to source location; it does not validate the source's truth.

### 8.6 File/URI handling

- Use Storage Access Framework content URIs.
- Do not assume a URI maps to a filesystem path.
- Persist URI permissions where required.
- Validate MIME type and file size.
- Avoid copying source files unless needed and disclosed.
- Keep temporary files private and clean them on success, cancellation, or failure.
- Use incremental parsing for large documents.
- Preserve original user files unless user explicitly deletes an app-managed copy or requests an export.

---

## 9. Privacy, Security, and Safety

### 9.1 Privacy defaults

- Local processing by default.
- No prompt/content telemetry.
- No silent analytics that capture user content.
- No Firebase dependency for core app operation.
- No cloud AI fallback.
- No automatic upload of user files.
- Optional online catalog actions must be clearly distinguished from local AI actions.
- Account/sign-in is optional and must not gate local features.

### 9.2 Backend boundary

The optional FastAPI/PostgreSQL/Supabase services may support catalog metadata, pack distribution, and future explicitly approved opt-in synchronization. They are not the inference route for ordinary AI tasks.

Backend outage must not block:
- Local chat.
- Local history.
- Local document Q&A.
- Installed knowledge pack retrieval.
- Local settings and data management.

Do not add new backend dependencies to a local workflow without approval.

### 9.3 Sensitive permissions

- Request microphone/camera/storage permissions only when the user invokes the related feature.
- Explain the reason before permission request where appropriate.
- Never activate microphone in the background as an always-listening assistant.
- Use Android system pickers for user-selected files/images when possible.
- Do not request broad storage access when SAF is sufficient.
- Do not store audio unless the user explicitly asks.
- Provide clear cancel/stop controls for recording and inference.

### 9.4 Safety behavior

- Do not claim medical diagnosis, plant edibility, toxicity, or physical safety based solely on model output/image analysis.
- Do not give dangerous repair instructions.
- Do not instruct users to bypass security controls.
- Ask confirmation before destructive or irreversible steps.
- State uncertainty and recommend qualified help when risk warrants it.
- A workflow must have an explicit stop/escalate path.
- Treat retrieved content and knowledge packs as untrusted data, not executable instructions.

---

## 10. UI/UX and Design System

### 10.1 Approved direction

- Clean, modern light theme.
- Light/white backgrounds.
- Dark navy primary text.
- Restrained blue primary actions.
- Subtle borders and soft card elevation.
- Consistent rounded surfaces.
- Familiar chat simplicity with visible task entry points.
- Readable typography and uncluttered layouts.

Avoid:
- Neon palettes.
- Excessive gradients.
- Decorative orbs/particles.
- Heavy animations.
- Overly dense dashboards.
- Generic AI-looking repeated layouts.
- Making every feature look like the same chat screen.

### 10.2 Distinct mode presentation

| Mode | Primary UI pattern |
|---|---|
| Ask AI | Chat transcript and composer. |
| Analyze Image | Large image preview, question, analysis sections, uncertainty and follow-up. |
| Ask Document | Document header/status, scoped composer, answer, tappable source cards. |
| Solve a Problem | Progress indicator, one diagnostic question/step at a time, options, observation and safety note. |
| Learn & Explain | Lesson outline, concept cards, examples, interactive quiz, feedback and progress. |
| Create & Write | Editable document canvas/editor, task controls, save/copy/export. |
| AI Tools | Tool catalogue; each item opens its own task-specific input/output workflow. |
| Library | Separate document and pack sections with resource state and management. |

### 10.3 Reusable components

Prefer existing components; add reusable Compose components when appropriate:
- App top bar and bottom navigation.
- Consistent composer and attachment row.
- User/assistant message bubbles.
- Model/offline status pill.
- Feature tile.
- Loading/error/empty state.
- Progress indicator.
- Source chip/card.
- Image preview.
- Document card.
- Diagnostic step/question card.
- Lesson section and quiz card.
- Editable writing surface.

Use semantic colors/tokens for spacing, radius, typography, elevation, and states. Do not create a new design language per feature.

### 10.4 Accessibility

- TalkBack labels and logical traversal order.
- Scalable text; avoid fixed-height containers clipping content.
- Large enough touch targets.
- Visible focus and pressed states.
- Do not use color alone for status; pair color with text/icon.
- Verify contrast, including grayscale.
- Keep long text selectable and readable.

---

## 11. Performance and Device Constraints

OpenField runs a roughly 2.6 GB local model on mobile hardware. Treat memory, thermal load, storage, and lifecycle as first-class constraints.

Measure rather than guess:
- Cold model load time.
- Time to first token.
- Token throughput.
- Peak and sustained RAM.
- CPU/GPU stability and fallback.
- Image preprocessing memory.
- PDF extraction/indexing time and memory.
- FTS query latency.
- App launch and navigation responsiveness.
- Battery and thermal behavior.
- Model download/resume/storage requirements.

Engineering rules:
- Do not load multiple model instances.
- Release temporary buffers and model/session resources at appropriate lifecycle boundaries.
- Avoid main-thread inference, parsing, or large file operations.
- Use bounded queues and bounded context.
- Cancel long operations cooperatively.
- Handle low-memory callbacks/process death safely.
- Do not assume GPU/NPU exists or works on every device.
- Provide CPU fallback only if the current runtime configuration supports it and the fallback is tested.
- Never promise a latency target without measuring it on a named device.

---

## 12. Testing and Definition of Done

### 12.1 Test layers

Add/update tests at the level appropriate to the change:
- Unit tests for prompt builders, validators, state machines, chunking, retrieval ranking, and utility contracts.
- Repository tests for persistence and file lifecycle.
- Room DAO and migration tests.
- ViewModel/state tests for loading, cancellation, errors, and restoration.
- Compose UI tests for mode-specific interactions.
- Instrumentation/device tests for LiteRT-LM, image input, file picker, camera/microphone, and lifecycle.
- Manual airplane-mode tests for offline claims.

### 12.2 Critical regression suite

After material changes, verify:
- Model load and unload.
- Normal `Hi` chat produces a real Gemma response.
- Follow-up uses relevant conversation context.
- Streaming and Stop work.
- Cancelled/failed responses are not marked complete.
- Conversation persists after process death.
- RAG answer cites the correct page/chunk.
- Unanswerable document question reports insufficient evidence.
- Image content is actually passed to vision inference.
- Feature-specific screens are not generic chat clones.
- App works in airplane mode for core local features.
- Backend absence does not block local functionality.
- No private content is transmitted unexpectedly.

### 12.3 Definition of done

A feature is **not complete** because it compiles or because a coding agent says it is complete.

For every milestone:
1. Inspect current implementation and PRD requirement IDs.
2. Write a short implementation plan and list affected files.
3. Reuse existing functionality.
4. Implement the smallest coherent change.
5. Add/update automated tests.
6. Run relevant Gradle tests.
7. Build debug APK.
8. Install and test on a physical device.
9. Verify offline behavior when relevant.
10. Test cancellation, failure, process recreation, and empty states where relevant.
11. Run critical Phase 1 regression checks.
12. Update documentation/roadmap with evidence and limitations.

Use these status labels accurately:
- Not started.
- In progress.
- Implemented.
- Automated-tested.
- Device-verified.
- Completed.
- Blocked.

### 12.4 Test evidence format

Record:
- Build/commit identifier.
- Device model.
- Android version.
- RAM tier.
- Model artifact/version.
- Online/offline condition.
- Steps performed.
- Expected and actual result.
- Logs or test report reference.
- Known limitations.

---

## 13. AI Coding Agent Workflow

### 13.1 Before implementation

Every agent must:
1. Read this `AGENTS.md`.
2. Read the relevant PRD pages/requirement IDs.
3. Read `docs/ROADMAP_P2.md` for Phase 2 tasks.
4. Inspect the actual repository tree and current git status.
5. Search for existing implementations before adding new files/classes.
6. Inspect related ViewModels, repositories, entities, prompts, and UI components.
7. Identify the smallest safe change.
8. State assumptions and unresolved technical risks.
9. Provide a concise plan and list expected files to change.

### 13.2 During implementation

- Follow existing naming, package structure, formatting, and dependency patterns.
- Keep changes focused.
- Do not edit unrelated screens or design tokens.
- Do not remove tests because they fail; diagnose and fix the cause.
- Do not change Gradle/plugin/runtime/model versions as part of an unrelated feature.
- Do not use placeholder data in production paths.
- Do not hardcode model responses.
- Do not add permissions without explaining why.
- Do not introduce a new service/API for something already handled locally.
- Keep secrets, signing keys, tokens, and credentials out of source control.
- Never overwrite user documents or reset Room data.
- Add migration code for schema changes.
- Use structured logs that exclude private user content.

### 13.3 After implementation

Report:
- What changed.
- Why it changed.
- Files changed.
- Tests executed and exact outcomes.
- Build outcome.
- Device verification outcome (or explicitly “not tested on device”).
- Offline verification outcome.
- Known limitations and follow-up work.
- Any PRD/roadmap changes needed.

Never report “all tests pass” if only compilation ran. Never report “feature works” if only a mock preview was tested.

### 13.4 When blocked

If blocked by missing information:
- Inspect source/docs first.
- Ask one focused question only if the decision cannot be derived.
- Do not invent APIs, schemas, model capabilities, package names, or file paths.
- Offer the safest minimal option and explain trade-offs.
- Avoid broad speculative changes.

---

## 14. Phase 2 Implementation Priorities

Use `docs/ROADMAP_P2.md` as the detailed milestone checklist. The recommended order is:

### 2.1 Mode-Based AI Architecture — first

- Audit existing screens, routes, ViewModels, AI calls, prompts, and Room entities.
- Define mode IDs/configuration and typed task contracts.
- Centralize prompt building and shared orchestration.
- Keep Ask AI as natural streaming chat.
- Route each feature to a dedicated screen/controller/renderer.
- Add robust structured-output parsing and validation.
- Preserve existing sessions and database data.
- Add Phase 1 regression tests.

### 2.2 Interactive Learn & Explain

- Topic and level.
- Lesson outline and sections.
- Examples/analogies.
- Interactive quiz and feedback.
- Hints, retry, follow-up.
- Local progress and resume.

### 2.3 Intelligent Problem Solver

- Structured diagnostic state machine.
- One question/step at a time.
- Options plus free text.
- Expected observations and safety cautions.
- Branching based on user observations.
- Stop/escalate/completion paths.
- Local persistence and resume.

### 2.4 Advanced Multimodal Intelligence

- Verify exact image path and real content delivery.
- Improve screenshot, chart, diagram, and text-in-image workflows only where tested.
- Investigate voice adapters separately; do not assume model audio support equals a full voice feature.
- Measure memory, latency, and thermal behavior.

### 2.5 Advanced Local Knowledge Engine

- Improve chunking and ranking.
- Preserve provenance/page metadata.
- Add multi-document scope only with explicit user control.
- Add summaries and cross-document comparison.
- Enforce insufficient-evidence responses.

### 2.6 Create & Write Workspace

- Dedicated editable writing UI.
- Draft, revise, rewrite, translate, summarize, extract.
- Save/reopen/copy/export.
- Preserve original content and user edits.

### 2.7 Personalization and Local Memory

- User-selected language and explanation level.
- Local conversation search and summaries.
- User-controlled memory with inspect/edit/delete.
- Keep sensitive inferences out of silent memory.
- Ensure deletion also cleans derived indexes.

### 2.8 Performance, Reliability, and Release Readiness

- Device matrix testing.
- Memory, latency, thermal and battery measurement.
- Process recreation, cancellation, storage and migration tests.
- Privacy/security review.
- Accessibility and documentation.
- Release notes and known limitations.

Do not attempt all milestones in one task. Complete, verify, and record one coherent milestone before expanding scope.

---

## 15. Documentation and Repository Hygiene

Maintain documentation as implementation evolves:
- `AGENTS.md` — agent operating manual and consolidated context.
- `OpenField_Phase1_PRD_50_Pages.pdf` — authoritative Phase 1 product requirements.
- `docs/ROADMAP.md` — Phase 1 roadmap/history.
- `docs/ROADMAP_P2.md` — Phase 2 milestones and progress.
- `PRIVACY_NOTICE.md` — user-facing data/privacy explanation.
- `FAQ.md` — user-facing support and limitations.
- `SUPPORTED_DEVICES.md` — tested device tiers and caveats.
- `RELEASE_NOTES.md` — release changes.
- `LICENSE` — app license and model attribution/terms.
- `.github/workflows/android-ci.yml` — CI workflow.

If actual filenames or locations differ, inspect the repository and update this list rather than creating duplicate documents.

For material architecture decisions, create a short ADR recording:
- Context/problem.
- Decision.
- Alternatives considered.
- Consequences/trade-offs.
- Date and affected milestone.

Do not commit:
- Local model artifacts unless explicitly required and licensed.
- API secrets, passwords, signing keys, or private customer files.
- Build outputs, caches, or generated local data unless repository policy explicitly says otherwise.

---

## 16. PRD Traceability Reference

The Phase 1 PRD uses stable requirement prefixes. Use them in issues, tests, and implementation notes.

| Prefix | Area |
|---|---|
| ONB | Onboarding and model setup |
| NAV | Navigation and readiness |
| CHAT | Chat and conversation history |
| IMG | Image input/analysis |
| VOICE | Voice input |
| DOC | Document import, extraction, and Q&A |
| GUIDE | Guided problem solving |
| LEARN | Learning and explanation |
| TOOL | AI utilities |
| LIB | Library and knowledge packs |
| MODEL | Model lifecycle |
| PRIV | Privacy |
| API | Optional backend |
| NFR | Non-functional requirements |

Important Phase 1 requirements called out in the PRD include:
- `ONB-001` to `ONB-005`: offline purpose, truthful model size, storage check, artifact verification, no mandatory account.
- `NAV-001` to `NAV-004`: Home/Chats/Tools/Library, context-preserving back navigation, model readiness.
- `CHAT-001` to `CHAT-010`: local conversations, chronological persistence, follow-up, cancellation, deletion, process restoration, duplicate-send safety, local/online clarity.
- `IMG-001` to `IMG-005`: picker, preview/replace/remove, capability check, local handling.
- `VOICE-001` to `VOICE-005`: explicit recording, stop/cancel, transcript review where supported, text fallback, no background always-listening.
- `DOC-001` to `DOC-010`: supported import, URI persistence, extraction/page boundaries, local chunking/indexing, grounded Q&A, reliable citations, unsupported-file handling, deletion/index cleanup.
- `GUIDE-001` to `GUIDE-006`: problem capture, clarification, step/observation, adaptation, persistence, safe terminal states.
- `LEARN-001` to `LEARN-002`: explanation levels, examples, practice.
- `TOOL-001` to `TOOL-002`: focused utility contracts and generated output clearly labelled as suggestion.
- `LIB-001` to `LIB-010`: separate documents/packs, version/status, manifest/checksum validation, atomic installation, provenance, no pack code execution.

Use the PDF itself for exact normative wording and full acceptance criteria; this table is a navigation aid, not a replacement.

---

## 17. Quick Agent Checklist

Before editing:
- [ ] Read this file and relevant PRD pages.
- [ ] Check `docs/ROADMAP_P2.md` and current git status.
- [ ] Inspect existing implementation and tests.
- [ ] Confirm the requested behavior belongs to the correct mode.
- [ ] Confirm no cloud inference or model substitution is being introduced.

Before finishing:
- [ ] No fake/sample AI output in production.
- [ ] No regression to normal Ask AI chat.
- [ ] No regression to local RAG and source references.
- [ ] No user data loss or destructive database reset.
- [ ] Cancellation and failure states are handled.
- [ ] Tests run and results reported honestly.
- [ ] APK build status reported.
- [ ] Physical-device verification status reported.
- [ ] Offline behavior checked where relevant.
- [ ] Roadmap/docs updated only with evidence.

---

## 18. Final Product Definition

OpenField should become:

- **One local AI engine** powered by Gemma 4 E2B IT.
- **Multiple distinct intelligent experiences** designed around the user's task.
- **A dependable offline application** that does not require cloud inference.
- **A private local workspace** for conversations, documents, learning, and writing.
- **An evidence-aware assistant** that distinguishes retrieved source material from model interpretation.
- **A transparent product** that communicates limitations and never pretends an unimplemented capability works.

The agent's job is not to maximize the number of features or rewrite the application. The agent's job is to improve OpenField incrementally while protecting its working local foundation, product identity, user data, and trust.
