from fastapi import FastAPI

app = FastAPI(
    title="OpenField Catalog API",
    version="0.1.0",
    description="Optional catalog API. Local AI and local content must not depend on it.",
)

@app.get("/health", tags=["health"])
def health():
    return {"status": "ok"}

@app.get("/api/v1/packs", tags=["packs"])
def list_packs():
    # TODO: implement pagination, filters, publication state and repository layer.
    return {"items": [], "next_cursor": None}
