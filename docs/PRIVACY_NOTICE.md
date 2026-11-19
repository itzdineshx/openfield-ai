# OpenField Privacy Notice

**Last Updated:** November 2026

## 1. Our Core Principle: Local & Offline by Default

OpenField is built on the fundamental principle that your questions, thoughts, personal documents, and photos belong to you. We believe private intelligence should never require transmitting your personal life across third-party networks.

All artificial intelligence capabilities in OpenField—including natural language chat, document question answering, image analysis, and concept explanations—are executed directly on your Android device using the Google LiteRT-LM on-device engine.

---

## 2. What Information Remains on Your Device

### A. Conversations and Chats
- All prompts, questions, responses, and conversation histories are stored strictly in an application-private SQLite database managed via Room (`openfield.db`).
- Conversations never leave your device. There is no remote logging, cloud synchronization, or external chat indexing.

### B. Uploaded & Imported Documents
- Documents (PDF, TXT, Markdown) imported via Android Storage Access Framework (SAF) are copied solely into OpenField's sandboxed, application-private storage.
- Document text extraction, semantic chunking, and SQLite FTS search indexing are executed 100% locally on your phone's processor.
- When you delete a document from OpenField, all associated text chunks and full-text index records are immediately and permanently removed from local storage.

### C. Photos and Images
- Images selected from your device gallery or captured for visual analysis are loaded directly into application memory.
- LiteRT-LM processes image tensors on your local CPU or GPU.
- Images are never uploaded to any remote server or third-party computer vision API.

---

## 3. Network and Cloud Connectivity

- **Zero Cloud AI Inference:** OpenField does not connect to cloud AI APIs (such as OpenAI, Anthropic, Gemini Cloud, or Firebase AI) for conversation or reasoning.
- **Model Download (One-Time):** The Gemma 4 E2B IT model artifact (`gemma-4-E2B-it.litertlm`) is downloaded directly from the official repository (or imported manually via Storage Access Framework). During download, standard HTTP requests are made strictly to retrieve the binary artifact. No user profile, device identifier, or telemetry is attached.
- **Airplane Mode Operation:** Once the model artifact is present on the device, OpenField operates fully in Airplane Mode without an internet connection.

---

## 4. Third-Party Trackers and Telemetry

- **No Analytics SDKs:** OpenField contains no Google Analytics, Firebase Analytics, Facebook SDK, or third-party advertising frameworks.
- **No Crash Logging of User Data:** Error handling routines explicitly exclude prompt content, user document contents, and personal identifiable information (PII) from logcat streams.

---

## 5. User Control and Data Erasure

You maintain complete sovereignty over your data:
1. **Clear History:** You can delete individual chat sessions or purge your entire chat history from the app settings at any time.
2. **Remove Documents:** Removing a document immediately purges the file copy and all associated chunks and search indexes.
3. **Uninstall:** Uninstalling the OpenField application will trigger Android's operating system to cleanly erase all application-private data, including the database and downloaded model artifacts.

For questions or security disclosures, please open an issue on the OpenField GitHub repository.
