package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun `includes topic, difficulty, count and recent existing items`() {
        val existing = (1..60).map { "Existing question $it" }
        val prompt = PromptBuilder.build(ContentType.MCQ, "Kotlin", "coroutines", Difficulty.ADVANCED, 10, existing)

        assertTrue(prompt.contains("Topic: Kotlin"))
        assertTrue(prompt.contains("coroutines"))
        assertTrue(prompt.contains(Difficulty.ADVANCED.promptHint))
        assertTrue(prompt.contains("exactly 10 multiple choice"))
        assertTrue(prompt.contains("Existing question 60"))
        assertFalse("only the most recent items are listed", prompt.contains("Existing question 1\n"))
    }

    @Test
    fun `omits empty sections`() {
        val prompt = PromptBuilder.build(ContentType.FUN_FACT, "Space", "", Difficulty.MIXED, 5)
        assertFalse(prompt.contains("Already covered"))
        assertFalse(prompt.contains("Learner's focus"))
    }
}
