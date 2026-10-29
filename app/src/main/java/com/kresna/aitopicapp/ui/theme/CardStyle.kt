package com.kresna.aitopicapp.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kresna.aitopicapp.domain.model.ContentType

/**
 * Visual identity of a card type in the feed: an icon, a short label and a two-colour gradient
 * picked from the current theme's palette, so the user can tell at a glance whether they are
 * reading, answering or recalling.
 */
data class CardStyle(
    val label: String,
    val icon: ImageVector,
    private val start: (AppPalette) -> Color,
    private val end: (AppPalette) -> Color
) {
    /** Main accent: pills, outlines, progress. */
    val accent: Color
        @Composable get() = start(AppTheme.palette)

    /** Second gradient colour. */
    val accentEnd: Color
        @Composable get() = end(AppTheme.palette)

    companion object {
        val Spark = CardStyle("SPARK", Icons.Rounded.AutoAwesome, { it.violet }, { it.pink })
        val Quiz = CardStyle("QUICK QUIZ", Icons.Rounded.Quiz, { it.blue }, { it.cyan })
        val Flashcard = CardStyle("FLASHCARD", Icons.Rounded.Style, { it.teal }, { it.sky })
        val Review = CardStyle("REVIEW", Icons.Rounded.Replay, { it.amber }, { it.orange })
        val Summary = CardStyle("RUN COMPLETE", Icons.Rounded.EmojiEvents, { it.violet }, { it.teal })
        val Neutral = CardStyle("", Icons.Rounded.Layers, { it.sky }, { it.violet })

        fun forType(type: ContentType): CardStyle = when (type) {
            ContentType.MCQ -> Quiz
            ContentType.QA -> Flashcard
            ContentType.FUN_FACT -> Spark
            ContentType.MIX -> Neutral
        }
    }
}
