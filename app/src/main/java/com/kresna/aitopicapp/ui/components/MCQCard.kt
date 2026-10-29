package com.kresna.aitopicapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.theme.CardStyle
import com.kresna.aitopicapp.ui.theme.ErrorRed
import com.kresna.aitopicapp.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** "A. ", "(b) ", "3) " prefixes the AI sometimes puts on options; the card shows its own letter badge. */
private val OPTION_PREFIX = Regex("^\\(?[A-Ha-h1-8][.):]\\s+")

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
    val isCorrect = isRevealed && selectedOption == content.correctIndex
    val haptics = rememberFeedHaptics()
    val scope = rememberCoroutineScope()
    val shake = remember(content.id) { Animatable(0f) }
    val style = CardStyle.Quiz

    FeedCard(style = style, modifier = modifier) {
        Text(
            text = content.question,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (content.codeSnippet != null) {
            Spacer(modifier = Modifier.height(16.dp))
            CodeBlock(code = content.codeSnippet, language = content.codeLanguage)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
        ) {
            content.options?.forEachIndexed { index, option ->
                OptionButton(
                    letter = 'A' + index,
                    text = OPTION_PREFIX.replace(option.trim(), ""),
                    state = when {
                        !isRevealed -> OptionState.Idle
                        index == content.correctIndex -> OptionState.Correct
                        index == selectedOption -> OptionState.Wrong
                        else -> OptionState.Dimmed
                    },
                    accent = style.accent,
                    onClick = {
                        if (isRevealed) return@OptionButton
                        selectedOption = index
                        onAnswer(index)
                        if (index == content.correctIndex) {
                            haptics.correct()
                        } else {
                            haptics.wrong()
                            scope.launch { shakeOnce(shake) }
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isRevealed,
            enter = fadeIn(tween(250)) + expandVertically(tween(250))
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                FeedbackBanner(
                    isCorrect = isCorrect,
                    correctLetter = content.correctIndex?.let { 'A' + it },
                    onReset = {
                        selectedOption = -1
                        onAnswer(null)
                    }
                )
                if (content.explanation != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = content.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

private enum class OptionState { Idle, Correct, Wrong, Dimmed }

@Composable
private fun OptionButton(
    letter: Char,
    text: String,
    state: OptionState,
    accent: Color,
    onClick: () -> Unit
) {
    val border by animateColorAsState(
        targetValue = when (state) {
            OptionState.Correct -> SuccessGreen
            OptionState.Wrong -> ErrorRed
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "optionBorder"
    )
    val fill by animateColorAsState(
        targetValue = when (state) {
            OptionState.Correct -> SuccessGreen.copy(alpha = 0.18f)
            OptionState.Wrong -> ErrorRed.copy(alpha = 0.18f)
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        },
        label = "optionFill"
    )
    // The right answer "pops" when revealed.
    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.Correct) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "optionScale"
    )
    val shape = RoundedCornerShape(18.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .scale(scale)
            .alpha(if (state == OptionState.Dimmed) 0.5f else 1f)
            .clip(shape)
            .background(fill)
            .border(width = 1.5.dp, color = border, shape = shape)
            .clickable(enabled = state == OptionState.Idle, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        OptionState.Correct -> SuccessGreen
                        OptionState.Wrong -> ErrorRed
                        else -> accent.copy(alpha = 0.22f)
                    }
                )
        ) {
            when (state) {
                OptionState.Correct -> Icon(Icons.Default.Check, contentDescription = "Correct answer", tint = Color.White, modifier = Modifier.size(18.dp))
                OptionState.Wrong -> Icon(Icons.Default.Close, contentDescription = "Your answer, incorrect", tint = Color.White, modifier = Modifier.size(18.dp))
                else -> Text(text = letter.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun FeedbackBanner(isCorrect: Boolean, correctLetter: Char?, onReset: () -> Unit) {
    val inspection = LocalInspectionMode.current
    val pop = remember { Animatable(if (inspection) 1f else 0.6f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isCorrect) "🎉 Nailed it!" else "Not quite" + (correctLetter?.let { ". It's $it." } ?: "."),
            style = MaterialTheme.typography.titleLarge,
            color = if (isCorrect) SuccessGreen else ErrorRed,
            modifier = Modifier
                .weight(1f)
                .scale(pop.value)
        )
        TextButton(onClick = onReset) {
            Text("Try again")
        }
    }
}

private suspend fun shakeOnce(shake: Animatable<Float, AnimationVector1D>) {
    for (target in listOf(-24f, 20f, -14f, 8f, 0f)) {
        shake.animateTo(target, tween(durationMillis = 55))
    }
}
