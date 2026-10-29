package com.kresna.aitopicapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kresna.aitopicapp.ui.theme.CardStyle
import com.kresna.aitopicapp.util.RunStats

/**
 * End of a run: celebrates the result and offers a clear choice between another run and stopping,
 * so the feed has a natural break instead of scrolling forever.
 */
@Composable
fun RunSummaryCard(
    stats: RunStats,
    onKeepGoing: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = stats.accuracyPercent
    val (emoji, headline) = when {
        accuracy == null -> "🏁" to "Run complete!"
        accuracy >= 80 -> "🏆" to "Brilliant run!"
        accuracy >= 50 -> "💪" to "Solid run!"
        else -> "🌱" to "Good effort, it'll stick next time"
    }
    val inspection = LocalInspectionMode.current
    val pop = remember(stats.runNumber) { Animatable(if (inspection) 1f else 0.3f) }
    LaunchedEffect(stats.runNumber) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow))
    }

    FeedCard(
        style = CardStyle.Summary,
        label = "RUN ${stats.runNumber} COMPLETE",
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = emoji,
            fontSize = 72.sp,
            modifier = Modifier
                .scale(pop.value)
                .clearAndSetSemantics { }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(value = "${stats.viewed}/${stats.cardCount}", label = "cards", modifier = Modifier.weight(1f))
            StatTile(
                value = if (stats.answered == 0) "–" else "${stats.correct}/${stats.answered}",
                label = "correct",
                modifier = Modifier.weight(1f)
            )
            StatTile(value = "🔥 ${stats.bestStreak}", label = "best streak", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Good place for a break. Ideas stick better when you come back to them later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onKeepGoing,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text("Keep going", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done for now")
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
