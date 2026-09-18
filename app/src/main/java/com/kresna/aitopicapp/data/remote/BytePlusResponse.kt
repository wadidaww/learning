package com.kresna.aitopicapp.data.remote

data class BytePlusResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int?,
    val message: ResponseMessage?,
    val finish_reason: String?
)

data class ResponseMessage(
    val role: String?,
    val content: String?
)

data class Usage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

data class BytePlusErrorResponse(
    val error: BytePlusError?
)

data class BytePlusError(
    val code: String?,
    val message: String?,
    val status: String?
)
