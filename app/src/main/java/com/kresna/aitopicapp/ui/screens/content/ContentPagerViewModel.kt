package com.kresna.aitopicapp.ui.screens.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.domain.usecase.GenerateContentUseCase
import com.kresna.aitopicapp.domain.usecase.GetTopicsUseCase
import com.kresna.aitopicapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentPagerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val generateContentUseCase: GenerateContentUseCase
) : ViewModel() {

    companion object {
        private const val DEFAULT_BATCH_SIZE = 10
        private const val PREFETCH_THRESHOLD = 2
    }

    private val topicId: Long = savedStateHandle.get<Long>("topicId") ?: 0L
    private val contentTypeStr: String = savedStateHandle.get<String>("contentType") ?: ContentType.MCQ.name

    private val _contentList = MutableStateFlow<List<GeneratedContent>>(emptyList())
    val contentList: StateFlow<List<GeneratedContent>> = _contentList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val contentType: ContentType = try {
        ContentType.valueOf(contentTypeStr)
    } catch (e: IllegalArgumentException) {
        ContentType.MCQ
    }

    private var hasMoreContent = true

    init {
        loadInitialContent()
    }

    private fun loadInitialContent() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val flow = if (contentType == ContentType.MIX) {
                    contentRepository.getContentByTopicForMix(topicId)
                } else {
                    contentRepository.getContentByTopicAndType(topicId, contentType)
                }

                flow.collect { contentList ->
                    _contentList.value = contentList
                    _uiState.value = UiState.Success(Unit)
                    hasMoreContent = true
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load content")
            }
        }
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
        val listSize = _contentList.value.size
        if (listSize > 0 && page >= listSize - PREFETCH_THRESHOLD) {
            loadMoreContent()
        }
    }

    private fun loadMoreContent() {
        if (_isLoadingMore.value || !hasMoreContent) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            try {
                val topic = getTopicsUseCase.getById(topicId)
                if (topic == null) {
                    _isLoadingMore.value = false
                    hasMoreContent = false
                    return@launch
                }

                val result = generateContentUseCase(
                    topicId = topicId,
                    topicName = topic.name,
                    topicDescription = topic.description,
                    contentType = contentType,
                    count = DEFAULT_BATCH_SIZE
                )

                result.fold(
                    onSuccess = { newContent ->
                        if (newContent.isEmpty()) {
                            hasMoreContent = false
                        } else {
                            _contentList.value = _contentList.value + newContent
                        }
                    },
                    onFailure = {
                        hasMoreContent = false
                    }
                )
            } catch (e: Exception) {
                hasMoreContent = false
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun markAsViewed(contentId: Long) {
        viewModelScope.launch {
            try {
                contentRepository.markAsViewed(contentId)
            } catch (_: Exception) {
                // Silently handle - not critical
            }
        }
    }
}
