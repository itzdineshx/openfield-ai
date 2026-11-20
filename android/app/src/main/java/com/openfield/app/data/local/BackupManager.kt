package com.openfield.app.data.local

import android.content.Context
import android.net.Uri
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.data.local.entity.ChatSessionEntity
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.local.entity.TroubleshootSessionEntity
import com.openfield.app.data.local.entity.WritingDraftEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles 100% offline, local backup and restore of user data (chats, notes, lessons, troubleshoots).
 * Zero cloud dependency — writes directly to user-selected SAF file destination.
 */
class BackupManager(private val database: OpenFieldDatabase) {

    suspend fun exportBackup(context: Context, destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val sessions = database.chatDao().getAllSessions()
            val messages = database.chatDao().getAllMessages()
            val drafts = database.modeSessionDao().getWritingDraftsList()
            val troubleshootSessions = database.modeSessionDao().getAllTroubleshootSessions()
            val learningSessions = database.modeSessionDao().getAllLearningSessions()

            val rootJson = JSONObject().apply {
                put("format", "openfield_backup")
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())

                // Sessions
                val sessionsArray = JSONArray()
                sessions.forEach { s ->
                    sessionsArray.put(JSONObject().apply {
                        put("id", s.id)
                        put("title", s.title)
                        put("preview", s.preview)
                        put("lastUpdated", s.lastUpdated)
                        put("messageCount", s.messageCount)
                        put("isPinned", s.isPinned)
                    })
                }
                put("sessions", sessionsArray)

                // Messages
                val messagesArray = JSONArray()
                messages.forEach { m ->
                    messagesArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("sessionId", m.sessionId)
                        put("text", m.text)
                        put("isUser", m.isUser)
                        put("timestamp", m.timestamp)
                        put("isComplete", m.isComplete)
                    })
                }
                put("messages", messagesArray)

                // Writing Drafts
                val draftsArray = JSONArray()
                drafts.forEach { d ->
                    draftsArray.put(JSONObject().apply {
                        put("id", d.id)
                        put("title", d.title)
                        put("intent", d.intent)
                        put("tone", d.tone)
                        put("content", d.content)
                        put("wordCount", d.wordCount)
                        put("updatedAt", d.updatedAt)
                    })
                }
                put("writingDrafts", draftsArray)

                // Troubleshoot Sessions
                val troubleshootArray = JSONArray()
                troubleshootSessions.forEach { t ->
                    troubleshootArray.put(JSONObject().apply {
                        put("id", t.id)
                        put("problem", t.problem)
                        put("category", t.category)
                        put("currentStepNumber", t.currentStepNumber)
                        put("isResolved", t.isResolved)
                        put("assessmentJson", t.assessmentJson)
                        put("historyJson", t.historyJson)
                        put("updatedAt", t.updatedAt)
                    })
                }
                put("troubleshootSessions", troubleshootArray)

                // Learning Sessions
                val learningArray = JSONArray()
                learningSessions.forEach { l ->
                    learningArray.put(JSONObject().apply {
                        put("id", l.id)
                        put("topic", l.topic)
                        put("level", l.level)
                        put("lessonTitle", l.lessonTitle)
                        put("lessonJson", l.lessonJson)
                        put("isQuizCompleted", l.isQuizCompleted)
                        put("updatedAt", l.updatedAt)
                    })
                }
                put("learningSessions", learningArray)
            }

            val jsonString = rootJson.toString(2)
            context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                outStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outStream.flush()
            } ?: return@withContext Result.failure(Exception("Could not open destination file for writing"))

            val totalRecords = sessions.size + messages.size + drafts.size + troubleshootSessions.size + learningSessions.size
            Result.success(totalRecords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(context: Context, sourceUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }
            } ?: return@withContext Result.failure(Exception("Could not open source backup file"))

            val rootJson = JSONObject(content.toString())
            if (!rootJson.has("format") || rootJson.getString("format") != "openfield_backup") {
                return@withContext Result.failure(Exception("Invalid file format. Expected OpenField backup."))
            }

            var restoredCount = 0

            // Restore Sessions
            if (rootJson.has("sessions")) {
                val sessionsArray = rootJson.getJSONArray("sessions")
                val sessionEntities = mutableListOf<ChatSessionEntity>()
                for (i in 0 until sessionsArray.length()) {
                    val obj = sessionsArray.getJSONObject(i)
                    sessionEntities.add(
                        ChatSessionEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            preview = obj.optString("preview", ""),
                            lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis()),
                            messageCount = obj.optInt("messageCount", 0),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
                if (sessionEntities.isNotEmpty()) {
                    database.chatDao().insertSessions(sessionEntities)
                    restoredCount += sessionEntities.size
                }
            }

            // Restore Messages
            if (rootJson.has("messages")) {
                val messagesArray = rootJson.getJSONArray("messages")
                val messageEntities = mutableListOf<ChatMessageEntity>()
                for (i in 0 until messagesArray.length()) {
                    val obj = messagesArray.getJSONObject(i)
                    messageEntities.add(
                        ChatMessageEntity(
                            id = obj.getString("id"),
                            sessionId = obj.getString("sessionId"),
                            text = obj.getString("text"),
                            isUser = obj.getBoolean("isUser"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isComplete = obj.optBoolean("isComplete", true)
                        )
                    )
                }
                if (messageEntities.isNotEmpty()) {
                    database.chatDao().insertMessages(messageEntities)
                    restoredCount += messageEntities.size
                }
            }

            // Restore Writing Drafts
            if (rootJson.has("writingDrafts")) {
                val draftsArray = rootJson.getJSONArray("writingDrafts")
                val draftEntities = mutableListOf<WritingDraftEntity>()
                for (i in 0 until draftsArray.length()) {
                    val obj = draftsArray.getJSONObject(i)
                    draftEntities.add(
                        WritingDraftEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            intent = obj.optString("intent", "general"),
                            tone = obj.optString("tone", "neutral"),
                            content = obj.getString("content"),
                            wordCount = obj.optInt("wordCount", 0),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (draftEntities.isNotEmpty()) {
                    database.modeSessionDao().insertWritingDrafts(draftEntities)
                    restoredCount += draftEntities.size
                }
            }

            // Restore Troubleshoot Sessions
            if (rootJson.has("troubleshootSessions")) {
                val array = rootJson.getJSONArray("troubleshootSessions")
                val list = mutableListOf<TroubleshootSessionEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TroubleshootSessionEntity(
                            id = obj.getString("id"),
                            problem = obj.getString("problem"),
                            category = obj.optString("category", "General"),
                            currentStepNumber = obj.optInt("currentStepNumber", 1),
                            isResolved = obj.optBoolean("isResolved", false),
                            assessmentJson = obj.optString("assessmentJson", "{}"),
                            historyJson = obj.optString("historyJson", "[]"),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.modeSessionDao().insertTroubleshootSessions(list)
                    restoredCount += list.size
                }
            }

            // Restore Learning Sessions
            if (rootJson.has("learningSessions")) {
                val array = rootJson.getJSONArray("learningSessions")
                val list = mutableListOf<LearningSessionEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LearningSessionEntity(
                            id = obj.getString("id"),
                            topic = obj.getString("topic"),
                            level = obj.optString("level", "INTERMEDIATE"),
                            lessonTitle = obj.optString("lessonTitle", ""),
                            lessonJson = obj.optString("lessonJson", "{}"),
                            isQuizCompleted = obj.optBoolean("isQuizCompleted", false),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.modeSessionDao().insertLearningSessions(list)
                    restoredCount += list.size
                }
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
