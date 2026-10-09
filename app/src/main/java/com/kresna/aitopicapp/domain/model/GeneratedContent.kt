package com.kresna.aitopicapp.domain.model

data class GeneratedContent(
    val id: Long = 0,
    val topicId: Long,
    val contentType: ContentType,
    val question: String,
    val answer: String? = null,
    val options: List<String>? = null,
    val correctIndex: Int? = null,
    val explanation: String? = null,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val isViewed: Boolean = false,
    /** Option picked by the user for an MCQ, or null if not answered yet. */
    val userAnswerIndex: Int? = null,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAnswered: Boolean get() = userAnswerIndex != null
    val isAnsweredCorrectly: Boolean get() = userAnswerIndex != null && userAnswerIndex == correctIndex

    /**
     * Text that identifies this item for duplicate detection. Fun facts all share the
     * same "Did you know?" question, so their fact text (stored in [answer]) is used.
     */
    val dedupeText: String
        get() = if (contentType == ContentType.FUN_FACT) answer.orEmpty() else question
}
