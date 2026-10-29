package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent

/** One full-screen page of the learning feed. */
sealed interface FeedPage {
    /** A learning card; [positionInRun] is 0-based within run [runIndex]. */
    data class Card(
        val content: GeneratedContent,
        val contentIndex: Int,
        val runIndex: Int,
        val positionInRun: Int
    ) : FeedPage

    /** Shown after every complete run: the natural "keep going or stop here" point. */
    data class RunSummary(val stats: RunStats) : FeedPage

    /** Trailing page that shows generation progress, errors, or "get more". */
    data object Status : FeedPage
}

data class RunStats(
    /** 1-based, for display. */
    val runNumber: Int,
    val cardCount: Int,
    val viewed: Int,
    val answered: Int,
    val correct: Int,
    /** Longest run of consecutive correct quiz answers, in card order. */
    val bestStreak: Int
) {
    val accuracyPercent: Int? get() = if (answered == 0) null else (correct * 100) / answered
}

/**
 * Splits content into runs of [Constants.FEED_RUN_SIZE] cards, each followed by a summary page,
 * so the infinite feed has a finish line every few minutes.
 */
object FeedLayout {

    fun build(
        content: List<GeneratedContent>,
        withRunSummaries: Boolean,
        withStatusPage: Boolean,
        runSize: Int = Constants.FEED_RUN_SIZE
    ): List<FeedPage> {
        val pages = ArrayList<FeedPage>(content.size + content.size / runSize + 1)
        content.chunked(runSize).forEachIndexed { runIndex, run ->
            run.forEachIndexed { position, item ->
                pages += FeedPage.Card(item, runIndex * runSize + position, runIndex, position)
            }
            if (withRunSummaries && run.size == runSize) {
                pages += FeedPage.RunSummary(runStats(runIndex, run))
            }
        }
        if (withStatusPage) pages += FeedPage.Status
        return pages
    }

    /** Page index of the card at [contentIndex] in a layout built with the same flags. */
    fun pageOf(contentIndex: Int, withRunSummaries: Boolean, runSize: Int = Constants.FEED_RUN_SIZE): Int =
        if (withRunSummaries) contentIndex + contentIndex / runSize else contentIndex

    /** Number of content cards on pages before [page], used to decide when to prefetch. */
    fun cardsBefore(pages: List<FeedPage>, page: Int): Int =
        pages.take(page.coerceIn(0, pages.size)).count { it is FeedPage.Card }

    fun runStats(runIndex: Int, run: List<GeneratedContent>): RunStats {
        var best = 0
        var current = 0
        run.filter { it.contentType == ContentType.MCQ && it.isAnswered }.forEach { item ->
            current = if (item.isAnsweredCorrectly) current + 1 else 0
            best = maxOf(best, current)
        }
        return RunStats(
            runNumber = runIndex + 1,
            cardCount = run.size,
            viewed = run.count { it.isViewed },
            answered = run.count { it.isAnswered },
            correct = run.count { it.isAnsweredCorrectly },
            bestStreak = best
        )
    }
}
