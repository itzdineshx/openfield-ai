# CRUX Phase 3 — Product Requirements Document (PRD)

**Document Version:** 1.0  
**Date:** 20 November 2026  
**Status:** Approved Product Specification  
**Baseline:** v0.2.1  
**Target Roadmap:** v0.3.0 → v1.0.0  
**Authoritative Reference:** `docs/OpenField_Phase1_PRD_50_Pages.pdf` (Phase 1 PRD v1.1, Expanded Specification)  
**Platform:** Native Android (Kotlin + Jetpack Compose + Material 3)  
**Local AI Engine:** Gemma 4 E2B IT via Google LiteRT-LM (0.19.1) on CPU

---

## 1. Executive Summary & Vision

CRUX is an **offline-first Android personal intelligence platform**. It empowers people to understand personal documents, organize knowledge, learn complex concepts, troubleshoot practical problems, perform everyday calculations, and produce useful writing—completely offline, with user data remaining private by default.

### 1.1 The Phase 3 Evolution
In Phase 1 and Phase 2, CRUX established capable, decoupled AI workspaces (Ask AI, Analyze Image, Document Q&A, Solve a Problem, Learn & Explain, Create & Write, AI Tools, and My Library).

In **Phase 3**, CRUX transforms from a suite of isolated tools into a **Unified Personal Intelligence Platform**:
1. **Unified Personal Knowledge (v0.3.0):** Users can organize, search, and reason across multiple local sources (PDFs, OCR scans, notes, chat history, and installed knowledge packs) simultaneously with verifiable citations.
2. **Context-Aware Assistant (v0.4.0):** Workspaces maintain continuous task context, enabling seamless transitions from research to explanation to drafting without repetitive prompts.
3. **Practical Offline Intelligence (v0.5.0):** Reliable, deterministic local utilities (calculator, unit converter, date/time, QR scanner, CSV inspector) complement generative AI.
4. **Personal Task Automation (v0.6.0):** Natural language requests translate into structured, editable task plans with grounded document generation.
5. **Advanced Intelligence & Quality (v0.7.0):** Measured retrieval improvements, local evaluation benchmarks, and user personalization.
6. **Productivity Workflows (v0.8.0):** Cohesive end-to-end workflows connecting research, learning, troubleshooting, and writing.
7. **Production Stabilization (v0.9.0 → v1.0.0):** Comprehensive testing, performance optimization, and release readiness.

---

## 2. Product Principles & Non-Negotiable Boundaries

### 2.1 Core Invariants
- **100% Offline-First AI:** All generative AI reasoning runs strictly on-device using Gemma 4 E2B IT via LiteRT-LM CPU. Zero cloud inference, zero external LLM API fallbacks.
- **Privacy by Default:** Zero background telemetry, zero prompt tracking, and zero automatic uploading of user files, images, voice recordings, or transcripts.
- **Evidence-Based Answers:** Any answer based on local knowledge must provide verifiable citations with source title and page number. If evidence is insufficient, the system must clearly state that facts cannot be found.
- **Deterministic Operations:** Calculations, unit conversions, and formatting use deterministic algorithms—never generative LLM guesses.
- **Human Control:** The system acts as an assistant and advisor, never executing irreversible actions (such as file deletion) without explicit confirmation.

### 2.2 Out-of-Scope Boundaries (per PRD Section 1.4)
Phase 3 explicitly excludes:
- Cloud-hosted AI inference or remote API proxies.
- GPS trip planning, navigation, or turn-by-turn routing.
- Social networking, community profiles, or cloud chat synchronization.
- Autonomous device modification or remote system repair.
- Background or always-listening microphone monitoring.

---

## 3. Detailed Requirements Catalogue by Release

### 3.1 v0.3.0 — Unified Personal Knowledge

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **KNOW-001** | Unified Knowledge Source Contract | MUST | Ingests documents, notes, chat sessions, and installed packs into a single `KnowledgeSource` representation without duplicate file copies. |
| **KNOW-002** | Unified Local Search Engine | MUST | Search queries execute across documents (FTS4), chats, drafts, and packs in parallel, returning ranked results with 120-character highlighted snippets in < 80 ms. |
| **KNOW-003** | Semantic Collections | MUST | Users can create, rename, and delete collections (e.g. "College Studies", "Engineering") and assign/remove sources. Collections persist across app restarts. |
| **KNOW-004** | Cross-Source Q&A Reasoning | MUST | Users can query across 2+ selected sources or a collection. The retriever gathers evidence across sources, caps context at 1,200 tokens, and Gemma generates an integrated answer. |
| **KNOW-005** | Verifiable Citations | MUST | Every factual claim in cross-source answers must cite `[Source: <Title>, Page <P>]`. Invalid or hallucinated citations are flagged. |
| **KNOW-006** | Evidence Explorer | MUST | Expandable UI panel displays all exact retrieved passages, relevance scores, and direct navigation links to open the original source page. |
| **KNOW-007** | Unified Knowledge Center UI | MUST | Modernized Knowledge Hub featuring Overview statistics, Collections shelf, Source cards with selection checkboxes, and multi-source action bar. |

### 3.2 v0.4.0 — Context-Aware Personal Assistant

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **CTX-001** | Persistent Task Sessions | MUST | Users can create and resume named task sessions retaining active sources, conversation context, and generated artifacts. |
| **CTX-002** | Cross-Workspace Context Sharing | MUST | Workspaces can share approved task context (e.g. Document Q&A evidence transferred to Create & Write canvas). |
| **CTX-003** | User Context Controls | MUST | Users can view, edit, add, or remove context items from an active session; context is inspectable and cleared on demand. |

### 3.3 v0.5.0 — Practical Offline Intelligence

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **UTIL-001** | Deterministic Calculator | MUST | Safe expression evaluator supporting arithmetic, percentages, powers, roots, and scientific formulas with correct operator precedence. |
| **UTIL-002** | Deterministic Unit Converter | MUST | Exact conversions for length, mass, temperature, area, volume, speed, time, data storage, and energy. |
| **UTIL-003** | Date & Time Tools | SHOULD | Calculate duration between dates, add/subtract time intervals, and compute deadlines respecting local time zones. |
| **UTIL-004** | QR & Barcode Scanner | SHOULD | Camera and image-based barcode scanning with user-controlled link opening (zero automatic navigation). |
| **UTIL-005** | Structured Data Inspector | SHOULD | Local CSV and JSON file viewer with sorting, column filtering, and table summary generation. |

### 3.4 v0.6.0 — Personal Task Automation

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **AUTO-001** | Natural Language Task Planning | MUST | Translates requests (e.g. "Prepare 7-day study plan") into structured, editable subtasks with priorities and deadlines. |
| **AUTO-002** | Local Task Management | MUST | Local persistence of tasks, completion tracking, subtasks, and source associations. |
| **AUTO-003** | Source-Grounded Document Generation | MUST | Generates structured revision notes, summaries, checklists, and reports linked to retrieved source citations. |
| **AUTO-004** | Action Confirmation Safety | MUST | Consequential actions (file overwriting, collection deletion, session purge) require explicit user confirmation dialogs. |

### 3.5 v0.7.0 — Advanced Intelligence & Personalization

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **INTEL-001**| Retrieval Ranking Optimization | SHOULD | Bounded query expansion, heading-aware ranking, and duplicate suppression evaluated against baseline. |
| **INTEL-002**| Local Personalization | SHOULD | Explicit user preferences for explanation depth, writing tone, and answer verbosity stored locally and editable. |
| **INTEL-003**| Repeatable Evaluation Dataset | MUST | Version-controlled benchmark dataset measuring retrieval recall, citation precision, TTFT, and throughput. |

### 3.6 v0.8.0 — Productivity & Integration

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **PROD-001** | End-to-End Workflows | MUST | Smooth multi-step workflows: Research (Import → Search → Compare), Study (Select → Explain → Quiz), and Problem Solving (Symptom → Steps → Report). |
| **PROD-002** | Navigation Ergonomics | MUST | Streamlined top-level navigation connecting Home, Knowledge, Chats, Tools, and Settings. |

### 3.7 v0.9.0 & v1.0.0 — Stabilization & Production Release

| Req ID | Requirement Statement | Priority | Acceptance Criteria |
|---|---|---|---|
| **REL-001** | 100% Offline Integrity | MUST | Complete feature suite operates without network connectivity in airplane mode. |
| **REL-002** | Non-Destructive Upgrades | MUST | Upgrades from v0.2.1 through v0.9.0 preserve all user documents, chats, drafts, and collections without data corruption. |
| **REL-003** | Signed Release Artifact | MUST | Signed production APK (`app-release.apk`) generated, checksum verified, and validated on physical hardware. |

---

## 4. Hardware Reference & Performance Budgets

Target Reference Hardware: **Lava LXX503 (Yuva 3 Pro)**  
- **OS:** Android 14 (API 34)
- **CPU:** Unisoc T616 (2x Cortex-A75 @ 2.0 GHz, 6x Cortex-A55 @ 1.8 GHz)
- **RAM:** 6 GB Physical RAM
- **Storage:** UFS 2.2 Flash

### Performance Targets:
- **Unified Search Latency:** < 80 ms across all local tables.
- **Cross-Source Retrieval Latency:** < 250 ms across up to 8 documents.
- **Time to First Token (TTFT):** < 6,000 ms on mobile CPU.
- **Text Generation Throughput:** > 3.0 tokens/second.
- **App Heap Footprint:** < 40 MB Java heap; native memory isolated in runtime.
- **Battery Drain:** < 4% per standard 15-minute multi-source research workflow.
- **Thermal Ceiling:** < 42.0°C; zero thermal throttling.
