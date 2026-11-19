# OpenField --- Phase 2 Roadmap

**Project:** OpenField --- Offline-first Android AI companion\
**Target versions:** v0.2.0--v0.5.0\
**Status:** Proposed roadmap\
**Model:** Gemma 4 E2B IT\
**Runtime:** Google LiteRT-LM\
**Platform:** Native Android (Kotlin + Jetpack Compose)\
**Persistence:** Room / SQLite

## 1. Phase 2 Vision

Phase 1 established the offline foundation: local Gemma inference,
conversational chat, persistence, image and document foundations, local
retrieval, guided workflows, utilities, and the knowledge library.

Phase 2 turns these capabilities into distinct experiences. OpenField
should no longer feel like one chatbot with different buttons. Each mode
should have its own interaction model, state, presentation, and
validation while sharing the existing local AI engine.

**Product goal:** Make OpenField a dependable offline AI companion for
asking, learning, analysing, troubleshooting, researching, and creating.

### Product principles

1.  **Offline-first:** Core AI features work without internet access.
2.  **Single-model architecture:** Keep Gemma 4 E2B IT as the only AI
    model unless explicitly changed by a future decision.
3.  **Preserve Phase 1:** Extend stable functionality; avoid unnecessary
    rewrites.
4.  **Mode-specific experiences:** Each mode has a purpose-built UI and
    interaction flow.
5.  **Shared inference:** All modes reuse one inference engine and
    shared orchestration.
6.  **Grounded answers:** Document answers must be supported by
    retrieved content and accurate page references.
7.  **Honest capability boundaries:** Distinguish implemented, tested,
    and device-verified features.
8.  **Privacy by design:** Keep core AI processing and user content
    on-device.

## 2. Phase 1 Baseline and Phase 2 Boundary

Phase 1 is treated as functionally complete at the prototype/roadmap
level, based on the project's completion report and device tests. Phase
2 must preserve:

-   Gemma 4 E2B IT local inference through LiteRT-LM.
-   Normal conversational chat and streaming.
-   Room conversation persistence.
-   Offline operation.
-   Existing image-analysis pipeline.
-   PDF import and extraction.
-   SQLite FTS retrieval and source-page references.
-   Existing guided troubleshooting and Learn & Explain foundations.
-   Existing AI utilities and Library.
-   Home, Chats, Tools, and Library navigation.
-   Existing visual design language.

A successful build or unit test does not prove device-only behaviour.
Record build status, automated test status, and physical-device
verification separately.

## 3. Phase 2 Experience Map

  -----------------------------------------------------------------------
  Experience                          Intended behaviour
  ----------------------------------- -----------------------------------
  **Ask AI**                          Conventional conversational
                                      assistant with natural streaming
                                      and follow-up context.

  **Analyze Image**                   Visual workspace with image
                                      preview, image-grounded analysis,
                                      findings, uncertainty, and
                                      follow-ups.

  **Ask Document**                    Document workspace with import,
                                      extraction/indexing status,
                                      grounded Q&A, and source-page
                                      references.

  **Solve a Problem**                 Guided diagnostic flow with
                                      questions, selectable answers,
                                      progress, next steps, and
                                      resolution checks.

  **Learn & Explain**                 Interactive tutor with learning
                                      levels, lesson sections, examples,
                                      knowledge checks, feedback, and
                                      progress.

  **Create & Write**                  Editable writing workspace for
                                      drafting, rewriting, summarising,
                                      translating, and improving content.

  **AI Tools**                        Task-oriented catalogue that opens
                                      the correct dedicated workflow for
                                      each utility.

  **My Library**                      Local knowledge packs and user
                                      documents, with search, indexing
                                      status, and management controls.
  -----------------------------------------------------------------------

Keep Home, Chats, Tools, and Library navigation consistent unless the
product specification explicitly approves a change.

## 4. Milestone Roadmap

### Milestone 2.1 --- Mode-Based AI Architecture

**Priority:** Critical · **Target:** v0.2.0

**Purpose:** Establish an architecture where each mode behaves
differently without creating separate AI engines.

**Scope** - Audit existing screens, navigation, ViewModels,
repositories, AI calls, Room entities, and feature implementations. -
Define strongly typed mode identifiers and mode configuration. -
Introduce or refine shared AI orchestration. - Separate prompt
construction from UI code. - Establish mode-specific controllers or
equivalent feature coordinators. - Define typed request/response
contracts where structured interaction is needed. - Add robust parsing
and validation for structured model output. - Keep normal chat as
natural conversational streaming. - Preserve feature-specific state and
session identifiers. - Route each mode to its dedicated screen and
renderer. - Reuse Phase 1 implementations instead of duplicating them. -
Add regression tests for chat, persistence, RAG, and navigation.

**Logical architecture**

-   **Presentation:** Dedicated screens, composables, mode-specific
    response components, and loading/empty/error/retry states.
-   **Feature layer:** Chat, image-analysis, document-Q&A,
    troubleshooting, learning, and writing controllers.
-   **Shared AI layer:** Inference abstraction, prompt construction,
    mode configuration, context assembly, streaming, cancellation,
    parsing, validation, and error mapping.
-   **Existing infrastructure:** LiteRT-LM, Gemma 4 E2B IT, Room,
    repositories, PDF extraction, FTS retrieval, image preprocessing,
    and model management.

**Acceptance criteria** - Ask AI remains a normal chatbot. - Each
feature opens its intended interaction experience. - All modes reuse the
same local inference engine. - No cloud inference or second model is
introduced. - Existing Phase 1 data and functionality are preserved. -
Structured-output failures are handled safely. - Room data is not
destroyed. - Unit tests pass, a debug APK builds, and device
verification confirms no Phase 1 regressions.

### Milestone 2.2 --- Interactive Learn & Explain

**Priority:** Critical · **Target:** v0.2.x

**Purpose:** Turn the learning foundation into an interactive offline
tutor.

**Scope** - Topic entry and topic suggestions. - Beginner, intermediate,
and advanced learning levels. - Structured lesson generation. - Short,
ordered lesson sections. - Concept explanations, definitions, examples,
and analogies. - Knowledge checks after concepts. - Multiple-choice
quizzes. - Hints and answer explanations. - Feedback for correct and
incorrect answers. - Follow-up questions within a lesson. - Lesson
completion and revision summary. - Resume unfinished lessons. - Local
progress tracking and history.

**Learning flow**

`Topic → Level → Lesson outline → Concept → Example → Knowledge check → Feedback → Next concept → Revision`

Persist lesson identity, topic, level, sections, question state,
submitted answers, completion state, and timestamps. Reopening a lesson
should not regenerate it unnecessarily.

**Acceptance criteria** - Learn & Explain is not rendered as a generic
chat transcript. - Learners progress through sections and answer
interactive questions. - Answers receive meaningful explanations. -
Progress survives app restart. - Invalid generated lesson or quiz data
is validated and handled. - All generation remains on-device.

### Milestone 2.3 --- Intelligent Problem Solver

**Priority:** High · **Target:** v0.2.x

**Purpose:** Evolve troubleshooting into a guided, stateful diagnostic
assistant.

**Scope** - Understand and categorise a reported problem. - Ask one
relevant diagnostic question at a time. - Render selectable answers and
support free-text answers. - Generate the next step based on previous
answers. - Maintain structured diagnostic state and progress. - Explain
why a diagnostic step is requested. - Present possible causes with
calibrated uncertainty. - Suggest safe next actions. - Ask whether a
solution worked. - Provide a final diagnostic summary. - Save and resume
sessions.

**Initial categories** - Computer and laptop issues. - Android device
issues. - Software and application errors. - Network and connectivity
problems. - General technical troubleshooting. - Everyday practical
problem-solving where safe.

**Safety requirements** - Never present uncertain causes as confirmed
diagnoses. - Include warnings for electrical, battery, hardware, or
physical risks. - Do not instruct users to perform dangerous repairs. -
Recommend qualified professional help when needed.

**Acceptance criteria** - The experience is a guided wizard, not a long
chatbot answer. - Each answer influences the next step. - Session state
is preserved. - Users can go back, cancel, resume, and finish. -
Generated steps are validated before display.

### Milestone 2.4 --- Advanced Multimodal Intelligence

**Priority:** High · **Target:** v0.3.0

**Purpose:** Improve image understanding and investigate practical
offline voice interaction.

**Image-analysis scope** - Camera capture and gallery selection. - Image
preview and replacement. - User question attached to the image. -
Verified delivery of actual image content to the model's multimodal
inference path. - Screenshot and error-message explanation. - Object and
scene description. - Diagram and chart explanation where supported. -
Visible text understanding. - Plant/nature identification with
uncertainty. - Multiple-image comparison if supported by runtime and
memory budget. - Follow-up questions retaining relevant image context. -
Clear limitations and safety messaging.

**Voice investigation** - Verify the exact Gemma artifact, LiteRT-LM
Android API, and supported preprocessing path before implementing model
audio input. - Investigate speech-to-text input, speech translation,
spoken prompts, and offline text-to-speech. - Document which component
performs each operation. Do not describe Android speech recognition as
native model audio understanding.

**Acceptance criteria** - Image prompts use real image content, not
filenames or placeholder descriptions. - Image analysis is tested on the
target device. - Unsupported image/audio paths fail clearly. - Voice
capabilities are labelled accurately. - Memory, latency, and thermal
behaviour are measured.

### Milestone 2.5 --- Advanced Local Knowledge Engine

**Priority:** High · **Target:** v0.3.x

**Purpose:** Improve existing PDF and FTS retrieval without replacing
its working foundation.

**Scope** - Review chunking quality and retrieval relevance. - Consider
heading-aware and structure-aware chunking. - Preserve document identity
and page metadata. - Improve query normalisation and retrieval. -
Consider query rewriting where useful. - Rank retrieved passages and
remove irrelevant duplicates. - Assemble bounded context for Gemma. -
Support grounded follow-up questions. - Support multi-document
questions. - Generate document and chapter summaries. - Extract key
points and definitions. - Compare information across documents. - Search
within Knowledge Packs. - Show source passages and page references. -
Handle missing or insufficient evidence honestly.

**Grounding rules** - Never invent page numbers or source passages. -
Distinguish retrieved evidence from model explanation. - State when
evidence is insufficient. - Do not claim a document supports a statement
unless relevant content was retrieved. - Keep retrieval and inference
offline.

**Acceptance criteria** - Existing RAG behaviour does not regress. -
Tests cover relevant, irrelevant, and insufficient-evidence cases. -
Page references remain accurate. - Multi-document results identify their
source documents. - Large documents do not cause unbounded memory use.

### Milestone 2.6 --- Create & Write Workspace

**Priority:** Medium · **Target:** v0.4.0

**Purpose:** Turn writing utilities into an editable content workspace.

**Scope** - Email and message drafting. - Assignments and reports. -
Project descriptions and summaries. - Professional and casual writing. -
Grammar and clarity improvement. - Rewrite and reframe. - Tone
adjustment. - Expand and shorten. - Translation and structured
extraction. - Editable generated content. - Copy, save, reopen, and
revise. - Export to suitable local formats such as TXT, Markdown, and
PDF where implemented.

**UX requirements** - Use a document-like editor, not a chat bubble as
the primary output. - Preserve the user's original input when
revising. - Make generated content editable. - Distinguish generation
from user edits. - Save drafts locally.

**Acceptance criteria** - Users can generate, edit, save, reopen, and
revise content. - Drafts remain available offline. - Exported files are
valid and readable. - Writing tools use shared AI orchestration.

### Milestone 2.7 --- Personalization and Local Memory

**Priority:** Medium · **Target:** v0.4.x

**Purpose:** Improve continuity while keeping information under user
control.

**Scope** - User-selected preferred language. - Preferred explanation
level. - Optional response-style preferences. - Resume learning
sessions. - User-created notes. - Conversation summaries. - Searchable
conversation history. - Relevant local context retrieval. -
User-controlled saved memory. - Review, edit, and delete memory. - Clear
retention and deletion behaviour.

**Privacy requirements** - Keep memory local. - Do not silently infer
sensitive personal details. - Separate temporary conversation context
from persistent user-controlled memory. - Explain what is saved and
why. - Provide controls to inspect and delete saved information. - Do
not send memory to the optional backend.

**Acceptance criteria** - Preferences persist locally. - Users can
inspect and remove saved memory. - Conversation search returns relevant
local results. - Memory does not unexpectedly override the current
request. - Deletion removes relevant stored data and derived indexes.

### Milestone 2.8 --- Performance, Reliability, and Release Readiness

**Priority:** Critical · **Target:** v0.5.0

**Performance** - Model load time. - Time to first token. - Token
generation throughput. - Peak and sustained memory use. - CPU/GPU
stability. - Image preprocessing memory. - Large-document processing. -
Database query performance. - App launch and screen-transition
responsiveness. - Battery and thermal behaviour.

**Reliability** - Cancellation during generation. - App
background/foreground transitions. - Process recreation. - Interrupted
model loading. - Interrupted document indexing. - Database migration and
recovery. - Corrupt or unsupported files. - Low-storage and low-memory
handling. - Structured-output parsing failures. - Offline error
handling.

**Quality and release** - Unit, repository, database, ViewModel/state,
and critical UI tests. - Device tests on the supported hardware
matrix. - Accessibility checks. - Privacy and security review. -
Documentation and release notes. - APK/AAB build verification as
appropriate.

**Acceptance criteria** - No critical data-loss or crash defects
remain. - Core features work without internet. - Performance is measured
on the minimum supported device tier. - Device-only claims have
device-test evidence. - Release documentation lists known limitations
accurately.

## 5. Release Plan

  -----------------------------------------------------------------------
  Release                             Main deliverables
  ----------------------------------- -----------------------------------
  **v0.2.0 --- Experience             Mode-based architecture, dedicated
  Foundation**                        mode routing, interactive Learn &
                                      Explain, improved troubleshooting.

  **v0.3.0 --- Multimodal             Advanced image analysis, verified
  Intelligence**                      voice paths, improved document
                                      understanding, and multi-document
                                      RAG.

  **v0.4.0 --- Personal               Create & Write workspace, local
  Productivity**                      personalization, memory controls,
                                      and conversation search.

  **v0.5.0 --- Release Readiness**    Performance, reliability,
                                      compatibility, privacy review, and
                                      release candidate.
  -----------------------------------------------------------------------

These are planning targets, not claims that milestones are already
implemented.

## 6. Cross-Cutting Engineering Requirements

### AI model and inference

-   Keep Gemma 4 E2B IT as the only model.
-   Preserve the working LiteRT-LM integration.
-   Keep inference on-device.
-   Reuse the shared engine.
-   Keep natural streaming for normal chat.
-   Use structured output only where a feature requires it.
-   Validate model-generated structured data.
-   Bound context and output sizes according to device capacity.
-   Handle cancellation and errors consistently.

### Android architecture

-   Continue using Kotlin and Jetpack Compose.
-   Follow existing package and naming conventions.
-   Reuse repositories and components.
-   Use lifecycle-aware state collection.
-   Keep expensive work off the main thread.
-   Use Room migrations for schema changes.
-   Avoid storing large binary assets directly in Room.
-   Preserve app-private file handling and Storage Access Framework
    permissions.

### UI and design

-   Preserve the current OpenField light visual language.
-   Use white/light surfaces and blue primary actions.
-   Maintain readable typography and clear hierarchy.
-   Use consistent spacing, icons, and rounded cards.
-   Make each mode visually and behaviourally distinct.
-   Avoid neon colours, excessive gradients, decorative orbs, and
    unnecessary animations.
-   Provide useful loading, empty, error, retry, and offline states.

### Privacy and offline operation

-   No cloud AI inference.
-   No Firebase dependency.
-   No external AI API dependency.
-   No prompt or content telemetry.
-   Keep user content on-device for core features.
-   Treat the optional pack catalog backend as non-essential.
-   Test critical workflows in airplane mode.

## 7. Definition of Done for Every Milestone

A milestone is not complete merely because the project compiles.

For each milestone:

1.  Inspect existing code before editing.
2.  Document the implementation plan and affected files.
3.  Reuse existing working functionality.
4.  Implement the smallest coherent change.
5.  Add or update automated tests.
6.  Run relevant Gradle test tasks.
7.  Build the debug APK.
8.  Install and test on a physical Android device.
9.  Verify offline behaviour where applicable.
10. Check regressions in chat, RAG, persistence, and navigation.
11. Update documentation and `docs/ROADMAP.md`.
12. Record limitations and distinguish implemented, automated-tested,
    and device-verified status.

Suggested baseline commands:

``` powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Use the project's configured test tasks if the current Gradle setup
differs.

## 8. Phase 2 Progress Tracker

Update this table as work is completed. Do not mark items complete
without evidence.

  Milestone                                             Status        Evidence / Notes
  ----------------------------------------------------- ------------- ------------------
  2.1 Mode-Based AI Architecture                        Not started   
  2.2 Interactive Learn & Explain                       Not started   
  2.3 Intelligent Problem Solver                        Not started   
  2.4 Advanced Multimodal Intelligence                  Not started   
  2.5 Advanced Local Knowledge Engine                   Not started   
  2.6 Create & Write Workspace                          Not started   
  2.7 Personalization and Local Memory                  Not started   
  2.8 Performance, Reliability, and Release Readiness   Not started   

### Status definitions

-   **Not started:** No implementation work has begun.
-   **In progress:** Implementation is underway.
-   **Implemented:** Code is present, but verification may be
    incomplete.
-   **Automated-tested:** Relevant automated tests pass.
-   **Device-verified:** Required behaviour has been tested on a
    physical device.
-   **Completed:** Acceptance criteria are met and evidence is recorded.
-   **Blocked:** Work cannot proceed because of a documented dependency
    or issue.

## 9. Recommended Immediate Execution Order

1.  Complete Milestone 2.1: Mode-Based AI Architecture.
2.  Complete Milestone 2.2: Interactive Learn & Explain.
3.  Complete Milestone 2.3: Intelligent Problem Solver.
4.  Complete Milestone 2.4: Advanced Multimodal Intelligence.
5.  Complete Milestone 2.5: Advanced Local Knowledge Engine.
6.  Complete Milestone 2.6: Create & Write Workspace.
7.  Complete Milestone 2.7: Personalization and Local Memory.
8.  Complete Milestone 2.8: Performance, Reliability, and Release
    Readiness.

Milestones may share infrastructure, but implementation should remain
incremental. Do not start a broad rewrite or attempt all milestones in
one coding-agent task.

## 10. Phase 2 Success Definition

Phase 2 succeeds when OpenField provides a coherent set of distinct
offline AI experiences:

-   Ask AI feels like a dependable conversational assistant.
-   Analyze Image provides real image-grounded understanding.
-   Ask Document provides evidence-based answers from local files.
-   Solve a Problem guides users through a stateful diagnostic process.
-   Learn & Explain behaves like an interactive personal tutor.
-   Create & Write provides a useful editable writing workspace.
-   AI Tools open task-specific experiences.
-   My Library manages local knowledge and documents.
-   Personalization is transparent and user-controlled.
-   The application remains reliable on its intended Android device
    tiers.

**Final product principle:** One local AI engine. Multiple intelligent
experiences. User data stays on the device. No feature is declared
complete without verification.
