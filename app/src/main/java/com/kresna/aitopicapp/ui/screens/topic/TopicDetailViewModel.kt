package com.kresna.aitopicapp.ui.screens.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.usecase.GenerateContentUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val generateContentUseCase: GenerateContentUseCase
) : ViewModel() {

    private val topicId: Long = savedStateHandle.get<Long>("topicId") ?: 0L

    private val _uiState = MutableStateFlow<UiState<Topic>>(UiState.Loading)
    val uiState: StateFlow<UiState<Topic>> = _uiState.asStateFlow()

    private val _selectedContentType = MutableStateFlow(ContentType.MCQ)
    val selectedContentType: StateFlow<ContentType> = _selectedContentType.asStateFlow()

    private val _selectedCount = MutableStateFlow(5)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationResult = MutableStateFlow<GenerationResult?>(null)
    val generationResult: StateFlow<GenerationResult?> = _generationResult.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadTopic()
    }

    private fun loadTopic() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val topic = getTopicsUseCase.getById(topicId)
                if (topic != null) {
                    _uiState.value = UiState.Success(topic)
                } else {
                    _uiState.value = UiState.Error("Topic not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load topic")
            }
        }
    }

    fun selectContentType(type: ContentType) {
        _selectedContentType.value = type
    }

    fun selectCount(count: Int) {
        _selectedCount.value = count
    }

    fun generateContent() {
        val topic = (_uiState.value as? UiState.Success)?.data ?: return

        viewModelScope.launch {
            _isGenerating.value = true
            _generationResult.value = null

            val result = generateContentUseCase(
                topicId = topicId,
                topicName = topic.name,
                topicDescription = topic.description,
                contentType = _selectedContentType.value,
                count = _selectedCount.value
            )

            _generationResult.value = result.fold(
                onSuccess = { GenerationResult.Success(it) },
                onFailure = { GenerationResult.Error(it.message ?: "Unknown error") }
            )

            _isGenerating.value = false
        }
    }

    fun clearGenerationResult() {
        _generationResult.value = null
    }
}

sealed class GenerationResult {
    data class Success(val content: List<GeneratedContent>) : GenerationResult()
    data class Error(val message: String) : GenerationResult()
}
