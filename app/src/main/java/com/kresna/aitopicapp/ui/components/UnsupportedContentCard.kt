package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.theme.CardStyle

@Composable
fun UnsupportedContentCard(
    content: GeneratedContent,
    modifier: Modifier = Modifier
) {
    FeedCard(style = CardStyle.Neutral, label = "UNSUPPORTED", modifier = modifier) {
        Text(
            text = "This card type (${content.contentType.displayName}) can't be shown yet.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (content.question.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content.question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
