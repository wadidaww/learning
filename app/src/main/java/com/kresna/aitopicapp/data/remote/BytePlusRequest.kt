package com.kresna.aitopicapp.data.remote

import com.google.gson.annotations.SerializedName

data class BytePlusRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096
)

data class ChatMessage(
    val role: String,
    val content: String
)
