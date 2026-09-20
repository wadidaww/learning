package com.kresna.aitopicapp.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kresna.aitopicapp.domain.model.GeneratedContent

@Composable
fun FunFactCard(
    content: GeneratedContent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ContentCardWrapper(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fun Fact",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "\uD83E\uDD2F",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = content.answer ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (content.codeSnippet != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CodeBlock(
                        code = content.codeSnippet,
                        language = content.codeLanguage
                    )
                }

                if (content.explanation != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "\u2014 ${content.explanation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            IconButton(
                onClick = { copyContentToClipboard(context, content) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun copyContentToClipboard(context: Context, content: GeneratedContent) {
    val text = buildString {
        appendLine("Fun Fact:")
        appendLine(content.answer ?: "")
        if (content.explanation != null) {
            appendLine()
            appendLine(content.explanation)
        }
        if (content.codeSnippet != null) {
            appendLine()
            appendLine("Code (${content.codeLanguage ?: "snippet"}):")
            appendLine(content.codeSnippet)
        }
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Fun Fact Content", text.trim())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
