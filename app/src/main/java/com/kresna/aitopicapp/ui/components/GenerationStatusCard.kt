package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Trailing pager page: shows that more content is being generated, why it failed, or offers more. */
@Composable
fun GenerationStatusCard(
    status: GenerationStatus,
    hasContent: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    ContentCardWrapper(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (status) {
                GenerationStatus.Running -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusText(
                        title = if (hasContent) "Generating more content..." else "Generating your first batch...",
                        body = "New items are added to the end of the list. This can take up to a minute."
                    )
                }

                is GenerationStatus.Failed -> {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusText(title = "Couldn't generate content", body = status.message)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onGenerate) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusText(
                        title = "You're all caught up",
                        body = "Generate a fresh batch to keep learning."
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onGenerate) {
                        Text("Generate more")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusText(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
