package com.openfield.app

import com.openfield.app.data.local.dao.SavedAnswerDao
import com.openfield.app.data.local.entity.SavedAnswerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SavedAnswerLifecycleTest {

    private class FakeSavedAnswerDao : SavedAnswerDao {
        val storage = mutableListOf<SavedAnswerEntity>()

        override fun getSavedAnswersFlow(): Flow<List<SavedAnswerEntity>> =
            flowOf(storage.sortedByDescending { it.savedAt })

        override suspend fun getSavedAnswers(): List<SavedAnswerEntity> =
            storage.sortedByDescending { it.savedAt }

        override suspend fun insertSavedAnswer(answer: SavedAnswerEntity) {
            storage.removeAll { it.id == answer.id }
            storage.add(answer)
        }

        override suspend fun deleteSavedAnswer(id: String) {
            storage.removeAll { it.id == id }
        }

        override suspend fun clearAllSavedAnswers() {
            storage.clear()
        }
    }

    private lateinit var dao: FakeSavedAnswerDao

    @Before
    fun setUp() {
        dao = FakeSavedAnswerDao()
    }

    @Test
    fun saveAnswer_insertsAndEmitsInDescendingOrder() = runBlocking {
        val answer1 = SavedAnswerEntity(
            id = "saved_1",
            question = "How to test voltage with a multimeter?",
            answer = "Switch DMM to DC voltage mode. Connect black lead to COM and red to V jack.",
            sourceTitle = "Digital Multimeter Guide",
            sourceType = "CHAT",
            savedAt = 1000L
        )

        val answer2 = SavedAnswerEntity(
            id = "saved_2",
            question = "What is the command for Linux file ownership?",
            answer = "Use `chown user:group filename`.",
            sourceTitle = "Linux File Permissions",
            sourceType = "DOCUMENT",
            savedAt = 2000L
        )

        dao.insertSavedAnswer(answer1)
        dao.insertSavedAnswer(answer2)

        val list = dao.getSavedAnswers()
        assertEquals(2, list.size)
        // Most recent first
        assertEquals("saved_2", list[0].id)
        assertEquals("saved_1", list[1].id)

        val flowList = dao.getSavedAnswersFlow().first()
        assertEquals(2, flowList.size)
        assertEquals("saved_2", flowList[0].id)
    }

    @Test
    fun deleteSavedAnswer_removesSpecificAnswer() = runBlocking {
        val answer = SavedAnswerEntity(
            id = "saved_target",
            question = "Q",
            answer = "A",
            sourceTitle = "Title",
            sourceType = "CHAT"
        )
        dao.insertSavedAnswer(answer)
        assertEquals(1, dao.getSavedAnswers().size)

        dao.deleteSavedAnswer("saved_target")
        assertNull(dao.getSavedAnswers().find { it.id == "saved_target" })
        assertTrue(dao.getSavedAnswers().isEmpty())
    }

    @Test
    fun clearAllSavedAnswers_cleansEntireCollection() = runBlocking {
        for (i in 1..5) {
            dao.insertSavedAnswer(
                SavedAnswerEntity(
                    id = "saved_$i",
                    question = "Question $i",
                    answer = "Answer $i",
                    sourceTitle = "Source $i",
                    sourceType = "CHAT"
                )
            )
        }
        assertEquals(5, dao.getSavedAnswers().size)

        dao.clearAllSavedAnswers()
        assertTrue(dao.getSavedAnswers().isEmpty())
    }

    @Test
    fun savedAnswerEntity_preservesMarkdownFormatting() {
        val complexMarkdown = """
            # Header
            - Item 1
            - Item 2
            ```kotlin
            val x = 42
            ```
            Math: ${'$'}E = mc^2${'$'}
        """.trimIndent()

        val entity = SavedAnswerEntity(
            id = "md_1",
            question = "Explain relativity",
            answer = complexMarkdown,
            sourceTitle = "Physics Notes",
            sourceType = "DOCUMENT"
        )

        assertEquals(complexMarkdown, entity.answer)
        assertEquals("DOCUMENT", entity.sourceType)
    }
}
