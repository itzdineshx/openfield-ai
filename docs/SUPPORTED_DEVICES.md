# CRUX Supported Devices & Hardware Guide

This document establishes the hardware compatibility matrix, processor requirements, and performance expectations for running **CRUX** and local **Gemma 4 E2B IT** inference via **LiteRT-LM**.

---

## 1. Baseline Requirements

| Specification | Minimum Required | Recommended Baseline |
| :--- | :--- | :--- |
| **Operating System** | Android 8.0 (API Level 26) | Android 12.0+ (API Level 31+) |
| **CPU Architecture** | 64-bit ARM (`arm64-v8a`) | ARMv8.2-A+ or ARMv9 with FP16/DotProd |
| **Total Device RAM** | 4 GB | 6 GB to 8 GB+ |
| **Free Internal Storage** | 3.5 GB (for model artifact & FTS) | 5.0 GB+ |
| **GPU / Acceleration** | OpenCL / Vulkan compliant GPU or modern multi-core CPU | Adreno 6xx/7xx, Mali-G7x/G7xx, or Immortalis |

> [!IMPORTANT]
> 32-bit ARM (`armeabi-v7a`) and devices with 3 GB or less RAM are unsupported due to the physical memory footprint of 2B-class LLM weights and KV cache tensors.

---

## 2. Hardware Tier Matrix

### Tier 1: Optimal (Flagship & Upper Mid-Range)
- **Processors:**
  - Qualcomm Snapdragon 8 Gen 1 / Gen 2 / Gen 3 / 8s Gen 3
  - Google Tensor G2 / G3 / G4
  - MediaTek Dimensity 8200 / 9000 / 9200 / 9300
- **RAM:** 8 GB to 16 GB
- **Backend:** GPU Acceleration (Vulkan / OpenCL)
- **Expected Performance:**
  - Cold Start: < 1.5 seconds
  - Generation Speed: 15–28 tokens/sec
  - Multimodal Vision: Fast (< 1.2s prefill)

### Tier 2: Supported (Mid-Range)
- **Processors:**
  - Qualcomm Snapdragon 778G / 7s Gen 2 / 7 Gen 3 / 6 Gen 1
  - MediaTek Dimensity 7050 / 7200 / 1080
  - Samsung Exynos 1380 / 1480
- **RAM:** 6 GB to 8 GB
- **Backend:** GPU (preferred) or CPU multi-threading
- **Expected Performance:**
  - Cold Start: 2.0–3.5 seconds
  - Generation Speed: 8–16 tokens/sec
  - Multimodal Vision: 1.5–2.5s prefill

### Tier 3: Minimum Viable (Entry & Budget Devices)
- **Processors:**
  - MediaTek Helio G88 / G96 / G99
  - Unisoc T616 / T606 / T618
  - Qualcomm Snapdragon 680 / 685
- **RAM:** 4 GB (strictly enforced single-tasking)
- **Backend:** CPU multi-threaded
- **Expected Performance:**
  - Cold Start: 4.0–6.0 seconds
  - Generation Speed: 4–8 tokens/sec
  - Recommendations: Close heavy background applications before running extensive multi-turn conversations.

---

## 3. Thermal and Battery Considerations

1. **Cooperative Cancellation:** CRUX supports cooperative cancellation through Kotlin Coroutines (`Job.cancel()`). Halting generation instantly ceases CPU/GPU utilization, preventing thermal escalation.
2. **Memory Footprint:** The application reserves ~2.1 GB of working memory for model weights and dynamic activation context during active inference.
3. **Background Behavior:** Model execution pauses when the application is backgrounded or minimized, ensuring zero passive battery drain.
