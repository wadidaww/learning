package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.GeneratedContent

object ContentDeduplicator {

    private val NON_ALPHANUMERIC = Regex("[^\\p{L}\\p{N}]+")

    /** Case, whitespace and punctuation insensitive key used to compare items. */
    fun normalize(text: String): String =
        NON_ALPHANUMERIC.replace(text.lowercase(), " ").trim()

    /**
     * Keeps the [candidates] whose [GeneratedContent.dedupeText] does not appear in
     * [existingTexts] and that are not repeated within the batch itself.
     */
    fun filterNew(candidates: List<GeneratedContent>, existingTexts: Collection<String>): List<GeneratedContent> {
        val seen = existingTexts.mapTo(HashSet()) { normalize(it) }
        return candidates.filter { item ->
            val key = normalize(item.dedupeText)
            key.isNotEmpty() && seen.add(key)
        }
    }
}
