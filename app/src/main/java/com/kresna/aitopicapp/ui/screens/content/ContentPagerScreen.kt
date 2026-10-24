package com.kresna.aitopicapp.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.ui.components.FunFactCard
import com.kresna.aitopicapp.ui.components.GenerationStatusCard
import com.kresna.aitopicapp.ui.components.MCQCard
import com.kresna.aitopicapp.ui.components.PagerProgressHeader
import com.kresna.aitopicapp.ui.components.QACard
import com.kresna.aitopicapp.ui.components.ShareButton
import com.kresna.aitopicapp.ui.components.UnsupportedContentCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPagerScreen(
    onBackClick: () -> Unit,
    onTopicDetails: () -> Unit,
    viewModel: ContentPagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val contentList = state.content
    // In learning mode an extra trailing page shows generation progress, errors and "load more".
    val statusPageCount = if (state.canGenerate) 1 else 0

    val pagerState = rememberPagerState(pageCount = { contentList.size + statusPageCount })
    val contentSize by rememberUpdatedState(contentList.size)
    val scope = rememberCoroutineScope()
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    // Resume where the user left off: jump once to the first unviewed item.
    LaunchedEffect(state.startPage, state.startPageConsumed) {
        val startPage = state.startPage
        if (state.startPageConsumed || startPage == null) return@LaunchedEffect
        pagerState.scrollToPage(startPage)
        viewModel.onStartPageConsumed()
    }

    // Only track pages after the initial jump, so page 0 is not marked as viewed on open.
    LaunchedEffect(pagerState, state.startPageConsumed) {
        if (!state.startPageConsumed) return@LaunchedEffect
        // Also re-check when content is appended while the user sits on the status page.
        snapshotFlow { pagerState.settledPage to contentSize }
            .distinctUntilChanged()
            .collect { (page, _) -> viewModel.onPageSettled(page) }
    }

    val currentContent = contentList.getOrNull(pagerState.currentPage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.topicName.ifBlank { "Content" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = modeLabel(state.contentType, state.filter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (currentContent != null) {
                        IconButton(onClick = { viewModel.toggleBookmark(currentContent) }) {
                            Icon(
                                imageVector = if (currentContent.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (currentContent.isBookmarked) "Remove bookmark" else "Bookmark"
                            )
                        }
                    }
                    if (contentList.size > 1) {
                        IconButton(onClick = {
                            jumpPageInput = ""
                            showJumpDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Jump to page"
                            )
                        }
                    }
                    if (currentContent != null) {
                        ShareButton(content = currentContent)
                    }
                    IconButton(onClick = onTopicDetails) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Topic progress"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> CenteredMessage(
                modifier = Modifier.padding(padding),
                showProgress = true,
                title = "Loading content..."
            )

            contentList.isEmpty() && !state.canGenerate -> CenteredMessage(
                modifier = Modifier.padding(padding),
                title = if (state.filter == ContentFilter.BOOKMARKED) "No bookmarks yet" else "No missed questions",
                body = if (state.filter == ContentFilter.BOOKMARKED) {
                    "Tap the bookmark icon on any card to save it for review."
                } else {
                    "Questions you answer incorrectly show up here so you can practise them again."
                },
                actionLabel = "Go Back",
                onAction = onBackClick
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (contentList.isNotEmpty()) {
                    PagerProgressHeader(
                        currentPage = pagerState.currentPage,
                        pageCount = contentList.size,
                        viewedCount = contentList.count { it.isViewed },
                        newItemsCount = state.newItemsCount
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                    // No item keys on purpose: when a batch arrives while the status page is shown,
                    // the first new item takes its place instead of the pager following the status page.
                ) { page ->
                    val content = contentList.getOrNull(page)
                    if (content == null) {
                        GenerationStatusCard(
                            status = state.generation,
                            hasContent = contentList.isNotEmpty(),
                            onGenerate = viewModel::retryGeneration
                        )
                    } else {
                        when (content.contentType) {
                            ContentType.MCQ -> MCQCard(
                                content = content,
                                onAnswer = { index -> viewModel.answer(content, index) }
                            )
                            ContentType.QA -> QACard(content = content)
                            ContentType.FUN_FACT -> FunFactCard(content = content)
                            else -> UnsupportedContentCard(content = content)
                        }
                    }
                }
            }
        }
    }

    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("Jump to Page") },
            text = {
                Column {
                    Text(
                        text = "Enter page number (1-${contentList.size})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumpPageInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                jumpPageInput = newValue
                            }
                        },
                        label = { Text("Page number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pageNumber = jumpPageInput.toIntOrNull()
                        if (pageNumber != null && pageNumber in 1..contentList.size) {
                            scope.launch {
                                pagerState.scrollToPage(pageNumber - 1)
                            }
                        }
                        showJumpDialog = false
                    },
                    enabled = jumpPageInput.toIntOrNull()?.let { it in 1..contentList.size } == true
                ) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun modeLabel(contentType: ContentType, filter: ContentFilter): String = when (filter) {
    ContentFilter.ALL -> if (contentType == ContentType.MIX) "Mixed content" else contentType.displayName
    ContentFilter.BOOKMARKED -> "Review: bookmarked"
    ContentFilter.MISSED -> "Review: missed questions"
}

@Composable
private fun CenteredMessage(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (body != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
