package com.kresna.aitopicapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.GeneratedContent
import com.kresna.aitopicapp.ui.components.FeedActionRail
import com.kresna.aitopicapp.ui.components.FeedHeader
import com.kresna.aitopicapp.ui.components.FeedProgress
import com.kresna.aitopicapp.ui.components.FunFactCard
import com.kresna.aitopicapp.ui.components.GenerationStatusCard
import com.kresna.aitopicapp.ui.components.MCQCard
import com.kresna.aitopicapp.ui.components.QACard
import com.kresna.aitopicapp.ui.components.RunSummaryCard
import com.kresna.aitopicapp.ui.screens.content.GenerationStatus
import com.kresna.aitopicapp.ui.theme.AITopicLearningTheme
import com.kresna.aitopicapp.ui.theme.CardStyle
import com.kresna.aitopicapp.util.RunStats
import org.junit.Rule
import org.junit.Test

/**
 * Renders the learning feed to PNGs without a device (Paparazzi). Run `./gradlew testDebugUnitTest`;
 * images land in `app/build/reports/paparazzi/`. CI uploads them as the `screenshots` artifact.
 */
class FeedScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.NoActionBar"
    )

    private val quiz = GeneratedContent(
        id = 1,
        topicId = 1,
        contentType = ContentType.MCQ,
        question = "Why does a hash map offer O(1) average lookups?",
        options = listOf(
            "A. It keeps keys sorted",
            "B. It jumps straight to a bucket computed from the key's hash",
            "C. It caches the last key you looked up",
            "D. It stores every key in a balanced tree"
        ),
        correctIndex = 1,
        explanation = "The hash tells it which bucket to check, so only a few entries are compared. Sorting and trees give O(log n), not O(1)."
    )

    private val flashcard = GeneratedContent(
        id = 2,
        topicId = 1,
        contentType = ContentType.QA,
        question = "What problem does a bloom filter solve?",
        answer = "It answers \"is this item possibly in the set?\" using very little memory. It can give false positives but never false negatives, so it is great as a cheap check before an expensive lookup."
    )

    private val spark = GeneratedContent(
        id = 3,
        topicId = 1,
        contentType = ContentType.FUN_FACT,
        question = "Did you know?",
        answer = "The first computer \"bug\" was a real moth, found stuck in a relay of the Harvard Mark II in 1947.",
        explanation = "Grace Hopper's team log book, Smithsonian"
    )

    @Test
    fun quizUnanswered() = snapshot(CardStyle.Quiz, FeedProgress.Run(2, 3, 10)) {
        MCQCard(content = quiz, onAnswer = {})
    }

    @Test
    fun quizAnsweredCorrectlyWithCombo() = snapshot(CardStyle.Quiz, FeedProgress.Run(2, 4, 10), combo = 4) {
        MCQCard(content = quiz.copy(userAnswerIndex = 1), onAnswer = {})
    }

    @Test
    fun quizAnsweredWrong() = snapshot(CardStyle.Quiz, FeedProgress.Run(2, 4, 10)) {
        MCQCard(content = quiz.copy(userAnswerIndex = 2), onAnswer = {})
    }

    @Test
    fun flashcardFront() = snapshot(CardStyle.Flashcard, FeedProgress.Run(2, 5, 10)) {
        QACard(content = flashcard)
    }

    @Test
    fun flashcardBack() = snapshot(CardStyle.Flashcard, FeedProgress.Run(2, 5, 10)) {
        QACard(content = flashcard, initiallyFlipped = true)
    }

    @Test
    fun spark() = snapshot(CardStyle.Spark, FeedProgress.Run(2, 6, 10), bookmarked = true) {
        FunFactCard(content = spark)
    }

    @Test
    fun runSummary() = snapshot(CardStyle.Summary, FeedProgress.Run(2, 10, 10), rail = false) {
        RunSummaryCard(
            stats = RunStats(runNumber = 2, cardCount = 10, viewed = 10, answered = 6, correct = 5, bestStreak = 4),
            onKeepGoing = {},
            onDone = {}
        )
    }

    @Test
    fun generatingMore() = snapshot(CardStyle.Summary, FeedProgress.Run(3, 0, 10), rail = false) {
        GenerationStatusCard(status = GenerationStatus.Running, hasContent = true, onGenerate = {})
    }

    /** One feed screen: the card plus the overlay header and action rail, as in ContentPagerScreen. */
    private fun snapshot(
        accent: CardStyle,
        progress: FeedProgress,
        combo: Int = 0,
        rail: Boolean = true,
        bookmarked: Boolean = false,
        card: @Composable () -> Unit
    ) {
        paparazzi.snapshot {
            AITopicLearningTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    card()
                    FeedHeader(
                        title = "Data structures",
                        subtitle = "Mixed feed",
                        progress = progress,
                        combo = combo,
                        showCombo = combo >= 3,
                        accent = accent,
                        onBack = {},
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 28.dp)
                    )
                    if (rail) {
                        FeedActionRail(
                            isBookmarked = bookmarked,
                            onToggleBookmark = {},
                            onShare = {},
                            onCopy = {},
                            onStats = {},
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 40.dp)
                        )
                    }
                }
            }
        }
    }
}
