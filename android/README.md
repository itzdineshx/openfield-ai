# OpenField — Native Android Frontend Prototype (Phase 1)

High-fidelity, offline-first Android companion application prototype built with **native Kotlin** and **Jetpack Compose**. Recreated directly from the reference UI specification across all 12 primary screens and 5 essential supporting screens.

---

## 📱 Implemented Screens

### 12 Reference Screens
1. **Screen 01: Splash Screen (`SplashScreen.kt`)**
   - Centered custom squircle mountain logo, typography, layered landscape backdrop, "Get Started" primary action, and footer labels (*Offline · Private · Open Source*).
2. **Screen 02: Onboarding — Ask Anything (`OnboardingScreen.kt`)**
   - Step 1/3 indicator, Skip action, multi-modal feature illustration (Text, Image, Voice), and Next navigation.
3. **Screen 03: Onboarding — Understand & Solve (`OnboardingScreen.kt`)**
   - Step 2/3 indicator, checklist illustration, feature breakdown (Troubleshoot & Fix, Learn New Things, Analyze Information), Back and Next navigation.
4. **Screen 04: Onboarding — Your Knowledge (`OnboardingScreen.kt`)**
   - Step 3/3 indicator, document library illustration, feature highlights (Ask Your Documents, Build Your Library, Private & Local), Back and "Get Started" buttons leading directly to the Home dashboard.
5. **Screen 05: Home Dashboard (`HomeScreen.kt`)**
   - Top bar with OpenField mountain squircle branding and profile avatar.
   - Offline status banner with pulsing green indicator (*"Offline Mode · All AI features available offline"*).
   - Large rounded prompt composer with attachment shortcuts (Image, Camera, Voice, Document) and active send button.
   - 2-column, 6-card feature grid:
     1. *Ask AI (Chat)*
     2. *Analyze Image*
     3. *Ask Document*
     4. *Solve a Problem*
     5. *Learn & Explain*
     6. *Create & Write*
   - Persistent bottom navigation: **Home | Chats | Tools | Library**.
6. **Screen 06: AI Chat (`ChatScreen.kt`)**
   - Live conversational timeline with formatted assistant and user message bubbles.
   - Demonstration of laptop Blue Screen (BSOD) diagnostic workflow with numbered troubleshooting steps.
   - Real-time reactive StateFlow observation with bottom message composer, voice shortcut, and clear/delete session actions.
7. **Screen 07: Image Analysis (`ImageAnalysisScreen.kt`)**
   - SAF image picker (`ActivityResultContracts.GetContent`) and botanical canvas illustration.
   - Detailed analysis card: Common Name (*Wild Jasmine*), Scientific Name (*Jasminum spp.*), Family (*Oleaceae*), Key Features, and safety notice.
   - Interactive follow-up Q&A stream with Share and Save/Bookmark actions.
8. **Screen 08: Document Q&A (`DocumentQAScreen.kt`)**
   - Active document card (`User_Manual.pdf`, 12.4 MB · 120 pages) with SAF PDF picker.
   - Real-time page extraction via Android's native `android.graphics.pdf.PdfRenderer`.
   - Conversational Q&A with clickable source reference citations (*Page 42*).
9. **Screen 09: Solve a Problem (`ProblemSolverScreen.kt`)**
   - 4-step guided troubleshooting wizard with linear progress indicator.
   - Interactive single-select problem diagnosis options.
   - Diagnostic recommendations with step-by-step resolution.
10. **Screen 10: Learn & Explain (`LearnScreen.kt`)**
    - Concept learning engine with segmented tabs (*Explain | Teach | Practice*).
    - Canvas-drawn Battery Energy Conversion diagram (positive/negative terminals, chemical reaction layer, electrical output flow).
    - "In Short" takeaways and interactive prompt suggestions.
11. **Screen 11: AI Tools (`ToolsScreen.kt`)**
    - Filterable pill bar (*All | Text | Image | Document*).
    - Complete suite of offline tools: *Image Analysis, Document Q&A, Summarize, Explain, Write & Create, Translate, Reframe / Improve*.
12. **Screen 12: My Library (`LibraryScreen.kt`)**
    - Segmented tabs: *Knowledge Packs* and *My Documents*.
    - Knowledge pack management (*Computer & Tech, Devices & Repairs, Learning, Programming, Languages*) with install/uninstall toggle and storage calculation.

### Supporting Navigation & Flows
- **Chats Tab (`HistoryScreen.kt`):** Full conversation history search, pin, create new conversation, and swipe/delete.
- **Tool Detail Screen (`ToolDetailScreen.kt`):** Dedicated playground to execute offline utilities (Summarize, Translate, Reframe, Explain, Write).
- **Document Detail Screen (`DocumentDetailScreen.kt`):** Summary, page count, key points, and sample questions for imported PDFs.
- **Knowledge Pack Detail Screen (`KnowledgePackDetailScreen.kt`):** Pack overview, topics list, and install toggle.
- **Profile & Settings (`ProfileMenuSheet.kt`, `SettingsScreen.kt`, `AboutScreen.kt`):** Offline mode toggle, model selector, cache cleaner, and version details.

---

## 🛠 Zero Mock Data Architecture

All interactions are backed by a live state management architecture in `OpenFieldRepository`:
- **Real File System Integration (SAF):** Android Storage Access Framework (`ActivityResultContracts.OpenDocument` & `ActivityResultContracts.GetContent`) enables selecting real PDFs and photos from the device.
- **Native PDF Inspection:** Uses Android's built-in `android.graphics.pdf.PdfRenderer` to inspect actual page counts and extract file metadata without external dependencies.
- **Reactive StateFlow:** Chat messages, troubleshooting wizard steps, knowledge pack installations, and document lists are backed by reactive `StateFlow` and `MutableStateFlow` streams.
- **Local Rule-Based Execution:** Offline tools dynamically process input text (e.g., word count reduction in Summarize, phrase substitution in Translate, formatting in Reframe, step outline generation in Write).

---

## 🎨 Design System & Tokens

Implemented strictly according to the reference specification:

| Token | Color Hex | Usage |
|---|---|---|
| `OpenFieldBackground` | `#FFFFFF` | Main screen background |
| `OpenFieldSecondaryBackground` | `#F7F9FC` | Card surfaces & secondary containers |
| `OpenFieldPrimaryBlue` | `#1769E8` | Primary CTA buttons & active highlights |
| `OpenFieldPrimaryBlueLight` | `#EEF5FF` | Chip backgrounds & icon badges |
| `OpenFieldNavyText` | `#101B36` | Headings, primary titles & bold labels |
| `OpenFieldSecondaryText` | `#68758A` | Subtitles, timestamps & metadata |
| `OpenFieldCardBorder` | `#E5EAF2` | Subtle borders for cards and dividers |
| `OpenFieldOfflineGreen` | `#239653` | Offline status indicator |
| `OpenFieldOfflineBg` | `#EAF7ED` | Offline banner container |

- **Corner Radii:** Cards: 16–20dp, Buttons: 14–20dp, Input Composers: 24dp.
- **Icons:** Material 3 Icons with auto-mirrored standard sets.

---

## 🚀 Building & Running

### Requirements
- **Android Studio:** Ladybug (2024.2.1+) or newer
- **JDK:** Java 17 (JDK 17 or Android Studio bundled JBR 17)
- **Android SDK:** API 35 (Android 15) with Build-Tools 34+
- **Min SDK:** API 26 (Android 8.0)

### Opening in Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the `android` folder (`e:\personal\OpenField\android`).
3. Allow Gradle to sync using the project's Gradle Version Catalog (`gradle/libs.versions.toml`).
4. Select an emulator or connected physical Android device and click **Run 'app'** (`Shift + F10`).

### Command-Line Build (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Users\DINESH\jdk-17.0.12+7"
cd e:\personal\OpenField\android
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
```
android\app\build\outputs\apk\debug\app-debug.apk
```
