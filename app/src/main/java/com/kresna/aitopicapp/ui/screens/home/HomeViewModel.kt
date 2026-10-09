package com.kresna.aitopicapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicOverview
import com.kresna.aitopicapp.domain.usecase.CreateTopicUseCase
import com.kresna.aitopicapp.domain.usecase.DeleteTopicUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.domain.usecase.UpdateTopicUseCase
import com.kresna.aitopicapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTopicsUseCase: GetTopicsUseCase,
    private val createTopicUseCase: CreateTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    /** Topics together with their progress, from a single reactive query. */
    val uiState: StateFlow<UiState<List<TopicOverview>>> = getTopicsUseCase.withProgress()
        .map<List<TopicOverview>, UiState<List<TopicOverview>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load topics")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun createTopic(
        name: String,
        description: String,
        defaultContentType: ContentType = ContentType.MCQ,
        difficulty: Difficulty = Difficulty.MIXED
    ) {
        viewModelScope.launch {
            createTopicUseCase(name, description, defaultContentType, difficulty)
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
}
