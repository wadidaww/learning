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
    val isViewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
