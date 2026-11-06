package com.kresna.aitopicapp.domain.repository

import com.kresna.aitopicapp.domain.model.DailyCandidate
import com.kresna.aitopicapp.domain.model.DailyCard
import com.kresna.aitopicapp.domain.model.DailyPick

/** Data for the home-screen daily card widget. */
interface DailyCardRepository {
    /** Every generated item that still belongs to a topic. */
    suspend fun getCandidates(): List<DailyCandidate>

    suspend fun getCard(contentId: Long): DailyCard?

    suspend fun getSavedPick(): DailyPick?

    suspend fun savePick(pick: DailyPick)
}
