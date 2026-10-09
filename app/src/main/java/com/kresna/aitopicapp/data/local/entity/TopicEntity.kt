package com.kresna.aitopicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty
import com.kresna.aitopicapp.domain.model.Topic

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val defaultContentType: String? = null,
    val difficulty: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Topic(
        id = id,
        name = name,
        description = description,
        defaultContentType = ContentType.fromString(defaultContentType),
        difficulty = Difficulty.fromString(difficulty),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(topic: Topic) = TopicEntity(
            id = topic.id,
            name = topic.name,
            description = topic.description,
            defaultContentType = topic.defaultContentType.name,
            difficulty = topic.difficulty.name,
            createdAt = topic.createdAt
        )
    }
}
