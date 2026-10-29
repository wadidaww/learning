package com.kresna.aitopicapp.ui.components

import android.content.Intent
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent

/** Opens the system share sheet with a card as plain text. */
fun shareContent(content: GeneratedContent, context: android.content.Context) {
    val shareText = buildShareText(content)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "AI Topic Learning - ${content.contentType.displayName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
            if (content.codeSnippet != null) {
                appendLine()
                appendLine("📝 Code (${content.codeLanguage ?: "code"}):")
                appendLine("```${content.codeLanguage ?: ""}")
                appendLine(content.codeSnippet)
                appendLine("```")
            }
        }

        ContentType.QA -> buildString {
            appendLine("📚 AI Topic Learning")
            appendLine()
            appendLine("❓ ${content.question}")
            appendLine()
            appendLine("💡 ${content.answer ?: ""}")
            if (content.codeSnippet != null) {
                appendLine()
                appendLine("📝 Code (${content.codeLanguage ?: "code"}):")
                appendLine("```${content.codeLanguage ?: ""}")
                appendLine(content.codeSnippet)
                appendLine("```")
            }
        }

        ContentType.FUN_FACT -> buildString {
            appendLine("📚 AI Topic Learning - Fun Fact")
            appendLine()
            appendLine("🤯 ${content.answer ?: ""}")
            if (content.explanation != null) {
                appendLine()
                appendLine("📖 Source: ${content.explanation}")
            }
            if (content.codeSnippet != null) {
                appendLine()
                appendLine("📝 Code (${content.codeLanguage ?: "code"}):")
                appendLine("```${content.codeLanguage ?: ""}")
                appendLine(content.codeSnippet)
                appendLine("```")
            }
        }

        ContentType.MIX -> buildString {
            appendLine("📚 AI Topic Learning")
            appendLine()
            appendLine("${content.question}")
            if (content.answer != null) {
                appendLine()
                appendLine("${content.answer}")
            }
            if (content.codeSnippet != null) {
                appendLine()
                appendLine("📝 Code (${content.codeLanguage ?: "code"}):")
                appendLine("```${content.codeLanguage ?: ""}")
                appendLine(content.codeSnippet)
                appendLine("```")
            }
        }
    }
}
