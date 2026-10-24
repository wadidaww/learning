package com.kresna.aitopicapp.ui.screens.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty
import com.kresna.aitopicapp.domain.model.GenerationException
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicProgress
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.domain.usecase.DeleteTopicUseCase
import com.kresna.aitopicapp.domain.usecase.GenerateContentUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.domain.usecase.UpdateTopicUseCase
import com.kresna.aitopicapp.ui.common.UiState
import com.kresna.aitopicapp.ui.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val generateContentUseCase: GenerateContentUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    contentRepository: ContentRepository
) : ViewModel() {

    private val topicId: Long = savedStateHandle.get<Long>(NavArgs.TOPIC_ID) ?: 0L

    private val _uiState = MutableStateFlow<UiState<Topic>>(UiState.Loading)
    val uiState: StateFlow<UiState<Topic>> = _uiState.asStateFlow()

    val progress: StateFlow<TopicProgress> = contentRepository.observeProgress(topicId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TopicProgress())

    private val _selectedContentType = MutableStateFlow(ContentType.MCQ)
    val selectedContentType: StateFlow<ContentType> = _selectedContentType.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationResult = MutableStateFlow<GenerationResult?>(null)
    val generationResult: StateFlow<GenerationResult?> = _generationResult.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _isDeleting = MutableStateFlow(false)

    private val _topicDeleted = MutableStateFlow(false)
    val topicDeleted: StateFlow<Boolean> = _topicDeleted.asStateFlow()

    init {
        loadTopic()
    }

    private fun loadTopic() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val topic = getTopicsUseCase.getById(topicId)
                if (topic != null) {
                    _selectedContentType.value = topic.defaultContentType
                    _uiState.value = UiState.Success(topic)
                } else {
                    _uiState.value = UiState.Error("Topic not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load topic")
            }
        }
    }

    private val currentTopic: Topic? get() = (_uiState.value as? UiState.Success)?.data

    fun selectContentType(type: ContentType) {
        val topic = currentTopic ?: return
        _selectedContentType.value = type
        if (topic.defaultContentType != type) {
            persist(topic.copy(defaultContentType = type), "Saved as default mode")
        }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        val topic = currentTopic ?: return
        if (topic.difficulty != difficulty) {
            persist(topic.copy(difficulty = difficulty), "Difficulty set to ${difficulty.displayName}")
        }
    }

    fun saveTopic(name: String, description: String, defaultContentType: ContentType, difficulty: Difficulty) {
        val topic = currentTopic ?: return
        _selectedContentType.value = defaultContentType
        persist(
            topic.copy(
                name = name.trim(),
                description = description.trim(),
                defaultContentType = defaultContentType,
                difficulty = difficulty
            ),
            "Topic updated"
        )
    }

    private fun persist(updatedTopic: Topic, successMessage: String) {
        // Optimistic update so chips react immediately.
        _uiState.value = UiState.Success(updatedTopic)
        viewModelScope.launch {
            updateTopicUseCase(updatedTopic)
                .onSuccess { _events.emit(successMessage) }
                .onFailure { e ->
                    loadTopic()
                    _events.emit(e.message ?: "Failed to update topic")
                }
        }
    }

    fun deleteTopic() {
        val topic = currentTopic ?: return
        if (_isDeleting.value) return

        viewModelScope.launch {
            _isDeleting.value = true
            deleteTopicUseCase(topic)
                .onSuccess {
                    _topicDeleted.value = true
                }
                .onFailure { e ->
                    _isDeleting.value = false
                    _events.emit(e.message ?: "Failed to delete topic")
                }
        }
    }

    fun generateContent() {
        val topic = currentTopic ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val contentType = _selectedContentType.value
            _generationResult.value = generateContentUseCase(topic.copy(defaultContentType = contentType), contentType)
                .fold(
                    onSuccess = { GenerationResult.Success(contentType, it.size) },
                    onFailure = {
                        GenerationResult.Error(
                            message = it.message ?: "Unknown error",
                            isRetryable = (it as? GenerationException)?.isRetryable ?: true
                        )
                    }
                )

            _isGenerating.value = false
        }
    }

    fun clearGenerationResult() {
        _generationResult.value = null
    }
}

sealed class GenerationResult {
    data class Success(val contentType: ContentType, val count: Int) : GenerationResult()
    data class Error(val message: String, val isRetryable: Boolean) : GenerationResult()
}
