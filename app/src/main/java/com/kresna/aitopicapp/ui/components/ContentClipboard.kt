package com.kresna.aitopicapp.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.kresna.aitopicapp.R
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.util.OptionText

/** Copies a card as plain text (question, options/answer, explanation, code) and confirms with a toast. */
fun copyContentToClipboard(context: Context, content: GeneratedContent) {
    val text = buildString {
        when (content.contentType) {
            ContentType.FUN_FACT -> {
                appendLine(context.getString(R.string.spark_title))
                appendLine(content.answer.orEmpty())
                content.explanation?.let { appendLine().appendLine(it) }
            }
            ContentType.MCQ -> {
                appendLine(content.question)
                appendLine()
                // Same letters and text as the card shows.
                val options = OptionText.stripEnumerators(content.options.orEmpty())
                options.forEachIndexed { index, option -> appendLine("${'A' + index}. $option") }
                appendLine()
                appendLine("Answer: ${options.getOrNull(content.correctIndex ?: -1).orEmpty()}")
                content.explanation?.let { appendLine().appendLine("Explanation: $it") }
            }
            else -> {
                appendLine(content.question)
                appendLine()
                appendLine("Answer: ${content.answer.orEmpty()}")
            }
        }
        content.codeSnippet?.let {
            appendLine()
            appendLine("Code (${content.codeLanguage ?: "snippet"}):")
            appendLine(it)
        }
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.clipboard_label), text.trim()))
    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
}
