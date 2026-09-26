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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kresna.aitopicapp.domain.model.Topic
import com.kresna.aitopicapp.ui.common.UiState
import com.kresna.aitopicapp.ui.components.TopicCard
import com.kresna.aitopicapp.ui.components.TopicCardShimmer
import com.kresna.aitopicapp.ui.components.TopicDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTopicClick: (Topic) -> Unit,
    onTopicWithContentClick: (topicId: Long, contentType: String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val topics by viewModel.topics.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Topic?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        when (uiState) {
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
                        text = (uiState as UiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is UiState.Success -> {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            key = { it.id }
                        ) { topic ->
                            SwipeToDeleteTopicItem(
                                topic = topic,
                                onTopicClick = onTopicClick,
                                onTopicWithContentClick = onTopicWithContentClick,
                                onDelete = { deletedTopic ->
                                    viewModel.deleteTopic(deletedTopic)
                                },
                                onEdit = { editTopic ->
                                    showEditDialog = editTopic
                                },
                                viewModel = viewModel
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
            onConfirm = { name, description, contentType ->
                viewModel.createTopic(name, description, contentType)
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
            onDismiss = { showEditDialog = null },
            onConfirm = { name, description, contentType ->
                viewModel.updateTopic(
                    topic.copy(
                        name = name,
                        description = description,
                        defaultContentType = contentType
                    )
                )
                showEditDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTopicItem(
    topic: Topic,
    onTopicClick: (Topic) -> Unit,
    onTopicWithContentClick: (topicId: Long, contentType: String) -> Unit,
    onDelete: (Topic) -> Unit,
    onEdit: (Topic) -> Unit,
    viewModel: HomeViewModel
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(topic)
                true
            } else {
                false
            }
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
        TopicCardContent(
            topic = topic,
            onClick = { onTopicClick(topic) },
            onSmartClick = onTopicWithContentClick,
            onEdit = onEdit,
            viewModel = viewModel
        )
    }
}

@Composable
private fun TopicCardContent(
    topic: Topic,
    onClick: () -> Unit,
    onSmartClick: (topicId: Long, contentType: String) -> Unit,
    onEdit: (Topic) -> Unit,
    viewModel: HomeViewModel
) {
    val contentCount by viewModel.topicContentCount(topic.id).collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    TopicCard(
        topic = topic,
        contentCount = contentCount,
        onClick = {
            if (contentCount > 0) {
                scope.launch {
                    val info = viewModel.getTopicContentInfo(topic.id)
                    if (info.hasContent && info.lastContentType != null) {
                        onSmartClick(topic.id, info.lastContentType.name)
                    } else {
                        onClick()
                    }
                }
            } else {
                onClick()
            }
        },
        onLongClick = { onEdit(topic) }
    )
}
