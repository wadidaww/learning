package com.kresna.aitopicapp.data.repository

import androidx.room.withTransaction
import com.kresna.aitopicapp.data.local.AppDatabase
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.dao.TopicDao
import com.kresna.aitopicapp.data.local.entity.TopicEntity
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicOverview
import com.kresna.aitopicapp.domain.model.TopicProgress
import com.kresna.aitopicapp.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val topicDao: TopicDao,
    private val contentDao: ContentDao
) : TopicRepository {

    override fun getAllTopics(): Flow<List<Topic>> {
        return topicDao.getAllTopics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTopicsWithProgress(): Flow<List<TopicOverview>> {
        return topicDao.observeTopicsWithProgress().map { rows ->
            rows.map { row ->
                TopicOverview(
                    topic = row.topic.toDomain(),
                    progress = TopicProgress(
                        total = row.total,
                        viewed = row.viewed,
                        answered = row.answered,
                        correct = row.correct,
                        bookmarked = row.bookmarked
                    )
                )
            }
        }
    }

    override fun observeTopic(topicId: Long): Flow<Topic?> {
        return topicDao.observeTopic(topicId).map { it?.toDomain() }
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
        database.withTransaction {
            contentDao.deleteForTopic(topic.id)
            topicDao.deleteTopic(TopicEntity.fromDomain(topic))
        }
    }
}
