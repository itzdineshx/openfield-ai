package com.openfield.app.feature.library

import com.openfield.app.data.local.entity.KnowledgePackArticleEntity

/**
 * Authentic, curated domain knowledge content for offline Knowledge Packs.
 * No mock data — real practical guides, technical procedures, formulas, and references.
 */
object KnowledgePackContent {

    val CURATED_ARTICLES: List<KnowledgePackArticleEntity> = listOf(
        // =================================================================
        // 1. COMPUTER & TECH (comp_tech)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "comp_tech_linux_perms",
            packId = "comp_tech",
            title = "Linux File Permissions & Ownership Guide",
            section = "Linux & Systems",
            content = """
# Linux File Permissions & Ownership

In Linux and Unix-like operating systems, file permissions and ownership protect system integrity and user privacy. Every file and directory is governed by three permission scopes: User (owner), Group, and Others.

## 1. Permission Representation
When inspecting files via `ls -l`, permissions appear as a 10-character string (e.g., `-rwxr-xr--`):
- Char 1: File type (`-` regular file, `d` directory, `l` symbolic link).
- Chars 2-4: Owner permissions (Read `r`, Write `w`, Execute `x`).
- Chars 5-7: Group permissions.
- Chars 8-10: Others (world) permissions.

## 2. Octal Mode Representation
Permissions are commonly represented in octal notation where:
- Read (`r`) = 4
- Write (`w`) = 2
- Execute (`x`) = 1
- None (`-`) = 0

Summing values produces single digits (0 through 7):
- `7` = `4+2+1` (rwx: full access)
- `6` = `4+2+0` (rw-: read and write, typical for data files)
- `5` = `4+0+1` (r-x: read and execute, typical for scripts/folders)
- `4` = `4+0+0` (r--: read only)

Common permission configurations:
- `chmod 755 script.sh` -> Owner: read/write/execute (7), Group: read/execute (5), Others: read/execute (5). Standard for executable binaries and scripts.
- `chmod 644 config.conf` -> Owner: read/write (6), Group: read (4), Others: read (4). Standard for system configuration files.
- `chmod 600 id_rsa` -> Owner: read/write (6), Group: none (0), Others: none (0). Required for SSH private keys to prevent unauthorized reading.

## 3. Modifying Ownership with chown
- Change owner: `chown user filename`
- Change owner and group: `chown user:group filename`
- Recursive modification: `chown -R user:group /opt/myapp/`

## 4. Special Directory Permissions
For directories, execute (`x`) permission is required to enter or traverse the directory (`cd` command). Without `x`, files inside cannot be accessed even if they have individual read permissions.
            """.trimIndent(),
            tagsJson = "[\"Linux\", \"chmod\", \"chown\", \"Permissions\", \"Security\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "comp_tech_network_diag",
            packId = "comp_tech",
            title = "Network Diagnostics & Connectivity Troubleshooting",
            section = "Networking",
            content = """
# Network Diagnostics & Connectivity Troubleshooting

When diagnosing local area network (LAN) and wide area network (WAN) outages, apply a systematic bottom-up troubleshooting methodology based on the OSI model.

## Phase 1: Physical and Link Layer Verification
1. Check interface status: `ip link show` or `ifconfig`. Confirm the state is `UP` and carrier is detected.
2. Verify local IP assignment: `ip addr show`. Verify that an IPv4 address has been assigned by DHCP (e.g., `192.168.1.50/24`) and not an APIPA fallback address (`169.254.x.x`).

## Phase 2: Gateway and Local Subnet Routing
1. Check routing table: `ip route show`. Identify the default gateway (e.g., `default via 192.168.1.1 dev eth0`).
2. Ping local gateway: `ping -c 4 192.168.1.1`.
   - If ping fails: The issue is local to the device, Ethernet cable, or Wi-Fi router.
   - If ping succeeds: Local link is operational; proceed to public routing.

## Phase 3: External IP Reachability (WAN)
1. Ping a reliable public IP address directly to bypass DNS: `ping -c 4 8.8.8.8` or `1.1.1.1`.
   - If ping succeeds: Internet connectivity is alive; failures are due to DNS name resolution.
   - If ping fails with "Destination Host Unreachable": Default gateway cannot route upstream; check ISP modem or WAN interface.

## Phase 4: DNS Name Resolution
1. Test domain resolution: `dig google.com +short` or `nslookup google.com`.
2. Inspect resolver configuration in `/etc/resolv.conf`.
3. If DNS fails, test querying public resolvers directly: `dig @1.1.1.1 google.com`. If direct query succeeds, replace local DNS servers in network configuration.

## Phase 5: Port and Service Inspection
1. Inspect active listening ports: `ss -tulpn` (or `netstat -tulpn`).
2. Verify target remote port connectivity: `nc -zv target-host 443` or `curl -Iv https://target-host`.
            """.trimIndent(),
            tagsJson = "[\"Networking\", \"ping\", \"traceroute\", \"DNS\", \"Troubleshooting\"]",
            orderIndex = 2
        ),
        KnowledgePackArticleEntity(
            id = "comp_tech_boot_diag",
            packId = "comp_tech",
            title = "PC Boot Sequence & Hardware Fault Isolation",
            section = "Hardware & OS",
            content = """
# PC Boot Sequence & Hardware Fault Isolation

Understanding the standard boot sequence allows technicians to isolate hardware failures, firmware corruption, and operating system loader errors.

## 1. Boot Stages
1. **Power Supply On (PS-ON):** Power supply asserts Power Good (+5V on grey wire) once all DC voltage rails (+12V, +5V, +3.3V) stabilize within tolerance.
2. **Firmware POST (Power-On Self Test):** The motherboard UEFI/BIOS initializes the CPU, detects memory (RAM), scans PCIe buses, and initializes the GPU.
3. **Bootloader Hand-off:** UEFI locates the EFI System Partition (ESP formatted in FAT32) and executes the bootloader (e.g., Windows Boot Manager `bootmgfw.efi` or GRUB `grubx64.efi`).
4. **Kernel Initialization:** The OS kernel loads device drivers and starts initialization daemons (`systemd` in Linux, `smss.exe` in Windows).

## 2. POST Diagnostic Codes & Symptoms
- **No Fans, No LEDs:** Dead PSU, tripped overcurrent protection (short-to-ground on 12V CPU or GPU rail), or disconnected front panel header.
- **Fans Spin, No Display (Black Screen):**
  - Continuous long beeps or cycling debug LEDs: RAM initialization failure. Reseat memory modules in dual-channel slots (typically A2 and B2).
  - 1 long beep, 2 or 3 short beeps: GPU failure or PCIe power cable unseated.
  - POST debug display codes (e.g., `00` or `FF`: CPU unseated or dead BIOS; `55` or `53`: Memory not detected).
- **"No Bootable Device Found":**
  - SATA/NVMe drive disconnected or failed.
  - UEFI boot mode mismatch (CSM/Legacy vs UEFI native).
  - Corrupt partition table or EFI partition.

## 3. Storage Drive Health (SMART)
- Use `smartctl -a /dev/nvme0n1` or CrystalDiskInfo.
- Critical SMART attributes to monitor:
  - `05 Reallocated Sectors Count`: Non-zero indicates physical platter degradation.
  - `B8 End-to-End Error`: Data path hardware integrity issue.
  - `0A Spin Retry Count`: Mechanical spindle motor failure.
            """.trimIndent(),
            tagsJson = "[\"Hardware\", \"POST\", \"UEFI\", \"BIOS\", \"Boot\"]",
            orderIndex = 3
        ),

        // =================================================================
        // 2. DEVICES & REPAIRS (devices_repairs)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "devices_multimeter_ref",
            packId = "devices_repairs",
            title = "Digital Multimeter Master Operating Reference",
            section = "Test Equipment",
            content = """
# Digital Multimeter (DMM) Master Operating Reference

A digital multimeter is the primary diagnostic instrument in electronics and electrical repair. Using the correct measurement mode and probe placement prevents device damage and ensures accurate readings.

## 1. Test Leads and Jacks
- **Black Probe:** Always plugged into the **COM** (Common/Ground) jack.
- **Red Probe:**
  - Plug into **V/Ω/Hz/Diode** jack for voltage, resistance, frequency, and continuity.
  - Plug into **10A / 20A** unfused/heavy-duty jack only when measuring high current.
  - *Caution:* Never measure voltage while the red lead is plugged into a Current (A/mA) jack, as this creates a direct dead short through the meter's internal shunt resistor!

## 2. Measurement Modes
### A. DC Voltage (V⎓)
- Used for batteries, DC power adapters, logic board power rails (1.8V, 3.3V, 5V, 12V, 19V).
- Black probe to ground (shielding or ground pad); red probe to test point.
- Reading should match expected rail voltage within ±5%.

### B. Continuity Test (Buzzer Mode 🔊)
- Verifies physical conductive paths between two points.
- Meter emits an audible tone when resistance is below ~30 to 50 ohms.
- Always perform continuity checks on **unpowered** circuits. Residual voltage can produce false triggers or blow the meter fuse.
- Used for finding broken traces, damaged ribbon cables, and detecting short-circuits between power rails and ground.

### C. Resistance Mode (Ω)
- Measures component resistance (resistors, heater elements, coils).
- Must be measured with power disconnected. For in-circuit testing, lift one leg of the component if parallel paths distort readings.

### D. Diode Mode (Forward Voltage Drop)
- Meters pass a small current (~1mA) and measure the voltage drop in volts.
- Used for testing PN junctions of silicon diodes and BJT/MOSFET transistors.
  - Normal silicon diode forward drop: `0.55V - 0.70V`. Reverse bias should read `OL` (Open Loop / Over Limit).
  - Schottky diode forward drop: `0.15V - 0.35V`.
  - A reading of `0.00V` in both directions indicates a shorted semiconductor.
            """.trimIndent(),
            tagsJson = "[\"Multimeter\", \"Voltage\", \"Continuity\", \"Resistance\", \"Diode\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "devices_soldering_guide",
            packId = "devices_repairs",
            title = "Precision Electronics Soldering & Desoldering Procedures",
            section = "Workshop Practice",
            content = """
# Precision Electronics Soldering & Desoldering Procedures

Reliable soldered joints require precise thermal transfer, appropriate flux chemistry, and clean copper surfaces.

## 1. Soldering Temperatures
- **Leaded Solder (Sn63/Pb37):** Eutectic melting point is 183°C. Recommended iron tip temperature: **300°C - 320°C**.
- **Lead-Free Solder (SAC305 - Sn96.5/Ag3.0/Cu0.5):** Melting point is 217°C. Recommended iron tip temperature: **350°C - 370°C**.
- Higher temperatures oxidize the copper tip rapidly and risk lifting delicate PCB traces. Use high-mass tips (chisel/bevel) rather than excessive heat for large ground planes.

## 2. Joint Formation Rules
1. **Clean Surfaces:** Clean oxide and conformal coating with 99% isopropyl alcohol (IPA).
2. **Apply Rosin/No-Clean Flux:** Flux strips surface oxidation at high temperatures and reduces molten solder surface tension, enabling capillary action.
3. **Heat Both Pad and Pin:** Touch the tinned iron tip simultaneously to the component lead and the PCB copper pad for 1 to 2 seconds.
4. **Feed Solder to the Joint, Not the Tip:** Feed solder wire directly onto the heated joint, allowing it to flow around the pin.
5. **Inspect the Finished Joint:**
   - A good joint has a bright, smooth, concave fillet (like a miniature volcano).
   - A **cold joint** appears dull, grainy, or ball-shaped; caused by moving the lead while cooling or insufficient heat transfer.

## 3. Desoldering Techniques
- **Copper Desoldering Braid (Wick):** Saturate the braid with liquid flux, place it over the solder fillet, press down with a flat chisel iron tip, and lift both together once the solder wicks upward.
- **Solder Sucker (Vacuum Desoldering Pump):** Heat the joint completely, position the Teflon nozzle directly over the liquid solder, and release the vacuum plunger.
            """.trimIndent(),
            tagsJson = "[\"Soldering\", \"Desoldering\", \"Flux\", \"PCB\", \"Repair\"]",
            orderIndex = 2
        ),
        KnowledgePackArticleEntity(
            id = "devices_power_rail_shorts",
            packId = "devices_repairs",
            title = "Power Rail Diagnostics & Short Circuit Isolation",
            section = "Diagnostics",
            content = """
# Power Rail Diagnostics & Short Circuit Isolation

Short-circuits on motherboard power rails typically occur when an SMD ceramic capacitor (MLCC) cracks or a MOSFET bridge suffers punch-through breakdown.

## 1. Initial Resistance to Ground Check
1. Switch multimeter to Resistance mode (lowest range: 200Ω).
2. Connect black probe to chassis ground.
3. Probe the output inductor / coil of each major voltage regulator stage:
   - Input Rail (12V or 19V): Normal reading is megaohms or kiloohms. Below 5Ω indicates a dead short.
   - Core Rails (CPU Vcore / GPU Core): Normal reading is naturally low (1Ω - 15Ω) due to high current silicon logic. Do not mistake normal core resistance for a short circuit.
   - 3.3V and 5V Standby Rails: Normal reading is above 100Ω.

## 2. Voltage Injection Fault Isolation Method
1. Set a bench DC power supply to **1.0V** and current limit to **1.0A**.
   - *Crucial Rule:* Never inject voltage higher than the nominal rail rating! For CPU rails, never exceed 1.0V.
2. Solder ground wire to board ground. Solder positive wire to the shorted inductor pad.
3. Slowly raise current limit while monitoring current draw.
4. Locate the defective component:
   - **Thermal Imaging:** The shorted capacitor or MOSFET will glow white-hot immediately.
   - **Isopropyl Alcohol (IPA) Evaporation:** Spray 99% IPA across the board. The shorted component dissipates electrical energy as heat (${'$'}P = I^2 \cdot R${'$'}), causing the alcohol on it to evaporate within seconds while surrounding parts remain wet.
            """.trimIndent(),
            tagsJson = "[\"Power Supply\", \"Short Circuit\", \"Capacitors\", \"Diagnostics\", \"MOSFET\"]",
            orderIndex = 3
        ),

        // =================================================================
        // 3. LEARNING (learning)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "learning_physics_mechanics",
            packId = "learning",
            title = "Classical Mechanics & Laws of Conservation",
            section = "Physics",
            content = """
# Classical Mechanics & Laws of Conservation

Classical mechanics describes the motion of macroscopic objects under the influence of forces. It is grounded in Sir Isaac Newton's three laws of motion and the conservation laws of energy and momentum.

## 1. Newton's Three Laws of Motion
1. **First Law (Law of Inertia):** An object at rest remains at rest, and an object in motion continues in motion with constant velocity along a straight line, unless acted upon by a net external force (${'$'}\sum \vec{F} = 0 \implies \vec{a} = 0${'$'}).
2. **Second Law (Fundamental Law of Dynamics):** The acceleration of an object is directly proportional to the net force acting upon it and inversely proportional to its mass:
   ${'$'}${'$'}\vec{F}_{\text{net}} = m \cdot \vec{a} = \frac{d\vec{p}}{dt}${'$'}${'$'}
   Units: Force in Newtons (N), where ${'$'}1\,\text{N} = 1\,\text{kg}\cdot\text{m/s}^2${'$'}.
3. **Third Law (Action and Reaction):** For every action, there is an equal and opposite reaction:
   ${'$'}${'$'}\vec{F}_{A \to B} = -\vec{F}_{B \to A}${'$'}${'$'}

## 2. Conservation of Mechanical Energy
In an isolated system subject only to conservative forces (such as gravity and ideal springs), total mechanical energy (${'$'}E${'$'}) remains constant over time:
${'$'}${'$'}E = E_k + E_p = \text{constant}${'$'}${'$'}
- **Kinetic Energy:** ${'$'}E_k = \frac{1}{2}m v^2${'$'}
- **Gravitational Potential Energy:** ${'$'}E_p = m \cdot g \cdot h${'$'} (where ${'$'}g \approx 9.81\,\text{m/s}^2${'$'})

## 3. Conservation of Linear Momentum
The total linear momentum (${'$'}\vec{p} = m \vec{v}${'$'}) of a closed system without external impulses is strictly conserved:
${'$'}${'$'}\sum m_i \vec{v}_{i,\text{before}} = \sum m_i \vec{v}_{i,\text{after}}${'$'}${'$'}
- **Elastic Collision:** Both total kinetic energy and momentum are conserved (${'$'}e = 1${'$'}).
- **Inelastic Collision:** Momentum is conserved, but kinetic energy is partially converted into internal thermal energy or deformation (${'$'}e < 1${'$'}).
            """.trimIndent(),
            tagsJson = "[\"Physics\", \"Mechanics\", \"Newton\", \"Energy\", \"Momentum\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "learning_chem_periodic_table",
            packId = "learning",
            title = "Periodic Table Principles & Chemical Bonding",
            section = "Chemistry",
            content = """
# Periodic Table Principles & Chemical Bonding

The periodic table organizes all known chemical elements in order of increasing atomic number (${'$'}Z${'$'}, the number of protons in the nucleus). Periodic recurrence of chemical properties arises from electron configurations in outer valence shells.

## 1. Table Structure & Periodic Trends
- **Periods (Horizontal Rows 1 to 7):** Indicate the principal quantum number (${'$'}n${'$'}) of the highest occupied electron shell.
- **Groups (Vertical Columns 1 to 18):** Elements within the same column have identical numbers of valence electrons, leading to similar bonding behaviors.
  - Group 1: Alkali Metals (Li, Na, K - highly reactive, oxidation state +1).
  - Group 2: Alkaline Earth Metals (Be, Mg, Ca - oxidation state +2).
  - Group 17: Halogens (F, Cl, Br, I - strong oxidizers, valence 7, oxidation state -1).
  - Group 18: Noble Gases (He, Ne, Ar - complete octet, exceptionally unreactive).

## 2. Fundamental Periodic Trends
- **Atomic Radius:** Decreases across a period (left to right) due to increasing nuclear charge pulling electrons inward; increases down a group due to additional electron shielding shells.
- **Electronegativity (Pauling Scale):** The tendency of an atom to attract shared electrons in a bond. Increases left-to-right (Fluorine has highest at 3.98) and decreases down a group.

## 3. Chemical Bonding Types
- **Ionic Bond:** Formed when the electronegativity difference between two atoms is large (${'$'}\Delta\text{EN} > 1.7${'$'}). Complete transfer of one or more valence electrons forms cations and anions held by electrostatic attraction (e.g., ${'$'}\text{Na}^+ + \text{Cl}^- \to \text{NaCl}${'$'}).
- **Covalent Bond:** Formed when atoms share electron pairs (${'$'}\Delta\text{EN} < 1.7${'$'}).
  - Non-polar covalent: Equal sharing (${'$'}\text{O}_2, \text{CH}_4${'$'}).
  - Polar covalent: Unequal sharing producing partial charges (${'$'}\text{H}_2\text{O}${'$'}).
            """.trimIndent(),
            tagsJson = "[\"Chemistry\", \"Periodic Table\", \"Bonds\", \"Electrons\", \"Elements\"]",
            orderIndex = 2
        ),

        // =================================================================
        // 4. PROGRAMMING (programming)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "programming_kotlin_coroutines",
            packId = "programming",
            title = "Kotlin Coroutines & Structured Concurrency",
            section = "Kotlin & Android",
            content = """
# Kotlin Coroutines & Structured Concurrency

Kotlin coroutines provide lightweight, suspendable execution primitives that run asynchronous code sequentially without blocking the underlying OS thread.

## 1. Core Primitives
- **Suspend Functions:** Marked with the `suspend` keyword. Can yield execution at suspension points without locking the thread.
- **CoroutineScope:** Defines the lifetime and boundaries of coroutines launched within it.
- **Job:** A handle to a coroutine that tracks lifecycle state (Active, Completing, Completed, Cancelled).

## 2. Dispatchers
Dispatchers specify which thread or thread pool executes the coroutine:
- `Dispatchers.Main`: Android main thread. Used strictly for UI updates and small callbacks. Never run disk I/O or heavy computation here.
- `Dispatchers.IO`: Backed by an elastic thread pool optimized for off-loading blocking I/O (file reading/writing, database queries, network operations).
- `Dispatchers.Default`: Optimized for CPU-intensive computation (JSON parsing, image resizing, heavy list transformations).

## 3. Structured Concurrency Principles
Coroutines adhere to hierarchical parent-child relationships:
1. A parent scope does not complete until all its child coroutines finish.
2. Cancelling a parent scope immediately cancels all child jobs recursively.
3. If a child coroutine fails with an uncaught exception, it propagates the failure upward, cancelling sibling coroutines and the parent.
4. **SupervisorJob:** Use a `SupervisorJob()` when child failures should be isolated and not trigger catastrophic cancellation of other tasks (e.g., handling independent network requests).

## 4. Cooperative Cancellation
Coroutines must cooperatively check for cancellation:
- Standard library functions like `delay()`, `yield()`, and `ensureActive()` check `Job.isActive` and throw `CancellationException` when cancelled.
- In CPU-intensive loops, periodically call `ensureActive()` or check `if (!isActive) break`.
            """.trimIndent(),
            tagsJson = "[\"Kotlin\", \"Coroutines\", \"Android\", \"Concurrency\", \"Threads\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "programming_room_database",
            packId = "programming",
            title = "Room Database Architecture & Migration Guidelines",
            section = "Android Persistence",
            content = """
# Room Database Architecture & Migration Guidelines

Android's Room persistence library provides an abstraction layer over SQLite that allows robust database access while harnessing the full power of SQLite.

## 1. Core Architectural Components
- **Entity:** Represents a table within the database. Annotated with `@Entity(tableName = "name")`. Each persistent property maps to a database column.
- **DAO (Data Access Object):** Defines database interactions using Kotlin functions annotated with `@Query`, `@Insert`, `@Update`, and `@Delete`.
- **Database:** Extends `RoomDatabase`. Lists entities and version number; exposes abstract DAO getters.

## 2. Reactive Data Streams with Flow
DAOs can return `Flow<List<Entity>>`. Room monitors database tables and automatically re-queries and emits fresh snapshots whenever corresponding rows are inserted, updated, or deleted, creating reactive UI pipelines.

## 3. SQLite Migrations
When changing database schemas (adding tables or altering columns), increment the database version and provide a `Migration(start, end)` object:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
    }
}
```
*Best Practice:* Always avoid destructive migrations (`fallbackToDestructiveMigration`) in production releases to prevent user data loss during app updates.
            """.trimIndent(),
            tagsJson = "[\"Room\", \"SQLite\", \"Database\", \"Android\", \"Persistence\"]",
            orderIndex = 2
        ),

        // =================================================================
        // 5. LANGUAGES (languages)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "languages_grammar_frameworks",
            packId = "languages",
            title = "Core Grammatical Frameworks & Syntactic Structures",
            section = "Linguistics",
            content = """
# Core Grammatical Frameworks & Syntactic Structures

Language structure relies on systematic patterns of words, morphemes, and clauses that govern meaning and communicative clarity.

## 1. Syntactic Typology
Languages differ in canonical word order for transitive sentences (Subject, Verb, Object):
- **SVO (Subject-Verb-Object):** English, Mandarin Chinese, Spanish, Russian. (e.g., "The engineer designed the circuit").
- **SOV (Subject-Object-Verb):** Japanese, Korean, Hindi, Turkish, Latin. (e.g., "The engineer the circuit designed").
- **VSO (Verb-Subject-Object):** Standard Arabic, Irish, Classical Hebrew.

## 2. Verb Tense, Aspect, and Mood (TAM)
- **Tense:** Locates an action in time relative to the speech event (Past, Present, Future).
- **Aspect:** Expresses how the action extends over time:
  - *Simple:* Complete fact ("I wrote the report").
  - *Continuous/Progressive:* Ongoing activity ("I was writing the report").
  - *Perfect:* Completed state with continuing relevance ("I have written the report").
- **Mood:** The speaker's attitude toward the event:
  - *Indicative:* Expressing factual statements ("It is raining").
  - *Subjunctive:* Expressing hypothesis, wish, or counter-factuality ("If I were in your position...").
  - *Imperative:* Expressing commands or requests ("Close the terminal").

## 3. Technical Translation & Register
When translating technical literature across languages:
- Maintain terminology consistency: Map technical definitions to standardized international glossaries (e.g., ISO, IEEE).
- Beware of **False Friends (Cognates):** Words in different languages that look similar but carry divergent meanings (e.g., Spanish *actual* means "current/present", not English "actual/real").
            """.trimIndent(),
            tagsJson = "[\"Linguistics\", \"Grammar\", \"Syntax\", \"Translation\", \"Language\"]",
            orderIndex = 1
        ),

        // =================================================================
        // 6. FIRST AID & EMERGENCY RESPONSE (first_aid)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "first_aid_cpr_adult",
            packId = "first_aid",
            title = "Cardiopulmonary Resuscitation (CPR) & Airway Obstruction",
            section = "Emergency Resuscitation",
            content = """
# Cardiopulmonary Resuscitation (CPR) & Airway Obstruction

When a casualty is unresponsive and not breathing normally (or only gasping), immediate high-quality CPR maintains oxygenated blood flow to the brain and vital organs until definitive care arrives.

## 1. Initial Assessment Protocol (DRSABCD)
1. **D - Danger:** Ensure the scene is safe for yourself, bystanders, and the casualty.
2. **R - Response:** Check for consciousness. Tap shoulders firmly and ask loudly: "Can you hear me? Open your eyes!"
3. **S - Send for Help:** Call emergency services (112, 911, or local dispatch) or direct a bystander to call and locate an Automated External Defibrillator (AED).
4. **A - Airway:** Open the airway using the head-tilt/chin-lift maneuver. Check the mouth for visible foreign blockages.
5. **B - Breathing:** Look, listen, and feel for normal chest rise for no more than 10 seconds. Agonal gasps are not normal breathing.
6. **C - CPR:** Immediately begin chest compressions if unbreathing.
7. **D - Defibrillation:** Attach AED pads as soon as available and follow audio prompts.

## 2. High-Quality Chest Compressions
- **Positioning:** Place the heel of one hand in the center of the chest (lower half of sternum) and interlock the other hand on top. Keep arms straight and shoulders positioned directly over the hands.
- **Compression Depth:** Compress the chest at least 5 cm (2 inches), but no more than 6 cm in adults.
- **Compression Rate:** 100 to 120 compressions per minute (tempo of "Stayin' Alive").
- **Full Recoil:** Allow the chest to fully re-expand between compressions to facilitate cardiac refill; do not lean on the chest.
- **Ratio:** 30 chest compressions followed by 2 rescue breaths. If untrained or unwilling to deliver rescue breaths, deliver continuous hands-only CPR.

## 3. Foreign Body Airway Obstruction (Choking)
- **Mild Obstruction (Casualty can speak, cough loudly):** Encourage vigorous coughing. Do not interfere.
- **Severe Obstruction (Silent, clutching neck, cyanotic lips):**
  1. Stand to the side and slightly behind the casualty. Support chest with one hand and lean them forward.
  2. Deliver up to **5 sharp back blows** between the shoulder blades with the heel of your hand.
  3. If back blows fail, deliver up to **5 abdominal thrusts (Heimlich maneuver)**: Position a fist just above the navel, grasp with the other hand, and pull inward and upward sharply.
  4. Repeat sequence. If casualty becomes unresponsive, lower carefully to the ground and initiate CPR immediately.
            """.trimIndent(),
            tagsJson = "[\"CPR\", \"Cardiac Arrest\", \"Choking\", \"BLS\", \"Emergency\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "first_aid_hemorrhage_tourniquet",
            packId = "first_aid",
            title = "Massive Hemorrhage Control & Tourniquet Application",
            section = "Trauma Management",
            content = """
# Massive Hemorrhage Control & Tourniquet Application

Arterial hemorrhage can cause fatal exsanguination in under 3 minutes. Stop the bleeding before any other intervention.

## 1. Stepwise Bleeding Control Hierarchy
1. **Immediate Firm Direct Pressure:** Press directly onto the bleeding site using sterile gauze, clean cloth, or gloved hands. Lean your full body weight into the wound.
2. **Wound Packing (Junctional Areas - Groin, Axilla, Neck):**
   - Pack hemostatic gauze (quikclot/kaolin) or standard gauze tightly into the wound cavity directly against the bleeding vessel.
   - Maintain continuous direct pressure over the packed wound for at least 3 minutes (5 minutes for non-hemostatic gauze).
3. **Tourniquet (Extremity Wounds):** Apply immediately for life-threatening limb hemorrhage.

## 2. Tourniquet Application Procedure (Combat Application Tourniquet - CAT)
1. Place the tourniquet **2 to 3 inches above the bleeding site**, directly on the skin (never over a joint; if bleeding location is unclear in the dark, place "high and tight" on the limb).
2. Pull the self-adhering band tightly to eliminate all slack before turning the windlass.
3. Twist the windlass rod until the arterial bleeding stops completely and distal pulse vanishes.
4. Lock the windlass rod into the clip.
5. Secure the friction buckle strap and write the exact time of application on the time tag (e.g., "TK 14:35").

## 3. Critical Tourniquet Safety Rules
- **Never loosen or remove a tourniquet in the field.** Removal must only be performed by medical personnel in a surgical setting.
- An improperly applied loose tourniquet acts as a venous tourniquet, increasing bleeding rather than stopping it.
- Pain is expected and indicates effective arterial occlusion.
            """.trimIndent(),
            tagsJson = "[\"Bleeding\", \"Tourniquet\", \"Trauma\", \"Wounds\", \"Hemostatic\"]",
            orderIndex = 2
        ),
        KnowledgePackArticleEntity(
            id = "first_aid_burns_treatment",
            packId = "first_aid",
            title = "Burn Classification & Emergency Treatment",
            section = "Wound Care",
            content = """
# Burn Classification & Emergency Treatment

Immediate cooling and sterile dressing minimize tissue damage and infection risk following thermal, chemical, or electrical burns.

## 1. Burn Depth Classification
- **First-Degree (Superficial):** Affects only the epidermis. Red, painful, dry, no blisters (e.g., mild sunburn).
- **Second-Degree (Partial-Thickness):** Involves epidermis and dermis. Redness, severe pain, edema, and fluid-filled blisters.
- **Third-Degree (Full-Thickness):** Extends through all skin layers into subcutaneous tissue. Leathery, charred black or waxy white, often painless at the center due to nerve destruction.

## 2. Emergency Management Steps
1. **Extinguish & Remove Source:** Smother flames, disconnect electrical supply, or brush dry chemicals off before flushing.
2. **Cool with Clean Running Water:** Apply cool running tap water (15°C - 20°C) over the burn for **at least 20 minutes**. Do this within the first 3 hours of injury.
   - *Warning:* Never use ice or ice-cold water; extreme cold constricts capillary blood flow, deepening tissue necrosis.
3. **Remove Constricting Items:** Gently take off rings, watches, belts, and tight clothing before swelling develops. Do not peel off clothing stuck to the wound.
4. **Cover the Burn:** Cover loosely with a sterile, non-adherent dressing, clean polyethylene plastic food wrap (applied lengthways, not wrapped tightly), or clean damp cloth.
5. **Keep Casualty Warm:** Extensive burns impair thermoregulation; wrap unburned areas in blankets to prevent hypothermia.

## 3. Do NOT Practices
- Do not apply butter, oil, grease, toothpaste, or home ointments.
- Do not burst intact blisters; the blister roof acts as a biological sterile barrier.
            """.trimIndent(),
            tagsJson = "[\"Burns\", \"Thermal\", \"Chemical\", \"First Aid\", \"Wounds\"]",
            orderIndex = 3
        ),
        KnowledgePackArticleEntity(
            id = "first_aid_fractures_splinting",
            packId = "first_aid",
            title = "Fractures, Dislocations & Field Splinting",
            section = "Musculoskeletal",
            content = """
# Fractures, Dislocations & Field Splinting

Immobilizing suspected bone fractures and joint dislocations prevents further soft tissue trauma, reduces pain, and prevents closed fractures from puncturing through the skin.

## 1. Signs of a Fracture
- Severe localized pain and tenderness.
- Visible deformity, unnatural angulation, or limb shortening.
- Swelling, bruising (ecchymosis), and inability to bear weight.
- Crepitus (grating sensation of bone ends touching).

## 2. Neurovascular Examination (PMS Protocol)
Always assess and record **PMS** distal to the injury **before and after** applying any splint:
1. **P - Pulse:** Check distal arterial pulse (radial pulse for arm, dorsalis pedis for leg).
2. **M - Motor:** Ask casualty to gently wiggle fingers or toes.
3. **S - Sensory:** Touch extremities and ask casualty if they feel it without looking.
- If distal pulse is absent, emergency reduction or repositioning may be required by trained medics.

## 3. Splinting Rules
- **Immobilize the Joint Above and Below:** For a mid-shaft forearm fracture, immobilize both the wrist and the elbow. For an ankle fracture, immobilize the foot and the knee.
- **Pad the Splint:** Pad rigid items (sticks, rolled magazines, padded boards, SAM splints) with cloth to eliminate pressure sores over bony prominences.
- **Secure in Position Found:** Never attempt to forcefully straighten a dislocated joint or push exposed bone back into an open fracture.
- **Open Fractures:** Cover exposed bone ends with sterile moist gauze before splinting.
            """.trimIndent(),
            tagsJson = "[\"Fracture\", \"Splint\", \"Bone\", \"Sprain\", \"Trauma\"]",
            orderIndex = 4
        ),

        // =================================================================
        // 7. FIELD ELECTRONICS & HARDWARE TROUBLESHOOTING (field_electronics)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "field_multimeter_diagnostics",
            packId = "field_electronics",
            title = "Digital Multimeter Diagnostics & Component Testing",
            section = "Test & Measurement",
            content = """
# Digital Multimeter Diagnostics & Component Testing

A digital multimeter (DMM) is the primary diagnostic instrument for troubleshooting unpowered and powered electronic assemblies.

## 1. Core Operating Modes
- **DC Voltage (V=):** Measures steady potential difference. Red probe to circuit node, black probe to common ground (GND). Use for verifying battery charge, power supplies, and logic rails (3.3V, 5V, 12V).
- **Continuity (Beeper):** Emits an audible tone when loop resistance is below approximately 30Ω - 50Ω. Used for tracing broken PCB tracks, blown fuses, and finding solder bridges.
- **Resistance (Ω):** Measures passive component impedance without power applied. Always isolate at least one pin of a resistor when testing in-circuit to avoid parallel paths.
- **Diode Mode:** Injects a constant current (~1mA) and displays the forward voltage drop (V_f):
  - Silicon PN Junction: 0.5V to 0.7V.
  - Schottky Diode: 0.15V to 0.35V.
  - Germanium Diode: 0.2V to 0.3V.
  - Open or Reverse Biased: "OL" (Over Limit).
  - Shorted Diode: 0.00V in both directions.

## 2. In-Circuit Capacitor Testing
1. Disconnect all power and safely discharge high-voltage electrolytic capacitors through a 1kΩ 5W power resistor.
2. In Resistance mode, connect probes across the capacitor:
   - Reading should start low, then continuously climb toward infinity (OL) as the internal DMM battery charges the capacitor.
   - If resistance stays near 0Ω: Capacitor is shorted internally (common failure mode of ceramic MLCCs).

## 3. MOSFET and BJT Transistor Quick Tests
- **NPN BJT:** With meter in Diode Mode, black probe on Base, red probe on Collector and Emitter should read OL. Red probe on Base, black probe on Collector and Emitter should both read ~0.65V.
- **N-Channel MOSFET:** Black probe on Source, red probe on Drain reads ~0.5V (internal body diode). Reverse should read OL. Touch red probe to Gate, then red probe to Drain: MOSFET should conduct (reading drops to 0.0V).
            """.trimIndent(),
            tagsJson = "[\"Multimeter\", \"Voltage\", \"Continuity\", \"Diode\", \"Resistance\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "field_lithium_battery_pack",
            packId = "field_electronics",
            title = "Lithium-Ion & LiFePO4 Battery Pack Diagnostics",
            section = "Power Systems",
            content = """
# Lithium-Ion & LiFePO4 Battery Pack Diagnostics

Lithium secondary cells store high chemical energy density. Proper cell balancing, cutoff voltages, and Battery Management System (BMS) diagnostics are critical to avoid thermal runaway.

## 1. Operating Voltage Thresholds per Cell
| Chemistry | Nominal Voltage | Full Charge (Cutoff) | Storage Voltage | Absolute Minimum (Discharged) |
|---|---|---|---|---|
| **Li-Ion (NMC / Cobalt)** | 3.6V - 3.7V | 4.20V ± 0.05V | 3.82V | 2.80V (Below 2.5V damages cell) |
| **LiFePO4 (Iron Phosphate)**| 3.2V | 3.65V ± 0.05V | 3.30V | 2.50V (Extremely safe chemistry) |
| **Li-Po (Pouch Cells)** | 3.7V | 4.20V (4.35V LiHV) | 3.85V | 3.00V |

## 2. Common Battery Pack Failure Modes
1. **Cell Imbalance in Series Strings (e.g. 3S, 4S, 7S):**
   - Measure each cell via balance connector. Imbalance greater than **50mV (0.05V)** reduces effective capacity. Imbalance over **200mV** trips BMS shutdown.
   - Rebalance using a dedicated balance charger or CC-CV single cell supply.
2. **BMS Lockout State:**
   - Many BMS boards enter protection mode if any single cell drops below 2.5V or shorts.
   - To reset a tripped BMS: Connect appropriate charging voltage across the main P+ and P- terminals for 5 seconds to re-engage the discharge MOSFETs.
3. **Physical Swelling & Dendrites:**
   - Any pouch cell showing gas swelling ("pillowing") has suffered electrolyte decomposition. Discontinue use immediately; do not puncture.
            """.trimIndent(),
            tagsJson = "[\"Battery\", \"18650\", \"Lithium\", \"BMS\", \"Safety\"]",
            orderIndex = 2
        ),

        // =================================================================
        // 8. WILDERNESS SURVIVAL & BUSHCRAFT NAVIGATION (wilderness_survival)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "survival_water_purification",
            packId = "wilderness_survival",
            title = "Wilderness Water Sourcing, Filtration & Purification",
            section = "Hydration",
            content = """
# Wilderness Water Sourcing, Filtration & Purification

Humans can survive approximately 3 weeks without food, but only 3 days without water. In wilderness conditions, unpurified water harbors pathogenic protozoa, bacteria, and viruses.

## 1. Biological Contaminants & Target Sizes
- **Protozoan Cysts (Giardia, Cryptosporidium):** 1 to 15 microns. Resistant to standard chlorine tablets; neutralized reliably by boiling or sub-micron ceramic/hollow-fiber membrane filters.
- **Bacteria (E. coli, Salmonella, Cholera):** 0.2 to 2 microns. Removed by 0.1-micron microfilters or heat.
- **Viruses (Hepatitis A, Norovirus, Rotavirus):** 0.02 to 0.1 microns. Too small for mechanical microfilters; neutralized by boiling, UV radiation, or chemical halogens.

## 2. Field Purification Methods
1. **Rolling Boil (The Gold Standard):**
   - Bring water to a vigorous rolling boil for **1 full minute** at sea level.
   - Above 2,000 meters (6,500 feet), boil for **3 full minutes** to compensate for reduced atmospheric pressure and lower boiling points.
2. **Improvised Multi-Layer Biofilter (Clarification):**
   - Cut the bottom off a plastic bottle and invert it.
   - Layer 1 (Bottom): Clean cloth or cotton.
   - Layer 2: Crushed wood charcoal from a campfire (adsorbs organic chemical odors and impurities).
   - Layer 3: Fine clean sand.
   - Layer 4: Coarse sand and gravel.
   - *Note:* Filtering removes suspended sediment and turbidity, but filtered water must still be boiled or chemically treated to kill pathogens.
3. **SODIS (Solar Water Disinfection):**
   - Fill clean, transparent PET bottles with clear water and expose to direct sunlight for **6 hours** (or 2 days if cloudy). UV-A rays and solar thermal heating destroy waterborne pathogens.
            """.trimIndent(),
            tagsJson = "[\"Water\", \"Purification\", \"Filtration\", \"Boiling\", \"Survival\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "survival_celestial_navigation",
            packId = "wilderness_survival",
            title = "Celestial Navigation Using Crux (Southern Cross) & Polaris",
            section = "Navigation",
            content = """
# Celestial Navigation Using Crux (Southern Cross) & Polaris

When magnetic compasses or GPS satellite receivers fail, celestial navigation using key circumpolar constellations provides absolute cardinal direction anywhere on Earth.

## 1. Southern Hemisphere: Finding True South via Crux (Southern Cross)
The constellation **Crux** is a distinctive 4-star kite/cross pattern accompanied by two bright "Pointer Stars" (Alpha and Beta Centauri).

### The Projection Method:
1. Locate the four primary stars of the Southern Cross: Acrux (bottom), Gacrux (top), Mimosa (left), and Imai (right).
2. Trace an imaginary straight line extending through the long axis of the cross, from **Gacrux through Acrux**, extending outward **4.5 times** the distance between these two stars.
3. Draw a second line between the two bright **Pointer Stars** (Alpha and Beta Centauri), and construct a perpendicular bisector line from its midpoint.
4. The intersection where the Crux long-axis line meets the Pointer bisector is the **South Celestial Pole (SCP)**.
5. Drop your gaze vertically downward from the South Celestial Pole directly to the horizon: **that point on the horizon is True South**.
6. Turn 180° around to face True North.

## 2. Northern Hemisphere: Finding True North via Polaris
Polaris (the North Star) sits almost directly above the geographic North Pole.

### The Pointer Stars Method:
1. Locate the **Big Dipper** (Ursa Major, the Plough).
2. Find the two outer stars of the bowl furthest from the handle: **Merak** (lower bowl) and **Dubhe** (upper bowl).
3. Draw a straight line from Merak through Dubhe and extend it outward approximately **5 times** the distance between them.
4. You will hit **Polaris**, a moderately bright star situated at the tip of the Little Dipper's handle (Ursa Minor).
5. Drop vertically from Polaris to the horizon: **that point on the horizon is True North**.
            """.trimIndent(),
            tagsJson = "[\"Navigation\", \"Crux\", \"Polaris\", \"Stars\", \"Compass\", \"Orientation\"]",
            orderIndex = 2
        ),

        // =================================================================
        // 9. PROGRAMMING & DEVELOPER CHEAT SHEETS (dev_cheatsheets)
        // =================================================================
        KnowledgePackArticleEntity(
            id = "dev_kotlin_coroutines",
            packId = "dev_cheatsheets",
            title = "Kotlin Coroutines & Flow Essential Cheat Sheet",
            section = "Android & Kotlin",
            content = """
# Kotlin Coroutines & Flow Essential Cheat Sheet

## 1. Builders and Dispatchers
```kotlin
// Launch fire-and-forget job
val job = scope.launch(Dispatchers.Default) { ... }

// Async with deferred result
val deferred = scope.async(Dispatchers.IO) { fetchData() }
val data = deferred.await()

// Dispatcher Selection
Dispatchers.Main        // UI operations & LiveData/StateFlow emission
Dispatchers.IO          // Network requests, Room DB, SAF file I/O (elastic thread pool)
Dispatchers.Default     // Heavy CPU calculation, JSON parsing, image downsampling
Dispatchers.Unconfined  // Starts on caller thread, resumes on suspension thread
```

## 2. StateFlow vs SharedFlow
- **StateFlow:** Hot stream, always holds one state value, replay cache of 1, conflates duplicate values, ideal for UI state (`val state = _state.asStateFlow()`).
- **SharedFlow:** Hot stream, zero default replay, ideal for one-off events (navigation, toast, snackbar).

## 3. Cooperative Cancellation
Coroutines are cooperatively cancellable. Always verify `isActive` or call `yield()` in CPU loops:
```kotlin
while (isActive) {
    // Process chunk
    ensureActive() // Throws CancellationException if job is cancelled
}
```
            """.trimIndent(),
            tagsJson = "[\"Kotlin\", \"Coroutines\", \"Flow\", \"Android\", \"Concurrency\"]",
            orderIndex = 1
        ),
        KnowledgePackArticleEntity(
            id = "dev_git_power_tools",
            packId = "dev_cheatsheets",
            title = "Git Version Control Master Reference",
            section = "Developer Tools",
            content = """
# Git Version Control Master Reference

## 1. Branching & Rebasing
```bash
# Rebase feature branch on top of latest master
git checkout feature
git fetch origin master
git rebase origin/master

# Interactive rebase last 4 commits (squash, edit, reword)
git rebase -i HEAD~4

# Abort an in-flight conflict rebase cleanly
git rebase --abort
```

## 2. Disaster Recovery with Reflog
Git never deletes committed objects immediately. The `reflog` tracks every HEAD movement:
```bash
# View chronological history of HEAD changes
git reflog

# Restore branch to state before an accidental reset or deleted branch
git reset --hard HEAD@{3}
```

## 3. Stashing & Cherry-Picking
```bash
# Stash untracked and modified files with description
git stash push -u -m "WIP: offline parser"

# Apply and drop most recent stash
git stash pop

# Pick specific commit into current branch
git cherry-pick <commit-sha>
```
            """.trimIndent(),
            tagsJson = "[\"Git\", \"VCS\", \"Rebase\", \"Reflog\", \"CLI\"]",
            orderIndex = 2
        ),
        KnowledgePackArticleEntity(
            id = "dev_regex_quickref",
            packId = "dev_cheatsheets",
            title = "Regular Expressions (Regex) Syntax & Token Guide",
            section = "Text Processing",
            content = """
# Regular Expressions (Regex) Syntax & Token Guide

## 1. Anchors and Boundaries
- `^` : Start of string (or line in multiline mode).
- `$` : End of string (or line in multiline mode).
- `\b` : Word boundary (transition between word and non-word character).
- `\B` : Non-word boundary.

## 2. Character Classes
- `.` : Any character except newline.
- `\d` : Digit `[0-9]`.
- `\D` : Non-digit `[^0-9]`.
- `\w` : Word character `[a-zA-Z0-9_]`.
- `\s` : Whitespace (space, tab, newline).
- `[a-z]` : Character range.
- `[^a-z]` : Negated character range.

## 3. Quantifiers
- `*` : 0 or more (greedy).
- `+` : 1 or more (greedy).
- `?` : 0 or 1 (optional).
- `*?` / `+?` : Non-greedy (lazy) matching.
- `{n}` : Exactly n times.
- `{n,m}` : Between n and m times.

## 4. Lookaround Assertions (Zero-Width)
- Positive Lookahead: `X(?=Y)` (matches X only if followed by Y).
- Negative Lookahead: `X(?!Y)` (matches X only if NOT followed by Y).
- Positive Lookbehind: `(?<=Y)X` (matches X only if preceded by Y).
- Negative Lookbehind: `(?<!Y)X` (matches X only if NOT preceded by Y).
            """.trimIndent(),
            tagsJson = "[\"Regex\", \"String\", \"Pattern\", \"Parsing\", \"CLI\"]",
            orderIndex = 3
        )
    )
}
