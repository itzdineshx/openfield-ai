# OpenField Privacy & Security Audit

**Version:** 0.2.0  
**Status:** Verified & Enforced  
**Last Audited:** November 2026

OpenField is designed from the ground up as an offline-first, private AI assistant. All inference, personal conversations, documents, images, and preferences remain strictly on the local device.

---

## 1. Network Requests & Permissions Review

### Declared Permissions in `AndroidManifest.xml`
| Permission | Justification | Cloud Data Transmission |
|---|---|---|
| `INTERNET` | Optional download of the ~2.58 GB `gemma-4-E2B-it.litertlm` model file from Hugging Face during initial setup. | **None** during inference. |
| `ACCESS_NETWORK_STATE` | Used solely by `ModelDownloadManager` to pause/resume background model download when connectivity changes. | **None**. |
| `WAKE_LOCK` & `FOREGROUND_SERVICE` | Keeps model download active in the background when user initiates download. | **None**. |
| `POST_NOTIFICATIONS` | Displays model download progress in Android notification tray. | **None**. |
| `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` | Used by Android photo picker to import images for visual inspection. | **None**. |

### Zero Telemetry & Analytics
- **No Analytics SDKs:** No Google Analytics, Firebase, Sentry, Mixpanel, or telemetry trackers exist in `build.gradle.kts`.
- **No Third-Party AI APIs:** Zero calls to OpenAI, Anthropic, Gemini Cloud, or remote inference endpoints.
- **Strict Offline Enforcement:** When airplane mode is toggled, all core features (Ask AI, Analyze Image, Ask Document, Solve a Problem, Learn & Explain, Create & Write, My Library) operate with 100% functionality.

---

## 2. Local Document & Vision Data Isolation

### Image Handling
- Images picked via Android's photo picker or Camera are copied to the private app cache directory (`context.cacheDir/vision_*.jpg`).
- Bitmaps are inspected and downsampled if exceeding 2048px to prevent GPU/NPU memory exhaustion.
- Temporary preview bitmaps are cleared when `controller.reset()` or `repository.clearAppCache(context)` is invoked.
- Raw image pixels are fed directly to the local LiteRT-LM vision encoder (`generateWithImageStream`) via file descriptor path; no external server ever receives visual data.

### Document & PDF Handling
- Documents imported via the Android Storage Access Framework (SAF) are parsed locally on-device using Android PDFBox.
- Text chunks are tokenized and stored in private Room SQLite database tables (`documents`, `document_chunks`, `document_chunks_fts`).
- Full-Text Search (FTS4) runs entirely in local SQLite within the app sandbox.

---

## 3. Local Memory & Transparent Personalization

### User Control
- Personalization settings (Preferred Learning Level, Response Length, Tone, and Custom Context) are stored locally in the private SQLite database.
- A dedicated **Inspect & Control Local Memory** dialog in Settings allows users to:
  1. View every piece of remembered context.
  2. Edit background instructions.
  3. Clear/wipe memory completely with a single tap.
- When memory is cleared, it is immediately expunged from memory and SQLite.

### Session Deletion
- Deleting a chat, document, troubleshooting session, or learning lesson instantly removes the associated rows and FTS chunks from SQLite, triggering complete index cleanup.

---

## 4. Privacy FAQ

**Q: Does OpenField send my questions or photos to any cloud server?**  
**A:** No. OpenField runs Gemma 4 E2B IT completely on your phone's processor using Google LiteRT-LM. No prompt, image, or document text is ever transmitted over the network.

**Q: Can OpenField work in Airplane Mode?**  
**A:** Yes. Once the model file is downloaded or imported, OpenField operates fully in Airplane Mode without internet access.

**Q: Where are my files stored?**  
**A:** All data is kept within the Android sandboxed application storage directory (`/data/data/com.openfield.app/`), protected by Android's application sandbox security.
