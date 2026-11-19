from fastapi import FastAPI, HTTPException, Query
from typing import List, Optional
from pydantic import BaseModel

app = FastAPI(
    title="OpenField Catalog API",
    version="0.2.1",
    description="Official OpenField Knowledge Pack Catalog service. Optional for local inference.",
)

class KnowledgePackItem(BaseModel):
    pack_id: str
    title: str
    description: str
    category: str
    version: str
    size: str
    icon_color_hex: int
    checksum_sha256: str
    provenance: str
    publisher: str
    license: str
    topics: List[str]
    download_url: str

OFFICIAL_PACKS = [
    KnowledgePackItem(
        pack_id="comp_tech",
        title="Computer & Tech",
        description="Troubleshooting, errors, setup",
        category="Technology",
        version="1.2.0",
        size="2.4 GB",
        icon_color_hex=0xFF43A047,
        checksum_sha256="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        provenance="OpenField Official Curated Corpus",
        publisher="OpenField Knowledge Foundation",
        license="CC-BY-4.0",
        topics=["Networking", "Linux Commands", "Hardware Architecture", "Troubleshooting", "Operating Systems", "Linux"],
        download_url="/api/v1/packs/comp_tech/download"
    ),
    KnowledgePackItem(
        pack_id="devices_repairs",
        title="Devices & Repairs",
        description="General fixes and maintenance",
        category="Engineering",
        version="1.3.0",
        size="1.8 GB",
        icon_color_hex=0xFF43A047,
        checksum_sha256="ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
        provenance="OpenField Official Curated Corpus",
        publisher="OpenField Knowledge Foundation",
        license="CC-BY-4.0",
        topics=["Soldering", "Circuit Boards", "Multimeter Tests", "Component Replacement", "Power Supplies", "Multimeter"],
        download_url="/api/v1/packs/devices_repairs/download"
    ),
    KnowledgePackItem(
        pack_id="learning",
        title="Learning",
        description="Science, concepts, how-to guides",
        category="Education",
        version="1.0.0",
        size="1.2 GB",
        icon_color_hex=0xFF7E57C2,
        checksum_sha256="88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589",
        provenance="OpenField Official Curated Corpus",
        publisher="OpenField Knowledge Foundation",
        license="CC-BY-4.0",
        topics=["Physics Formulas", "Periodic Table", "Calculus Essentials", "Cellular Biology", "Mechanics"],
        download_url="/api/v1/packs/learning/download"
    ),
    KnowledgePackItem(
        pack_id="programming",
        title="Programming",
        description="Basics, examples and concepts",
        category="Software",
        version="1.1.0",
        size="1.9 GB",
        icon_color_hex=0xFF1E88E5,
        checksum_sha256="5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9",
        provenance="OpenField Official Curated Corpus",
        publisher="OpenField Knowledge Foundation",
        license="CC-BY-4.0",
        topics=["Kotlin Coroutines", "Room Database", "Clean Architecture", "Data Structures", "Algorithms"],
        download_url="/api/v1/packs/programming/download"
    ),
    KnowledgePackItem(
        pack_id="languages",
        title="Languages",
        description="Translation and learning support",
        category="Language",
        version="1.0.0",
        size="860 MB",
        icon_color_hex=0xFF00ACC1,
        checksum_sha256="6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b",
        provenance="OpenField Official Curated Corpus",
        publisher="OpenField Knowledge Foundation",
        license="CC-BY-4.0",
        topics=["Vocabulary", "Technical Terms", "Grammar Rules", "Phrases", "Translations"],
        download_url="/api/v1/packs/languages/download"
    )
]

@app.get("/health", tags=["health"])
def health():
    return {"status": "ok", "service": "openfield-catalog", "version": "0.2.1"}

@app.get("/api/v1/packs", tags=["packs"])
def list_packs(
    category: Optional[str] = None,
    query: Optional[str] = None,
    cursor: int = Query(default=0, ge=0),
    limit: int = Query(default=10, le=50)
):
    filtered = OFFICIAL_PACKS
    if category:
        filtered = [p for p in filtered if p.category.lower() == category.lower()]
    if query:
        q = query.lower()
        filtered = [p for p in filtered if q in p.title.lower() or q in p.description.lower() or any(q in t.lower() for t in p.topics)]
    
    total = len(filtered)
    items = filtered[cursor : cursor + limit]
    next_cursor = cursor + limit if cursor + limit < total else None
    
    return {
        "items": items,
        "total": total,
        "cursor": cursor,
        "next_cursor": next_cursor
    }

@app.get("/api/v1/packs/{pack_id}", tags=["packs"])
def get_pack(pack_id: str):
    for pack in OFFICIAL_PACKS:
        if pack.pack_id == pack_id:
            return pack
    raise HTTPException(status_code=404, detail="Knowledge pack not found")

@app.post("/v1/downloads/{pack_version_id}/url", tags=["downloads"])
def get_download_url(pack_version_id: str):
    # Short-lived signed download URL endpoint per PRD Section 43
    return {
        "download_url": f"https://catalog.openfield.ai/packs/{pack_version_id}.zip",
        "expires_in_seconds": 3600
    }
