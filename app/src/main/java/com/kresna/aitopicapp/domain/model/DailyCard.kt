package com.kresna.aitopicapp.domain.model

/** The card shown on the home-screen widget today. */
data class DailyCard(
    val contentId: Long,
    val topicId: Long,
    val topicName: String,
    val contentType: ContentType,
    /** What the widget shows: the fact for a Spark, the question otherwise. */
    val text: String
)

/** One item the daily card can be picked from. */
data class DailyCandidate(
    val contentId: Long,
    val topicId: Long,
    val isViewed: Boolean
)

/** The card picked for a day, kept so every refresh on that day shows the same card. */
data class DailyPick(
    /** [java.time.LocalDate.toEpochDay] of the day the card was picked for. */
    val epochDay: Long,
    val contentId: Long
)
