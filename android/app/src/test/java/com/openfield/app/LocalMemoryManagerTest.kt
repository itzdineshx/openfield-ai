package com.openfield.app

import com.openfield.app.data.local.dao.SettingsDao
import com.openfield.app.data.local.entity.SettingEntity
import com.openfield.app.feature.ai.memory.LocalMemoryManager
import com.openfield.app.feature.ai.memory.MemoryCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMemoryManagerTest {

    private class FakeSettingsDao : SettingsDao {
        val map = mutableMapOf<String, String>()

        override suspend fun getSetting(key: String): String? = map[key]

        override suspend fun setSetting(entity: SettingEntity) {
            map[entity.key] = entity.value
        }

        override fun getAllSettingsFlow(): Flow<List<SettingEntity>> = flowOf(emptyList())
    }

    @Test
    fun addMemory_addsItemAndBuildsPromptContext() {
        val fakeDao = FakeSettingsDao()
        val manager = LocalMemoryManager(fakeDao, CoroutineScope(Dispatchers.Unconfined))

        val item1 = manager.addMemory(MemoryCategory.PREFERENCES, "Prefer concise answers with code snippets")
        val item2 = manager.addMemory(MemoryCategory.LEARNING_GOALS, "Studying Kotlin coroutines and Room migrations")

        assertEquals(2, manager.memories.value.size)
        assertEquals("Prefer concise answers with code snippets", item1.content)
        assertEquals(MemoryCategory.PREFERENCES, item1.category)

        val promptContext = manager.buildContextForPrompt()
        assertTrue(promptContext.contains("User Profile & Context:"))
        assertTrue(promptContext.contains("Prefer concise answers with code snippets"))
        assertTrue(promptContext.contains("Studying Kotlin coroutines and Room migrations"))
    }

    @Test
    fun updateMemory_updatesContentCorrectly() {
        val fakeDao = FakeSettingsDao()
        val manager = LocalMemoryManager(fakeDao, CoroutineScope(Dispatchers.Unconfined))

        val item = manager.addMemory(MemoryCategory.DOMAIN_CONTEXT, "Working in Automotive Embedded Engineering")
        manager.updateMemory(item.id, "Working in Automotive Robotics Engineering")

        assertEquals(1, manager.memories.value.size)
        assertEquals("Working in Automotive Robotics Engineering", manager.memories.value[0].content)
    }

    @Test
    fun deleteMemory_removesTargetItem() {
        val fakeDao = FakeSettingsDao()
        val manager = LocalMemoryManager(fakeDao, CoroutineScope(Dispatchers.Unconfined))

        val item = manager.addMemory(MemoryCategory.PREFERENCES, "Use metric units")
        manager.deleteMemory(item.id)

        assertTrue(manager.memories.value.isEmpty())
        assertTrue(manager.buildContextForPrompt().isEmpty())
    }

    @Test
    fun clearAll_clearsAllItems() {
        val fakeDao = FakeSettingsDao()
        val manager = LocalMemoryManager(fakeDao, CoroutineScope(Dispatchers.Unconfined))

        manager.addMemory(MemoryCategory.PREFERENCES, "Pref 1")
        manager.addMemory(MemoryCategory.LEARNING_GOALS, "Goal 1")
        assertEquals(2, manager.memories.value.size)

        manager.clearAll()
        assertTrue(manager.memories.value.isEmpty())
    }
}
