# Optional OpenField backend

Phase 1 backend supports knowledge-pack catalog metadata and artifact distribution only.

## Run locally
```bash
python -m venv .venv
# Windows: .venv\Scripts\Activate.ps1
# macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

- Health: `/health`
- API docs: `/docs`
- Starter list endpoint: `/api/v1/packs`

Next: configuration, PostgreSQL connection, migrations, pack schema, pagination, checksum-backed artifact storage, publisher permissions, tests, and rate/size limits.

Do not add chat inference endpoints in Phase 1 unless the PRD is explicitly changed.
