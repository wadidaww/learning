package com.kresna.aitopicapp.domain.repository

import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicOverview
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    fun getAllTopics(): Flow<List<Topic>>
    fun observeTopicsWithProgress(): Flow<List<TopicOverview>>
    fun observeTopic(topicId: Long): Flow<Topic?>
    suspend fun getTopicById(topicId: Long): Topic?
    suspend fun insertTopic(topic: Topic): Long
    suspend fun updateTopic(topic: Topic)

    /** Deletes the topic together with all of its generated content. */
    suspend fun deleteTopic(topic: Topic)
}
