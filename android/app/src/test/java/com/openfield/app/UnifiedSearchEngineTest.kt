package com.openfield.app

import com.openfield.app.feature.document.rag.UnifiedSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class UnifiedSearchEngineTest {

    // Instantiate with dummy DAOs using dynamic proxy
    private val engine = createTestEngine()

    @Test
    fun sanitizeQuery_removesStopWordsAndPunctuation() {
        val query = "What is the battery replacement procedure for the scanner?"
        val terms = engine.sanitizeQuery(query)

        assertFalse(terms.contains("what"))
        assertFalse(terms.contains("is"))
        assertFalse(terms.contains("the"))
        assertFalse(terms.contains("for"))
        assertTrue(terms.contains("battery"))
        assertTrue(terms.contains("replacement"))
        assertTrue(terms.contains("procedure"))
        assertTrue(terms.contains("scanner"))
    }

    @Test
    fun generateSnippet_centersAroundMatchedTerms() {
        val fullText = "Preliminary safety inspection. Ensure that the device is unplugged. The battery replacement procedure requires unscrewing the back panel with a Phillips screwdriver. Once opened, disconnect the lithium cell."
        val terms = listOf("battery", "replacement")

        val snippet = engine.generateSnippet(fullText, terms, maxChars = 80)

        assertTrue(snippet.contains("battery replacement"))
        assertTrue(snippet.length <= 85) // Including ellipsis
    }

    @Test
    fun generateSnippet_handlesShortTextGracefully() {
        val shortText = "Battery status OK."
        val terms = listOf("battery")

        val snippet = engine.generateSnippet(shortText, terms, maxChars = 100)

        assertEquals("Battery status OK.", snippet)
    }

    private companion object {
        fun createTestEngine(): UnifiedSearchEngine {
            val docDao = Proxy.newProxyInstance(
                com.openfield.app.data.local.dao.DocumentDao::class.java.classLoader,
                arrayOf(com.openfield.app.data.local.dao.DocumentDao::class.java)
            ) { _, _, _ -> null } as com.openfield.app.data.local.dao.DocumentDao

            val chatDao = Proxy.newProxyInstance(
                com.openfield.app.data.local.dao.ChatDao::class.java.classLoader,
                arrayOf(com.openfield.app.data.local.dao.ChatDao::class.java)
            ) { _, _, _ -> null } as com.openfield.app.data.local.dao.ChatDao

            val modeDao = Proxy.newProxyInstance(
                com.openfield.app.data.local.dao.ModeSessionDao::class.java.classLoader,
                arrayOf(com.openfield.app.data.local.dao.ModeSessionDao::class.java)
            ) { _, _, _ -> null } as com.openfield.app.data.local.dao.ModeSessionDao

            val packDao = Proxy.newProxyInstance(
                com.openfield.app.data.local.dao.KnowledgePackDao::class.java.classLoader,
                arrayOf(com.openfield.app.data.local.dao.KnowledgePackDao::class.java)
            ) { _, _, _ -> null } as com.openfield.app.data.local.dao.KnowledgePackDao

            return UnifiedSearchEngine(docDao, chatDao, modeDao, packDao)
        }
    }
}
