package com.kresna.aitopicapp.data.repository

import com.kresna.aitopicapp.data.local.dao.TopicDao
import com.kresna.aitopicapp.data.local.entity.TopicEntity
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepositoryImpl @Inject constructor(
    private val topicDao: TopicDao
) : TopicRepository {

    override fun getAllTopics(): Flow<List<Topic>> {
        return topicDao.getAllTopics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTopicById(topicId: Long): Topic? {
        return topicDao.getTopicById(topicId)?.toDomain()
    }

    override suspend fun insertTopic(topic: Topic): Long {
        return topicDao.insertTopic(TopicEntity.fromDomain(topic))
    }

    override suspend fun updateTopic(topic: Topic) {
        topicDao.updateTopic(TopicEntity.fromDomain(topic))
    }

    override suspend fun deleteTopic(topic: Topic) {
        topicDao.deleteTopic(TopicEntity.fromDomain(topic))
    }

    override fun getContentCount(topicId: Long): Flow<Int> {
        return topicDao.getContentCount(topicId)
    }
}
