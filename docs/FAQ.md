# OpenField Frequently Asked Questions (FAQ)

### General & Architecture

#### What is OpenField?
OpenField is an open-source, offline-first Android AI companion. It delivers conversational intelligence, document question-answering (RAG), visual understanding, guided problem solving, and concept learning directly on your phone without requiring an active internet connection.

#### Why is OpenField local-first?
Traditional AI assistants upload your personal questions, sensitive documents, and photos to cloud servers for remote processing. OpenField runs AI inference locally directly on your device's CPU or GPU. This ensures complete privacy, zero latency variation from network issues, and continuous reliability even in remote locations, airplanes, or low-connectivity zones.

#### Which AI model does OpenField use?
OpenField exclusively uses **Gemma 4 E2B IT** (`gemma-4-E2B-it.litertlm`) powered by the official **Google LiteRT-LM** (formerly LiteRT / MediaPipe GenAI) Android runtime. Gemma 4 E2B IT is an instruction-tuned, multimodal-capable model providing strong reasoning, conversational coherence, and visual processing in an edge-optimized footprint (~2.58 GB).

---

### Hardware, Storage & Performance

#### What are the minimum device requirements?
- **Operating System:** Android 8.0 (API level 26) or higher (Android 12+ recommended).
- **Architecture:** 64-bit ARM (`arm64-v8a`).
- **RAM:** Minimum 4 GB RAM (6 GB or 8 GB recommended for optimal multi-turn performance).
- **Storage:** At least 3.5 GB of free internal storage to house the model weights and local search index.

#### Why does the model require ~2.58 GB of storage?
Real on-device AI runs billions of neural network weights locally without cloud shortcuts. Gemma 4 E2B IT packs state-of-the-art quantized neural weights into a compact ~2.58 GB `.litertlm` artifact, balancing high language comprehension with mobile storage constraints.

#### Can I import the model file from an SD card or external drive?
Yes. OpenField provides a native Storage Access Framework (SAF) import option. If you have already downloaded `gemma-4-E2B-it.litertlm` on a PC or stored it on a microSD card, you can import it into OpenField without consuming mobile data.

#### How does OpenField balance battery and thermal limits?
OpenField loads model weights on-demand and manages session memory tightly. Inference executes on background coroutines with cooperative cancellation: if you navigate away or tap "Stop Generating", execution halts immediately to preserve battery.

---

### Features & Capabilities

#### How does Document Q&A work offline?
When you import a PDF or text file, OpenField extracts its text using an embedded, offline PDF processor. The document is chunked into overlapping passages (approx. 500 characters with 100-character overlap) and indexed into a local SQLite FTS (Full-Text Search) table. When you ask a question, the most relevant passages are retrieved and supplied as local context to Gemma 4 E2B IT, which generates an accurate answer referencing specific page numbers.

#### What happens if my PDF is a scanned image?
Phase 1 focuses on native digital text extraction. Documents containing purely scanned images without an embedded text layer will yield low extraction results. Scanned OCR support is slated for a future phase.

#### Can I use OpenField in Airplane Mode?
Yes! Once the Gemma 4 E2B IT model artifact is downloaded or imported, all chat, document Q&A, and utility features operate seamlessly with Wi-Fi and Cellular data turned off.

#### Does OpenField share my data with anyone?
No. OpenField has zero remote tracking, no analytics SDKs, and no cloud inference fallbacks. Everything stays on your phone.
