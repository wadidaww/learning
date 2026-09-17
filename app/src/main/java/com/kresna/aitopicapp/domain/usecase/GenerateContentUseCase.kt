package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.repository.ContentRepository
import javax.inject.Inject

class GenerateContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(
        topicId: Long,
        topicName: String,
        topicDescription: String,
        contentType: ContentType,
        count: Int
    ): Result<List<GeneratedContent>> {
        return contentRepository.generateContent(
            topicId = topicId,
            topicName = topicName,
            topicDescription = topicDescription,
            contentType = contentType,
            count = count
        )
    }
}
