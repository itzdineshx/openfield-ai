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
        )
    )
}
