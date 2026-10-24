package com.kresna.aitopicapp.domain.repository

import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicProgress
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    /** Content ordered oldest first; [ContentType.MIX] returns every type. */
    fun observeContent(topicId: Long, contentType: ContentType, filter: ContentFilter): Flow<List<GeneratedContent>>

    fun observeProgress(topicId: Long): Flow<TopicProgress>

    suspend fun markAsViewed(contentId: Long)

    suspend fun setUserAnswer(contentId: Long, answerIndex: Int?)

    suspend fun setBookmarked(contentId: Long, bookmarked: Boolean)

    /**
     * Generates new content for [topic], stores it after the existing content and returns it.
     * Fails with a [com.kresna.aitopicapp.domain.model.GenerationException] when nothing new
     * could be generated; it never succeeds with an empty list.
     */
    suspend fun generateContent(
        topic: Topic,
        contentType: ContentType,
        count: Int
    ): Result<List<GeneratedContent>>

    suspend fun hasContentForTopic(topicId: Long): Boolean

    suspend fun getLastContentTypeForTopic(topicId: Long): ContentType?
}
