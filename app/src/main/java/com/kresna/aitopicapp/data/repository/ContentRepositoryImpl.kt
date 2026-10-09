package com.kresna.aitopicapp.data.repository

import android.util.Log
import com.google.gson.Gson
import com.kresna.aitopicapp.data.local.AppSettings
import com.kresna.aitopicapp.data.local.SettingsDataStore
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.data.remote.BytePlusApi
import com.kresna.aitopicapp.data.remote.BytePlusErrorResponse
import com.kresna.aitopicapp.data.remote.BytePlusRequest
import com.kresna.aitopicapp.data.remote.ChatMessage
import com.kresna.aitopicapp.di.IoDispatcher
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.model.GenerationException
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicProgress
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.util.Constants
import com.kresna.aitopicapp.util.ContentDeduplicator
import com.kresna.aitopicapp.util.ContentParser
import com.kresna.aitopicapp.util.PromptBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InterruptedIOException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ContentRepository"

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val settingsDataStore: SettingsDataStore,
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentRepository {

    private val gson = Gson()
    private val apisByBaseUrl = ConcurrentHashMap<String, BytePlusApi>()

    /**
     * Serialises generation so two callers (e.g. the topic screen and the pager prefetch)
     * cannot generate at the same time and insert duplicates of each other.
     */
    private val generationMutex = Mutex()

    override fun observeContent(
        topicId: Long,
        contentType: ContentType,
        filter: ContentFilter
    ): Flow<List<GeneratedContent>> {
        val entities = when (filter) {
            ContentFilter.BOOKMARKED -> contentDao.observeBookmarked(topicId)
            ContentFilter.MISSED -> contentDao.observeMissed(topicId)
            ContentFilter.ALL -> if (contentType == ContentType.MIX) {
                contentDao.observeAllForTopic(topicId)
            } else {
                contentDao.observeByTopicAndType(topicId, contentType)
            }
        }
        return entities.map { list -> list.map { it.toDomain() } }
    }

    override fun observeProgress(topicId: Long): Flow<TopicProgress> {
        return contentDao.observeProgress(topicId).map { row ->
            TopicProgress(
                total = row.total,
                viewed = row.viewed,
                answered = row.answered,
                correct = row.correct,
                bookmarked = row.bookmarked
            )
        }
    }

    override suspend fun markAsViewed(contentId: Long) {
        contentDao.markAsViewed(contentId)
    }

    override suspend fun setUserAnswer(contentId: Long, answerIndex: Int?) {
        contentDao.setUserAnswer(contentId, answerIndex)
    }

    override suspend fun setBookmarked(contentId: Long, bookmarked: Boolean) {
        contentDao.setBookmarked(contentId, bookmarked)
    }

    override suspend fun generateContent(
        topic: Topic,
        contentType: ContentType,
        count: Int
    ): Result<List<GeneratedContent>> = withContext(ioDispatcher) {
        generationMutex.withLock {
            try {
                Result.success(generateWithRetry(topic, contentType, count))
            } catch (e: CancellationException) {
                throw e
            } catch (e: GenerationException) {
                Log.w(TAG, "Generation failed: ${e.message}", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected generation error", e)
                Result.failure(GenerationException("Something went wrong: ${e.message ?: e.javaClass.simpleName}", isRetryable = true, cause = e))
            }
        }
    }

    private suspend fun generateWithRetry(topic: Topic, contentType: ContentType, count: Int): List<GeneratedContent> {
        val settings = settingsDataStore.getSettings()
        if (settings.apiKey.isBlank()) {
            throw GenerationException("API key not configured. Please set it in Settings.", isRetryable = false)
        }

        var lastError: GenerationException? = null
        for (attempt in 0 until Constants.MAX_GENERATION_ATTEMPTS) {
            if (attempt > 0) delay(Constants.RETRY_BASE_DELAY_MS * attempt)
            try {
                return generateOnce(settings, topic, contentType, count)
            } catch (e: GenerationException) {
                Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")
                lastError = e
                if (!e.isRetryable) throw e
            }
        }
        throw lastError ?: GenerationException("Content generation failed.", isRetryable = true)
    }

    private suspend fun generateOnce(
        settings: AppSettings,
        topic: Topic,
        contentType: ContentType,
        count: Int
    ): List<GeneratedContent> {
        val existingTexts = if (contentType == ContentType.MIX) {
            contentDao.getDedupeTexts(topic.id)
        } else {
            contentDao.getDedupeTextsByType(topic.id, contentType)
        }

        val prompt = PromptBuilder.build(
            contentType = contentType,
            topic = topic.name,
            context = topic.description,
            difficulty = topic.difficulty,
            count = count,
            existingItems = existingTexts
        )
        val responseText = requestCompletion(settings, prompt)

        val parsed = ContentParser.parse(responseText, contentType, topic.id)
        if (parsed.isEmpty()) {
            Log.w(TAG, "Unparseable response: ${responseText.take(500)}")
            throw GenerationException(
                "The AI response could not be read as study content. Try again or pick another model in Settings.",
                isRetryable = true
            )
        }

        val fresh = ContentDeduplicator.filterNew(parsed, existingTexts)
        if (fresh.isEmpty()) {
            throw GenerationException(
                "The AI only returned content you already have. Try again, or broaden the topic description.",
                isRetryable = true
            )
        }
        return save(topic.id, fresh)
    }

    /**
     * Stores [items] after all existing content of the topic. Each item gets its own,
     * increasing timestamp so the batch keeps the order the AI produced.
     */
    private suspend fun save(topicId: Long, items: List<GeneratedContent>): List<GeneratedContent> {
        val latest = contentDao.getLatestCreatedAt(topicId) ?: 0L
        val base = maxOf(System.currentTimeMillis(), latest + 1)
        val stamped = items.mapIndexed { index, item -> item.copy(id = 0, createdAt = base + index) }
        val ids = contentDao.insertAll(stamped.map { ContentEntity.fromDomain(it) })
        return stamped.mapIndexed { index, item -> item.copy(id = ids.getOrElse(index) { 0L }) }
    }

    private suspend fun requestCompletion(settings: AppSettings, prompt: String): String {
        val request = BytePlusRequest(
            model = settings.modelId,
            messages = listOf(
                ChatMessage(role = "system", content = PromptBuilder.SYSTEM_PROMPT),
                ChatMessage(role = "user", content = prompt)
            ),
            temperature = Constants.TEMPERATURE,
            maxTokens = Constants.MAX_OUTPUT_TOKENS
        )

        val response = try {
            apiFor(settings.baseUrl).chatCompletions("Bearer ${settings.apiKey.trim()}", request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: UnknownHostException) {
            throw GenerationException("No internet connection. Check your network and try again.", isRetryable = true, cause = e)
        } catch (e: InterruptedIOException) {
            throw GenerationException("The AI took too long to respond. Try again or choose a faster model.", isRetryable = true, cause = e)
        } catch (e: IOException) {
            throw GenerationException("Network error: ${e.message ?: "connection failed"}.", isRetryable = true, cause = e)
        } catch (e: Exception) {
            throw GenerationException("Unexpected response from the AI service.", isRetryable = true, cause = e)
        }

        if (!response.isSuccessful) {
            throw httpError(response.code(), response.errorBody()?.string(), settings.modelId)
        }

        val choice = response.body()?.choices?.firstOrNull()
        val content = choice?.message?.content
        if (content.isNullOrBlank()) {
            throw when (choice?.finishReason) {
                "length" -> GenerationException(
                    "The AI ran out of output tokens before answering. Try again or choose a non-reasoning model.",
                    isRetryable = true
                )
                "content_filter" -> GenerationException(
                    "The AI provider blocked this request. Try rewording the topic.",
                    isRetryable = false
                )
                else -> GenerationException("The AI returned an empty response.", isRetryable = true)
            }
        }
        return content
    }

    private fun httpError(code: Int, errorBody: String?, modelId: String): GenerationException {
        val serverMessage = parseErrorMessage(errorBody)
        val detail = serverMessage?.let { " ($it)" }.orEmpty()
        return when (code) {
            401, 403 -> GenerationException("Your API key was rejected$detail. Check it in Settings.", isRetryable = false)
            404 -> GenerationException(
                "Model \"$modelId\" was not found$detail. Make sure it is activated for your account and region.",
                isRetryable = false
            )
            408 -> GenerationException("The request timed out$detail.", isRetryable = true)
            429 -> GenerationException("Rate limit or quota reached$detail. Wait a moment and try again.", isRetryable = true)
            in 500..599 -> GenerationException("The AI service is temporarily unavailable (HTTP $code)$detail.", isRetryable = true)
            else -> GenerationException(serverMessage ?: "API error: HTTP $code", isRetryable = false)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            gson.fromJson(errorBody, BytePlusErrorResponse::class.java)?.error?.message?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun apiFor(baseUrl: String): BytePlusApi = apisByBaseUrl.getOrPut(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BytePlusApi::class.java)
    }

    override suspend fun hasContentForTopic(topicId: Long): Boolean {
        return contentDao.hasContentForTopic(topicId)
    }

    override suspend fun getLastContentTypeForTopic(topicId: Long): ContentType? {
        return contentDao.getLastContentTypeForTopic(topicId)
    }
}
