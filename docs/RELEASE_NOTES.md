# OpenField Release Notes

## Version 0.9.0 — Phase 3 Release Candidate (RC)

**Release Date:** November 2026

OpenField v0.9.0 represents the feature-complete Release Candidate for Phase 3. It freezes all major feature developments, completes system-wide navigation and accessibility audits, hardens on-device security and data privacy boundaries, and verifies seamless end-to-end workflows on physical hardware:

---

### Highlights & Key Features

#### 1. Navigation, Accessibility & UI Consistency (Gate 9)
- **Unified Navigation Hierarchy:** Structured navigation across all 15 application screens (Home, Ask AI Chat, Image Analysis, Document Q&A, Document Detail, Problem Solver, Learn & Explain, AI Tools, Calculator & Units, Workflows & Checklists, Create & Write, Knowledge Center, Cross-Source Q&A, Settings, About).
- **Reliable Back-Stack Navigation:** Clean back navigation and state retention across every detail screen and bottom sheet.
- **Accessibility & Contrast Compliance:** Complies with WCAG AAA contrast guidelines (>12:1 for body text `#0F172A` on `#F8FAFC`). Minimum 48dp touch targets on all interactive components.

#### 2. Performance, Security, Privacy & Reliability (Gate 10)
- **100% Local On-Device AI:** Gemma 4 E2B IT (`gemma-4-E2B-it.litertlm`) executes exclusively on-device via LiteRT-LM CPU. Zero cloud inference, zero external APIs, zero mock responses.
- **Privacy First by Design:** No telemetry, no prompt logging, no automatic cloud uploads, no background microphone listeners.
- **Non-Destructive Room SQLite Migrations:** Migrations `MIGRATION_1_2` through `MIGRATION_6_7` maintain 100% backward compatibility for all user conversations, documents, collections, and task checklists.

#### 3. Verification & Release Artifacts (Gate 11)
- **Automated Test Suite:** **186/186 unit tests passing (100% green)** across all data layer, business logic, math engine, retrieval optimizer, and migration components.
- **Signed Release Candidate APK:**
  - File: `android/app/build/outputs/apk/release/app-release.apk`
  - Size: 115,003,298 bytes (~109.67 MB)
  - SHA-256: `F65816B8559FB92F18DDA4192B8B0488948BC8755FBD41B3B00163DAB24FBEC8`
  - Physical Device: Verified and tested on LAVA LXX503 (Android 14, ARM64).

---

## Version 0.6.0 — Phase 3 Advanced Personalization, Intelligence Quality & Integrated Productivity Workflows

**Release Date:** November 2026

OpenField v0.6.0 completes the integration of on-device personal memory, advanced retrieval optimization, and end-to-end productivity workflows. Users now experience seamless synergy across learning, diagnostic troubleshooting, research synthesis, and task management, backed by strict offline privacy and zero cloud dependence:

---

### Highlights & Key Features

#### 1. Structured Local Memory & Transparency (`LocalMemoryManager`)
- **Explicit Memory Taxonomy:** Categorizes user-retained preferences across three discrete boundaries:
  - **User Preferences:** Communication style, brevity, tone, and formatting.
  - **Learning Goals:** Active subjects, exam targets, and skill progression.
  - **Domain Context:** Technical specializations, professional focus, and project domains.
- **Full User Sovereignty:** Inspect, edit, filter by category, and delete local memories at any time with immediate effect on derived prompts.
- **Safe Context Injection:** Bounded to prevent CPU prompt bloat while ensuring personalized conversational responses.

#### 2. Advanced Retrieval Optimization (`RetrievalOptimizer`)
- **Offline Query Expansion:** Built-in domain dictionary expands technical, academic, and practical acronyms ("ai", "db", "carb", "ml", "calc") without external embedding models.
- **Heading-Aware Relevance Scoring:** Boosts evidence passages matching section headings (`#`, `##`, `###`) for sharper semantic alignment.
- **Fair-Share Multi-Source Interleaving:** Round-robin interleaving ensures balanced multi-document representation within the ~1,200 token budget, preventing single dense documents from starving the context window.
- **Drafts Ingestion in Unified Search:** Writing drafts and personal notes are now searchable alongside documents and packs in `UnifiedSearchEngine`.

#### 3. Integrated Productivity Workflows (Gate 8)
- **Learning → Task Automation:** One-tap action from any lesson ("Add to Study Tasks") converts structured concepts into an actionable review checklist.
- **Troubleshooting → Maintenance Tasks:** One-tap action from diagnostic resolutions ("Add to Maintenance Tasks") creates actionable repair checklists with safety notices attached.
- **Research → Writer:** Unified citation retention allows cross-source query findings to flow straight into drafting canvas.

#### 4. Extended Personalization Controls
- **Preferred Measurement Units:** Direct toggle between Metric (km, kg, °C) and Imperial (miles, lbs, °F) in Settings with automatic prompt directive injection.
- **Preferred Formatting Style:** Options for Standard, Bullet Points, and Markdown with Headings.
- **Bounded Context Safety (`PromptBuilder.boundContext`):** Automatically limits excessive context payloads to protect mobile CPUs from thermal throttling.

#### 5. Verification & Release Artifacts
- **Automated Test Suite:** **186/186 unit tests passing (100% green)** including retrieval optimization, memory lifecycle, personalized prompt directives, and context bounding.
- **Signed Release APK:**
  - File: `android/app/build/outputs/apk/release/app-release.apk`
  - Size: 115,003,298 bytes (~109.67 MB)
  - SHA-256: `8A3E56124D369A66473D4D62008E708F3736C81E017E60C0544CA1CB411B3854`
  - Target: Android 14+ / ARM64, Unisoc T616 / Snapdragon CPU local execution.

---

## Version 0.5.0 — Phase 3 Practical Offline Intelligence & Task Workflows

**Release Date:** November 2026

OpenField v0.5.0 introduces automated personal task planning, structured workflow execution, and deterministic offline date and data calculation tools. Users can now convert natural-language goals into structured, actionable checklists with progress tracking, while accessing high-reliability offline utilities without internet connectivity or cloud inference:

---

### Highlights & Key Features

#### 1. Personal Task & Workflow Automation (`TaskAutomationScreen` & `TaskDao`)
- **Action-Oriented Goal Breakdown:** Converts high-level user goals into structured, editable action checklists.
- **Natural-Language Task Planning (`PromptBuilder.buildTaskPlanningPrompt`):** Directs the local Gemma 4 model to decompose complex tasks into concrete, prioritized subtasks with categories.
- **Structured Plan Validation (`StructuredOutputParser.parseTaskPlan`):** Validates generated plan schemas, guaranteeing valid titles, descriptions, priorities, and subtask arrays.
- **Interactive Checklist Execution:** Real-time completion toggling, progress bars, task expansion, and deletion controls.
- **Workflow Templates:** One-tap templates for Exam Preparation, Technology Learning, Device Maintenance, and Research Projects.
- **Local Persistence (`TaskItemEntity`):** Persistent task storage in Room SQLite with full offline reliability.

#### 2. Deterministic Date, Time & Calendar Calculator (`DateTimeCalculator`)
- **Zero-LLM Accuracy:** Uses Android `java.time` APIs for deterministic date and calendar arithmetic.
- **Calendar & Business Days:** Calculates exact day differences and excludes weekends for accurate work/business day schedules.
- **Duration Arithmetic:** Precision addition and subtraction of days, months, and years.
- **Future Day-of-Week Resolution:** Deterministic calculations for recurring schedules and upcoming milestones.

#### 3. Structured Data & CSV Table Inspector (`StructuredDataInspector`)
- **Bounded-Memory Offline Parsing:** Robust CSV parsing handling nested quotes, commas, and escaped characters.
- **Statistical Column Analysis:** Automatically detects numeric columns and computes min, max, sum, and average metrics.

#### 4. Room Database v7 Non-Destructive Migration (`MIGRATION_6_7`)
- **Automated Tasks Schema:** Adds `task_items` table with indices on `updatedAt`, `status`, and `priority`.
- **100% Non-Destructive:** Retains all existing conversations, documents, collections, and drafts without data loss.

#### 5. Verification & Release Artifacts
- **Automated Test Suite:** **173/173 unit tests passing (100% green)** including date arithmetic, CSV statistics, task schema parsing, and migration DDL.
- **Signed Release APK:**
  - File: `android/app/build/outputs/apk/release/app-release.apk`
  - Size: 115,003,298 bytes (~109.67 MB)
  - SHA-256: `82764A0E664C7B492228C2360B2E0001594A2DDE10F1CA921DA3C2B8EFD4E621`
  - Target: Android 14+ / ARM64, Unisoc T616 / Snapdragon CPU local execution.

---

## Version 0.4.0 — Phase 3 Context-Aware Personal Assistant & Practical Offline Intelligence

**Release Date:** November 2026

OpenField v0.4.0 delivers seamless cross-workspace continuity and deterministic offline utilities, allowing ongoing user tasks to flow naturally between features without manual re-entry, while introducing high-precision offline calculation and unit conversion tools:

---

### Highlights & Key Features

#### 1. Cross-Workspace Shared Context & Task Sessions (`SharedContextManager`)
- **Seamless Task Handoffs:** Move smoothly across specialized workspaces with pre-loaded context:
  - **Document Q&A → Create & Write:** One tap on "Draft Note" transfers active document citations and summaries into the writing canvas with source references attached.
  - **Image Analysis → Problem Solver:** One tap on "Troubleshoot" transfers visual inspection findings and the captured equipment photo directly into guided diagnostics.
  - **Knowledge Center → Ask AI:** Send multi-source search results and passages into conversational chat.
- **Task Sessions Persistence (`TaskSessionEntity`):** Tracks ongoing multi-step user engagements with Room SQLite persistence, surviving process termination.

#### 2. Deterministic Offline Math Engine & Expression Evaluator (`DeterministicMathEngine`)
- **Zero-LLM Fast Arithmetic:** Standard mathematical expression evaluator built using the Shunting-yard algorithm and Reverse Polish Notation (RPN).
- **Rich Operator & Function Support:** Evaluates addition, subtraction, multiplication, division, exponents (`^`), modulo (`%`), unary negative, parentheses, and functions (`sqrt`, `abs`, `round`).
- **Zero Hallucination Guarantee:** Guaranteed mathematical precision without relying on LLM guesses, network calls, or external dependencies.

#### 3. Deterministic Offline Unit Converter (`UnitConverter`)
- **Multi-Category Conversion:** Offline formulas across 5 key categories:
  - **Length:** Meters, Kilometers, Centimeters, Millimeters, Miles, Yards, Feet, Inches.
  - **Mass & Weight:** Kilograms, Grams, Milligrams, Pounds, Ounces.
  - **Temperature:** Celsius, Fahrenheit, Kelvin.
  - **Digital Storage:** Bytes, KB, MB, GB, TB.
  - **Speed:** m/s, km/h, mph, knots.
- **Instant Two-Way Conversion:** Dynamic swap and real-time formatting.

#### 4. Dedicated Offline Calculator & Units Screen (`CalculatorConverterScreen`)
- **Interactive Keypad & Display:** Clean Material 3 split interface featuring an expression calculator with a physical keypad and a unit converter with category tabs and dropdown pickers.
- **Direct Access from AI Tools:** Prominently featured in the Tools gallery and accessible directly via `AppRoutes.CALCULATOR`.

#### 5. Room Database v6 Non-Destructive Migration (`MIGRATION_5_6`)
- **Task Sessions Schema:** Automatically creates `task_sessions` table with indices on `updatedAt` and `status`.
- **100% Data Integrity:** Non-destructive migration preserving all user documents, collections, conversations, and drafts.

#### 6. Verification & Release Artifacts
- **Automated Test Suite:** **141/141 unit tests passing (100% green)** including math precedence, unit conversions, task handoffs, and migration DDL.
- **Signed Release APK:**
  - File: `android/app/build/outputs/apk/release/app-release.apk`
  - Size: 114,921,382 bytes (~109.6 MB)
  - SHA-256: `BE5A5765939EDD8A01F1B708149171EBCCCDCB384DB177284C0E7A8761F47F29`
  - Target: Android 14+ / ARM64, Unisoc T616 / Snapdragon CPU local execution.

---

## Version 0.3.0 — Phase 3 Unified Personal Knowledge

**Release Date:** November 2026

OpenField v0.3.0 transforms the application from independent specialized workspaces into a unified personal knowledge platform. Users can now organize, discover, and reason across their documents, notes, conversations, and installed knowledge packs with cross-source synthesis, strict evidence verification, and interactive inspection.

---

### Highlights & Key Features

#### 1. Unified Knowledge Center (`KnowledgeCenterScreen`)
- **Complete Library Overhaul:** Replaces the legacy tabbed library with a modern high-density Knowledge Hub featuring:
  - **Metrics Overview:** Real-time visibility into total indexed sources, document count, saved notes, chat sessions, and installed packs.
  - **Semantic Collections Shelf:** Organize related items into user-defined collections with custom titles and color accents.
  - **Multi-Source Selection & Batch Actions:** Select any combination of documents, notes, and packs to launch cross-source queries or batch-manage items.
  - **Quick Ingestion Floating Actions:** 1-tap import for PDFs and text documents.

#### 2. Cross-Source Multi-Document Q&A (`CrossSourceQaScreen` & `CrossSourceRetriever`)
- **Reasoning Across Disparate Sources:** Enables synthesis, comparison, and contrast across multiple documents and knowledge packs simultaneously (e.g. comparing repair guidelines in a pack with error codes in a user manual).
- **Fair-Share Context Budgeting:** Bounded to ~1,200 tokens across sources, guaranteeing fast Time to First Token (< 6s) on mobile CPUs without single-source starvation.
- **Source Attribution Tags:** Enforces precise provenance tags formatted as `[Source: <Title> (Page <P>)]` or `[Source: <Title>]` directly in model generation instructions.
- **Conflict Identification:** Directs Gemma to explicitly call out conflicting recommendations between different sources.

#### 3. Post-Generation Citation Verification (`CitationValidator`)
- **Zero-Tolerance Hallucination Check:** Deterministically verifies that all inline citations in model responses strictly correspond to actual retrieved evidence passages and valid page numbers.
- **Hallucination Flagging:** Flags fabricated sources or non-existent page numbers in the UI, preserving user trust.

#### 4. Evidence Explorer (`EvidenceExplorerSheet`)
- **Radical Transparency:** Expandable Material 3 bottom sheet accessible directly from the Cross-Source Q&A screen.
- **Grounding Passages Inspector:** Lists every exact passage, page number, section, and relevance score supplied to Gemma for the active response.

#### 5. High-Speed Unified Local Search (`UnifiedSearchEngine`)
- **Sub-80ms Lexical Retrieval:** Parallel asynchronous search across SQLite FTS4 document chunks, chat history, writing drafts, and installed knowledge packs.
- **Highlighted Excerpts:** Dynamic snippet generation centered around query match terms with contextual boundary clipping.

#### 6. Room Database v5 Non-Destructive Migration (`MIGRATION_4_5`)
- **Schema Evolution:** Introduces `knowledge_collections` and `collection_source_cross_ref` tables with foreign key cascades and composite primary keys.
- **100% Data Preservation:** Fully automated migration preserves all pre-existing user documents, conversations, notes, packs, and learning sessions without data loss.

#### 7. Verification & Release Artifacts
- **Automated Test Suite:** **126/126 unit tests passing (100% green)** including migration DDL, search ranking, prompt formatting, and citation validation suites.
- **Signed Release APK:**
  - File: `android/app/build/outputs/apk/release/app-release.apk`
  - Size: 114,888,610 bytes (~109.5 MB)
  - SHA-256: `67127CB0B59B4660870E3E5C3C4B45148F281F509DB266FEFFC3B9FF42A2E2D3`
  - Target: Android 14+ / ARM64, Unisoc T616 / Snapdragon / Tensor CPU execution.

---

## Version 0.2.1 — Phase 2.1 Offline Intelligence, Reliable OCR & Real Catalog

**Release Date:** November 2026

OpenField Phase 2.1 hardens the offline-first capabilities of the application by introducing bundled on-device OCR, an extensible local speech recognition abstraction, real knowledge-pack catalog synchronization via FastAPI, and streamlined prompt budgeting:

---

### Highlights & Key Features

#### 1. Fully Offline On-Device OCR (`LocalOcrEngine`)
- **Bundled ML Kit Latin Recognition:** Integrates `com.google.mlkit:text-recognition:16.0.1` containing bundled recognition models directly inside the APK. Requires zero network access, zero Google Play Services, and zero cloud calls.
- **Selective Scanned Page Processing:** Digital text pages are extracted instantly via PDFBox. Scanned pages or pages with `< 30` characters are rendered via Android `PdfRenderer` into memory-bounded bitmaps (max 1600px dimension) and processed by `LocalOcrEngine`.
- **Immediate Resource Cleanup:** Native Bitmaps are explicitly recycled immediately after recognition, preventing heap bloat and Out-Of-Memory errors on 4–6 GB devices.
- **Knowledge Engine Integration:** OCR extracted text preserves exact 1-based page numbers and is chunked into SQLite FTS4, enabling full grounded Q&A with clickable citation cards (`Page X >`).

#### 2. Guaranteed Local Speech Recognition Architecture (`SpeechToTextEngine`)
- **Dedicated Engine Abstraction:** Decoupled speech recognition from the generative LLM pipeline (`AiOrchestrator`).
- **Priority Fallback Chain:** Automatically leverages locally installed acoustic models via `LocalModelSpeechToTextEngine` if present, with seamless fallback to `SystemSpeechToTextEngine` configured with `android.speech.extra.PREFER_OFFLINE = true`.
- **Strict Privacy Lifecycle:** Recording begins only on explicit user tap; mic permission requested lazily; immediate release of audio buffers upon cancellation or completion; zero background listening.

#### 3. Real Knowledge-Pack Catalog & Remote Synchronization
- **FastAPI Backend Integration (`backend/app/main.py`):** Fully implemented REST catalog service exposing `GET /health`, `GET /api/v1/packs`, `GET /api/v1/packs/{pack_id}`, and `POST /v1/downloads/{pack_version_id}/url`.
- **Remote Client (`CatalogApiClient.kt`):** User-initiated catalog refresh with HTTP error handling, connection timeout management, and offline detection.
- **Offline Cache Preservation:** Downloaded and cached catalog entries in Room are preserved during network outages; a failed refresh never wipes the local database.
- **Integrity Validation:** Enforces semantic versioning, SHA-256 checksum verification, and provenance checks.

#### 4. Prompt Optimization & Token Budgeting
- **Streamlined System Directives:** Removed redundant prompt instructions across `PromptBuilder.kt`, reducing template overhead and lowering Time to First Token (TTFT).
- **Quality & Contract Invariants:** Maintained strict JSON contracts for troubleshooting and interactive tutoring while ensuring Ask AI remains purely conversational.

#### 5. Verification & Test Evidence
- **Automated Test Suite:** **110/110 unit tests passing (100% green)** across 16 test suites.
- **Release APK:** `app-release.apk` (114.79 MB, SHA-256: `7AB56552E3078B83DEB93FBFDA57F67200D9384377BCEB9318519FA00D37A190`).
- **Physical Device:** Installed and verified in offline mode on Lava LXX503 (Android 14, 6 GB RAM).

---

## Version 0.2.0 — Phase 2 Mode-Based AI Architecture & Deepened Offline Workspaces

**Release Date:** November 2026

OpenField Phase 2 delivers a complete, task-specific offline AI experience across all application modes, powered exclusively by the on-device Gemma 4 E2B IT model and LiteRT-LM runtime:

---

### Highlights & Key Features

#### 1. Mode-Based Architecture & AI Orchestrator
- **Shared Inference Orchestrator (`AiOrchestrator`):** Central coordinator routing requests to `LocalAiEngine` / `LiteRtLmEngine` with zero duplicate engines and zero cloud dependencies.
- **Typed Mode Configurations (`ModeConfiguration`):** Strongly typed registry (`OpenFieldMode`) configuring system prompts, generation parameters, input requirements, and output formats per mode.
- **Prompt Separation (`PromptBuilder`):** Prompt templates decoupled from presentation composables.
- **Structured Output Parsing & Resilient Validation (`StructuredOutputParser`, `OutputValidator`):** Safe JSON extraction from code fences and raw braces with markdown fallback recovery, preventing crashes or infinite loops.

#### 2. Distinct Mode Workspaces
- **Mode A: Ask AI (Normal Chat):** Natural conversational chat with token streaming, clipboard copy, response regeneration, cooperative stop, and conversation history. Does not emit quizzes or forms.
- **Mode B: Analyze Image:** Vision workspace featuring image preview, replace/remove controls, quick-prompt filters (*Screenshot / Error*, *Diagram*, *Hardware*, *Nature*), downsampling memory safety checks, 6 structured sections (*What I see*, *Important details*, *Explanation*, *Possible identification*, *Suggested next steps*, *Uncertainty & limitations*), and interactive follow-up Q&A on the same image.
- **Mode C: Ask Document (Advanced Knowledge Engine):** Grounded document Q&A backed by heading-aware chunking preserving section context, SQLite FTS4 retrieval, multi-document search scope, document comparison, executive summarization, and verifiable source-page citations.
- **Mode D: Solve a Problem (Intelligent Diagnostics):** Multi-step diagnostic state machine with contextual equipment photo attachment, category classification, safety hazard alerts, selectable observation options (A/B/C/D), diagnostic history, alternative root-cause branching, escalation warnings for high-risk hazards, and fix verification.
- **Mode E: Learn & Explain (Interactive Tutoring):** Level selection (*Beginner*, *Intermediate*, *Advanced*), structured lesson breakdown, intuitive analogies, interactive actions (*Explain More Simply*, *Show Another Example*, *Summarize Topic*), interactive multiple-choice quiz with hints, free-response concept evaluation with constructive AI feedback, and lesson persistence.
- **Mode F: Create & Write (Document Studio):** Dedicated document workspace with intent selectors (*Email*, *Report*, *Assignment*, *Summary*, *Message*, *Rewrite*, *Grammar*, *Tone*), tone chips, editable document canvas, live word/character count, quick refinement actions (*Shorter*, *Professional*, *Fix Grammar*), local export to Markdown (`.md`) and Plain text (`.txt`), and Android System Share (`Intent.ACTION_SEND`).
- **Mode G: AI Tools & Mode H: My Library:** Dedicated tools and library navigation preserved and routed to their respective dedicated experiences.

#### 3. Personalization & Local Memory (Gate 7)
- **User-Controlled Preferences:** Custom explanation levels (Beginner, Intermediate, Advanced), response length preferences (Concise, Balanced, Detailed), and writing tones.
- **Transparent Local Memory:** Dedicated memory inspection sheet allowing users to view, edit, or wipe custom background context and preferences with zero telemetry.

#### 4. Room Database Migration (v3 -> v4)
- **Zero Data Loss:** Clean SQLite migration adding `troubleshoot_sessions`, `learning_sessions`, and `writing_drafts` without modifying existing chat sessions, messages, or documents.

#### 5. Physical Hardware Verification & Edge Performance Metrics
- **Test Device:** Lava LXX503 (Android 14, `arm64-v8a`, Unisoc T616 8-core CPU, Mali-G57 GPU, 6 GB RAM, 128 GB Storage).
- **Model Loading:** Cold model initialization completed in **2,121 ms (2.12 s)** directly from flash storage into system memory on CPU.
- **Inference Latency & Throughput:**
  - Standard Chat TTFT: **4,288 ms**; Generation Speed: **4.8 tok/s**.
  - Multimodal Vision Preprocessing: **93 ms**; Vision Generation Speed: **3.8 tok/s**.
- **Memory & Resource Efficiency:**
  - Sustained app runtime heap: **12 MB – 26 MB / 512 MB** maximum allocation.
  - Available system memory throughout all multi-turn sessions: **>1.3 GB free**.
  - Battery & Thermal stability: Only **3% battery consumed** across the entire 6-mode evaluation suite; peak battery temperature remained steady at **40.2°C** with zero thermal throttling or crashes.
- **Zero Cloud & Strict Privacy:** Verified 100% on-device operation with zero cloud telemetry or external network calls.
- **Automated Test Suite:** 68/68 unit tests passing across JVM and Android target runners.

---

## Version 0.1.0-alpha — Phase 1 High-Fidelity Prototype & Local AI Engine

**Release Date:** November 2026

We are excited to deliver the first functional release of **OpenField**, the open-source, offline-first Android companion for questions, document understanding, and problem solving.

---

### Highlights & Key Features

#### 1. Official Google LiteRT-LM & Gemma 4 E2B IT Integration
- **100% On-Device AI:** Runs natural language and multimodal inference locally without requiring internet connectivity or cloud servers.
- **Exclusive Model Baseline:** Powered exclusively by `gemma-4-E2B-it.litertlm` from the official `litert-community/gemma-4-E2B-it-litert-lm` repository (~2.58 GB).
- **Flexible Hardware Acceleration:** Supports GPU (OpenCL/Vulkan) acceleration with automatic fallback to high-performance multi-threaded CPU execution.
- **Streaming Responses:** Real-time token streaming with cooperative user cancellation.

#### 2. Local Document Intelligence & RAG
- **Offline Document Q&A:** Import PDF or text files via Android Storage Access Framework (SAF).
- **Embedded Text Extraction:** Extracts document contents and segments them into semantic chunks with overlapping boundaries.
- **SQLite FTS Search:** Fast local retrieval referencing specific document pages without cloud vector databases.

#### 3. Guided Problem Solving & Concept Learning
- **Step-by-Step Troubleshooting:** Structured diagnostic flows for hardware, device, and everyday issues.
- **Learn & Explain:** Breaks down complex science and technology concepts into intuitive explanations.
- **AI Utility Suite:** Quick action tools for Summarizing, Explaining, Translating, Rewriting, Extracting Key Points, and Writing/Drafting.

#### 4. Complete 12-Screen Jetpack Compose UI
- **Pixel-Accurate Interface:** Built with Jetpack Compose and Material 3 adhering strictly to the design reference:
  - Splash Screen
  - 3-Step Onboarding Flow
  - Home Dashboard & Quick Prompts
  - AI Chat with streaming text bubbles
  - Image Analysis preview and findings
  - Document Q&A with page reference cards
  - Problem Solver diagnostic sequence
  - Learn & Explain concept view
  - AI Tools catalog
  - My Library and Knowledge Packs
- **Clean Light Palette:** Pure white surfaces, soft slate-blue accents, and dark navy typography.

#### 5. Resilient Build Toolchain
- **Upgraded Stack:** Built with Gradle 9.7.1, Android Gradle Plugin 9.3.1, Kotlin 2.4.21, KSP 2.3.14, and Room 2.7.2.
- **Java 25+ Compatible:** Seamlessly compiles with modern Android Studio JBR.
- **Comprehensive Unit Testing:** Integrated test coverage for Document Chunking and Model Catalog constraints.

---

### Known Limitations in Phase 1
- **Scanned PDFs:** Pure image PDFs require an OCR pre-pass, which is slated for a future milestone.
- **Minimum RAM:** Requires a 64-bit ARM device with at least 4 GB RAM. Devices with 3 GB or less RAM cannot sustain 2B-class local LLM weights.
