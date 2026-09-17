package com.kresna.aitopicapp.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent

@Composable
fun ShareButton(
    content: GeneratedContent,
    context: Context
) {
    IconButton(
        onClick = { shareContent(content, context) }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

private fun shareContent(content: GeneratedContent, context: Context) {
    val shareText = buildShareText(content)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "AI Topic Learning - ${content.contentType.displayName}")
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun buildShareText(content: GeneratedContent): String {
    return when (content.contentType) {
        ContentType.MCQ -> buildString {
            appendLine("📚 AI Topic Learning")
            appendLine()
            appendLine("❓ ${content.question}")
            appendLine()
            content.options?.forEach { option ->
                appendLine("• $option")
            }
            appendLine()
            if (content.explanation != null) {
                appendLine("💡 ${content.explanation}")
            }
        }

        ContentType.QA -> buildString {
            appendLine("📚 AI Topic Learning")
            appendLine()
            appendLine("❓ ${content.question}")
            appendLine()
            appendLine("💡 ${content.answer ?: ""}")
        }

        ContentType.FUN_FACT -> buildString {
            appendLine("📚 AI Topic Learning - Fun Fact")
            appendLine()
            appendLine("🤯 ${content.answer ?: ""}")
            if (content.explanation != null) {
                appendLine()
                appendLine("📖 Source: ${content.explanation}")
            }
        }
    }
}
