package com.kresna.aitopicapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.ui.theme.Amber
import com.kresna.aitopicapp.ui.theme.CardStyle
import com.kresna.aitopicapp.ui.theme.Orange

/** Where the user is in the feed, for the header's progress display. */
sealed interface FeedProgress {
    /** [filled] of [total] segments of run [runNumber] are done. */
    data class Run(val runNumber: Int, val filled: Int, val total: Int) : FeedProgress

    /** Fixed review list: "position / count". */
    data class Count(val position: Int, val count: Int) : FeedProgress
}

/**
 * Overlay header of the feed: back button, topic and mode, combo counter and run progress.
 * Translucent so the card underneath stays the focus.
 */
@Composable
fun FeedHeader(
    title: String,
    subtitle: String,
    progress: FeedProgress?,
    combo: Int,
    showCombo: Boolean,
    accent: CardStyle,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            AnimatedVisibility(
                visible = showCombo,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ComboChip(combo = combo)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        when (progress) {
            is FeedProgress.Run -> RunProgressBar(progress = progress, accent = accent)
            is FeedProgress.Count -> Text(
                text = "${progress.position} / ${progress.count}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
            null -> Unit
        }
    }
}

/** Stories-style bar: one segment per card of the current run. */
@Composable
private fun RunProgressBar(progress: FeedProgress.Run, accent: CardStyle) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Run ${progress.runNumber}: ${progress.filled} of ${progress.total} cards"
            }
    ) {
        repeat(progress.total) { index ->
            val color by animateColorAsState(
                targetValue = if (index < progress.filled) accent.accent else Color.White.copy(alpha = 0.18f),
                label = "runSegment"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ComboChip(combo: Int) {
    // Bounce every time the combo grows.
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(combo) {
        bounce.snapTo(1.3f)
        bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
    }
    Text(
        text = "🔥 x$combo",
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        modifier = Modifier
            .scale(bounce.value)
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Orange, Amber)))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = "$combo correct in a row" }
    )
}

/** One button of the right-hand action rail: a round icon with a short caption. */
@Composable
fun RailAction(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.28f))
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
