package com.kresna.aitopicapp.domain.model

/** Which subset of a topic's content the pager shows. */
enum class ContentFilter {
    /** All content of the selected type; new content is generated when reaching the end. */
    ALL,

    /** Only bookmarked items (review mode, no generation). */
    BOOKMARKED,

    /** Only MCQs answered incorrectly (review mode, no generation). */
    MISSED;

    companion object {
        fun fromString(value: String?): ContentFilter =
            entries.firstOrNull { it.name == value } ?: ALL
    }
}
