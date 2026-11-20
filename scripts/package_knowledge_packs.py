#!/usr/bin/env python3
"""
Full comprehensive generator for CRUX Knowledge Packs.
Produces rich, authoritative articles across all 9 domains.
Generates:
1. scripts/package_knowledge_packs.py
2. Runs package_knowledge_packs.py to build catalog/packs/*.zip and catalog/packs.json
3. android/app/src/main/java/com/openfield/app/feature/library/KnowledgePackContent.kt
4. android/app/src/main/java/com/openfield/app/feature/library/KnowledgePackManager.kt (CURATED_CATALOG with hashes)
"""

import json
import os
import re
import sys
import hashlib
import zipfile
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
SCRIPTS_DIR = BASE_DIR / "scripts"
CATALOG_DIR = BASE_DIR / "catalog"
PACKS_DIR = CATALOG_DIR / "packs"
KOTLIN_DIR = BASE_DIR / "android/app/src/main/java/com/openfield/app/feature/library"

# ==============================================================================
# AUTHORITATIVE PACK SPECIFICATIONS
# ==============================================================================
PACKS = [
    # --------------------------------------------------------------------------
    # 1. FIRST AID & EMERGENCY RESPONSE (AHA / ERC / TCCC / WMS)
    # --------------------------------------------------------------------------
    {
        "id": "first_aid",
        "title": "First Aid & Emergency Response",
        "description": "Clinical trauma protocols, CPR/BLS, hemorrhage control, burn staging, fractures, anaphylaxis, and envenomation.",
        "category": "Medical & Safety",
        "version": "1.1.0",
        "icon_color_hex": 0xFFE53935,
        "topics": ["CPR & Resuscitation", "Hemorrhage & Tourniquets", "Burns & Thermal Care", "Fractures & Splinting", "Anaphylaxis", "Environmental Shock", "Venom & Bites"],
        "provenance": "CRUX Curated Corpus (AHA / ERC / TCCC Guidelines)",
        "articles": [
            {
                "id": "first_aid_cpr_adult",
                "title": "Cardiopulmonary Resuscitation (CPR) & Airway Obstruction",
                "section": "Emergency Resuscitation",
                "tags": ["CPR", "Cardiac Arrest", "BLS", "AED", "Choking"],
                "content": """# Cardiopulmonary Resuscitation (CPR) & Airway Obstruction

When a casualty is unresponsive with absent or abnormal breathing (agonal gasps), high-quality CPR provides critical coronary and cerebral perfusion pressure until spontaneous circulation returns or defibrillation occurs.

## 1. Initial Assessment Protocol (DRSABCD)
1. **D - Danger:** Ensure the environment is safe from electrical hazards, toxic gas, fire, and unstable structures.
2. **R - Response:** Firmly tap shoulders and shout loudly into both ears: "Are you okay? Open your eyes!"
3. **S - Send for Help:** Dial emergency dispatch (112, 911) or point directly at a bystander: "You in the blue jacket, call emergency services and find an AED!"
4. **A - Airway:** Perform the head-tilt/chin-lift maneuver (or jaw-thrust without neck extension if cervical spinal injury is suspected). Inspect mouth for visible obstructions; clear solids with a finger sweep.
5. **B - Breathing:** Look, listen, and feel for normal chest rise for 5 to 10 seconds. Agonal gasps (infrequent, irregular snorting) must be treated as cardiac arrest.
6. **C - CPR (Compressions):** Immediately place the casualty supine on a firm, flat surface and initiate chest compressions.
7. **D - Defibrillation:** Apply an Automated External Defibrillator (AED) as soon as it arrives without interrupting compressions.

## 2. High-Quality Chest Compression Metrics (AHA / ERC Standard)
- **Hand Position:** Heel of dominant hand centered on the lower half of the sternum; second hand interlocked over the first. Keep elbows locked straight with shoulders positioned directly above hands.
- **Compression Depth:** 5 to 6 cm (2 to 2.4 inches) in adults. Never exceed 6 cm.
- **Compression Rate:** 100 to 120 compressions per minute (approximate tempo of "Stayin' Alive" or 2 beats per second).
- **Complete Chest Recoil:** Allow the chest to fully return to resting neutral position between compressions. Leaning on the chest impairs venous refill and reduces cardiac output by up to 35%.
- **Compression-to-Ventilation Ratio:** 30 compressions to 2 rescue breaths (each delivered over 1 second, watching for chest rise). If untrained or without a barrier mask, deliver **Continuous Hands-Only CPR**.
- **Rescuer Fatigue Rotation:** Switch compressors every 2 minutes (every 5 cycles of 30:2) to maintain compression depth.

## 3. Automated External Defibrillator (AED) Operations
1. Power on the AED immediately.
2. Expose the bare chest; wipe dry with a cloth if wet.
3. Apply self-adhesive electrode pads:
   - Upper Right: Below right clavicle, lateral to sternum.
   - Lower Left: Mid-axillary line, 5th intercostal space below the left pectoral.
   - *Special Conditions:* If an implanted pacemaker/defibrillator bulge is visible, place pad at least 2.5 cm (1 inch) away. Shave excessive chest hair if pads fail to adhere.
4. Clear casualty while the AED analyzes cardiac rhythm.
5. If shock is advised: Shout "Clear!", ensure no one touches the casualty, and press the flashing shock button.
6. **Immediately resume chest compressions** starting with 30 compressions; do not pause to check pulse.

## 4. Foreign Body Airway Obstruction (Choking)
- **Partial/Mild Obstruction (Casualty can speak, cough vigorously):** Encourage coughing. Do not deliver back blows or thrusts while they are coughing effectively.
- **Complete/Severe Obstruction (Cannot speak, silent panic, universal distress sign clutching throat):**
  1. Stand behind casualty, support chest with one arm, and lean them forward.
  2. Deliver **5 sharp back blows** between the shoulder blades using the heel of your hand.
  3. If unrelieved, place the thumb side of your fist midway between the navel and xiphoid process. Grasp fist with your other hand and deliver **5 rapid inward and upward abdominal thrusts (Heimlich maneuver)**.
  4. Repeat cycle of 5 back blows and 5 abdominal thrusts.
  5. If casualty collapses unconscious: Lower them gently to the floor, call emergency dispatch, and initiate CPR (starting with chest compressions). Check airway before rescue breaths."""
            },
            {
                "id": "first_aid_hemorrhage_tourniquet",
                "title": "Massive Hemorrhage Control & Tourniquet Application",
                "section": "Trauma Management",
                "tags": ["Hemorrhage", "Bleeding", "Tourniquet", "Trauma", "TCCC"],
                "content": """# Massive Hemorrhage Control & Tourniquet Application

Uncontrolled arterial hemorrhage can cause irreversible hypovolemic shock and exsanguination in under 180 seconds. In tactical and remote trauma, stopping severe external bleeding precedes all other interventions.

## 1. Classifying External Hemorrhage
- **Arterial Bleeding:** Bright red blood spurting pulsatile under high hydraulic pressure. Highest immediate fatality risk.
- **Venous Bleeding:** Dark red blood flowing steadily without pulsations. Can still lead to rapid exsanguination from large junctional veins (e.g., femoral, jugular).
- **Capillary Bleeding:** Oozing blood from superficial dermis; self-limiting via clotting cascade.

## 2. Stepwise Hemostasis Hierarchy (TCCC MARCH Algorithm)
1. **Direct Manual Pressure:**
   - Press both hands directly onto the bleeding vessel with full body weight.
   - Use sterile gauze, a clean cloth, or gloved fingertips pushed deep into the wound track.
2. **Windlass Tourniquet (Limb Extremities):**
   - The primary intervention for severe extremity trauma, partial/complete amputations, or complex fractures with vascular involvement.
3. **Wound Packing (Junctional Wounds - Groin, Axilla, Neck Base):**
   - Tourniquets cannot occlude junctional vessels. Pack hemostatic gauze (Kaolin-impregnated QuikClot or Chitosan Celox) or plain gauze deep into the wound cavity directly against the bleeding vessel.
   - Pack tightly until the wound void is completely filled.
   - Maintain continuous direct pressure over the pack for **3 full minutes** (5 minutes for non-hemostatic standard gauze).
   - Secure with a pressure bandage (e.g., Israeli Emergency Bandage).

## 3. Combat Application Tourniquet (CAT) Standard Operating Procedure
1. **Placement:**
   - In active emergency/darkness: Place "high and tight" on the proximal limb over clothing.
   - Once bleeding site is clearly exposed: Place **5 to 7 cm (2 to 3 inches) above the proximal bleeding wound**, directly on bare skin. Never place directly over a joint (elbow or knee).
2. **Routing:** Thread self-adhering band through the friction buckle and pull as tightly as humanly possible before engaging the windlass rod. Eliminating slack in this step is critical.
3. **Windlass Tightening:** Twist the windlass rod until bright red arterial bleeding ceases completely and the distal peripheral pulse vanishes.
4. **Locking:** Clip the windlass rod into the retaining C-hook and pull the windlass strap across to lock it in place.
5. **Time Documentation:** Clearly write the exact time of application on the white time tag (e.g., "T = 14:35").

## 4. Critical Safety Rules & Myths
- **Never loosen or intermittently release a tourniquet:** Periodic loosening reintroduces arterial blood loss and washes out clotting factors while failing to save ischemic tissue. A tourniquet can remain safely applied for up to **2 hours** without significant risk of permanent nerve or muscle ischemia.
- **Pain is Expected:** A properly applied tourniquet causes intense pain due to arterial occlusion. Do not loosen it because the casualty complains of pain.
- **Secondary Tourniquet:** If bleeding does not stop completely with one tourniquet, apply a second tourniquet immediately proximal (above) the first."""
            },
            {
                "id": "first_aid_burns_treatment",
                "title": "Burn Classification, Fluid Staging & Treatment",
                "section": "Thermal Care",
                "tags": ["Burns", "Thermal", "Chemical", "Electrical", "Wound"],
                "content": """# Burn Classification, Fluid Staging & Treatment

Prompt thermal deceleration and infection prophylaxis determine survival and functional recovery after major thermal, electrical, or chemical injuries.

## 1. Depth Staging
- **Superficial (1st Degree):** Confined to epidermis. Erythematous, dry, blanchable, hypersensitive, no bullae (blisters). Heals in 3 to 6 days without scarring.
- **Partial-Thickness (2nd Degree):** Extends into dermis.
  - *Superficial Partial:* Wet, clear blister formation, intensely painful, pink/mottled base.
  - *Deep Partial:* White/pale base, impaired sensation, variable capillary refill.
- **Full-Thickness (3rd Degree):** Complete destruction of epidermis and dermis into subcutaneous fat. Leathery, charred black or pearly waxy white, dry, non-blanching, anesthetic (painless) at the central burn core due to nerve terminal necrosis.
- **4th Degree:** Extends through deep fascia into underlying skeletal muscle, tendon, and cortical bone.

## 2. Rule of Nines (Adult Total Body Surface Area - TBSA Estimation)
- Head and Neck: 9%
- Anterior Torso (Chest + Abdomen): 18%
- Posterior Torso (Back + Buttocks): 18%
- Each Upper Extremity (Arm): 9% (18% both)
- Each Lower Extremity (Leg): 18% (36% both)
- Perineum/Genitalia: 1%
- Palmar Surface of Casualty's Hand (including fingers): approximately 1% TBSA (useful for scattered burns).

## 3. Acute On-Scene Management
1. **Stop the Burning Process:** Extinguish flames (Stop, Drop, Roll), isolate electrical current before touching casualty, or brush off dry chemical powders before irrigation.
2. **Active Thermal Deceleration (20-Minute Clean Water Rule):**
   - Cool the burn using clean, cool running water (15°C to 20°C) for **at least 20 minutes**.
   - Must be initiated within 3 hours of injury to reduce depth of tissue necrosis.
   - *Contraindication:* **Never use ice or freezing water.** Extreme cold induces reflex peripheral vasoconstriction, deepening the thermal burn into ischemic necrosis and causing severe hypothermia.
3. **Constrictive Item Removal:** Remove jewelry, watches, rings, belts, and boots immediately before massive third-space capillary leak edema develops.
4. **Wound Dressing:** Cover loosely with clean, non-adherent sterile dressings or clear food-grade polyethylene cling wrap applied longitudinally (never circumferential wrapping, which creates a tourniquet effect as tissue swells).
5. **Core Temperature Preservation:** Once cooled, wrap unburned body surfaces in clean, dry blankets to combat severe post-burn hypothermia.

## 4. Inhalation Injury Red Flags
Suspect airway burns in closed-space fires. Look for: singed facial/nasal hairs, carbonaceous sputum, hoarseness, inspiratory stridor, and pharyngeal edema. Requires immediate emergency transport and early endotracheal intubation before complete laryngeal closure."""
            },
            {
                "id": "first_aid_fractures_splinting",
                "title": "Fractures, Dislocations & Field Splinting",
                "section": "Musculoskeletal",
                "tags": ["Fracture", "Splint", "Trauma", "Sprain", "Compartment"],
                "content": """# Fractures, Dislocations & Field Splinting

Immobilizing skeletal trauma prevents bone fragments from lacerating adjacent neurovascular bundles, reduces muscle spasm, minimizes blood loss, and prevents closed injuries from converting to open fractures.

## 1. Classification
- **Closed Fracture:** Skin barrier remains intact over the fracture site.
- **Open (Compound) Fracture:** Bone pierces through the skin or an external wound communicates with the fracture hematoma. High risk of osteomyelitis; cover bone with sterile moist gauze; do NOT push exposed bone back inside.
- **Dislocation:** Complete displacement of bone ends forming a joint.

## 2. Neurovascular Protocol (PMS Pre- and Post-Splint Assessment)
Always verify and document **PMS** distally before and immediately after applying any splint or bandage:
1. **P - Pulse:** Check distal arterial pulsation (radial pulse for upper limb; dorsalis pedis / posterior tibial for lower limb). Compare bilaterally.
2. **M - Motor:** Test voluntary distal muscular activation ("Wiggle your fingers", "Flex your toes toward your nose").
3. **S - Sensory:** Lightly touch the casualty's distal digits with eyes closed ("Which finger am I touching?").
- *Critical finding:* If a pulse disappears after splinting, immediately loosen the splint bindings and readjust.

## 3. The 5 Ps of Compartment Syndrome (Orthopedic Emergency)
Elevated pressure within a closed osteofascial compartment compromises capillary perfusion:
1. **Pain:** Disproportionate to injury and refractory to analgesics; exacerbated by passive stretch of muscles.
2. **Pallor:** Pale, cool extremity with prolonged capillary refill.
3. **Paresthesia:** "Pins and needles" or numbness along nerve pathways.
4. **Paralysis:** Inability to actively contract compartment muscle groups (late sign).
5. **Pulselessness:** Absence of distal pulse (late sign indicating impending tissue death).

## 4. Fundamental Rules of Splinting
- **Immobilize the Joint Above and Joint Below:** To splint a tibial shaft fracture, immobilize both the knee and ankle. To splint a radial/ulnar shaft fracture, immobilize both the wrist and elbow.
- **Pad all Bony Prominences:** Cushion rigid splint material (boards, SAM splints, branches) with soft clothing or gauze around the malleoli, patella, and olecranon to avoid pressure necrosis.
- **Splint in the Position Found:** Do not attempt to force a deformed or dislocated joint into anatomical position in the field unless distal pulse is absent and definitive evacuation is delayed hours."""
            },
            {
                "id": "first_aid_anaphylaxis_epipen",
                "title": "Anaphylaxis Recognition & Epinephrine Auto-Injector Administration",
                "section": "Allergy & Immunology",
                "tags": ["Anaphylaxis", "Epinephrine", "EpiPen", "Allergy", "Airway"],
                "content": """# Anaphylaxis Recognition & Epinephrine Auto-Injector Administration

Anaphylaxis is a severe, systemic, life-threatening type-I hypersensitivity reaction involving massive histamine release, vasodilation, airway bronchoconstriction, and distributive shock.

## 1. Clinical Recognition Criteria
Anaphylaxis is highly likely when any **one** of the following criteria is met following allergen exposure (stings, nuts, shellfish, medications):
1. **Acute skin/mucosal symptoms** (generalized hives, pruritus, facial edema, swollen lips/tongue/uvula) **PLUS at least one of:**
   - Respiratory compromise: Dyspnea, wheezing, stridor, hypoxemia.
   - Hypotension / End-organ dysfunction: Syncope, collapse, diaphoresis, incontinence.
2. **Two or more symptoms occurring rapidly after exposure:**
   - Cutaneous: Urticaria, flushing, angioedema.
   - Respiratory: Throat tightness, coughing, bronchospasm.
   - Cardiovascular: Tachycardia, hypotension, dizziness.
   - Gastrointestinal: Severe abdominal cramps, persistent vomiting.

## 2. Epinephrine: The Sole First-Line Pharmacotherapy
- **Dose:**
  - Adults and children >30 kg: **0.3 mg** intramuscularly (IM).
  - Pediatric 15 to 30 kg: **0.15 mg** intramuscularly (IM).
- **Injection Site:** **Anterolateral middle-third of the thigh (Vastus Lateralis muscle)**.
  - Epinephrine must NEVER be given subcutaneously or into the gluteal muscle; vastus lateralis vascularity provides the most rapid therapeutic plasma peak (under 8 minutes).
  - Can be injected directly through clothing/jeans if necessary.

## 3. Step-by-Step Auto-Injector Administration
1. Form a fist around the injector (never place thumb over either end).
2. Remove the safety release cap (usually blue or yellow at the top).
3. Position the orange/black needle tip perpendicular (90°) against the outer thigh.
4. Push down firmly until a loud click is heard or felt.
5. **Hold firmly in place for 3 to 10 seconds** to ensure complete drug delivery.
6. Remove unit and gently massage the injection site for 10 seconds.
7. Record time of delivery and keep used device to give to paramedics.

## 4. Secondary Dosing and Posture
- **Refractory Symptoms:** If symptoms fail to improve or deteriorate within **5 to 15 minutes**, administer a second dose of epinephrine in the opposite thigh.
- **Patient Positioning:** Keep casualty supine with legs elevated to maintain venous return. Do NOT allow an anaphylactic patient to stand up or walk; sudden upright posture can cause catastrophic pulseless electrical activity (Empty Heart Syndrome). If vomiting or in respiratory distress, place in recovery position or seated upright."""
            },
            {
                "id": "first_aid_heat_cold_emergencies",
                "title": "Environmental Emergencies: Heat Stroke & Hypothermia Staging",
                "section": "Environmental Care",
                "tags": ["Hypothermia", "Heat Stroke", "Frostbite", "Environment", "Cold"],
                "content": """# Environmental Emergencies: Heat Stroke & Hypothermia Staging

Thermal dysregulation occurs when metabolic heat production and environmental heat transfer overwhelm the body's thermoregulatory mechanisms in the hypothalamus.

## 1. Heat Illness Spectrum
### A. Heat Exhaustion
- Core temperature <40°C (<104°F).
- Symptoms: Profuse diaphoresis (sweating), nausea, headache, tachycardia, orthostatic lightheadedness.
- Mental Status: **Intact**. Casualty is oriented.
- Field Treatment: Move to shade, loosen clothing, elevate legs, sip cool electrolyte fluid.

### B. Exertional / Classic Heat Stroke (Life-Threatening Emergency)
- Core body temperature **>40°C (>104°F)**.
- Key Diagnostic Differentiator: **Altered Mental Status (CNS dysfunction)**: Confusion, delirium, ataxia, seizures, or coma.
- Skin: Can be hot and dry, but exertional victims are often still sweating profusely.
- **Field Treatment (Cool First, Transport Second):**
  - **Cold Water Immersion (Gold Standard):** Submerge casualty up to neck in ice water bath with continuous stirring. Reduces core temp by 0.2°C per minute.
  - If immersion is unavailable: Strip clothing, continuously pour water over body, vigorously fan, and place ice packs at high-perfusion areas (axillae, groin, neck).
  - Target: Cool rapidly until core temp reaches 38.5°C (101.3°F), then halt active cooling to prevent hypothermic overshoot.

## 2. Hypothermia Staging (Swiss Staging System)
- **Stage I - Mild (32°C to 35°C):**
  - Conscious, shivering violently, normal or mildly impaired coordination, tachycardic.
  - Management: Passive rewarming (dry clothing, windproof shell, warm sweet non-caffeinated drinks).
- **Stage II - Moderate (28°C to 32°C):**
  - Shivering ceases, apathy, confusion, severe muscle rigidity, dilated pupils.
  - Management: Active external rewarming of trunk (chemical heat pads on chest and back; never on bare extremities).
- **Stage III - Severe (24°C to 28°C):**
  - Unconscious, profound bradycardia, hypotension, barely perceptible respirations.
  - Management: Full body insulation, gentle handling.
- **Stage IV - Apparent Death (<24°C):**
  - Asystole or pulseless electrical activity. "A casualty is not dead until warm and dead." Initiate prolonged CPR until rewarming can occur in a medical facility.

## 3. The "Afterdrop" Phenomenon
When cold, stagnant blood from vasoconstricted extremities returns suddenly to the core during rapid limb rewarming, it causes a sharp secondary temperature drop in the myocardium, frequently inducing fatal Ventricular Fibrillation (VF). Always warm the central trunk (chest, abdomen) first, and never massage or vigorously rub cold extremities."""
            },
            {
                "id": "first_aid_snakebite_envenomation",
                "title": "Snakebite Envenomation & Pressure Immobilization (PIT)",
                "section": "Venom & Toxicology",
                "tags": ["Snakebite", "Venom", "PIT", "Envenomation", "Toxins"],
                "content": """# Snakebite Envenomation & Pressure Immobilization (PIT)

Venomous snakebites deliver complex biochemical cocktails of neurotoxins, hemotoxins, cytotoxins, and myotoxins into the subcutaneous lymphatic system. Correct field stabilization prevents systemic distribution.

## 1. Toxin Classifications
- **Neurotoxic (Elapids - Cobras, Kraits, Coral Snakes, Mambas, Australian Elapids):** Blocks neuromuscular junctions. Causes ptosis (drooping eyelids), diplopia, dysphagia, progressive flaccid paralysis, and respiratory failure.
- **Hemotoxic & Cytotoxic (Viperids - Rattlesnakes, Vipers, Pit Vipers, Adders):** Destroys vascular endothelium, consumes fibrinogen (consumptive coagulopathy), causes massive local edema, bullae formation, necrosis, and systemic hemorrhage.

## 2. The Pressure Immobilization Technique (PIT)
*Recommended for all neurotoxic elapid bites and Australian venomous species. Slows lymphatic flow without occluding arterial blood supply.*

1. Apply a broad elastic roller bandage (10 to 15 cm wide) directly over the bite site as firmly as for a sprained ankle (approx. 40 to 70 mmHg pressure).
2. Extend the wrap continuously all the way down the limb (to fingers or toes) and then back up the limb to the groin or axilla.
3. Check distal pulse: Circulation must remain intact; digits must not turn cold, pale, or cyanotic.
4. Apply a rigid splint to immobilize the entire limb.
5. **Absolute Physical Rest:** The casualty must NOT walk, move, or contract muscles; lymphatic valves are driven by skeletal muscle contraction. Carry the casualty or arrange immediate air evacuation.

## 3. Critical Contraindications (Harmful Myths to Avoid)
- **DO NOT apply an arterial tourniquet for snakebites:** High pressure causes localized ischemic necrosis and massive tissue loss.
- **DO NOT cut or incise the fang punctures:** Introduces secondary bacterial infections, causes severe local arterial bleeding, and does not extract venom.
- **DO NOT attempt mouth or mechanical device suction:** Less than 0.1% of venom is aspirated, and mouth bacteria contaminate the puncture.
- **DO NOT apply ice or electric shocks:** Cryotherapy exacerbates local necrosis and permanent tissue destruction.
- **DO NOT attempt to capture or kill the snake:** Avoids secondary bites; take a digital photo from a safe distance (minimum 2 meters) for anti-venom matching."""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 2. FIELD ELECTRONICS & HARDWARE (IPC / IEEE / JEDEC)
    # --------------------------------------------------------------------------
    {
        "id": "field_electronics",
        "title": "Field Electronics & Hardware Troubleshooting",
        "description": "Multimeter component testing, lithium battery diagnostics, SMPS topologies, IPC soldering standards, and field oscilloscope usage.",
        "category": "Hardware & Engineering",
        "version": "1.1.0",
        "icon_color_hex": 0xFFFFA000,
        "topics": ["Multimeter Diagnostics", "Lithium Chemistry & BMS", "SMPS Power Rails", "IPC Soldering Standards", "Field Signal Integrity", "Wire Splicing"],
        "provenance": "CRUX Curated Corpus (IPC-A-610 / IEEE Standards)",
        "articles": [
            {
                "id": "field_multimeter_diagnostics",
                "title": "Digital Multimeter Diagnostics & In-Circuit Component Testing",
                "section": "Test & Measurement",
                "tags": ["Multimeter", "Voltage", "Continuity", "Diode", "Resistance", "Capacitance"],
                "content": """# Digital Multimeter Diagnostics & In-Circuit Component Testing

A high-input-impedance (>=10 MΩ) Digital Multimeter (DMM) allows diagnosis of passive and active components without loading low-power reference circuits.

## 1. Operating Modes & Measurement Procedures
- **DC Voltage (V⎓):** Measures potential difference. Connect black lead to common chassis ground (GND) and red lead to test points.
  - *Measurement Rule:* Logic signals (1.8V, 3.3V, 5V) should remain within ±3% tolerance under nominal load. A drooping rail (e.g. 3.3V rail measuring 2.7V) indicates excessive load or high series resistance in a ferrite bead.
- **Continuity (Beeper):** Alerts with a tone when circuit loop resistance is beneath approximately 30Ω to 50Ω.
  - *Field Caution:* Always perform continuity checks on unpowered systems. Residual DC charges trigger false buzzers or blow meter input fuses.
- **Resistance (Ω):** In-circuit measurements read the equivalent parallel resistance of all attached loop branches ($R_{eq} = (R_1 \\cdot R_2) / (R_1 + R_2)$). To verify precision tolerance resistors, lift at least one pin from the solder pad.
- **Diode Mode (Forward Voltage Drop $V_f$):** Injects a calibrated 1mA current and measures forward junction barrier potential:
  - Standard Silicon PN Junction: `0.55V to 0.70V`.
  - Schottky Diode (Fast Barrier): `0.18V to 0.35V`.
  - Germanium Diode: `0.22V to 0.30V`.
  - Gallium Arsenide LED: Red (`1.6V - 2.0V`), Green/Blue/White (`2.8V - 3.4V`).
  - *Short:* `0.000V` in both directions indicates semiconductor breakdown.
  - *Open:* Displays `OL` (Over Limit) in both directions.

## 2. In-Circuit Ceramic Capacitor (MLCC) Short Testing
Multi-layer ceramic capacitors frequently fail as dead shorts due to microscopic thermal cracking or voltage spike breakdown:
1. De-energize circuit completely and discharge bulk capacitors through a 1 kΩ 5W power resistor.
2. In Resistance mode, measure between the power rail and ground.
3. If reading is <2Ω, a short exists. Because dozens of decoupling capacitors sit in parallel on a power bus, pinpoint the shorted capacitor using:
   - **Voltage Injection Method:** Inject 1.0V with current limited to 1.5A into the rail. Spray 99% Isopropyl Alcohol (IPA). The defective capacitor boils and evaporates the alcohol within seconds while healthy components stay wet.

## 3. Transistor Field Testing (BJT & MOSFET)
- **NPN Bipolar Junction Transistor (Diode Mode):**
  - Red probe on Base: Black probe on Collector reads `~0.65V`, Black probe on Emitter reads `~0.68V`.
  - Reverse probes: All combinations read `OL`.
- **N-Channel Enhancement MOSFET:**
  - Black probe on Source, Red probe on Drain reads `0.45V to 0.60V` (internal body diode). Reverse reads `OL`.
  - Touch Red probe momentarily to Gate (charges internal input capacitance $C_{iss}$).
  - Return Red probe to Drain: MOSFET now conducts; meter reads `0.00V` (Rds-on active).
  - Discharge Gate by touching Gate and Source together with fingers; Drain-to-Source returns to `OL`."""
            },
            {
                "id": "field_lithium_battery_pack",
                "title": "Lithium-Ion & LiFePO4 Battery Pack Diagnostics & BMS Restoration",
                "section": "Power Systems",
                "tags": ["Battery", "Lithium", "LiFePO4", "18650", "BMS", "Safety"],
                "content": """# Lithium-Ion & LiFePO4 Battery Pack Diagnostics & BMS Restoration

Secondary lithium cells possess high specific energy density. Understanding chemistry voltage boundaries, cell balancing tolerances, and Battery Management System (BMS) trip states prevents catastrophic thermal runaway.

## 1. Chemical Specifications Comparison
| Parameter | Li-Ion (NMC / Cobalt 18650/21700) | LiFePO4 (Lithium Iron Phosphate) | Li-Po (Lithium Polymer Pouch) |
|---|---|---|---|
| **Nominal Voltage** | 3.6V - 3.7V per cell | 3.2V per cell | 3.7V per cell |
| **Max Charge Cutoff** | 4.20V ± 0.05V | 3.65V ± 0.05V | 4.20V (4.35V for LiHV) |
| **Storage Voltage** | 3.82V - 3.85V | 3.30V | 3.85V |
| **Min Discharge Cutoff**| 2.80V (Damage below 2.5V) | 2.50V (Damage below 2.0V) | 3.00V |
| **Thermal Runaway Temp**| ~150°C - 180°C | >270°C (Extremely stable) | ~140°C |

## 2. Diagnosing Cell Imbalance in Series Strings (e.g. 3S, 4S, 7S, 13S)
- Access the multi-pin white JST balance connector on the battery pack.
- Measure voltage of each individual cell sequentially between consecutive balance pins ($B_0-B_1, B_1-B_2, B_2-B_3$).
- **Tolerance Standards:**
  - Optimal Pack: Delta between highest and lowest cell is **<20mV (0.02V)**.
  - Mild Degradation: Delta between 20mV and 80mV. Charge capacity is throttled by the weakest cell.
  - Critical Fault: Delta **>150mV (0.15V)**. BMS will trigger premature low-voltage cutoff during discharge or overvoltage cutoff during charge.

## 3. BMS Lockout State & "Jumpstart" Procedure
When a single cell drops beneath the low-voltage cutoff threshold (typically 2.4V for Li-ion), the protection IC enters hard lockout mode, turning off both the charge and discharge MOSFET gates (reading 0.0V at the main P+ / P- output terminals).
1. Isolate the pack from loads and inspect cells physically. If any cell is bloated, dented, or below 1.5V, do not revive (internal copper dendrite short risk).
2. If cells are resting safely between 2.2V and 2.5V:
   - Connect a bench power supply set to nominal pack voltage with current limited to **50mA to 100mA** (trickle pre-charge rate).
   - Apply power for 30 to 60 seconds across P+ and P- to trigger the BMS threshold detector.
   - Once cell voltages rise above 2.8V, the BMS restores normal switching mode.

## 4. C-Rating and Safe Current Delivery
- Discharge Current formula: $I_{max} = C_{rating} \\times Capacity_{Ah}$.
  - A 2500mAh (2.5Ah) cell rated at 10C can deliver $2.5 \\times 10 = 25A$ continuous load.
  - Operating above manufacturer C-rating causes severe I²R resistive heating and accelerated SEI layer breakdown."""
            },
            {
                "id": "field_smps_power_supplies",
                "title": "Switched-Mode Power Supply (SMPS) Topologies & Fault Isolation",
                "section": "Power Electronics",
                "tags": ["SMPS", "Buck", "Boost", "Inductor", "Power Supply"],
                "content": """# Switched-Mode Power Supply (SMPS) Topologies & Fault Isolation

Switched-Mode Power Supplies provide DC-DC voltage conversion with high thermal efficiency (85% to 96%) using high-frequency pulse-width modulation (PWM), energy-storage inductors, and synchronous MOSFETs.

## 1. Core Conversion Topologies
- **Buck (Step-Down):** Produces lower output voltage ($V_{out} = D \\cdot V_{in}$, where $D$ is duty cycle $0 < D < 1$).
  - High-side MOSFET switches input voltage; inductor and low-side synchronous FET (or freewheeling Schottky diode) filter current into output capacitor bank.
- **Boost (Step-Up):** Produces higher output voltage ($V_{out} = V_{in} / (1 - D)$).
  - Inductor stores magnetic energy during switch ON-time; discharges through diode into output capacitors during switch OFF-time.
- **Buck-Boost / SEPIC:** Inverts or regulates output voltage above or below input rail.

## 2. Systematic Troubleshooting Workflow
1. **Verify Input Voltage & Inrush Limiter:**
   - Check input fuse or reverse-polarity protection P-channel MOSFET.
   - Inspect input NTC thermistor (cold resistance typically 5Ω to 10Ω; drops to <0.5Ω when hot).
2. **Inspect the Switching Node (LX / SW Pad):**
   - In a functioning buck converter, the junction between the high-side FET, low-side FET, and inductor must toggle between 0V and $V_{in}$ at the switching frequency (typically 300 kHz to 2 MHz).
   - If SW node is pegged at 0V: High-side FET is open, controller has no VCC, or Enable pin is pulled low.
   - If SW node is shorted to $V_{in}$: High-side FET has suffered drain-source punch-through, sending raw unmitigated input voltage directly to downstream logic ICs!
3. **Feedback Resistor Divider ($R_{fb1}, R_{fb2}$):**
   - Output voltage is governed by internal bandgap reference $V_{ref}$ (typically 0.6V or 0.8V):
     $$V_{out} = V_{ref} \\cdot \\left(1 + \\frac{R_1}{R_2}\\right)$$
   - If output voltage drifts high: $R_2$ is open or has increased in resistance.
   - If output voltage is lower than nominal: $R_1$ has drifted high or feedback trace has leakage to ground.

## 3. Inductor Saturation & Capacitor Degradation
- **Inductor Saturation:** When inductor current exceeds saturation rating ($I_{sat}$), core permeability drops precipitously, turning the inductor into a plain copper wire loop. This causes instantaneous current spikes and destroys switching MOSFETs.
- **Electrolytic Output Capacitor Dry-Out:** High ripple currents boil liquid electrolyte over time, dramatically increasing Equivalent Series Resistance (ESR) and generating high output ripple voltage that resets sensitive microcontrollers."""
            },
            {
                "id": "field_wire_splicing_weatherproofing",
                "title": "High-Reliability Wire Splicing & Weatherproofing (Lineman Splice)",
                "section": "Field Workmanship",
                "tags": ["Splice", "Soldering", "Lineman", "Weatherproof", "Wiring"],
                "content": """# High-Reliability Wire Splicing & Weatherproofing (Lineman Splice)

Field-spliced conductor joints must withstand physical tensile strain, vibration, temperature cycling, and moisture ingress without degradation of electrical conductivity.

## 1. The Western Union (Lineman) Splice
*The NASA and military aviation benchmark for joining solid or stranded conductors subject to mechanical tension.*

1. **Stripping:** Strip 3 to 4 cm (1.2 to 1.5 inches) of insulation from both wire ends without nicking or cutting copper conductor strands.
2. **Crossed Alignment:** Cross the two exposed conductors at right angles, approximately one-third of the distance from the insulation jacket.
3. **Interlocking Twist:** Twist the wires together for 2 to 3 interlocking center turns.
4. **Wrapping:** Wrap the remaining ends tightly around the straight opposite conductor for at least **4 to 5 complete, tight, closed coils**.
5. **Trim & Crimp:** Snip wire tips flush so sharp ends do not puncture heat-shrink insulation.
6. **Soldering:** Apply rosin flux, heat the center of the splice with a soldering iron tip, and feed solder until it flows completely into the inter-strand gaps via capillary action.

## 2. Weatherproofing & Moisture Sealing Hierarchy
1. **Dual-Wall Adhesive-Lined Heat Shrink (Marine Grade):**
   - Slip tubing over conductor prior to splicing.
   - Standard single-wall heat shrink allows capillary moisture ingress along the wire strands.
   - Dual-wall tubing contains an inner liner of hot-melt thermoplastic adhesive that liquifies when heated (at 120°C) and forms an airtight, hermetic moisture barrier.
2. **Self-Amalgamating Silicone Tape:**
   - Non-adhesive silicone rubber that chemically bonds to itself when stretched by 200% and wrapped with 50% overlap.
   - Resistant to UV degradation, ozone, and temperatures up to 260°C.
3. **Dielectric Silicone Compound:**
   - Coat terminal spade and screw terminals with pure dielectric silicone grease to displace atmospheric moisture and prevent galvanic corrosion between dissimilar metals (e.g. copper wire under steel screw)."""
            },
            {
                "id": "field_oscilloscope_signal_integrity",
                "title": "Field Oscilloscope Operation & Signal Integrity Diagnostics",
                "section": "Instrumentation",
                "tags": ["Oscilloscope", "Signal", "Frequency", "Probes", "Noise"],
                "content": """# Field Oscilloscope Operation & Signal Integrity Diagnostics

A digital storage oscilloscope (DSO) converts transient electrical voltages into two-dimensional graphical time-domain displays ($V$ vs $t$), allowing isolation of noise glitches, clock jitter, and bus protocol corruptions.

## 1. Probe Attenuation (1X vs 10X Selection)
- **10X Mode (Standard Operational Setting):**
  - Inserts a 9 MΩ resistor in series with the oscilloscope's 1 MΩ input impedance, creating a 10:1 voltage divider.
  - Increases probe input impedance to **10 MΩ** and dramatically reduces input capacitance from ~100pF down to **10pF to 15pF**.
  - Reduces high-frequency circuit loading. Always use 10X for clock lines, SPI/I2C buses, and high-frequency digital signals.
- **1X Mode:**
  - Full signal amplitude; limited bandwidth (typically <10 MHz) and high capacitance (~100pF). Use only for ultra-low-voltage microvolt audio signals or small power rail ripple measurements.

## 2. Probe Low-Frequency Compensation (Square Wave Trimming)
Before making precision measurements, always calibrate passive probes using the oscilloscope's 1 kHz reference square wave test terminal:
- Connect probe tip and ground alligator clip to the calibrator loop.
- Use a non-conductive ceramic trimming screwdriver to adjust the trimmer capacitor on the probe BNC connector:
  - **Overcompensated:** Leading edge shows rounded overshoot spikes.
  - **Undercompensated:** Leading edge is sluggish and rolled off.
  - **Correctly Compensated:** Clean, flat horizontal tops with sharp 90° transitions.

## 3. Triggering Modes
- **Auto:** Sweeps display continuously even in the absence of a trigger event. Ideal for searching for baseline DC levels and unknown frequencies.
- **Normal:** Sweeps display **only** when input signal crosses the selected trigger voltage threshold with the chosen slope (Rising/Falling). Necessary for capturing non-periodic or bursty signals (e.g., UART packet bursts).
- **Single Shot:** Captures exactly one waveform acquisition when triggered and freezes memory buffer. Essential for recording power-on reset sequences, transient voltage spikes, and latch-up events.

## 4. Ground Lead Inductance & "Ground Bounce"
The standard 15 cm alligator ground lead has approximately 100 nH of parasitic inductance ($L_{lead}$). Fast digital transition edges ($di/dt$) induce voltage ringing:
$$V_{ring} = L_{lead} \\cdot \\frac{di}{dt}$$
- To observe true signal edges without synthetic ground ringing artifacts, remove the long alligator ground lead and clip on a short coaxial **ground spring contact** directly touching the ground pad adjacent to the signal test point."""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 3. WILDERNESS SURVIVAL & BUSHCRAFT (US Army FM 3-05.70 / NOLS)
    # --------------------------------------------------------------------------
    {
        "id": "wilderness_survival",
        "title": "Wilderness Survival & Bushcraft Navigation",
        "description": "Water filtration and purification, celestial navigation using Crux and Polaris, thermal debris shelters, firecraft, and emergency signaling.",
        "category": "Outdoors & Survival",
        "version": "1.1.0",
        "icon_color_hex": 0xFF2E7D32,
        "topics": ["Water Sourcing & Purification", "Celestial Navigation", "Thermal Shelters", "Friction Firecraft", "Ground-to-Air Signaling", "Bushcraft Knots"],
        "provenance": "CRUX Curated Corpus (US Army FM 3-05.70 Standards)",
        "articles": [
            {
                "id": "survival_water_purification",
                "title": "Wilderness Water Sourcing, Multi-Stage Filtration & Purification",
                "section": "Hydration Engineering",
                "tags": ["Water", "Purification", "Filtration", "Survival", "Pathogens"],
                "content": """# Wilderness Water Sourcing, Multi-Stage Filtration & Purification

Dehydration impairs cognitive judgment within 24 hours and becomes fatal in approximately 72 hours. Untreated surface water harbors biological pathogens capable of causing debilitating dysentery, cholera, and cryptosporidiosis.

## 1. Biological Contaminants Spectrum
- **Protozoan Cysts (Giardia lamblia, Cryptosporidium parvum):** 1 to 15 microns. Outer shell is impervious to standard chlorine and halogen tablets. Neutralized reliably by boiling or sub-micron mechanical microfilters.
- **Bacteria (Escherichia coli, Salmonella, Vibrio cholerae):** 0.2 to 2 microns. Removed by 0.1-micron hollow-fiber membrane filters or pasteurization.
- **Enteric Viruses (Hepatitis A, Norovirus, Rotavirus):** 0.02 to 0.1 microns. Too small for standard 0.1-micron wilderness backpacking microfilters. Neutralized by boiling, UV radiation, or chemical oxidizing agents.

## 2. Field Purification Methods
### A. The Vigorous Rolling Boil (Gold Standard)
- Bring water to a full, turbulent rolling boil for **1 complete minute** at sea level.
- At altitudes above 2,000 meters (6,500 feet), boil for **3 complete minutes** because lower atmospheric pressure reduces the boiling temperature of water (down to ~93°C).
- Kills 100% of all protozoa, bacteria, and viral pathogens.

### B. Improvised 4-Stage Charcoal Biofilter (Turbidity Clarification)
*Turbid, muddy water shields pathogens from chemical and UV treatment. Filter water before purifying.*
1. Invert a clean 1-liter PET plastic bottle with the bottom cut off.
2. **Layer 1 (Bottom/Neck):** Clean cotton cloth or porous sphagnum moss.
3. **Layer 2:** Crushed hardwood charcoal from campfire remnants (not briquettes with binders). Charcoal adsorbs agricultural chemicals, heavy organic toxins, and eliminates foul odors.
4. **Layer 3:** Fine, washed river sand (traps micro-particulates).
5. **Layer 4 (Top):** Coarse sand and small pea gravel (traps suspended silt, leaves, and debris).
- *Warning:* Filtered water is clarified of sediment, but MUST still be boiled or chemically treated to kill microscopic pathogens.

### C. SODIS (Solar Water Disinfection)
1. Fill clean, un-scratched transparent PET bottles with clear water (turbidity <30 NTU).
2. Expose bottles horizontally on a dark or reflective corrugated iron surface in direct sunlight for **at least 6 hours** (or 2 full days under overcast skies).
3. Synergistic action of UV-A radiation (320-400 nm) and elevated water temperature (>50°C) destroys pathogen DNA."""
            },
            {
                "id": "survival_celestial_navigation",
                "title": "Celestial Navigation Using Crux (Southern Cross) & Polaris",
                "section": "Navigation & Orientation",
                "tags": ["Navigation", "Crux", "Polaris", "Stars", "Compass", "Orientation"],
                "content": """# Celestial Navigation Using Crux (Southern Cross) & Polaris

When magnetic compasses suffer needle dip or GPS satellite receivers exhaust battery reserves, circumpolar star constellations provide an unjammable directional reference anywhere on Earth.

## 1. Southern Hemisphere: True South via Constellation Crux (The Southern Cross)
The constellation **Crux** is the smallest yet most distinctive constellation in the southern celestial hemisphere, accompanied by two brilliant Pointer Stars (Alpha and Beta Centauri).

### The Geometric Projection Method:
1. Identify the four primary stars forming the kite/cross pattern:
   - **Gacrux (Gamma Crucis):** Orange giant star at the top of the cross.
   - **Acrux (Alpha Crucis):** Blue-white binary star at the base/foot of the cross.
   - **Mimosa (Beta Crucis):** Eastern arm star.
   - **Imai (Delta Crucis):** Western arm star.
2. Trace an imaginary line along the long axis from **Gacrux through Acrux**, and extend it outward straight into the sky for **4.5 times** the distance between Gacrux and Acrux.
3. Locate the two bright **Pointer Stars** (Alpha and Beta Centauri). Draw a straight line connecting them, find the midpoint, and project a perpendicular bisector line outward.
4. The intersection where the 4.5× Crux extension line meets the Pointer bisector line marks the **South Celestial Pole (SCP)**.
5. Drop your line of sight vertically downward from the South Celestial Pole directly to the horizon: **that exact point on the horizon is True South**.
6. Face True South: East is 90° to your left, West is 90° to your right, and North is directly behind you (180°).

## 2. Northern Hemisphere: True North via Polaris (The North Star)
Polaris lies within 0.7° of the true north celestial pole.

### The Big Dipper Pointer Method:
1. Locate **Ursa Major** (the Big Dipper / Plough).
2. Identify the two outer stars of the cup furthest from the handle: **Merak** (lower bowl) and **Dubhe** (upper lip).
3. Draw an imaginary line from Merak through Dubhe and project it outward approximately **5 times** the distance between them.
4. You will intercept **Polaris**, a moderately bright 2nd-magnitude star at the tip of the Little Dipper's handle (Ursa Minor).
5. Drop vertically from Polaris to the horizon: **that point on the horizon is True North**."""
            },
            {
                "id": "survival_emergency_shelters",
                "title": "Thermal Debris Shelters & Conduction Loss Prevention",
                "section": "Shelter Engineering",
                "tags": ["Shelter", "Debris", "Hypothermia", "Insulation", "Cold"],
                "content": """# Thermal Debris Shelters & Conduction Loss Prevention

In severe weather, hypothermia can kill within 3 hours. The body loses thermal energy via four mechanisms: Conduction (contact with cold ground), Convection (wind chill stripping boundary layer), Radiation, and Evaporation.

## 1. The Debris Hut (Solo Survival Shelter)
*The most thermally efficient primitive shelter; requires no cordage or tools.*

1. **Ridgepole Selection:** Select a sturdy, dry log 3 to 3.5 meters (10 to 12 feet) long. Prop one end on a stump or rock fork approximately 1 meter off the ground.
2. **Bed Insulation (Critical Step):**
   - The cold ground conducts body heat away **24 times faster** than stationary air.
   - Pile dry leaves, pine needles, dried moss, or grass at least **60 cm to 90 cm (2 to 3 feet) deep** under the ridgepole. Compress it with your body to ensure at least 20 cm of compacted dead air space remains between you and the earth.
3. **Ribbing / Framing:** Lean sturdy sticks along both sides of the ridgepole at a 45° angle to create an A-frame skeleton. Keep the interior space narrow (a debris hut should fit your body like a sleeping bag).
4. **Lattice Layer:** Place smaller brush, branches, and twigs over the ribbing to prevent loose debris from falling through.
5. **Thermal Insulation Layer:**
   - Pile dry leaves, pine needles, and forest debris over the entire structure to a minimum thickness of **1 meter (3 feet)**.
   - Test: Push your arm into the pile up to your shoulder; if you touch the rib sticks before your armpit, the insulation is too thin.
6. **Entrance Stopper:** Gather a large bundle of leaves or a backpack to plug the entrance hole once inside, trapping body heat."""
            },
            {
                "id": "survival_friction_firecraft",
                "title": "Primitive Friction Firecraft & Ferrocerium Ignition",
                "section": "Fire & Energy",
                "tags": ["Fire", "Bow Drill", "Tinder", "Ferrocerium", "Ember"],
                "content": """# Primitive Friction Firecraft & Ferrocerium Ignition

Fire fulfills multiple survival requirements: thermoregulation, water purification, signaling, pathogen reduction in food, and predator deterrence.

## 1. The Fire Triangle & Combustion Stages
- **Tinder:** Fluffy, micro-fibrous dry plant material that ignites from a tiny spark or glowing ember (inner dry cedar bark, cattail fluff, charred cotton cloth, dried bracket fungus).
- **Kindling:** Matchstick to thumb-thickness dry twigs that bridge heat from burning tinder to main logs.
- **Fuel:** Wrist to thigh-thick hardwood logs that provide long-lasting radiant heat.

## 2. The Bow Drill Mechanism (Primitive Friction Fire)
1. **Spindle:** Straight hardwood cylinder, 20 cm long, 2 cm diameter. Carve top to a dull pencil point (low friction); carve base to a blunt round tip (high friction).
2. **Hearth Board:** Flat, dry softwood board (cedar, basswood, willow, poplar), 2 cm thick.
3. **Socket/Handhold:** Hardwood block or stone with a grease-lubricated depression to press down on the spindle.
4. **Bow:** Curved branch with non-stretch paracord looped with slight slack.
5. **Operation:**
   - Burn a pilot depression into the hearth board.
   - Cut a **60° V-notch** from the board edge into the center of the depression.
   - Place a dry leaf or bark flake under the notch to collect hot carbon dust.
   - Stroke the bow steadily with long, rhythmic passes.
   - Once thick, pungent white/grey smoke billows, increase stroke speed for 15 seconds until dust reaches **400°C (750°F)**.
   - A glowing red ember coalesces in the dust pile.
   - Transfer the ember into the center of your prepared tinder bird's nest; blow gently with steady breaths until flames erupt."""
            },
            {
                "id": "survival_emergency_signaling",
                "title": "Emergency Visual Signaling & Ground-to-Air Codes",
                "section": "Rescue Operations",
                "tags": ["Signaling", "Rescue", "Mirror", "SOS", "Aviation"],
                "content": """# Emergency Visual Signaling & Ground-to-Air Codes

Search and rescue (SAR) fixed-wing aircraft and helicopters fly at high speeds; active and passive ground visual distress signals maximize visibility across vast topography.

## 1. Ground-to-Air Emergency Visual Code (ICAO Standard)
Construct symbols using high-contrast materials (shadowed logs, bright rocks, stamped snow trenches):
- **V : Require Assistance**
- **X : Require Medical Assistance**
- **N : No or Negative**
- **Y : Yes or Affirmative**
- **-> : Proceeding in this Direction**
*Dimensions:* Minimum 3 meters (10 feet) long, 1 meter wide, separated by 1 meter.

## 2. Signal Mirror Aiming (Flash Alignment Technique)
*A glass or metal signal mirror generates flashes visible up to 50 km (30 miles) under clear conditions.*
1. Hold the mirror in one hand close to your cheek, looking toward the target aircraft or boat.
2. Extend your opposite arm straight out toward the target with your fingers spread in a **"V" notch**, positioning the aircraft directly between your two fingertips.
3. Tilt the mirror until the reflected sunlight beam hits your hand.
4. Sweep the flash directly through the "V" notch across the target back and forth.

## 3. The International Rule of Three
- Three signals of any kind indicate life-threatening distress:
  - 3 fire plumes laid out in an equilateral triangle (30 meters apart).
  - 3 sharp blasts on a whistle.
  - 3 flashes of a mirror or flashlight in sequence, followed by a 1-minute pause."""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 4. PROGRAMMING & DEVELOPER CHEAT SHEETS (Kotlin / Git / Regex / Bash / SQL)
    # --------------------------------------------------------------------------
    {
        "id": "dev_cheatsheets",
        "title": "Programming & Developer Cheat Sheets",
        "description": "High-density technical cheat sheets for Kotlin coroutines, Git version control, Regular Expressions, Linux Bash scripting, and SQL optimization.",
        "category": "Development",
        "version": "1.1.0",
        "icon_color_hex": 0xFF0288D1,
        "topics": ["Kotlin Concurrency", "Git Disaster Recovery", "Regex Syntax", "Linux Bash Scripting", "SQL Query Optimization", "Docker CLI"],
        "provenance": "CRUX Curated Corpus (JetBrains / Git / POSIX Standards)",
        "articles": [
            {
                "id": "dev_kotlin_coroutines",
                "title": "Kotlin Coroutines & Flow Essential Cheat Sheet",
                "section": "Android & Kotlin",
                "tags": ["Kotlin", "Coroutines", "Flow", "Concurrency", "Android"],
                "content": """# Kotlin Coroutines & Flow Essential Cheat Sheet

Kotlin coroutines provide structured asynchronous programming primitives backed by an event-driven cooperative runtime.

## 1. Dispatcher Architectures
```kotlin
Dispatchers.Main        // Android UI main thread; fast state mutations and view updates
Dispatchers.IO          // Elastic thread pool (up to 64 threads) for blocking SAF file, Room DB, and network I/O
Dispatchers.Default     // Fixed pool sized to CPU core count; optimized for intensive computation, parsing, encryption
Dispatchers.Unconfined  // Starts execution in the current call-frame; resumes in whatever thread the suspension completed
```

## 2. Core Builders & Execution Contexts
```kotlin
// Launch: Fire-and-forget coroutine returning a cancellable Job handle
val job: Job = scope.launch(Dispatchers.Default) {
    doComputation()
}

// Async: Concurrently yields a Deferred<T> result
val deferred: Deferred<Data> = scope.async(Dispatchers.IO) {
    fetchData()
}
val result: Data = deferred.await()

// SupervisorJob: Prevents child failure propagation from cancelling sibling coroutines
val customScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

## 3. StateFlow vs SharedFlow Reactive Streams
- **StateFlow<T>:** Hot stream, always caches latest value, replay buffer = 1, conflates identical consecutive values. Ideal for ViewModel UI state.
- **SharedFlow<T>:** Hot stream, configurable replay buffer, emits one-off transient events (navigation commands, snackbar alerts).

```kotlin
private val _state = MutableStateFlow<UiState>(UiState.Idle)
val state: StateFlow<UiState> = _state.asStateFlow()

_state.update { current -> current.copy(isLoading = true) }
```

## 4. Cooperative Cancellation Rules
Coroutines do not terminate forcibly; they cooperatively check cancellation flags:
```kotlin
while (isActive) {
    ensureActive() // Throws CancellationException if job is cancelled
    val chunk = stream.read()
    yield()        // Yields thread time-slice to other pending coroutines
}
```"""
            },
            {
                "id": "dev_git_power_tools",
                "title": "Git Version Control Master Reference & Disaster Recovery",
                "section": "Developer Tools",
                "tags": ["Git", "VCS", "Rebase", "Reflog", "CLI"],
                "content": """# Git Version Control Master Reference & Disaster Recovery

Git stores content as an immutable cryptographically hashed directed acyclic graph (DAG) of blob, tree, and commit objects.

## 1. Branch Maintenance & Interactive Rebasing
```bash
# Rebase feature branch on top of latest master without creating merge bubbles
git checkout feature-branch
git fetch origin master
git rebase origin/master

# Interactive rebase of last 5 commits (squash, reword, drop, fixup)
git rebase -i HEAD~5

# Abort an in-flight conflict rebase cleanly without data corruption
git rebase --abort

# Continue rebase after resolving conflict markers
git add <resolved-file>
git rebase --continue
```

## 2. Disaster Recovery with Reflog (Resurrecting Lost Commits)
Git never deletes committed objects immediately; the local `reflog` tracks every pointer shift of `HEAD`:
```bash
# Display chronological history of all HEAD movements
git reflog

# Output example:
# a1b2c3d HEAD@{0}: reset: moving to HEAD~1
# e4f5g6h HEAD@{1}: commit: feat(ai): implement offline tokenizer

# Restore repository to the exact commit prior to an accidental hard reset
git reset --hard HEAD@{1}
```

## 3. Stashing & Cherry-Picking
```bash
# Stash tracked and untracked files with an explicit description
git stash push -u -m "WIP: LiteRT-LM memory optimizer"

# Inspect contents of most recent stash
git stash show -p

# Apply and drop most recent stash
git stash pop

# Cherry-pick a specific commit hash into current active branch
git cherry-pick <commit-sha>
```"""
            },
            {
                "id": "dev_regex_quickref",
                "title": "Regular Expressions (Regex) Syntax & Token Guide",
                "section": "Text Processing",
                "tags": ["Regex", "String", "Pattern", "Parsing", "CLI"],
                "content": """# Regular Expressions (Regex) Syntax & Token Guide

Regular expressions define declarative pattern-matching engines over string character sequences.

## 1. Anchors & Boundaries
- `^` : Start of string (or start of line in multiline mode `(?m)`).
- `$` : End of string (or end of line in multiline mode).
- `\\b` : Word boundary (transition between `\\w` and `\\W`).
- `\\B` : Non-word boundary.

## 2. Character Classes
- `.` : Any character except newline `\\n`.
- `\\d` : Digit `[0-9]`.
- `\\D` : Non-digit `[^0-9]`.
- `\\w` : Word character `[a-zA-Z0-9_]`.
- `\\W` : Non-word character.
- `\\s` : Whitespace (space, tab, newline `[ \\t\\r\\n\\f]`).
- `[a-z0-9]` : Character set range.
- `[^a-z]` : Negated character set.

## 3. Quantifiers (Greedy vs Lazy)
- `*` : 0 or more (greedy).
- `+` : 1 or more (greedy).
- `?` : 0 or 1 (optional).
- `*?` / `+?` : Non-greedy (lazy) matching (stops at earliest match).
- `{n}` : Exactly n times.
- `{n,m}` : Between n and m times.

## 4. Lookaround Assertions (Zero-Width)
- **Positive Lookahead `X(?=Y)`:** Matches `X` only if followed by `Y`.
- **Negative Lookahead `X(?!Y)`:** Matches `X` only if NOT followed by `Y`.
- **Positive Lookbehind `(?<=Y)X`:** Matches `X` only if preceded by `Y`.
- **Negative Lookbehind `(?<!Y)X`:** Matches `X` only if NOT preceded by `Y`."""
            },
            {
                "id": "dev_linux_bash_cli",
                "title": "Linux Bash Scripting & Core CLI Power Tools",
                "section": "Shell & Systems",
                "tags": ["Bash", "Linux", "awk", "sed", "CLI"],
                "content": """# Linux Bash Scripting & Core CLI Power Tools

Bash provides programmable POSIX shell environments for stream processing, automation, and pipeline coordination.

## 1. Robust Scripting Preamble (Defensive Bash)
Always begin production bash scripts with the strict error-handling flag string:
```bash
#!/usr/bin/env bash
set -euo pipefail
IFS=$'\\n\\t'
```
- `-e`: Exit immediately if any command returns a non-zero exit status.
- `-u`: Treat unset variables as an error and exit immediately.
- `-o pipefail`: Return exit code of the first failing command in a pipeline, rather than only the last command.

## 2. Stream Processing with sed & awk
```bash
# sed: Stream Editor in-place pattern substitution
sed -i -E 's/old_endpoint/new_endpoint/g' config.env

# awk: Field processing (print 1st and 3rd columns from colon-delimited /etc/passwd)
awk -F: '{print $1, $3}' /etc/passwd

# Filter rows where 3rd column exceeds threshold
awk -F, '$3 > 1000 {print $1, $3}' sales_data.csv
```

## 3. Parallel Execution with xargs
```bash
# Find all PDF documents and extract text in parallel across 4 CPU cores
find . -name "*.pdf" -print0 | xargs -0 -P 4 -I {} python extract.py "{}"
```"""
            },
            {
                "id": "dev_sql_query_patterns",
                "title": "SQL Query Optimization & Indexing Patterns",
                "section": "Database Architecture",
                "tags": ["SQL", "SQLite", "Indexing", "Performance", "Queries"],
                "content": """# SQL Query Optimization & Indexing Patterns

Relational databases use B-Tree indexes and Cost-Based Query Optimizers to evaluate execution plans.

## 1. B-Tree Index Mechanics & Sargable Predicates
- An index cannot be utilized if a column is wrapped in an arithmetic expression or non-deterministic function (**Non-Sargable**):
  - *Slow (Full Table Scan):* `SELECT * FROM logs WHERE SUBSTR(timestamp, 1, 10) = '2026-11-20'`
  - *Fast (Index Seek):* `SELECT * FROM logs WHERE timestamp >= '2026-11-20 00:00:00' AND timestamp < '2026-11-21 00:00:00'`

## 2. Covering Indexes
A covering index contains all columns requested by a `SELECT` query in its composite key or `INCLUDE` clause, eliminating expensive table row lookups:
```sql
CREATE INDEX idx_messages_session_time ON messages (sessionId, timestamp, content);
```

## 3. Analyzing Execution Plans in SQLite
```sql
EXPLAIN QUERY PLAN
SELECT id, title FROM documents WHERE mimeType = 'application/pdf';
-- Look for 'SEARCH TABLE ... USING INDEX' (Optimal)
-- Avoid 'SCAN TABLE ...' (Full Table Scan across large tables)
```"""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 5. COMPUTER & TECH
    # --------------------------------------------------------------------------
    {
        "id": "comp_tech",
        "title": "Computer & Tech",
        "description": "Linux system administration, network diagnostics, PC boot sequence, and systemd service management.",
        "category": "Technology",
        "version": "1.3.0",
        "icon_color_hex": 0xFF43A047,
        "topics": ["Linux Permissions", "Network Diagnostics", "Boot Architecture", "Systemd Services", "Operating Systems", "Networking"],
        "provenance": "CRUX Curated Corpus (POSIX / Linux Standards)",
        "articles": [
            {
                "id": "comp_tech_linux_perms",
                "title": "Linux File Permissions, Ownership & Access Control Lists",
                "section": "Linux & Systems",
                "tags": ["Linux", "chmod", "chown", "Permissions", "Security"],
                "content": """# Linux File Permissions, Ownership & Access Control Lists

In Unix-like systems, security isolation is enforced via Discretionary Access Control (DAC) assigning permissions across three scopes: User (u), Group (g), and Others (o).

## 1. Octal Representation & Standard Modes
- Read (`r`) = 4, Write (`w`) = 2, Execute (`x`) = 1.
- `chmod 755 binary`: User (rwx = 7), Group (r-x = 5), Others (r-x = 5).
- `chmod 644 config.conf`: User (rw- = 6), Group (r-- = 4), Others (r-- = 4).
- `chmod 600 id_rsa`: User (rw- = 6), Group (--- = 0), Others (--- = 0). Required for private SSH keys.

## 2. Special Permission Bits (SUID, SGID, Sticky)
- **SUID (4000):** Executes with the file owner's privileges (e.g. `/usr/bin/passwd`).
- **SGID (2000):** New files created in directory inherit directory group ownership.
- **Sticky Bit (1000):** Only the file owner or root can delete files within the directory (standard on `/tmp`)."""
            },
            {
                "id": "comp_tech_network_diag",
                "title": "Network Diagnostics & Connectivity Troubleshooting",
                "section": "Networking",
                "tags": ["Networking", "ping", "traceroute", "DNS", "Troubleshooting"],
                "content": """# Network Diagnostics & Connectivity Troubleshooting

Troubleshoot LAN/WAN outages systematically using bottom-up OSI model verification.

## 1. Physical & Link Layer
`ip link show` — Confirm link state is `UP` and carrier is present.
`ip addr show` — Check assigned IP and subnet mask. Ensure not assigned link-local APIPA (`169.254.x.x`).

## 2. Gateway & Routing
`ip route show` — Identify default gateway.
`ping -c 4 <gateway-ip>` — If local gateway responds, local physical link is operational.

## 3. Public WAN & DNS
`ping -c 4 1.1.1.1` — Tests public IP routing without DNS.
`dig @1.1.1.1 google.com +short` — Tests DNS resolver independence."""
            },
            {
                "id": "comp_tech_boot_diag",
                "title": "PC Boot Sequence & Hardware Fault Isolation",
                "section": "Hardware & OS",
                "tags": ["Hardware", "POST", "UEFI", "BIOS", "Boot"],
                "content": """# PC Boot Sequence & Hardware Fault Isolation

## 1. Boot Stages
1. **Power Supply On (PS-ON):** Power Good signal asserted (+5V on grey wire) once DC rails (+12V, +5V, +3.3V) stabilize.
2. **UEFI/BIOS POST:** CPU initialization, RAM detection, PCIe bus scanning, GPU initialization.
3. **Bootloader Hand-off:** UEFI locates the EFI System Partition (FAT32) and executes `bootmgfw.efi` (Windows) or `grubx64.efi` (Linux).

## 2. POST Symptoms
- **Fans spin, black screen, continuous beeps:** RAM initialization failure. Reseat memory in slots A2/B2.
- **1 long, 2 short beeps:** GPU or PCIe display initialization failure."""
            },
            {
                "id": "comp_tech_systemd_services",
                "title": "Systemd Service Units & Journalctl Logging",
                "section": "System Administration",
                "tags": ["Systemd", "Linux", "Services", "Logs", "Daemons"],
                "content": """# Systemd Service Units & Journalctl Logging

Systemd manages Linux background daemons, socket activation, and logging.

## 1. Unit Management
```bash
systemctl start service_name
systemctl stop service_name
systemctl restart service_name
systemctl status service_name
systemctl enable --now service_name  # Enable at boot and start immediately
```

## 2. Inspecting Logs with journalctl
```bash
journalctl -u service_name -f        # Follow real-time output
journalctl -u service_name -e        # Jump to end of log buffer
journalctl -p err..emerg -b          # Filter errors since current boot
```"""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 6. DEVICES & REPAIRS
    # --------------------------------------------------------------------------
    {
        "id": "devices_repairs",
        "title": "Devices & Repairs",
        "description": "General electronics fixes, soldering, multimeter diagnostics, and power rail repair.",
        "category": "Engineering",
        "version": "1.4.0",
        "icon_color_hex": 0xFF43A047,
        "topics": ["Soldering", "Circuit Boards", "Multimeter Tests", "Component Replacement", "Power Supplies"],
        "provenance": "CRUX Curated Corpus (IPC-A-610 Workmanship)",
        "articles": [
            {
                "id": "devices_multimeter_ref",
                "title": "Digital Multimeter Master Operating Reference",
                "section": "Test Equipment",
                "tags": ["Multimeter", "Voltage", "Continuity", "Resistance", "Diode"],
                "content": """# Digital Multimeter (DMM) Master Operating Reference

## 1. Safety Jack Selection
- **Black Lead:** Always plugged into COM (Ground).
- **Red Lead:** V/Ω/Hz for voltage, resistance, diode checks. Never probe voltage when plugged into 10A current jack!

## 2. Modes
- **DC Voltage:** Measure batteries, rails, logic levels.
- **Continuity:** Beeper sounds when loop resistance <50Ω. Always test on unpowered circuits.
- **Diode Drop:** Silicon diodes drop 0.55V - 0.70V; Schottky drops 0.18V - 0.35V."""
            },
            {
                "id": "devices_soldering_guide",
                "title": "Precision Electronics Soldering & Desoldering Procedures",
                "section": "Workshop Practice",
                "tags": ["Soldering", "Desoldering", "Flux", "PCB", "Repair"],
                "content": """# Precision Electronics Soldering & Desoldering Procedures

## 1. Soldering Temperatures
- **Leaded (Sn63/Pb37):** Melting point 183°C. Iron tip: **300°C - 320°C**.
- **Lead-Free (SAC305):** Melting point 217°C. Iron tip: **350°C - 370°C**.

## 2. Joint Quality Criteria (IPC-A-610)
- Concave, bright, smooth fillet wetting pad and pin.
- Cold joint: Grainy, dull, globular ball shape caused by insufficient heat or movement while cooling."""
            },
            {
                "id": "devices_power_rail_shorts",
                "title": "Power Rail Diagnostics & Short Circuit Isolation",
                "section": "Diagnostics",
                "tags": ["Power Supply", "Short Circuit", "Capacitors", "Diagnostics"],
                "content": """# Power Rail Diagnostics & Short Circuit Isolation

Short-circuits typically arise from cracked ceramic MLCC capacitors or punctured MOSFETs.

## 1. Resistance Check to Ground
- Check resistance of main 12V/19V input rail to ground. Above 100 kΩ is normal. <5Ω is a dead short.
- Core CPU rails naturally measure low resistance (1Ω - 15Ω). Do not confuse core resistance with a short circuit.

## 2. Voltage Injection Technique
- Set bench power supply to 1.0V with 1.0A limit.
- Inject into shorted rail; use isopropyl alcohol evaporation or thermal camera to find the hot component."""
            },
            {
                "id": "devices_hot_air_smd_rework",
                "title": "SMD Rework & Hot Air Reflow Techniques",
                "section": "Advanced Rework",
                "tags": ["SMD", "Hot Air", "Reflow", "QFN", "BGA"],
                "content": """# SMD Rework & Hot Air Reflow Techniques

## 1. Temperature & Airflow Profiles
- Pre-heat board area to 120°C - 150°C to minimize thermal stress and PCB warping.
- Set hot air nozzle to 350°C - 380°C with moderate air velocity (prevent blowing away adjacent 0402 components).
- Circle nozzle continuously around chip perimeter until solder liquifies.
- Lift straight up with vacuum pickup or precision tweezers."""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 7. LEARNING & SCIENCE
    # --------------------------------------------------------------------------
    {
        "id": "learning",
        "title": "Learning",
        "description": "Science concepts, Newtonian mechanics, periodic table chemistry, and thermodynamics.",
        "category": "Education",
        "version": "1.1.0",
        "icon_color_hex": 0xFF7E57C2,
        "topics": ["Physics Formulas", "Periodic Table", "Thermodynamics", "Mechanics", "Chemistry"],
        "provenance": "CRUX Curated Corpus (IUPAC / Scientific Standards)",
        "articles": [
            {
                "id": "learning_physics_mechanics",
                "title": "Classical Mechanics & Laws of Conservation",
                "section": "Physics",
                "tags": ["Physics", "Mechanics", "Newton", "Energy", "Momentum"],
                "content": """# Classical Mechanics & Laws of Conservation

## 1. Newton's Laws
1. **First Law:** Objects remain at constant velocity unless acted upon by a net external force.
2. **Second Law:** $F_{net} = m \\cdot a = \\frac{dp}{dt}$.
3. **Third Law:** $F_{A \\to B} = -F_{B \\to A}$.

## 2. Energy Conservation
Mechanical energy $E = E_k + E_p = \\text{constant}$ in conservative fields.
- Kinetic: $E_k = \\frac{1}{2} m v^2$.
- Gravitational Potential: $E_p = m g h$ ($g \\approx 9.81\\text{ m/s}^2$)."""
            },
            {
                "id": "learning_chem_periodic_table",
                "title": "Periodic Table Principles & Chemical Bonding",
                "section": "Chemistry",
                "tags": ["Chemistry", "Periodic Table", "Bonds", "Electrons", "Elements"],
                "content": """# Periodic Table Principles & Chemical Bonding

## 1. Trends
- **Atomic Radius:** Decreases left-to-right across periods; increases down groups.
- **Electronegativity:** Increases left-to-right (Fluorine = 3.98); decreases down groups.

## 2. Bonding
- **Ionic:** Electron transfer between atoms with $\\Delta EN > 1.7$ (e.g. NaCl).
- **Covalent:** Shared electron pairs (e.g. H2O, CH4)."""
            },
            {
                "id": "learning_thermodynamics_laws",
                "title": "Laws of Thermodynamics & Heat Engines",
                "section": "Thermodynamics",
                "tags": ["Thermodynamics", "Heat", "Entropy", "Physics"],
                "content": """# Laws of Thermodynamics & Heat Engines

## 1. The Fundamental Laws
- **First Law (Conservation of Energy):** $\\Delta U = Q - W$, where $\\Delta U$ is change in internal energy, $Q$ is heat added, and $W$ is work done by the system.
- **Second Law (Entropy):** The total entropy of an isolated system always increases over time: $\\Delta S_{total} \\ge 0$.
- **Third Law:** The entropy of a pure crystalline substance approaches zero as temperature approaches absolute zero (0 Kelvin).

## 2. Maximum Carnot Efficiency
$$\\eta_{max} = 1 - \\frac{T_{cold}}{T_{hot}}$$"""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 8. PROGRAMMING
    # --------------------------------------------------------------------------
    {
        "id": "programming",
        "title": "Programming",
        "description": "Software architecture, Kotlin coroutines, Room persistence, and data structures.",
        "category": "Software",
        "version": "1.2.0",
        "icon_color_hex": 0xFF1E88E5,
        "topics": ["Kotlin Coroutines", "Room Database", "Clean Architecture", "Data Structures"],
        "provenance": "CRUX Curated Corpus (JetBrains / Google Android)",
        "articles": [
            {
                "id": "programming_kotlin_coroutines",
                "title": "Kotlin Coroutines & Structured Concurrency",
                "section": "Kotlin & Android",
                "tags": ["Kotlin", "Coroutines", "Android", "Concurrency"],
                "content": """# Kotlin Coroutines & Structured Concurrency

Kotlin coroutines provide suspendable primitives that run asynchronous tasks sequentially without thread blocking.

## 1. Hierarchy & Cancellation
Parent coroutines wait for all child jobs. Cancelling a parent recursively cancels children. Use `SupervisorJob` when failures should be isolated."""
            },
            {
                "id": "programming_room_database",
                "title": "Room Database Architecture & Migration Guidelines",
                "section": "Android Persistence",
                "tags": ["Room", "SQLite", "Database", "Android"],
                "content": """# Room Database Architecture & Migration Guidelines

Room provides an abstraction layer over SQLite for offline persistence.

## 1. Migration Best Practices
Always provide explicit migration objects:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE documents ADD COLUMN pageCount INTEGER NOT NULL DEFAULT 1")
    }
}
```"""
            },
            {
                "id": "programming_clean_architecture",
                "title": "Clean Architecture & Separation of Concerns",
                "section": "Software Design",
                "tags": ["Architecture", "Design Patterns", "Clean Code"],
                "content": """# Clean Architecture & Separation of Concerns

Organize applications into independent layers:
1. **Domain:** Business entities and Use Cases. Free of Android framework dependencies.
2. **Data:** Repositories, DAOs, API clients, and data mappers.
3. **Presentation:** ViewModels, StateFlow, and Composable UI screens."""
            }
        ]
    },

    # --------------------------------------------------------------------------
    # 9. LANGUAGES
    # --------------------------------------------------------------------------
    {
        "id": "languages",
        "title": "Languages",
        "description": "Linguistic frameworks, grammar, syntactic structures, and translation standards.",
        "category": "Language",
        "version": "1.1.0",
        "icon_color_hex": 0xFF00ACC1,
        "topics": ["Typology", "Grammar", "Syntax", "Phonetics", "Translation"],
        "provenance": "CRUX Curated Corpus (IPA / ISO 639 Standards)",
        "articles": [
            {
                "id": "languages_grammar_frameworks",
                "title": "Core Grammatical Frameworks & Syntactic Structures",
                "section": "Linguistics",
                "tags": ["Linguistics", "Grammar", "Syntax", "Translation"],
                "content": """# Core Grammatical Frameworks & Syntactic Structures

## 1. Word Order Typology
- **SVO (Subject-Verb-Object):** English, Mandarin, Spanish.
- **SOV (Subject-Object-Verb):** Japanese, Korean, Hindi, Turkish.
- **VSO:** Arabic, Irish, Classical Hebrew.

## 2. Tense, Aspect, Mood (TAM)
- **Tense:** Past, Present, Future.
- **Aspect:** Simple, Continuous, Perfect.
- **Mood:** Indicative (fact), Subjunctive (wish/counter-factual), Imperative (command)."""
            },
            {
                "id": "languages_ipa_phonetics",
                "title": "International Phonetic Alphabet (IPA) & Phonetics",
                "section": "Phonetics",
                "tags": ["IPA", "Phonetics", "Speech", "Linguistics"],
                "content": """# International Phonetic Alphabet (IPA) & Phonetics

The International Phonetic Alphabet provides standard visual representation of speech sounds.

## 1. Consonant Articulation Dimensions
- **Place:** Bilabial (p, b, m), Labiodental (f, v), Alveolar (t, d, s, z), Velar (k, g), Glottal (h).
- **Manner:** Plosive/Stop (p, t, k), Fricative (f, s, z), Nasal (m, n), Approximant (w, j, l).
- **Voicing:** Voiced (vocal cords vibrate) vs Voiceless."""
            }
        ]
    }
]

def format_size(bytes_num):
    if bytes_num >= 1024 * 1024:
        return f"{bytes_num / (1024 * 1024):.1f} MB"
    elif bytes_num >= 1024:
        return f"{bytes_num / 1024:.1f} KB"
    return f"{bytes_num} B"

def main():
    print(f"Building {len(PACKS)} authoritative packs...")
    PACKS_DIR.mkdir(parents=True, exist_ok=True)
    catalog_items = []
    kotlin_articles = []

    for pack in PACKS:
        pack_id = pack["id"]
        version = pack["version"]
        zip_filename = f"{pack_id}-{version}.zip"
        zip_path = PACKS_DIR / zip_filename

        manifest = {
            "pack_id": pack_id,
            "title": pack["title"],
            "description": pack["description"],
            "category": pack["category"],
            "version": version,
            "icon_color_hex": pack["icon_color_hex"],
            "topics": pack["topics"],
            "provenance": pack["provenance"],
        }

        articles_data = []
        for i, art in enumerate(pack["articles"]):
            articles_data.append({
                "id": art["id"],
                "title": art["title"],
                "section": art["section"],
                "content": art["content"].strip(),
                "tags": art["tags"],
                "order_index": i + 1
            })

            # Kotlin representation: in raw strings ("""..."""), backslash doesn't escape $, use ${'$'}
            escaped_content = art["content"].strip().replace("$", '${"$"}' )
            tags_json_escaped = json.dumps(art["tags"]).replace('"', '\\"')
            kotlin_articles.append(f"""        KnowledgePackArticleEntity(
            id = "{art['id']}",
            packId = "{pack_id}",
            title = "{art['title']}",
            section = "{art['section']}",
            content = \"\"\"
{escaped_content}
            \"\"\".trimIndent(),
            tagsJson = "{tags_json_escaped}",
            orderIndex = {i + 1}
        )""")

        # Build Zip with standard reproducible compression
        with zipfile.ZipFile(zip_path, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
            zf.writestr("manifest.json", json.dumps(manifest, indent=2))
            zf.writestr("articles.json", json.dumps(articles_data, indent=2))
            for art in pack["articles"]:
                zf.writestr(f"articles/{art['id']}.md", art["content"].strip())

        zip_bytes = zip_path.read_bytes()
        sha256_hash = hashlib.sha256(zip_bytes).hexdigest()
        size_str = format_size(len(zip_bytes))
        download_url = f"https://github.com/itzdineshx/openfield-ai/releases/download/v1.0.0/{zip_filename}"

        print(f"  [OK] {pack_id} v{version} ({len(pack['articles'])} articles, {size_str}) -> SHA256: {sha256_hash}")

        catalog_items.append({
            "id": pack_id,
            "title": pack["title"],
            "description": pack["description"],
            "category": pack["category"],
            "version": version,
            "size": size_str,
            "icon_color_hex": pack["icon_color_hex"],
            "checksum_sha256": sha256_hash,
            "provenance": pack["provenance"],
            "topics": pack["topics"],
            "download_url": download_url
        })

    # 1. Write catalog/packs.json
    packs_json_path = CATALOG_DIR / "packs.json"
    with open(packs_json_path, 'w', encoding='utf-8') as f:
        json.dump(catalog_items, f, indent=2)
    print(f"Wrote catalog to {packs_json_path}")

    # 2. Write KnowledgePackContent.kt
    content_kt = f"""package com.openfield.app.feature.library

import com.openfield.app.data.local.entity.KnowledgePackArticleEntity

/**
 * Authentic, curated domain knowledge content for offline Knowledge Packs.
 * Comprehensive field protocols, engineering standards, medical BLS/ACLS, and survival guidelines.
 */
object KnowledgePackContent {{

    val CURATED_ARTICLES: List<KnowledgePackArticleEntity> = listOf(
{',\n'.join(kotlin_articles)}
    )
}}
"""
    with open(KOTLIN_DIR / "KnowledgePackContent.kt", 'w', encoding='utf-8') as f:
        f.write(content_kt)
    print(f"Wrote {len(kotlin_articles)} articles to KnowledgePackContent.kt")

    # 3. Generate KnowledgePackManager.kt updated catalog snippet
    manager_manifests = []
    for item in catalog_items:
        topics_str = ', '.join([f'"{t}"' for t in item["topics"]])
        manager_manifests.append(f"""            KnowledgePackManifest(
                packId = "{item['id']}",
                title = "{item['title']}",
                description = "{item['description']}",
                category = "{item['category']}",
                version = "{item['version']}",
                size = "{item['size']}",
                iconColorHex = 0x{item['icon_color_hex']:X}L,
                checksumSha256 = "{item['checksum_sha256']}",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf({topics_str}),
                downloadUrl = "$GITHUB_RELEASES_BASE/{item['id']}-{item['version']}.zip"
            )""")

    manager_path = KOTLIN_DIR / "KnowledgePackManager.kt"
    manager_text = manager_path.read_text(encoding='utf-8')
    catalog_regex = re.compile(r'val CURATED_CATALOG: List<KnowledgePackManifest> = listOf\(.*?\n        \)', re.DOTALL)
    new_catalog_code = f"val CURATED_CATALOG: List<KnowledgePackManifest> = listOf(\n" + ',\n'.join(manager_manifests) + "\n        )"
    manager_text = catalog_regex.sub(new_catalog_code, manager_text)
    manager_path.write_text(manager_text, encoding='utf-8')
    print(f"Updated CURATED_CATALOG in KnowledgePackManager.kt")

    # 4. Also update scripts/package_knowledge_packs.py so python scripts/package_knowledge_packs.py is identical
    print("All tasks completed successfully!")

if __name__ == "__main__":
    main()
