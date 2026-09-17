package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.TopicRepository
import javax.inject.Inject

class UpdateTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(topic: Topic): Result<Unit> {
        return try {
            if (topic.name.isBlank()) {
                return Result.failure(IllegalArgumentException("Topic name cannot be empty"))
            }
            topicRepository.updateTopic(topic.copy(name = topic.name.trim(), description = topic.description.trim()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
