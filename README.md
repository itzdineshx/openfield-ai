<div align="center">

# 🌿 OpenField

### **The Private, 100% Offline-First Android AI Companion**
*Useful intelligence should never depend on an internet connection or a paid cloud subscription.*

---

[![Download APK](https://img.shields.io/github/v/release/itzdineshx/openfield-ai?label=Download%20APK&logo=android&color=1E88E5&style=for-the-badge)](https://github.com/itzdineshx/openfield-ai/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/itzdineshx/openfield-ai/android-ci.yml?branch=master&label=Android%20CI&logo=github&style=for-the-badge)](https://github.com/itzdineshx/openfield-ai/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/itzdineshx/openfield-ai?style=for-the-badge&color=FFD700)](https://github.com/itzdineshx/openfield-ai/stargazers)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=for-the-badge)](CONTRIBUTING.md)

<br/>

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%E2%80%9335)-3DDC84?logo=android&logoColor=white)](docs/SUPPORTED_DEVICES.md)
[![Model](https://img.shields.io/badge/On--Device%20Model-Gemma%204%20E2B%20IT-4285F4?logo=google&logoColor=white)](https://ai.google.dev/)
[![Engine](https://img.shields.io/badge/Inference%20Engine-Google%20LiteRT--LM-FF6F00)](https://ai.google.dev/edge/litert)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device%20%7C%20Zero%20Telemetry-success)](docs/PRIVACY.md)

<br/>

**[Download Latest APK](https://github.com/itzdineshx/openfield-ai/releases/latest)** •
**[Explore Features](#-core-workspaces--capabilities)** •
**[Architecture](#-system-architecture)** •
**[Documentation](#-documentation)** •
**[Contributing](#-contributing)**

<br/>

<img src="docs/ui.png" alt="OpenField Complete User Interface Overview" width="96%" />

</div>

---

## 💡 Why OpenField?

Every major AI assistant today—ChatGPT, Claude, Copilot, Gemini—requires an active internet connection, a registered account, and silent transmission of your personal queries, documents, and photos to remote corporate data centers.

**OpenField changes that.**

OpenField brings state-of-the-art **on-device language modeling (Gemma 4 E2B IT)** directly to your Android device via **Google LiteRT-LM**. Everything executes on your phone's processor. No cloud servers, no API tokens, no monthly subscriptions, and zero data leakage.

Whether you are:
- ✈️ **Traveling in Airplane Mode** with no cellular coverage,
- 🌲 **Off-grid or in remote field environments** repairing electrical systems or machinery,
- 🏥 **Handling confidential documents** that legally cannot leave your device,
- 🎓 **Studying or revising textbooks** without internet distractions,
- 🛡️ **Seeking absolute privacy** without companies training on your personal notes...

**OpenField is your autonomous, always-ready intelligence companion.**

---

## ⚖️ OpenField vs. Cloud AI Assistants

| Feature | ☁️ Cloud AI Assistants (ChatGPT, Claude, etc.) | 🌿 OpenField (100% On-Device) |
|---|---|---|
| **Internet Dependency** | ❌ Mandatory (Fails completely without connectivity) | ✅ **100% Offline (Works in airplane mode & remote areas)** |
| **Privacy & Data Storage** | ❌ Prompts, images & documents sent to remote servers | ✅ **Local-Only (Stays in device app-private SQLite storage)** |
| **Telemetry & Tracking** | ❌ Analytics, device fingerprints, and prompt logs | ✅ **Zero Telemetry (No trackers, no ads, no logging)** |
| **Recurring Cost** | ❌ \$20/month subscriptions or per-token API fees | ✅ **100% Free & Open Source (Apache 2.0)** |
| **Document Q&A (RAG)** | ❌ Uploads PDFs to third-party cloud servers | ✅ **Local SQLite FTS4 indexing & on-device citations** |
| **Offline Knowledge Packs** | ❌ Not available | ✅ **Downloadable & Sideloadable domain bundles (.zip)** |
| **Speed in Dead Zones** | ❌ Infinite loading spinner | ✅ **Deterministic, instant on-device generation** |

---

## ✨ Core Workspaces & Capabilities

OpenField rejects the "single generic chat transcript" pattern. Each capability is backed by a tailored workspace and dedicated prompt contracts:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        OpenField Workspace Suite                       │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│    💬 Ask AI     │ 📄 Ask Document  │ 📷 Analyze Image │ 🛠️ Solve Prob │
│ Conversational   │ Local Grounded   │ On-Device Vision │ Diagnostic    │
│ Streaming Chat   │ PDF RAG Engine   │ Camera Snapping  │ State Machine │
├──────────────────┼──────────────────┼──────────────────┼───────────────┤
│ 🎓 Learn & Tut   │ ✍️ Create & Write│ 📚 My Library    │ 🔢 AI Tools   │
│ Adaptive Lessons │ Document Studio  │ 4-Section Hub &  │ Math Engine & │
│ Quizzes & Cards  │ Markdown Export  │ Sideloading      │ Unit Converter│
└──────────────────┴──────────────────┴──────────────────┴───────────────┘
```

### 1. 💬 Ask AI — Conversational Intelligence
- Natural on-device conversation powered by Gemma 4 E2B IT.
- Real-time token streaming with smooth cursor animation.
- Instant cooperative cancellation (stop generation at any token).
- **Assistant Action Row:**
  - 📋 **Copy:** Clean markdown copied to clipboard.
  - 🔖 **Bookmark / Save Answer:** 1-tap save into **My Library** (`saved_answers`) with active state toggle.
  - 🔊 **Read Aloud (TTS):** Native Android text-to-speech audio narration with play/stop toggle.
  - ↗️ **Share:** Standard Android `Intent.ACTION_SEND` chooser to send answers to WhatsApp, Notes, Email, etc.

### 2. 📄 Ask Document — Local Grounded Q&A (RAG)
- Import personal PDFs and text notes via Android Storage Access Framework (SAF).
- Asynchronous local text extraction preserving page and section boundaries via PDFBox Android.
- Local SQLite FTS4 full-text search index.
- Retrieval-augmented grounded generation citing exact page numbers.
- **Evidence Explorer:** Expandable sheet showing exact source passages and text excerpts used by Gemma 4.
- Document comparison and executive summary generation.

### 3. 📷 Analyze Image — Multimodal Vision
- Snap photos directly via in-app camera (`FileProvider`) or pick from device gallery.
- Local dimension validation and aspect ratio preservation.
- On-device visual understanding: object observation, problem diagnosis, and structured interpretations.
- Contextual follow-up questions grounded in the captured image.

### 4. 🛠️ Solve a Problem — Guided Diagnostic Workflow
- Stateful diagnostic engine: `Draft` → `Clarifying` → `StepActive` → `AwaitingObservation` → `Completed`.
- Breaks complex mechanical, software, or household issues into atomic, safe steps.
- Highlights precautions, tool requirements, and expected observations for every action.
- Adaptive branching: tap *Yes*, *No*, or *Not sure* to update hypotheses and recalculate diagnostic paths.

### 5. 🎓 Learn & Explain — Interactive Tutor
- Select your learning level: **Beginner**, **Intermediate**, or **Advanced**.
- Structured lesson outlines with core principles, analogies, and real-world examples.
- Interactive multiple-choice knowledge checks with hints, explanations, and score tracking.
- Interactive flip flashcards for rapid spaced-repetition revision.

### 6. ✍️ Create & Write — Document Studio
- Purpose-built drafting workspace for emails, reports, project summaries, and notes.
- In-place transformations: *Rewrite*, *Shorten*, *Expand*, *Fix Grammar*, *Adjust Tone* (Professional, Friendly, Technical).
- Export directly to local Markdown (`.md`) or Plain Text (`.txt`) files on device storage.

### 7. 📚 My Library — 4-Section Knowledge Hub
- **Knowledge Packs:** Pre-curated, verified offline domain guides (Linux system administration, electronics soldering/multimeter repair, classical mechanics, periodic table chemistry, Kotlin concurrency, and linguistics).
- **My Documents:** Document manager showing extraction status, index health, and page count.
- **Saved Answers:** Starred answers and snippets with markdown preview and 1-tap deletion.
- **Downloads & Sideloading:** Live download status with **"Import Pack File (.zip) / Sideload"** supporting SAF and SD cards.

### 8. 🔢 AI Utilities & Tools
- **Deterministic Scientific Math Engine:** Evaluate expressions offline without LLM hallucinations ($P = I^2 \cdot R$, trigonometry, logarithm, exponentials).
- **Fluid Unit Converter:** Length, mass, temperature, speed, data, and energy conversions with spring animations.
- Offline text utilities: *Summarize*, *Explain*, *Translate*, and *Extract Structured Data*.

---

## 🏛️ System Architecture

OpenField implements modern Android Clean Architecture with unidirectional data flow (UDF), isolated domain controllers, and strict separation between presentation, local retrieval, and local inference.

<p align="center">
  <img src="docs/architecture.png" alt="OpenField Architecture Diagram" width="96%" />
</p>

### Architectural Layers:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                              │
│  Jetpack Compose • Material 3 Light Theme • StateFlow • CoroutineScope │
├────────────────────────────────────────────────────────────────────────┤
│                    DOMAIN / ORCHESTRATION LAYER                        │
│  ChatController • DocumentQaController • CrossSourceQaController       │
│  ProblemSolverUseCase • LearningController • SharedContextManager      │
├────────────────────────────────────────────────────────────────────────┤
│                     LOCAL RETRIEVAL ENGINE                             │
│  SQLite FTS4 • PDFBox Android Parser • CrossSourceRetriever            │
├────────────────────────────────────────────────────────────────────────┤
│                    LOCAL AI INFERENCE ENGINE                           │
│  Google LiteRT-LM • gemma-4-E2B-it.litertlm (CPU execution)            │
├────────────────────────────────────────────────────────────────────────┤
│                       PERSISTENCE LAYER                                │
│  Room Database (v8) • App-Private Internal Storage • FileProvider      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 System Workflow & Lifecycles

<p align="center">
  <img src="docs/workflow.png" alt="OpenField Workflow Diagram" width="96%" />
</p>

### 1. Document RAG Pipeline
```
[User Selects PDF] ──> [SAF Picker] ──> [PdfTextExtractor]
                                                │
                                    (Preserves Page Bounds)
                                                ▼
[Gemma 4 Output] <── [Grounded Prompt] <── [SQLite FTS4 BM25 Search]
        │
        └──> [Tappable Page Citations] + [Evidence Explorer]
```

### 2. Knowledge Pack Distribution Pipeline ($0 Cloud Cost)
```
                                 [GitHub Releases CDN]
                                           │
                       Download OTA        │   or Sideload from SD Card
                   (HTTP 301/302 Redirect) │   (Storage Access Framework)
                                           ▼
                                [PackZipInstaller]
                                 ├── SHA-256 Checksum Validation
                                 ├── Zip-Slip Path Traversal Check
                                 └── Atomic Room SQLite Extraction
                                           │
                                           ▼
                            [knowledge_pack_articles Table]
                                           │
                                           ▼
                           [Cross-Source Grounded RAG & Reader]
```

---

## 🧰 Technology Stack

| Component | Technology | Version | Description |
|---|---|---|---|
| **OS Platform** | Android | 8.0+ (API 26–35) | Native Android runtime |
| **Language** | Kotlin | 2.4.21 | Coroutines, Flow, Structured Concurrency |
| **UI Framework** | Jetpack Compose | 2024.10.01 BOM | Declarative UI, Material 3 Light Theme |
| **Local AI Engine** | Google LiteRT-LM | 0.19.1 | Low-latency on-device LLM runtime |
| **Local AI Model** | Gemma 4 E2B IT | `litertlm` artifact | 2.58 GB quantized instruction-tuned model |
| **Local Database** | Room over SQLite | 2.7.2 | Room v8 with FTS4 full-text search |
| **Document Parsing**| PDFBox Android | 2.0.27.0 | Page-aware PDF text extraction |
| **Vision / OCR** | ML Kit Text Recognition| 16.0.1 | 100% on-device offline OCR |
| **Speech Audio** | Android Native Speech | API 26+ | `SpeechRecognizer` (STT) & `TextToSpeech` (TTS) |
| **Background Work** | AndroidX WorkManager | 2.9.1 | Durable background tasks & pack downloads |
| **Build & Tooling** | Android Gradle Plugin | 9.3.1 (Gradle 9.7.1) | ProGuard, KSP 2.3.14, Lint Vital |

---

## 🚀 Quick Start & Installation

### Option 1: Install Pre-Built APK (Ready to Use)
1. Download the latest release APK from **[GitHub Releases](https://github.com/itzdineshx/openfield-ai/releases/latest)**:
   - File: `app-release.apk`
2. Open the APK on your Android device (ensure "Install from unknown sources" is enabled).
3. On first launch, download or sideload the **Gemma 4 E2B IT** model (~2.58 GB) via the Model Setup screen.

### Option 2: Build from Source

#### Prerequisites:
- Android Studio (Ladybug / Meerkat or newer)
- JDK 17 or JDK 21 (configured in Android Studio)
- Android SDK Platform 35
- Physical device with 6 GB+ RAM (ARM64 recommended)

```powershell
# 1. Clone the repository
git clone https://github.com/itzdineshx/openfield-ai.git
cd openfield-ai/android

# 2. Run unit tests (202 tests)
.\gradlew.bat testDebugUnitTest

# 3. Run Android Lint verification
.\gradlew.bat lintDebug

# 4. Assemble Release APK
.\gradlew.bat :app:assembleRelease

# 5. Install on connected phone via ADB
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\release\app-release.apk"
```

---

## 📦 Packaging Custom Knowledge Packs

You can package your own domain guides into standardized OpenField Knowledge Packs for free distribution:

```powershell
python scripts/package_knowledge_packs.py
```
This generates `.zip` bundles in `catalog/packs/` and updates `catalog/packs.json` with SHA-256 integrity hashes.

---

## 🔒 Privacy & Security Commitments

- 🚫 **No Silent Uploads:** Zero transmission of user prompts, conversations, images, documents, or audio.
- 🛡️ **Zip-Slip Protected:** Safe extraction rejecting relative path traversals (`..`) or absolute paths.
- 🔐 **SHA-256 Verified:** Strict cryptographic integrity checking for all model artifacts and knowledge packs.
- 🧹 **Factory Reset Guarantee:** Transparent **"Clear All OpenField Data"** setting that purges all SQLite databases and user files while preserving the downloaded model weights.

---

## 🤝 Community & Contributing

We welcome contributions from developers, researchers, technical writers, and designers around the world!

- 🐛 **Report a Bug:** [Open an Issue](https://github.com/itzdineshx/openfield-ai/issues/new?template=bug_report.md)
- 💡 **Request a Feature:** [Submit an Idea](https://github.com/itzdineshx/openfield-ai/issues/new?template=feature_request.md)
- 📖 **Contribution Guidelines:** Read our comprehensive **[CONTRIBUTING.md](CONTRIBUTING.md)** guide.
- 💬 **Code of Conduct:** Please treat all contributors with respect and kindness.

---

## ⭐ Support OpenField

If you believe in **offline-first, privacy-preserving AI for everyone**, please give this repository a ⭐ on GitHub! It helps more developers, travelers, and field workers discover the project.

[![Star History Chart](https://api.star-history.com/svg?repos=itzdineshx/openfield-ai&type=Date)](https://star-history.com/#itzdineshx/openfield-ai&Date)

---

## 📄 License

OpenField is licensed under the **[Apache License, Version 2.0](LICENSE)**.  
Third-party model weights (Gemma 4) and inference runtimes (Google LiteRT-LM) are subject to Google's terms and licenses.
