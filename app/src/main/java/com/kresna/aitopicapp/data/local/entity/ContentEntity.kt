package com.kresna.aitopicapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent

@Entity(
    tableName = "generated_content",
    indices = [Index(value = ["topicId"])]
)
data class ContentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: Long,
    val contentType: ContentType,
    val question: String,
    val answer: String? = null,
    val options: List<String>? = null,
    val correctIndex: Int? = null,
    val explanation: String? = null,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val isViewed: Boolean = false,
    val userAnswerIndex: Int? = null,
    @ColumnInfo(defaultValue = "0")
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = GeneratedContent(
        id = id,
        topicId = topicId,
        contentType = contentType,
        question = question,
        answer = answer,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation,
        codeSnippet = codeSnippet,
        codeLanguage = codeLanguage,
        isViewed = isViewed,
        userAnswerIndex = userAnswerIndex,
        isBookmarked = isBookmarked,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(content: GeneratedContent) = ContentEntity(
            id = content.id,
            topicId = content.topicId,
            contentType = content.contentType,
            question = content.question,
            answer = content.answer,
            options = content.options,
            correctIndex = content.correctIndex,
            explanation = content.explanation,
            codeSnippet = content.codeSnippet,
            codeLanguage = content.codeLanguage,
            isViewed = content.isViewed,
            userAnswerIndex = content.userAnswerIndex,
            isBookmarked = content.isBookmarked,
            createdAt = content.createdAt
        )
    }
}
