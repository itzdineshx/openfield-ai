package com.openfield.app.feature.ai.memory

import com.openfield.app.data.local.dao.SettingsDao
import com.openfield.app.data.local.entity.SettingEntity
import com.openfield.app.feature.ai.mode.LenientJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages user-controlled on-device personal memory across explicit categories (Phase 3 Gate 7).
 * Zero cloud transmission. Fully inspectable, editable, and clearable by the user.
 */
class LocalMemoryManager(
    private val settingsDao: SettingsDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _memories = MutableStateFlow<List<LocalMemoryItem>>(emptyList())
    val memories: StateFlow<List<LocalMemoryItem>> = _memories.asStateFlow()

    init {
        loadPersistedMemories()
    }

    private fun loadPersistedMemories() {
        scope.launch {
            try {
                val rawJson = settingsDao.getSetting(SETTINGS_KEY)
                if (!rawJson.isNullOrBlank()) {
                    val list = (LenientJson.parse(rawJson) as? List<*>) ?: emptyList<Any>()
                    val parsed = list.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val id = map["id"]?.toString() ?: return@mapNotNull null
                        val catStr = map["category"]?.toString() ?: MemoryCategory.PREFERENCES.name
                        val category = try {
                            MemoryCategory.valueOf(catStr)
                        } catch (e: Exception) {
                            MemoryCategory.PREFERENCES
                        }
                        val content = map["content"]?.toString() ?: return@mapNotNull null
                        val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        LocalMemoryItem(id = id, category = category, content = content, createdAt = createdAt)
                    }
                    _memories.value = parsed
                }
            } catch (ignored: Exception) {}
        }
    }

    fun addMemory(category: MemoryCategory, content: String): LocalMemoryItem {
        val trimmed = content.trim()
        val item = LocalMemoryItem(category = category, content = trimmed)
        _memories.value = _memories.value + item
        persistMemories()
        return item
    }

    fun updateMemory(id: String, newContent: String) {
        val trimmed = newContent.trim()
        _memories.value = _memories.value.map {
            if (it.id == id) it.copy(content = trimmed) else it
        }
        persistMemories()
    }

    fun deleteMemory(id: String) {
        _memories.value = _memories.value.filter { it.id != id }
        persistMemories()
    }

    fun clearAll() {
        _memories.value = emptyList()
        persistMemories()
    }

    /**
     * Formats memory items into a compact context string for LLM prompt prefill.
     * Bounded to a maximum number of items to prevent CPU prompt latency bloat.
     */
    fun buildContextForPrompt(maxItems: Int = 5): String {
        val active = _memories.value.take(maxItems)
        if (active.isEmpty()) return ""

        return buildString {
            append("User Profile & Context:\n")
            active.groupBy { it.category }.forEach { (category, items) ->
                append("- ${category.displayName}: ")
                append(items.joinToString("; ") { it.content })
                append("\n")
            }
        }.trim()
    }

    private fun persistMemories() {
        val current = _memories.value
        scope.launch {
            try {
                val serializableList = current.map { item ->
                    mapOf(
                        "id" to item.id,
                        "category" to item.category.name,
                        "content" to item.content,
                        "createdAt" to item.createdAt
                    )
                }
                val json = LenientJson.write(serializableList)
                settingsDao.setSetting(SettingEntity(key = SETTINGS_KEY, value = json))
            } catch (ignored: Exception) {}
        }
    }

    companion object {
        const val SETTINGS_KEY = "user_structured_memories"
    }
}
