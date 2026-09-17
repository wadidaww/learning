package com.kresna.aitopicapp.data.remote

data class BytePlusRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 4096
)

data class ChatMessage(
    val role: String,
    val content: String
)
