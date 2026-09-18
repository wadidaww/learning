package com.kresna.aitopicapp.domain.model

enum class ContentType(val displayName: String) {
    MCQ("Multiple Choice"),
    QA("Question & Answer"),
    FUN_FACT("Fun Fact"),
    MIX("Mix");

    companion object {
        fun fromString(value: String?): ContentType {
            return try {
                valueOf(value ?: "")
            } catch (e: IllegalArgumentException) {
                MCQ
            }
        }
    }
}
