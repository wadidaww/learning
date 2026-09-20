package com.kresna.aitopicapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.usecase.CreateTopicUseCase
import com.kresna.aitopicapp.domain.usecase.DeleteTopicUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicContentInfoUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.domain.usecase.UpdateTopicUseCase
import com.kresna.aitopicapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val createTopicUseCase: CreateTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase,
    private val getTopicContentInfoUseCase: GetTopicContentInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Topic>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Topic>>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val topics: StateFlow<List<Topic>> = getTopicsUseCase()
        .catch { e ->
            _uiState.value = UiState.Error(e.message ?: "Failed to load topics")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            topics.collect { topicList ->
                _uiState.value = if (topicList.isEmpty() && _uiState.value is UiState.Loading) {
                    UiState.Success(emptyList())
                } else {
                    UiState.Success(topicList)
                }
            }
        }
    }

    fun createTopic(name: String, description: String) {
        viewModelScope.launch {
            createTopicUseCase(name, description)
                .onSuccess {
                    _events.emit("Topic created")
                }
                .onFailure { e ->
                    _events.emit(e.message ?: "Failed to create topic")
                }
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            deleteTopicUseCase(topic)
                .onSuccess {
                    _events.emit("${topic.name} deleted")
                }
                .onFailure { e ->
                    _events.emit(e.message ?: "Failed to delete topic")
                }
        }
    }

    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            updateTopicUseCase(topic)
                .onSuccess {
                    _events.emit("${topic.name} updated")
                }
                .onFailure { e ->
                    _events.emit(e.message ?: "Failed to update topic")
                }
        }
    }

    fun topicContentCount(topicId: Long) = getTopicsUseCase.getContentCount(topicId)

    suspend fun getTopicContentInfo(topicId: Long): com.kresna.aitopicapp.domain.usecase.TopicContentInfo {
        return getTopicContentInfoUseCase(topicId)
    }
}
