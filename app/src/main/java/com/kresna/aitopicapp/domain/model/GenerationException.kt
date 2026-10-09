package com.kresna.aitopicapp.domain.model

/**
 * A content generation failure with a user-facing [message].
 * [isRetryable] is true for transient problems (network, rate limit, server errors,
 * malformed AI output) where trying again is likely to help.
 */
class GenerationException(
    message: String,
    val isRetryable: Boolean,
    cause: Throwable? = null
) : Exception(message, cause)
