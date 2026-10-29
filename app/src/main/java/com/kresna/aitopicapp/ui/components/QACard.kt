package com.kresna.aitopicapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.theme.CardStyle

/**
 * Q&A shown as a flashcard: the user tries to recall the answer first (retrieval practice),
 * then taps to flip the card.
 */
@Composable
fun QACard(
    content: GeneratedContent,
    modifier: Modifier = Modifier,
    initiallyFlipped: Boolean = false
) {
    var isFlipped by remember(content.id) { mutableStateOf(initiallyFlipped) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "flashcardFlip"
    )
    val haptics = rememberFeedHaptics()
    val style = CardStyle.Flashcard
    val shape = RoundedCornerShape(24.dp)

    FeedCard(style = style, modifier = modifier) {
        Text(
            text = if (isFlipped) "Did you get it?" else "Think of the answer, then tap the card",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(style.start.copy(alpha = 0.22f), style.end.copy(alpha = 0.10f))
                    )
                )
                .border(1.5.dp, style.accent.copy(alpha = 0.6f), shape)
                .clickable(
                    onClickLabel = if (isFlipped) "Show question" else "Reveal answer",
                    role = Role.Button
                ) {
                    haptics.tap()
                    isFlipped = !isFlipped
                }
                .padding(24.dp)
        ) {
            if (rotation <= 90f) {
                Text(
                    text = content.question,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            } else {
                // Counter-rotate the back face so its text is not mirrored.
                Column(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                    Text(
                        text = content.answer.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (content.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CodeBlock(code = content.codeSnippet, language = content.codeLanguage)
                    }
                }
            }
        }

        if (isFlipped) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content.question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
