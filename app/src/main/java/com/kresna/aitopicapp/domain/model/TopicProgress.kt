package com.kresna.aitopicapp.domain.model

data class TopicProgress(
    val total: Int = 0,
    val viewed: Int = 0,
    val answered: Int = 0,
    val correct: Int = 0,
    val bookmarked: Int = 0
) {
    val unviewed: Int get() = (total - viewed).coerceAtLeast(0)
    val missed: Int get() = (answered - correct).coerceAtLeast(0)
    val viewedFraction: Float get() = if (total == 0) 0f else viewed.toFloat() / total

    /** Percentage of answered MCQs that were correct, or null if none answered yet. */
    val accuracyPercent: Int? get() = if (answered == 0) null else (correct * 100) / answered
}

data class TopicOverview(
    val topic: Topic,
    val progress: TopicProgress
)
