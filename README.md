# OpenField

**Knowledge should never depend on connectivity.**

OpenField is an open-source, offline-first Android AI companion for everyday questions, learning, document understanding, image analysis, and guided problem solving. The Phase 1 goal is to keep core AI experiences local and private by default.

> **Project status:** Early development / Phase 1. Features and supported devices are being validated. Do not assume image, audio, or every model capability is available on all phones.

## Phase 1 focus

- Local AI chat
- Ask questions about imported documents
- Image analysis (after runtime/device validation)
- Learn & Explain
- Guided problem solving
- AI utilities: summarize, explain, translate, rewrite, extract, create
- Local library and knowledge-pack management
- Optional pack catalog/download backend

Trips, emergency response, and survival workflows are **not Phase 1 scope**.

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
