# Architecture Decision Record: Offline Voice Capabilities and Model Constraints

**Date:** 2026-11-19  
**Status:** Approved / Documented  
**Component:** Multimodal Intelligence (Gate 4)  
**Target:** CRUX v0.2.0

---

## 1. Context & Objective
Gate 4 requires evaluating offline voice intelligence capabilities alongside image understanding. We audited the exact capabilities of the single approved local model (`Gemma 4 E2B IT`, `gemma-4-E2B-it.litertlm`) and Google LiteRT-LM runtime (`com.google.ai.edge.litertlm:litertlm-android:0.19.1`).

## 2. Technical Findings

### A. Model Modality
- `gemma-4-E2B-it.litertlm` is a multimodal **Vision-Language Model (VLM)**.
- Supported modalities:
  1. **Text Prompt Tokens** (Bidirectional & Autoregressive text generation).
  2. **Image Vision Tokens** (RGB pixel tensors extracted via LiteRT image encoder).
- Unsupported modality:
  - **Raw Audio / Speech Waveforms**: The Gemma 4 E2B IT model architecture does not contain an audio encoder or direct speech token projection weights.

### B. Runtime Capabilities (LiteRT-LM 0.19.1)
- The LiteRT-LM Android C++/Java library (`com.google.ai.edge.litertlm.Engine`) exposes:
  - `generateStream(prompt)`
  - `generateWithImageStream(imagePath, prompt)`
- The engine does not expose native audio waveform input APIs for Gemma 4 E2B IT. Passing raw PCM audio to LiteRT-LM is neither supported nor documented.

## 3. Decision & Architecture for Voice Intelligence

To preserve 100% offline functionality, privacy, and memory budgets:

1. **Keep Gemma 4 E2B IT Strict:**
   - Do not attempt unsupported audio tensor hacks or cloud speech APIs.
   - Gemma serves as the cognitive reasoning engine for both text and visual comprehension.

2. **Decoupled Local Speech-To-Text (STT):**
   - Use Android's native `android.speech.SpeechRecognizer` with offline language packs installed on-device.
   - Microphone lifecycle must remain strictly explicit: no background or always-listening loops.
   - Transcripts must be presented to the user for preview and editing before inference.

3. **Decoupled Local Text-To-Speech (TTS):**
   - Use Android's native `android.speech.tts.TextToSpeech` engine, which operates locally and consumes negligible RAM/GPU compared to a second LLM.

4. **Resource Management:**
   - Keeping STT/TTS separate from Gemma preserves precious NPU/GPU RAM (saving 1.5GB+ compared to running an on-device audio LLM concurrently with Gemma).

## 4. Acceptance Verification
- Gemma model integrity preserved without third-party audio dependencies.
- Zero network requests or telemetry for voice workflows.
