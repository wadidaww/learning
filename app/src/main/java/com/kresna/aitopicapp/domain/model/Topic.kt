package com.kresna.aitopicapp.domain.model

data class Topic(
    val id: Long = 0,
    val name: String,
    val description: String,
    val defaultContentType: ContentType = ContentType.MCQ,
    val createdAt: Long = System.currentTimeMillis()
)
