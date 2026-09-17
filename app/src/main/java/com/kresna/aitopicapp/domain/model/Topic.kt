package com.kresna.aitopicapp.domain.model

data class Topic(
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
