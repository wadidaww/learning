package com.kresna.aitopicapp.util

object Constants {
    const val DEFAULT_BATCH_SIZE = 10

    /** How many already generated items are listed in the prompt to steer the model away from repeats. */
    const val EXISTING_QUESTIONS_LIMIT = 40

    /** Long items are shortened in the prompt to keep the request small. */
    const val EXISTING_QUESTION_MAX_CHARS = 140

    /** Start generating the next batch when the user is this many pages away from the end. */
    const val PREFETCH_THRESHOLD = 3

    /** Total attempts for one generation request (initial try + retries). */
    const val MAX_GENERATION_ATTEMPTS = 3
    const val RETRY_BASE_DELAY_MS = 1_500L

    const val MAX_OUTPUT_TOKENS = 8192
    const val TEMPERATURE = 0.8

    object MixContent {
        const val MCQ_COUNT = 4
        const val QA_COUNT = 3
        const val FUN_FACT_COUNT = 3
        val TOTAL get() = MCQ_COUNT + QA_COUNT + FUN_FACT_COUNT
    }
}
