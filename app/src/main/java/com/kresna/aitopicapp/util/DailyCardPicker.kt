package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.DailyCandidate
import com.kresna.aitopicapp.domain.model.DailyPick
import kotlin.random.Random

object DailyCardPicker {

    /**
     * Card to show on [epochDay].
     *
     * Keeps [saved] for the whole day it was picked for (as long as the card still exists), so the
     * widget doesn't change when it refreshes or when the user opens the card. On a new day it
     * rotates through the topics, one topic per day, and picks an unviewed card of that topic,
     * avoiding yesterday's card. The pick depends only on the inputs, so it is reproducible.
     */
    fun choose(candidates: List<DailyCandidate>, epochDay: Long, saved: DailyPick?): DailyCandidate? {
        if (candidates.isEmpty()) return null
        if (saved != null && saved.epochDay == epochDay) {
            candidates.firstOrNull { it.contentId == saved.contentId }?.let { return it }
        }

        val topics = candidates.map { it.topicId }.distinct().sorted()
        val topicId = topics[Math.floorMod(epochDay, topics.size.toLong()).toInt()]
        val inTopic = candidates.filter { it.topicId == topicId }.sortedBy { it.contentId }

        val notPrevious = inTopic.filter { it.contentId != saved?.contentId }
        val pool = notPrevious.filter { !it.isViewed }
            .ifEmpty { notPrevious }
            .ifEmpty { inTopic }
        return pool[Random(epochDay * 31 + topicId).nextInt(pool.size)]
    }
}
