package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicOverview
import com.kresna.aitopicapp.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(): Flow<List<Topic>> = topicRepository.getAllTopics()

    fun withProgress(): Flow<List<TopicOverview>> = topicRepository.observeTopicsWithProgress()

    fun observe(topicId: Long): Flow<Topic?> = topicRepository.observeTopic(topicId)

    suspend fun getById(topicId: Long): Topic? = topicRepository.getTopicById(topicId)
}
