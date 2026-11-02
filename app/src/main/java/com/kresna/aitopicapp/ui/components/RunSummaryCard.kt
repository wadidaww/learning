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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.R
import com.kresna.aitopicapp.ui.theme.AppTheme
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
    val palette = AppTheme.palette
    val (icon, tint, headline) = when {
        accuracy == null -> Triple(Icons.Rounded.Flag, palette.violet, R.string.run_complete)
        accuracy >= 80 -> Triple(Icons.Rounded.EmojiEvents, palette.amber, R.string.run_brilliant)
        accuracy >= 50 -> Triple(Icons.AutoMirrored.Rounded.TrendingUp, palette.teal, R.string.run_solid)
        else -> Triple(Icons.Rounded.Eco, palette.teal, R.string.run_good_effort)
    }
    val inspection = LocalInspectionMode.current
    val pop = remember(stats.runNumber) { Animatable(if (inspection) 1f else 0.3f) }
    LaunchedEffect(stats.runNumber) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow))
    }

    FeedCard(
        style = CardStyle.Summary,
        label = stringResource(R.string.card_type_run_complete, stats.runNumber),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconBadge(icon = icon, tint = tint, size = 88.dp, modifier = Modifier.scale(pop.value))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(headline),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(value = "${stats.viewed}/${stats.cardCount}", label = stringResource(R.string.run_stat_cards), modifier = Modifier.weight(1f))
            StatTile(
                value = if (stats.answered == 0) "–" else "${stats.correct}/${stats.answered}",
                label = stringResource(R.string.run_stat_correct),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = "${stats.bestStreak}",
                label = stringResource(R.string.run_stat_best_streak),
                icon = Icons.Rounded.LocalFireDepartment,
                iconTint = palette.amber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.run_break_hint),
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
            Text(stringResource(R.string.run_keep_going), style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.run_done))
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
