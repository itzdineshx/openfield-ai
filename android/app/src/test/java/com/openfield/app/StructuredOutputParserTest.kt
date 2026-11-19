package com.openfield.app

import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredOutputParserTest {

    @Test
    fun extractJsonString_fromMarkdownFences() {
        val raw = """
Here is your diagnostic step:
```json
{
  "category": "Electrical",
  "question": "Is the fuse blown?"
}
```
Hope this helps!
""".trimIndent()

        val json = StructuredOutputParser.extractJsonString(raw)
        assertNotNull(json)
        assertTrue(json!!.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"category\": \"Electrical\""))
    }

    @Test
    fun extractJsonString_fromRawBraces() {
        val raw = "Response preamble: {\"key\": \"value\"} trailing text."
        val json = StructuredOutputParser.extractJsonString(raw)
        assertEquals("{\"key\": \"value\"}", json)
    }

    @Test
    fun extractJsonString_returnsNullForNoJson() {
        val raw = "Plain unstructured text without braces."
        assertNull(StructuredOutputParser.extractJsonString(raw))
    }

    @Test
    fun parseDiagnosticAssessment_validJson() {
        val raw = """
```json
{
  "category": "Automotive / Starting System",
  "safetyNotice": "Wear eye protection and keep clear of engine pulleys.",
  "currentStepNumber": 1,
  "totalEstimatedSteps": 3,
  "question": "When you turn the ignition key, what sound do you hear?",
  "explanation": "Different sounds isolate starter solenoid, relay, or dead battery.",
  "options": [
    {"id": "A", "text": "Rapid clicking sound"},
    {"id": "B", "text": "Single loud click"},
    {"id": "C", "text": "Engine cranks slowly"},
    {"id": "D", "text": "Complete silence"}
  ],
  "isResolutionReached": false,
  "likelyCause": null,
  "recommendedSolution": null
}
```
""".trimIndent()

        val assessment = StructuredOutputParser.parseDiagnosticAssessment(raw)
        assertNotNull(assessment)
        assertEquals("Automotive / Starting System", assessment!!.category)
        assertEquals("When you turn the ignition key, what sound do you hear?", assessment.question)
        assertEquals(4, assessment.options.size)
        assertEquals("A", assessment.options[0].id)
        assertEquals("Rapid clicking sound", assessment.options[0].text)
        assertNotNull(assessment.safetyNotice)
    }

    @Test
    fun parseDiagnosticAssessment_withEscapedQuotesAndNewlines() {
        val raw = """
```json
{
  "category": "Electronics / Computer",
  "question": "Does the \"Power LED\" blink or stay solid?\nPlease verify.",
  "explanation": "Diagnostic check for power rail.",
  "options": [
    {"id": "A", "text": "It blinks \"amber\" code"},
    {"id": "B", "text": "Solid green light"}
  ],
  "isResolutionReached": false
}
```
""".trimIndent()
        val assessment = StructuredOutputParser.parseDiagnosticAssessment(raw)
        assertNotNull(assessment)
        assertEquals("Does the \"Power LED\" blink or stay solid?\nPlease verify.", assessment!!.question)
        assertEquals("It blinks \"amber\" code", assessment.options[0].text)
    }

    @Test
    fun parseDiagnosticAssessment_markdownFallback() {
        val raw = """
Based on the symptoms, here is step 1:

What is the color of the exhaust smoke?
A) Blue or gray smoke
B) Thick black smoke
C) White billowing vapor
D) No visible smoke

Safety: Ensure vehicle is in a well-ventilated area.
""".trimIndent()

        val assessment = StructuredOutputParser.parseDiagnosticAssessment(raw)
        assertNotNull(assessment)
        assertTrue(assessment!!.question.contains("exhaust smoke"))
        assertTrue(assessment.options.isNotEmpty())
    }

    @Test
    fun parseStructuredLesson_validJson() {
        val raw = """
```json
{
  "topic": "Gravity",
  "title": "Understanding Gravity: The Universal Pull",
  "overview": "Gravity is the fundamental attractive force between masses.",
  "sections": [
    {
      "heading": "Mass and Attraction",
      "content": "Every object with mass exerts a gravitational pull on all other masses."
    },
    {
      "heading": "Curvature of Spacetime",
      "content": "Einstein revealed that mass curves spacetime, causing objects to follow curved paths."
    }
  ],
  "practicalExample": "Dropping a ball from your hand demonstrates Earth's gravitational acceleration.",
  "quiz": {
    "question": "What determines the strength of gravitational pull between two masses?",
    "options": [
      "Their mass and the distance between them",
      "Their speed and air temperature",
      "Their volume and atmospheric pressure",
      "Their electric charge and magnetic polarity"
    ],
    "correctOptionIndex": 0,
    "explanation": "Newton's law states gravity is directly proportional to mass and inversely to distance squared.",
    "hint": "Think about Newton's formula involving M1, M2, and r."
  }
}
```
""".trimIndent()

        val lesson = StructuredOutputParser.parseStructuredLesson(raw, "Gravity", LearningLevel.INTERMEDIATE)
        assertNotNull(lesson)
        assertEquals("Understanding Gravity: The Universal Pull", lesson!!.title)
        assertEquals(2, lesson.sections.size)
        assertEquals("Mass and Attraction", lesson.sections[0].heading)
        val quiz = lesson.quiz
        assertNotNull(quiz)
        assertEquals(4, quiz!!.options.size)
        assertEquals(0, quiz.correctOptionIndex)
    }

    @Test
    fun parseImageSections_extractsRequiredSections() {
        val raw = """
## 1. What I See
A small green caterpillar with yellow stripes crawling on a tomato leaf.

## 2. Important Details
- Distinct white v-shaped markings
- Black horn projection at the rear
- Leaf shows severe chewing damage

## 3. Explanation
This is a larval stage of the Manduca genus, commonly known as a tomato hornworm.

## 4. Possible Identification
Tomato Hornworm (Manduca quinquemaculata)

## 5. Suggested Next Steps
- Hand-pick from the foliage
- Check for parasitic braconid wasp cocoons

## 6. Uncertainty & Limitations
Angle does not show underbelly spiracles clearly.
""".trimIndent()

        val sections = StructuredOutputParser.parseImageSections(raw)
        assertNotNull(sections)
        assertTrue(sections.whatISee.contains("caterpillar"))
        assertTrue(sections.importantDetails.isNotEmpty())
        assertTrue(sections.explanation.contains("Manduca"))
        assertEquals("Tomato Hornworm (Manduca quinquemaculata)", sections.possibleIdentification)
        assertEquals(2, sections.suggestedNextSteps.size)
        assertNotNull(sections.uncertaintyAndLimitations)
    }

    @Test
    fun parseDiagnosticAssessment_withEscalation_parsesCorrectly() {
        val raw = """
```json
{
  "category": "High-Voltage Electrical",
  "safetyNotice": "Hazard of lethal electrical shock. Do not touch bare copper.",
  "currentStepNumber": 1,
  "totalEstimatedSteps": 1,
  "question": "Are sparks or burning smells emanating from the main service panel?",
  "explanation": "Arcing mains wires present extreme fire hazard.",
  "options": [
    {"id": "A", "text": "Yes, active arcing observed"},
    {"id": "B", "text": "No, completely quiet"}
  ],
  "isResolutionReached": false,
  "likelyCause": "Main breaker terminal bus failure",
  "recommendedSolution": "Switch off main disconnect if safe and call power utility emergency.",
  "isEscalationNeeded": true,
  "escalationReason": "Lethal 240V arc flash hazard requiring utility grid isolation."
}
```
""".trimIndent()

        val parsed = StructuredOutputParser.parseDiagnosticAssessment(raw)
        assertNotNull(parsed)
        assertTrue(parsed!!.isEscalationNeeded)
        assertEquals("Lethal 240V arc flash hazard requiring utility grid isolation.", parsed.escalationReason)
        assertEquals("High-Voltage Electrical", parsed.category)
    }
}
