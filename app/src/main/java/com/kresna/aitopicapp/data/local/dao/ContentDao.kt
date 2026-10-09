package com.kresna.aitopicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

/**
 * All lists are ordered oldest first, so newly generated content is appended at the end.
 * `id` breaks ties between items of the same batch.
 */
@Dao
interface ContentDao {
    @Query("SELECT * FROM generated_content WHERE topicId = :topicId AND contentType = :contentType ORDER BY createdAt ASC, id ASC")
    fun observeByTopicAndType(topicId: Long, contentType: ContentType): Flow<List<ContentEntity>>

    @Query("SELECT * FROM generated_content WHERE topicId = :topicId ORDER BY createdAt ASC, id ASC")
    fun observeAllForTopic(topicId: Long): Flow<List<ContentEntity>>

    @Query("SELECT * FROM generated_content WHERE topicId = :topicId AND isBookmarked = 1 ORDER BY createdAt ASC, id ASC")
    fun observeBookmarked(topicId: Long): Flow<List<ContentEntity>>

    @Query(
        "SELECT * FROM generated_content WHERE topicId = :topicId AND contentType = 'MCQ' " +
            "AND userAnswerIndex IS NOT NULL AND userAnswerIndex != correctIndex ORDER BY createdAt ASC, id ASC"
    )
    fun observeMissed(topicId: Long): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(content: List<ContentEntity>): List<Long>

    @Query("UPDATE generated_content SET isViewed = 1 WHERE id = :contentId AND isViewed = 0")
    suspend fun markAsViewed(contentId: Long)

    @Query("UPDATE generated_content SET userAnswerIndex = :answerIndex WHERE id = :contentId")
    suspend fun setUserAnswer(contentId: Long, answerIndex: Int?)

    @Query("UPDATE generated_content SET isBookmarked = :bookmarked WHERE id = :contentId")
    suspend fun setBookmarked(contentId: Long, bookmarked: Boolean)

    /** Text used for duplicate detection: the fact for fun facts, the question otherwise. Oldest first. */
    @Query(
        "SELECT CASE WHEN contentType = 'FUN_FACT' THEN COALESCE(answer, '') ELSE question END " +
            "FROM generated_content WHERE topicId = :topicId ORDER BY createdAt ASC, id ASC"
    )
    suspend fun getDedupeTexts(topicId: Long): List<String>

    @Query(
        "SELECT CASE WHEN contentType = 'FUN_FACT' THEN COALESCE(answer, '') ELSE question END " +
            "FROM generated_content WHERE topicId = :topicId AND contentType = :contentType ORDER BY createdAt ASC, id ASC"
    )
    suspend fun getDedupeTextsByType(topicId: Long, contentType: ContentType): List<String>

    @Query("SELECT MAX(createdAt) FROM generated_content WHERE topicId = :topicId")
    suspend fun getLatestCreatedAt(topicId: Long): Long?

    @Query("SELECT COUNT(*) > 0 FROM generated_content WHERE topicId = :topicId")
    suspend fun hasContentForTopic(topicId: Long): Boolean

    @Query("SELECT contentType FROM generated_content WHERE topicId = :topicId ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun getLastContentTypeForTopic(topicId: Long): ContentType?

    @Query("DELETE FROM generated_content WHERE topicId = :topicId")
    suspend fun deleteForTopic(topicId: Long)

    @Query(
        "SELECT COUNT(*) AS total, " +
            "COALESCE(SUM(isViewed), 0) AS viewed, " +
            "COALESCE(SUM(CASE WHEN userAnswerIndex IS NOT NULL THEN 1 ELSE 0 END), 0) AS answered, " +
            "COALESCE(SUM(CASE WHEN userAnswerIndex IS NOT NULL AND userAnswerIndex = correctIndex THEN 1 ELSE 0 END), 0) AS correct, " +
            "COALESCE(SUM(isBookmarked), 0) AS bookmarked " +
            "FROM generated_content WHERE topicId = :topicId"
    )
    fun observeProgress(topicId: Long): Flow<ProgressRow>
}

data class ProgressRow(
    val total: Int,
    val viewed: Int,
    val answered: Int,
    val correct: Int,
    val bookmarked: Int
)
