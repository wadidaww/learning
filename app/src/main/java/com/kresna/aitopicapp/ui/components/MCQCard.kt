package com.kresna.aitopicapp.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.theme.ErrorRed
import com.kresna.aitopicapp.ui.theme.SuccessGreen

@Composable
fun MCQCard(
    content: GeneratedContent,
    onAnswer: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local copy for instant feedback; synced with the stored answer, keyed per item because
    // pager pages are reused for different items.
    var selectedOption by remember(content.id, content.userAnswerIndex) {
        mutableIntStateOf(content.userAnswerIndex ?: -1)
    }
    val isRevealed = selectedOption >= 0
    val context = LocalContext.current

    ContentCardWrapper(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Multiple Choice",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content.question,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (content.codeSnippet != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CodeBlock(
                        code = content.codeSnippet,
                        language = content.codeLanguage
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                content.options?.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrect = content.correctIndex == index
                    val showCorrect = isRevealed && isCorrect
                    val showWrong = isRevealed && isSelected && !isCorrect

                    OutlinedButton(
                        onClick = {
                            if (!isRevealed) {
                                selectedOption = index
                                onAnswer(index)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = when {
                                showCorrect -> SuccessGreen.copy(alpha = 0.1f)
                                showWrong -> ErrorRed.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            }
                        )
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = when {
                                showCorrect -> SuccessGreen
                                showWrong -> ErrorRed
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                if (isRevealed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val isCorrect = selectedOption == content.correctIndex
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isCorrect) "Correct!" else "Not quite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCorrect) SuccessGreen else ErrorRed
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            selectedOption = -1
                            onAnswer(null)
                        }) {
                            Text("Try again")
                        }
                    }
                }

                if (isRevealed && content.explanation != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Explanation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = content.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
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
        appendLine(content.question)
        appendLine()
        content.options?.forEachIndexed { index, option ->
            appendLine("${'a' + index}) $option")
        }
        appendLine()
        appendLine("Answer: ${content.options?.getOrNull(content.correctIndex ?: 0) ?: ""}")
        if (content.explanation != null) {
            appendLine()
            appendLine("Explanation: ${content.explanation}")
        }
        if (content.codeSnippet != null) {
            appendLine()
            appendLine("Code (${content.codeLanguage ?: "snippet"}):")
            appendLine(content.codeSnippet)
        }
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("MCQ Content", text.trim())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
