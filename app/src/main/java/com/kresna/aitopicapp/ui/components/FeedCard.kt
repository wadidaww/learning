package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kresna.aitopicapp.ui.theme.AppTheme
import com.kresna.aitopicapp.ui.theme.CardStyle

/** Space kept free on the right for the feed's action rail. */
val FeedRailWidth = 76.dp

/** Space kept free at the top for the feed's overlay header (back button, run progress). */
private val FeedHeaderHeight = 72.dp

/**
 * Full-screen page of the learning feed: a dark canvas lit by the card type's gradient, a type
 * label pill, and a content column that is centred when short and scrolls when long. Leaves room
 * for the overlay header at the top and the action rail on the right.
 */
@Composable
fun FeedCard(
    style: CardStyle,
    modifier: Modifier = Modifier,
    label: String = style.label,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val start = style.accent
    val end = style.accentEnd
    // A faint wash of the card colour: enough to tell card types apart, not enough to glare.
    val glow = if (AppTheme.palette.isDark) 0.16f else 0.12f
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + FeedHeaderHeight
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(background)
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(start.copy(alpha = glow), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.12f),
                        radius = size.maxDimension * 0.7f
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(end.copy(alpha = glow * 0.6f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.92f),
                        radius = size.maxDimension * 0.6f
                    )
                )
            }
    ) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = viewportHeight)
                    .padding(start = 20.dp, end = FeedRailWidth, top = top, bottom = bottom),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = horizontalAlignment
            ) {
                if (label.isNotEmpty()) {
                    TypePill(text = label, style = style)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                content()
            }
        }
    }
}

/** Small tinted label with the card type's icon, e.g. "QUICK QUIZ". */
@Composable
fun TypePill(text: String, style: CardStyle, modifier: Modifier = Modifier) {
    val accent = style.accent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.16f))
            .padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Icon(imageVector = style.icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = accent)
    }
}

/** Rounded-square tile holding a large icon, used as the hero image of a card. */
@Composable
fun IconBadge(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3.5f))
            .background(tint.copy(alpha = 0.16f))
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}
