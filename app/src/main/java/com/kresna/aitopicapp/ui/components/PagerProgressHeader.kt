package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Compact "page x of n" header with a progress bar. Replaces the dot indicator,
 * which did not scale beyond a handful of pages.
 */
@Composable
fun PagerProgressHeader(
    currentPage: Int,
    pageCount: Int,
    viewedCount: Int,
    newItemsCount: Int,
    modifier: Modifier = Modifier
) {
    val position = (currentPage + 1).coerceAtMost(pageCount)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (currentPage >= pageCount) "End of list" else "$position / $pageCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (newItemsCount > 0) {
                Text(
                    text = "+$newItemsCount new",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = "$viewedCount seen",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (pageCount == 0) 0f else position.toFloat() / pageCount },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
