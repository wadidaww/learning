package com.kresna.aitopicapp.ui.widget

import kotlin.math.floor

/**
 * How many lines of card text fit in a widget of a given height, so the text ends with an
 * ellipsis instead of a half-clipped line. Keep the numbers in sync with
 * `res/layout/widget_daily_card.xml`.
 */
object WidgetSizing {
    /** Used when the launcher doesn't report a size. */
    const val DEFAULT_BODY_LINES = 3
    private const val MAX_BODY_LINES = 8

    /** Vertical padding (12 + 12), header row (24), body top margin (6), hint top margin (4) and a little slack. */
    private const val FIXED_DP = 12f + 12f + 24f + 6f + 4f + 4f

    /** Rendered line height of a TextView is about 1.17 × the text size for the system font. */
    private const val FONT_LINE_FACTOR = 1.17f
    private const val HINT_TEXT_SP = 13f
    private const val BODY_TEXT_SP = 16f
    private const val BODY_LINE_SPACING = 1.15f

    fun bodyMaxLines(heightDp: Float, fontScale: Float = 1f): Int {
        if (heightDp <= 0f) return DEFAULT_BODY_LINES
        val chrome = FIXED_DP + HINT_TEXT_SP * FONT_LINE_FACTOR * fontScale
        val line = BODY_TEXT_SP * FONT_LINE_FACTOR * BODY_LINE_SPACING * fontScale
        return floor((heightDp - chrome) / line).toInt().coerceIn(1, MAX_BODY_LINES)
    }
}
