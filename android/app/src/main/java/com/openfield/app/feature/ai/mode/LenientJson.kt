package com.openfield.app.feature.ai.mode

/**
 * Forgiving JSON reader for small on-device model output: tolerates trailing commas,
 * unquoted keys, single quotes, comments, unescaped inner quotes and truncated output
 * (an object cut off mid-stream returns whatever fields were completed).
 */
object LenientJson {

    fun parse(text: String): Any? = try {
        Reader(text).readValue()
    } catch (e: Exception) {
        null
    }

    fun parseObject(text: String): Map<String, Any?>? {
        val start = text.indexOf('{')
        if (start < 0) return null
        @Suppress("UNCHECKED_CAST")
        return parse(text.substring(start)) as? Map<String, Any?>
    }

    fun write(value: Any?): String = StringBuilder().also { writeTo(it, value) }.toString()

    private fun writeTo(sb: StringBuilder, value: Any?) {
        when (value) {
            null -> sb.append("null")
            is String -> writeString(sb, value)
            is Boolean, is Int, is Long -> sb.append(value.toString())
            is Number -> sb.append(value.toDouble().let { if (it == Math.floor(it)) it.toLong().toString() else it.toString() })
            is Map<*, *> -> {
                sb.append('{')
                var first = true
                value.forEach { (k, v) ->
                    if (!first) sb.append(',')
                    first = false
                    writeString(sb, k.toString())
                    sb.append(':')
                    writeTo(sb, v)
                }
                sb.append('}')
            }
            is Iterable<*> -> {
                sb.append('[')
                value.forEachIndexed { i, v ->
                    if (i > 0) sb.append(',')
                    writeTo(sb, v)
                }
                sb.append(']')
            }
            else -> writeString(sb, value.toString())
        }
    }

    private fun writeString(sb: StringBuilder, s: String) {
        sb.append('"')
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append(String.format("\\u%04x", c.code)) else sb.append(c)
            }
        }
        sb.append('"')
    }

    private class Reader(private val s: String) {
        private var i = 0

        fun readValue(): Any? {
            skipWs()
            if (i >= s.length) return null
            return when (val c = s[i]) {
                '{' -> readObject()
                '[' -> readArray()
                '"', '\'', '“' -> readString(if (c == '“') '”' else c)
                else -> readBare()
            }
        }

        private fun readObject(): Map<String, Any?> {
            val map = LinkedHashMap<String, Any?>()
            i++
            while (true) {
                skipWs()
                if (i >= s.length) return map
                if (s[i] == '}') { i++; return map }
                if (s[i] == ',') { i++; continue }
                val key = when (s[i]) {
                    '"', '\'' -> readString(s[i])
                    else -> readKeyword()
                }
                skipWs()
                if (i < s.length && (s[i] == ':' || s[i] == '=')) i++ else if (i >= s.length) return map
                skipWs()
                if (i >= s.length) return map
                map[key] = readValue()
            }
        }

        private fun readArray(): List<Any?> {
            val list = mutableListOf<Any?>()
            i++
            while (true) {
                skipWs()
                if (i >= s.length) return list
                when (s[i]) {
                    ']' -> { i++; return list }
                    ',' -> { i++; continue }
                    '}' -> { i++; return list }
                }
                val before = i
                list.add(readValue())
                if (i == before) i++
            }
        }

        private fun readString(quote: Char): String {
            val sb = StringBuilder()
            i++
            while (i < s.length) {
                val c = s[i]
                if (c == '\\' && i + 1 < s.length) {
                    val n = s[i + 1]
                    when (n) {
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        'r' -> {}
                        'b', 'f' -> {}
                        'u' -> {
                            val hex = s.substring(i + 2, minOf(i + 6, s.length))
                            hex.toIntOrNull(16)?.let { sb.append(it.toChar()) }
                            i += 4
                        }
                        else -> sb.append(n)
                    }
                    i += 2
                    continue
                }
                if (c == quote || (quote == '”' && c == '"')) {
                    if (endsString()) {
                        i++
                        return sb.toString()
                    }
                    sb.append(c)
                    i++
                    continue
                }
                sb.append(c)
                i++
            }
            return sb.toString()
        }

        /** A quote only closes the string if structural JSON follows it. */
        private fun endsString(): Boolean {
            var j = i + 1
            while (j < s.length && (s[j] == ' ' || s[j] == '\t' || s[j] == '\r')) j++
            if (j >= s.length) return true
            return when (s[j]) {
                ',', '}', ']', ':', '\n' -> true
                else -> false
            }
        }

        private fun readKeyword(): String {
            val start = i
            while (i < s.length && s[i] != ':' && s[i] != '=' && s[i] != ',' && s[i] != '}' && !s[i].isWhitespace()) i++
            return s.substring(start, i)
        }

        private fun readBare(): Any? {
            val start = i
            while (i < s.length && s[i] != ',' && s[i] != '}' && s[i] != ']' && s[i] != '\n') i++
            val token = s.substring(start, i).trim()
            return when {
                token.equals("true", true) -> true
                token.equals("false", true) -> false
                token.equals("null", true) || token.isEmpty() -> null
                else -> token.toLongOrNull() ?: token.toDoubleOrNull() ?: token
            }
        }

        private fun skipWs() {
            while (i < s.length) {
                val c = s[i]
                when {
                    c.isWhitespace() -> i++
                    c == '/' && s.getOrNull(i + 1) == '/' -> {
                        while (i < s.length && s[i] != '\n') i++
                    }
                    else -> return
                }
            }
        }
    }
}

internal fun Map<String, Any?>.str(vararg keys: String): String? {
    for (key in keys) {
        val entry = entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: continue
        val v = entry.value ?: continue
        val text = when (v) {
            is String -> v
            is Number, is Boolean -> v.toString()
            is List<*> -> v.mapNotNull { item ->
                when (item) {
                    is String -> item.trim().takeIf { it.isNotEmpty() }
                    is Map<*, *> -> item.values.firstOrNull { it is String && it.isNotBlank() } as String?
                    null -> null
                    else -> item.toString()
                }
            }.mapIndexed { idx, item -> "${idx + 1}. ${item.replace(Regex("^\\d+[.)]\\s*"), "")}" }.joinToString("\n")
            else -> continue
        }.trim()
        if (text.isNotEmpty() && !text.equals("null", ignoreCase = true) && !text.equals("none", ignoreCase = true)) {
            return text
        }
    }
    return null
}

internal fun Map<String, Any?>.int(vararg keys: String): Int? {
    for (key in keys) {
        val v = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value ?: continue
        val n = when (v) {
            is Number -> v.toInt()
            is String -> v.trim().toIntOrNull()
            else -> null
        }
        if (n != null) return n
    }
    return null
}

internal fun Map<String, Any?>.bool(vararg keys: String): Boolean? {
    for (key in keys) {
        val v = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value ?: continue
        when (v) {
            is Boolean -> return v
            is String -> v.trim().lowercase().let { if (it == "true" || it == "yes") return true; if (it == "false" || it == "no") return false }
        }
    }
    return null
}

internal fun Map<String, Any?>.list(vararg keys: String): List<Any?>? {
    for (key in keys) {
        val v = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
        if (v is List<*>) return v
    }
    return null
}

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.obj(vararg keys: String): Map<String, Any?>? {
    for (key in keys) {
        val v = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
        if (v is Map<*, *>) return v as Map<String, Any?>
    }
    return null
}
