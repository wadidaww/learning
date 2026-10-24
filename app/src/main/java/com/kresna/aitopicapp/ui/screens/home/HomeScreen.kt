package com.kresna.aitopicapp.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.domain.model.TopicOverview
import com.kresna.aitopicapp.ui.common.UiState
import com.kresna.aitopicapp.ui.components.TopicCard
import com.kresna.aitopicapp.ui.components.TopicCardShimmer
import com.kresna.aitopicapp.ui.components.TopicDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTopicDetails: (Topic) -> Unit,
    onContinueLearning: (Topic) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Topic?>(null) }
    var pendingDelete by remember { mutableStateOf<Topic?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Topic Learning") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Topic"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        TopicCardShimmer()
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is UiState.Success -> {
                val topics = state.data
                if (topics.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No topics yet.\nTap + to create one!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = topics,
                            key = { it.topic.id }
                        ) { overview ->
                            SwipeToDeleteTopicItem(
                                overview = overview,
                                onClick = {
                                    // Topics with content resume learning directly; new topics open their setup screen.
                                    if (overview.progress.total > 0) {
                                        onContinueLearning(overview.topic)
                                    } else {
                                        onTopicDetails(overview.topic)
                                    }
                                },
                                onDetails = { onTopicDetails(overview.topic) },
                                onDelete = { pendingDelete = overview.topic },
                                onEdit = { showEditDialog = overview.topic }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TopicDialog(
            title = "Create New Topic",
            confirmText = "Create",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description, contentType, difficulty ->
                viewModel.createTopic(name, description, contentType, difficulty)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { topic ->
        TopicDialog(
            title = "Edit Topic",
            confirmText = "Save",
            initialName = topic.name,
            initialDescription = topic.description,
            initialContentType = topic.defaultContentType,
            initialDifficulty = topic.difficulty,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, description, contentType, difficulty ->
                viewModel.updateTopic(
                    topic.copy(
                        name = name,
                        description = description,
                        defaultContentType = contentType,
                        difficulty = difficulty
                    )
                )
                showEditDialog = null
            }
        )
    }

    pendingDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Topic") },
            text = { Text("Delete \"${topic.name}\" and all of its generated content? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTopic(topic)
                    pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTopicItem(
    overview: TopicOverview,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                // Ask for confirmation first; the card springs back either way.
                onDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipeColor"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0.75f,
                label = "swipeScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        TopicCard(
            topic = overview.topic,
            progress = overview.progress,
            onClick = onClick,
            onDetailsClick = onDetails,
            onLongClick = onEdit
        )
    }
}
