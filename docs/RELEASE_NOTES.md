# OpenField Release Notes

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
