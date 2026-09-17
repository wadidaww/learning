package com.kresna.aitopicapp.domain.repository

import com.kresna.aitopicapp.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    fun getAllTopics(): Flow<List<Topic>>
    suspend fun getTopicById(topicId: Long): Topic?
    suspend fun insertTopic(topic: Topic): Long
    suspend fun updateTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)
    fun getContentCount(topicId: Long): Flow<Int>
}
