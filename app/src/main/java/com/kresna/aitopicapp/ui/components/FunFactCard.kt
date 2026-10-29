package com.kresna.aitopicapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.theme.CardStyle

/** A fun fact shown as a "Spark": one surprising idea, big and clear. */
@Composable
fun FunFactCard(
    content: GeneratedContent,
    modifier: Modifier = Modifier
) {
    // Previews and screenshot tests render a single frame, so start them at the final size.
    val inspection = LocalInspectionMode.current
    val emojiScale = remember(content.id) { Animatable(if (inspection) 1f else 0.4f) }
    LaunchedEffect(content.id) {
        emojiScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow))
    }

    val style = CardStyle.Spark
    FeedCard(style = style, modifier = modifier) {
        IconBadge(
            icon = Icons.Rounded.Lightbulb,
            tint = style.accent,
            modifier = Modifier.scale(emojiScale.value)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Did you know?",
            style = MaterialTheme.typography.titleLarge,
            color = style.accent
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content.answer.orEmpty(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (content.codeSnippet != null) {
            Spacer(modifier = Modifier.height(20.dp))
            CodeBlock(code = content.codeSnippet, language = content.codeLanguage)
        }

        if (content.explanation != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "— ${content.explanation}",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
