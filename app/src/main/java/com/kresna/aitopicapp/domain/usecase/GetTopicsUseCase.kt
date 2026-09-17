package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(): Flow<List<Topic>> = topicRepository.getAllTopics()

    suspend fun getById(topicId: Long): Topic? = topicRepository.getTopicById(topicId)

    fun getContentCount(topicId: Long): Flow<Int> = topicRepository.getContentCount(topicId)
}
