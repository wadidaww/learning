package com.kresna.aitopicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM generated_content WHERE topicId = :topicId AND contentType = :contentType ORDER BY createdAt DESC")
    fun getContentByTopicAndType(topicId: Long, contentType: ContentType): Flow<List<ContentEntity>>

    @Query("SELECT * FROM generated_content WHERE topicId = :topicId ORDER BY createdAt DESC")
    fun getAllContentForTopic(topicId: Long): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(content: List<ContentEntity>)

    @Query("UPDATE generated_content SET isViewed = 1 WHERE id = :contentId")
    suspend fun markAsViewed(contentId: Long)

    @Query("SELECT * FROM generated_content WHERE topicId = :topicId ORDER BY createdAt DESC")
    fun getContentByTopicForMix(topicId: Long): Flow<List<ContentEntity>>

    @Query("SELECT question FROM generated_content WHERE topicId = :topicId AND contentType = :contentType")
    suspend fun getExistingQuestions(topicId: Long, contentType: ContentType): List<String>

    @Query("SELECT question FROM generated_content WHERE topicId = :topicId")
    suspend fun getExistingQuestionsForMix(topicId: Long): List<String>
}
