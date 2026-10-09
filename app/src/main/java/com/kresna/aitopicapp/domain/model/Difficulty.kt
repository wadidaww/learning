package com.kresna.aitopicapp.domain.model

enum class Difficulty(val displayName: String, val promptHint: String) {
    BEGINNER(
        "Beginner",
        "Target beginners: focus on core concepts and terminology, use plain language and concrete examples."
    ),
    INTERMEDIATE(
        "Intermediate",
        "Target intermediate learners: go beyond definitions into how and why things work, common pitfalls and trade-offs."
    ),
    ADVANCED(
        "Advanced",
        "Target advanced learners: cover edge cases, internals, nuanced trade-offs and expert-level reasoning."
    ),
    MIXED(
        "Mixed",
        "Mix difficulty levels (roughly 30% easy, 50% medium, 20% hard)."
    );

    companion object {
        fun fromString(value: String?): Difficulty =
            entries.firstOrNull { it.name == value } ?: MIXED
    }
}
