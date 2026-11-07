package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.DailyCandidate
import com.kresna.aitopicapp.domain.model.DailyPick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCardPickerTest {

    private fun card(id: Long, topic: Long, viewed: Boolean = false) = DailyCandidate(id, topic, viewed)

    private val twoTopics = listOf(
        card(1, topic = 10), card(2, topic = 10), card(3, topic = 10),
        card(4, topic = 20), card(5, topic = 20)
    )

    @Test
    fun `no content gives no card`() {
        assertNull(DailyCardPicker.choose(emptyList(), epochDay = 100, saved = null))
    }

    @Test
    fun `keeps the saved card for the rest of its day`() {
        val pick = DailyCardPicker.choose(twoTopics, epochDay = 100, saved = DailyPick(100, 5))
        assertEquals(5L, pick?.contentId)
    }

    @Test
    fun `picks again when the saved card was deleted`() {
        val pick = DailyCardPicker.choose(twoTopics, epochDay = 100, saved = DailyPick(100, 99))
        assertTrue(pick in twoTopics)
    }

    @Test
    fun `same inputs give the same card`() {
        val first = DailyCardPicker.choose(twoTopics, epochDay = 200, saved = null)
        val second = DailyCardPicker.choose(twoTopics.shuffled(), epochDay = 200, saved = null)
        assertEquals(first, second)
    }

    @Test
    fun `rotates through topics day by day`() {
        val topics = (300L..303L).map { day -> DailyCardPicker.choose(twoTopics, day, saved = null)!!.topicId }
        assertEquals(listOf(10L, 20L, 10L, 20L), topics)
    }

    @Test
    fun `prefers cards the user has not seen`() {
        val content = listOf(card(1, 10, viewed = true), card(2, 10, viewed = true), card(3, 10), card(4, 10, viewed = true))
        repeat(20) { day ->
            assertEquals(3L, DailyCardPicker.choose(content, day.toLong(), saved = null)?.contentId)
        }
    }

    @Test
    fun `does not repeat yesterday's card when there is another`() {
        val content = listOf(card(1, 10), card(2, 10))
        repeat(20) { day ->
            val pick = DailyCardPicker.choose(content, day.toLong() + 1, saved = DailyPick(day.toLong(), 1))
            assertNotEquals(1L, pick?.contentId)
        }
    }

    @Test
    fun `falls back to a seen card, then to the only card`() {
        val allSeen = listOf(card(1, 10, viewed = true), card(2, 10, viewed = true))
        assertEquals(2L, DailyCardPicker.choose(allSeen, 5, saved = DailyPick(4, 1))?.contentId)

        val single = listOf(card(7, 10, viewed = true))
        assertEquals(7L, DailyCardPicker.choose(single, 5, saved = DailyPick(4, 7))?.contentId)
    }

    @Test
    fun `negative days still pick a topic`() {
        assertTrue(DailyCardPicker.choose(twoTopics, epochDay = -3, saved = null) in twoTopics)
    }
}
