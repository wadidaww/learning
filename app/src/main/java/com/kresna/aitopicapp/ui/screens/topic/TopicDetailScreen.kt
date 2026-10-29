package com.kresna.aitopicapp.ui.screens.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kresna.aitopicapp.domain.model.ContentFilter
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty
import com.kresna.aitopicapp.domain.model.TopicProgress
import com.kresna.aitopicapp.ui.common.UiState
import com.kresna.aitopicapp.ui.components.ErrorDialog
import com.kresna.aitopicapp.ui.components.TopicCardShimmer
import com.kresna.aitopicapp.ui.components.TopicDialog
import com.kresna.aitopicapp.util.Constants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopicDetailScreen(
    onBackClick: () -> Unit,
    onOpenContent: (ContentType, ContentFilter) -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val selectedContentType by viewModel.selectedContentType.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationResult by viewModel.generationResult.collectAsState()
    val topicDeleted by viewModel.topicDeleted.collectAsState()
    var errorDialog by remember { mutableStateOf<GenerationResult.Error?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(topicDeleted) {
        if (topicDeleted) {
            onBackClick()
        }
    }

    LaunchedEffect(generationResult) {
        when (val result = generationResult) {
            is GenerationResult.Success -> {
                viewModel.clearGenerationResult()
                // The pager resumes at the first unviewed item, i.e. the start of the new batch.
                onOpenContent(result.contentType, ContentFilter.ALL)
            }
            is GenerationResult.Error -> {
                errorDialog = result
                viewModel.clearGenerationResult()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (val state = uiState) {
                            is UiState.Success -> state.data.name
                            else -> "Topic"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState is UiState.Success) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Topic"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Topic"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopicCardShimmer()
                }
            }

            is UiState.Success -> {
                val topic = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (topic.description.isNotBlank()) {
                        Text(
                            text = topic.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    ProgressCard(progress = progress)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (progress.total > 0) {
                        Button(
                            onClick = { onOpenContent(selectedContentType, ContentFilter.ALL) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (progress.unviewed > 0) "Continue learning (${progress.unviewed} new)" else "Continue learning")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = { viewModel.generateContent() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Generating...")
                        } else {
                            Text(
                                if (selectedContentType == ContentType.MIX) {
                                    "Generate ${Constants.MixContent.TOTAL} new items"
                                } else {
                                    "Generate ${Constants.DEFAULT_BATCH_SIZE} new items"
                                }
                            )
                        }
                    }

                    if (progress.bookmarked > 0 || progress.missed > 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Review")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (progress.missed > 0) {
                                OutlinedButton(
                                    onClick = { onOpenContent(ContentType.MCQ, ContentFilter.MISSED) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Missed (${progress.missed})")
                                }
                            }
                            if (progress.bookmarked > 0) {
                                OutlinedButton(
                                    onClick = { onOpenContent(ContentType.MIX, ContentFilter.BOOKMARKED) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Saved (${progress.bookmarked})")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle("Content Mode")
                    Text(
                        text = "Selection is saved as this topic's default",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ContentType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedContentType == type,
                                onClick = { viewModel.selectContentType(type) },
                                label = { Text(type.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionTitle("Difficulty")
                    Text(
                        text = "Applies to newly generated content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Difficulty.entries.forEach { level ->
                            FilterChip(
                                selected = topic.difficulty == level,
                                onClick = { viewModel.selectDifficulty(level) },
                                label = { Text(level.displayName) }
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        (uiState as? UiState.Success)?.data?.let { topic ->
            TopicDialog(
                title = "Edit Topic",
                confirmText = "Save",
                initialName = topic.name,
                initialDescription = topic.description,
                initialContentType = topic.defaultContentType,
                initialDifficulty = topic.difficulty,
                onDismiss = { showEditDialog = false },
                onConfirm = { name, description, contentType, difficulty ->
                    viewModel.saveTopic(name, description, contentType, difficulty)
                    showEditDialog = false
                }
            )
        }
    }

    if (showDeleteDialog) {
        val topicName = (uiState as? UiState.Success)?.data?.name ?: "this topic"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Topic") },
            text = {
                Text("Delete \"${topicName}\" and all of its generated content? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTopic()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    errorDialog?.let { error ->
        ErrorDialog(
            title = "Generation Failed",
            message = error.message,
            icon = Icons.Rounded.Warning,
            onDismiss = { errorDialog = null },
            onRetry = if (error.isRetryable) {
                {
                    errorDialog = null
                    viewModel.generateContent()
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ProgressCard(progress: TopicProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your progress",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (progress.total == 0) {
                Text(
                    text = "No content yet. Pick a mode and difficulty below, then generate your first batch.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                LinearProgressIndicator(
                    progress = { progress.viewedFraction },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Stat(label = "Seen", value = "${progress.viewed}/${progress.total}", modifier = Modifier.weight(1f))
                    Stat(label = "Answered", value = "${progress.answered}", modifier = Modifier.weight(1f))
                    Stat(
                        label = "Accuracy",
                        value = progress.accuracyPercent?.let { "$it%" } ?: "–",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
