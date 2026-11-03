package com.kresna.aitopicapp.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.kresna.aitopicapp.util.TapZones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tap the right half of a feed page to go to the next card and the left half to go back.
 *
 * Buttons inside the page (quiz options, the flashcard, Keep going) consume their own taps first,
 * so this only fires on taps that land on empty space or plain text. Drags past touch slop cancel
 * the tap, so swiping and scrolling long cards still work.
 */
fun Modifier.tapToNavigate(pagerState: PagerState, scope: CoroutineScope, isRtl: Boolean): Modifier =
    pointerInput(pagerState, isRtl) {
        detectTapGestures { offset ->
            val zone = TapZones.zoneFor(offset.x, size.width.toFloat(), isRtl) ?: return@detectTapGestures
            // During a running animation, step on from where it is heading so quick taps add up.
            val from = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
            val target = TapZones.targetPage(zone, from, pagerState.pageCount) ?: return@detectTapGestures
            scope.launch { pagerState.animateScrollToPage(target) }
        }
    }
