package com.kresna.aitopicapp.util

/**
 * Handles the "A. ", "(b) ", "3) " enumerators the model sometimes puts in front of quiz options.
 * The card, the clipboard and the share sheet all show options through this, so they agree.
 */
object OptionText {
    private val ENUMERATOR = Regex("^\\(?[A-Ha-h1-8][.):]\\s+")

    /** True when [option] starts with an enumerator such as "B) ". */
    fun hasEnumerator(option: String): Boolean = ENUMERATOR.containsMatchIn(option.trim())

    /** Removes a leading enumerator from one option (used when matching an answer against options). */
    fun stripEnumerator(option: String): String = ENUMERATOR.replace(option.trim(), "")

    /**
     * Removes the enumerators from a full option list, but only when every option has one. A lone
     * match is real text ("C. elegans is a nematode", "1. 5 million") and is left alone.
     */
    fun stripEnumerators(options: List<String>): List<String> {
        val trimmed = options.map { it.trim() }
        val allEnumerated = trimmed.size > 1 && trimmed.all { hasEnumerator(it) }
        return if (allEnumerated) trimmed.map { stripEnumerator(it) } else trimmed
    }
}
