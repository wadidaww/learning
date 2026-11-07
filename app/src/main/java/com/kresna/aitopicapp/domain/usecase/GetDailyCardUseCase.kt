package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.DailyCard
import com.kresna.aitopicapp.domain.model.DailyPick
import com.kresna.aitopicapp.domain.repository.DailyCardRepository
import com.kresna.aitopicapp.util.DailyCardPicker
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import javax.inject.Inject

/** Today's card for the home-screen widget, or null when no topic has content yet. */
class GetDailyCardUseCase @Inject constructor(
    private val repository: DailyCardRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Result<DailyCard?> = try {
        val day = today.toEpochDay()
        val saved = repository.getSavedPick()
        val pick = DailyCardPicker.choose(repository.getCandidates(), day, saved)
        if (pick != null && saved != DailyPick(day, pick.contentId)) {
            repository.savePick(DailyPick(day, pick.contentId))
        }
        Result.success(pick?.let { repository.getCard(it.contentId) })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
