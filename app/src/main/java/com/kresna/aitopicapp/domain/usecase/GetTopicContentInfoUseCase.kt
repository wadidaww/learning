package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.repository.ContentRepository
import javax.inject.Inject

data class TopicContentInfo(
    val hasContent: Boolean,
    val lastContentType: ContentType?
)

class GetTopicContentInfoUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(topicId: Long): TopicContentInfo {
        val hasContent = contentRepository.hasContentForTopic(topicId)
        val lastContentType = if (hasContent) {
            contentRepository.getLastContentTypeForTopic(topicId)
        } else {
            null
        }
        return TopicContentInfo(hasContent, lastContentType)
    }
}
