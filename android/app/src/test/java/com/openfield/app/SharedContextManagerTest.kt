package com.openfield.app

import com.openfield.app.data.local.dao.TaskSessionDao
import com.openfield.app.data.local.entity.TaskSessionEntity
import com.openfield.app.feature.ai.context.SharedContextHandoff
import com.openfield.app.feature.ai.context.SharedContextManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Proxy

class SharedContextManagerTest {

    private val fakeDao = Proxy.newProxyInstance(
        TaskSessionDao::class.java.classLoader,
        arrayOf(TaskSessionDao::class.java)
    ) { _, method, args ->
        when (method.name) {
            "getActiveSessionsFlow" -> flowOf(emptyList<TaskSessionEntity>())
            else -> null
        }
    } as TaskSessionDao

    private val manager = SharedContextManager(fakeDao, CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun handoffToWriter_setsPendingHandoffProperly() {
        manager.handoffToWriter(
            title = "Maintenance Summary",
            initialDraft = "The scanner roller must be cleaned every 500 scans.",
            sourceDocIds = listOf("doc_manual_1")
        )

        val pending = manager.pendingHandoff.value
        assertNotNull(pending)
        assertEquals("DOCUMENT_QA", pending!!.sourceWorkspace)
        assertEquals("CREATE_WRITE", pending.targetWorkspace)
        assertEquals("Maintenance Summary", pending.title)
        assertEquals("The scanner roller must be cleaned every 500 scans.", pending.textSnippet)
        assertEquals(listOf("doc_manual_1"), pending.sourceReferences)
    }

    @Test
    fun handoffToTroubleshooter_setsImageAndProblemSummary() {
        manager.handoffToTroubleshooter(
            problemSummary = "Flashing amber LED error on control board",
            imagePath = "/data/user/0/cache/board.jpg"
        )

        val pending = manager.pendingHandoff.value
        assertNotNull(pending)
        assertEquals("IMAGE_ANALYSIS", pending!!.sourceWorkspace)
        assertEquals("PROBLEM_SOLVER", pending.targetWorkspace)
        assertEquals("/data/user/0/cache/board.jpg", pending.imagePath)
    }

    @Test
    fun consumePendingHandoff_clearsPendingState() {
        val handoff = SharedContextHandoff(
            sourceWorkspace = "KNOWLEDGE_CENTER",
            targetWorkspace = "ASK_AI",
            title = "Test Question"
        )
        manager.setPendingHandoff(handoff)
        assertNotNull(manager.pendingHandoff.value)

        val consumed = manager.consumePendingHandoff()
        assertNotNull(consumed)
        assertEquals("Test Question", consumed!!.title)
        assertNull(manager.pendingHandoff.value)
    }
}
