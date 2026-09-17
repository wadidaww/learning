package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.TopicRepository
import javax.inject.Inject

class DeleteTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(topic: Topic): Result<Unit> {
        return try {
            topicRepository.deleteTopic(topic)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
