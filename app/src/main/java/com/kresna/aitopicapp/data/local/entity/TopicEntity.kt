package com.kresna.aitopicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kresna.aitopicapp.domain.model.Topic

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Topic(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(topic: Topic) = TopicEntity(
            id = topic.id,
            name = topic.name,
            description = topic.description,
            createdAt = topic.createdAt
        )
    }
}
