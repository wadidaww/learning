package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.TopicRepository
import javax.inject.Inject

class CreateTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        defaultContentType: ContentType = ContentType.MCQ
    ): Result<Long> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Topic name cannot be empty"))
            }
            val topic = Topic(
                name = name.trim(),
                description = description.trim(),
                defaultContentType = defaultContentType
            )
            val id = topicRepository.insertTopic(topic)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
