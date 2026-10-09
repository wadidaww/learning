package com.kresna.aitopicapp.domain.usecase

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.util.Constants
import javax.inject.Inject

class GenerateContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(
        topic: Topic,
        contentType: ContentType,
        count: Int = Constants.DEFAULT_BATCH_SIZE
    ): Result<List<GeneratedContent>> {
        return contentRepository.generateContent(
            topic = topic,
            contentType = contentType,
            count = count
        )
    }
}
