package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedLayoutTest {

    private fun items(count: Int) = (0 until count).map { i ->
        GeneratedContent(id = i.toLong(), topicId = 1, contentType = ContentType.QA, question = "q$i")
    }

    private fun mcq(id: Long, answer: Int?) = GeneratedContent(
        id = id, topicId = 1, contentType = ContentType.MCQ, question = "q$id",
        options = listOf("a", "b"), correctIndex = 0, userAnswerIndex = answer
    )

    @Test
    fun `a summary follows each full run and the status page comes last`() {
        val pages = FeedLayout.build(items(23), withRunSummaries = true, withStatusPage = true, runSize = 10)
        // 23 cards + 2 summaries (runs 1 and 2 are full, run 3 is not) + status
        assertEquals(26, pages.size)
        assertTrue(pages[10] is FeedPage.RunSummary)
        assertTrue(pages[21] is FeedPage.RunSummary)
        assertTrue(pages.last() is FeedPage.Status)
        assertEquals(2, (pages[21] as FeedPage.RunSummary).stats.runNumber)
    }

    @Test
    fun `review layout has no summaries and no status page`() {
        val pages = FeedLayout.build(items(12), withRunSummaries = false, withStatusPage = false, runSize = 10)
        assertEquals(12, pages.size)
        assertTrue(pages.all { it is FeedPage.Card })
    }

    @Test
    fun `pageOf skips the summary pages before the card`() {
        val pages = FeedLayout.build(items(25), withRunSummaries = true, withStatusPage = true, runSize = 10)
        listOf(0, 9, 10, 19, 20, 24).forEach { index ->
            val page = pages[FeedLayout.pageOf(index, withRunSummaries = true, runSize = 10)]
            assertEquals(index, (page as FeedPage.Card).contentIndex)
        }
    }

    @Test
    fun `cards are numbered within their run`() {
        val pages = FeedLayout.build(items(12), withRunSummaries = true, withStatusPage = false, runSize = 10)
        val card = pages[11] as FeedPage.Card
        assertEquals(10, card.contentIndex)
        assertEquals(1, card.runIndex)
        assertEquals(0, card.positionInRun)
    }

    @Test
    fun `cardsBefore counts only cards`() {
        val pages = FeedLayout.build(items(12), withRunSummaries = true, withStatusPage = true, runSize = 10)
        assertEquals(10, FeedLayout.cardsBefore(pages, 11))
        assertEquals(12, FeedLayout.cardsBefore(pages, pages.size))
    }

    @Test
    fun `run stats count answers and the best correct streak`() {
        val run = listOf(mcq(1, 0), mcq(2, 0), mcq(3, 1), mcq(4, 0), mcq(5, null), mcq(6, 0), mcq(7, 0))
        val stats = FeedLayout.runStats(0, run)
        assertEquals(6, stats.answered)
        assertEquals(5, stats.correct)
        // 4, (5 unanswered does not break it), 6, 7
        assertEquals(3, stats.bestStreak)
        assertEquals(83, stats.accuracyPercent)
    }

    @Test
    fun `accuracy is null without answers`() = assertNull(FeedLayout.runStats(0, items(3)).accuracyPercent)
}
