package com.kresna.aitopicapp.data.remote

import com.google.gson.annotations.SerializedName

data class BytePlusResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int?,
    val message: ResponseMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class ResponseMessage(
    val role: String?,
    val content: String?,
    /** Filled by reasoning models; it is not part of the answer. */
    @SerializedName("reasoning_content")
    val reasoningContent: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?
)

data class BytePlusErrorResponse(
    val error: BytePlusError?
)

data class BytePlusError(
    val code: String?,
    val message: String?,
    val status: String?
)
