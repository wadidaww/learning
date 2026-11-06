package com.kresna.aitopicapp.data.repository

import android.content.Context
import androidx.core.content.edit
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.di.IoDispatcher
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.DailyCandidate
import com.kresna.aitopicapp.domain.model.DailyCard
import com.kresna.aitopicapp.domain.model.DailyPick
import com.kresna.aitopicapp.domain.repository.DailyCardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Today's pick lives in `shared_prefs/daily_card.xml`; delete it to force a new pick. */
@Singleton
class DailyCardRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val contentDao: ContentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DailyCardRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getCandidates(): List<DailyCandidate> = withContext(ioDispatcher) {
        contentDao.getDailyCandidates().map { DailyCandidate(it.id, it.topicId, it.isViewed) }
    }

    override suspend fun getCard(contentId: Long): DailyCard? = withContext(ioDispatcher) {
        contentDao.getDailyCard(contentId)?.let { row ->
            DailyCard(
                contentId = row.id,
                topicId = row.topicId,
                topicName = row.topicName,
                contentType = row.contentType,
                text = if (row.contentType == ContentType.FUN_FACT) row.answer.orEmpty() else row.question
            )
        }
    }

    override suspend fun getSavedPick(): DailyPick? = withContext(ioDispatcher) {
        if (!prefs.contains(KEY_DAY) || !prefs.contains(KEY_CONTENT_ID)) {
            null
        } else {
            DailyPick(prefs.getLong(KEY_DAY, 0), prefs.getLong(KEY_CONTENT_ID, 0))
        }
    }

    override suspend fun savePick(pick: DailyPick) = withContext(ioDispatcher) {
        prefs.edit {
            putLong(KEY_DAY, pick.epochDay)
            putLong(KEY_CONTENT_ID, pick.contentId)
        }
    }

    private companion object {
        const val PREFS_NAME = "daily_card"
        const val KEY_DAY = "epoch_day"
        const val KEY_CONTENT_ID = "content_id"
    }
}
