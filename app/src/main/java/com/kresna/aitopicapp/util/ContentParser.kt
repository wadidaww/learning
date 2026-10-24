package com.kresna.aitopicapp.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import java.io.StringReader

/**
 * Turns a raw LLM reply into [GeneratedContent] items.
 *
 * Models do not always follow the "return ONLY a JSON array" instruction, so this parser is
 * deliberately forgiving: it strips reasoning blocks and markdown fences, accepts an object
 * wrapping the array, tolerates loosely typed fields, and salvages the complete items of a
 * reply that was cut off by the token limit. Items missing required fields are dropped.
 */
object ContentParser {

    private val THINK_BLOCK = Regex("(?s)<think>.*?</think>")
    private val CODE_FENCE = Regex("(?s)```[a-zA-Z0-9_-]*\\s*\\n?(.*?)```")
    private val OPTION_PREFIX = Regex("^\\(?[A-Ha-h1-8][.):]\\s+")
    private val ARRAY_KEYS = listOf("items", "content", "questions", "facts", "data", "results")

    fun parse(responseText: String, requestedType: ContentType, topicId: Long): List<GeneratedContent> {
        return extractObjects(responseText).mapNotNull { parseItem(it, requestedType, topicId) }
    }

    /** Returns every JSON object item found in [responseText], best effort. */
    internal fun extractObjects(responseText: String): List<JsonObject> {
        val text = cleanResponse(responseText)
        if (text.isEmpty()) return emptyList()

        parseLenient(text)?.let { element ->
            val objects = objectsFrom(element)
            if (objects.isNotEmpty()) return objects
        }
        // The full text is not valid JSON (usually truncated output): salvage complete objects.
        return salvageObjects(text)
    }

    private fun cleanResponse(raw: String): String {
        var text = THINK_BLOCK.replace(raw, "").trim()
        // An unterminated <think> means the whole reply is reasoning without an answer.
        if (text.startsWith("<think>")) return ""

        CODE_FENCE.find(text)?.let { match ->
            val inner = match.groupValues[1].trim()
            if (inner.startsWith("[") || inner.startsWith("{")) text = inner
        }
        // Drop an unterminated opening fence left by a truncated reply.
        if (text.startsWith("```")) text = text.substringAfter('\n', "")

        val start = text.indexOfFirst { it == '[' || it == '{' }
        return if (start >= 0) text.substring(start).trim() else ""
    }

    private fun parseLenient(text: String): JsonElement? {
        return try {
            val reader = JsonReader(StringReader(text)).apply { isLenient = true }
            JsonParser.parseReader(reader)
        } catch (e: Exception) {
            null
        }
    }

    private fun objectsFrom(element: JsonElement): List<JsonObject> {
        return when {
            element.isJsonArray -> element.asJsonArray.filterIsInstance<JsonObject>()
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val wrapped = ARRAY_KEYS.firstNotNullOfOrNull { key ->
                    obj.get(key)?.takeIf { it.isJsonArray }?.asJsonArray
                } ?: obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
                wrapped?.filterIsInstance<JsonObject>() ?: listOf(obj)
            }
            else -> emptyList()
        }
    }

    /** Scans for balanced top-level `{...}` blocks inside the outer array and parses each one. */
    private fun salvageObjects(text: String): List<JsonObject> {
        val result = mutableListOf<JsonObject>()
        // Items live inside the first array, also when the array is wrapped in an object.
        val searchFrom = text.indexOf('[').let { if (it >= 0) it + 1 else 0 }

        var depth = 0
        var inString = false
        var escaped = false
        var objectStart = -1
        for (i in searchFrom until text.length) {
            val c = text[i]
            if (inString) {
                when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> {
                    if (depth == 0) objectStart = i
                    depth++
                }
                '}' -> if (depth > 0) {
                    depth--
                    if (depth == 0 && objectStart >= 0) {
                        val element = parseLenient(text.substring(objectStart, i + 1))
                        if (element != null && element.isJsonObject) result.add(element.asJsonObject)
                        objectStart = -1
                    }
                }
            }
        }
        return result
    }

    private fun parseItem(obj: JsonObject, requestedType: ContentType, topicId: Long): GeneratedContent? {
        val type = if (requestedType == ContentType.MIX) resolveMixType(obj) else requestedType
        return when (type) {
            ContentType.MCQ -> parseMcq(obj, topicId)
            ContentType.QA -> parseQa(obj, topicId)
            ContentType.FUN_FACT -> parseFunFact(obj, topicId)
            else -> null
        }
    }

    private fun resolveMixType(obj: JsonObject): ContentType? {
        val declared = obj.string("type", "contentType", "kind")
            ?.uppercase()
            ?.replace(Regex("[^A-Z]"), "")
        return when (declared) {
            "MCQ", "MULTIPLECHOICE", "QUIZ" -> ContentType.MCQ
            "QA", "QANDA", "QUESTIONANSWER", "QUESTIONANDANSWER" -> ContentType.QA
            "FUNFACT", "FACT" -> ContentType.FUN_FACT
            else -> when {
                obj.has("options") || obj.has("choices") -> ContentType.MCQ
                obj.has("fact") -> ContentType.FUN_FACT
                obj.has("answer") -> ContentType.QA
                else -> null
            }
        }
    }

    private fun parseMcq(obj: JsonObject, topicId: Long): GeneratedContent? {
        val question = obj.string("question", "prompt") ?: return null
        val options = obj.stringList("options", "choices")
        if (options.size < 2) return null
        val correctIndex = resolveCorrectIndex(obj, options) ?: return null

        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.MCQ,
            question = question,
            options = options,
            correctIndex = correctIndex,
            explanation = obj.string("explanation", "rationale"),
            codeSnippet = obj.string("codeSnippet", "code_snippet", "code"),
            codeLanguage = obj.string("codeLanguage", "code_language", "language")
        )
    }

    private fun parseQa(obj: JsonObject, topicId: Long): GeneratedContent? {
        val question = obj.string("question", "prompt") ?: return null
        val answer = obj.string("answer", "response") ?: return null
        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.QA,
            question = question,
            answer = answer,
            codeSnippet = obj.string("codeSnippet", "code_snippet", "code"),
            codeLanguage = obj.string("codeLanguage", "code_language", "language")
        )
    }

    private fun parseFunFact(obj: JsonObject, topicId: Long): GeneratedContent? {
        val fact = obj.string("fact", "funFact", "text", "answer") ?: return null
        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.FUN_FACT,
            question = FUN_FACT_TITLE,
            answer = fact,
            explanation = obj.string("source", "explanation"),
            codeSnippet = obj.string("codeSnippet", "code_snippet", "code"),
            codeLanguage = obj.string("codeLanguage", "code_language", "language")
        )
    }

    /**
     * Accepts a 0-based index, a 1-based index (only when unambiguous), a letter ("B"),
     * or the text of the correct option.
     */
    private fun resolveCorrectIndex(obj: JsonObject, options: List<String>): Int? {
        val raw = sequenceOf("correctIndex", "correct_index", "answerIndex", "correctAnswer", "correct", "answer")
            .mapNotNull { key -> obj.get(key)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive }
            .firstOrNull() ?: return null

        val asText = raw.asString.trim()
        val number = if (raw.isNumber) raw.asDouble.toInt() else asText.toIntOrNull()
        if (number != null) {
            return when (number) {
                in options.indices -> number
                options.size -> number - 1
                else -> null
            }
        }

        if (asText.length == 1 && asText[0].isLetter()) {
            val index = asText.uppercase()[0] - 'A'
            return index.takeIf { it in options.indices }
        }
        // "B. Quick Sort" or "(B) Quick Sort"
        OPTION_PREFIX.find(asText)?.let { prefix ->
            val letter = prefix.value.firstOrNull { it.isLetter() }
            val index = letter?.let { it.uppercaseChar() - 'A' }
            if (index != null && index in options.indices) return index
        }
        // "Quick Sort"
        val normalizedAnswer = stripOptionPrefix(asText).lowercase()
        return options.indexOfFirst { stripOptionPrefix(it).lowercase() == normalizedAnswer }
            .takeIf { it >= 0 }
    }

    private fun stripOptionPrefix(option: String): String = OPTION_PREFIX.replace(option.trim(), "")

    private fun JsonObject.string(vararg keys: String): String? {
        for (key in keys) {
            val element = get(key) ?: continue
            if (element.isJsonPrimitive) {
                val value = element.asString.trim()
                if (value.isNotEmpty()) return value
            }
        }
        return null
    }

    private fun JsonObject.stringList(vararg keys: String): List<String> {
        for (key in keys) {
            val element = get(key) ?: continue
            val values = when {
                element.isJsonArray -> element.asJsonArray.primitiveStrings()
                // {"A": "...", "B": "..."}
                element.isJsonObject -> element.asJsonObject.entrySet()
                    .filter { it.value.isJsonPrimitive }
                    .map { "${it.key}. ${it.value.asString.trim()}" }
                else -> emptyList()
            }.filter { it.isNotBlank() }
            if (values.isNotEmpty()) return values
        }
        return emptyList()
    }

    private fun JsonArray.primitiveStrings(): List<String> =
        filter { it.isJsonPrimitive }.map { it.asString.trim() }

    const val FUN_FACT_TITLE = "Did you know?"
}
