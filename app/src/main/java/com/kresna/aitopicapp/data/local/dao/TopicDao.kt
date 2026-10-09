package com.kresna.aitopicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kresna.aitopicapp.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    /** Topics with their learning progress, computed in one query instead of one per row. */
    @Query(
        "SELECT t.*, " +
            "COUNT(c.id) AS total, " +
            "COALESCE(SUM(c.isViewed), 0) AS viewed, " +
            "COALESCE(SUM(CASE WHEN c.userAnswerIndex IS NOT NULL THEN 1 ELSE 0 END), 0) AS answered, " +
            "COALESCE(SUM(CASE WHEN c.userAnswerIndex IS NOT NULL AND c.userAnswerIndex = c.correctIndex THEN 1 ELSE 0 END), 0) AS correct, " +
            "COALESCE(SUM(c.isBookmarked), 0) AS bookmarked " +
            "FROM topics t LEFT JOIN generated_content c ON c.topicId = t.id " +
            "GROUP BY t.id ORDER BY t.createdAt DESC"
    )
    fun observeTopicsWithProgress(): Flow<List<TopicWithProgressRow>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Long): TopicEntity?

    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun observeTopic(topicId: Long): Flow<TopicEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)
}

data class TopicWithProgressRow(
    @Embedded val topic: TopicEntity,
    val total: Int,
    val viewed: Int,
    val answered: Int,
    val correct: Int,
    val bookmarked: Int
)
