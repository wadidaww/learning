package com.kresna.aitopicapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.kresna.aitopicapp.domain.model.ContentType

/**
 * Visual identity of a card type in the feed: a two-colour gradient and a short label,
 * so the user can tell at a glance whether they are reading, answering or recalling.
 */
data class CardStyle(
    val label: String,
    val start: Color,
    val end: Color
) {
    /** Colour for small accents (pills, outlines) on top of the dark card. */
    val accent: Color get() = start

    companion object {
        val Spark = CardStyle(label = "SPARK", start = Violet, end = Pink)
        val Quiz = CardStyle(label = "QUICK QUIZ", start = Blue, end = Cyan)
        val Flashcard = CardStyle(label = "FLASHCARD", start = Teal, end = Sky)
        val Review = CardStyle(label = "REVIEW", start = Gold, end = Orange)
        val Summary = CardStyle(label = "RUN COMPLETE", start = Violet, end = Cyan)
        val Neutral = CardStyle(label = "", start = Ink500, end = Ink700)

        fun forType(type: ContentType): CardStyle = when (type) {
            ContentType.MCQ -> Quiz
            ContentType.QA -> Flashcard
            ContentType.FUN_FACT -> Spark
            ContentType.MIX -> Neutral
        }
    }
}
