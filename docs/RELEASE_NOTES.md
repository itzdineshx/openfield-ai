# OpenField Release Notes

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
