package com.kresna.aitopicapp.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent

private const val TAG = "ContentParser"

object ContentParser {
    private val gson = Gson()

    fun parseResponse(responseText: String, contentType: ContentType, topicId: Long): Result<List<GeneratedContent>> {
        return try {
            val cleanedJson = cleanJsonResponse(responseText)
            val jsonArray = gson.fromJson(cleanedJson, JsonArray::class.java)

            if (jsonArray == null) {
                Log.w(TAG, "Parsed JSON array is null")
                return Result.success(emptyList())
            }

            val results = mutableListOf<GeneratedContent>()

            for (i in 0 until jsonArray.size()) {
                val element = jsonArray.get(i)
                if (element is JsonObject) {
                    val content = when (contentType) {
                        ContentType.MCQ -> parseMcq(element, topicId)
                        ContentType.QA -> parseQa(element, topicId)
                        ContentType.FUN_FACT -> parseFunFact(element, topicId)
                    }
                    results.add(content)
                } else {
                    Log.w(TAG, "Array element $i is not a JsonObject: ${element?.javaClass?.simpleName}")
                }
            }

            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse AI response", e)
            Result.failure(Exception("Failed to parse AI response: ${e.message}"))
        }
    }

    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    private fun parseMcq(jsonObject: JsonObject, topicId: Long): GeneratedContent {
        val question = jsonObject.get("question")?.asString ?: ""
        val optionsArray = jsonObject.getAsJsonArray("options")
        val options = (0 until optionsArray.size()).map { optionsArray.get(it).asString }
        val correctIndex = jsonObject.get("correctIndex")?.asInt ?: 0
        val explanation = jsonObject.get("explanation")?.asString

        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.MCQ,
            question = question,
            options = options,
            correctIndex = correctIndex,
            explanation = explanation
        )
    }

    private fun parseQa(jsonObject: JsonObject, topicId: Long): GeneratedContent {
        val question = jsonObject.get("question")?.asString ?: ""
        val answer = jsonObject.get("answer")?.asString

        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.QA,
            question = question,
            answer = answer
        )
    }

    private fun parseFunFact(jsonObject: JsonObject, topicId: Long): GeneratedContent {
        val fact = jsonObject.get("fact")?.asString ?: ""
        val source = jsonObject.get("source")?.asString

        return GeneratedContent(
            topicId = topicId,
            contentType = ContentType.FUN_FACT,
            question = "Did you know?",
            answer = fact,
            explanation = source
        )
    }
}
