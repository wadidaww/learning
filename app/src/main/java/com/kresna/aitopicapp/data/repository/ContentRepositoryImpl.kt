package com.kresna.aitopicapp.data.repository

import com.kresna.aitopicapp.data.local.SettingsDataStore
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.data.remote.BytePlusApi
import com.kresna.aitopicapp.data.remote.BytePlusRequest
import com.kresna.aitopicapp.data.remote.ChatMessage
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.util.ContentParser
import com.kresna.aitopicapp.util.PromptBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val settingsDataStore: SettingsDataStore
) : ContentRepository {

    override fun getUnviewedContent(topicId: Long): Flow<List<GeneratedContent>> {
        return contentDao.getUnviewedContent(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllContentForTopic(topicId: Long): Flow<List<GeneratedContent>> {
        return contentDao.getAllContentForTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getContentByTopicAndType(topicId: Long, contentType: ContentType): Flow<List<GeneratedContent>> {
        return contentDao.getContentByTopicAndType(topicId, contentType).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getContentCountByType(topicId: Long, contentType: ContentType): Flow<Int> {
        return contentDao.getContentCountByType(topicId, contentType)
    }

    override suspend fun markAsViewed(contentId: Long) {
        contentDao.markAsViewed(contentId)
    }

    override suspend fun resetViewedStatus(topicId: Long) {
        contentDao.resetViewedStatus(topicId)
    }

    override suspend fun generateContent(
        topicId: Long,
        topicName: String,
        topicDescription: String,
        contentType: ContentType,
        count: Int
    ): Result<List<GeneratedContent>> {
        return try {
            val settings = settingsDataStore.getSettings()
            if (settings.apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured. Please set it in Settings."))
            }

            val prompt = when (contentType) {
                ContentType.MCQ -> PromptBuilder.buildMcqPrompt(topicName, topicDescription, count)
                ContentType.QA -> PromptBuilder.buildQaPrompt(topicName, topicDescription, count)
                ContentType.FUN_FACT -> PromptBuilder.buildFunFactPrompt(topicName, topicDescription, count)
            }

            val request = BytePlusRequest(
                model = settings.modelId,
                messages = listOf(
                    ChatMessage(role = "user", content = prompt)
                )
            )

            val api = createApiWithSettings(settings.apiKey, settings.baseUrl)
            val response = api.chatCompletions(request)

            val text = response.choices?.firstOrNull()?.message?.content

            if (text != null) {
                ContentParser.parseResponse(text, contentType, topicId)
                    .onSuccess { contentList ->
                        val entities = contentList.map { ContentEntity.fromDomain(it) }
                        contentDao.insertAll(entities)
                        return Result.success(contentList)
                    }
                    .onFailure { e ->
                        return Result.failure(e)
                    }
            }
            Result.failure(Exception("Empty response from AI"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createApiWithSettings(apiKey: String, baseUrl: String): BytePlusApi {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(BytePlusApi::class.java)
    }
}
