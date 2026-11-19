# OpenField

**Knowledge should never depend on connectivity.**

OpenField is an open-source, offline-first Android AI companion for everyday questions, learning, document understanding, image analysis, and guided problem solving. The Phase 1 goal is to keep core AI experiences local and private by default.

> **Project status:** Active Development / Phase 2 (v0.2.0). Mode-Based AI Architecture, local multimodal reasoning, interactive tutoring, diagnostic branching, multi-document retrieval, and local export are implemented and verified offline with Google LiteRT-LM and Gemma 4 E2B IT.

## Phase 2 Core Workspaces & Capabilities

- **Ask AI:** Conversational chatbot with streaming text, response regeneration, copy, and cooperative stop.
- **Analyze Image:** Dedicated multimodal workspace with image replace/remove, dimension safety checks, structured observations, and contextual follow-up Q&A.
- **Ask Document:** Local RAG with heading-aware chunking, SQLite FTS4 retrieval, multi-document selection scope, document comparison, and executive summaries.
- **Solve a Problem:** Multi-step diagnostic state machine supporting contextual equipment photo intake, selectable observation options, alternative cause branching, and safety escalation alerts.
- **Learn & Explain:** Interactive AI tutor with difficulty level selection, structured principles, analogies, interactive multiple-choice quizzes with hints, free-response evaluations, and lesson progress saving.
- **Create & Write:** Document studio supporting email, report, assignment, summary drafting, rewriting, grammar fixing, local export to Markdown (`.md`) and Plain text (`.txt`), and formatted system sharing.
- **Personalization & Memory:** User-controlled explanation levels, response lengths, tones, and transparent on-device memory inspection with 1-tap wipe.
- **AI Tools & Library:** Quick offline text utilities, PDF document management, and knowledge pack catalog.

## Architecture principles

- Android app: Kotlin + Jetpack Compose
- Local inference: LiteRT-LM adapter (model/runtime feasibility is a release gate)
- Local persistence: Room / SQLite
- Document search: local indexing; FTS5 availability must be validated on supported Android versions
- Optional backend: FastAPI + PostgreSQL for pack catalog metadata
- Local chat and installed content must work without backend connectivity
- User content is not uploaded by default

## Repository layout

```text
openfield/
├── android/             # Kotlin Android application
├── backend/             # Optional FastAPI pack catalog
├── docs/                # Product and engineering specifications
├── pack-spec/            # Knowledge-pack schema/specification
├── sample-packs/         # Small, license-cleared sample packs
└── .github/              # CI, issue and pull-request templates
```

## Getting started

### Android
1. Install Android Studio stable and a compatible JDK/Android SDK.
2. Open `android/` in Android Studio.
3. Select a physical Android device for model/runtime tests.
4. Follow `android/README.md`.
5. Do not download or bundle model artifacts until their license, checksum, size, and runtime compatibility are verified.

### Backend (optional)
```bash
cd backend
python -m venv .venv
# Windows PowerShell:
.venv\Scripts\Activate.ps1
# macOS/Linux:
# source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```
Open `/docs` on the local server for the API schema.

## Documentation

- `docs/PRD.md` — Phase 1 product requirements
- `docs/TECHNICAL_ARCHITECTURE.md` — Android, AI and backend architecture
- `docs/DATABASE_API.md` — data model and API contract
- `docs/ROADMAP_P1.md` — milestones and development checklist
- `docs/PRIVACY_NOTICE.md` — offline privacy commitments and zero telemetry
- `docs/FAQ.md` — frequently asked questions and troubleshooting
- `docs/SUPPORTED_DEVICES.md` — hardware compatibility matrix and specifications
- `docs/RELEASE_NOTES.md` — release history and highlights

## Privacy

OpenField is designed local-first. Before enabling any network feature, document exactly what data leaves the device, why, and how the user controls it. Never log prompts, private documents, or image contents.

## Contributing

Read [`CONTRIBUTING.md`](CONTRIBUTING.md), open an issue before substantial work, and include tests or reproducible verification with pull requests.

## License

OpenField is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details. Third-party model weights and runtime libraries are subject to their respective terms.
