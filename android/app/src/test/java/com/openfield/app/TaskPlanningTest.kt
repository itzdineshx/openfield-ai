package com.openfield.app

import com.openfield.app.feature.ai.mode.PromptBuilder
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskPlanningTest {

    @Test
    fun buildTaskPlanningPrompt_containsUserInputAndSchema() {
        val prompt = PromptBuilder.buildTaskPlanningPrompt("Prepare for Machine Learning Final Exam")

        assertTrue(prompt.contains("Prepare for Machine Learning Final Exam"))
        assertTrue(prompt.contains("\"title\""))
        assertTrue(prompt.contains("\"subtasks\""))
        assertTrue(prompt.contains("\"priority\""))
    }

    @Test
    fun parseTaskPlan_validJson_parsesCorrectly() {
        val json = """
            {
              "title": "Clean Motorcycle Carburetor",
              "description": "Disassemble, inspect jets, and clean float bowl",
              "priority": "HIGH",
              "suggestedCategory": "Maintenance",
              "subtasks": [
                "Turn off fuel petcock and disconnect fuel line",
                "Drain float bowl",
                "Remove carburetor from manifold",
                "Clean main and pilot jets with solvent"
              ]
            }
        """.trimIndent()

        val plan = StructuredOutputParser.parseTaskPlan(json)

        assertNotNull(plan)
        assertEquals("Clean Motorcycle Carburetor", plan?.title)
        assertEquals("Disassemble, inspect jets, and clean float bowl", plan?.description)
        assertEquals("HIGH", plan?.priority)
        assertEquals("Maintenance", plan?.suggestedCategory)
        assertEquals(4, plan?.subtasks?.size)
        assertEquals("Turn off fuel petcock and disconnect fuel line", plan?.subtasks?.get(0))
    }

    @Test
    fun parseTaskPlan_jsonInCodeFence_parsesCorrectly() {
        val response = """
            Here is the plan for your research project:
            ```json
            {
              "title": "Literature Review on Quantum Computing",
              "description": "Synthesize key 2024-2026 papers",
              "priority": "medium",
              "suggestedCategory": "Research",
              "subtasks": [
                "Query local document index for arXiv preprints",
                "Draft outline of state-of-the-art architectures"
              ]
            }
            ```
            Good luck!
        """.trimIndent()

        val plan = StructuredOutputParser.parseTaskPlan(response)

        assertNotNull(plan)
        assertEquals("Literature Review on Quantum Computing", plan?.title)
        assertEquals("MEDIUM", plan?.priority)
        assertEquals(2, plan?.subtasks?.size)
    }

    @Test
    fun parseTaskPlan_missingTitle_returnsNull() {
        val json = """
            {
              "description": "Missing title",
              "priority": "LOW",
              "subtasks": ["Step 1"]
            }
        """.trimIndent()

        assertNull(StructuredOutputParser.parseTaskPlan(json))
    }

    @Test
    fun parseTaskPlan_invalidJson_returnsNull() {
        assertNull(StructuredOutputParser.parseTaskPlan("Not valid JSON at all"))
    }
}
