package com.kresna.aitopicapp.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.components.FeedHeader
import com.kresna.aitopicapp.ui.components.FeedProgress
import com.kresna.aitopicapp.ui.components.FunFactCard
import com.kresna.aitopicapp.ui.components.GenerationStatusCard
import com.kresna.aitopicapp.ui.components.MCQCard
import com.kresna.aitopicapp.ui.components.QACard
import com.kresna.aitopicapp.ui.components.RailAction
import com.kresna.aitopicapp.ui.components.RunSummaryCard
import com.kresna.aitopicapp.ui.components.UnsupportedContentCard
import com.kresna.aitopicapp.ui.components.copyContentToClipboard
import com.kresna.aitopicapp.ui.components.rememberFeedHaptics
import com.kresna.aitopicapp.ui.components.shareContent
import com.kresna.aitopicapp.ui.theme.CardStyle
import com.kresna.aitopicapp.ui.theme.Gold
import com.kresna.aitopicapp.util.Constants
import com.kresna.aitopicapp.util.FeedPage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Full-screen, swipe-up learning feed (like short-form video apps): one card per screen, a
 * translucent header with run progress and combo, and an action rail on the right.
 */
@Composable
fun ContentPagerScreen(
    onBackClick: () -> Unit,
    onTopicDetails: () -> Unit,
    viewModel: ContentPagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pages = state.pages

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val pageCount by rememberUpdatedState(pages.size)
    val scope = rememberCoroutineScope()

    // Resume where the user left off: jump once to the first unviewed card.
    LaunchedEffect(state.startPage, state.startPageConsumed) {
        val startPage = state.startPage
        if (state.startPageConsumed || startPage == null) return@LaunchedEffect
        pagerState.scrollToPage(startPage)
        viewModel.onStartPageConsumed()
    }

    // Only track pages after the initial jump, so page 0 is not marked as viewed on open.
    LaunchedEffect(pagerState, state.startPageConsumed) {
        if (!state.startPageConsumed) return@LaunchedEffect
        // Also re-check when cards are appended while the user sits on the status page.
        snapshotFlow { pagerState.settledPage to pageCount }
            .distinctUntilChanged()
            .collect { (page, _) -> viewModel.onPageSettled(page) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> CenteredMessage(showProgress = true, title = "Loading your cards...")

            pages.isEmpty() -> CenteredMessage(
                title = if (state.filter == ContentFilter.BOOKMARKED) "No saved cards yet" else "No missed questions",
                body = if (state.filter == ContentFilter.BOOKMARKED) {
                    "Tap Save on any card to keep it for review."
                } else {
                    "Quiz questions you get wrong show up here so you can practise them again."
                },
                actionLabel = "Go back",
                onAction = onBackClick
            )

            else -> {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                    // No item keys on purpose: when a batch arrives while the status page is shown,
                    // the first new card takes its place instead of the pager following the status page.
                ) { page ->
                    when (val feedPage = pages.getOrNull(page)) {
                        is FeedPage.Card -> ContentCard(
                            content = feedPage.content,
                            onAnswer = { index -> viewModel.answer(feedPage.content, index) }
                        )
                        is FeedPage.RunSummary -> RunSummaryCard(
                            stats = feedPage.stats,
                            onKeepGoing = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                            onDone = onBackClick
                        )
                        FeedPage.Status, null -> GenerationStatusCard(
                            status = state.generation,
                            hasContent = state.content.isNotEmpty(),
                            onGenerate = viewModel::retryGeneration
                        )
                    }
                }

                val currentPage = pages.getOrNull(pagerState.currentPage)
                val currentContent = (currentPage as? FeedPage.Card)?.content

                FeedHeader(
                    title = state.topicName.ifBlank { "Learning" },
                    subtitle = modeLabel(state.contentType, state.filter),
                    progress = progressFor(state, currentPage, pagerState.currentPage),
                    combo = state.combo,
                    showCombo = state.combo >= Constants.COMBO_MIN_STREAK,
                    accent = currentContent?.let { CardStyle.forType(it.contentType) } ?: CardStyle.Summary,
                    onBack = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 4.dp)
                )

                if (currentContent != null) {
                    ActionRail(
                        content = currentContent,
                        onToggleBookmark = { viewModel.toggleBookmark(currentContent) },
                        onTopicDetails = onTopicDetails,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = 8.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentCard(content: GeneratedContent, onAnswer: (Int?) -> Unit) {
    when (content.contentType) {
        ContentType.MCQ -> MCQCard(content = content, onAnswer = onAnswer)
        ContentType.QA -> QACard(content = content)
        ContentType.FUN_FACT -> FunFactCard(content = content)
        else -> UnsupportedContentCard(content = content)
    }
}

@Composable
private fun ActionRail(
    content: GeneratedContent,
    onToggleBookmark: () -> Unit,
    onTopicDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberFeedHaptics()
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        RailAction(
            icon = if (content.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            label = if (content.isBookmarked) "Saved" else "Save",
            contentDescription = if (content.isBookmarked) "Remove from saved cards" else "Save card for review",
            tint = if (content.isBookmarked) Gold else MaterialTheme.colorScheme.onBackground,
            onClick = {
                haptics.tap()
                onToggleBookmark()
            }
        )
        RailAction(
            icon = Icons.Default.Share,
            label = "Share",
            contentDescription = "Share card",
            onClick = { shareContent(content, context) }
        )
        RailAction(
            icon = Icons.Default.ContentCopy,
            label = "Copy",
            contentDescription = "Copy card text",
            onClick = { copyContentToClipboard(context, content) }
        )
        RailAction(
            icon = Icons.Default.Insights,
            label = "Stats",
            contentDescription = "Topic progress",
            onClick = onTopicDetails
        )
    }
}

private fun progressFor(state: ContentPagerUiState, page: FeedPage?, pageIndex: Int): FeedProgress? {
    val runSize = Constants.FEED_RUN_SIZE
    if (!state.hasRuns) {
        if (state.content.isEmpty()) return null
        return FeedProgress.Count(position = (pageIndex + 1).coerceAtMost(state.content.size), count = state.content.size)
    }
    return when (page) {
        is FeedPage.Card -> FeedProgress.Run(page.runIndex + 1, page.positionInRun + 1, runSize)
        is FeedPage.RunSummary -> FeedProgress.Run(page.stats.runNumber, runSize, runSize)
        FeedPage.Status, null -> FeedProgress.Run(
            runNumber = state.content.size / runSize + 1,
            filled = state.content.size % runSize,
            total = runSize
        )
    }
}

private fun modeLabel(contentType: ContentType, filter: ContentFilter): String = when (filter) {
    ContentFilter.ALL -> if (contentType == ContentType.MIX) "Mixed feed" else contentType.displayName
    ContentFilter.BOOKMARKED -> "Review: saved cards"
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
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
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
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
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
