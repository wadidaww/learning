package com.kresna.aitopicapp.ui.screens.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.model.GenerationException
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.domain.usecase.GenerateContentUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.ui.navigation.NavArgs
import com.kresna.aitopicapp.util.Constants
import com.kresna.aitopicapp.util.LearningPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GenerationStatus {
    data object Idle : GenerationStatus
    data object Running : GenerationStatus
    data class Failed(val message: String, val isRetryable: Boolean) : GenerationStatus
}

data class ContentPagerUiState(
    val isLoading: Boolean = true,
    val topicName: String = "",
    val contentType: ContentType = ContentType.MCQ,
    val filter: ContentFilter = ContentFilter.ALL,
    val content: List<GeneratedContent> = emptyList(),
    /** Page to jump to once the content is first shown; null when there is nothing to jump to. */
    val startPage: Int? = null,
    val startPageConsumed: Boolean = false,
    val generation: GenerationStatus = GenerationStatus.Idle,
    /** Number of items added since the screen was opened, to show a "new" hint. */
    val newItemsCount: Int = 0
) {
    /** Only the regular learning mode generates more content; review modes are fixed lists. */
    val canGenerate: Boolean get() = filter == ContentFilter.ALL
}

@HiltViewModel
class ContentPagerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val generateContentUseCase: GenerateContentUseCase
) : ViewModel() {

    private val topicId: Long = savedStateHandle.get<Long>(NavArgs.TOPIC_ID) ?: 0L
    private val contentType = ContentType.fromString(savedStateHandle.get<String>(NavArgs.CONTENT_TYPE))
    private val filter = ContentFilter.fromString(savedStateHandle.get<String>(NavArgs.FILTER))

    private val _uiState = MutableStateFlow(ContentPagerUiState(contentType = contentType, filter = filter))
    val uiState: StateFlow<ContentPagerUiState> = _uiState.asStateFlow()

    /** Ids already marked as viewed in this session, to avoid redundant database writes. */
    private val markedViewed = HashSet<Long>()

    init {
        viewModelScope.launch {
            getTopicsUseCase.observe(topicId).collect { topic ->
                _uiState.update { it.copy(topicName = topic?.name.orEmpty()) }
            }
        }
        viewModelScope.launch { observeContent() }
    }

    private suspend fun observeContent() {
        var initialCount = -1
        contentFlow().collect { list ->
            val isFirstEmission = initialCount < 0
            if (isFirstEmission) initialCount = list.size
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    content = list,
                    startPage = if (isFirstEmission && list.isNotEmpty()) startPageFor(list) else state.startPage,
                    startPageConsumed = state.startPageConsumed || (isFirstEmission && list.isEmpty()),
                    newItemsCount = (list.size - initialCount).coerceAtLeast(0)
                )
            }
            if (isFirstEmission && list.isEmpty() && filter == ContentFilter.ALL) {
                generateMore()
            }
        }
    }

    /**
     * Review modes show a snapshot of the matching items so that answering or un-bookmarking
     * an item does not make it disappear from under the user; the items themselves stay live.
     */
    private suspend fun contentFlow(): Flow<List<GeneratedContent>> {
        if (filter == ContentFilter.ALL) {
            return contentRepository.observeContent(topicId, contentType, ContentFilter.ALL)
        }
        val ids = contentRepository.observeContent(topicId, contentType, filter).first().map { it.id }.toSet()
        return contentRepository.observeContent(topicId, ContentType.MIX, ContentFilter.ALL)
            .map { all -> all.filter { it.id in ids } }
    }

    private fun startPageFor(list: List<GeneratedContent>): Int =
        if (filter == ContentFilter.ALL) LearningPosition.startIndex(list) else 0

    fun onStartPageConsumed() {
        _uiState.update { it.copy(startPageConsumed = true) }
    }

    /** Called when the pager settles on [page]; the page after the last item is the status page. */
    fun onPageSettled(page: Int) {
        val state = _uiState.value
        state.content.getOrNull(page)?.let { markAsViewed(it) }

        val nearEnd = page >= state.content.size - Constants.PREFETCH_THRESHOLD
        // Do not auto-retry after a failure; the user retries from the status page.
        if (state.canGenerate && nearEnd && state.generation == GenerationStatus.Idle) {
            generateMore()
        }
    }

    fun retryGeneration() {
        generateMore()
    }

    private fun generateMore() {
        if (!_uiState.value.canGenerate || _uiState.value.generation == GenerationStatus.Running) return
        _uiState.update { it.copy(generation = GenerationStatus.Running) }

        viewModelScope.launch {
            val topic = getTopicsUseCase.getById(topicId)
            if (topic == null) {
                _uiState.update { it.copy(generation = GenerationStatus.Failed("Topic not found.", isRetryable = false)) }
                return@launch
            }
            // New items reach the list through the database flow, appended after existing content.
            generateContentUseCase(topic, contentType)
                .onSuccess { _uiState.update { it.copy(generation = GenerationStatus.Idle) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            generation = GenerationStatus.Failed(
                                message = error.message ?: "Failed to generate content.",
                                isRetryable = (error as? GenerationException)?.isRetryable ?: true
                            )
                        )
                    }
                }
        }
    }

    private fun markAsViewed(content: GeneratedContent) {
        if (content.isViewed || !markedViewed.add(content.id)) return
        viewModelScope.launch {
            runCatching { contentRepository.markAsViewed(content.id) }
                .onFailure { markedViewed.remove(content.id) }
        }
    }

    fun answer(content: GeneratedContent, optionIndex: Int?) {
        viewModelScope.launch {
            runCatching { contentRepository.setUserAnswer(content.id, optionIndex) }
        }
    }

    fun toggleBookmark(content: GeneratedContent) {
        viewModelScope.launch {
            runCatching { contentRepository.setBookmarked(content.id, !content.isBookmarked) }
        }
    }
}
