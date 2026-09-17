package com.kresna.aitopicapp.ui.screens.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.domain.repository.ContentRepository
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
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val topicId: Long = savedStateHandle.get<Long>("topicId") ?: 0L
    private val contentTypeStr: String = savedStateHandle.get<String>("contentType") ?: ContentType.MCQ.name

    private val _uiState = MutableStateFlow<UiState<List<GeneratedContent>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<GeneratedContent>>> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val contentType = ContentType.valueOf(contentTypeStr)
                contentRepository.getContentByTopicAndType(topicId, contentType).collect { contentList ->
                    _uiState.value = if (contentList.isEmpty()) {
                        UiState.Success(emptyList())
                    } else {
                        UiState.Success(contentList)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load content")
            }
        }
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
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
