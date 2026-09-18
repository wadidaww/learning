package com.kresna.aitopicapp.domain.repository

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getUnviewedContent(topicId: Long): Flow<List<GeneratedContent>>
    fun getAllContentForTopic(topicId: Long): Flow<List<GeneratedContent>>
    fun getContentByTopicAndType(topicId: Long, contentType: ContentType): Flow<List<GeneratedContent>>
    fun getContentCountByType(topicId: Long, contentType: ContentType): Flow<Int>
    suspend fun markAsViewed(contentId: Long)
    suspend fun resetViewedStatus(topicId: Long)
    fun getContentByTopicForMix(topicId: Long): Flow<List<GeneratedContent>>
    suspend fun generateContent(
        topicId: Long,
        topicName: String,
        topicDescription: String,
        contentType: ContentType,
        count: Int
    ): Result<List<GeneratedContent>>
}
