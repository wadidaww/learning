package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.ui.screens.content.GenerationStatus
import com.kresna.aitopicapp.ui.theme.CardStyle

/** Trailing feed page: shows that more content is being generated, why it failed, or offers more. */
@Composable
fun GenerationStatusCard(
    status: GenerationStatus,
    hasContent: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    FeedCard(
        style = CardStyle.Spark,
        label = "",
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        when (status) {
            GenerationStatus.Running -> {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(20.dp))
                StatusText(
                    title = if (hasContent) "Cooking up your next cards..." else "Creating your first cards...",
                    body = "New cards appear right here. This can take up to a minute."
                )
            }

            is GenerationStatus.Failed -> {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                StatusText(title = "Couldn't make new cards", body = status.message)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                    Text("Try again")
                }
            }

            GenerationStatus.Idle -> {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                StatusText(
                    title = "You're all caught up",
                    body = "Get a fresh set of cards to keep going."
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                    Text("Get more cards")
                }
            }
        }
    }
}

@Composable
private fun StatusText(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
