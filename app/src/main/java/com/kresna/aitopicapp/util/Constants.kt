package com.kresna.aitopicapp.util

object Constants {
    const val DEFAULT_BATCH_SIZE = 10
    const val EXISTING_QUESTIONS_LIMIT = 20
    const val PREFETCH_THRESHOLD = 2

    object MixContent {
        const val MCQ_COUNT = 4
        const val QA_COUNT = 3
        const val FUN_FACT_COUNT = 3
        val TOTAL get() = MCQ_COUNT + QA_COUNT + FUN_FACT_COUNT
    }
}
